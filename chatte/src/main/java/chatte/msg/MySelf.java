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

import javax.xml.bind.annotation.XmlRootElement;

import chatte.net.ProxyMode;

@XmlRootElement
public class MySelf extends Friend {
	private static final long serialVersionUID = 1L;

	boolean autoConnect;
	boolean blinkToolbar;
	boolean showNotifications;
	ProxyMode proxyMode;
	String proxyHost;
	Integer proxyPort;

	
	public MySelf() {
		super.setConnected(true);
	}
	
	@Override
	public String getHost() {
		return "localhost"; //$NON-NLS-1$
	}
	
	@Override
	public void setConnected(boolean connected) {
		// do nothing, always connected
	}
	
	public boolean isMyself() {
		return true;
	}

	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public boolean isBlinkToolbar() {
		return blinkToolbar;
	}

	public void setBlinkToolbar(boolean blinkToolbar) {
		this.blinkToolbar = blinkToolbar;
	}

	public boolean isShowNotifications() {
		return showNotifications;
	}

	public void setShowNotifications(boolean showNotifications) {
		this.showNotifications = showNotifications;
	}

	public ProxyMode getProxyMode() {
		return proxyMode;
	}

	public void setProxyMode(ProxyMode proxyMode) {
		this.proxyMode = proxyMode;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}
	
}
