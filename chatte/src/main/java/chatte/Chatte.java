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
package chatte;

import java.net.URL;

import javax.swing.SwingUtilities;

import chatte.config.ConfigService;
import chatte.config.ConfigServiceImpl;
import chatte.msg.ChatoMessageBroker;
import chatte.msg.MessageBroker;
import chatte.net.ChatoURLStreamHandlerFactory;
import chatte.net.MsgListener;
import chatte.resources.ChatoResourceManager;
import chatte.resources.ResourceManager;
import chatte.resources.ResourceRequestHandler;
import chatte.ui.ChatFrame;

public class Chatte {
	
	public static void main(String[] args) {
		// Configure SSL context
		System.setProperty("javax.net.ssl.keyStore","ssl.key");
		System.setProperty("javax.net.ssl.keyStorePassword","ssl.key");
		System.setProperty("javax.net.ssl.trustStore","ssl.key");
		System.setProperty("javax.net.ssl.trustStorePassword","ssl.key");
		
		final ConfigService configService = new ConfigServiceImpl();
		final ResourceManager resourceManager = new ChatoResourceManager();
		final MessageBroker messageBroker = new ChatoMessageBroker(resourceManager);
		final ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler(resourceManager, messageBroker);
		messageBroker.addListener(resourceRequestHandler);

		// Register URL handler factory
		URL.setURLStreamHandlerFactory(new ChatoURLStreamHandlerFactory(resourceManager));
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				try {
//					javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//				} catch (Exception e) {
//				}
				ChatFrame blah = new ChatFrame(resourceManager, messageBroker, configService);
				
				// start server
				Thread t = new Thread(new MsgListener(messageBroker, configService), "network service");
				t.start();

				blah.setVisible(true);
			}
		});
	}

}
