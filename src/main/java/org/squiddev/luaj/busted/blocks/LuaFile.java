package org.squiddev.luaj.busted.blocks;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;
import org.squiddev.luaj.busted.BustedRunner;
import org.squiddev.luaj.busted.ITestItem;
import org.squiddev.luaj.busted.descriptor.BustedContext;

import java.util.List;

/**
 * A file in the busted suite
 */
public class LuaFile extends Block implements ITestItem {
	public final String path;
	public final BustedRunner runner;

	public LuaFile(String path, BustedRunner runner) throws InitializationError {
		// TODO: Cache the TestClass instance somehow
		super(new Globals(runner));

		this.path = path;
		this.runner = runner;
	}

	/**
	 * Returns a list of objects that define the children of this Runner.
	 */
	@Override
	protected List<ITestItem> getInternalChildren() throws Exception {
		runner.runFile.invoke(null, runner.bustedRoot + path, context);
		return context.tests;
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

		/**
		 * Execute the parent executor then this executor
		 *
		 * @param descriptor The name of the item to run
		 * @param propagate Call the parent context
		 */
		@Override
		public void execute(String descriptor, boolean propagate) {
			execute(descriptor);
		}

		/**
		 * Execute this executor then the parent
		 *
		 * @param descriptor The name of the item to run
		 * @param propagate Call the parent context
		 */
		@Override
		public void executeReverse(String descriptor, boolean propagate) {
			execute(descriptor);
		}
	}
}
