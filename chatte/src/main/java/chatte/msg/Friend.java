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
package chatte.msg;

import java.io.Serializable;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyBooleanWrapper;

public class Friend implements Serializable {
	private static final long serialVersionUID = 1L;

	String nick;
	String host;
	int port;
	
	transient ReadOnlyBooleanWrapper connectedProperty;
	transient ReadOnlyStringWrapper nickProperty;

	public Friend() {
		connectedProperty = new ReadOnlyBooleanWrapper(false);
		nickProperty = new ReadOnlyStringWrapper();
	}
	
	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
		this.nickProperty.set(nick);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isConnected() {
		return connectedProperty.get();
	}

	public void setConnected(boolean connected) {
		this.connectedProperty.set(connected);
	}
	
	public ReadOnlyBooleanProperty connectedProperty() {
		return connectedProperty.getReadOnlyProperty();
	}
	
	public ReadOnlyStringProperty nickProperty() {
		return nickProperty.getReadOnlyProperty();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Friend && ((Friend)obj).getHost().equals(getHost());
	}
	
	@Override
	public String toString() {
		return new StringBuilder(getNick()).append(" [").append(getHost()).append("]").toString();
	}
	
	public boolean isMyself() {
		return false;
	}
}
