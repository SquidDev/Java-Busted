package squiddev.busted;

import org.junit.runner.RunWith;

@RunWith(BustedRunner.class)
public class BustedRunnerTest {
	@BustedRunner.Sources(root = "/squiddev/busted/")
	public static String[] sources() {
		return new String[]{
			"BustedTest.lua",

			"luassert/spec/assertions_spec.lua",
			"luassert/spec/spies_spec.lua",
		};
	}
}
