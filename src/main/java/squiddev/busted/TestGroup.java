package squiddev.busted;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.List;

/**
 * Create a test suite
 */
public class TestGroup extends TestItemRunner<ITestItem> implements ITestItem {
	private final String name;
	private final LuaValue closure;
	private final BustedRunner runner;

	/**
	 * @param name   The name of the test
	 * @param runner The owning {@link BustedRunner}
	 * @throws InitializationError
	 */
	protected TestGroup(String name, LuaValue closure, BustedRunner runner) throws InitializationError {
		// TODO: Cache the TestClass instance somehow
		super(runner.getTestClass().getJavaClass());

		this.name = name;
		this.closure = closure;
		this.runner = runner;
	}

	/**
	 * Returns a list of objects that define the children of this Runner.
	 * <p>
	 * This creates a new environment which references the parent one
	 * to enable us to define child tests
	 */
	@Override
	protected List<ITestItem> getChildren() {
		LuaValue env = closure.getfenv();

		LuaTable envMeta = new LuaTable(0, 1);
		envMeta.set("__index", env);

		LuaTable newEnv = new LuaTable();
		newEnv.setmetatable(envMeta);

		BustedContext bustedVars = new BustedContext(runner);
		bustedVars.bindEnvironment(newEnv);

		closure.setfenv(newEnv);
		closure.invoke();

		return bustedVars.tests;
	}

	/**
	 * Returns a name used to describe this Runner
	 */
	@Override
	protected String getName() {
		return name;
	}
}
