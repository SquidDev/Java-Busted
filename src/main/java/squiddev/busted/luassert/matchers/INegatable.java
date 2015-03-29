package squiddev.busted.luassert.matchers;

import org.hamcrest.Matcher;

/**
 * A test case that can be reversed
 */
public interface INegatable<T> extends Matcher<T> {
	/**
	 * Negate the current test
	 *
	 * @return The negated assertion
	 */
	INegatable<T> negate();
}
