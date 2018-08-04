package chatte.resources;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.*;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import chatte.msg.Friend;
import chatte.msg.MessageBroker;
import chatte.msg.ResourceMessage;
import chatte.msg.ResourceRequestMessage;
import chatte.msg.ResourceUpdatedMessage;

public class ResourceRequestHandlerTest {

	ResourceRequestHandler instance;
	ResourceManager resourceManager;
	MessageBroker messageBroker;
	
	@Before
	public void setUp() throws Exception {
		resourceManager = mock(ResourceManager.class);
		messageBroker = mock(MessageBroker.class);
		instance = new ResourceRequestHandler(resourceManager, messageBroker); 
	}

	@Test
	public void constructorShouldSetPropertiesToGivenParameters() {
		assertSame("The instance's resourceManager is the same as out mock", resourceManager, instance.resourceManager);
		assertSame("The instance's mesageBroker is the same as out mock", messageBroker, instance.messageBroker);
	}

	@Test
	public void sendRequestedResourceShouldIgnoreLocalMessages() {
		ResourceRequestMessage message = mock(ResourceRequestMessage.class);
		doReturn(false).when(message).isRemote();
		
		instance.sendRequestedResource(message);
		
		verify(message).isRemote();
		verifyNoMoreInteractions(message);
	}

	// FIXME maybe I should refactor this test?
	// How to improve this?
	@Test
	public void sendRequestedResourceShouldIterateAllResourcesAndSendNewResourceMessageWithResourceContents() throws Exception {
		// Prepare test data consisting on a message that references 3 resources
		// resource1 is gif and has 1 byte;
		// resource2 is jpg and has 2 bytes;
		// resource3 is png and has 3 bytes;
		String resource1, resource2, resource3;
		List<String> resources = Arrays.asList(resource1="AAAAAA", resource2="BBBBBB", resource3="CCCCCC");
		Friend friend = mock(Friend.class);
		ResourceRequestMessage message = mock(ResourceRequestMessage.class);
		doReturn(true).when(message).isRemote();
		doReturn(new LinkedHashSet<>(resources)).when(message).getResources();
		doReturn(friend).when(message).getFrom();
		doReturn(new File(resource1+".gif"),new File(resource2+".jpg"),new File(resource3+".png"))
		.when(resourceManager).getResourceFile(anyString());
		doReturn(new byte[1],new byte[2],new byte[3])
		.when(resourceManager).getResourceData(anyString());
		
		
		// ACT NOW!
		instance.sendRequestedResource(message);
		
		
		// Retrieve the messages sent
		ArgumentCaptor<ResourceMessage> messageCaptor = ArgumentCaptor.forClass(ResourceMessage.class);
		verify(messageBroker, times(3)).sendMessage(messageCaptor.capture());

		List<ResourceMessage> allMessages = messageCaptor.getAllValues();
		ResourceMessage messageSent1 = allMessages.get(0);
		ResourceMessage messageSent2 = allMessages.get(1);
		ResourceMessage messageSent3 = allMessages.get(2);
		
		// check messages contents
		assertSame("Fist message ressource destination is same friend",   friend, messageSent1.getFrom());
		assertSame("Second message ressource destination is same friend", friend, messageSent2.getFrom());
		assertSame("Third message ressource destination is same friend",  friend, messageSent3.getFrom());
		
		assertEquals("Fist message is a GIF",   "gif", messageSent1.getType());
		assertEquals("Second message is a JPG", "jpg", messageSent2.getType());
		assertEquals("Third message is a PNG",  "png", messageSent3.getType());
		
		assertArrayEquals("Fist message ressource has 1 byte",    new byte[1], messageSent1.getContents());
		assertArrayEquals("Second message ressource has 2 bytes", new byte[2], messageSent2.getContents());
		assertArrayEquals("Third message ressource has 3 bytes",  new byte[3], messageSent3.getContents());
		
	}

	@Test
	public void sendResourceShouldRegisterNewResourceAndSendMessageNotificationOfNewResourceReception() throws Exception {
		// 
		byte [] contents = new byte[] {'d','a','t','a'};
		String type = "gif";
		Friend friend = mock(Friend.class);
		ResourceMessage message = mock(ResourceMessage.class);
		doReturn(true).when(message).isRemote();
		doReturn(contents).when(message).getContents();
		doReturn(friend).when(message).getFrom();
		doReturn(type).when(message).getType();
		String resourceCode = "NEW_RESOURCE";
		doReturn(resourceCode).when(resourceManager).addResource(any(byte[].class), any(String.class));
		
		// act
		instance.sendResource(message);
		
		// assert calls and new message contents
		verify(resourceManager).addResource(aryEq(contents), eq(type));
		
		ArgumentCaptor<ResourceUpdatedMessage> messageCaptor = ArgumentCaptor.forClass(ResourceUpdatedMessage.class);
		verify(messageBroker, times(1)).sendMessage(messageCaptor.capture());
		ResourceUpdatedMessage notifMessage = messageCaptor.getValue();
		assertEquals("Notification message resource code must be NEW_RESOURCE", resourceCode, notifMessage.getResourceCode());
		assertSame("Notification message from the same test friend", friend, notifMessage.getFrom());
	}

	@Test
	public void sendResourceShouldIgnoreLocalMessages() {
		ResourceMessage message = mock(ResourceMessage.class);
		doReturn(false).when(message).isRemote();
		
		instance.sendResource(message);
		
		verify(message).isRemote();
		verifyNoMoreInteractions(message);
	}

}
