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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import chatte.config.ConfigService;
import chatte.msg.ConnectedMessage;
import chatte.msg.DisconnectedMessage;
import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.MySelf;
import chatte.msg.NewFriendMessage;
import chatte.msg.WelcomeMessage;

public class MsgListener implements Runnable {
	
	MessageBroker messageBroker;
	ConfigService configService;
	boolean running = true;
	Set<Friend> connectedFriends;
	Deque<Friend> connectingFriends;
	Set<String> localAddresses;
	Thread connectorThread;
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	private class FriendConnector implements Runnable {
		@Override
		public void run() {
			while(running) {
				if(!connectingFriends.isEmpty())
					connectToFriend(connectingFriends.pop());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					
				}
			}
		}
	}
	
	public MsgListener(MessageBroker messageBroker, ConfigService configService) {
		this.messageBroker = messageBroker;
		this.configService = configService;
		this.connectedFriends = new HashSet<>();
		this.connectingFriends = new LinkedList<>();
		this.localAddresses = new HashSet<>();
		loadLocalAddresses();
		this.connectorThread = new Thread(new FriendConnector(), "Friend Connector");
		this.connectorThread.start();
	}
	
	public void sendStop() {
		running = false;
		connectorThread.interrupt();
	}
	
	void loadLocalAddresses () {
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()){
					InetAddress i = (InetAddress) ee.nextElement();
					localAddresses.add(i.getHostAddress());
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error loading local addresses list", ex);
		}
	}
	
	
	void printNetAddresses () {
		log.info("Local addresses: "+this.localAddresses);
	}
	
	@Override
	public void run() {
		messageBroker.addListener(this);
		
		// list IP addresses
		printNetAddresses();
		
		// connect to all friends
		connectingFriends.addAll(configService.getKnownFriends());

		// listen for connections
		ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
		try (ServerSocket server = factory.createServerSocket(getPort())) {
			while(running) {
				try {
					Socket socket = server.accept();
					String host = socket.getInetAddress().getHostAddress();
					
					log.info("Got a connection from "+host);
					Friend friend = configService.getFriend(host);

					connectedFriends.add(friend);
					
					MsgWorker newWorker = new MsgWorker(friend, messageBroker, this, socket);
					new Thread(newWorker,"net client "+host).start();
				} catch(IOException ex) {
					log.log(Level.SEVERE, "Connection error", ex);
				}
			}
			
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			messageBroker.removeListener(this);
		}
	}
	
	public List<Friend> getConnectedFriends() {
		return configService.getKnownFriends();// connectedFriends;
	}
	
	@MessageListener
	public void connectNewFriend(NewFriendMessage message) {
		log.fine("New friend added "+message.getFriend().getHost());
		connectToFriend(message.getFriend());
	}
	
	@MessageListener
	public void welcomeFriend(WelcomeMessage message) {
		log.fine("Hello new friend!");
		Friend friend = configService.getFriend(message.getFrom().getHost());
		friend.setPort(message.getPort());
		friend.setNick(message.getNick());
		configService.addFriend(friend);
		friend.setConnected(true);
		log.fine("We are now connected: "+friend);
		messageBroker.sendMessage(new ConnectedMessage(friend));
		if(message.getKnownFriends() != null && !message.getKnownFriends().isEmpty()) {
			for(Friend newFriend : message.getKnownFriends()) {
				if(newFriend instanceof MySelf) {
					log.fine("WELCOME FRIENDS - Skipping my self...");
					continue;
				}
				if(this.localAddresses.contains(newFriend.getHost())) {
					log.fine("WELCOME FRIENDS - Skipping my onw machine...");
					continue;
				}
				configService.addFriend(newFriend);
				connectingFriends.add(configService.getFriend(newFriend.getHost()));
			}
		}
	}
	
	@MessageListener
	public void byebyeFriend(DisconnectedMessage message) {
		// new friend just disconnected.
		Friend friend = configService.getFriend(message.getFrom().getHost());
		friend.setConnected(false);
		while(connectingFriends.remove(friend));
		connectedFriends.remove(friend);
	}
	
	void connectToFriend(Friend friend) {
		if(null == friend || friend instanceof MySelf || localAddresses.contains(friend.getHost())) {
			log.fine("Ignoring local connection");
			return;
		}
		log.info("Connection to "+friend.getHost()+"...");
		
		if(connectedFriends.contains(friend)) {
			log.fine("My friend, you are already connected/connecting: "+friend);
			return;
		}
		
		connectedFriends.add(friend);
		try {
			Socket socket = SSLSocketFactory.getDefault().createSocket(friend.getHost(), friend.getPort());
			MsgWorker newWorker = new MsgWorker(friend, messageBroker, this, socket);
			Thread workerThread = new Thread(newWorker,"net client "+socket.getInetAddress());
			workerThread.start();
		} catch(IOException e) {
			friend.setConnected(false);
			log.log(Level.SEVERE, "Connection to "+friend.getHost()+" failed", e);
		}
		
	}
	
	// listen to status to handle connected friends
	void consume(WelcomeMessage welcome) {
		// check existing friends
		for(Friend friend : welcome.getKnownFriends()) {
			Friend known = configService.getFriend(friend.getHost());
			if(known == null) {
				// new friend!!
				configService.addFriend(friend);
				connectToFriend(friend);
			}
		}
	}

	public String getNick() {
		return configService.getNick();
	}

	public int getPort() {
		return configService.getPort();
	}
	
	
}
