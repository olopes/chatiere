package chatte.net;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class ChatoSecureSocketService implements SecureSocketService {

	SSLContext ctx;
	char[] storePass;
	char[] keyPass;
	public ChatoSecureSocketService(char[] password) throws SecureSocketServiceException {
		this(password, password);
	}
	public ChatoSecureSocketService(char[] storePass, char[] keyPass) throws SecureSocketServiceException {
		this.storePass = storePass;
		this.keyPass = keyPass;
		
		try {
			KeyStore identity = KeyStore.getInstance("JKS");
			try(InputStream in = new FileInputStream("ssl.key")) {
				identity.load(in, storePass);
			}
			ctx = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(identity, keyPass);
			ctx.init(kmf.getKeyManagers(), null, null);
		} catch(Exception e) {
			throw new SecureSocketServiceException(e);
		}
	}
	
	@Override
	public SSLServerSocketFactory newServerSocketFactory() {
		return ctx.getServerSocketFactory();
	}
	
	@Override
	public SSLSocketFactory newSocketFactory() {
		return ctx.getSocketFactory();
	}
}
