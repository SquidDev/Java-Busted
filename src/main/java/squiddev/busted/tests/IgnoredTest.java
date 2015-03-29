package squiddev.busted.tests;

import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import squiddev.busted.ITestItem;
import squiddev.busted.descriptor.BustedContext;

public class IgnoredTest implements ITestItem {
	public final Description description;
	private final String name;
	public final BustedContext context;

	/**
	 * Create a new busted test
	 *
	 * @param name    The name of the test
	 * @param context The current context
	 */
	public IgnoredTest(String name, BustedContext context) {
		this.name = name;
		this.description = Description.createTestDescription(context.runner.getTestClass().getJavaClass(), name);
		this.context = context;

		context.tests.add(this);
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
		eachNotifier.fireTestIgnored();
	}
}
