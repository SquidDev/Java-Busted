package squiddev.busted;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Busted testing framework implemented in Java
 * http://olivinelabs.com/busted/
 */
public class BustedRunner extends ParentRunner<BustedRunner.BustedIt> {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Sources {
		String name() default "{index} {0}";

		String root() default "/";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GetGlobals {
	}

	private Sources sources;
	private String[] bustedSources;
	private Method globals;

	private List<BustedIt> children = null;

	/**
	 * Constructs a new {@code BustedRunner} that will run {@code @TestClass}
	 */
	public BustedRunner(Class<?> klass) throws InitializationError {
		super(klass);

		try {
			getSources();
			globals = getGlobals();
		} catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	protected void getSources() throws Exception {
		for (FrameworkMethod m : getTestClass().getAnnotatedMethods(Sources.class)) {
			if (m.isStatic() && m.isPublic() && m.getReturnType().equals(String[].class)) {
				bustedSources = (String[]) m.getMethod().invoke(null);
				sources = m.getAnnotation(Sources.class);
				return;
			}
		}

		throw new IllegalArgumentException(String.format("Class '%s' must have a public static BustedSources method", getTestClass().getJavaClass()));
	}

	protected Method getGlobals() throws Exception {
		for (FrameworkMethod m : getTestClass().getAnnotatedMethods(GetGlobals.class)) {
			if (m.isStatic() && m.isPublic() && m.getReturnType().equals(LuaValue.class)) {
				return m.getMethod();
			}
		}

		throw new IllegalArgumentException(String.format("Class '%s' must have a public static BustedGlobals method", getTestClass().getJavaClass()));
	}

	@Override
	protected List<BustedIt> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
			for (String source : bustedSources) {
				try {
					LuaValue globals = (LuaValue) this.globals.invoke(null);

					BustedItFunction f = new BustedItFunction();
					f.it = children;
					globals.set("it", f);

					LoadState.load(getClass().getResourceAsStream(sources.root() + source), source, globals).invoke();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return children;
	}

	@Override
	protected Description describeChild(BustedIt child) {
		return child.description;
	}

	@Override
	protected void runChild(final BustedIt child, RunNotifier notifier) {
		Statement statement = new Statement() {
			@Override
			public void evaluate() {
				child.closure.invoke();
			}
		};
		runLeaf(statement, child.description, notifier);
	}

	public class BustedIt {
		public final String text;
		public final LuaValue closure;

		public final Description description;

		public BustedIt(String text, LuaValue closure) {
			this.text = text;
			this.closure = closure;
			description = Description.createTestDescription(getTestClass().getJavaClass(), text);
		}
	}

	public class BustedItFunction extends TwoArgFunction {
		public List<BustedIt> it = new ArrayList<>();

		@Override
		public LuaValue call(LuaValue luaValue, LuaValue luaValue1) {
			it.add(new BustedIt(luaValue.checkjstring(), luaValue1.checkfunction()));
			return LuaValue.NIL;
		}
	}
}
