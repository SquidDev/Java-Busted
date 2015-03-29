package squiddev.busted.luassert.matchers;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * A basic matcher that takes no arguments
 */
public class BasicAssertion implements IAssertion {
	private final Matcher<LuaValue> matcher;

	public BasicAssertion(Matcher<LuaValue> matcher) {
		this.matcher = matcher;
	}

	@Override
	public Matcher<LuaValue> match(Varargs args) {
		return matcher;
	}
}
