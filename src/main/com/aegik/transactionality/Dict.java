package com.aegik.transactionality;

import java.util.Map;
import java.util.*;

/**
 * A dictionary with key-value pairs.
 * <p>
 * The between this and a hash map with string keys, is that
 * Dict supports transactional changes using a common root object.
 *
 * @see Root
 * @author Christoffer Lerno
 */
public class Dict<C> extends TransactionalContainer<C> implements Map<String, C>, NonPrimitive
{
	private HashMap<String, C> m_values;

	/**
	 * Creates a dictionary with transaction support through the root node.
	 *
	 * @param root the root for this dictionary.
	 * @param internalType the internal type of objects of this dictionary.
	 */
	Dict(Root root, ElementType<C> internalType)
	{
		super(root, internalType);
		m_values = new HashMap<String, C>();

	}

	/**
	 * Creates a copy of an existing dictionary.
	 *
	 * @param original the original dictionary.
	 */
	public Dict(Dict<C> original)
	{
		this(original.root(), original.internalType());
		putAll(original.internalMap());
	}

	protected Map<String, C> internalMap()
	{
		return m_values;
	}

	public Map<String, Object> primitive()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		for (Map.Entry<String, C> entry : m_values.entrySet())
		{
			map.put(entry.getKey(), Klass.getPrimitive(entry.getValue()));
		}
		return map;
	}

	public C create(final String property)
	{
		C newObject = createNewObject();
		put(property, newObject);
		return newObject;
	}

	/**
	 * Sets a property.
	 *
	 * @param property the name of the property.
	 * @param value the new value of the property.
	 * @return the old value of the property.
	 * @throws IllegalArgumentException if the class of the new value is unsupported.
	 */
	public C put(final String property, C value)
	{
		if (value == null)
		{
			return remove(property);
		}
		C oldValue = m_values.put(property, value);
		addUndo(new PutUndo<C>(this, property, oldValue));
		return oldValue;
	}

	/**
	 * Add all properties in a map to this dictionary.
	 *
	 * @param map the map to add.
	 * @throws IllegalArgumentException if any of the values in this map
	 * has a class that is unsupported.
	 */
	public void putAll(Map<? extends String, ? extends C> map)
	{
		if (map.isEmpty()) return;
		HashMap<String, C> values = m_values;
		clear();
		m_values.putAll(values);
		m_values.putAll(map);
	}

	/**
	 * Removes the mapping for this key from this map if present.
	 *
	 * @param key key whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 * if there was no mapping for key.  A <tt>null</tt> return can
	 * also indicate that the map previously associated <tt>null</tt>
	 * with the specified key.
	 */
	public C remove(Object key)
	{
		final String stringKey = String.valueOf(key);
		final C oldValue = m_values.remove(stringKey);
		if (oldValue == null) return null;
		addUndo(new PutUndo<C>(this, stringKey, oldValue));
		return oldValue;
	}

	/**
	 * Returns the number of key-value mappings in this dictionary.
	 *
	 * @return the number of key-value mappings in this dictionary.
	 */
	public int size()
	{
		return m_values.size();
	}

	/**
	 * Returns an unmodifiable collection of all values in this dictionary.
	 *
	 * @return an unmodifiable collection of all values in this dictionary.
	 */
	public Collection<C> values()
	{
		return Collections.unmodifiableCollection(m_values.values());
	}

	/**
	 * Remove all key-value pairs from this map.
	 */
	public void clear()
	{
		if (m_values.isEmpty()) return;
		addUndo(new ClearUndo<C>(this));
		m_values = new HashMap<String, C>();
	}

	/**
	 * Returns <tt>true</tt> if this Dict contains a mapping for the
	 * specified key.
	 *
	 * @param key The key whose presence in this dictionary is to be tested
	 * @return <tt>true</tt> if this dictionary contains a mapping for the specified
	 * key.
	 */
	public boolean containsKey(Object key)
	{
		return m_values.containsKey(key);
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the
	 * specified value.
	 *
	 * @param value value whose presence in this map is to be tested.
	 * @return <tt>true</tt> if this map maps one or more keys to the
	 * specified value.
	 */
	public boolean containsValue(Object value)
	{
		return m_values.containsValue(value);
	}

	/**
	 * Returns an unmodifiable set of all entries in this dictionary.
	 *
	 * @return an unmodifiable set of all entries in this dictionary.
	 */
	public Set<Entry<String, C>> entrySet()
	{
		return Collections.unmodifiableSet(m_values.entrySet());
	}

	/**
	 * Returns a value given the key.
	 *
	 * @param key the key to get the value for.
	 * @return the value associated with the key, or null if no
	 * value is associated with the key.
	 */
	public C get(Object key)
	{
		return m_values.get(key);
	}

	/**
	 * Tests if this dictionary is empty.
	 *
	 * @return true if this dictionary is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return m_values.isEmpty();
	}

	/**
	 * Returns an unmodifiable set of all keys in this dictionary.
	 *
	 * @return an unmodifiable set of all keys in this dictionary.
	 */
	public Set<String> keySet()
	{
		return Collections.unmodifiableSet(m_values.keySet());
	}

	/**
	 * Tests if another Map equals this Dict.
	 *
	 * @return true if the other object is a Map and has the
	 * same key-value pairs as this Dict.
	 */
	@SuppressWarnings({"NonFinalFieldReferenceInEquals"})
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null) return false;
		if (o instanceof Transactional)
		{
			return o.equals(m_values);
		}
		return (o instanceof Map) && m_values.equals(o);
	}

	/**
	 * Returns the hash code for this Dict. The hash code is calculated
	 * from the contents of the dictionary and so will change if the elements
	 * are removed or added.
	 *
	 * @return the hash code of this Dict.
	 */
	@SuppressWarnings({"NonFinalFieldReferencedInHashCode"})
	@Override
	public int hashCode()
	{
		return m_values.hashCode();
	}

	/**
	 * Returns a string representation of the list on the form:
	 * <p>
	 * {@code "{<key1>=<value1>, <key2>=<value2>, ... }" }
	 * <p>
	 * E.g. a dictionary containing the key-values "a" => 1, "b" => 4, "f" => 9, would return the
	 * string "{a=1, b=4, f=9}".
	 *
	 * @return the string representation of this dictionary.
	 */
	@Override
	public String toString()
	{
		return m_values.toString();
	}

	public void init(Map<String, ?> map)
	{
		m_values.clear();
		for (Map.Entry<String, ?> entry : map.entrySet())
		{
			m_values.put(entry.getKey(), internalType().newObject(root(), entry.getValue()));
		}
	}

	/**
	 * Implements undo for clear.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	private static class ClearUndo<T> implements Undo
	{
		private final Dict<T> m_dict;
		private final HashMap<String, T> m_old;

		private ClearUndo(Dict<T> dict)
		{
			m_dict = dict;
			m_old = m_dict.m_values;
		}

		public void undo()
		{
			m_dict.m_values = m_old;
		}
	}

	/**
	 * Implements undo for put.
	 */
	private static class PutUndo<C> implements Undo
	{
		private final Dict<C> m_dict;
		private final String m_property;
		private final C m_oldValue;

		public PutUndo(Dict<C> dict, String property, C oldValue)
		{
			m_dict = dict;
			m_property = property;
			m_oldValue = oldValue;
		}

		@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
		public void undo()
		{
			if (m_oldValue == null)
			{
				m_dict.m_values.remove(m_property);
			}
			else
			{
				m_dict.m_values.put(m_property, m_oldValue);
			}
		}
	}

	public static <C> Dict<C> newDict(Object... keyValues)
	{
		if (keyValues.length % 2 != 0) throw new IllegalArgumentException("Mismatched key/value pairs.");
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < keyValues.length; i += 2)
		{
			if (keyValues[i] == null || !keyValues[i].getClass().equals(String.class)) throw new IllegalArgumentException("Illegal key #" + i / 2 + ": " + keyValues[i]);
			map.put((String) keyValues[i], keyValues[i + 1]);
		}
		return newDict(map);
	}

	@SuppressWarnings({"unchecked"})
    public static <C> Dict<C> newDict(Map map)
	{
        ElementType<C> elementType = ElementType.getReturnType(map.values().iterator().next().getClass());
		Dict<C> d = new Dict(null,
		                     map.isEmpty()
		                     ? null
		                     : elementType);
		d.putAll(map);
		return d;
	}
}
