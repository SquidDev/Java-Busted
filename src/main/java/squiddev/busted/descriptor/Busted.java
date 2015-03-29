package squiddev.busted.descriptor;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.Map;

import static squiddev.busted.descriptor.DefaultDescriptors.*;

/**
 * Handles registration of busted globals
 */
public class Busted {
	public final Map<String, IBustedDescriptor> executors = new HashMap<>();
	public final Map<String, LuaValue> globals = new HashMap<>();

	public Busted() {
		register("define", new DefineFunction());
		register("isolate", new DefineFunction(BustedContext.EnvironmentType.Isolate));
		register("expose", new DefineFunction(BustedContext.EnvironmentType.Expose));

		register("it", new ItFunction());
		register("pending", new PendingFunction());

		register("context", "describe");
		register("spec", "it");
		register("test", "it");

		register("before_each");
		register("after_each");

		register("strict_setup");
		register("strict_teardown");
		register("lazy_setup", new LazyDescriptor("lazy_setup"));
		register("lazy_teardown", new LazyDescriptor("lazy_teardown"));

		register("setup", "strict_setup");
		register("teardown", "strict_teardown");
	}

	/**
	 * Bind the globals to a context
	 *
	 * @param context The context to bind to
	 */
	public void bind(BustedContext context) {
		LuaValue environment = context.getEnv();

		for (Map.Entry<String, IBustedDescriptor> executor : executors.entrySet()) {
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
	public void register(String name, IBustedDescriptor executor) {
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
	 * Register a descriptor that
	 *
	 * @param name The name of the descriptor
	 */
	public void register(String name) {
		register(name, new Descriptor(name));
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
		public final IBustedDescriptor executor;

		public Executor(IBustedDescriptor executor, BustedContext context) {
			this.executor = executor;
			this.context = context;
		}

		@Override
		public Varargs invoke(Varargs args) {
			executor.invoke(context, args);
			return LuaValue.NONE;
		}
	}
}
