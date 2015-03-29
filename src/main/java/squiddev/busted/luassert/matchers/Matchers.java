package squiddev.busted.luassert.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import squiddev.busted.luassert.Util;
import squiddev.busted.luassert.ValueWrapper;

import java.util.Map;

/**
 * Helper functions for matches
 */
public class Matchers {
	@Factory
	public static Matcher<LuaValue> truthy() {
		return new IsTruthy();
	}

	@Factory
	public static Matcher<LuaValue> falsy() {
		return new IsFalsy();
	}

	@Factory
	public static Matcher<LuaValue> unique(boolean deep) {
		return new IsUnique(deep);
	}

	@Factory
	public static Matcher<LuaValue> unique() {
		return unique(true);
	}

	@Factory
	public static Matcher<LuaValue> isTrue() {
		return new IsEqual(LuaValue.TRUE);
	}

	@Factory
	public static Matcher<LuaValue> isFalse() {
		return new IsEqual(LuaValue.FALSE);
	}

	@Factory
	public static Matcher<LuaValue> isType(String type) {
		return new IsType(type);
	}

	@Factory
	public static Matcher<LuaValue> hasErrors(String message) {
		return new HasErrors(message);
	}

	@Factory
	public static Matcher<LuaValue> hasErrors() {
		return hasErrors(null);
	}

	public static class IsTruthy extends Negatable.BasicNegatable<LuaValue> {
		@Override
		public boolean matchesSafely(LuaValue value) {
			return value != LuaValue.NIL && value != LuaValue.FALSE;
		}

		@Override
		public String getPositive() {
			return "is not truthy";
		}

		@Override
		public String getNegative() {
			return "is truthy";
		}
	}

	public static class IsFalsy extends IsTruthy {
		@Override
		public boolean matchesSafely(LuaValue value) {
			return !super.matchesSafely(value);
		}

		@Override
		public String getPositive() {
			return "is not falsey";
		}

		@Override
		public String getNegative() {
			return "is falsy";
		}
	}

	public static class IsUnique extends Negatable.BasicNegatable<LuaValue> {
		private final boolean deep;

		public IsUnique(boolean deep) {
			this.deep = deep;
		}

		@Override
		protected boolean matchesSafely(LuaValue items) {
			boolean deep = this.deep;
			ValueWrapper wrapper = new ValueWrapper(items);

			for (Map.Entry<LuaValue, LuaValue> a : wrapper) {
				for (Map.Entry<LuaValue, LuaValue> b : wrapper) {
					if (!a.getKey().equals(b.getKey())) {
						if (deep) {
							if (Util.deepCompare(a.getValue(), b.getValue(), true)) return false;
						} else {
							if (a.getValue().equals(b.getValue())) return false;
						}
					}
				}
			}

			return true;
		}

		@Override
		public String getPositive() {
			return "is not unique";
		}

		@Override
		public String getNegative() {
			return "is unique";
		}
	}

	public static class IsType extends Negatable.BasicNegatable<LuaValue> {
		private final String type;

		public IsType(String type) {
			this.type = type;
		}

		@Override
		public String getPositive() {
			return "is not type" + type;
		}

		@Override
		public String getNegative() {
			return "is type" + type;
		}

		@Override
		protected boolean matchesSafely(LuaValue item) {
			return item.typename().equals(type);
		}
	}

	public static class IsEqual extends Negatable<LuaValue> {
		private final LuaValue expected;

		public IsEqual(LuaValue expected) {
			this.expected = expected;
		}

		@Override
		protected boolean matchesSafely(LuaValue item) {
			return item.eq_b(expected);
		}

		@Override
		public void addPositive(Description description) {
			description.appendValue(expected);
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("not").appendValue(expected);
		}
	}

	public static class IsSame extends Negatable<LuaValue> {
		private final LuaValue expected;

		public IsSame(LuaValue expected) {
			this.expected = expected;
		}

		@Override
		public void addPositive(Description description) {
			description.appendText("same").appendValue(expected);
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("not same").appendValue(expected);
		}

		@Override
		protected boolean matchesSafely(LuaValue item) {
			return Util.deepCompare(expected, item, true);
		}
	}

	public static class HasErrors extends Negatable<LuaValue> {
		private final String expected;

		public HasErrors(String expected) {
			this.expected = expected;
		}

		@Override
		protected boolean matchesSafely(LuaValue item) {
			Varargs pcall = BaseLib.pcall(item, LuaValue.NONE, null);
			boolean ok = pcall.arg(1).toboolean();
			String message = pcall.arg(2).toString();

			if (ok || expected == null) {
				return !ok;
			}

			if (message.contains(message)) {
				return true;
			}

			return false;
		}

		@Override
		public void addPositive(Description description) {
			description.appendText("error");
			description.appendValue(expected);
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("no error");
			description.appendValue(expected);
		}
	}
}
