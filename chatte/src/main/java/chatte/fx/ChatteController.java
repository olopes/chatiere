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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import chatte.config.ConfigService;
import chatte.msg.ChatMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.TypedMessage;
import chatte.resources.ResourceManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class ChatteController implements Initializable {
	// components injected by FXML
	
	// Views
	@FXML ListView<Friend> listView;
	@FXML WebView webView;
	@FXML WebView inputArea;

	// toolbar buttons
	@FXML Button preferencesBtn;
	@FXML Button emoticonBtn;
	@FXML Button addImgBtn;
	@FXML Button pasteBtn;
	@FXML Region leftSpring;
	@FXML Button clsBtn;
	@FXML Region rightSpring;
	@FXML Button sendBtn;
	@FXML Button snipBtn;
	
	// class specific stuff
	Stage mainWindow;
	Stage pickerStage;
	WebView pickerView;
	ResourceBundle bundle;
	Friend me;
	MessageBroker messageBroker;
	ResourceManager resourceManager;
	
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ChatteController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		this.messageBroker=messageBroker;
		this.resourceManager = resourceManager;
		me = configService.getSelf();
		this.messageBroker.addListener(this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;
		
		// Region springs
		HBox.setHgrow(leftSpring, Priority.ALWAYS);
		HBox.setHgrow(rightSpring, Priority.ALWAYS);
		
		WebEngine engine = inputArea.getEngine();
		engine.setJavaScriptEnabled(true);
		engine.load(getClass().getResource("InputView.html").toExternalForm());
		
		WebEngine viewEngine = webView.getEngine();
		viewEngine.setJavaScriptEnabled(true);
		viewEngine.load(getClass().getResource("ChatView.html").toExternalForm());
	}

	void doSendMessage() {
		WebEngine engine = inputArea.getEngine();
		String txtData = (String) engine.executeScript("document.body.innerHTML");
		// HTMLElement body = (HTMLElement) engine.getDocument().getElementsByTagName("body").item(0);
		log.fine(txtData);
		engine.executeScript("document.body.innerHTML=''");
		this.messageBroker.sendMessage(new TypedMessage(me, txtData, null));
	}
	
	// input
	
	@FXML 
	void checkKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			if(!event.isShiftDown()) {
				event.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
				doSendMessage();
			}
		}
	}
	
	// Menu actions
	
	@FXML 
	void doExit(ActionEvent event) {
		log.fine("Window close request");
		ChatteDialog dialog = new ChatteDialog(
				mainWindow,
				bundle.getString("dialog.close.title"),
				bundle.getString("dialog.close.message"),
				new String [] {
						bundle.getString("dialog.close.cancel"),
						bundle.getString("dialog.close.exit"),
				}
				);
		int selected = dialog.showDialog();
		if(selected == 1) {
			mainWindow.close();
		}

	}
	
	// Toolbar button actions
	
	@FXML
	void doOpenPreferences(ActionEvent event) {
		
	}
	
	@FXML
	void doOpenEmoticonPanel(ActionEvent event) {
		if(pickerStage == null) {
			pickerStage = new Stage();
			pickerStage.initOwner(mainWindow);
			pickerView = new WebView();

			pickerStage.setScene(new Scene(pickerView, 640, 400));

			WebEngine engine = pickerView.getEngine();
			engine.setJavaScriptEnabled(true);
			engine.getLoadWorker().stateProperty().addListener(
					new ChangeListener<State>() {
						@Override
						public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
							if (newState == State.SUCCEEDED) {
								JSObject htmlWindow = (JSObject) pickerView.getEngine().executeScript("window");
								htmlWindow.setMember("app", new JavascritpAdapter(pickerStage, ChatteController.this));
								htmlWindow.call("loadResources", (Object)resourceManager.getResources());
							}
						}
					});

		}
		pickerView.getEngine().load(getClass().getResource("EmoticonView.html").toExternalForm());
		pickerStage.show();
	}

	@FXML
	void doPasteImage(ActionEvent event) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		if(clipboard.hasContent(DataFormat.IMAGE)) {
			try {
				Image image = clipboard.getImage();
				java.awt.image.BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
				File tempFile = File.createTempFile("tmp_", ".png");
				ImageIO.write(bufferedImage, "png", tempFile);
				String resource = resourceManager.addResource(tempFile);
				tempFile.delete();

				appendInputImage(resource);
			} catch(Exception e) {
				log.log(Level.SEVERE, "Error copying image from clipboard", e);
			}
		}
		inputArea.requestFocus();
	}
	
	@FXML
	void doPanic(ActionEvent event) {
		WebEngine engine = webView.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("clearScreen");
		mainWindow.setIconified(true);
		inputArea.requestFocus();
	}
	
	@FXML 
	void doSend(ActionEvent event) {
		doSendMessage();
		inputArea.requestFocus();
	}
	
	public void appendInputImage(String resourceCode) {
		WebEngine engine = inputArea.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("appendImage", resourceCode);
	}
	
	public void messageTyped(TypedMessage msg) {
		chatMessageReceived(new ChatMessage(msg));
	}
	
	public void chatMessageReceived(final ChatMessage message) {
		WebEngine engine = webView.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("displayMessage", message);
		mainWindow.toFront();
	}

	public Stage getMainWindow() {
		return mainWindow;
	}

	public void setMainWindow(Stage mainWindow) {
		this.mainWindow = mainWindow;
	}

}
