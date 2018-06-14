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

import java.util.ResourceBundle;
import java.util.logging.Logger;

import chatte.ChatteServices;
import chatte.ChatteServicesBuilder;
import chatte.ChatteServicesFactory;
import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.msg.StopServicesMessage;
import chatte.resources.ResourceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ChatteFX extends Application {

	private ConfigService configService;
	private ResourceManager resourceManager;
	private MessageBroker messageBroker;
	private ResourceBundle resources;
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}
	
	public static void main(String[] args) {
		launch(ChatteFX.class, args);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		this.resources = ResourceBundle.getBundle("chatte.fx.Bundle"); 

		ChatteServicesBuilder builder = new ChatteServicesFactory();
		ChatteServices services = builder.buildServices();
		
		this.configService = services.getConfigService();
		this.resourceManager = services.getResourceManager();
		this.messageBroker = services.getMessageBroker();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Font.loadFont(getClass().getResource("OpenSansEmoji.ttf").toExternalForm(), 12);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatteFX.fxml"), this.resources);
		Parent root = (Parent)loader.load();
		ChatteController controller = loader.getController();
		controller.configure(configService, resourceManager, messageBroker);
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("ChatteFX.css").toExternalForm());
		
		primaryStage.setTitle("ChatteFX");
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	@Override
	public void stop() throws Exception {
		log.info("Stopping stuff");
		this.messageBroker.sendMessage(new StopServicesMessage());
		super.stop();
	}
	
}
