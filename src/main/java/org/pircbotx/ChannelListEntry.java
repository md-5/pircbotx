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

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class ChannelListEntry {
	protected final String name;
	protected final int users;
	protected final String topic;

	public ChannelListEntry(String name, int users, String topic) {
		this.name = name;
		this.users = users;
		this.topic = topic;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the users
	 */
	public int getUsers() {
		return users;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}
}
