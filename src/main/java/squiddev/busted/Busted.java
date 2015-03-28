package squiddev.busted;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles registration of busted globals
 */
public class Busted {
	public final Map<String, IBustedExecutor> executors = new HashMap<>();
	public final Map<String, LuaValue> globals = new HashMap<>();

	public Busted() {
		register("define", new BustedDefineFunction());
		register("isolate", new BustedDefineFunction(BustedContext.EnvironmentType.Isolate));
		register("expose", new BustedDefineFunction(BustedContext.EnvironmentType.Expose));

		register("it", new BustedItFunction());
		register("pending", new BustedPendingFunction());

		register("context", "describe");
		register("spec", "it");
		register("test", "it");
	}

	/**
	 * Bind the globals to a context
	 *
	 * @param context The context to bind to
	 */
	public void bind(BustedContext context) {
		LuaValue environment = context.getEnv();

		for (Map.Entry<String, IBustedExecutor> executor : executors.entrySet()) {
			environment.rawset(executor.getKey(), new Executor(executor.getValue(), context));
		}

		for (Map.Entry<String, LuaValue> global : globals.entrySet()) {
			environment.rawset(global.getKey(), global.getValue());
		}
	}

	/**
	 * Register a function
	 *
	 * @param name     The name of the function
	 * @param executor The executor to run on this function being called
	 */
	public void register(String name, IBustedExecutor executor) {
		if (executors.containsKey(name)) throw new IllegalArgumentException("Cannot override " + name);
		executors.put(name, executor);
	}

	/**
	 * Create an alias of an existing function
	 *
	 * @param name     The alias of the function
	 * @param original The original name of the function
	 */
	public void register(String name, String original) {
		executors.put(name, executors.get(original));
	}

	/**
	 * Export a value to the globals
	 *
	 * @param name  The name of the object to export
	 * @param value The LuaValue to use
	 */
	public void export(String name, LuaValue value) {
		if (globals.containsKey(name)) throw new IllegalArgumentException("Cannot override " + name);
		globals.put(name, value);
	}

	/**
	 * Wraps a function in an executor
	 */
	private static class Executor extends VarArgFunction {
		public final BustedContext context;
		public final IBustedExecutor executor;

		public Executor(IBustedExecutor executor, BustedContext context) {
			this.executor = executor;
			this.context = context;
		}

		@Override
		public Varargs invoke(Varargs args) {
			executor.invoke(context, args);
			return LuaValue.NONE;
		}
	}

	public interface IBustedExecutor {
		void invoke(BustedContext context, Varargs args);
	}

	public static abstract class BustedExecutor implements IBustedExecutor {
		@Override
		public void invoke(BustedContext context, Varargs args) {
			invoke(context, args.arg(1).checkjstring(), args.arg(2).checkfunction());
		}

		public abstract void invoke(BustedContext context, String name, LuaValue closure);
	}

	private static class BustedDefineFunction extends BustedExecutor {
		public final BustedContext.EnvironmentType environment;

		public BustedDefineFunction(BustedContext.EnvironmentType type) {
			environment = type;
		}

		public BustedDefineFunction() {
			this(BustedContext.EnvironmentType.Unwrap);
		}

		@Override
		public void invoke(BustedContext parent, String name, LuaValue closure) {
			try {
				new TestGroup(name, closure, new BustedContext(parent, environment));
			} catch (InitializationError e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class BustedItFunction extends BustedExecutor {
		@Override
		public void invoke(BustedContext parent, String name, LuaValue closure) {
			BustedContext context = new BustedContext(parent, BustedContext.EnvironmentType.Wrap);
			context.rejectAll();

			new Test(name, closure, context);
		}
	}

	private static class BustedPendingFunction implements IBustedExecutor {
		@Override
		public void invoke(BustedContext parent, Varargs args) {
			new IgnoredTest(args.arg1().checkjstring(), parent);
		}
	}
}
