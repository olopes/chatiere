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
			Path hist = Paths.get("chatlog"); //$NON-NLS-1$
			Files.createDirectories(hist);
			if(!Files.exists(hist.resolve("ChatView.css"))) { //$NON-NLS-1$
				Files.copy(getClass().getResourceAsStream("/chatte/ui/fx/ChatView.css"), hist.resolve("ChatView.css")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if(!Files.exists(hist.resolve("OpenSansEmoji.ttf"))) { //$NON-NLS-1$
				Files.copy(getClass().getResourceAsStream("/chatte/ui/fx/OpenSansEmoji.ttf"), hist.resolve("OpenSansEmoji.ttf")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String histFileName = new SimpleDateFormat("'ChatLog_'yyyyMMdd_HHmmss'.html'").format(now); //$NON-NLS-1$
			String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(now); //$NON-NLS-1$
			Path path = hist.resolve(histFileName);
			BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			writer = new PrintWriter(bufferedWriter);
			writer.println("<!DOCTYPE html>\n" +  //$NON-NLS-1$
					"<html>\n" +  //$NON-NLS-1$
					"<head>\n" +  //$NON-NLS-1$
					"<meta charset=\"utf-8\" />\n" + //$NON-NLS-1$ 
					"<title>ChatteFX - Conversation log "+timestamp+"</title>\n" + //$NON-NLS-1$ //$NON-NLS-2$ 
					"<style>\n" + //$NON-NLS-1$
					"@font-face {\n" +  //$NON-NLS-1$
					"    font-family: 'OpenSansEmoji';\n" + //$NON-NLS-1$ 
					"    src: url('OpenSansEmoji.ttf');\n" +  //$NON-NLS-1$
					"}\n" + //$NON-NLS-1$
					"</style>\n" +  //$NON-NLS-1$
					"<link rel=\"stylesheet\" href=\"ChatView.css\" />\n" + //$NON-NLS-1$ 
					"</head>\n" +  //$NON-NLS-1$
					"<body>\n" +  //$NON-NLS-1$
					"<h1>Welcome Grande Chato</h1>\n" + //$NON-NLS-1$ 
					"<div id=\"msglist\">"); //$NON-NLS-1$
		} catch(Exception e) {
			throw new RuntimeException("Failed to setup chat history", e); //$NON-NLS-1$
		}
	}

	@MessageListener
	public void closeHistory(final StopServicesMessage message) {
		closeHistoryFile();
	}
	
	
	private void closeHistoryFile() {
		writer.println("</div></body></html>"); //$NON-NLS-1$
		writer.close();
	}

	@Override
	public void recordMessage(String message, Set<String> resources) {
		// TODO Auto-generated method stub
		String fixedMessage = message;
		if(resources != null && !resources.isEmpty()) {
			for(String resourceCode : resources) {
				String resourceName = resourceManager.getResourceFile(resourceCode).getName();
				fixedMessage = fixedMessage.replace("\"chato:"+resourceCode+"\"", "\"../toybox/"+resourceName+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		writer.println("<div>"+fixedMessage+"</div>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
