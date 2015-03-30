package squiddev.busted.descriptor;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import squiddev.busted.blocks.TestGroup;
import squiddev.busted.tests.IgnoredTest;
import squiddev.busted.tests.Test;

/**
 * squiddev.busted.descriptor (Java-Busted
 */
public class DefaultDescriptors {
	/**
	 * An executor that takes a name and a function
	 * (describe & it)
	 */
	public static abstract class NamedExecutor implements IBustedDescriptor {
		@Override
		public void invoke(BustedContext context, Varargs args) {
			invoke(context, args.arg(1).checkjstring(), args.arg(2).checkfunction());
		}

		public abstract void invoke(BustedContext context, String name, LuaValue closure);
	}

	/**
	 * An executor that just takes a function
	 * (before_each, after_each, setup, teardown)
	 */
	public static abstract class AnonExecutor implements IBustedDescriptor {
		@Override
		public void invoke(BustedContext context, Varargs args) {
			invoke(context, args.arg(1).checkfunction());
		}

		public abstract void invoke(BustedContext context, LuaValue closure);
	}

	public static class Descriptor extends AnonExecutor {
		public final String name;
		public final BustedContext.EnvironmentType environment;

		public Descriptor(String name, BustedContext.EnvironmentType environment) {
			this.name = name;
			this.environment = environment;
		}

		public Descriptor(String name) {
			this(name, BustedContext.EnvironmentType.Unwrap);
		}

		@Override
		public void invoke(BustedContext parent, final LuaValue closure) {
			BustedContext context = new BustedContext(parent, environment);

			context.rejectAll();
			context.setup();
			closure.setfenv(context.getEnv());

			parent.descriptors.put(name, getRunnable(closure));
		}

		protected IBustedExecutor getRunnable(final LuaValue closure) {
			return new IBustedExecutor() {
				@Override
				public void invoke(BustedContext context) {
					closure.invoke();
				}
			};
		}
	}

	public static class LazyDescriptor extends Descriptor {
		public LazyDescriptor(String name) {
			super(name);
		}

		@Override
		protected IBustedExecutor getRunnable(final LuaValue closure) {
			return new IBustedExecutor() {
				private boolean called = false;

				@Override
				public void invoke(BustedContext context) {
					if (context.tests.size() > 0 && !called) {
						closure.invoke();
						called = true;
					}
				}
			};
		}
	}

	public static class DescribeFunction extends NamedExecutor {
		public final BustedContext.EnvironmentType environment;

		public DescribeFunction(BustedContext.EnvironmentType type) {
			environment = type;
		}

		public DescribeFunction() {
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

	public static class ItFunction extends NamedExecutor {
		@Override
		public void invoke(BustedContext parent, String name, LuaValue closure) {
			BustedContext context = new BustedContext(parent, BustedContext.EnvironmentType.Wrap);
			context.rejectAll();

			new Test(name, closure, context);
		}
	}

	public static class PendingFunction implements IBustedDescriptor {
		@Override
		public void invoke(BustedContext parent, Varargs args) {
			new IgnoredTest(args.arg1().checkjstring(), parent);
		}
	}
}
