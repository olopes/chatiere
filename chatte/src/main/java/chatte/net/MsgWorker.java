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
package chatte.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import chatte.msg.AbstractMessage;
import chatte.msg.ChatMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.ResourceMessage;
import chatte.msg.ResourceRequestMessage;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;

public class MsgWorker implements Runnable {
	
	MsgListener server;
	MessageBroker messageBroker;
	Socket sock;
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}
	
	
	final Friend me;
	ObjectInputStream in;
	ObjectOutputStream out;
	
	public MsgWorker(Friend friend, MessageBroker messageBroker, MsgListener server, Socket sock) {
		this.messageBroker = messageBroker;
		this.server = server;
		this.sock = sock;
		this.me = friend;

		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
			log.info("1. Streams created");
		} catch(Exception e) {
			log.log(Level.SEVERE, "Socket stream creation failed", e);
		}

	}

	@MessageListener
	public void sendTypedMessage(TypedMessage message) {
		log.fine("sending message "+message);
		try {
			out.writeObject(new ChatMessage(message));
			out.flush();
		} catch(IOException e) {
			messageBroker.sendMessage(new DisconnectedMessage(me));
		}
	}
	
	@MessageListener
	public void sendResourceRequest(ResourceRequestMessage message) {
		log.fine("preparing to send message "+message);
		if(message.getFrom() == me && !message.isRemote()) {
			try {
				out.writeObject(message);
				out.flush();
			} catch(IOException e) {
				messageBroker.sendMessage(new DisconnectedMessage(me));
			}
		}
	}
	
	@MessageListener
	public void sendResource(ResourceMessage message) {
		log.fine("preparing to send message "+message);
		if(message.getFrom() == me && !message.isRemote()) {
			try {
				out.writeObject(message);
				out.flush();
			} catch(IOException e) {
				messageBroker.sendMessage(new DisconnectedMessage(me));
			}
		}
	}
	
	void dispatchMessage(AbstractMessage message) {
		log.fine(" => dispatching received message :-D ("+message.getClass().getSimpleName()+")");
		message.setRemote(true);
		message.setFrom(me);
		messageBroker.sendMessage(message);
	}
	
	@Override
	public void run() {
		log.info("Starting client worker...");
		messageBroker.addListener(this);
		try {
			// protocol negotiation.
			// 1. send WelcomeMessage
			// 2. wait for more messages
			WelcomeMessage welcome = new WelcomeMessage();
			welcome.setNick(server.getNick()); // from config
			welcome.setPort(server.getPort()); // from config
			welcome.setKnownFriends(server.getConnectedFriends());

			out.writeObject(welcome);
			out.flush();

			log.info("2. Welcome message sent");
			
			WelcomeMessage greeting = (WelcomeMessage) in.readObject();
			log.info("3. Greeting received");
			dispatchMessage(greeting);
			
			log.info("4. Waiting...");
			while(sock.isConnected()) {
				try {
					AbstractMessage message = (AbstractMessage) in.readObject();
					dispatchMessage(message);
				} catch(ClassNotFoundException ex) {
					log.log(Level.SEVERE, "Bad message type received: "+ex.getMessage());
				}
			}
			
		} catch(IOException | ClassNotFoundException e) {
			log.log(Level.SEVERE, "Something bad happened....", e);
		} finally {
			log.info("4. Disconnected.");
			messageBroker.removeListener(this);
			messageBroker.sendMessage(new DisconnectedMessage(me));
		}
		
	}

}
