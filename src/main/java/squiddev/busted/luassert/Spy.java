package squiddev.busted.luassert;

import org.hamcrest.Description;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import squiddev.busted.luassert.assertions.IAssertion;
import squiddev.busted.luassert.assertions.Negatable;
import squiddev.busted.luassert.modifiers.IModifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Spies on functions and tracks calls
 */
public class Spy {
	public final LuaTable table;

	public Spy(AssertData data) {
		LuaTable table = this.table = new LuaTable();

		table.set("new", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue callback) {
				return new Watcher(callback).table;
			}
		});

		table.set("on", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue table, LuaValue key) {
				return new TableWatcher(table, key).table;
			}
		});

		table.set("is_spy", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue table) {
				return LuaValue.valueOf(isSpy(table));
			}
		});

		data.modifiers.register("spy", IModifier.EMPTY);
		data.assertions.register("called", new IAssertion() {
			@Override
			public void match(Varargs args, Varargs payload, IModifier modifier) {
				LuaValue numTimes = args.arg1();
				LuaValue spy = payload.arg1();

				if (isSpy(spy)) {
					assertThat(spy, modifier.modify(new AssertCalled(numTimes)));
				} else if (Util.callable(spy)) {
					fail("When calling 'spy(aspy)', 'aspy' must not be the original function, but the spy function replacing the original");
				} else {
					fail("'called' must be chained after 'spy(aspy)'");
				}
			}
		});

		data.assertions.register("called_with", new IAssertion() {
			@Override
			public void match(Varargs args, Varargs payload, IModifier modifier) {
				LuaValue spy = payload.arg1();

				if (isSpy(spy)) {
					assertThat(spy, modifier.modify(new AssertCalledWith(new LuaTable(args))));
				} else if (Util.callable(spy)) {
					fail("When calling 'spy(aspy)', 'aspy' must not be the original function, but the spy function replacing the original");
				} else {
					fail("'called_with' must be chained after 'spy(aspy)'");
				}
			}
		});
	}

	/**
	 * Watches what a function is called with
	 */
	public static class Watcher {
		public final LuaTable table;

		public final List<LuaValue> calledWith = new ArrayList<>();
		public final LuaValue callback;

		public Watcher(final LuaValue callback) {
			if (!Util.callable(callback)) {
				throw new LuaError("Cannot spy on type '" + callback.typename() + "', only on functions or callable elements");
			}

			this.callback = callback;

			LuaTable table = this.table = new LuaTable();
			table.set("__isSpy", LuaValue.TRUE);

			table.set("revert", new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					return rollback();
				}
			});

			table.set("called", new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					LuaValue called = args.arg(2);

					LuaValue calledSize = LuaValue.valueOf(calledWith.size());

					if (called.isnil()) {
						return LuaValue.varargsOf(calledSize.gt(0), calledSize);
					}

					return LuaValue.varargsOf(calledSize.eq(called), calledSize);
				}
			});

			table.set("called_with", new TwoArgFunction() {
				@Override
				public LuaValue call(LuaValue self, LuaValue args) {
					return LuaValue.valueOf(calledWith(args));
				}
			});

			LuaTable meta = new LuaTable();
			table.setmetatable(meta);
			meta.set(LuaValue.CALL, new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					return Watcher.this.call(args.subargs(2));
				}
			});
		}

		/**
		 * Rollback to the normal callback
		 */
		public LuaValue rollback() {
			return callback;
		}

		public boolean calledWith(LuaValue args) {
			for (LuaValue called : calledWith) {
				if (Util.deepCompare(called, args, false)) {
					return true;
				}
			}

			return false;
		}

		public Varargs call(Varargs args) {
			calledWith.add(new LuaTable(args));
			return callback.invoke(args);
		}
	}

	/**
	 * Overrides a table's function
	 */
	public static class TableWatcher extends Watcher {
		public final LuaValue lookup;
		public final LuaValue key;

		protected boolean rolledBack = false;

		public TableWatcher(LuaValue table, LuaValue key) {
			super(table.get(key));

			table.set(key, this.table);

			this.lookup = table;
			this.key = key;
		}

		/**
		 * Rollback to the normal callback
		 */
		@Override
		public LuaValue rollback() {
			if (!rolledBack) {
				rolledBack = true;
				lookup.set(key, callback);
			}

			return super.rollback();
		}
	}

	public boolean isSpy(LuaValue value) {
		return value.type() == LuaValue.TTABLE && value.get("__isSpy").toboolean();
	}

	private static final class AssertCalled extends Negatable<LuaValue> {
		public final LuaValue called;

		private AssertCalled(LuaValue called) {
			this.called = called;
		}

		@Override
		public void addPositive(Description description) {
			description.appendText("called ").appendValue(called);
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("not called ").appendValue(called);
		}

		@Override
		protected boolean matchesSafely(LuaValue spy) {
			return spy.method("called", called).toboolean(1);
		}
	}

	private static final class AssertCalledWith extends Negatable<LuaValue> {
		public final LuaValue args;

		private AssertCalledWith(LuaValue args) {
			this.args = args;
		}

		@Override
		public void addPositive(Description description) {
			description.appendText("called with ").appendValue(new ValueWrapper(args));
		}

		@Override
		public void addNegative(Description description) {
			description.appendText("not called with ").appendValue(new ValueWrapper(args));
		}

		@Override
		protected boolean matchesSafely(LuaValue spy) {
			return spy.method("called_with", args).toboolean(1);
		}
	}
}
