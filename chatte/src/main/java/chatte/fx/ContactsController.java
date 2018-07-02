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
