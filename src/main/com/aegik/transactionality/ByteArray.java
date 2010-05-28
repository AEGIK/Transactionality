package com.aegik.transactionality;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/** @author Christoffer Lerno */
public class ByteArray implements Iterable<Byte>, NonPrimitive
{
	private final static byte[] EMPTY_ARRAY = new byte[0];

	private byte[] m_bytes;
	private final Root m_root;

	/**
	 * Create a new byte array with transactional support.
	 *
	 * @param root the root owning this array. I.e. that handles
	 * transactions for the array.
	 */
	public ByteArray(Root root)
	{
		this(root, EMPTY_ARRAY);
	}

	/**
	 * Create a new byte array without transactional support.
	 */
	public ByteArray()
	{
		this(EMPTY_ARRAY);
	}

	/**
	 * Create a new byte array without transactional support.
	 *
	 * @param bytes the initial array of bytes embedded in this arrya.
	 */
	public ByteArray(byte[] bytes)
	{
		m_bytes = bytes;
		m_root = null;
	}

	/**
	 * Create a new byte array with transactional support.
	 *
	 * @param root the root owning this array. I.e. that handles
	 * transactions for the array.
	 * @param bytes the initial array of bytes embedded in this arrya.
	 */
	public ByteArray(Root root, byte[] bytes)
	{
		m_bytes = bytes;
		m_root = root;
	}

	/**
	 * Set the value of a byte in this array.
	 *
	 * @param index the index to set a value.
	 * @param value the value to set.
	 * @throws ArrayIndexOutOfBoundsException if the index is outside the bounds of this array.
	 */
	public void set(int index, byte value)
	{
		addUndo(new SetUndo(m_bytes, index));
		m_bytes[index] = value;
	}

	private void addUndo(Undo undo)
	{
		if (m_root != null) m_root.addAction(undo);
	}

	/**
	 * Replaces the current array of bytes with a new array.
	 *
	 * @param bytes the bytes to replace the current array with.
	 */
	public void replace(byte[] bytes)
	{
		addUndo(new ReplaceUndo(this));
		m_bytes = bytes;
	}

	/**
	 * Return the byte value at the given index.
	 *
	 * @param index the index to retrieve the value for.
	 * @return the byte value at the index.
	 * @throws ArrayIndexOutOfBoundsException if the index is outside the bounds of this array.
	 */
	public byte get(int index)
	{
		return m_bytes[index];
	}

	/**
	 * Creates and returns a copy of the internal byte array.
	 *
	 * @return a copy of the internal byte array.
	 */
	public byte[] toByteArray()
	{
		byte[] bytes = new byte[m_bytes.length];
		System.arraycopy(m_bytes, 0, bytes, 0, m_bytes.length);
		return bytes;
	}

	/**
	 * Returns the length of this array.
	 *
	 * @return the lenght of this array.
	 */
	public int size()
	{
		return m_bytes.length;
	}

	public Root root()
	{
		return m_root;
	}
	
	/**
	 * Implements undo for a replace.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	private static class ReplaceUndo implements Undo
	{
		private final ByteArray m_bytes;
		private final byte[] m_old;

		private ReplaceUndo(ByteArray bytes)
		{
			m_bytes = bytes;
			m_old = m_bytes.m_bytes;
		}

		public void undo()
		{
			m_bytes.m_bytes = m_old;
		}
	}

	/**
	 * Implements undo for a set.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	private static class SetUndo implements Undo
	{
		private final byte[] m_array;
		private final int m_index;
		private final byte m_byte;

		private SetUndo(byte[] array, int index)
		{
			m_array = array;
			m_index = index;
			m_byte = array[index];
		}

		public void undo()
		{
			m_array[m_index] = m_byte;
		}
	}

	/**
	 * Returns a string represenation of this byte array as hex,
	 * <p>
	 * For example, the array {@code [0, 10, 20, -5] } would return the string
	 * "000A14FB"
	 *
	 * @return the hexadecimal representation of the arrya of bytes.
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (byte b : m_bytes)
		{
			builder.append(Integer.toHexString((b & 0xF0) >> 4).toUpperCase());
			builder.append(Integer.toHexString(b & 0x0F).toUpperCase());
		}
		return builder.toString();
	}


	/**
	 * Returns the value of a single bit.
	 *
	 * @param bitIndex the number of the bit, assuming a left to right orientation of
	 * bits.
	 * @return the value of the bit.
	 * @throws ArrayIndexOutOfBoundsException if the bit is outside the byte array.
	 */
	public boolean getBit(int bitIndex)
	{
		int byteIndex = bitIndex / 8;
		int bitValue = 1 << (bitIndex % 8);
		return (get(byteIndex) & bitValue) == bitValue;
	}

	/**
	 * Sets the value of a single bit.
	 *
	 * @param bitIndex the number of the bit, assuming a left to right orientation of
	 * bits.
	 * @param value the new state of the bit, true if set, false if clear.
	 * @throws ArrayIndexOutOfBoundsException if the bit is outside the byte array.
	 */
	public void setBit(int bitIndex, boolean value)
	{
		int byteIndex = bitIndex / 8;
		int bitValue = 1 << (bitIndex % 8);
		if (value)
		{
			set(byteIndex, (byte) (get(byteIndex) | bitValue));
		}
		else
		{
			set(byteIndex, (byte) (get(byteIndex) & ~bitValue));
		}
	}

	/**
	 * Flips the value of a single bit.
	 *
	 * @param bitIndex the number of the bit, assuming a left to right orientation of
	 * bits.
	 * @throws ArrayIndexOutOfBoundsException if the bit is outside the byte array.
	 */
	public void flipBit(int bitIndex)
	{
		int byteIndex = bitIndex / 8;
		int bitValue = 1 << (bitIndex % 8);
		set(byteIndex, (byte) (get(byteIndex) ^ bitValue));
	}

	/**
	 * Tests if another Bytes or byte[] equals this Dict.
	 *
	 * @return true if the other object is a Map and has the
	 * same key-value pairs as this Dict.
	 */
	@SuppressWarnings({"NonFinalFieldReferenceInEquals"})
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o == this) return true;
		if (o instanceof byte[]) return Arrays.equals((byte[]) o, m_bytes);
		return o instanceof ByteArray && o.equals(m_bytes);
	}

	/**
	 * Returns an iterator over the bytes in this array.
	 *
	 * @return an immutable iterator.
	 */
	public ListIterator<Byte> iterator()
	{
		return new ByteArrayIterator(this);
	}

	/**
	 * Returns a binary string representation of this array, with
	 * all set bits as ones and clear bits as zeroes.
	 * <p>
	 * For instance, [129, 1] would be written as "10000001 00000001".
	 *
	 * @return the binary string representation of this array.
	 */
	public String toBinaryString()
	{
		if (m_bytes.length == 0) return "";
		StringBuilder builder = new StringBuilder(size() * 8);
		for (byte b : m_bytes)
		{
			for (int i = 0; i < 8; i++)
			{
				builder.append((b & (1 << i)) == 0 ? '0' : '1');
			}
			builder.append(' ');
		}
		builder.setLength(builder.length() - 1);
		return builder.toString();
	}
	private final static class ByteArrayIterator implements ListIterator<Byte>
	{
		private final ByteArray m_array;
		private int m_index;

		private ByteArrayIterator(ByteArray array)
		{
			m_array = array;
			m_index = -1;
		}

		public void add(Byte o)
		{
			throw new UnsupportedOperationException();
		}

		public boolean hasNext()
		{
			return nextIndex() < m_array.size();
		}

		public boolean hasPrevious()
		{
			return m_index > -1;
		}

		public Byte next()
		{
			if (!hasNext()) throw new NoSuchElementException();
			return m_array.get(++m_index);
		}

		public int nextIndex()
		{
			return m_index + 1;
		}

		public Byte previous()
		{
			if (!hasPrevious()) throw new NoSuchElementException();
			return m_array.get(m_index--);
		}

		public int previousIndex()
		{
			return m_index;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		public void set(Byte o)
		{
			if (hasPrevious())
			{
				m_array.set(m_index, o);
			}
		}
	}

	public Object primitive()
	{
		return toByteArray();
	}
}
