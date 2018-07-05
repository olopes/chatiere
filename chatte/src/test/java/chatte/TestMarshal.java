package chatte;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import chatte.msg.AbstractMessage;
import chatte.msg.ChatMessage;
import chatte.msg.Friend;
import chatte.msg.WelcomeMessage;

public class TestMarshal {
	private static String marshalingExample(AbstractMessage message) throws JAXBException
	{
	    JAXBContext jaxbContext = JAXBContext.newInstance(message.getClass());
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	 
	    // jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	     
	    //Marshal the employees list in console
	    // jaxbMarshaller.marshal(message, System.out);
	     
	    //Marshal the employees list in file
	    StringWriter sw = new StringWriter();
	    jaxbMarshaller.marshal(message, sw);
	    return sw.toString();
	}
	
	public static void main(String[] args) throws Exception {
		Friend me = new Friend();
		me.setHost("localhost");
		me.setPort(1234);
		me.setNick("Me self");
		Friend other = new Friend();
		me.setHost("otherhost");
		me.setPort(4321);
		me.setNick("Otter");
		
		ChatMessage message = new ChatMessage();
		message.setFrom(me);
		message.setMessage("Hi there!<br/>How are you?");
		message.setComplete(true);
		message.setNick("Maria");
		message.setRemote(false);
		message.setResourceRefs(new HashSet<String>());

		WelcomeMessage welcome = new WelcomeMessage();
		welcome.setFrom(me);
		welcome.setKnownFriends(Arrays.asList(other));
		welcome.setNick("Xico");
		welcome.setPort(1029);
		
		System.out.println("Serialize chat message");
		String msgStr = marshalingExample(message);
		System.out.println(msgStr);
		
		System.out.println("Serialize welcome message");
		String wlcStr = marshalingExample(welcome);
		System.out.println(wlcStr);


	}

}
