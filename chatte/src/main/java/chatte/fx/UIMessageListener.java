package chatte.fx;

import chatte.msg.ChatMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.MessageListener;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;
import javafx.application.Platform;

public class UIMessageListener {

	ChatteController controller;
	
	public UIMessageListener(ChatteController controller) {
		this.controller = controller;
	}
	
	@MessageListener
	public void messageTyped(final TypedMessage message) {
		// ask to run in JavaFX thread
		Platform.runLater(new Runnable(){
			public void run() {
				controller.messageTyped(message);
			}
		});
	}
	
	@MessageListener
	public void chatMessageReceived(final ChatMessage message) {
		// ask to run in JavaFX thread
		Platform.runLater(new Runnable(){
			public void run() {
				controller.chatMessageReceived(message);
			}
		});
	}

	@MessageListener
	public void welcomeFriend(final WelcomeMessage message) {
		Platform.runLater(new Runnable(){
			public void run() {
				controller.welcomeFriend(message);
			}
		});
	}
	
	@MessageListener
	public void byebyeFriend(final DisconnectedMessage message) {
		Platform.runLater(new Runnable(){
			public void run() {
				controller.byebyeFriend(message);
			}
		});
	}
	
}
