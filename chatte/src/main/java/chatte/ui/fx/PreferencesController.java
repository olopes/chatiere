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
import chatte.msg.MessageBroker;
import chatte.net.ProxyMode;
import chatte.resources.ResourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public class PreferencesController extends BaseChatteController {
	
	// root panel
	@FXML Parent preferences;
	
	// text fields
	@FXML TextField nickField;
	@FXML TextField portField;
	@FXML CheckBox  autoConnectCheck;
	@FXML CheckBox  blinkToolbarCheck;
	@FXML CheckBox  showNotifsCheck;
	@FXML ComboBox<ProxyMode> proxyMode;
	@FXML TextField proxyHost;
	@FXML TextField proxyPort;
	
	
	// buttons
	@FXML Button okButton;
	@FXML Button cancelButton;
	

	public PreferencesController(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		super(configService, resourceManager, messageBroker);
	}

	@Override
	public void initialize(URL baseUrl, final ResourceBundle resourceBundle) {
		super.initialize(baseUrl, resourceBundle);
		
		proxyMode.setConverter(new StringConverter<ProxyMode>() {
			
			@Override
			public String toString(ProxyMode usage) {
				String label = resourceBundle.getString("dialog.prefs.network.proxymode."+usage);
				return label;
			}
			
			@Override
			public ProxyMode fromString(String string) {
				return null;
			}
		});
		proxyMode.getItems().addAll(ProxyMode.values());
	}
	
	public Parent getRoot() {
		return preferences;
	}
	
	public void checkNumericKey(KeyEvent evt) {
		if(!evt.getCharacter().matches("[0-9]")) { //$NON-NLS-1$
			evt.consume();
		}
	}
	
	public void doOkClick(ActionEvent evt) {
		
	}

	public void doCancelClick(ActionEvent evt) {
		
	}
	
	public void toggleProxyFields(ActionEvent evt) {
		
	}

}
