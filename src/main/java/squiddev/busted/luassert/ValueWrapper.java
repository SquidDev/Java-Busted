package squiddev.busted.luassert;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.*;

/**
 * Wraps a Lua value with helper functions
 */
public class ValueWrapper extends AbstractMap<LuaValue, LuaValue> implements Iterable<Map.Entry<LuaValue, LuaValue>> {
	public final LuaValue value;

	public ValueWrapper(LuaValue value) {
		this.value = value;
	}

	@Override
	public int size() {
		return value.length();
	}

	@Override
	public boolean isEmpty() {
		return value.isnil() || value.istable() && !value.next(LuaValue.NIL).arg1().isnil();
	}

	@Override
	public boolean containsKey(Object k) {
		return !value.get((LuaValue) k).isnil();
	}

	@Override
	public boolean containsValue(Object v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LuaValue get(Object k) {
		return value.get((LuaValue) k);
	}

	@Override
	public LuaValue put(LuaValue k, LuaValue v) {
		value.set(k, v);
		return v;
	}

	@Override
	public LuaValue remove(Object key) {
		LuaValue v = value.get((LuaValue) key);

		if (!v.isnil()) value.set((LuaValue) key, v);
		return v;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<LuaValue, LuaValue>> entrySet() {
		return new EntrySet();
	}


	@Override
	public boolean equals(Object o) {
		return o instanceof LuaValue && (value == o || value.eq_b((LuaValue) o));
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public Iterator<Entry<LuaValue, LuaValue>> iterator() {
		return new EntryIterator();
	}

	@Override
	public String toString() {
		if (value.type() == LuaValue.TTABLE) {
			StringBuilder builder = new StringBuilder();

			int previous = 0;
			for (Entry<LuaValue, LuaValue> item : this) {
				if (item.getKey().isnumber() && item.getKey().toint() == previous + 1) {
					previous++;
					builder.append(item.getValue());
				} else {
					builder.append("[").append(item.getKey()).append("] = ").append(item.getValue());
				}

				builder.append(", ");
			}

			if (builder.length() > 0) return builder.substring(0, builder.length() - 2);
			return builder.toString();

		} else {
			return value.toString();
		}
	}

	private class EntrySet extends AbstractSet<Entry<LuaValue, LuaValue>> {
		public Iterator<Entry<LuaValue, LuaValue>> iterator() {
			return ValueWrapper.this.iterator();
		}

		public boolean contains(Object o) {
			return containsKey(o);
		}

		public boolean remove(Object o) {
			return o instanceof Entry && !ValueWrapper.this.remove(((Entry) o).getKey()).isnil();

		}

		public int size() {
			return ValueWrapper.this.size();
		}

		public void clear() {
			ValueWrapper.this.clear();
		}
	}

	private class EntryIterator implements Iterator<Entry<LuaValue, LuaValue>> {
		private LuaValue next = LuaValue.NIL;

		@Override
		public boolean hasNext() {
			return !value.next(next).isnil(1);
		}

		@Override
		public Entry<LuaValue, LuaValue> next() {
			Varargs n = value.next(next);
			return new AbstractMap.SimpleEntry<>(next = n.arg(1), n.arg(2));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
