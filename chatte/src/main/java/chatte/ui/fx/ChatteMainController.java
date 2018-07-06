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
package chatte.ui.fx;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import chatte.config.ConfigService;
import chatte.history.HistoryLogger;
import chatte.history.HistoryLoggerImpl;
import chatte.msg.ChatMessage;
import chatte.msg.ConnectedMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.StatusMessage;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;
import chatte.resources.ResourceManager;
import chatte.ui.UserColors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import netscape.javascript.JSObject;

public class ChatteMainController implements Initializable, ChatteContext, ChatteController {
	// components injected by FXML
	@FXML BorderPane chattefx;
	
	// Views
	@FXML ListView<Friend> listView;
	@FXML WebView webView;
	@FXML WebView inputArea;

	// Contact list buttons
	@FXML Region leftContactSpring;
	@FXML Button addContact;
	@FXML Button removeContact;
	@FXML Region rightContactSpring;

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
	
	// Controller factory used to create this controller
	@FXML ControllerFactory controllerFactory;

	// windows, dialogs and stuff
	PreferencesController preferencesController;
	ContactsController contactPanelController;
	NotifController notifPopupController;
	AlertController alertController;

	
	// class specific stuff
	Stage window;
	Stage pickerStage;
	WebView pickerView;
	ResourceBundle bundle;
	Friend me;
	MessageBroker messageBroker;
	ResourceManager resourceManager;
	HistoryLogger history;
	ConfigService configService;
	SoEnv soEnv;
	int currentColor = 0;
	
	private String lastStyle="odd";
	private Friend lastUser=null;

	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ChatteMainController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		this.messageBroker=messageBroker;
		this.resourceManager = resourceManager;
		this.history = new HistoryLoggerImpl(messageBroker, configService, resourceManager);
		this.configService=configService;
		this.me = configService.getSelf();
		this.messageBroker.addListener(this);
		soEnv = SoEnv.getEnv();
	}

	@Override
	public Friend getMyself() {
		return this.me;
	}
	
	@Override
	public ResourceBundle getResourceBundle() {
		return this.bundle;
	}

	@Override
	public MessageBroker getMessageBroker() {
		return this.messageBroker;
	}

	@Override
	public ResourceManager getResourceManager() {
		return this.resourceManager;
	}

	@Override
	public ConfigService getConfigService() {
		return this.configService;
	}

	@Override
	public ChatteController getMainController() {
		return this;
	}
	
	@Override
	public Parent getRoot() {
		return chattefx;
	}
	
	@Override
	public Window createWindow(Window parent) {
		return getWindow();
	}

	public Stage getWindow() {
		return window;
	}

	void setupWindows(Stage mainWindow) {
		this.window = mainWindow;
		ChatteControllerManager manager = new ChatteControllerManager(controllerFactory);
		// create notification popup

		// notifPopup = new NotifPopup();
		alertController = manager.newFxml("Alert.fxml", this);
		notifPopupController = manager.newFxml("NotifPopup.fxml", this);
		preferencesController = manager.newFxml("Preferences.fxml", this);
		contactPanelController = manager.newFxml("Contacts.fxml", this);
		
		
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bundle = resources;
		
		// Region springs
		HBox.setHgrow(leftSpring, Priority.ALWAYS);
		HBox.setHgrow(rightSpring, Priority.ALWAYS);
		
		// configure friend list
        listView.setCellFactory(new FriendCellFactory());
        List<Friend> knownFriends = configService.getKnownFriends();
        for(Friend f : knownFriends) {
        	f.setColor(UserColors.colors[currentColor]);
        	currentColor = (currentColor+1) % UserColors.colors.length;
        }
        listView.getItems().addAll(knownFriends);
		
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
		JSObject inputContents = (JSObject) engine.executeScript("getInputContents()");
		String txtData = (String) inputContents.getMember("text");
		log.fine(txtData);
		
		Set<String> resources = new HashSet<>();
		JSObject resourcesObject = (JSObject) inputContents.getMember("resources");
		int size = ((Number)resourcesObject.getMember("length")).intValue();
		for(int i = 0; i < size; i++) {
			resources.add((String)resourcesObject.getSlot(i));
		}
		log.fine("Resources: "+resources);
		engine.executeScript("document.body.innerHTML=''");
		this.messageBroker.sendMessage(new TypedMessage(me, txtData, resources));
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
				window,
				bundle.getString("dialog.close.title"),
				bundle.getString("dialog.close.message"),
				new String [] {
						bundle.getString("dialog.close.cancel"),
						bundle.getString("dialog.close.exit"),
				}
				);
		int selected = dialog.showDialog();
		if(selected == 1) {
			window.close();
		}

	}
	
	// Toolbar button actions
	
	@FXML
	void doRemoveContact(ActionEvent event) {
		List<Friend> selected = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
		for(Friend friend : selected) {
			if(friend == me) continue;
			configService.removeFriend(friend);
			listView.getItems().remove(friend);
		}
		
		listView.getSelectionModel().clearSelection();
	}
	
	@FXML
	void doOpenPreferences(ActionEvent event) throws Exception {
		preferencesController.showWindow(getWindow());
	}
	
	@FXML
	void doOpenEmoticonPanel(ActionEvent event) {
		if(pickerStage == null) {
			pickerStage = new Stage();
			pickerStage.initOwner(window);
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
								htmlWindow.setMember("app", new JavascritpAdapter(pickerStage, ChatteMainController.this));
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
		File selected = fileChooser.showOpenDialog(window);
		if(selected != null) {
			String newResource = resourceManager.addResource(selected);
			if(newResource != null)
				appendInputImage(newResource);
		}
	}
	
	@FXML
	void doPasteImage(ActionEvent event) {
		// mixing awt/swing/javafx... why? Why? WHY?
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
        	try {
        		java.awt.Image awtImage = (java.awt.Image) transferable.getTransferData(DataFlavor.imageFlavor);
    			java.awt.image.BufferedImage bufferedImage = null;
        		if(awtImage instanceof java.awt.image.BufferedImage) {
        			bufferedImage = (java.awt.image.BufferedImage) awtImage;
        		} else {
        			int w = awtImage.getWidth(null);
        			int h = awtImage.getHeight(null);
        			int type = java.awt.image.BufferedImage.TYPE_INT_RGB;  // other options
        			bufferedImage = new java.awt.image.BufferedImage(w, h, type);
        			java.awt.Graphics2D g2 = bufferedImage.createGraphics();
        			g2.drawImage(awtImage, 0, 0, null);
        			g2.dispose();
        		}
        		
        		bufferedImage.flush();
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
	void doOpenSnipingTool(ActionEvent event) {
		// kde -> spectacle
		// gnome -> gnome-screenshot -i
		// windows -> %windir%\system32\SnippingTool.exe
		
		String [] cmdline = soEnv.getCmd();
		if(cmdline.length == 0) return ; // nothing to run
		final ProcessBuilder processBuilder = new ProcessBuilder(cmdline).inheritIO();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					processBuilder.start();
				} catch (IOException e) {
	        		log.log(Level.SEVERE, "Error launching snipping tool", e);
				}
			}
		}).start();
	}
	
	@FXML
	void doPanic(ActionEvent event) {
		WebEngine engine = webView.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("clearScreen");
		window.setIconified(true);
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

		Date received = new Date();
		boolean newUser = false;
		if(lastUser != msg.getFrom()) {
			newUser = true;
			lastUser = msg.getFrom();
			lastStyle = "even".equals(lastStyle)?"odd":"even";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(lastStyle).append("\">");
		if(newUser) {
			String friendColor = lastUser.getColor();
			sb.append("<div class=\"from\" style=\"color:").append(friendColor).append(";\">")
			.append(msg.getFrom().getNick())
			.append("</div>");
		}

		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		sb
		.append("<div class=\"message\">").append(msg.getMessage()).append(' ')
		.append("<span class=\"timestamp\">").append(fmt.format(received)).append("</span> ")
		
		.append("</div>")
		.append("</div>");
		// sb.append("<div><img src=\"").append(new File("img/hello.gif").toURI().toURL()).append("\" /></div>");

		return sb.toString();
	}

	
	public void chatMessageReceived(final ChatMessage message) {
		WebEngine engine = webView.getEngine();
		
		String htmlMessage = convertChatMessage(message);
		
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("displayMessage", htmlMessage);
		history.recordMessage(htmlMessage, message.getResourceRefs());
		
		notifPopupController.show(window, message.getNick(), message.getMessage());
		// mainWindow.toFront();
	}
	
	public void displayStatusMessage(StatusMessage msg) {
		log.info("Status message received: "+msg);
		WebEngine engine = webView.getEngine();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(msg.getStatus()).append("\">");
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		sb.append("<span class=\"timestamp\">").append(fmt.format(new Date())).append("</span> ")
		.append(msg.getFrom())
		.append(" &lt;").append(msg.getStatus()).append("&gt;</div>");

		String htmlMessage = sb.toString();
		
		JSObject htmlWindow = (JSObject) engine.executeScript("window");
		htmlWindow.call("displayMessage", htmlMessage);
		history.recordMessage(htmlMessage, null);
		window.toFront();
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
