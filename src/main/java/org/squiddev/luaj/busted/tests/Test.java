package org.squiddev.luaj.busted.tests;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.squiddev.luaj.busted.ITestItem;
import org.squiddev.luaj.busted.descriptor.BustedContext;

import java.util.UUID;

/**
 * A single busted test
 */
public class Test implements ITestItem {
	private final LuaValue closure;
	private final Description description;
	public final String name;
	public final BustedContext context;

	private LuaValue atEnd;

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
		this.description = Description.createTestDescription(context.runner.getTestClass().getName(), name, UUID.randomUUID());
		this.context = context;

		context.getEnv().rawset("finally", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				atEnd = arg;
				return LuaValue.NONE;
			}
		});

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

		// Reset finally section
		atEnd = null;

		try {
			// TODO: Put this somewhere more sensible
			context.execute("lazy_setup", true);

			context.execute("before_each", true);

			closure.setfenv(context.getEnv());
			closure.invoke();

			context.executeReverse("after_each", true);
		} catch (AssumptionViolatedException e) {
			eachNotifier.addFailedAssumption(e);
		} catch (Throwable e) {
			eachNotifier.addFailure(e);
		} finally {
			if (atEnd != null) {
				// Call finally if we have one
				try {
					atEnd.invoke();
				} catch (AssumptionViolatedException e) {
					eachNotifier.addFailedAssumption(e);
				} catch (Throwable e) {
					eachNotifier.addFailure(e);
				}
			}

			eachNotifier.fireTestFinished();
		}
	}
}
