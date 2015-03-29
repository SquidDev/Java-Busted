package squiddev.busted.tests;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.luaj.vm2.LuaValue;
import squiddev.busted.ITestItem;
import squiddev.busted.descriptor.BustedContext;

/**
 * A single busted test
 */
public class Test implements ITestItem {
	private final LuaValue closure;
	private final Description description;
	public final String name;
	public final BustedContext context;

	/**
	 * Create a new busted test
	 *
	 * @param name    The name of the test
	 * @param closure The {@link org.luaj.vm2.LuaFunction} to run
	 * @param context The current context
	 */
	public Test(String name, LuaValue closure, BustedContext context) {
		this.name = name;
		this.closure = closure;
		this.description = Description.createTestDescription(context.runner.getTestClass().getJavaClass(), name);
		this.context = context;

		context.setup();
		closure.setfenv(context.getEnv());

		context.parent.tests.add(this);
	}

	/**
	 * Get the description for this item.
	 * <p/>
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
			context.execute("before_each");
			closure.invoke();
			context.executeReverse("after_each");
		} catch (AssumptionViolatedException e) {
			eachNotifier.addFailedAssumption(e);
		} catch (Throwable e) {
			eachNotifier.addFailure(e);
		} finally {
			eachNotifier.fireTestFinished();
		}
	}
}
