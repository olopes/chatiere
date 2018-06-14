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
package chatte.resources;

import java.io.File;
import java.util.logging.Logger;

import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.ResourceMessage;
import chatte.msg.ResourceRequestMessage;
import chatte.msg.ResourceUpdatedMessage;

public class ResourceRequestHandler {
	ResourceManager resourceManager;
	MessageBroker messageBroker;
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ResourceRequestHandler(ResourceManager resourceManager, MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
		this.resourceManager = resourceManager;
	}


	@MessageListener
	public void sendRequestedResource(ResourceRequestMessage message) {
		if(message.isRemote()) {
			// TODO move this to the proper place
			for(String resourceCode : message.getResources()) {
				File resourceFile = resourceManager.getResourceFile(resourceCode);
				if(resourceFile == null) continue;
				
				String type = resourceFile.getName();
				type = type.substring(type.lastIndexOf('.')+1);
				byte [] data = resourceManager.getResourceData(resourceCode);
				ResourceMessage resourceMsg = new ResourceMessage(message.getFrom(), type, data);
				messageBroker.sendMessage(resourceMsg);
			}
		}
	}
	
	@MessageListener
	public void sendResource(ResourceMessage message) {
		log.fine("New resource!! heheheheh!");
		if(message.isRemote()) {
			String newResource = resourceManager.addResource(message.getContents(), message.getType());
			messageBroker.sendMessage(new ResourceUpdatedMessage(message.getFrom(), newResource));
		}
	}
}
