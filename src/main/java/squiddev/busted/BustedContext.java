package squiddev.busted;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects variables into a scope
 */
public class BustedContext {
	public enum EnvironmentType {
		/**
		 * Use parent environment
		 */
		Parent,

		/**
		 * Clone the global table
		 */
		Isolate,

		/**
		 * Store variables in the table above
		 */
		Unwrap,

		/**
		 * Use the parent environment via metatables
		 */
		Wrap,

		/**
		 * Store variables in the table 2 above
		 */
		Expose,
	}

	public final BustedRunner runner;
	public final BustedContext parent;
	public final EnvironmentType type;
	public final int depth;

	protected final List<ITestItem> tests = new ArrayList<>();

	private LuaValue env;

	public BustedContext(BustedContext parent, EnvironmentType type) {
		this.runner = parent.runner;
		this.parent = parent;
		this.type = type;
		depth = parent.depth + 1;
	}

	public BustedContext(BustedRunner runner) {
		this.runner = runner;
		this.parent = this;
		this.type = EnvironmentType.Parent;
		depth = 0;
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
		}

		final BustedContext p = parent;

		LuaTable metaEnv = new LuaTable();
		metaEnv.set("__newindex", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
				(p.getEnv()).set(key, value);
				return LuaValue.NIL;
			}
		});
		metaEnv.set("__index", this.parent.getEnv());

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
				case Wrap: {
					LuaTable env = new LuaTable();
					LuaTable metaEnv = new LuaTable();
					metaEnv.set("__index", parent.getEnv());
					env.setmetatable(metaEnv);
					return this.env = env;
				}
				case Isolate:
					// TODO: Clone global table
					return parent.getEnv().get("_G");
				case Unwrap:
					return env = unwrap(1);
				case Expose:
					return env = unwrap(2);
				case Parent:
					if (parent == this) throw new IllegalStateException("This item has no parent");
					return env = parent.env;
			}
		}

		return env;
	}

	/**
	 * Bind the busted environment to this
	 */
	public void setup() {
		runner.busted.bind(this);
	}

	/**
	 * Disable a function in this environment
	 *
	 * @param descriptor The name of the function to use
	 */
	public void reject(final String descriptor) {
		LuaValue env = getEnv();
		if (!env.get(descriptor).isnil()) {
			env.set("descriptor", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					throw new LuaError("'" + descriptor + "' not supported inside current block");
				}
			});
		}
	}

	/**
	 * Disable all busted functions
	 */
	public void rejectAll() {
		for (String descriptor : runner.busted.executors.keySet()) {
			reject(descriptor);
		}
	}
}
