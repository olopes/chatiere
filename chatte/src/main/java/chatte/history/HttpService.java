package chatte.history;

import static java.util.Collections.reverseOrder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import chatte.msg.MessageListener;
import chatte.msg.StopServicesMessage;
import chatte.resources.ChatoResourceManager;
import chatte.resources.ResourceManager;

@SuppressWarnings("restriction")
public class HttpService implements HttpHandler {
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	HttpServer server;
	ResourceManager resourceManager;
	int httpPort;
	
	public HttpService(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		this.httpPort = -1;
		int tries = 0;
		while(httpPort == -1) {
			try(ServerSocket serverSocket = new ServerSocket(0)) {
				this.httpPort = serverSocket.getLocalPort();
			} catch(Exception e) {
				log.log(Level.WARNING, "Failed to open server port: \""+e.getMessage()+"\" Retrying...");
				tries++;
				if(tries >= 3) throw new RuntimeException(e);
			}
		}
		
		  
		  // TODO configure random port?
		InetSocketAddress address;
		try {
			address = new InetSocketAddress("127.0.0.1", this.httpPort); //$NON-NLS-1$
			server = HttpServer.create(address, 0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		server.createContext("/", this);
		server.setExecutor(null);
		server.start();
		httpPort = address.getPort();
	}
	
	public int getHttpPort() {
		return httpPort;
	}

	@MessageListener
	public void shutdown(StopServicesMessage msg) {
		server.stop(10);
	}
	
	Pattern logPattern = Pattern.compile("/ChatLog_(\\d{8}).html"); //$NON-NLS-1$
	Pattern resourcePattern = Pattern.compile("/(\\w{40})"); //$NON-NLS-1$
	Pattern filePattern = Pattern.compile("/(ChatView\\.css|ChatView\\.js|OpenSansEmoji\\.ttf)"); //$NON-NLS-1$
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		Matcher m = logPattern.matcher(path);
		if("/".equals(path) || "/index.html".equals(path)) {
			sendIndexHtml(exchange);
			return;
		}
		
        if (m.matches()) {
           sendLog(exchange, m.group(1));
           return;
        } 
        
        m = resourcePattern.matcher(path);
        if (m.matches()) {
        	sendResource(exchange, m.group(1));
        	return;
        } 
        
        m = filePattern.matcher(path);
        if (m.matches()) {
        	sendFile(exchange, m.group(1));
        	return;
        }
        
        sendNotFound(exchange);
	}
	
	// note: HTML5 doesn't support frames, so a select box with an iframe is built
	static final String indexHtml = "<!DOCTYPE html>\n" + 
			"<html><head><meta charset='utf-8' />" + 
			"<title>ChatteFX - Conversation History</title>" + 
			"<link rel='stylesheet' href='ChatView.css' />" + 
			"<script type='text/javascript'>\n" + 
			"function selectLog(sel) {document.getElementById('logview').src=sel.options[sel.selectedIndex].text;}\n" + 
			"</script></head>" + 
			"<body style='margin:0px;padding:0px;overflow:hidden'>" + 
			"<div id='loglist' style='margin-left:4em;'>Select log file: <select onchange='selectLog(this)'>%1$s</select></div>" + 
			"<iframe id='logview' src='%2$s' style='overflow:hidden;overflow-x:hidden;overflow-y:hidden;height:90%%;width:96%%;position:absolute;top:2em;margin:auto;margin-right:auto;left:0px;right:0px;bottom:20px' ></iframe>" + 
			"</body></html>";

	static final String header="<!DOCTYPE html>\n<html><head><meta charset=\"utf-8\" />" //$NON-NLS-1$
			+ "<title>%1$s</title><style>@font-face {\n\tfont-family: 'OpenSansEmoji';" //$NON-NLS-1$
			+ "\n\tsrc: url('OpenSansEmoji.ttf');\n\t}</style>" //$NON-NLS-1$
			+ "<link rel=\"stylesheet\" href=\"ChatView.css\" /></head>" //$NON-NLS-1$
			+ "<body><h1>%1$s</h1><div id=\"msglist\">"; //$NON-NLS-1$
	static final byte[] footer = "</div></body></html>".getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$
	static final byte [] empty = new byte[0];
	static final byte[] notFound = "FILE NOT FOUND".getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$

	void sendIndexHtml(HttpExchange exchange) throws IOException {
		
		final Path path = Paths.get("chatlog");
		final Set<String> logFiles = new TreeSet<>(reverseOrder());
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		        if(dir.equals(path))
		        	return FileVisitResult.CONTINUE;

	    		return FileVisitResult.SKIP_SUBTREE;
			}
			
		    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		        throws IOException {
		    	String fileName = file.getFileName().toString();
		    	if(Pattern.matches("ChatLog_\\d{8}\\.html", fileName)) {
		    		logFiles.add(fileName);
		    	}
		        return FileVisitResult.CONTINUE;
		    }
			
		});
		
		StringBuilder options = new StringBuilder(logFiles.size()*50+10);
		String selected = "about:blank";
		if(!logFiles.isEmpty()) {
			boolean first = true;
			for(String file : logFiles) {
				if(first) {
					selected = file;
					options.append("<option selected=\"selected\">");
					first=false;
				} else {
					options.append("<option>");
				}
				options.append(file).append("</option>");
			}
		}

		// build response
		byte[] indexContents = String.format(indexHtml, options, selected).getBytes(StandardCharsets.UTF_8);
		
		String contentType = getContentType("index.html");
		exchange.getResponseHeaders().add("Content-Type", contentType); //$NON-NLS-1$
		exchange.sendResponseHeaders(200, indexContents.length);
		
		OutputStream out = exchange.getResponseBody();
		out.write(indexContents);
		out.flush();
		
		exchange.close();

	}
	
	void sendLog(HttpExchange exchange, String group) throws IOException {
		// TODO Auto-generated method stub
		//String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(now); //$NON-NLS-1$
		File logFile = new File("chatlog", "ChatLog_"+group+".html"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
		if(!logFile.exists()) {
			sendNotFound(exchange);
			return;
		}
		
		String timestamp = "Conversation history "+group; //$NON-NLS-1$
		byte [] prepend = String.format(header, timestamp).getBytes(StandardCharsets.UTF_8);
		sendURL(exchange, logFile.toURI().toURL(), prepend, footer);
	}
	
	void sendResource(HttpExchange exchange, String resourceCode) throws IOException {
		URL file = resourceManager.getResourceURL(resourceCode);
		if (file == null)
			sendNotFound(exchange);
		else
			sendURL(exchange, file, empty, empty);
	}

	void sendFile(HttpExchange exchange, String group) throws IOException {
		URL file = getClass().getResource("/chatte/ui/fx/"+group); //$NON-NLS-1$
		sendURL(exchange, file, empty, empty);
	}

	void sendURL(HttpExchange exchange, URL source, byte [] prepend, byte [] append) throws IOException {
		String path = source.getPath().toLowerCase();
		URLConnection conn = source.openConnection();
		long len = conn.getContentLengthLong()+prepend.length+append.length;
		String lastModified = conn.getHeaderField("Last-Modified"); //$NON-NLS-1$
		String contentType = getContentType(path);
		exchange.getResponseHeaders().add("Content-Type", contentType); //$NON-NLS-1$
		exchange.getResponseHeaders().add("Last-Modified", lastModified); //$NON-NLS-1$
		exchange.sendResponseHeaders(200, len);
		
		try (InputStream in = conn.getInputStream()) {
			byte [] buff = new byte[8192];
			int r = 0;
			OutputStream out = exchange.getResponseBody();
			out.write(prepend);
			while((r = in.read(buff)) >= 0) {
				out.write(buff, 0, r);
			}
			out.write(append);
			out.flush();
		}
		
		exchange.close();
	}
	
	String getContentType(String path) {
		if(path == null) return null;
		path = path.toLowerCase();
		String contentType = "application/octet-stream"; //$NON-NLS-1$
		if(path.endsWith(".js")) { //$NON-NLS-1$
			contentType = "application/javascript"; //$NON-NLS-1$
		} else if(path.endsWith(".html")) { //$NON-NLS-1$
			contentType = "text/html; charset=utf-8"; //$NON-NLS-1$
		} else if(path.endsWith(".css")) { //$NON-NLS-1$
			contentType = "text/css"; //$NON-NLS-1$
		} else if(path.endsWith(".ttf")) { //$NON-NLS-1$
			contentType = "text/css"; //$NON-NLS-1$
		} else if(path.endsWith(".gif")) { //$NON-NLS-1$
			contentType = "image/gif"; //$NON-NLS-1$
		} else if(path.endsWith(".png")) { //$NON-NLS-1$
			contentType = "image/png"; //$NON-NLS-1$
		} else if(path.endsWith(".jpg")) { //$NON-NLS-1$
			contentType = "image/jpg"; //$NON-NLS-1$
		}
		return contentType;
	}
	
	void sendNotFound(HttpExchange exchange) throws IOException {

		exchange.sendResponseHeaders(404, notFound.length);
		OutputStream out = exchange.getResponseBody();
		out.write(notFound);
		out.flush();
		
		exchange.close();
		
	}

	public static void main(String[] args) {
		HttpService service = new HttpService(new ChatoResourceManager());
		System.out.println("http://localhost:"+service.getHttpPort()+"/"); //$NON-NLS-1$ //$NON-NLS-2$
		
		for(;;) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				service.shutdown(null);
			}
		}
	}
}
