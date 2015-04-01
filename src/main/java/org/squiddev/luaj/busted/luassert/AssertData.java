package org.squiddev.luaj.busted.luassert;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.squiddev.luaj.busted.Registry;
import org.squiddev.luaj.busted.luassert.assertions.IAssertion;
import org.squiddev.luaj.busted.luassert.assertions.INegatable;
import org.squiddev.luaj.busted.luassert.assertions.Matchers;
import org.squiddev.luaj.busted.luassert.modifiers.IModifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.squiddev.luaj.busted.luassert.assertions.IAssertion.*;

public class AssertData {
	public final Registry<IModifier> modifiers = new Registry<>();
	public final Registry<IAssertion> assertions = new Registry<>();

	public AssertData() {
		modifiers.register(new String[]{"is", "are", "was", "has"}, IModifier.EMPTY);

		modifiers.register(new String[]{"no", "not"}, new IModifier() {
			@Override
			public Matcher<LuaValue> modify(Matcher<LuaValue> matcher) {
				if (matcher instanceof INegatable) return ((INegatable<LuaValue>) matcher).negate();
				throw new UnsupportedOperationException("Matcher cannot be negated");
			}
		});

		assertions.register("true", new BasicAssertion(Matchers.isTrue()));
		assertions.register("false", new BasicAssertion(Matchers.isFalse()));

		for (final String name : new String[]{"boolean", "number", "string", "table", "nil", "userdata", "function", "thread"}) {
			assertions.register(name, new BaseAssertion() {
				@Override
				public Matcher<LuaValue> getMatcher(Varargs args) {
					return Matchers.isType(name);
				}
			});
		}

		assertions.register(new String[]{"equal", "equals"}, new ListAssertion() {
			@Override
			public Matcher<LuaValue> getMatcher(LuaValue arg) {
				return Matchers.isEqual(arg);
			}
		});

		assertions.register("same", new ListAssertion() {
			@Override
			public Matcher<LuaValue> getMatcher(LuaValue arg) {
				return Matchers.isSame(arg);
			}
		});

		assertions.register("unique", new BaseAssertion() {
			@Override
			public Matcher<LuaValue> getMatcher(Varargs args) {
				return Matchers.isUnique(args.toboolean(1));
			}
		});

		assertions.register(new String[]{"error", "errors"}, new IAssertion() {
			@Override
			public void match(Varargs args, Varargs payload, IModifier modifier) {
				LuaValue arg = args.arg1();
				assertThat(arg, Matchers.isCallable());

				LuaValue message = null;

				try {
					arg.invoke();
				} catch (LuaError le) {
					String m = le.getMessage();
					message = m != null ? LuaValue.valueOf(m) : LuaValue.NIL;
				} catch (Throwable e) {
					String m = e.getMessage();
					message = LuaValue.valueOf(m != null ? m : e.toString());
				}

				assertThat(message, modifier.modify(Matchers.hasErrors(args.optjstring(2, null))));
			}
		});

		assertions.register("truthy", new BasicAssertion(Matchers.isTruthy()));
		assertions.register("falsy", new BasicAssertion(Matchers.isFalsy()));
	}
}
