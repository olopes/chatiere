package chatte.fx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.resources.ResourceManager;
import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object> {

	MessageBroker messageBroker;
	ResourceManager resourceManager;
	ConfigService configService;

	public ControllerFactory(ConfigService configService, ResourceManager resourceManager, MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
		this.resourceManager=resourceManager;
		this.configService=configService;
	}

	@Override
	public Object call(Class<?> clazz) {
		Object instance = null;
		try {
			try {
				Constructor<?> const1 = clazz.getConstructor(ConfigService.class, ResourceManager.class, MessageBroker.class);
				instance = const1.newInstance(configService, resourceManager, messageBroker);
			} catch (NoSuchMethodException e) {
				instance = clazz.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return instance;
	}

}
