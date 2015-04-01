package org.squiddev.luaj.busted.blocks;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;
import org.squiddev.luaj.busted.ITestItem;
import org.squiddev.luaj.busted.descriptor.BustedContext;

import java.util.List;

/**
 * Create a test suite
 */
public class TestGroup extends Block implements ITestItem {
	private final String name;
	private final LuaValue closure;

	/**
	 * Create a new TestGroup
	 *
	 * @param name    The name of the group
	 * @param closure The closure that provides its children
	 * @param context The current context
	 * @throws InitializationError
	 */
	public TestGroup(String name, LuaValue closure, BustedContext context) throws InitializationError {
		// TODO: Cache the TestClass instance somehow
		super(context);

		this.name = name;
		this.closure = closure;

		context.setup();

		context.parent.tests.add(this);
	}

	/**
	 * Returns a list of objects that define the children of this Runner.
	 * <p/>
	 * This creates a new environment which references the parent one
	 * to enable us to define child tests
	 */
	@Override
	protected List<ITestItem> getInternalChildren() {
		closure.setfenv(context.getEnv());
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
