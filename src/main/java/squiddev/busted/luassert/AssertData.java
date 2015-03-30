package squiddev.busted.luassert;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.busted.Registry;
import squiddev.busted.luassert.assertions.IAssertion;
import squiddev.busted.luassert.assertions.INegatable;
import squiddev.busted.luassert.modifiers.IModifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static squiddev.busted.luassert.assertions.IAssertion.*;
import static squiddev.busted.luassert.assertions.Matchers.*;

public class AssertData {
	public final Registry<IModifier> modifiers = new Registry<>();
	public final Registry<IAssertion> assertions = new Registry<>();

	public AssertData() {
		modifiers.register(new String[]{"is", "are", "was", "has"}, new IModifier() {
			@Override
			public Matcher<LuaValue> modify(Matcher<LuaValue> matcher) {
				return matcher;
			}
		});

		modifiers.register(new String[]{"no", "not"}, new IModifier() {
			@Override
			public Matcher<LuaValue> modify(Matcher<LuaValue> matcher) {
				if (matcher instanceof INegatable) return ((INegatable<LuaValue>) matcher).negate();
				throw new UnsupportedOperationException("Matcher cannot be negated");
			}
		});

		assertions.register("true", new BasicAssertion(isTrue()));
		assertions.register("false", new BasicAssertion(isFalse()));

		for (final String name : new String[]{"boolean", "number", "string", "table", "nil", "userdata", "function", "thread"}) {
			assertions.register(name, new BaseAssertion() {
				@Override
				public Matcher<LuaValue> getMatcher(Varargs args) {
					return isType(name);
				}
			});
		}

		assertions.register(new String[]{"equal", "equals"}, new ListAssertion() {
			@Override
			public Matcher<LuaValue> getMatcher(LuaValue arg) {
				return isEqual(arg);
			}
		});

		assertions.register("same", new ListAssertion() {
			@Override
			public Matcher<LuaValue> getMatcher(LuaValue arg) {
				return isSame(arg);
			}
		});

		assertions.register("unique", new BaseAssertion() {
			@Override
			public Matcher<LuaValue> getMatcher(Varargs args) {
				return isUnique(args.toboolean(1));
			}
		});

		assertions.register(new String[]{"error", "errors"}, new IAssertion() {
			@Override
			public void match(Varargs args, IModifier modifier) {
				LuaValue arg = args.arg1();
				assertThat(arg, isCallable());
				assertThat(arg, modifier.modify(hasErrors(args.optjstring(2, null))));
			}
		});

		assertions.register("truthy", new BasicAssertion(isTruthy()));
		assertions.register("falsy", new BasicAssertion(isFalsy()));
	}
}
