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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import chatte.config.ConfigService;
import chatte.msg.ChatMessage;
import chatte.msg.ConnectedMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.StatusMessage;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
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
	
	private String lastStyle="odd";
	private Friend lastUser=null;
	private Date lastReceived = new Date();

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

	public Stage getMainWindow() {
		return mainWindow;
	}

	public void setMainWindow(Stage mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	private static class FriendStringConverter extends StringConverter<Friend> {
		@Override
		public Friend fromString(String string) {
			return null;
		}
		
		@Override
		public String toString(Friend object) {
			return object.getNick();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;
		
		// Region springs
		HBox.setHgrow(leftSpring, Priority.ALWAYS);
		HBox.setHgrow(rightSpring, Priority.ALWAYS);
		
		// configure friend list
        listView.setCellFactory(new Callback<ListView<Friend>, ListCell<Friend>>() {
        	@Override
        	public ListCell<Friend> call(ListView<Friend> param) {
        		final TextFieldListCell<Friend> cell = new TextFieldListCell<>(new FriendStringConverter());
        		cell.indexProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						if(oldValue != null) { 
							int oldIdx = oldValue.intValue()%10;
							cell.getStyleClass().remove("friend"+oldIdx);
						}
						int newIdx = newValue.intValue()%10;
						cell.getStyleClass().add("friend"+newIdx);
					}
				});
        		cell.itemProperty().addListener(new ChangeListener<Friend>() {
					@Override
					public void changed(ObservableValue<? extends Friend> observable, Friend oldValue, Friend newValue) {
						if(newValue == null)
							cell.getTooltip().setText("");
						else
							cell.getTooltip().setText(newValue.getNick()+"@"+newValue.getHost());
					}
				});
        		cell.setTooltip(new Tooltip(""));
        		log.finer("Cell created");
        		return cell;
        	}
        });
        listView.getItems().add(me);
		
		// configure input area
		WebEngine engine = inputArea.getEngine();
		inputArea.setContextMenuEnabled(false);
		engine.setJavaScriptEnabled(true);
		engine.load(getClass().getResource("InputView.html").toExternalForm());
		
		// configure conversation area
		WebEngine viewEngine = webView.getEngine();
		webView.setContextMenuEnabled(false);
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
			pickerView.setContextMenuEnabled(false);
			
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
	void doAddImage(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(bundle.getString("dialog.addEmoticon.title"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(bundle.getString("dialog.addEmoticon.filter"), resourceManager.getValidFileExtensions()));
		File selected = fileChooser.showOpenDialog(mainWindow);
		if(selected != null) {
			String newResource = resourceManager.addResource(selected);
			if(newResource != null)
				appendInputImage(newResource);
		}
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
	
	String convertChatMessage(final ChatMessage msg) {

		System.out.println("Chat message recieved from "+msg.getFrom());
		System.out.println(msg.getMessage());
		
		Date received = new Date();
		boolean newUser = false, newDate = false;
		if(lastUser != msg.getFrom()) {
			newUser = true;
			lastUser = msg.getFrom();
			lastStyle = "even".equals(lastStyle)?"odd":"even";
		}
		
		if(newUser || received.getTime()-lastReceived.getTime() > 60000L /*300000L*/) {
			lastReceived= received;
			newDate = true;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(lastStyle).append("\">");
		if(newUser || newDate) {
			int idx = listView.getItems().indexOf(msg.getFrom())%10;
			String friendClass = "friend"+idx;
			SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
			sb.append("<div class=\"from ").append(friendClass).append("\">")
			.append("<span class=\"timestamp\">").append(fmt.format(new Date())).append("</span> ")
			.append(msg.getFrom().getNick())
			.append("</div>");
		}

		sb
		.append("<div class=\"message\">").append(msg.getMessage()).append("</div>")
		.append("</div>");
		// sb.append("<div><img src=\"").append(new File("img/hello.gif").toURI().toURL()).append("\" /></div>");
		// System.out.println(sb);
		return sb.toString();
	}

	
	public void chatMessageReceived(final ChatMessage message) {
		WebEngine engine = webView.getEngine();
		
		String htmlMessage = convertChatMessage(message);
		
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("displayMessage", htmlMessage);
		mainWindow.toFront();
	}
	
	public void displayStatusMessage(StatusMessage message) {
		log.info("Status message received: "+message);
	}

	public void welcomeFriend(final WelcomeMessage message) {
		// new friend just connected. Update Clients panel
		final Friend friend = message.getFrom();
		if(!listView.getItems().contains(friend)) {
			listView.getItems().add(friend);
		}

		displayStatusMessage(new ConnectedMessage(friend));
	}
	
	public void byebyeFriend(final DisconnectedMessage message) {
		// new friend just disconnected.
		Friend friend = message.getFrom();
		listView.getItems().remove(friend);
		displayStatusMessage(message);
	}
	
}
