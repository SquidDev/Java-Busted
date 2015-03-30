package squiddev.busted;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import squiddev.busted.blocks.LuaFile;
import squiddev.busted.descriptor.Busted;
import squiddev.busted.descriptor.BustedContext;

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
	 * {@link BustedContext} being additional globals to use
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface RunFile {
	}

	/**
	 * A method that takes {@link Busted} as an argument to register custom globals
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SetupBusted {
	}

	public final String bustedRoot;
	public final String[] bustedSources;
	public final Method runFile;

	public final Busted busted = new Busted();

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
					runFile = BustedRunner.class.getDeclaredMethod("runFile", String.class, LuaFile.Globals.class);
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
					if (params.length != 2 || !params[0].equals(String.class) || !params[1].equals(LuaFile.Globals.class)) {
						throw new IllegalArgumentException("@RunFile method must accept be in the form (String, LuaFile.Globals)");
					}

					runFile = method;
				}
			}

			{
				for (FrameworkMethod m : getTestClass().getAnnotatedMethods(SetupBusted.class)) {
					// Should be a public static method
					if (!m.isStatic() || !m.isPublic()) {
						throw new IllegalArgumentException("@SetupBusted method must be public static");
					}
					Method method = m.getMethod();

					// Needs to accept (String, BustedGlobals)
					Class<?>[] params = method.getParameterTypes();
					if (params.length != 1 || !params[0].equals(String.class) || !params[1].equals(LuaFile.Globals.class)) {
						throw new IllegalArgumentException("@SetupBusted method must accept be in the form (Busted)");
					}

					method.invoke(null, busted);
				}
			}
		} catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	@Override
	protected List<LuaFile> getInternalChildren() throws Exception {
		List<LuaFile> children = new ArrayList<>();
		for (String source : bustedSources) {
			children.add(new LuaFile(source, this));
		}
		return children;
	}

	/**
	 * Default RunFile method
	 *
	 * @param file    The path of the file to run
	 * @param context The busted globals to use
	 * @throws Exception
	 */
	public static void runFile(String file, LuaFile.Globals context) throws Exception {
		LuaValue globals = JsePlatform.debugGlobals();
		context.setEnv(globals);
		LoadState.load(BustedRunner.class.getResourceAsStream(file), file, globals).invoke();
	}
}
