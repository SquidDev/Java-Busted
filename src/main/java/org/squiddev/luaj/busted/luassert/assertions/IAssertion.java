package org.squiddev.luaj.busted.luassert.assertions;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.squiddev.luaj.busted.luassert.modifiers.IModifier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Assert an item is true
 */
public interface IAssertion {
	void match(Varargs args, Varargs payload, IModifier modifier);

	/**
	 * The first argument is the expected one, the following ones are valid arguments
	 */
	abstract class BaseAssertion implements IAssertion {
		@Override
		public void match(Varargs args, Varargs payload, IModifier modifier) {
			assertThat(args.arg1(), modifier.modify(getMatcher(args.subargs(2))));
		}

		public abstract Matcher<LuaValue> getMatcher(Varargs args);
	}


	/**
	 * A basic matcher that takes no arguments
	 */
	class BasicAssertion extends BaseAssertion {
		private final Matcher<LuaValue> matcher;

		public BasicAssertion(Matcher<LuaValue> matcher) {
			this.matcher = matcher;
		}

		@Override
		public Matcher<LuaValue> getMatcher(Varargs args) {
			return matcher;
		}
	}

	/**
	 * Validates every item in the list
	 */
	abstract class ListAssertion implements IAssertion {
		@Override
		public void match(Varargs args, Varargs payload, IModifier modifier) {
			if(args.narg() < 2) {
				assertThat("Must specify at least 2 arguments", args.narg(), is(2));
			}
			Matcher<LuaValue> arg1 = modifier.modify(getMatcher(args.arg1()));
			for (int i = 2, n = args.narg(); i <= n; i++) {
				assertThat(args.arg(i), arg1);
			}
		}

		public abstract Matcher<LuaValue> getMatcher(LuaValue actual);
	}
}
