package org.squiddev.luaj.busted.luassert.modifiers;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;

/**
 * A value that modifies a matcher
 */
public interface IModifier {
	IModifier EMPTY = new IModifier() {
		@Override
		public Matcher<LuaValue> modify(Matcher<LuaValue> matcher) {
			return matcher;
		}
	};

	/**
	 * Modify the current assertion
	 *
	 * @param matcher The assertion to modify
	 * @return The new assertion
	 */
	Matcher<LuaValue> modify(Matcher<LuaValue> matcher);
}
