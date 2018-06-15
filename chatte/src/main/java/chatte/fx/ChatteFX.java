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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatteFX extends Application {

	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}


	MessageBroker messageBroker;
	ResourceManager resourceManager;
	ConfigService configService;
	ResourceBundle resources;

	public static void main(String [] args) {
		// Configure SSL context
		System.setProperty("javax.net.ssl.keyStore","ssl.key");
		System.setProperty("javax.net.ssl.keyStorePassword","ssl.key");
		System.setProperty("javax.net.ssl.trustStore","ssl.key");
		System.setProperty("javax.net.ssl.trustStorePassword","ssl.key");
		
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

		resources = ResourceBundle.getBundle("chatte.fx.Bundle"); 

		configService = new ConfigServiceImpl();
		resourceManager = new ChatoResourceManager();
		messageBroker = new ChatoMessageBroker(resourceManager);

		// RegisterCustom URL handler
		ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler(resourceManager, messageBroker);
		messageBroker.addListener(resourceRequestHandler);
		URL.setURLStreamHandlerFactory(new ChatoURLStreamHandlerFactory(resourceManager));

		// start network listener
		// new Thread(new chatte.net.MsgListener(messageBroker, configService), "MsgListener").start();
		
		// register "custom" font
		Font.loadFont(getClass().getResource("OpenSansEmoji.ttf").toExternalForm(), 12);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatteFX.fxml"), resources);
		Parent root = (Parent)loader.load();
		ChatteController controller = loader.getController();
		controller.configure(configService, resourceManager, messageBroker);

		// register UI message listener
		messageBroker.addListener(new UIMessageListener(controller));

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("ChatteFX.css").toExternalForm());

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				log.fine("Window close request");
			}
		});
		primaryStage.setTitle("ChatteFX");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon/cat-upsidedown-icon.png")));
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception {
		log.info("Stopping stuff");
		messageBroker.sendMessage(new StopServicesMessage());
		super.stop();
	}

}
