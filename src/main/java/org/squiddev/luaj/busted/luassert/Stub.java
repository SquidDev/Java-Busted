package org.squiddev.luaj.busted.luassert;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.squiddev.luaj.busted.luassert.modifiers.IModifier;

import static org.junit.Assert.fail;

/**
 * Create a stub object
 */
public class Stub {
	public final LuaTable table;

	public Stub(AssertData data) {
		LuaTable table = this.table = new LuaTable();

		final VarArgFunction stub = new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				return createStub(args.arg1(), args.arg(2), args.subargs(3)).table;
			}
		};
		table.set("new", stub);

		LuaTable meta = new LuaTable();
		table.setmetatable(meta);
		meta.set(LuaValue.CALL, new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				return stub.invoke(args.subargs(2));
			}
		});

		data.modifiers.register("stub", IModifier.EMPTY);
	}

	public StubWrapper createStub(LuaValue object, LuaValue key, final Varargs args) {
		// Create an empty stub
		if (object.isnil() && key.isnil()) {
			object = new LuaTable();
			key = LuaValue.EMPTYSTRING;
		}

		if (object.type() != LuaValue.TTABLE || key.isnil()) {
			fail("stub.new(): Can only create stub on a table key, call with 2 params; table, key");
		}

		if (!object.get(key).isnil() && !Util.callable(object.get(key))) {
			fail("stub.new(): The element for which to create a stub must either be callable, or be nil");
		}

		LuaValue original = object.get(key);
		object.set(key, new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs ignore) {
				return args;
			}
		});

		return new StubWrapper(object, key, original);
	}

	public static class StubWrapper extends Spy.TableWatcher {
		public final LuaValue original;

		public StubWrapper(LuaValue table, LuaValue key, LuaValue original) {
			super(table, key);
			this.original = original;
		}

		/**
		 * Rollback to the normal callback
		 */
		@Override
		public LuaValue rollback() {
			if (!rolledBack) {
				rolledBack = true;
				lookup.set(key, original);
			}

			return original;
		}
	}
}
