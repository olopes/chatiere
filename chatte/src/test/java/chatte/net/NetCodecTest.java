package chatte.net;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import chatte.msg.AbstractMessage;
import chatte.msg.ChatMessage;
import chatte.msg.Friend;
import chatte.msg.InboundMessage;
import chatte.msg.MessageBroker;
import chatte.msg.OutboundMessage;
import chatte.msg.ResourceMessage;
import chatte.msg.ResourceRequestMessage;
import chatte.msg.WelcomeMessage;

public class NetCodecTest {

	MessageBroker messageBroker;
	
	NetCodec instance;
	
	@Before
	public void setUp() throws Exception {
		messageBroker = mock(MessageBroker.class);
		instance = new NetCodec(messageBroker);
//				WelcomeMessage.class,
//				ChatMessage.class,
//				ResourceMessage.class,
//				ResourceRequestMessage.class,

	}

	@Test
	public void unmarshalMarshalWelcomeMessageDoesNotThrowsException() throws Exception {
		
		byte[] contents = WELC.getBytes(StandardCharsets.UTF_8);
		
		WelcomeMessage message = (WelcomeMessage)instance.unmarshalMessage(contents);
		
		byte [] result = instance.marshalMessage(message);
		
		assertTrue("WelcomeMessage run finished successfuly", result != null && result.length > 0);
	}

	@Test
	public void unmarshalMarshalChatMessageDoesNotThrowsException() throws Exception {
		byte[] contents = MESS.getBytes(StandardCharsets.UTF_8);
		
		ChatMessage message = (ChatMessage)instance.unmarshalMessage(contents);
		
		byte [] result = instance.marshalMessage(message);
		
		assertTrue("ChatMessage run finished successfuly", result != null && result.length > 0);
	}

	@Test
	public void unmarshalMarshalResourceRequestMessageDoesNotThrowsException() throws Exception {
		byte[] contents = RERE.getBytes(StandardCharsets.UTF_8);
		
		ResourceRequestMessage message = (ResourceRequestMessage)instance.unmarshalMessage(contents);
		
		byte [] result = instance.marshalMessage(message);
		
		assertTrue("ResourceRequestMessage run finished successfuly", result != null && result.length > 0);
	}

	@Test
	public void unmarshalMarshalResourceMessageDoesNotThrowsException() throws Exception {
		byte[] contents = REME.getBytes(StandardCharsets.UTF_8);
		
		ResourceMessage message = (ResourceMessage)instance.unmarshalMessage(contents);
		
		byte [] result = instance.marshalMessage(message);
		
		assertTrue("ResourceMessage run finished successfuly", result != null && result.length > 0);
	}

	@Test
	public void testConvertMessage() throws Exception {
		instance = spy(instance);
		doReturn(new byte[] {'X','X','X'}).when(instance).marshalMessage(any(AbstractMessage.class));
		AbstractMessage message = mock(AbstractMessage.class);
		
		OutboundMessage actual = instance.convertMessage(message);
		
		assertNotNull("OutboundMessage was created", actual);
		assertArrayEquals(new byte[] {'X', 'X','X'}, actual.getContents());
	}
	
	@Test
	public void sendResourceRequestInboundMessageShouldUnmarshalAndDispatchMessage() throws Exception {
		instance = spy(instance);
		AbstractMessage unmarshalled = mock(AbstractMessage.class);
		doReturn(unmarshalled).when(instance).unmarshalMessage(any(byte[].class));
		Friend friend = mock(Friend.class);
		InboundMessage message = new InboundMessage();
		message.setContents(new byte[] {'X','X','X'});
		message.setRemote(true);
		message.setFrom(friend);
		
		instance.sendResourceRequest(message);
		
		InOrder inOrder = inOrder(instance, messageBroker, unmarshalled);
		inOrder.verify(instance).unmarshalMessage(aryEq(new byte[] {'X','X','X'}));
		inOrder.verify(unmarshalled).setRemote(eq(true));
		inOrder.verify(unmarshalled).setFrom(same(friend));
		inOrder.verify(messageBroker).sendMessage(same(unmarshalled));
		
	}

	@Test
	public void convertAndDispatchShouldConvertMessageAndSendItImmediatly() throws Exception {
		instance = spy(instance);
		OutboundMessage converted = mock(OutboundMessage.class);
		doReturn(converted).when(instance).convertMessage(any(AbstractMessage.class));
		AbstractMessage message = mock(AbstractMessage.class);
		
		instance.convertAndDispatch(message);
		
		InOrder inOrder = inOrder(instance, messageBroker);
		inOrder.verify(instance).convertMessage(same(message));
		inOrder.verify(messageBroker).sendMessage(same(converted));
		inOrder.verifyNoMoreInteractions();

	}
	
	// messages
	static final String FRIEND_DATA = "<host>remote</host><nick>PAL</nick><port>4321</port>";
	static final String WELC = "<WELC>"
			+ "<complete>false</complete>"
			+ "<remote>false</remote>"
			+ "<resourcesRequested>false</resourcesRequested>"
			+ "<knownFriends>"+FRIEND_DATA+"</knownFriends>"
			+ "<knownFriends>"+FRIEND_DATA+"</knownFriends>"
			+ "<nick>NÎÇK</nick>"
			+ "<port>1234</port>"
			+ "</WELC>";
	
	static final String MESS = "<MESS>"
			+ "<complete>false</complete>"
			+ "<remote>false</remote>"
			+ "<resourceRefs>RESOURCE</resourceRefs>"
			+ "<resourcesRequested>false</resourcesRequested>"
			+ "<message>hello</message>"
			+ "<nick>NICK</nick>"
			+ "</MESS>";
	
	static final String RERE = "<RERE>"
			+ "<complete>false</complete>"
			+ "<remote>false</remote>"
			+ "<resourcesRequested>false</resourcesRequested>"
			+ "<resources>RESOURCE2</resources>"
			+ "<resources>RESOURCE1</resources>"
			+ "</RERE>";
	
	static final String REME = "<REME>"
			+ "<complete>false</complete>"
			+ "<remote>false</remote>"
			+ "<resourcesRequested>false</resourcesRequested>"
			+ "<contents>RkdISgH2Cg==</contents>"
			+ "<type>gif</type>"
			+ "</REME>";

}
