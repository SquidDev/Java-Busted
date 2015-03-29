package squiddev.busted;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A registry for items that can be aliased
 */
public class Registry<T> implements Iterable<Map.Entry<String, T>> {
	public final Map<String, T> items = new HashMap<>();

	/**
	 * Register a function
	 *
	 * @param name The name of the item
	 * @param item The item
	 */
	public void register(String name, T item) {
		if (items.containsKey(name)) throw new IllegalArgumentException("Cannot override " + name);
		items.put(name, item);
	}

	/**
	 * Register a function many times
	 *
	 * @param names The names to register under
	 * @param item  The item to register
	 */
	public void register(String[] names, T item) {
		for (String name : names) {
			register(name, item);
		}
	}

	/**
	 * Create an alias of an existing item
	 *
	 * @param name     The alias of the item
	 * @param original The original name of the item
	 */
	public void register(String name, String original) {
		items.put(name, items.get(original));
	}

	@Override
	public Iterator<Map.Entry<String, T>> iterator() {
		return items.entrySet().iterator();
	}
}
