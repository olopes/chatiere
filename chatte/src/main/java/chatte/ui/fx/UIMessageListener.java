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

import chatte.msg.ChatMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.MessageListener;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;
import javafx.application.Platform;

public class UIMessageListener {

	ChatteMainController controller;
	
	public UIMessageListener(ChatteMainController controller) {
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
