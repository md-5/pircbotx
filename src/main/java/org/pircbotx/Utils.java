/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.pircbotx;

/**
 *
 * @author  Forked by Leon Blakey as part of the PircBotX project
 *          <a href="http://pircbotx.googlecode.com">http://pircbotx.googlecode.com/</a>
 * @version    2.0 Alpha
 */
public class Utils {
	public static boolean isBlank(String str) {
		return (str != null) && (str.trim().equals(""));
	}
}
