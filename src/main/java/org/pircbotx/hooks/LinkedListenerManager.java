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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.ManyToManyMap;
import org.pircbotx.exception.UnknownEventException;
import org.pircbotx.hooks.listeners.ActionListener;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class LinkedListenerManager implements ListenerManager {
	protected final Map<Class<? extends Event>, Class<? extends Listener>> listenerEvents = new HashMap();
	/**
	 * Maps listeners to their type of listener
	 * A - Type of Listener. Eg {@link ActionListener}. Only in Class form
	 * B - Provided listener class. Needs to be a concrete implementation
	 */
	protected final ManyToManyMap<Class<? extends Listener>, Listener> map = new ManyToManyMap();

	public void dispatchEvent(Event event) {
		try {
			//Get the listener
			Class<? extends Listener> listenerClass = listenerEvents.get(event.getClass());

			for (Listener curListener : map.getAValues(listenerClass))
				listenerClass.getDeclaredMethods()[0].invoke(curListener, event);
		} catch (Throwable t) {
			event.getBot().logException(t);
		}
	}

	public void addListener(Listener listener) {
		for (Class<? extends Listener> listenerType : getAllInterfaces(listener.getClass())) {
			//Get the event out of it
			Class<?> rawParamType = listenerType.getMethods()[0].getParameterTypes()[0];
			if (rawParamType.isAssignableFrom(Event.class) && !listenerEvents.containsValue(listenerType))
				listenerEvents.put(rawParamType.asSubclass(Event.class), listenerType);

			//Associate the listener type with the actual listener
			map.put(listenerType, listener);
		}
	}

	public void removeListener(Listener listener) {
		//Delete all associations with listener
		map.deleteB(listener);
	}

	public Set<Listener> getListeners() {
		//Returned map is already unmodifiable
		return map.getBValues();
	}

	protected static Set<Class<? extends Listener>> getAllInterfaces(Class<?> listener) {
		Set<Class<?>> list = new HashSet();

		//Get all interfaces in the entire heirachy
		Class<?> superclass = listener.getSuperclass();
		if (superclass != Object.class && superclass != null)
			list.addAll(getAllInterfaces(superclass));
		Class<?>[] interfaces = listener.getInterfaces();
		if (interfaces.length != 0) {
			list.addAll(Arrays.asList(interfaces));
			for (Class<?> anInterface : interfaces)
				list.addAll(getAllInterfaces(anInterface));
		}

		//Filter out classes that should be ignored
		list.remove(Listener.class);

		//Filter out interfaces that don't extend Listener
		HashSet<Class<? extends Listener>> goodInterfaces = new HashSet();
		for (Class<?> currentInterfaces : list)
			if (!Arrays.asList(currentInterfaces.getInterfaces()).contains(Listener.class))
				goodInterfaces.add(currentInterfaces.asSubclass(Listener.class));

		return goodInterfaces;
	}
}
