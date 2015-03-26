package squiddev.busted;

import org.junit.runner.RunWith;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

@RunWith(BustedRunner.class)
public class BustedRunnerTest {
	@BustedRunner.GetGlobals
	public static LuaValue getGlobals() {
		return JsePlatform.debugGlobals();
	}

	@BustedRunner.Sources(root = "/squiddev/busted/")
	public static String[] sources() {
		return new String[]{
			"BustedTest.lua"
		};
	}
}
