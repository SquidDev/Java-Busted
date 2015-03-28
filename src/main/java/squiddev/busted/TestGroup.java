package squiddev.busted;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;

import java.util.List;

/**
 * Create a test suite
 */
public class TestGroup extends TestItemRunner<ITestItem> implements ITestItem {
	private final String name;
	private final LuaValue closure;
	private final BustedContext context;

	/**
	 * Create a new TestGroup
	 *
	 * @param name    The name of the group
	 * @param closure The closure that provides its children
	 * @param context The current context
	 * @throws InitializationError
	 */
	protected TestGroup(String name, LuaValue closure, BustedContext context) throws InitializationError {
		// TODO: Cache the TestClass instance somehow
		super(context.runner.getTestClass().getJavaClass());

		this.name = name;
		this.closure = closure;
		this.context = context;

		context.setup();
		closure.setfenv(context.getEnv());

		context.parent.tests.add(this);
	}

	/**
	 * Returns a list of objects that define the children of this Runner.
	 * <p/>
	 * This creates a new environment which references the parent one
	 * to enable us to define child tests
	 */
	@Override
	protected List<ITestItem> getChildren() {
		closure.invoke();
		return context.tests;
	}

	/**
	 * Returns a name used to describe this Runner
	 */
	@Override
	protected String getName() {
		return name;
	}
}
