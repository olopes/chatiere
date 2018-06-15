package chatte.fx;

import chatte.msg.ChatMessage;
import chatte.msg.MessageListener;
import chatte.msg.TypedMessage;
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

}
