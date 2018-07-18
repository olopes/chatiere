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
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public class EmojiController extends BaseChatteController implements EventHandler<WebEvent<String>>, Callback<PromptData, String> {
	
	@FXML Parent emojiSelectionPanel;
	
	@FXML WebView gifsView;
	@FXML WebView emojiView;
	
	ChatteMainController parentController;
	ResourceManager resourceManager;
	
	public EmojiController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		super(configService, resourceManager, messageBroker);
		this.resourceManager = resourceManager;
	}

	@Override
	public Parent getRoot() {
		return emojiSelectionPanel;
	}

	@Override
	public synchronized Window createWindow(final ChatteController parent) {
		if(window == null) {
			parentController = (ChatteMainController) parent;
			window = new Stage();
			window.initOwner(parent.getWindow());
			window.initModality(Modality.WINDOW_MODAL);
			
			window.setScene(new Scene(getRoot(), 640, 400));
			
			WebEngine gifEngine = gifsView.getEngine();
			gifEngine.setOnAlert(this);
			gifEngine.setPromptHandler(this);
			gifEngine.setJavaScriptEnabled(true);

			WebEngine emoEngine = emojiView.getEngine();
			emoEngine.setOnAlert(this);
			emoEngine.setJavaScriptEnabled(true);

		}
		
		gifsView.getEngine().load(getClass().getResource("EmoticonView.html").toExternalForm()); //$NON-NLS-1$
		emojiView.getEngine().load(getClass().getResource("EmojiList.html").toExternalForm()); //$NON-NLS-1$
		return window;
	}
	
	@Override
	public void handle(WebEvent<String> event) {
		event.consume();
		final String message = event.getData();
		Platform.runLater(new Runnable () {
			@Override
			public void run() {
				if(message.startsWith("chato:")) {
					parentController.appendInputImage(message.substring(6));
				} else {
					parentController.appendEmoji(message);
				}

			}
		});
	}
	
	@Override
	public String call(PromptData param) {
		String [] resources = resourceManager.getResources();
		if(resources == null || resources.length == 0) return "";
		// join strings
		StringBuilder sb = new StringBuilder(41*resources.length); // SHA1 (40 chars) + 1 separator
		for(String s : resources)
			sb.append(s).append(',');
		sb.setLength(sb.length()-1);
		return sb.toString();
	}

}
