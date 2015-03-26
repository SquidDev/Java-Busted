package squiddev.busted;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects variables into a scope
 */
public class BustedVariables {
	private final BustedRunner runner;
	protected final List<ITestItem> tests = new ArrayList<>();

	public BustedVariables(BustedRunner runner) {
		this.runner = runner;
	}

	public void bind(LuaValue value) {
		value.set("define", new BustedDefineFunction());
		value.set("it", new BustedItFunction());
	}

	private class BustedDefineFunction extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue name, LuaValue closure) {
			try {
				tests.add(new TestGroup(name.checkjstring(), closure.checkfunction(), runner));
			} catch (InitializationError e) {
				throw new RuntimeException(e);
			}
			return LuaValue.NONE;
		}
	}

	private class BustedItFunction extends TwoArgFunction {

		@Override
		public LuaValue call(LuaValue name, LuaValue closure) {
			tests.add(new Test(name.checkjstring(), closure.checkfunction(), runner));
			return LuaValue.NONE;
		}
	}
}
