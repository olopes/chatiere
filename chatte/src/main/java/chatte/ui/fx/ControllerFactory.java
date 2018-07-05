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
package chatte.ui.fx;

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
