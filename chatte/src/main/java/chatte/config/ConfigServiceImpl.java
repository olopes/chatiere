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
package chatte.config;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import chatte.msg.Friend;
import chatte.msg.MySelf;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigServiceImpl implements ConfigService {

	private static int DEFAULT_PORT = 6666;

	private MySelf me = new MySelf();
	private Map<String, Friend> knownFriends;

	public ConfigServiceImpl() {
		loadConfig();
	}

	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	void loadConfig() {
		File cfgFile = new File("config.xml");
		if(cfgFile.exists()) {
			try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(cfgFile)))) {
				@SuppressWarnings("unchecked")
				Map<String, Friend> readObject = (Map<String, Friend>) decoder.readObject();
				knownFriends = readObject;
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error reading config.xml", e);
				// TODO handle this....
			}
			me = (MySelf) knownFriends.get(me.getHost());
			
		} else {
			me = new MySelf();
			me.setNick(System.getProperty("user.name", "me"));
			me.setPort(DEFAULT_PORT);
			knownFriends = new LinkedHashMap<>();
			knownFriends.put(me.getHost(), me);
			saveConfig();
		}
	}

	void saveConfig() {
		File cfgFile = new File("config.xml");
		if(cfgFile.exists()) cfgFile.delete();
		try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(cfgFile)))) {
			encoder.writeObject(knownFriends);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error saving config.xml", ex);
		}
	}

	@Override
	public String getNick() {
		return me.getNick();
	}

	@Override
	public int getPort() {
		return me.getPort();
	}

	@Override
	public Friend getSelf() {
		return me;
	}
	
	@Override
	public List<Friend> getKnownFriends() {
		return new ArrayList<>(knownFriends.values());
	}

	@Override
	public Friend getFriend(String addr) {
		// resolve??
		String host = fixHost(addr);
		Friend ff = knownFriends.get(host);
		if(null == ff) {
			ff = new Friend();
			ff.setHost(host);
			// Assume default port
			ff.setPort(DEFAULT_PORT);
			addFriend(ff);
		}
		return ff;
	}

	@Override
	public void addFriend(Friend friend) {
		if(null == friend) return;
		String host = fixHost(friend.getHost());
		log.info("Registering new friend with host "+host);
		if(me.getHost().equals(host)) {
			me.setNick(friend.getNick());
			me.setPort(friend.getPort());
		} else if(knownFriends.containsKey(host)) {
			Friend existing = knownFriends.get(host);
			existing.setPort(friend.getPort());
			existing.setNick(friend.getNick());
		} else {
			friend.setHost(host);
			knownFriends.put(host, friend);
		}
		saveConfig();
	}
	
	String fixHost(String host) {
		if(null == host) return host;
		if(me.getHost().equals(host)) return host;
		try {
			return InetAddress.getByName(host).getHostAddress();
		} catch (UnknownHostException e) {
			log.log(Level.WARNING, "Host not found: "+host, e);
			return null;
		}

	}

	@Override
	public void removeFriend(Friend friend) {
		// fetch true reference to friend
		String realHost = fixHost(friend.getHost());
		knownFriends.remove(realHost);
	}
}
