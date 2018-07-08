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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import chatte.msg.AbstractMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.Friend;
import chatte.msg.InboundMessage;
import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.OutboundMessage;
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
	InputStream in;
	OutputStream out;
	
	public MsgWorker(Friend friend, MessageBroker messageBroker, MsgListener server, Socket sock) {
		this.messageBroker = messageBroker;
		this.server = server;
		this.sock = sock;
		this.me = friend;

		try {
			out = sock.getOutputStream();
			in = sock.getInputStream();
			log.info("1. Streams created"); //$NON-NLS-1$
		} catch(Exception e) {
			log.log(Level.SEVERE, "Socket stream creation failed", e); //$NON-NLS-1$
		}

	}

	@MessageListener
	public void sendOutboundMessage(OutboundMessage message) {
		log.fine("sending message "+message); //$NON-NLS-1$
		if(message.getContents()==null) return;
		try {
			String sizeStr = String.format("%08x", message.getContents().length); //$NON-NLS-1$
			out.write(sizeStr.getBytes());
			out.write(message.getContents());
			out.flush();
		} catch(IOException e) {
			messageBroker.sendMessage(new DisconnectedMessage(me));
		}
	}
	
	
	void dispatchMessage(AbstractMessage message) {
		log.fine(" => dispatching received message :-D ("+message.getClass().getSimpleName()+")"); //$NON-NLS-1$ //$NON-NLS-2$
		message.setRemote(true);
		message.setFrom(me);
		messageBroker.sendMessage(message);
	}
	
	@Override
	public void run() {
		log.info("Starting client worker..."); //$NON-NLS-1$
		messageBroker.addListener(this);
		try {
			// protocol negotiation.
			// 1. send WelcomeMessage
			// 2. wait for more messages
			WelcomeMessage welcome = new WelcomeMessage();
			welcome.setNick(server.getNick()); // from config
			welcome.setPort(server.getPort()); // from config
			welcome.setKnownFriends(server.getConnectedFriends());
			log.info("1. Welcome message ready"); //$NON-NLS-1$
			sendOutboundMessage(NetCodec.convertMessage(welcome));

			log.info("2. Welcome message sent"); //$NON-NLS-1$
			
			log.info("3. Waiting..."); //$NON-NLS-1$
			byte [] sizebuf = new byte[8];
			while(sock.isConnected()) {
				// shitty shit can be shitty if one is not careful enough
				int nread = in.read(sizebuf);
				int messageSize = Integer.parseInt(new String(sizebuf), 16);
				byte [] contents = new byte[messageSize];
				nread = in.read(contents);

				InboundMessage message = new InboundMessage(contents);
				dispatchMessage(message);
			}
			
		} catch(IOException e) {
			log.log(Level.SEVERE, "Something bad happened....", e); //$NON-NLS-1$
		} finally {
			log.info("4. Disconnected."); //$NON-NLS-1$
			messageBroker.removeListener(this);
			messageBroker.sendMessage(new DisconnectedMessage(me));
		}
		
	}

}
