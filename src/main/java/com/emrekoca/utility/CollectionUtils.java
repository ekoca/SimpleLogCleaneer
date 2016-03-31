package com.emrekoca.utility;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Utilities for collections.
 * 
 * @author Elli Albek
 */
public class CollectionUtils {

	/**
	 * Returns a lightweight unmodifiable view of the two lists. The method
	 * accepts null and returns the most compact and optimal format, normally
	 * one of the lists passed to it.
	 * 
	 * @return a list view that contains both lists, or null if both are empty.
	 */
	public static <T> List<T> combine(List<T> l1, List<T> l2) {
		if (l1 == null || l1.isEmpty()) { // case l1 empty
			if (l2 == null || l2.isEmpty())
				// case both empty
				return null;
			// case only l2 empty
			return l2;
		} else { // case l1 not empty
			if (l2 == null || l2.isEmpty()) // case only l2 empty
				return l1;
			// case both not empty
			return new CompositeUnmodifiableList<T>(l1, l2);
		}
	}

	/**
	 * Returns non empty list. Empty list returns null
	 */
	public static <T> List<T> notEmpty(List<T> l) {
		if (l == null || l.isEmpty())
			return null;
		return l;
	}

	private static class CompositeUnmodifiableList<E> extends AbstractList<E> {

		private final List<E> list1;
		private final List<E> list2;

		public CompositeUnmodifiableList(List<E> list1, List<E> list2) {
			this.list1 = list1;
			this.list2 = list2;
		}

		@Override
		public E get(int index) {
			if (index < list1.size()) {
				return list1.get(index);
			}
			return list2.get(index - list1.size());
		}

		@Override
		public int size() {
			return list1.size() + list2.size();
		}
	}

	/**
	 * Splits a collection to smaller collections each sized batchSize or less
	 * (the last may be less).
	 */
	public static <T> List<List<T>> split(final List<T> c, final int batchSize) {
		final int size = c.size();

		if (size <= batchSize)
			return Arrays.asList(c);

		int start = 0;
		ArrayList<List<T>> l = new ArrayList<List<T>>();

		while (true) {
			int end = start + batchSize;
			if (end < size) {
				l.add(c.subList(start, end));
				start = end;
			} else
			// last
			{
				l.add(c.subList(start, size));
				return l;
			}
		}
	}

	/**
	 * Inserts an object in the beginning of an array. Returns a new arrays
	 * instance that is one size longer.
	 */
	public static Object[] insert(Object obj, Object[] array) {
		// special case can be optimized
		if (array == null || array.length == 0)
			return new Object[] { obj };
		int len = array.length;
		Object[] r = new Object[len + 1];
		r[0] = obj;
		System.arraycopy(array, 0, r, 1, len);
		return r;
	}

	// public static void main(String[] args) {
	// List<Integer> l = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
	// System.out.println(split(l, 1));
	// System.out.println(split(l, 2));
	// System.out.println(split(l, 3));
	// System.out.println(split(l, 9));
	// }

	// ------------- Multi Map Methods -------------

	/**
	 * Create a map from key/value pairs.
	 */
	public static Map<String, List<String>> createMultivaluedMap(String... keyValues) {
		HashMap<String, List<String>> m = new HashMap<String, List<String>>();
		for (int i = 0; i < keyValues.length; i++) {
			String key = keyValues[i];
			String value = keyValues[++i];
			ArrayList<String> values = new ArrayList<String>(1);
			values.add(value);
			m.put(key, values);
		}

		return m;
	}

	public static <T> T getFirst(Map<String, List<T>> multiValutMap, String key) {
		List<T> values = multiValutMap.get(key);
		if (values != null && !values.isEmpty())
			return values.get(0);
		return null;
	}

	/**
	 * Add a value to a multimap. Supports variety of implementations, to make
	 * the util packages depend on as less third party as possible.
	 */
	public static <T> void add(Map<String, List<T>> map, String key, T value) {
		List<T> values = map.get(key);
		if (values == null) {
			values = new ArrayList<T>();
			map.put(key, values);
		}
		values.add(value);
	}

	public static <T> void addValues(Map<String, List<T>> map, String key, T... values) {
		List<T> array = map.get(key);
		if (array == null) {
			array = new ArrayList<T>();
			map.put(key, array);
		}
		for (T value : values)
			array.add(value);
	}

	/**
	 * Returns a collection with fast contains() implementation. The collection
	 * is intended to be reusable, a single use will not be efficient. Most
	 * efficient with primitive objects (String, Integer...)
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> fastSearchCollection(T... arr) {
		final int size = arr.length;
		if (size == 0)
			return Collections.EMPTY_LIST;

		if (size == 1)
			Collections.singletonList(arr[0]);

		// optimization for short list, avoid hash
		if (size <= 8) {
			return Arrays.asList(arr);
		}
		// big lists, use hash (constant time)
		return new HashSet<T>(Arrays.asList(arr));
	}

	/**
	 * Returns a collection with fast contains() implementation. The collection
	 * is intended to be reusable. A single use will not be efficient as it
	 * allocates memory for the new collection.
	 * 
	 * Most efficient with primitive objects (String, Integer...)
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> fastSearchCollection(List<T> list) {
		final int size = list.size();
		if (size == 0)
			return Collections.EMPTY_LIST;

		if (size == 1)
			Collections.singletonList(list.get(0));

		// optimization for short list, avoid hash
		if (size <= 8) {
			if (list instanceof ArrayList)
				// ArrayList similar to Arrays for short lists, save some
				// memory.
				return (ArrayList<T>) list;

			// not ArrayList, play safe with final array type list
			T[] s = (T[]) list.toArray();
			return Arrays.asList(s);
		}
		// big lists, use hash (constant time)
		return new HashSet<T>(list);
	}

	public static <T> T getFirst(List<T> l) {
		if (l == null || l.isEmpty())
			return null;
		return l.get(0);
	}
}
