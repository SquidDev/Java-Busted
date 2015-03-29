package squiddev.busted.luassert.matchers;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Assert an item is true
 */
public interface IAssertion {
	Matcher<LuaValue> match(Varargs args);
}
