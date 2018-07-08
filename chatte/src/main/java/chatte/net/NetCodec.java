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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import chatte.msg.AbstractMessage;
import chatte.msg.ChatMessage;
import chatte.msg.InboundMessage;
import chatte.msg.MessageBroker;
import chatte.msg.MessageListener;
import chatte.msg.OutboundMessage;
import chatte.msg.ResourceMessage;
import chatte.msg.ResourceRequestMessage;
import chatte.msg.TypedMessage;
import chatte.msg.WelcomeMessage;

public class NetCodec {
	private static Logger log = Logger.getLogger(NetCodec.class.getName());
	static final JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(
					WelcomeMessage.class,
					ChatMessage.class,
					ResourceMessage.class,
					ResourceRequestMessage.class
					);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	static byte[] marshalMessage(AbstractMessage message) {
		try {
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			// force UTF-8
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
			// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			jaxbMarshaller.marshal(message, output);
			return output.toByteArray();
		} catch(Exception e) {
			log.log(Level.SEVERE, "Something bad happened", e); //$NON-NLS-1$
		}
		return null;
	}
	
	static AbstractMessage unmarshalMessage(byte [] contents) {
		AbstractMessage message = null;
		try {
			Unmarshaller jaxbMarshaller = jaxbContext.createUnmarshaller();
			// force UTF-8
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
			// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			ByteArrayInputStream input = new ByteArrayInputStream(contents);
			message = (AbstractMessage)jaxbMarshaller.unmarshal(input);
		} catch(Exception e) {
			log.log(Level.SEVERE, "Something bad happened", e); //$NON-NLS-1$
		}
		return message;
	}
	
	static OutboundMessage convertMessage(AbstractMessage message) {
		return new OutboundMessage(marshalMessage(message));
	}
	
	MessageBroker messageBroker;
	public NetCodec(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
	}
	
	// message received
	@MessageListener
	public void sendResourceRequest(InboundMessage message) {
		AbstractMessage receivedMessage = unmarshalMessage(message.getContents());
		if(receivedMessage != null) {
			receivedMessage.setRemote(message.isRemote());
			receivedMessage.setFrom(message.getFrom());
			messageBroker.sendMessage(receivedMessage);
		}
	}
	
	void convertAndDispatch(AbstractMessage message) {
		messageBroker.sendMessage(convertMessage(message));
	}
	
	@MessageListener
	public void sendResourceRequest(WelcomeMessage message) {
		convertAndDispatch(message);
	}
	
	@MessageListener
	public void sendResourceRequest(TypedMessage message) {
		convertAndDispatch(new ChatMessage(message));
	}
	
	@MessageListener
	public void sendResourceRequest(ResourceMessage message) {
		convertAndDispatch(message);
	}
	
	@MessageListener
	public void sendResourceRequest(ResourceRequestMessage message) {
		convertAndDispatch(message);
	}
	
}
