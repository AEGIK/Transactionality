package transactionality;

import java.util.*;

/**
 * An array works like an ArrayList, containing a list of items.
 * <p>
 * The difference is that Array supports transactional changes.
 *
 * @author Christoffer Lerno
 */
public class Array<C> extends TransactionalContainer<C> implements List<C>, NonPrimitive
{
	private ArrayList<C> m_list;

	/**
	 * Creates an array with transactional support, belonging to the given root.
	 *
	 * @param root the root owning this list. I.e. that handles
	 * transactions for this array.
	 * @param internalType the object type for this class.
	 */

	Array(Root root, ElementType internalType)
	{
		super(root, internalType);

		m_list = new ArrayList<C>();
	}


	/**
	 * Creates a copy of an existing array.
	 *
	 * @param original the original array.
	 */
	public Array(Array<C> original)
	{
		this(original.root(), original.internalType());
		addAll(original);
	}


	public C create()
	{
		C object = createNewObject();
		add(object);
		return object;
	}

	public C create(int index)
	{
		C object = createNewObject();
		add(index, object);
		return object;
	}

	/**
	 * Returns an entry in this list.
	 *
	 * @param index the index to extract the entry from.
	 * @return the value at the index.
	 * @throws IndexOutOfBoundsException if index is out of range.
	 */
	public C get(int index)
	{
		return m_list.get(index);
	}

	/**
	 * Returns the size of this list.
	 *
	 * @return the size of the list.
	 */
	public int size()
	{
		return m_list.size();
	}

	/**
	 * Inserts the specified element at the specified position in this list
	 * shifting the element currently at that position (if any) and any
	 * subsequent elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted.
	 * @param element element to be inserted.
	 * @throws IllegalArgumentException if the class of the specified element
	 * is not supported.
	 * @throws NullPointerException if the specified element is null.
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	public void add(final int index, C element)
	{
		m_list.add(index, element);
		addUndo(new AddUndo(this, index));
	}

	/**
	 * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of the
     * <tt>Collection.add</tt> method).
	 * @throws IllegalArgumentException if the class of the specified element
	 * is not supported.
     * @throws NullPointerException if the specified element is null and this
     * list does not support null elements.
     */
	public boolean add(C o)
	{
		addUndo(new AddUndo(this, m_list.size()));
		return m_list.add(o);
	}

	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the specified
	 * collection's iterator.
	 *
	 * @param c collection whose elements are to be added to this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * @throws IllegalArgumentException if the class of any of the elements
	 * in the collection is not supported.
	 * @throws NullPointerException if the specified collection contains one
	 * or more null elements.
	 */
	public boolean addAll(Collection<? extends C> c)
	{
		List old = m_list;
		clear();
		m_list.addAll(old);
		return m_list.addAll(c);
	}

	/**
	 * Inserts all of the elements in the specified collection into this
	 * list at the specified position.  Shifts the
	 * element currently at that position (if any) and any subsequent
	 * elements to the right (increases their indices).  The new elements
	 * will appear in this list in the order that they are returned by the
	 * specified collection's iterator.
	 *
	 * @param index index at which to insert first element from the specified
	 * collection.
	 * @param c collection whose elements are to be added to this list.
	 * @return <tt>true</tt> if this list changed as a result of the call.
	 * @throws IllegalArgumentException if the class of any of the elements
	 * in the collection is not supported.
	 * @throws NullPointerException if the specified collection contains one
	 * or more null elements.
	 */
	public boolean addAll(int index, Collection<? extends C> c)
	{
		List old = m_list;
		clear();
		m_list.addAll(old);
		return m_list.addAll(index, c);
	}

	/**
	 * Clears this array.
	 */
	public void clear()
	{
		addUndo(new ClearUndo(this));
		m_list = new ArrayList();
	}



	/**
	 * Returns <tt>true</tt> if this list contains the specified element.
	 *
	 * @param o element whose presence in this list is to be tested.
	 * @return <tt>true</tt> if this list contains the specified element.
	 */
	public boolean contains(Object o)
	{
		return m_list.contains(o);
	}

	/**
	 * Returns <tt>true</tt> if this list contains <em>all</em> the
	 * elements in the specified collection.
	 *
	 * @param c the collection to test.
	 * @return true if all the elements in the collection exists in the array, false otherwise.
	 */
	public boolean containsAll(Collection<?> c)
	{
		return m_list.containsAll(c);
	}

	/**
	 * Searches for the first occurence of the given argument, testing
	 * for equality using the <tt>equals</tt> method.
	 *
	 * @param o the object to search for.
	 * @return  the index of the first occurrence of the argument in this
	 * ist; returns <tt>-1</tt> if the object is not found.
	 */
	public int indexOf(Object o)
	{
		return m_list.indexOf(o);
	}

	/**
	 * Tests if this list has no elements.
	 *
	 * @return  <tt>true</tt> if this list has no elements;
	 * <tt>false</tt> otherwise.
	 */
	public boolean isEmpty()
	{
		return m_list.isEmpty();
	}

	/**
     * Returns an iterator over the elements in this list in proper sequence.
     * <p>
	 * Note that this iterator does not support remove.
	 *
     * @return an iterator over the elements in this list in proper sequence.
     */
	public Iterator<C> iterator()
	{
		return Collections.unmodifiableList(m_list).iterator();
	}

	/**
	 * Returns the index of the last occurrence of the specified object in
	 * this list.
	 *
	 * @param o the element to look for.
	 * @return  the index of the last occurrence of the specified object in
	 * this list; returns -1 if the object is not found.
	 */
	public int lastIndexOf(Object o)
	{
		return m_list.lastIndexOf(o);
	}

	/**
     * Returns a list iterator of the elements in this list.
	 * <p>
	 * Note that this iterator does not support remove.
     *
     * @return a list iterator of the elements in this list.
     */
	public ListIterator<C> listIterator()
	{
		return Collections.unmodifiableList(m_list).listIterator();
	}

	/**
	 * Returns a list iterator of the elements in this list starting
	 * from the given index.
	 * <p>
	 * Note that this iterator does not support remove.
	 *
	 * @param index the index to start from.
	 * @return a list iterator of the elements in this list.
	 */
	public ListIterator<C> listIterator(int index)
	{
		return Collections.unmodifiableList(m_list).listIterator(index);
	}

	/**
	 * Removes the element at the specified position in this list.
	 * Shifts any subsequent elements to the left (subtracts one from their
	 * indices).
	 *
	 * @param index the index of the element to removed.
	 * @return the element that was removed from the list.
	 * @throws    IndexOutOfBoundsException if index out of range <tt>(index
	 * &lt; 0 || index &gt;= size())</tt>.
	 */
	public C remove(final int index)
	{
		C removed = m_list.remove(index);
		addUndo(new RemoveUndo(this, index, removed));
		return removed;
	}

	/**
	 * Removes a single instance of the specified element from this
	 * list, if it is present.
	 *
	 * @param o element to be removed from this list, if present.
	 * @return <tt>true</tt> if the list contained the specified element.
	 */
	public boolean remove(Object o)
	{
		final int index = m_list.indexOf(o);
		if (index == -1) return false;
		remove(index);
		return true;
	}

	/**
	 * Removes from this collection all of its elements that are contained in
	 * the specified collection.
	 *
	 * @param c elements to be removed from this collection.
	 * @return <tt>true</tt> if this collection changed as a result of the
	 * call.
	 * @throws NullPointerException if the specified collection is null.
	 */
	public boolean removeAll(Collection<?> c)
	{
		if (c == null) throw new NullPointerException();
		ArrayList oldList = m_list;
		clear();
		m_list.addAll(oldList);
		return m_list.removeAll(c);
	}

	/**
	 * Retains only the elements in this collection that are contained in the
	 * specified collection (optional operation).  In other words, removes
	 * from this collection all of its elements that are not contained in the
	 * specified collection.
	 *
	 * @param c elements to be retained in this collection.
	 * @return <tt>true</tt> if this collection changed as a result of the
	 * call.
	 * @throws NullPointerException if the specified collection is null.
	 */
	public boolean retainAll(Collection<?> c)
	{
		if (c == null) throw new NullPointerException();
		ArrayList oldList = m_list;
		clear();
		m_list.addAll(oldList);
		return m_list.retainAll(c);
	}

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     *
     * @throws NullPointerException if the specified element is null and
     * this list does not support null elements.
     * @throws IllegalArgumentException if the class of this element isn't
     * supported.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
	public C set(int index, C element)
	{
		C old = remove(index);
		add(index, element);
		return old;
	}

	/**
	 * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     *
	 * @param fromIndex the starting index.
	 * @param toIndex the end index.
	 * @return the sub list.
	 */
	public List<C> subList(int fromIndex, int toIndex)
	{
		return Collections.unmodifiableList(m_list.subList(fromIndex, toIndex));
	}

	/**
	 * Returns an array containing all of the elements in this list
	 * in the correct order.
	 *
	 * @return an array containing all of the elements in this list
	 * in the correct order.
	 */
	public Object[] toArray()
	{
		return m_list.toArray();
	}

	/**
	 * Returns an array containing all of the elements in this list in the
	 * correct order; the runtime type of the returned array is that of the
	 * specified array.  If the list fits in the specified array, it is
	 * returned therein.  Otherwise, a new array is allocated with the runtime
	 * type of the specified array and the size of this list.<p>
	 * @param a the array into which the elements of the list are to
	 * be stored, if it is big enough; otherwise, a new array of the
	 * same runtime type is allocated for this purpose.
	 * @return an array containing the elements of the list.
	 * @throws ArrayStoreException if the runtime type of a is not a supertype
	 * of the runtime type of every element in this list.
	 */
	public <T> T[] toArray(T[] a)
	{
		return m_list.toArray(a);
	}


	/**
	 * Returns a string representation of the list on the form:
	 * <p>
	 * {@code "[<get(0).toString()>, <get(1).toString()>, ... ]" }
	 * <p>
	 * E.g. an array containing the numbers 4, 6 and 9 in that order, would return the
	 * string "[4, 6, 9]".
	 *
	 * @return the string representation of this array.
	 */
	@Override
	public String toString()
	{
		return m_list.toString();
	}

	/**
	 * Tests this array list for equality with another list.
	 *
	 * @param obj another object to compare to.
	 * @return true if the other object is a List with objects that
	 * equal the objects in this Array in the same order.
	 */
	@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass", "NonFinalFieldReferenceInEquals"})
	@Override
	public boolean equals(Object obj)
	{
		return m_list.equals(obj);
	}

	/**
	 * Returns the hash code for this list. The hash code is calculated
	 * from the contents of the list and so it will change if the elements
	 * of the Array is changed.
	 *
	 * @return the hash code of this Array.
	 */
	@SuppressWarnings({"NonFinalFieldReferencedInHashCode"})
	@Override
	public int hashCode()
	{
		return m_list.hashCode();
	}

	public List primitive()
	{
		ArrayList list = new ArrayList();
		for (Object entry : m_list)
		{
			list.add(Klass.getPrimitive(entry));
		}
		return list;
	}

	/**
	 * Implements undo for a clear.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	private static class ClearUndo implements Undo
	{
		private final Array m_array;
		private final ArrayList m_old;

		private ClearUndo(Array array)
		{
			m_array = array;
			m_old = m_array.m_list;
		}

		public void undo()
		{
			m_array.m_list = m_old;
		}
	}

	/**
	 * Implements undo for a remove.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	private static class RemoveUndo implements Undo
	{
		private final Array m_array;
		private final int m_index;
		private final Object m_oldValue;

		private RemoveUndo(Array array, int index, Object oldValue)
		{
			m_array = array;
			m_index = index;
			m_oldValue = oldValue;
		}

		public void undo()
		{
			m_array.m_list.add(m_index, m_oldValue);
		}

	}

	/**
	 * Implements undo for an add.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	private static class AddUndo implements Undo
	{
		private final Array m_array;
		private final int m_index;

		private AddUndo(Array array, int index)
		{
			m_array = array;
			m_index = index;
		}

		public void undo()
		{
			m_array.m_list.remove(m_index);
		}
	}

	/**
	 * Get the last element of this array.
	 *
	 * @return the last element of this array or null if the array is empty.
	 */
	public <C> C getLast()
	{
		return isEmpty() ? null : (C) get(size() - 1);
	}

	/**
	 * Get the first element of this array.
	 *
	 * @return the first element of this array, or null if the array is empty.
	 */
	public <C> C getFirst()
	{
		return isEmpty() ? null : (C) get(0);
	}

	/**
	 * Remove the last element of this array.
	 *
	 * @return the last element of this array or null if the array is empty.
	 */
	public <C> C removeLast()
	{
		return isEmpty() ? null : (C) remove(size() - 1);
	}

	/**
	 * Remove the first element of this array.
	 *
	 * @return the first element of this array or null if the array is empty.
	 */
	public <C> C removeFirst()
	{
		return isEmpty() ? null : (C) remove(0);
	}

	public boolean setAdd(C c)
	{
		if (!contains(c))
		{
			add(c);
			return true;
		}
		return false;
	}

	public static <C> Array<C> newArray(C... objects)
	{
		return newArray(Arrays.asList(objects));
	}
	
	public static <C> Array<C> newArray(List<C> list)
	{
		Array<C> a = new Array<C>(null,
		                          list.isEmpty()
		                          ? null
		                          : ElementType.getReturnType(list.get(0).getClass()));
		a.addAll(list);
		return a;
	}

}