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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.DccFileTransfer;
import lombok.Data; 
import lombok.EqualsAndHashCode; 
import org.pircbotx.hooks.helpers.BaseEvent;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.helpers.BaseListener;
import org.pircbotx.hooks.helpers.BaseSimpleListener;

/**
 * This method is called whenever a DCC SEND request is sent to the PircBotX.
 * This means that a client has requested to send a file to us.
 * This abstract implementation performs no action, which means that all
 * DCC SEND requests will be ignored by default. If you wish to receive
 * the file, then you may override this method and call the receive method
 * on the DccFileTransfer object, which connects to the sender and downloads
 * the file.
 *  <p>
 * Example:
 * <pre>
 *     DccFileTransfer transfer = event.getTransfer();
 *     // Use the suggested file name.
 *     File file = transfer.getFile();
 *     // Receive the transfer and save it to the file, allowing resuming.
 *     transfer.receive(file, true);
 * </pre>
 *  <p>
 * <b>Warning:</b> Receiving an incoming file transfer will cause a file
 * to be written to disk. Please ensure that you make adequate security
 * checks so that this file does not overwrite anything important!
 *  <p>
 * Each time a file is received, it happens within a new Thread
 * in order to allow multiple files to be downloaded by the PircBotX
 * at the same time.
 *  <p>
 * If you allow resuming and the file already partly exists, it will
 * be appended to instead of overwritten.  If resuming is not enabled,
 * the file will be overwritten if it already exists.
 *  <p>
 * You can throttle the speed of the transfer by calling the setPacketDelay
 * method on the DccFileTransfer object, either before you receive the
 * file or at any moment during the transfer.
 *  <p>
 * The implementation of this method in the PircBotX abstract class
 * performs no actions and may be overridden as required.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 * @see DccFileTransfer
 */
public class IncomingFileTransfer {
	/**
	 * Simple listener that takes event parameters as parameters. See
	 * {@link IncomingFileTransfer} for an explanation on use
	 * @see IncomingFileTransfer
	 */
	public static interface SimpleListener extends BaseSimpleListener {
		/**
		 * Simple Listener for IncomingFileTransfer Events. See {@link IncomingFileTransfer} for a complete description on when
		 * this is called.
		 * @see IncomingFileTransfer
		 * @see SimpleListener
		 */
		public void onIncomingFileTransfer(DccFileTransfer transfer);
	}

	/**
	 * Listener that receives an event. See {@link IncomingFileTransfer} for an explanation
	 * on use and {@link Event} for an explanation on the event.
	 * @see IncomingFileTransfer
	 * @see Event
	 */
	public static interface Listener extends BaseListener {
		/**
		 * Listener for IncomingFileTransfer Events. See {@link IncomingFileTransfer} for a complete description on when
		 * this is called.
		 * @see IncomingFileTransfer
		 * @see Listener
		 */
		public void onIncomingFileTransfer(Event event);
	}

	/**
	 * Event that is passed to all listeners that contains all the given
	 * information. See {@link IncomingFileTransfer} for an explanation on when this is created
	 * <p>
	 * <b>Note:<b> This class and all its subclasses are immutable since
	 * data should not change after creation
	 * @see IncomingFileTransfer
	 * @see Listener
	 */
	@Data
	@EqualsAndHashCode(callSuper=false)
	public static class Event extends BaseEvent {
		protected final DccFileTransfer transfer;

		/**
		 * Default constructor to setup object. Timestamp is automatically set
		 * to current time as reported by {@link System#currentTimeMillis() }
		 * @param transfer The DcccFileTransfer that you may accept.
		 */
		public <T extends PircBotX> Event(T bot, DccFileTransfer transfer) {
			super(bot);
			this.transfer = transfer;
		}
	}
}
