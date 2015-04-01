package org.squiddev.luaj.busted.luassert;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Map;

/**
 * Overwrites every item with a spy
 */
public class Mock extends VarArgFunction {
	public final Stub stub;
	public final Spy spy;

	public Mock(Spy spy, Stub stub) {
		this.stub = stub;
		this.spy = spy;
	}

	@Override
	public Varargs invoke(Varargs args) {
		return mock(args.arg(1), args.arg(2), args.arg(3), args.arg(4), args.arg(5));
	}

	public LuaValue mock(LuaValue object, LuaValue doStub, LuaValue func, LuaValue self, LuaValue key) {
		if (object.type() == LuaValue.TTABLE) {
			if (!spy.isSpy(object)) {
				for (Map.Entry<LuaValue, LuaValue> entry : new ValueWrapper(object)) {
					object.set(entry.getKey(), mock(entry.getValue(), doStub, func, object, entry.getKey()));
				}
			}
		} else if (object.type() == LuaValue.TFUNCTION) {
			if (doStub.toboolean()) {
				return stub.createStub(self, key, func).table;
			} else if (self.isnil()) {
				return new Spy.Watcher(object).table;
			} else {
				return new Spy.TableWatcher(self, key).table;
			}
		}

		return object;
	}
}
