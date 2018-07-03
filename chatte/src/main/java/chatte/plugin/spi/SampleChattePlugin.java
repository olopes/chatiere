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
package chatte.plugin.spi;

import java.util.logging.Logger;

import chatte.msg.ChatMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.MessageListener;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;
import chatte.plugin.ChattePlugin;

public class SampleChattePlugin implements ChattePlugin {
	private final Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	@Override
	public String getName() {
		return "Sample plugin";
	}

	@Override
	public String getDescription() {
		return "Sample plugin description";
	}
	
	@MessageListener
	public void welcomeFriend(WelcomeMessage message) {
		log.info("Hello '"+message.getFrom().getNick()+"'! How are you today?");
	}
	
	@MessageListener
	public void byebyeFriend(final DisconnectedMessage message) {
		log.info("Bye bye '"+message.getFrom().getNick()+"'. I hope to meet you again.");
	}
	
	@MessageListener
	public void messageReceived(final ChatMessage message) {
		log.info("'"+message.getFrom().getNick()+"' said: "+message.getMessage());
	}

	@MessageListener
	public void messageTyped(final TypedMessage message) {
		log.info("I say: "+message.getMessage());
	}

}

