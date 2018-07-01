package chatte.fx;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import chatte.config.ConfigService;
import chatte.msg.MessageBroker;
import chatte.resources.ResourceManager;
import javafx.fxml.FXML;
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
		
		// Try to inject factory class
		Class<?> factoryClass = getClass();
		Field [] fields = clazz.getDeclaredFields();
		for(Field fld : fields) {
			FXML fxml = fld.getAnnotation(FXML.class);
			if(fxml != null && fld.getType().isAssignableFrom(factoryClass)) {
				try {
					fld.setAccessible(true);
					fld.set(instance, this);
					break;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return instance;
	}

}
