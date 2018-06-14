package chatte;

import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.resources.ResourceManager;

public interface ChatteServices {

	ConfigService getConfigService();
	ResourceManager getResourceManager();
	MessageBroker getMessageBroker();

}
