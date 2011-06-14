/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.pircbotx;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.ChannelInfoEvent;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.OpEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.UserListEvent;
import org.pircbotx.hooks.events.UserModeEvent;
import org.pircbotx.hooks.events.VoiceEvent;
import org.pircbotx.hooks.managers.GenericListenerManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Usability tests for PircBotX that test how PircBotX handles lines and events.
 * Any other tests not involving processing lines should be in PircBotXTest
 * 
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class PircBotXProcessingTest {
	final PircBotX bot = new PircBotX();
	final Set<Event> events = new HashSet<Event>();
	final String aString = "I'm some super long string that has multiple words";
	
	/**
	 * General bot setup: Use GenericListenerManager (no threading), add custom
	 * listener to add all called events to Event set, set nick, etc
	 */
	@BeforeClass
	public void setup() {
		bot.setListenerManager(new GenericListenerManager());
		bot.getListenerManager().addListener(new Listener() {
			public void onEvent(Event event) throws Exception {
				events.add(event);
			}
		});
		bot.setNick("PircBotXUser");
		bot.setName("PircBotXUser");
	}
	
	/**
	 * Test mode being changed
	 */
	@Test
	public void userModeTest() {
		//Use two users to differentiate between source and target
		User aUser = bot.getUser("PircBotXUser");
		User aUser2 = bot.getUser("PircBotXUser2");
		events.clear();
		bot.handleLine(":PircBotXUser MODE PircBotXUser2 :+i");
		
		//Verify event contents
		UserModeEvent uevent = getEvent(UserModeEvent.class, "UserModeEvent not dispatched on change");
		assertEquals(uevent.getSource(), aUser, "UserModeEvent's source does not match given");
		assertEquals(uevent.getTarget(), aUser2, "UserModeEvent's target does not match given");
		assertEquals(uevent.getMode(), "+i", "UserModeEvent's mode does not match given");

		//Cleanup
		bot._userChanInfo.deleteB(aUser);
		bot._userChanInfo.deleteB(aUser2);
		
		System.out.println("Success: UserModeEvent gives expected results");
	}
	
	/**
	 * Simulate /LIST response with 3 channels
	 */
	@Test(dependsOnMethods="userModeTest")
	public void listTest() {
		events.clear();
		bot.handleLine(":irc.someserver.net 321 Channel :Users Name");
		bot.handleLine(":irc.someserver.net 322 PircBotXUser #PircBotXChannel 99 :" + aString);
		bot.handleLine(":irc.someserver.net 322 PircBotXUser #PircBotXChannel1 100 :" + aString + aString);
		bot.handleLine(":irc.someserver.net 322 PircBotXUser #PircBotXChannel2 101 :" + aString + aString + aString);
		bot.handleLine(":irc.someserver.net 323 :End of /LIST");
		ChannelInfoEvent cevent = getEvent(ChannelInfoEvent.class, "No ChannelInfoEvent dispatched");
		Set<ChannelListEntry> channels = cevent.getList();
		assertEquals(channels.size(), 3);
		boolean channelParsed = false;
		for (ChannelListEntry entry : channels)
			if (entry.getName().equals("#PircBotXChannel1")) {
				assertEquals(entry.getName(), "#PircBotXChannel1");
				assertEquals(entry.getTopic(), aString + aString);
				assertEquals(entry.getUsers(), 100);
				channelParsed = true;
			}
		assertTrue(channelParsed, "Channel #PircBotXChannel1 not found in /LIST results!");

		System.out.println("Success: Output from /LIST command gives expected results");
	}
	
	/**
	 * Simulate getting invited to a channel
	 */
	@Test(dependsOnMethods="listTest", description="Hey look, text")
	public void inviteTest() {
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host INVITE PircBotXUser :#aChannel");
		
		//Verify event values
		InviteEvent ievent = getEvent(InviteEvent.class, "No InviteEvent dispatched");
		assertEquals(ievent.getUser(), "AUser", "InviteEvent user is wrong");
		assertEquals(ievent.getChannel(), "#aChannel", "InviteEvent channel is wrong");
		
		//Make sure the event doesn't create a user or a channel
		assertFalse(bot.channelExists("#aChannel"), "InviteEvent created channel, shouldn't have");
		assertFalse(bot.userExists("AUser"), "InviteEvent created user, shouldn't have");
		
		System.out.println("Success: Output from /LIST command gives expected results");
	}
	
	/**
	 * Simulate another using joining the channel we just joined
	 */
	@Test(dependsOnMethods="inviteTest")
	public void joinTest() {
		events.clear();
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		bot.handleLine(":AUser!~ALogin@some.host JOIN :#aChannel");
		
		//Make sure the event gives us the same channels
		JoinEvent jevent = getEvent(JoinEvent.class, "No aChannel dispatched");
		assertEquals(jevent.getChannel(), aChannel, "Event's channel does not match origional channel");
		assertEquals(jevent.getUser(), aUser, "Event's user does not match origional user");
		
		//Make sure user info was updated
		assertEquals(aUser.getLogin(), "~ALogin", "User login wrong on JoinEvent");
		assertEquals(aUser.getHostmask(), "some.host", "User hostmask wrong on JoinEvent");
		Channel userChan = null;
		for(Channel curChan : aUser.getChannels())
			if(curChan.getName().equals("#aChannel"))
				userChan = curChan;
		assertNotNull(userChan, "User is not joined to channel after JoinEvent");
		User chanUser = null;
		for(User curUser : aChannel.getUsers())
			if(curUser.getNick().equals("AUser"))
				chanUser = curUser;
		assertNotNull(chanUser, "Channel is not joined to user after JoinEvent");
		assertTrue(bot.userExists("AUser"));
		
		System.out.println("Success: Information up to date when user joins");
	}
	
	/**
	 * Simulate a /TOPIC (no args) or /JOIN (inital topic sent when joining)
	 */
	@Test(dependsOnMethods="joinTest")
	public void topicTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		bot.handleLine(":irc.someserver.net 332 PircBotXUser #aChannel :" + aString + aString);
		assertEquals(aChannel.getTopic(), aString + aString);

		System.out.println("Success: Topic content output from /TOPIC or /JOIN gives expected results");
	}

	/**
	 * Simulate a /TOPIC info (sent after joining a channel and topic is sent)
	 */
	@Test(dependsOnMethods="topicTest")
	public void topicInfoTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		events.clear();
		bot.handleLine(":irc.someserver.net 333 PircBotXUser #aChannel AUser 1268522937");
		assertEquals(aChannel.getTopicSetter(), "AUser");
		assertEquals(aChannel.getTopicTimestamp(), (long) 1268522937 * 1000);

		TopicEvent tevent = getEvent(TopicEvent.class, "No topic event dispatched");
		assertEquals(tevent.getChannel(), aChannel, "Event channel and origional channel do not match");
		
		System.out.println("Success: Topic info output from /TOPIC or /JOIN gives expected results");
	}
	
	/**
	 * Simulate a channel message being sent
	 */
	@Test(dependsOnMethods="topicInfoTest")
	public void messageTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host PRIVMSG #aChannel :" + aString);
		
		//Verify event contents
		MessageEvent mevent = getEvent(MessageEvent.class, "MessageEvent not dispatched");
		assertEquals(mevent.getChannel(), aChannel, "Event channel and origional channel do not match");
		assertEquals(mevent.getUser(), aUser, "Event user and origional user do not match");
		assertEquals(mevent.getMessage(), aString, "Message sent does not match");
		
		System.out.println("Success: MessageEvent gives expected results");
	}
	
	/**
	 * Simulate a private message from a user that's already a part of one of our channels
	 */
	@Test(dependsOnMethods="messageTest")
	public void privateMessageTest() {
		User aUser = bot.getUser("AUser");
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host PRIVMSG PircBotXUser :" + aString);
		
		//Verify event contents
		PrivateMessageEvent pevent = getEvent(PrivateMessageEvent.class, "MessageEvent not dispatched");
		assertEquals(pevent.getUser(), aUser, "Event user and origional user do not match");
		assertEquals(pevent.getMessage(), aString, "Message sent does not match");
		
		System.out.println("Success: MessageEvent gives expected results");
	}
	
	/**
	 * Simulate a NOTICE sent to the channel
	 */
	@Test(dependsOnMethods="privateMessageTest")
	public void channelNoticeTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host NOTICE #aChannel :" + aString);
		
		//Verify event contents
		NoticeEvent nevent = getEvent(NoticeEvent.class, "NoticeEvent not dispatched for channel notice");
		assertEquals(nevent.getChannel(), aChannel, "NoticeEvent's channel does not match given");
		assertEquals(nevent.getUser(), aUser, "NoticeEvent's user does not match given");
		assertEquals(nevent.getNotice(), aString, "NoticeEvent's notice message does not match given");
		
		System.out.println("Success: NoticeEvent gives expected results");
	}
	
	/**
	 * Simulate a NOTICE sent directly to us
	 */
	@Test(dependsOnMethods="channelNoticeTest")
	public void userNoticeTest() {
		User aUser = bot.getUser("AUser");
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host NOTICE PircBotXUser :" + aString);
		
		//Verify event contents
		NoticeEvent nevent = getEvent(NoticeEvent.class, "NoticeEvent not dispatched for channel notice");
		assertNull(nevent.getChannel(), "NoticeEvent's channel isn't null during private notice");
		assertEquals(nevent.getUser(), aUser, "NoticeEvent's user does not match given");
		assertEquals(nevent.getNotice(), aString, "NoticeEvent's notice message does not match given");
		
		System.out.println("Success: NoticeEvent gives expected results");
	}
	
	/**
	 * Simulate a generic channel mode change
	 */
	@Test(dependsOnMethods="userNoticeTest")
	public void genericModeTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host MODE #aChannel +m");
		
		//Verify event contents
		ModeEvent mevent = getEvent(ModeEvent.class, "No ModeEvent dispatched with +m");
		assertEquals(mevent.getChannel(), aChannel, "ModeEvent's channel does not match given");
		assertEquals(mevent.getUser(), aUser, "ModeEvent's user does not match given");
		assertEquals(mevent.getMode(), "+m", "ModeEvent's mode does not match given mode");
		System.out.println("Success: ModeEvent gives expected results");
		
		//Check channel mode
		assertEquals(aChannel.getMode(), "m", "Channel's mode is not updated");
		
		System.out.println("Success: Channel's mode is up to date");
	}
	
	/**
	 * Check internal ManyToManyMap for any extra values
	 */
	@Test(dependsOnMethods="genericModeTest")
	public void mapCheck1Test() {
		ManyToManyMap<Channel, User> map = bot._userChanInfo;
		
		assertEquals(map.getAValues().size(), 1, "Extra Channel values. Full printout \n " + StringUtils.join(map.getAValues().toArray(), "\n "));
		assertEquals(map.getBValues().size(), 1, "Extra User values. Full printout \n " + StringUtils.join(map.getBValues().toArray(), "\n "));
		
		System.out.println("Success: Map check for extra values #1 is good");
	}
	
	/**
	 * Test opping person that just joined. Note that since joinTest passed
	 * (this test indirectly depends on it), simulating a JOIN is safe
	 */
	@Test(dependsOnMethods="mapCheck1Test")
	public void opTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		events.clear();
		bot.handleLine(":OtherUser!~OtherLogin@some.host1 JOIN :#aChannel");
		User otherUser = bot.getUser("OtherUser");
		bot.handleLine(":AUser!~ALogin@some.host MODE #aChannel +o OtherUser");
		
		//Verify event contents
		OpEvent oevent = getEvent(OpEvent.class, "OpEvent not dispatched");
		assertEquals(oevent.getChannel(), aChannel, "OpEvent's channel does not match given");
		assertEquals(oevent.getSource(), aUser, "OpEvent's source user does not match given");
		assertEquals(oevent.getRecipient(), otherUser, "OpEvent's reciepient user does not match given");
		
		//Check op lists
		assertTrue(aChannel.isOp(otherUser), "Channel's internal Op list not updated with new Op");
		
		System.out.println("Success: Oping a user updates the appropiate places");
	}
	
	/**
	 * Test another user getting voiced
	 */
	@Test(dependsOnMethods="opTest")
	public void voiceTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		User otherUser = bot.getUser("OtherUser");
		events.clear();
		bot.handleLine(":AUser!~ALogin@some.host MODE #aChannel +v OtherUser");
		
		//Verify event contents
		VoiceEvent vevent = getEvent(VoiceEvent.class, "VoiceEvent not dispatched");
		assertEquals(vevent.getChannel(), aChannel, "OpEvent's channel does not match given");
		assertEquals(vevent.getSource(), aUser, "OpEvent's source user does not match given");
		assertEquals(vevent.getRecipient(), otherUser, "OpEvent's reciepient user does not match given");
		
		//Check voice lists
		assertTrue(aChannel.hasVoice(otherUser), "Channel's internal voice list not updated with new voice");
	}
	
	/**
	 * Simulate names response
	 */
	@Test(dependsOnMethods="voiceTest")
	public void namesTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		User otherUser = bot.getUser("OtherUser");
		//Remove from lists, should be added back by command
		aChannel.ops.remove(otherUser);
		aChannel.voices.remove(otherUser);
		events.clear();
		bot.handleLine(":irc.someserver.net 353 PircBotXUser = #aChannel :AUser @+OtherUser");
		
		//Make sure channel op and voice lists are updated
		assertTrue(aChannel.isOp(otherUser), "NAMES response doesn't give user op status");
		assertTrue(aChannel.hasVoice(otherUser), "NAMES response doesn't give user voice status");
	}
	
	/**
	 * Simulate WHO response. 
	 */
	@Test(dependsOnMethods="namesTest")
	public void whoTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		User otherUser = bot.getUser("OtherUser");
		//Remove from lists, should be added back by command
		bot._userChanInfo.deleteB(aUser);
		bot._userChanInfo.deleteB(otherUser);
		events.clear();
		bot.handleLine(":irc.someserver.net 352 PircBotXUser #aChannel ~ALogin some.host irc.someserver.net AUser H@+ :2 " + aString);
		bot.handleLine(":irc.someserver.net 352 PircBotXUser #aChannel ~OtherLogin some.host1 irc.otherserver.net OtherUser G :4 " + aString);
		bot.handleLine(":irc.someserver.net 315 PircBotXUser #aChannel :End of /WHO list.");
		
		//Get new user objects 
		aUser = bot.getUser("AUser");
		otherUser = bot.getUser("OtherUser");
		
		//Verify event
		UserListEvent uevent = getEvent(UserListEvent.class, "UserListEvent not dispatched");
		assertEquals(uevent.getChannel(), aChannel, "UserListEvent's channel does not match given");
		assertEquals(uevent.getUsers().size(), 2, "UserListEvent's users is larger than it should be");
		assertTrue(uevent.getUsers().contains(aUser), "UserListEvent doesn't contain aUser");
		assertTrue(uevent.getUsers().contains(aUser), "UserListEvent doesn't contain OtherUser");
		
		//Verify AUser
		assertEquals(aUser.getNick(), "AUser", "Login doesn't match one given during WHO");
		assertEquals(aUser.getLogin(), "~ALogin", "Login doesn't match one given during WHO");
		assertEquals(aUser.getHostmask(), "some.host", "Host doesn't match one given during WHO");
		assertEquals(aUser.getHops(), 2, "Hops doesn't match one given during WHO");
		assertEquals(aUser.getRealName(), aString, "RealName doesn't match one given during WHO");
		assertEquals(aUser.getServer(), "irc.someserver.net", "Server doesn't match one given during WHO");
		assertFalse(aUser.isAway(), "User is away even though specified as here in WHO");
		assertTrue(aChannel.isOp(aUser),"User isn't labeled as an op even though specified as one in WHO");
		assertTrue(aChannel.hasVoice(aUser),"User isn't voiced even though specified as one in WHO");
		
		//Verify otherUser
		assertEquals(otherUser.getNick(), "OtherUser", "Login doesn't match one given during WHO");
		assertEquals(otherUser.getLogin(), "~OtherLogin", "Login doesn't match one given during WHO");
		assertEquals(otherUser.getHostmask(), "some.host1", "Host doesn't match one given during WHO");
		assertEquals(otherUser.getHops(), 4, "Hops doesn't match one given during WHO");
		assertEquals(otherUser.getRealName(), aString, "RealName doesn't match one given during WHO");
		assertEquals(otherUser.getServer(), "irc.otherserver.net", "Server doesn't match one given during WHO");
		assertTrue(otherUser.isAway(), "User is not away though specified as here in WHO");
		assertFalse(aChannel.isOp(otherUser),"User is labeled as an op even though specified as one in WHO");
		assertFalse(aChannel.hasVoice(otherUser),"User is labeled as voiced even though specified as one in WHO");
		otherUser.setAway(true);
	}
	
	/**
	 * Simulate another user being kicked
	 */
	@Test(dependsOnMethods="whoTest")
	public void kickTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User aUser = bot.getUser("AUser");
		events.clear();
		User otherUser = bot.getUser("OtherUser");
		bot.handleLine(":AUser!~ALogin@some.host KICK #aChannel OtherUser :" + aString);
		
		//Verify event contents
		KickEvent kevent = getEvent(KickEvent.class, "KickEvent not dispatched");
		assertEquals(kevent.getChannel(), aChannel, "KickEvent channel does not match");
		assertEquals(kevent.getSource(), aUser, "KickEvent's getSource doesn't match kicker user");
		assertEquals(kevent.getRecipient(), otherUser, "KickEvent's getRecipient doesn't match kickee user");
		assertEquals(kevent.getReason(), aString, "KickEvent's reason doesn't match given one");
		
		//Make sure we've sufficently forgotten about the user
		assertFalse(bot._userChanInfo.containsB(otherUser), "Bot still has refrence to use even though they were kicked");
		assertFalse(aChannel.isOp(otherUser), "Channel still considers user that was kicked an op");
		assertFalse(aChannel.hasVoice(otherUser), "Channel still considers user that was kicked with voice");
		assertFalse(bot.userExists("OtherUser"), "Bot still considers user to exist after kick");
		
		System.out.println("Success: KickEvent gives expected results");
	}
	
	/**
	 * Test a user quitting
	 */
	@Test(dependsOnMethods="kickTest")
	public void quitTest() {
		Channel aChannel = bot.getChannel("#aChannel");
		User otherUser = bot.getUser("OtherUser");
		events.clear();
		bot.handleLine(":OtherUser!~OtherLogin@some.host1 JOIN :#aChannel");
		bot.handleLine(":AUser!~ALogin@some.host MODE #aChannel +o OtherUser");
		bot.handleLine(":AUser!~ALogin@some.host MODE #aChannel +v OtherUser");
		bot.handleLine(":OtherUser!~OtherLogin@some.host1 QUIT :" + aString);
		
		//Check event contents
		QuitEvent qevent = getEvent(QuitEvent.class, "QuitEvent not dispatched");
		assertEquals(qevent.getUser(), otherUser, "QuitEvent's user does not match given");
		assertEquals(qevent.getReason(), aString, "QuitEvent's reason does not match given");
		
		//Make sure user is gone
		assertFalse(bot._userChanInfo.containsB(otherUser), "Bot still has refrence to use even though they quit");
		assertFalse(aChannel.isOp(otherUser), "Channel still considers user that quit an op");
		assertFalse(aChannel.hasVoice(otherUser), "Channel still considers user that quit with voice");
		assertFalse(bot.userExists("OtherUser"), "Bot still considers user to exist after quit");
		assertTrue(otherUser.getChannels().isEmpty(), "User still connected to other channels after quit");
		assertFalse(aChannel.getUsers().contains(otherUser), "Channel still associated with user that quit");
		
		System.out.println("Success: QuitEvent gives the appropiate information and bot forgets refrences");
	}
	
	/**
	 * Simpler test for QUITs with no reason. We know that the bot will forget
	 * about the user already, so no need to test here as well
	 */
	@Test(dependsOnMethods="quitTest")
	public void quitTest2() {
		Channel aChannel = bot.getChannel("#aChannel");
		User otherUser = bot.getUser("OtherUser");
		events.clear();
		bot.handleLine(":OtherUser!~OtherLogin@some.host1 JOIN :#aChannel");
		bot.handleLine(":OtherUser!~OtherLogin@some.host1 QUIT :");
		
		//Check event contents
		QuitEvent qevent = getEvent(QuitEvent.class, "QuitEvent not dispatched");
		assertEquals(qevent.getUser(), otherUser, "QuitEvent's user does not match given");
		assertEquals(qevent.getReason(), "", "QuitEvent's reason does not match given");
		
		System.out.println("Success: QuitEvent with no reason gives the appropiate information");
	}
	
	/**
	 * After simulating a server response, call this to get a specific Event from
	 * the Event set. Note that if the event does not exist an Assertion error will
	 * be thrown. Also note that only one event will be fetched
	 * @param <B> The event type to be fetched
	 * @param clazz The class of the event type
	 * @param errorMessage An error message if the event type does not exist
	 * @return A single requested event
	 */
	protected <B> B getEvent(Class<B> clazz, String errorMessage) {
		B cevent = null;
		for(Event curEvent : events)
			if(curEvent.getClass().isAssignableFrom(clazz))
				return (B)curEvent;
		//Failed, does not exist
		assertNotNull(cevent, errorMessage);
		return null;
	}
}