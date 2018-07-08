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
import java.util.logging.Level;
import java.util.logging.Logger;

import chatte.resources.ResourceManager;

public class ChatoMessageBroker implements MessageBroker, Runnable {
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	private static class Listener {
		Method method;
		Object instance;
		private Logger log = getLogger();
		Logger getLogger() {
			return Logger.getLogger(getClass().getName());
		}

		public Listener(Object instance, Method method) {
			this.instance = instance;
			this.method = method;
		}

		void call(AbstractMessage message) {
			try {
				log.fine("Calling..."); //$NON-NLS-1$
				this.method.invoke(this.instance, message);
				log.fine("done!"); //$NON-NLS-1$
			} catch (Exception e) {
				log.log(Level.SEVERE, "method call failed with exception", e); //$NON-NLS-1$
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

		log.info("Registering new listener of type "+listenClass.getSimpleName()); //$NON-NLS-1$
		
		for(Method method : listenClass.getMethods()) {
			log.fine("   checking method "+method.getName()); //$NON-NLS-1$
			MessageListener listenerAnnotation = method.getAnnotation(MessageListener.class);
			if(listenerAnnotation == null) continue;
			log.fine("      Annotation found!"); //$NON-NLS-1$
			Class<? extends AbstractMessage> messageType = listenerAnnotation.forMessage();
			log.fine("      Message type is "+messageType.getSimpleName()); //$NON-NLS-1$
			if(messageType == AbstractMessage.class) {
				log.fine("          AbstractMessage or default value. Inspecting method params..."); //$NON-NLS-1$
				// default value - check parameters
				Class<?> [] paramTypes = method.getParameterTypes();
				if(paramTypes.length == 1 && AbstractMessage.class.isAssignableFrom(paramTypes[0])) {
					@SuppressWarnings("unchecked")
					Class<? extends AbstractMessage> paramType = (Class<? extends AbstractMessage>) paramTypes[0];
					messageType = paramType;
					log.fine("          one parameter of type "+paramType.getSimpleName()); //$NON-NLS-1$
				}
			}
			if(messageType != AbstractMessage.class) {
				// all good! register type
				List<Listener> typeListeners = listeners.get(messageType);
				if(typeListeners == null)
					listeners.put(messageType, typeListeners = new LinkedList<>());
				typeListeners.add(new Listener(listenerInstance, method));
				log.info("   listener method registered"); //$NON-NLS-1$
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

		log.fine("doSend message "+message.getClass().getSimpleName()); //$NON-NLS-1$
		// Check if all resources are available
		Set<String> missingResources = missingRessources(message);
		boolean messageComplete = missingResources.isEmpty();
		message.setComplete(messageComplete);
		
		if(messageComplete) {
			log.fine("Message complete"); //$NON-NLS-1$
			Class<? extends AbstractMessage> messageClass = message.getClass();
			List<Listener> typeListeners = listeners.get(messageClass);
			log.fine("Listeners for this message type: "+typeListeners); //$NON-NLS-1$
			if(typeListeners != null && !typeListeners.isEmpty()) {
				for (Listener listener : typeListeners) {
					log.fine("Dispatching message to listener..."); //$NON-NLS-1$
					listener.call(message);
				}
			}
		} else {
			log.fine("Incomplete message. Move it to pending messages"); //$NON-NLS-1$
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
			log.fine("Checking message with resources. "+message.getResourceRefs()); //$NON-NLS-1$
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
		log.fine("The resource "+message.getResourceCode()+" was updated"); //$NON-NLS-1$ //$NON-NLS-2$
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
