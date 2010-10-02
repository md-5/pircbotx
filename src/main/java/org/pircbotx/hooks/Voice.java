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
package org.pircbotx.hooks;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.helpers.BaseEvent;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.helpers.BaseListener;
import org.pircbotx.hooks.helpers.BaseSimpleListener;

/**
 * Called when a user (possibly us) gets voice status granted in a channel.
 *  <p>
 * This is a type of mode change and is also passed to the onMode
 * method in the PircBotX class.
 *  <p>
 * The implementation of this method in the PircBotX abstract class
 * performs no actions and may be overridden as required.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class Voice {
	/**
	 * Simple listener that takes event parameters as parameters. See 
	 * {@link Voice} for an explanation on use 
	 * @see Voice 
	 */
	public static interface SimpleListener extends BaseSimpleListener {
		/**
		 * Simple Listener for Voice Events. See {@link Voice} for a complete description on when
		 * this is called.
		 * @param channel The channel in which the mode change took place.
		 * @param source The user that performed the mode change.
		 * @param recipient The nick of the user that got 'voiced'.
		 * @see Voice
		 * @see SimpleListener
		 */
		public void onVoice(Channel channel, User source, User recipient);
	}

	/**
	 * Listener that receives an event. See {@link Voice} for an explanation 
	 * on use and {@link Event} for an explanation on the event. 
	 * @see Voice 
	 * @see Event 
	 */
	public static interface Listener extends BaseListener {
		/**
		 * Listener for Voice Events. See {@link Voice} for a complete description on when
		 * this is called.
		 * @see Voice
		 * @see Listener
		 */
		public void onVoice(Event event);
	}

	/**
	 * Event that is passed to all listeners that contains all the given
	 * information. See {@link Voice} for an explanation on when this is created
	 * <p>
	 * <b>Note:<b> This class and all its subclasses are immutable since
	 * data should not change after creation
	 * @see Voice 
	 * @see Listener
	 */
	public static class Event extends BaseEvent {
		protected final Channel channel;
		protected final User source;
		protected final User recipient;

		/**
		 * Default constructor to setup object. Timestamp is automatically set
		 * to current time as reported by {@link System#currentTimeMillis() }
		 * @param channel The channel in which the mode change took place.
		 * @param source The user that performed the mode change.
		 * @param recipient The nick of the user that got 'voiced'.
		 */
		public <T extends PircBotX> Event(T bot, Channel channel, User source, User recipient) {
			super(bot);
			this.channel = channel;
			this.source = source;
			this.recipient = recipient;
		}

		public Channel getChannel() {
			return channel;
		}

		public User getRecipient() {
			return recipient;
		}

		public User getSource() {
			return source;
		}
	}
}
