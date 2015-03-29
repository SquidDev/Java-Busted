package squiddev.busted.luassert;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.busted.Registry;
import squiddev.busted.luassert.matchers.BasicAssertion;
import squiddev.busted.luassert.matchers.IAssertion;
import squiddev.busted.luassert.matchers.INegatable;
import squiddev.busted.luassert.matchers.Matchers;
import squiddev.busted.luassert.modifiers.IModifier;

public class AssertLib {
	public final Registry<IModifier> modifiers = new Registry<>();
	public final Registry<IAssertion> assertions = new Registry<>();

	public AssertLib() {
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

		assertions.register("false", new BasicAssertion(Matchers.isTrue()));
		assertions.register("false", new BasicAssertion(Matchers.isFalse()));

		for (final String name : new String[]{"boolean", "number", "string", "table", "nil", "userdata", "function", "thread"}) {
			assertions.register(name, new IAssertion() {
				@Override
				public Matcher<LuaValue> match(Varargs args) {
					return Matchers.isType(name);
				}
			});
		}

		assertions.register("same", new IAssertion() {
			@Override
			public Matcher<LuaValue> match(Varargs args) {
				return new Matchers.IsSame(args.arg1());
			}
		});

		assertions.register("unique", new IAssertion() {
			@Override
			public Matcher<LuaValue> match(Varargs args) {
				return new Matchers.IsUnique(args.toboolean(1));
			}
		});

		assertions.register(new String[]{"equal", "equals"}, new IAssertion() {
			@Override
			public Matcher<LuaValue> match(Varargs args) {
				return new Matchers.IsEqual(args.arg1());
			}
		});

		assertions.register(new String[]{"error", "errors"}, new IAssertion() {
			@Override
			public Matcher<LuaValue> match(Varargs args) {
				return new Matchers.HasErrors(args.tojstring(1));
			}
		});

		assertions.register("truthy", new BasicAssertion(Matchers.truthy()));
		assertions.register("falsy", new BasicAssertion(Matchers.falsy()));
	}
}
