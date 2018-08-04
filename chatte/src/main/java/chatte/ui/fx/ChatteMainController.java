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
import java.awt.datatransfer.UnsupportedFlavorException;
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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
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
import javafx.util.Callback;
import netscape.javascript.JSObject;

public class ChatteMainController implements Initializable, ChatteContext, ChatteController, Callback<String,Boolean> {
	// components injected by FXML
	@FXML BorderPane chattefx;
	
	// Views
	@FXML ListView<Friend> listView;
	@FXML WebView webView;
	@FXML WebView inputArea;

	// context menu for the listview
    MenuItem addNewFriend, modifyFriend, removeFriend;

	// toolbar buttons
	@FXML Region leftSpring;
	@FXML Region rightSpring;
	
	// Controller factory used to create this controller
	@FXML ControllerFactory controllerFactory;

	// windows, dialogs and stuff
	PreferencesController preferencesController;
	ContactsController contactPanelController;
	NotifController notifPopupController;
	AlertController alertController;
	EmojiController emojiController;
	StorePasswordController passwordController;

	
	// class specific stuff
	ChatteFX application;
	Stage window;
	ResourceBundle bundle;
	Friend me;
	MessageBroker messageBroker;
	ResourceManager resourceManager;
	HistoryLogger history;
	ConfigService configService;
	SoEnv soEnv;
	int currentColor = 0;
	
	private String lastStyle="odd"; //$NON-NLS-1$
	private Friend lastUser=null;

	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ChatteMainController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker, Application application) {
		this.application = (ChatteFX)application;
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
	public Window createWindow(ChatteController parent) {
		return getWindow();
	}

	@Override
	public Stage getWindow() {
		return window;
	}

	@Override
	public Boolean call(String url) {
		log.fine("Open URL with external browser: "+url);
		doOpenURLWithBrowser(url);
		// Always return false because the confirm handler is used to open links on click.
		return false;
	}
	
	void setupWindows(Stage mainWindow) {
		
		webView.getEngine().setConfirmHandler(this);
		inputArea.getEngine().setConfirmHandler(this);
		
		this.window = mainWindow;
		ChatteControllerManager manager = new ChatteControllerManager(controllerFactory);
		// create notification popup

		// notifPopup = new NotifPopup();
		alertController = manager.newFxml("Alert.fxml", this); //$NON-NLS-1$
		notifPopupController = manager.newFxml("NotifPopup.fxml", this); //$NON-NLS-1$
		preferencesController = manager.newFxml("Preferences.fxml", this); //$NON-NLS-1$
		contactPanelController = manager.newFxml("Contacts.fxml", this); //$NON-NLS-1$
		emojiController = manager.newFxml("EmojiSelection.fxml", this); //$NON-NLS-1$
		
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
        configService.getSelf().setColor(UserColors.colors[0]);
        listView.getItems().add(me);
        currentColor=1;
        for(Friend f : knownFriends) {
        	f.setColor(UserColors.colors[currentColor]);
        	currentColor = (currentColor+1) % UserColors.colors.length;
        }
        listView.getItems().addAll(knownFriends);
        
        addNewFriend = new MenuItem();
        addNewFriend.setText(resources.getString("menuitem.addFriend"));
        addNewFriend.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				doAddNewFriend(event);
			}
		});
        modifyFriend = new MenuItem();
        modifyFriend.setText(resources.getString("menuitem.modifyFriend"));
        modifyFriend.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				doModifyFriend(event);
			}
		});
        removeFriend = new MenuItem();
        removeFriend.setText(resources.getString("menuitem.removeFriend"));
        removeFriend.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				doRemoveFriend(event);
			}
		});
        
        listView.setContextMenu(new ContextMenu(addNewFriend, modifyFriend, removeFriend));
        
		// configure input area
		WebEngine engine = inputArea.getEngine();
		inputArea.setContextMenuEnabled(false);
		engine.setJavaScriptEnabled(true);
		engine.load(getClass().getResource("InputView.html").toExternalForm()); //$NON-NLS-1$
		
		// configure conversation area
		WebEngine viewEngine = webView.getEngine();
		webView.setContextMenuEnabled(false);
		viewEngine.setJavaScriptEnabled(true);
		viewEngine.load(getClass().getResource("ChatView.html").toExternalForm()); //$NON-NLS-1$

	}

	void doOpenURLWithBrowser(final String url) {
		log.fine("Open URL with external browser: "+url);
		// they say this might prevent JVM crashes?
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				application.getHostServices().showDocument(url);
			}
		});
	}
	
	void doSendMessage() {
		WebEngine engine = inputArea.getEngine();
		JSObject inputContents = (JSObject) engine.executeScript("getInputContents()"); //$NON-NLS-1$
		String txtData = (String) inputContents.getMember("text"); //$NON-NLS-1$
		log.fine(txtData);
		
		Set<String> resources = new HashSet<>();
		JSObject resourcesObject = (JSObject) inputContents.getMember("resources"); //$NON-NLS-1$
		int size = ((Number)resourcesObject.getMember("length")).intValue(); //$NON-NLS-1$
		for(int i = 0; i < size; i++) {
			resources.add((String)resourcesObject.getSlot(i));
		}
		log.fine("Resources: "+resources); //$NON-NLS-1$
		engine.executeScript("document.body.innerHTML=''"); //$NON-NLS-1$
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
		log.fine("Window close request"); //$NON-NLS-1$
		ChatteDialog dialog = new ChatteDialog(
				window,
				bundle.getString("dialog.close.title"), //$NON-NLS-1$
				bundle.getString("dialog.close.message"), //$NON-NLS-1$
				new String [] {
						bundle.getString("dialog.close.cancel"), //$NON-NLS-1$
						bundle.getString("dialog.close.exit"), //$NON-NLS-1$
				}
				);
		int selected = dialog.showDialog();
		if(selected == 1) {
			window.close();
		}

	}
	
	// Toolbar button actions
	
	@FXML
	void onListViewContextMenu(ContextMenuEvent event) {
		Friend selected = listView.getSelectionModel().getSelectedItem();
		boolean disableDependents = (selected == null || selected.isMyself());
		removeFriend.setDisable(disableDependents);
		modifyFriend.setDisable(disableDependents);
		// do not consume the event and allow propagation
	}
	
	
	@FXML
	void doAddNewFriend(ActionEvent event) {
		contactPanelController.showWindow(getMainController());
	}
	
	@FXML
	void doModifyFriend(ActionEvent event) {
		Friend selectedFriend = listView.getSelectionModel().getSelectedItem();
		if(selectedFriend == null || selectedFriend.isMyself()) return;
		contactPanelController.showWindow(getMainController(), selectedFriend);
	}
	
	@FXML
	void doRemoveFriend(ActionEvent event) {
		Friend selected = listView.getSelectionModel().getSelectedItem();
		if(selected == null || selected.isMyself()) return;
		
		
		
	}
	
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
		preferencesController.showWindow(this);
	}
	
	@FXML
	void doOpenEmoticonPanel(ActionEvent event) {
		emojiController.showWindow(this);
	}

	@FXML
	void doAddImage(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(bundle.getString("dialog.addEmoticon.title")); //$NON-NLS-1$
		fileChooser.getExtensionFilters().add(new ExtensionFilter(bundle.getString("dialog.addEmoticon.filter"), resourceManager.getValidFileExtensions())); //$NON-NLS-1$
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
        if(transferable == null) return;
        if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				doPasteDropFiles(files);
			} catch (UnsupportedFlavorException | IOException e) {
        		log.log(Level.SEVERE, "Transfer files from clipboard failed", e);
			}
        } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
        	try  {
        		java.awt.Image awtImage = (java.awt.Image) transferable.getTransferData(DataFlavor.imageFlavor);
        		doPasteDropImage(awtImage);
        	} catch(Exception e) {
        		log.log(Level.SEVERE, "Could not transfer text from clipboard", e);
        	}
        } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        	try  {
        		String clipboardText = (String) transferable.getTransferData(DataFlavor.stringFlavor);
        		doPasteDropString(clipboardText);
        	} catch(Exception e) {
        		log.log(Level.SEVERE, "Could not transfer text from clipboard", e);
        	}
		}
		inputArea.requestFocus();
	}
	
	void doPasteDropImage(java.awt.Image awtImage) {
    	try {
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
    		File tempFile = File.createTempFile("tmp_", ".png"); //$NON-NLS-1$ //$NON-NLS-2$
    		ImageIO.write(bufferedImage, "png", tempFile); //$NON-NLS-1$
    		String resource = resourceManager.addResource(tempFile);
    		tempFile.delete();

    		appendInputImage(resource);

    	} catch(Exception e) {
    		log.log(Level.SEVERE, "Error copying image from clipboard", e); //$NON-NLS-1$
    	}

	}
	void doPasteDropImage(Image image) {
		doPasteDropImage(SwingFXUtils.fromFXImage(image, null));
	}
	
	void doPasteDropFiles(List<File> files) {
    	if(files == null) return;

		for(File f : files) {
			if(!resourceManager.isValidResourceFile(f)) continue;
			String res = resourceManager.addResource(f);
			appendInputImage(res);
		}
	}
	
	void doPasteDropString(String str) {
    	appendText(str);
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
	        		log.log(Level.SEVERE, "Error launching snipping tool", e); //$NON-NLS-1$
				}
			}
		}).start();
	}
	
	@FXML
	void doPanic(ActionEvent event) {
		WebEngine engine = webView.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window"); //$NON-NLS-1$
		htmlWindow.call("clearScreen"); //$NON-NLS-1$
		window.setIconified(true);
		inputArea.requestFocus();
	}
	
	@FXML 
	void doSend(ActionEvent event) {
		doSendMessage();
		inputArea.requestFocus();
	}
	
	@FXML
	void handleDrop(DragEvent event) {
		// Region target = (Region)event.getGestureTarget();
        Dragboard db = event.getDragboard();
        boolean success = false;
        if(db.hasFiles()) {
        	log.fine("Paste files");
        	doPasteDropFiles(db.getFiles());
        	success=true;
        } else if(db.hasImage()) {
        	log.fine("Paste image");
        	doPasteDropImage(db.getImage());
        	success=true;
        } else if(db.hasHtml()) {
        	log.fine("Paste html");
        	doPasteDropString(db.getHtml());
        	success=true;
        } else if(db.hasString()) {
        	log.fine("Paste String");
        	doPasteDropString(db.getString());
        	success=true;
        }

//        if (db.hasString()) {
//            dropped.setText(db.getString());
//            success = true;
//        }
        /* let the source know whether the string was successfully 
         * transferred and used */
        event.setDropCompleted(success);
        // target.getStyleClass().removeAll("drag-accept", "drag-reject");

        event.consume();
	}
	
	@FXML
    void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
        	log.fine("Tem files");
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        } else if (db.hasImage()) {
        	log.fine("Tem Image");
        	event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        } else if (db.hasHtml()) {
        	log.fine("Tem HTML");
        	event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        } else if (db.hasString()) {
        	log.fine("Tem String");
        	event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

	
	boolean acceptFiles(List<File> files) {
		if(null == files || files.isEmpty()) return false;
		for(File f : files) {
			if(!resourceManager.isValidResourceFile(f))
				return false;
		}
		return true;
	}
	
//	@FXML
//	void handleDragEnter(DragEvent event) {
////		Region target = (Region)event.getGestureTarget();
////        Dragboard db = event.getDragboard();
////		if(db.hasImage())
////			target.getStyleClass().add("drag-accept");
////		else if(db.hasFiles() && acceptFiles(db.getFiles()))
////			target.getStyleClass().add("drag-accept");
////		else if(db.hasString())
////			target.getStyleClass().add("drag-accept");
////		else
////			target.getStyleClass().add("drag-reject");
////		
////        event.consume();
//	}
//	
//	@FXML
//	void handleDragExit(DragEvent event) {
////		Region target = (Region)event.getGestureTarget();
////		target.getStyleClass().removeAll("drag-accept", "drag-reject");
////		event.consume();
//	}
//	
	public void appendInputImage(String resourceCode) {
		WebEngine engine = inputArea.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window"); //$NON-NLS-1$
		htmlWindow.call("appendImage", resourceCode); //$NON-NLS-1$
	}
	
	public void appendEmoji(String emoji) {
		WebEngine engine = inputArea.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window"); //$NON-NLS-1$
		htmlWindow.call("appendEmoji", emoji); //$NON-NLS-1$
	}
	
	public void appendText(String text) {
		WebEngine engine = inputArea.getEngine();
		JSObject htmlWindow = (JSObject) engine.executeScript("window"); //$NON-NLS-1$
		htmlWindow.call("appendText", text); //$NON-NLS-1$
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
			lastStyle = "even".equals(lastStyle)?"odd":"even"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(lastStyle).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$
		if(newUser) {
			String friendColor = lastUser.getColor();
			sb.append("<div class=\"from\" style=\"color:").append(friendColor).append(";\">") //$NON-NLS-1$ //$NON-NLS-2$
			.append(msg.getFrom().getNick())
			.append("</div>"); //$NON-NLS-1$
		}

		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		sb
		.append("<div class=\"message\">").append(msg.getMessage()).append(' ') //$NON-NLS-1$
		.append("<span class=\"timestamp\">").append(fmt.format(received)).append("</span> ") //$NON-NLS-1$ //$NON-NLS-2$
		
		.append("</div>") //$NON-NLS-1$
		.append("</div>"); //$NON-NLS-1$
		// sb.append("<div><img src=\"").append(new File("img/hello.gif").toURI().toURL()).append("\" /></div>");

		return sb.toString();
	}

	
	public void chatMessageReceived(final ChatMessage message) {
		WebEngine engine = webView.getEngine();
		
		String htmlMessage = convertChatMessage(message);
		
		JSObject htmlWindow = (JSObject) engine.executeScript("window"); //$NON-NLS-1$
		htmlWindow.call("displayMessage", htmlMessage); //$NON-NLS-1$
		history.recordMessage(htmlMessage, message.getResourceRefs());
		
		notifPopupController.show(this, message.getNick(), message.getMessage());
		// mainWindow.toFront();
	}
	
	public void displayStatusMessage(StatusMessage msg) {
		log.info("Status message received: "+msg); //$NON-NLS-1$
		WebEngine engine = webView.getEngine();
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"").append(msg.getStatus()).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
		sb.append("<span class=\"timestamp\">").append(fmt.format(new Date())).append("</span> ") //$NON-NLS-1$ //$NON-NLS-2$
		.append(msg.getFrom())
		.append(" &lt;").append(msg.getStatus()).append("&gt;</div>"); //$NON-NLS-1$ //$NON-NLS-2$

		String htmlMessage = sb.toString();
		
		JSObject htmlWindow = (JSObject) engine.executeScript("window"); //$NON-NLS-1$
		htmlWindow.call("displayMessage", htmlMessage); //$NON-NLS-1$
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
