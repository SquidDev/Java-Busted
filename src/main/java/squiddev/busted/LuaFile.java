package squiddev.busted;

import org.junit.runners.model.InitializationError;

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
		BustedContext globals = new BustedContext(runner);

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
}
