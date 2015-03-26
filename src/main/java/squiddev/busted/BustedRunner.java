package squiddev.busted;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

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
public class BustedRunner extends TestItemRunner<LuaFile> {
	/**
	 * A method that returns a {@code String[]} of files to execute
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Sources {
		/**
		 * The root path to find files from
		 */
		String root() default "/";
	}

	/**
	 * A method that takes two arguments:
	 * {@link String} would be the file path
	 * {@link BustedVariables} being additional globals to use
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface RunFile {
	}

	public final String bustedRoot;
	public final String[] bustedSources;
	public final Method runFile;

	/**
	 * Constructs a new {@code BustedRunner} that will run {@code @TestClass}
	 */
	public BustedRunner(Class<?> klass) throws InitializationError {
		super(klass);

		try {
			{
				List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Sources.class);
				if (methods.size() != 1) {
					throw new IllegalArgumentException(String.format("Class '%s' must have one @Sources method", getTestClass().getJavaClass()));
				}

				FrameworkMethod m = methods.get(0);
				if (!m.isStatic() || !m.isPublic()) {
					throw new IllegalArgumentException("@Sources method must be public static");
				}
				if (!m.getReturnType().equals(String[].class)) {
					throw new IllegalArgumentException("@Sources method must return a String[]");
				}

				bustedRoot = m.getAnnotation(Sources.class).root();
				bustedSources = (String[]) m.getMethod().invoke(null);

			}
			{
				List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(RunFile.class);

				// If we have no methods then we can just return the default
				if (methods.size() == 0) {
					runFile = BustedRunner.class.getDeclaredMethod("runFile", String.class, BustedVariables.class);
				} else {

					// If we have more than one than that is a programmer error
					if (methods.size() > 1) {
						throw new IllegalArgumentException(String.format("Class '%s' must have only one @Sources method", getTestClass().getJavaClass()));
					}

					FrameworkMethod m = methods.get(0);

					// Should be a public static method
					if (!m.isStatic() || !m.isPublic()) {
						throw new IllegalArgumentException("@RunFile method must be public static");
					}
					Method method = m.getMethod();

					// Needs to accept (String, BustedGlobals)
					Class<?>[] params = method.getParameterTypes();
					if (params.length != 2 || !params[0].equals(String.class) || !params[1].equals(BustedVariables.class)) {
						throw new IllegalArgumentException("@RunFile method must accept be in the form (String, BustedGlobals)");
					}

					runFile = method;
				}
			}
		} catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	@Override
	protected List<LuaFile> getChildren() {
		List<LuaFile> children = new ArrayList<>();
		for (String source : bustedSources) {
			try {
				children.add(new LuaFile(source, this));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return children;
	}

	/**
	 * Default RunFile method
	 *
	 * @param file   The path of the file to run
	 * @param busted The busted globals to use
	 * @throws Exception
	 */
	protected static void runFile(String file, BustedVariables busted) throws Exception {
		LuaValue globals = JsePlatform.debugGlobals();
		busted.bind(globals);
		LoadState.load(BustedRunner.class.getResourceAsStream(file), file, globals).invoke();
	}
}
