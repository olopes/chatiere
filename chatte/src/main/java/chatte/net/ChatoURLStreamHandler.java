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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.logging.Logger;

import chatte.resources.ResourceManager;

public class ChatoURLStreamHandler extends URLStreamHandler {
	
	private ResourceManager resourceManager;
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ChatoURLStreamHandler(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		log.fine("Opening connection to 'chato' url: "+u); //$NON-NLS-1$
		URL url = resourceManager.getResourceURL(u.getFile());
		if(url != null)
			return url.openConnection();
		return null;
	}

}
