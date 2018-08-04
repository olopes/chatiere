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

import java.net.URL;
import java.util.ResourceBundle;

import chatte.config.ConfigService;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.NewFriendMessage;
import chatte.resources.ResourceManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ContactsController extends BaseChatteController implements ChangeListener<String> {
	
	@FXML VBox contactsPanel;
	@FXML TextField nick;
	@FXML TextField host;
	@FXML TextField port;
	
	@FXML Button okButton;

	boolean approved;
	Friend friend;
	
	public ContactsController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		super(configService, resourceManager, messageBroker);
	}
	
	@Override
	public void initialize(URL baseURL, ResourceBundle bundle) {
		super.initialize(baseURL, bundle);
		port.textProperty().addListener(this);
	}

	@FXML 
	void onOkButton(ActionEvent event) {
		log.fine("Ok Button clicked"); //$NON-NLS-1$
		Friend friend = new Friend();
		friend.setNick(nick.getText());
		friend.setHost(host.getText());
		friend.setPort(Integer.parseInt(port.getText()));
		configService.addFriend(friend);
		messageBroker.sendMessage(new NewFriendMessage(friend));
		Stage stage = (Stage)contactsPanel.getScene().getWindow();
		stage.close();
	}

	@Override
	public Parent getRoot() {
		return contactsPanel;
	}

	@Override
	public void showWindow(ChatteController owner) {
		showWindow(owner, null);
	}
	
	public void showWindow(ChatteController owner, Friend friend) {
		if(friend != null) {
			// modification mode
			nick.setText(friend.getNick());
			host.setText(friend.getHost());
			port.setText(String.valueOf(friend.getPort()));
		}
		super.showWindow(owner);
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if (!newValue.matches("\\d*")) { //$NON-NLS-1$
			port.setText(newValue.replaceAll("[^\\d]", "")); //$NON-NLS-1$  $NON-NLS-2$
		}
	}


}
