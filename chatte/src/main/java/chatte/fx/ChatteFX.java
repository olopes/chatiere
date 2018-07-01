/*
 * MIT License
 * 
 * Copyright (c) 2018 OLopes
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package chatte.fx;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import chatte.config.ConfigService;
import chatte.config.ConfigServiceImpl;
import chatte.msg.ChatoMessageBroker;
import chatte.msg.MessageBroker;
import chatte.msg.StopServicesMessage;
import chatte.net.ChatoURLStreamHandlerFactory;
import chatte.resources.ChatoResourceManager;
import chatte.resources.ResourceManager;
import chatte.resources.ResourceRequestHandler;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ChatteFX extends Application {

	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	boolean passwordSet = false;
	MessageBroker messageBroker;
	ResourceManager resourceManager;
	ConfigService configService;
	ResourceBundle resourceBundle;
	ControllerFactory controllerFactory;
	
	public static void main(String [] args) {
		
		// Configure logging
		if(!System.getProperties().containsKey("java.util.logging.config.file")) {
			if(new java.io.File("logging.properties").exists()) {
				System.setProperty("java.util.logging.config.file","logging.properties");
			} else {
				new java.io.File("logs").mkdirs();
				try {
					LogManager.getLogManager().readConfiguration(ChatteFX.class.getResourceAsStream("/logging.properties"));
				} catch (SecurityException | IOException e) {
					System.err.println("Error during java logging configuration");
					e.printStackTrace();
				}
			}
		}
		Logger.getLogger("chatte.fx.ChatteFX").severe("Starting ChatteFX");
		launch(ChatteFX.class, args);
	}

	@Override
	public void init() throws Exception {
		super.init();

		// Application.Parameters parameters = getParameters();

		resourceBundle = ResourceBundle.getBundle("chatte.fx.Bundle"); 

		configService = new ConfigServiceImpl();
		resourceManager = new ChatoResourceManager();
		messageBroker = new ChatoMessageBroker(resourceManager);

		// RegisterCustom URL handler
		ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler(resourceManager, messageBroker);
		messageBroker.addListener(resourceRequestHandler);
		URL.setURLStreamHandlerFactory(new ChatoURLStreamHandlerFactory(resourceManager));

		// start network listener
		// new Thread(new chatte.net.MsgListener(messageBroker, configService), "MsgListener").start();
		
		controllerFactory = new ControllerFactory(configService, resourceManager, messageBroker);
		
		// register "custom" font
		Font.loadFont(getClass().getResource("OpenSansEmoji.ttf").toExternalForm(), 12);
	}

    private boolean setStorePassword(final String password) {
    	passwordSet = isGoodPassword("ssl.key", password);
		// Configure SSL context
		System.setProperty("javax.net.ssl.keyStore","ssl.key");
		// System.setProperty("javax.net.ssl.keyStorePassword","ssl.key");
		System.setProperty("javax.net.ssl.keyStorePassword",password);
		System.setProperty("javax.net.ssl.trustStore","ssl.key");
		// System.setProperty("javax.net.ssl.trustStorePassword","ssl.key");
		System.setProperty("javax.net.ssl.trustStorePassword",password);
        log.info("Key configured!");
        return passwordSet;
    }

	
	private boolean isGoodPassword(String storeFile, String password) {
		try (FileInputStream in = new FileInputStream(storeFile)){
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			store.load(in, password.toCharArray());
			log.info("KeyStore successfuly loaded");
			return true;
		} catch(Exception e) {
			log.info("Bad password");
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatteFX.fxml"), resourceBundle);
		loader.setControllerFactory(controllerFactory);
		Parent root = (Parent)loader.load();
		ChatteController controller = loader.getController();
		controller.setMainWindow(primaryStage);
		

		// register UI message listener
		messageBroker.addListener(new UIMessageListener(controller));

		Scene scene = new Scene(root);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				log.fine("Window close request");
				ChatteDialog dialog = new ChatteDialog(
						primaryStage,
						resourceBundle.getString("dialog.close.title"),
						resourceBundle.getString("dialog.close.message"),
						new String [] {
								resourceBundle.getString("dialog.close.cancel"),
								resourceBundle.getString("dialog.close.exit"),
						}
						);
				int selected = dialog.showDialog();
				if(selected != 1) {
					// exit was not selected. Consume the event to prevent window from closing
					event.consume();
				}
			}
		});
		primaryStage.setTitle("ChatteFX");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon/cat-upsidedown-icon.png")));
		primaryStage.setScene(scene);
		primaryStage.show();

		// Ask for keystore password. It won't connect to the network is the password is wrong
		Stage passwordStage = new Stage(StageStyle.UTILITY);
		passwordStage.initOwner(primaryStage);
		passwordStage.initModality(Modality.WINDOW_MODAL);
		createLoginScene(passwordStage);
		passwordStage.showAndWait();
		if(!passwordSet) {
			// remove event handler
			primaryStage.setOnCloseRequest(null);
			primaryStage.hide();
			return;
		}
	}

	@Override
	public void stop() throws Exception {
		log.info("Stopping stuff");
		messageBroker.sendMessage(new StopServicesMessage());
		super.stop();
	}
	
    private Scene createLoginScene(final Stage stage) {
        VBox vbox = new VBox(3);
        vbox.setPadding(new Insets(10.0));
 
        final Label loginLabel = new Label();
        loginLabel.setText("name");
        
        final PasswordField passwordBox = new PasswordField();
        passwordBox.setPromptText("password");
        
        final Button button = new Button("Log in");
        button.setDefaultButton(true);
        button.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent t) {
                // Save credentials
                String password = passwordBox.getText();
                // Do not allow any further edits
                passwordBox.setEditable(false);
                button.setDisable(true);
                
                if(setStorePassword(password)) {
                	new chatte.net.MsgListener(messageBroker, configService);
                	log.info("Good password :-)");
                    // Hide if app is ready
                    stage.close();
                } else {
                	log.log(Level.SEVERE, "BAD PASSWORD!");
                	// enable edits and show 
                    passwordBox.setEditable(true);
                    button.setDisable(false);
                }
                
            }
        });
        HBox buttonHolder = new HBox();
        buttonHolder.setAlignment(Pos.TOP_CENTER);
        buttonHolder.getChildren().add(button);
        vbox.getChildren().addAll(loginLabel, passwordBox, buttonHolder);
        
        Scene sc = new Scene(vbox);//, 200, 200);
        stage.setScene(sc);
        return sc;
    }
    


}
