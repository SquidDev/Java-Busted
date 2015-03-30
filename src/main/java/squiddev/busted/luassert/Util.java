package squiddev.busted.luassert;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * squiddev.busted.luassert (Java-Busted
 */
public class Util {
	/**
	 * Compare two values
	 *
	 * @param a          Value a
	 * @param b          The other value to compare
	 * @param ignoreMeta Ignore metatable __eq methods
	 * @return If the tables are equal
	 */
	public static boolean deepCompare(LuaValue a, LuaValue b, boolean ignoreMeta) {
		int typeA = a.type();
		if (typeA != b.type()) return false;

		if (typeA != LuaValue.TTABLE) return a.eq_b(b);

		LuaValue metaA = a.getmetatable();
		LuaValue metaB = b.getmetatable();

		// If equality can be checked with __eq
		if (metaA != null && metaA.toboolean() && metaA.eq_b(metaB) && metaA.get(LuaValue.EQ).toboolean()) {
			// If we should use metatables then use them
			if (!ignoreMeta) return a.eq_b(b);
		} else {
			// If they are the same
			if (a.eq_b(b)) return true;
		}

		Varargs n;
		LuaValue k = LuaValue.NIL;

		// Check every key in a and see if b is the same
		while (!(k = ((n = a.next(k)).arg1())).isnil()) {
			LuaValue valueB = b.get(k);
			if (valueB.isnil() || !deepCompare(n.arg(2), valueB, ignoreMeta)) return false;
		}

		// Check a has all of b's keys - we've done comparison above
		while (!(k = b.next(k).arg1()).isnil()) {
			if (a.get(k).isnil()) return false;
		}

		return true;
	}

	/**
	 * Checks if a value can be called
	 *
	 * @param value The value to check
	 * @return If this value is a function or has the __call metamethod
	 */
	public static boolean callable(LuaValue value) {
		if (value.isfunction()) return true;

		LuaValue meta = value.getmetatable();
		return meta != null && !meta.isnil() && meta.get(LuaValue.CALL).isfunction();

	}

	public static void tableInsert(LuaValue table, LuaValue value) {
		tableInsert(table, value, LuaValue.NIL);
	}

	public static void tableInsert(LuaValue table, LuaValue value, LuaValue position) {
		LuaValue length = table.get("n");
		if (length.isnil()) length = table.len();
		length.add(1);

		if (position.isnil()) {
			position = length;
		} else if (position.gt_b(length)) {
			table.set(position, value);
			table.set("n", position);
			return;
		}

		table.set("n", length);

		int pos = position.toint();
		for (int i = length.toint(); i > pos; i--) {
			table.set(i, table.get(i - 1));
		}

		table.set(pos, value);
	}
}
