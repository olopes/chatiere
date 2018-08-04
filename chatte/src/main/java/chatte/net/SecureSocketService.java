package chatte.net;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public interface SecureSocketService {
	
	SSLServerSocketFactory newServerSocketFactory();
	
	SSLSocketFactory newSocketFactory();
}
