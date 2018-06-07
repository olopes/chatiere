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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import chatte.resources.ResourceManager;

public class ChatoMessageBroker implements MessageBroker, Runnable {

	private static class Listener {
		Method method;
		Object instance;

		public Listener(Object instance, Method method) {
			this.instance = instance;
			this.method = method;
		}

		void call(AbstractMessage message) {
			try {
				System.out.println("Calling...");
				this.method.invoke(this.instance, message);
				System.out.println("done!");
			} catch (Exception e) {
				System.out.println("method call failed with exception");
				e.printStackTrace();
			}
		}
	}

	Thread messageDispatch;
	ResourceManager resourceManager;
	Deque<AbstractMessage> messageQueue;
	Deque<AbstractMessage> pendingMessages;
	Map<Class<? extends AbstractMessage>, List<Listener>> listeners;

	public ChatoMessageBroker(ResourceManager resourceManaer) {
		this.resourceManager = resourceManaer;
		this.messageQueue = new LinkedBlockingDeque<>();
		this.pendingMessages = new LinkedBlockingDeque<>();
		this.listeners = new HashMap<>();
		List<Listener> empty = Collections.emptyList();
		this.listeners.put(AbstractMessage.class, empty);
		this.messageDispatch = new Thread(this);
		this.messageDispatch.start();
		
		addListener(this);
	}

	@Override
	public void addListener(Object listenerInstance) {
		if (null == listenerInstance)
			return;

		Class<?> listenClass = listenerInstance.getClass();

		System.out.println("Registering new listener of type "+listenClass.getSimpleName());
		
		for(Method method : listenClass.getMethods()) {
			// System.out.println("   checking method "+method.getName());
			MessageListener listenerAnnotation = method.getAnnotation(MessageListener.class);
			if(listenerAnnotation == null) continue;
			System.out.println("      Annotation found!");
			Class<? extends AbstractMessage> messageType = listenerAnnotation.forMessage();
			System.out.println("      Message type is "+messageType.getSimpleName());
			if(messageType == AbstractMessage.class) {
				System.out.println("          AbstractMessage or default value. Inspecting method params...");
				// default value - check parameters
				Class<?> [] paramTypes = method.getParameterTypes();
				if(paramTypes.length == 1 && AbstractMessage.class.isAssignableFrom(paramTypes[0])) {
					@SuppressWarnings("unchecked")
					Class<? extends AbstractMessage> paramType = (Class<? extends AbstractMessage>) paramTypes[0];
					messageType = paramType;
					System.out.println("          one parameter of type "+paramType.getSimpleName());
				}
			}
			if(messageType != AbstractMessage.class) {
				// all good! register type
				List<Listener> typeListeners = listeners.get(messageType);
				if(typeListeners == null)
					listeners.put(messageType, typeListeners = new LinkedList<>());
				typeListeners.add(new Listener(listenerInstance, method));
				System.out.println("   listener method registered");
			}
		}
	}

	@Override
	public void removeListener(Object listenerInstance) {
		if (null == listenerInstance)
			return;

		for(List<Listener> typeListeners : listeners.values()) {
			ListIterator<Listener> iter = typeListeners.listIterator();
			while(iter.hasNext()) {
				Listener listener = iter.next();
				if(listener.instance == listenerInstance)
					iter.remove();
			}
		}
	}

	public void stop() {
		messageQueue.addFirst(new StopServicesMessage());
		messageDispatch.interrupt(); // wake up!
	}

	@Override
	public void sendMessage(AbstractMessage message) {
		if (message == null)
			return;
		messageQueue.push(message);
		messageDispatch.interrupt(); // wake up!
	}

	void doSendMessage(AbstractMessage message) {
		if (message == null)
			return;

		System.out.println("doSend message "+message.getClass().getSimpleName());
		// Check if all resources are available
		Set<String> missingResources = missingRessources(message);
		boolean messageComplete = missingResources.isEmpty();
		message.setComplete(messageComplete);
		
		if(messageComplete) {
			System.out.println("Message complete");
			Class<? extends AbstractMessage> messageClass = message.getClass();
			List<Listener> typeListeners = listeners.get(messageClass);
			System.out.println("Listeners for this message type: "+typeListeners);
			if(typeListeners != null && !typeListeners.isEmpty()) {
				for (Listener listener : typeListeners) {
					System.out.println("Dispatching message to listener...");
					listener.call(message);
				}
			}
		} else {
			System.out.println("Incomplete message. Move it to pending messages");
			if(!message.isResourcesRequested()) {
				// Move the request to the front
				messageQueue.addFirst(new ResourceRequestMessage(message.getFrom(), missingResources));
				message.setResourcesRequested(true);
			}
			pendingMessages.addLast(message);
		}
	}
	
	Set<String> missingRessources(AbstractMessage message) {
		Set<String> missingResources = new HashSet<>();
		if(message.getResourceRefs() != null && !message.getResourceRefs().isEmpty()) {
			System.out.println("Checking message with resources. "+message.getResourceRefs());
			for(String resource : message.getResourceRefs()) {
				boolean found = resourceManager.resourceExist(resource);
				if(!found) {
					missingResources.add(resource);
				}
			}
		}
		return missingResources;
	}
	
	@MessageListener
	public void resourceUpdated(ResourceUpdatedMessage message) {
		System.out.println("The resource "+message.getResourceCode()+" was updated");
		while(!pendingMessages.isEmpty())
			messageQueue.addLast(pendingMessages.pop());
	}

	// dispatch loop
	public void run() {
		while(true) {
			if(messageQueue.isEmpty()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			} else {
				AbstractMessage message = messageQueue.pop();
				doSendMessage(message);
				if(message instanceof StopServicesMessage) {
					// System.exit?
					return;
				}
			}
		}
	}
}
