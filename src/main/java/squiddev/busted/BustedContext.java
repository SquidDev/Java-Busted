package squiddev.busted;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects variables into a scope
 */
public class BustedContext {
	public enum EnvironmentType {
		None,
		Isolate,
		Unwrap,
		Expose,
	}

	public final BustedRunner runner;
	public final BustedContext parent;
	public final EnvironmentType type;

	protected final List<ITestItem> tests = new ArrayList<>();

	private LuaValue env = new LuaTable();

	public BustedContext(BustedRunner runner, BustedContext parent, EnvironmentType type) {
		this.runner = runner;
		this.parent = parent;
		this.type = type;
	}

	public BustedContext(BustedRunner runner) {
		this(runner, null, EnvironmentType.None);
	}

	public void bindEnvironment(LuaValue value) {
		value.set("define", new BustedDefineFunction());
		value.set("it", new BustedItFunction());
	}

	/**
	 * Create a new environment that references an environment 'n' levels above
	 *
	 * @param levels Number of levels to go up
	 * @return The created environment
	 */
	private LuaTable unwrap(int levels) {
		BustedContext parent = this;
		for (int i = 0; i < levels; i++) {
			parent = parent.parent;
			if (parent == null) break;
		}

		final BustedContext p = parent;

		LuaTable metaEnv = new LuaTable();
		metaEnv.set("__newindex", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
				if (p == null) {
					self.get("_G").set(key, value);
				} else {
					p.getEnv().set(key, value);
				}

				return LuaValue.NIL;
			}
		});

		LuaTable env = new LuaTable();
		env.setmetatable(metaEnv);
		return env;
	}

	/**
	 * Get the environment for this context
	 *
	 * @return The environment for this context
	 */
	public LuaValue getEnv() {
		if (env == null) {
			switch (type) {
				case None:
					return env = parent.env;
				case Isolate:
					return new LuaTable();
				case Unwrap:
					return env = unwrap(1);
				case Expose:
					return env = unwrap(2);
			}
		}

		return env;
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
