/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pircbotx.hooks;

import org.pircbotx.User;
import org.pircbotx.hooks.helpers.BaseEvent;
import org.pircbotx.hooks.helpers.BaseListener;
import org.pircbotx.hooks.helpers.BaseSimpleListener;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class Motd {
	/**
	 * Simple listener that takes event parameters as parameters. See
	 * {@link Motd} for an explanation on use
	 * @see Motd
	 */
	public static interface SimpleListener extends BaseSimpleListener {
		/**
		 * Simple Listener for NickChange Events. See {@link Motd} for a complete description on when
		 * this is called.
		 * @param motd The full motd separated by newlines (<code>\n</code>)
		 * @see Motd
		 * @see SimpleListener
		 */
		public void onNickChange(String motd);
	}

	/**
	 * Listener that receives an event. See {@link Motd} for an explanation
	 * on use and {@link Event} for an explanation on the event.
	 * @see Motd
	 * @see Event
	 */
	public static interface Listener extends BaseListener {
		/**
		 * Listener for NickChange Events. See {@link Motd} for a complete description on when
		 * this is called.
		 * @see Motd
		 * @see Listener
		 */
		public void onNickChange(Event event);
	}

	/**
	 * Event that is passed to all listeners that contains all the given
	 * information. See {@link Motd} for an explanation on when this is created
	 * <p>
	 * <b>Note:<b> This class and all its subclasses are immutable since
	 * data should not change after creation
	 * @see Motd
	 * @see Listener
	 */
	public static class Event implements BaseEvent {
		protected final long timestamp;
		protected final String motd;

		/**
		 * Default constructor to setup object. Timestamp is automatically set
		 * to current time as reported by {@link System#currentTimeMillis() }
		 * @param motd The full motd separated by newlines (<code>\n</code>)
		 */
		public Event(String motd) {
			this.timestamp = System.currentTimeMillis();
			this.motd = motd;
		}

		public String getMotd() {
			return motd;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}
}
