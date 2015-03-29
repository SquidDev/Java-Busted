package squiddev.busted.luassert.modifiers;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;

/**
 * A value that modifies a matcher
 */
public interface IModifier {
	/**
	 * Modify the current assertion
	 *
	 * @param matcher The assertion to modify
	 * @return The new assertion
	 */
	Matcher<LuaValue> modify(Matcher<LuaValue> matcher);
}
