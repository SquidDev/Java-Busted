package org.squiddev.luaj.busted.descriptor;

import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.squiddev.luaj.busted.blocks.TestGroup;
import org.squiddev.luaj.busted.tests.IgnoredTest;
import org.squiddev.luaj.busted.tests.Test;

/**
 * All the default descriptors
 */
public class DefaultDescriptors {
	/**
	 * An executor that takes a name and a function
	 * (describe & it)
	 */
	public static abstract class NamedExecutor implements IBustedDescriptor {
		@Override
		public Varargs invoke(final BustedContext context, Varargs args) {
			final String funcName = args.arg(1).checkjstring();
			if (args.arg(2).isnil()) {
				return new OneArgFunction() {
					@Override
					public LuaValue call(LuaValue closure) {
						NamedExecutor.this.invoke(context, funcName, closure.checkfunction());
						return LuaValue.NONE;
					}
				};
			}

			invoke(context, funcName, args.arg(2).checkfunction());
			return LuaValue.NONE;
		}

		public abstract void invoke(BustedContext context, String name, LuaValue closure);
	}

	/**
	 * An executor that just takes a function
	 * (before_each, after_each, setup, teardown)
	 */
	public static abstract class AnonExecutor implements IBustedDescriptor {
		@Override
		public Varargs invoke(BustedContext context, Varargs args) {
			invoke(context, args.arg(1).checkfunction());
			return LuaValue.NONE;
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

			parent.descriptors.put(name, getRunnable(context, closure));
		}

		protected IBustedExecutor getRunnable(final BustedContext context, final LuaValue closure) {
			return new IBustedExecutor() {
				@Override
				public void invoke(BustedContext executingContext) {
					closure.setfenv(context.getEnv());
					closure.invoke();
				}
			};
		}
	}

	public static class LazyDescriptor extends Descriptor {
		private final String requirement;
		public LazyDescriptor(String name, String requirement) {
			super(name);
			this.requirement = requirement;
		}

		public LazyDescriptor(String name) {
			this(name, null);
		}

		@Override
		protected IBustedExecutor getRunnable(final BustedContext context, final LuaValue closure) {
			return new IBustedExecutor() {
				private boolean called = false;

				@Override
				public void invoke(BustedContext executingContext) {
					if (!called) {

						if(requirement != null) {
							Boolean bool = executingContext.descriptorSuccess.get(requirement);
							if(bool == null || !bool) return;

							executingContext.descriptorSuccess.remove(requirement);
						}

						closure.setfenv(context.getEnv());
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
			this(BustedContext.EnvironmentType.Wrap);
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
		public Varargs invoke(BustedContext parent, Varargs args) {
			new IgnoredTest(args.arg1().checkjstring(), parent);
			return LuaValue.NIL;
		}
	}

	public static class RandomizeFunction implements IBustedDescriptor {

		@Override
		public Varargs invoke(BustedContext context, Varargs args) {
			context.randomize = true;
			return LuaValue.NONE;
		}
	}
}
