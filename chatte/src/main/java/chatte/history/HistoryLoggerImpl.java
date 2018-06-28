package chatte.history;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.StopServicesMessage;
import chatte.resources.ResourceManager;

public class HistoryLoggerImpl implements HistoryLogger {

	PrintWriter writer;
	ResourceManager resourceManager;
	
	public HistoryLoggerImpl(MessageBroker messageBroker, ConfigService configService, ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		this.writer = new PrintWriter(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
			}
			@Override
			public void flush() throws IOException {
			}
			@Override
			public void close() throws IOException {
			}
		});
		setupHistoryFile(configService);
		messageBroker.addListener(this);
	}
	
	private void setupHistoryFile(ConfigService configService) {
		try {
			Date now = new Date();
			Path hist = Paths.get("chatlog");
			Files.createDirectories(hist);
			if(!Files.exists(hist.resolve("ChatView.css"))) {
				Files.copy(getClass().getResourceAsStream("/chatte/fx/ChatView.css"), hist.resolve("ChatView.css"));
			}
			if(!Files.exists(hist.resolve("Friends.css"))) {
				Files.copy(getClass().getResourceAsStream("/chatte/fx/Friends.css"), hist.resolve("Friends.css"));
			}
			if(!Files.exists(hist.resolve("OpenSansEmoji.ttf"))) {
				Files.copy(getClass().getResourceAsStream("/chatte/fx/OpenSansEmoji.ttf"), hist.resolve("OpenSansEmoji.ttf"));
			}
			String histFileName = new SimpleDateFormat("'ChatLog_'yyyyMMdd_HHmmss'.html'").format(now);
			String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(now);
			Path path = hist.resolve(histFileName);
			BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			writer = new PrintWriter(bufferedWriter);
			writer.println("<!DOCTYPE html>\n" + 
					"<html>\n" + 
					"<head>\n" + 
					"<meta charset=\"utf-8\" />\n" + 
					"<title>ChatteFX - Conversation log "+timestamp+"</title>\n" + 
					"<style>\n" +
					"@font-face {\n" + 
					"    font-family: 'OpenSansEmoji';\n" + 
					"    src: url('OpenSansEmoji.ttf');\n" + 
					"}\n" +
					"</style>\n" + 
					"<link rel=\"stylesheet\" href=\"ChatView.css\" />\n" + 
					"<link rel=\"stylesheet\" href=\"Friends.css\" />\n" + 
					"</head>\n" + 
					"<body>\n" + 
					"<h1>Welcome Grande Chato</h1>\n" + 
					"<div id=\"msglist\">");
		} catch(Exception e) {
			throw new RuntimeException("Failed to setup chat history", e);
		}
	}

	@MessageListener
	public void closeHistory(final StopServicesMessage message) {
		closeHistoryFile();
	}
	
	
	private void closeHistoryFile() {
		writer.println("</div></body></html>");
		writer.close();
	}

	@Override
	public void recordMessage(String message, Set<String> resources) {
		// TODO Auto-generated method stub
		String fixedMessage = message;
		if(resources != null && !resources.isEmpty()) {
			for(String resourceCode : resources) {
				String resourceName = resourceManager.getResourceFile(resourceCode).getName();
				fixedMessage = fixedMessage.replace("\"chato:"+resourceCode+"\"", "\"../toybox/"+resourceName+"\"");
			}
		}
		writer.println("<div>"+fixedMessage+"</div>");
	}

}
