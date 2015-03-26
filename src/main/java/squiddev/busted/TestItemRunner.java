package squiddev.busted;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

/**
 * Useful super class for {@link ITestItem}
 */
public abstract class TestItemRunner<T extends ITestItem> extends ParentRunner<T> {
	/**
	 * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
	 *
	 * @param testClass The test class to use
	 */
	protected TestItemRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	/**
	 * Returns a {@link Description} for {@code child}, which can be assumed to
	 * be an element of the list returned by {@link ParentRunner#getChildren()}
	 *
	 * @param child The child test to describe
	 */
	@Override
	protected Description describeChild(T child) {
		return child.getDescription();
	}

	/**
	 * Runs the test corresponding to {@code child}, which can be assumed to be
	 * an element of the list returned by {@link ParentRunner#getChildren()}.
	 * Subclasses are responsible for making sure that relevant test events are
	 * reported through {@code notifier}
	 *
	 * @param child    The child test to run
	 * @param notifier The run notifier for this test
	 */
	@Override
	protected void runChild(T child, RunNotifier notifier) {
		child.run(notifier);
	}
}
