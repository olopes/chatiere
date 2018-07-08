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
package chatte.plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.PluginRegistedMessage;
import chatte.plugin.spi.ChattePluginProvider;
import chatte.ui.fx.ChatteContext;

public class ChattePluginLoader {

	static List<ChattePlugin> knownPlugins;

	public static synchronized List<ChattePlugin> loadPlugins(ChatteContext context) {

		if (ChattePluginLoader.knownPlugins == null) {
			Logger logger = Logger.getLogger(ChattePluginLoader.class.getName());
			MessageBroker messageBroker = context.getMessageBroker();
			Friend me = context.getMyself();

			List<ChattePlugin> knownPlugins = new LinkedList<>();
			ServiceLoader<ChattePluginProvider> loader = ServiceLoader.load(ChattePluginProvider.class, ChattePluginProvider.class.getClassLoader());
			for (ChattePluginProvider provider : loader) {
				ChattePlugin plugin = provider.create(context);
				knownPlugins.add(plugin);
				messageBroker.sendMessage(new PluginRegistedMessage(me, plugin.getName(), plugin.getDescription(), plugin.getClass().getName()));
				logger.info(String.format("Plugin %s (%s) registered", plugin.getName(), plugin.getClass().getName())); //$NON-NLS-1$
			}
			ChattePluginLoader.knownPlugins = Collections.unmodifiableList(knownPlugins);
		}

		return ChattePluginLoader.knownPlugins;
	}

}
