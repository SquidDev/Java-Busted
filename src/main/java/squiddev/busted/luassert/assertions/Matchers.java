package squiddev.busted.luassert.assertions;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import squiddev.busted.luassert.Util;
import squiddev.busted.luassert.ValueWrapper;

import java.util.Map;

/**
 * Helper functions for matches
 */
public class Matchers {
	@Factory
	public static INegatable<LuaValue> isTruthy() {
		return new IsTruthy();
	}

	@Factory
	public static INegatable<LuaValue> isFalsy() {
		return new IsFalsy();
	}

	@Factory
	public static INegatable<LuaValue> isUnique(boolean deep) {
		return new IsUnique(deep);
	}

	@Factory
	public static INegatable<LuaValue> isUnique() {
		return isUnique(true);
	}

	@Factory
	public static INegatable<LuaValue> isType(String type) {
		return new IsType(type);
	}

	@Factory
	public static INegatable<LuaValue> isTrue() {
		return new IsEqual(LuaValue.TRUE);
	}

	@Factory
	public static INegatable<LuaValue> isFalse() {
		return new IsEqual(LuaValue.FALSE);
	}

	@Factory
	public static INegatable<LuaValue> isEqual(LuaValue expected) {
		return new IsEqual(expected);
	}

	@Factory
	public static INegatable<LuaValue> isSame(LuaValue expected) {
		return new IsSame(expected);
	}

	@Factory
	public static INegatable<LuaValue> hasErrors(String message) {
		return new HasErrors(message);
	}

	@Factory
	public static INegatable<LuaValue> isCallable() {
		return new IsCallable();
	}

	public static class IsTruthy extends Negatable.BasicNegatable<LuaValue> {
		@Override
		public boolean matchesSafely(LuaValue value) {
			return value != LuaValue.NIL && value != LuaValue.FALSE;
		}

		@Override
		public String getPositive() {
			return "truthy";
		}

		@Override
		public String getNegative() {
			return "not truthy";
		}
	}

	public static class IsFalsy extends IsTruthy {
		@Override
		public boolean matchesSafely(LuaValue value) {
			return !super.matchesSafely(value);
		}

		@Override
		public String getPositive() {
			return "falsey";
		}

		@Override
		public String getNegative() {
			return "not falsy";
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
			return "type " + type;
		}

		@Override
		public String getNegative() {
			return "not type " + type;
		}

		@Override
		protected boolean matchesSafely(LuaValue item) {
			return item.typename().equals(type);
		}
	}

	/**
	 * Checks if the items are the same using equality (==)
	 */
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
			description.appendText("not ").appendValue(expected);
		}
	}

	/**
	 * Checks if the items are the same using a deep compare
	 */
	public static class IsSame extends Negatable<LuaValue> {
		private final LuaValue expected;

		public IsSame(LuaValue expected) {
			this.expected = expected;
		}

		@Override
		public void addPositive(Description description) {
			description.appendText("same ").appendValue(expected);
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("not same ").appendValue(expected);
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
		protected boolean matchesSafely(LuaValue message) {
			boolean ok = message == null;

			if (ok || expected == null) {
				return !ok;
			}

			return message.toString().contains(expected);

		}
		@Override
		public void addPositive(Description description) {
			description.appendText("error");
			if(expected != null) description.appendText(" ").appendValue(LuaString.valueOf(expected));
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("no error");
			if(expected != null) description.appendText(" ").appendValue(LuaString.valueOf(expected));
		}
	}

	public static class IsCallable extends Negatable.BasicNegatable<LuaValue> {
		@Override
		public String getPositive() {
			return "callable";
		}

		@Override
		public String getNegative() {
			return "not callable";
		}

		@Override
		protected boolean matchesSafely(LuaValue item) {
			return Util.callable(item);
		}
	}
}
