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
import java.util.logging.Logger;

import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.resources.ResourceManager;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public abstract class BaseChatteController implements Initializable, ChatteController {
	
	Stage window;
	
	ConfigService configService;
	MessageBroker messageBroker;
	
	final Logger log;
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}
	
	public BaseChatteController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		log = getLogger();
		this.configService = configService;
		this.messageBroker = messageBroker;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// do nothing
	}

	@Override
	public synchronized Window createWindow(ChatteController parent) {
		// TODO display preferences window
		if(window == null) {
			// load FXML
			window = new Stage(StageStyle.UTILITY);
			window.initOwner(parent.getWindow());
			window.initModality(Modality.WINDOW_MODAL);
			window.setScene(new Scene(getRoot()));
		}
		// preferencesWindow.showAndWait();
		
		return window;
	}
	
	@Override
	public Window getWindow() {
		return window;
	}

	public void showWindow(ChatteController owner) {
		((Stage)createWindow(owner)).show();
	}

}
