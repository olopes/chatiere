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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import chatte.config.ConfigService;
import chatte.msg.ChatMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.TypedMessage;
import chatte.resources.ResourceManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class ChatteController implements Initializable {
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
	
	ResourceBundle bundle;
	Friend me;
	MessageBroker messageBroker;
	
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public void configure(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		this.messageBroker=messageBroker;
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

	@FXML 
	void sendButtonClick(ActionEvent event) {
		doSendMessage();
	}

	@FXML 
	void checkKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			if(!event.isShiftDown()) {
				event.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
				doSendMessage();
			}
		}
	}
	
	void doSendMessage() {
		WebEngine engine = inputArea.getEngine();
		String txtData = (String) engine.executeScript("document.body.innerHTML");
		// HTMLElement body = (HTMLElement) engine.getDocument().getElementsByTagName("body").item(0);
		log.fine(txtData);
		engine.executeScript("document.body.innerHTML=''");
		this.messageBroker.sendMessage(new TypedMessage(me, txtData, null));
	}
	
	@FXML 
	void doExit(ActionEvent event) {
		log.info("Close menuitem clicked");
		Platform.exit();
	}
	
	public void messageTyped(TypedMessage msg) {
		chatMessageReceived(new ChatMessage(msg));
	}
	
	public void chatMessageReceived(final ChatMessage message) {
		WebEngine engine = webView.getEngine();
		JSObject displayMessage = (JSObject) engine.executeScript("window");
		displayMessage.call("displayMessage", message);
	}
}
