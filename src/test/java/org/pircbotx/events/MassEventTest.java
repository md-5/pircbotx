package org.pircbotx.events;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.pircbotx.PircBotX;
import org.pircbotx.Utils;
import static org.testng.Assert.*;
import org.pircbotx.hooks.helpers.ListenerAdapterInterface;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Test()
public class MassEventTest {
	List<Class<?>> eventClasses = new ArrayList();

	@BeforeClass
	public void setup() {
		for (Class listenerClass : ListenerAdapterInterface.class.getInterfaces())
			//Add listener parameter method to the list
			eventClasses.add(listenerClass.getDeclaredMethods()[0].getParameterTypes()[0]);
	}

	public void constructorTest() {
		for (Class<?> clazz : eventClasses) {
			//Is there only one constructor?
			Constructor[] constructors = clazz.getDeclaredConstructors();
			assertEquals(constructors.length, 1, Utils.genClass("Event", clazz) + " doesn't have a constructor or has an extra one");

			Constructor constructor = constructors[0];

			//Is the first parameter a bot refrence?
			Class[] constParams = constructor.getParameterTypes();
			assertEquals(constParams[0], PircBotX.class, "First parameter in constructor of " + Utils.genClass("Event", clazz) + " isn't of PircBotX type");

			//Are the number of fields and constructor parameters equal?
			//(subtract one parameter to account for bot)
			assertEquals(constParams.length - 1, clazz.getDeclaredFields().length, "Number of Contructor paramenters in " + Utils.genClass("Event", clazz) + " don't match number of fields");
		}
		System.out.println("Success: Event constructor is good");
	}
}
