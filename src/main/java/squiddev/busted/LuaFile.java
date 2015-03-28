package squiddev.busted;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;

import java.util.List;

/**
 * A file in the busted suite
 */
public class LuaFile extends TestItemRunner<ITestItem> implements ITestItem {
	public final String path;
	public final BustedRunner runner;

	public LuaFile(String path, BustedRunner runner) throws InitializationError {
		// TODO: Cache the TestClass instance somehow
		super(runner.getTestClass().getJavaClass());

		this.path = path;
		this.runner = runner;
	}

	/**
	 * Returns a list of objects that define the children of this Runner.
	 */
	@Override
	protected List<ITestItem> getChildren() {
		Globals globals = new Globals(runner);

		try {
			runner.runFile.invoke(null, runner.bustedRoot + path, globals);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return globals.tests;

	}

	/**
	 * Returns a name used to describe this Runner
	 */
	@Override
	protected String getName() {
		// We need to replace the "." as they are read as name separators
		return path.replace(".lua", "").replace(".", "-");
	}

	public static class Globals extends BustedContext {
		public LuaValue environment;

		public Globals(BustedRunner runner) {
			super(runner);
		}

		/**
		 * Get the environment for this context
		 *
		 * @return The environment for this context
		 */
		@Override
		public LuaValue getEnv() {
			if(environment == null) throw new IllegalStateException("Must set environment before getting it");
			return environment;
		}

		/**
		 * Set the environment for this context
		 * @param environment The environment to use
		 */
		public void setEnv(LuaValue environment) {
			if(this.environment != null) throw new IllegalStateException("Cannot set environment again");
			this.environment = environment;

			runner.busted.bind(this);
		}
	}
}
