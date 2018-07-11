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

import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.resources.ResourceManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

public class NotifController extends BaseChatteController {

	@FXML Parent notifPanel;
	
	NotifPopup window;
	
	public NotifController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		super(configService, resourceManager, messageBroker);
	}

	@Override
	public synchronized Window createWindow(ChatteController parent) {
		if(window == null)
			window = new NotifPopup(notifPanel);
		return window;
	}

	public void show(ChatteController parent, String title, String message) {
		window.show(parent.getWindow(), title, message);
	}

	@Override
	public Parent getRoot() {
		return notifPanel;
	}
	
	@Override
	public void showWindow(ChatteController owner) {
	}
	
	@FXML
	public void notifClicked(MouseEvent evt) {
		window.hide();
	}

}
