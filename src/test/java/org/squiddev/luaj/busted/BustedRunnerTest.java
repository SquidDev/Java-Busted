package org.squiddev.luaj.busted;

import org.junit.runner.RunWith;

@RunWith(BustedRunner.class)
public class BustedRunnerTest {
	@BustedRunner.Sources(root = "/org/squiddev/luaj/busted/")
	public static String[] sources() {
		return new String[]{
			"BustedTest.lua",

			"spec/core_spec.lua",
			"spec/execution_order_sync_spec.lua",
			"spec/export_spec.lua",
			"spec/file_context_support_spec.lua",
			"spec/file_randomize_spec.lua",
			"spec/insulate-expose_spec.lua",
			"spec/randomize_spec.lua",
			"spec/test_runner/interface_spec.lua",


			"luassert/spec/assertions_spec.lua",
			"luassert/spec/spies_spec.lua",
			"luassert/spec/stub_spec.lua",
			"luassert/spec/mocks_spec.lua",
		};
	}
}
