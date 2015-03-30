package squiddev.busted;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful super class for {@link ITestItem}
 */
public abstract class TestItemRunner<T extends ITestItem> extends ParentRunner<T> {
	private Throwable error = null;

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

	/**
	 * Returns a list of objects that define the children of this Runner.
	 */
	@Override
	protected List<T> getChildren() {
		try {
			return getInternalChildren();
		} catch(Throwable e) {
			error = e;
			return new ArrayList<>();
		}
	}

	/**
	 * Returns a list of objects that define the children of this Runner.
	 */
	protected abstract List<T> getInternalChildren() throws Exception;

	/**
	 * Constructs a {@code Statement} to run all of the tests in the test class.
	 * Override to add pre-/post-processing. Here is an outline of the
	 * implementation:
	 * <ol>
	 * <li>Determine the children to be run using {@link #getChildren()}
	 * (subject to any imposed filter and sort).</li>
	 * <li>Even if there are no children we will still:
	 * <ol>
	 * <li>Apply all {@code ClassRule}s on the test-class and superclasses.</li>
	 * <li>Run all non-overridden {@code @BeforeClass} methods on the test-class
	 * and superclasses; if any throws an Exception, stop execution and pass the
	 * exception on.</li>
	 * <li>Run all remaining tests on the test-class.</li>
	 * <li>Run all non-overridden {@code @AfterClass} methods on the test-class
	 * and superclasses: exceptions thrown by previous steps are combined, if
	 * necessary, with exceptions from AfterClass methods into a
	 * {@link org.junit.runners.model.MultipleFailureException}.</li>
	 * </ol>
	 * </li>
	 * </ol>
	 *
	 * @return {@code Statement}
	 */
	protected Statement classBlock(final RunNotifier notifier) {
		Statement statement = childrenInvoker(notifier);

		// Don't ignore all children
		statement = withBeforeClasses(statement);
		statement = withAfterClasses(statement);
		statement = withClassRules(statement);
		return statement;
	}

	/**
	 * Returns a {@link Statement}: apply all
	 * static fields assignable to {@link TestRule}
	 * annotated with {@link ClassRule}.
	 *
	 * @param statement the base statement
	 * @return a RunRules statement if any class-level {@link Rule}s are
	 *         found, or the base statement
	 */
	private Statement withClassRules(Statement statement) {
		List<TestRule> classRules = classRules();
		return classRules.isEmpty() ? statement :
			new RunRules(statement, classRules, getDescription());
	}

	@Override
	public void run(final RunNotifier notifier) {
		if(error != null) {
			notifier.fireTestFailure(new Failure(getDescription(), error));
		}

		super.run(notifier);
	}
}
