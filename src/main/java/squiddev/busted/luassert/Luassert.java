package squiddev.busted.luassert;

import org.hamcrest.Matcher;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import squiddev.busted.luassert.assertions.IAssertion;
import squiddev.busted.luassert.modifiers.IModifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The main assert library
 * TODO: Make this not an ugly mess
 */
public class Luassert {
	public final AssertData assertData = new AssertData();
	public final Spy spy = new Spy(assertData);
	public final Stub stub = new Stub(assertData);
	public final Mock mock = new Mock(spy, stub);

	public final LuaTable table;

	public Luassert() {
		LuaTable table = this.table = new LuaTable();
		table.set("state", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new AssertChain().table;
			}
		});

		LuaTable meta = new LuaTable();
		table.setmetatable(meta);

		meta.set(LuaValue.INDEX, new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue self, LuaValue key) {
				AssertChain state = new AssertChain();
				state.index(key.toString());
				return state.table;
			}
		});

		meta.set(LuaValue.CALL, new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				if (!args.arg(2).toboolean()) {
					throw new LuaError(new AssertionError(args.optjstring(3, "assertion failed!")).fillInStackTrace());
				}

				// TODO: Returning subargs seems to break things (only last value is copied)
				int narg = args.narg() - 1;
				LuaValue[] varargs = new LuaValue[narg];
				for (int i = 0; i < narg; i++) {
					varargs[i] = args.arg(i + 2);
				}
				return LuaValue.varargsOf(varargs);
			}
		});
	}

	public class AssertChain {
		private final List<String> tokens = new ArrayList<>();

		/**
		 * The API table
		 */
		public final LuaTable table;

		/**
		 * Arguments from a previous file
		 */
		public Varargs payload;

		public AssertChain() {
			LuaTable table = this.table = new LuaTable();

			LuaTable meta = new LuaTable();
			table.setmetatable(meta);

			meta.set(LuaValue.INDEX, new TwoArgFunction() {
				@Override
				public LuaValue call(LuaValue self, LuaValue key) {
					AssertChain.this.index(key.toString());
					return self;
				}
			});

			meta.set(LuaValue.CALL, new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					AssertChain.this.call(args.subargs(2));
					return args.arg1();
				}
			});
		}

		public void call(Varargs args) {
			final List<IModifier> mods = new ArrayList<>();

			List<String> tokens = this.tokens;
			String key = null;

			final Map<String, IAssertion> assertions = assertData.assertions.items;
			final Map<String, IModifier> modifiers = assertData.modifiers.items;

			IAssertion assertion = null;
			for (int i = tokens.size() - 1; i >= 0; i--) {
				String token = tokens.get(i);

				if (key != null) token = token + "_" + key;

				IAssertion newAssertion = assertions.get(token);
				if (newAssertion != null) {
					assertion = newAssertion;
					key = null;
				} else {
					IModifier modifier = modifiers.get(token);

					if (modifier != null) {
						mods.add(modifier);
						key = null;
					} else {
						key = token;
					}
				}
			}

			if (assertion != null) {
				try {
					assertion.match(args, payload, new IModifier() {
						@Override
						public Matcher<LuaValue> modify(Matcher<LuaValue> matcher) {
							for (IModifier mod : mods) {
								matcher = mod.modify(matcher);
							}

							return matcher;
						}
					});
				} catch (Throwable e) {
					throw new LuaError(e);
				}
			} else {
				payload = args;
			}
		}

		public void index(String key) {
			Collections.addAll(tokens, key.toLowerCase().split("_"));
		}
	}
}
