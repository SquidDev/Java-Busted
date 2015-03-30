package squiddev.busted.luassert.assertions;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Marks a Matcher that can be negated
 */
public abstract class Negatable<T> extends TypeSafeMatcher<T> implements INegatable<T> {
	/**
	 * Add the description when the test fails
	 */
	public abstract void addPositive(Description description);

	/**
	 * Add the description when the negated test fails
	 */
	public abstract void addNegative(Description description);

	@Override
	public void describeTo(Description description) {
		addPositive(description);
	}

	/**
	 * Negate the current test
	 *
	 * @return The negated test
	 */
	public INegatable<T> negate() {
		return new Negater<>(this);
	}

	/**
	 * Class that can negate a negatable
	 */
	public static class Negater<T> extends BaseMatcher<T> implements INegatable<T> {
		public Negatable<T> matcher;

		public Negater(Negatable<T> matcher) {
			this.matcher = matcher;
		}

		@Override
		public void describeTo(Description description) {
			matcher.addNegative(description);
		}

		@Override
		public boolean matches(Object item) {
			return !matcher.matches(item);
		}

		@Override
		public INegatable<T> negate() {
			return matcher;
		}
	}

	/**
	 * Basic negatable test
	 */
	public static abstract class BasicNegatable<T> extends Negatable<T> {
		@Override
		public void addPositive(Description description) {
			description.appendText(getPositive());
		}

		@Override
		public void addNegative(Description description) {
			description.appendText(getNegative());
		}

		public abstract String getPositive();

		public abstract String getNegative();
	}
}
