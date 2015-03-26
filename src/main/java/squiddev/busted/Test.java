package squiddev.busted;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.luaj.vm2.LuaValue;

/**
 * A single busted test
 */
public class Test implements ITestItem {
	private final LuaValue closure;
	private final Description description;

	/**
	 * Create a new busted test
	 *
	 * @param name    The name of the test
	 * @param closure The {@link org.luaj.vm2.LuaFunction} to run
	 * @param runner  The {@link BustedRunner} that owns this test
	 */
	public Test(String name, LuaValue closure, BustedRunner runner) {
		this.closure = closure;
		this.description = Description.createTestDescription(runner.getTestClass().getJavaClass(), name);
	}

	/**
	 * Get the description for this item.
	 * <p>
	 * This can include tests or child {@link ITestItem}s
	 *
	 * @return The description
	 */
	@Override
	public Description getDescription() {
		return description;
	}

	/**
	 * Run the {@link ITestItem}
	 *
	 * @param notifier The test notifier
	 */
	@Override
	public void run(RunNotifier notifier) {
		EachTestNotifier eachNotifier = new EachTestNotifier(notifier, getDescription());
		eachNotifier.fireTestStarted();
		try {
			closure.invoke();
		} catch (AssumptionViolatedException e) {
			eachNotifier.addFailedAssumption(e);
		} catch (Throwable e) {
			eachNotifier.addFailure(e);
		} finally {
			eachNotifier.fireTestFinished();
		}
	}
}
