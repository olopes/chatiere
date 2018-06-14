package chatte;

import java.net.URL;

import chatte.config.ConfigService;
import chatte.config.ConfigServiceImpl;
import chatte.msg.ChatoMessageBroker;
import chatte.msg.MessageBroker;
import chatte.net.ChatoURLStreamHandlerFactory;
import chatte.resources.ChatoResourceManager;
import chatte.resources.ResourceManager;
import chatte.resources.ResourceRequestHandler;

public class ChatteServicesFactory implements ChatteServicesBuilder {

	@Override
	public ChatteServices buildServices() {
		final ConfigService configService = new ConfigServiceImpl();
		final ResourceManager resourceManager = new ChatoResourceManager();
		final MessageBroker messageBroker = new ChatoMessageBroker(resourceManager);

		// RegisterCustom URL handler
		ResourceRequestHandler resourceRequestHandler = new ResourceRequestHandler(resourceManager, messageBroker);
		messageBroker.addListener(resourceRequestHandler);
		URL.setURLStreamHandlerFactory(new ChatoURLStreamHandlerFactory(resourceManager));

		// XXX disable network for now...
//		chatte.net.MsgListener msgListener = new chatte.net.MsgListener(messageBroker, configService);
//		
//		new Thread(msgListener, "network service").start();


		return new ChatteServices() {
			
			@Override
			public ResourceManager getResourceManager() {
				return resourceManager;
			}
			
			@Override
			public MessageBroker getMessageBroker() {
				return messageBroker;
			}
			
			@Override
			public ConfigService getConfigService() {
				return configService;
			}
		};
	}

}
