package transactionality;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ByteArrayTest extends TestCase
{
	ByteArray m_bytes;

	@Override
	protected void setUp() throws Exception
	{
		Root root = new Root();
		m_bytes = new ByteArray(root, new byte[]{0, 10, 20, -5});
	}

	public void testSet() throws Exception
	{
		m_bytes.root().beginTransaction();
		m_bytes.set(2, (byte) 50);
		assertEquals(50, m_bytes.get(2));
		assertEquals(10, m_bytes.get(1));
		m_bytes.root().rollback();
		assertEquals(20, m_bytes.get(2));
	}

	public void testReplace() throws Exception
	{
		m_bytes.root().beginTransaction();
		m_bytes.replace(new byte[]{1, 2, 3});
		assertEquals(3, m_bytes.get(2));
		assertEquals(3, m_bytes.size());
		m_bytes.root().rollback();
		assertEquals(20, m_bytes.get(2));
		assertEquals(4, m_bytes.size());
	}

	public void testToString() throws Exception
	{
		assertEquals("000A14FB", m_bytes.toString());
	}

	public void testGetByPath() throws Exception
	{
		assertEquals((byte) 0, m_bytes.getByPath("0"));
		assertEquals(null, m_bytes.getByPath("0.1"));
		assertEquals(null, m_bytes.getByPath("4"));
	}

	public void testEquals() throws Exception
	{
		assertTrue(m_bytes.equals(m_bytes.toByteArray()));
		assertTrue(m_bytes.equals(m_bytes));
		assertTrue(m_bytes.equals(new ByteArray(m_bytes.toByteArray())));
		assertFalse(m_bytes.equals(new byte[0]));
		assertFalse(m_bytes.equals(new ByteArray()));
		assertFalse(m_bytes.equals(null));
	}
	public void testIterator() throws Exception
	{
		ListIterator<Byte> it = m_bytes.iterator();
		m_bytes.root().beginTransaction();
		assertFalse(it.hasPrevious());
		assertTrue(it.hasNext());
		assertEquals(0, it.nextIndex());
		assertEquals(-1, it.previousIndex());
		try
		{
			it.previous();
			fail();
		}
		catch (NoSuchElementException e)
		{
		}
		assertEquals(0, it.next().intValue());
		assertTrue(it.hasPrevious());
		assertEquals(0, it.previousIndex());
		assertEquals(1, it.nextIndex());
		assertEquals(10, it.next().intValue());
		assertEquals(20, it.next().intValue());
		assertEquals(-5, it.next().intValue());
		assertEquals(4, it.nextIndex());
		assertEquals(3, it.previousIndex());
		assertFalse(it.hasNext());
		try
		{
			it.next();
			fail();
		}
		catch (NoSuchElementException e)
		{
		}
		assertTrue(it.hasPrevious());
		assertEquals(-5, it.previous().intValue());
		assertEquals(20, it.previous().intValue());
		assertEquals(2, it.nextIndex());
		assertEquals(1, it.previousIndex());
		assertEquals(20, it.next().intValue());
		it.set((byte) 15);
		assertEquals(15, m_bytes.get(2));
		assertEquals(15, it.previous().intValue());
		m_bytes.root().rollback();
		assertEquals(20, m_bytes.get(2));
		try
		{
			it.remove();
			fail();
		}
		catch (UnsupportedOperationException e)
		{
		}
		try
		{
			it.add((byte) 1);
			fail();
		}
		catch (UnsupportedOperationException e)
		{
		}


	}

	public void testGetBit() throws Exception
	{
		assertEquals("", new ByteArray().toBinaryString());
		assertEquals("00000000 01010000 00101000 11011111", m_bytes.toBinaryString());
		assertEquals(false, m_bytes.getBit(0));
		assertEquals(false, m_bytes.getBit(7));
		assertEquals(false, m_bytes.getBit(8));
		assertEquals(true, m_bytes.getBit(9));
		m_bytes.setBit(1, true);
		assertEquals("01000000 01010000 00101000 11011111", m_bytes.toBinaryString());
		m_bytes.setBit(1, true);
		assertEquals("01000000 01010000 00101000 11011111", m_bytes.toBinaryString());
		m_bytes.flipBit(1);
		assertEquals("00000000 01010000 00101000 11011111", m_bytes.toBinaryString());
		m_bytes.flipBit(1);
		assertEquals("01000000 01010000 00101000 11011111", m_bytes.toBinaryString());
		m_bytes.setBit(1, false);
		assertEquals("00000000 01010000 00101000 11011111", m_bytes.toBinaryString());
	}
}