package chatte.fx;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import chatte.config.ConfigService;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.NewFriendMessage;
import chatte.resources.ResourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ContactsController implements Initializable {
	
	@FXML VBox contactsPanel;
	@FXML TextField nick;
	@FXML TextField host;
	@FXML TextField port;
	
	@FXML Button okButton;

	ConfigService configService;
	MessageBroker messageBroker;
	
	private final Logger log;
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ContactsController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		log = getLogger();
		this.configService = configService;
		this.messageBroker = messageBroker;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	@FXML 
	void doConnect(ActionEvent event) {
		log.fine("Window close request");
		Friend friend = new Friend();
		friend.setNick(nick.getText());
		friend.setHost(host.getText());
		friend.setPort(Integer.parseInt(port.getText()));
		configService.addFriend(friend);
		messageBroker.sendMessage(new NewFriendMessage(friend));
	}
	
}
