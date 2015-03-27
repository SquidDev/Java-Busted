package squiddev.busted;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Helper functions for working with environments
 */
public class Environment {
	/**
	 * Wrap a value with an environment
	 *
	 * @param value       The function to wrap
	 * @param environment The new environment to use
	 * @return The wrapped LuaValue (identical to value)
	 */
	public static LuaValue wrapValue(LuaValue value, LuaValue environment) {
		value.setfenv(createEnvironment(value.getfenv(), environment));

		return value;
	}

	/**
	 * Create an environment
	 *
	 * @param oldEnv The original environment
	 * @param newEnv The new variables of the environment
	 * @return The new environment
	 */
	public static LuaValue createEnvironment(LuaValue oldEnv, LuaValue newEnv) {
		if (newEnv.getmetatable() != LuaValue.NIL) {
			throw new IllegalArgumentException("Environment metatable must be nil");
		}
		LuaValue metaEnv = new LuaTable(0, 1);
		metaEnv.set("__index", oldEnv);
		newEnv.setmetatable(metaEnv);

		return newEnv;
	}
}
