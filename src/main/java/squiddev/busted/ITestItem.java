package squiddev.busted;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

/**
 * A Busted test item.
 * This could be a suite (describe) or a test (it)
 */
public interface ITestItem {
	/**
	 * Get the description for this item.
	 * <p>
	 * This can include tests or child {@link ITestItem}s
	 *
	 * @return The description
	 */
	Description getDescription();


	/**
	 * Run the {@link ITestItem}
	 *
	 * @param notifier The test notifier
	 */
	void run(RunNotifier notifier);
}
