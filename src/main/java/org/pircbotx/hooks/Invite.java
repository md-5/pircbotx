/*
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of pircbotx.
 *
 * pircbotx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pircbotx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with pircbotx.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pircbotx.hooks;

import org.pircbotx.Channel;
import org.pircbotx.User;
import lombok.Data; 
import lombok.EqualsAndHashCode; 
import org.pircbotx.hooks.helpers.BaseEvent;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.helpers.BaseListener;
import org.pircbotx.hooks.helpers.BaseSimpleListener;

/**
 * Called when we are invited to a channel by a user.
 *  <p>
 * The implementation of this method in the PircBotX abstract class
 * performs no actions and may be overridden as required.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class Invite {
	/**
	 * Simple listener that takes event parameters as parameters. See 
	 * {@link Invite} for an explanation on use 
	 * @see Invite 
	 */
	public static interface SimpleListener extends BaseSimpleListener {
		/**
		 * Simple Listener for Invite Events. See {@link Invite} for a complete description on when
		 * this is called.
		 * @param source The user that sent the invitation.
		 * @param channel The channel that we're being invited to.
		 * @see Invite
		 * @see SimpleListener
		 */
		public void onInvite(User source, Channel channel);
	}

	/**
	 * Listener that receives an event. See {@link Invite} for an explanation 
	 * on use and {@link Event} for an explanation on the event. 
	 * @see Invite 
	 * @see Event 
	 */
	public static interface Listener extends BaseListener {
		/**
		 * Listener for Invite Events. See {@link Invite} for a complete description on when
		 * this is called.
		 * @see Invite
		 * @see Listener
		 */
		public void onInvite(Event event);
	}

	/**
	 * Event that is passed to all listeners that contains all the given
	 * information. See {@link Invite} for an explanation on when this is created
	 * <p>
	 * <b>Note:<b> This class and all its subclasses are immutable since
	 * data should not change after creation
	 * @see Invite 
	 * @see Listener
	  */
	@Data
	@EqualsAndHashCode(callSuper=false)
	public static class Event extends BaseEvent {
		protected final User source;
		protected final Channel channel;

		/**
		 * Default constructor to setup object. Timestamp is automatically set
		 * to current time as reported by {@link System#currentTimeMillis() }
		 * @param source The user that sent the invitation.
		 * @param channel The channel that we're being invited to.
		 */
		public <T extends PircBotX> Event(T bot, User source, Channel channel) {
			super(bot);
			this.source = source;
			this.channel = channel;
		}
	}
}
