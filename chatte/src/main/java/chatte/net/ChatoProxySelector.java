package chatte.net;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import chatte.msg.ConfigChangedMessage;
import chatte.msg.MessageListener;
import chatte.msg.MySelf;

public class ChatoProxySelector extends ProxySelector {
	
	final ProxySelector defProxySelector;
	final MySelf self;
	ProxyMode usage;
	public ChatoProxySelector(ProxySelector defProxySelector, MySelf self) {
		this.defProxySelector = defProxySelector;
		this.self = self;
		usage = ProxyMode.SYSTEM;
	}
	
	@Override
	public List<Proxy> select(URI uri) {
		if("chato".equals(uri.getScheme()))
			return Arrays.asList(Proxy.NO_PROXY);
		switch (usage) {
		case SYSTEM:
			return defProxySelector.select(uri);
		// case MANUAL:
		//	return Arrays.asList(a); // TODO fetch proxy config: HTTP, SOCKS
		default:
			return Arrays.asList(Proxy.NO_PROXY);
		}
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		if("chato".equals(uri.getScheme())) {
			// ??
		} else if(usage == ProxyMode.SYSTEM) {
			defProxySelector.connectFailed(uri, sa, ioe);
		}
	}
	
	@MessageListener
	public void configUpdated(ConfigChangedMessage msg) {
		
	}
}
