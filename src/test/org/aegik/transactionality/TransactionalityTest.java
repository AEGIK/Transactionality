package org.aegik.transactionality;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.util.*;

import xmlwise.XmlParseException;
import xmlwise.Plist;

public class TransactionalityTest extends TestCase
{
	Transactionality m_transactionality;

	enum Test
	{
		A,
		B
	}
	interface Test1 extends Transactional
	{
		Integer getA();
		String getB();
		Date getC();
		Boolean getD();
		Test getEnum();

		@Optional Test1 getE();
		Array<Boolean> getF();
		Array<Test1> getG();
		Array<Array<Boolean>> getH();
		void setA(Integer x);
		void setB(String x);
		void setC(Date d);
		void setD(Boolean bool);
		Test1 createE();
		boolean deleteE();
		Array<Test1> createG();
		Array<Array<Boolean>> createH();
		Dict<Integer> getI();
		Dict<Integer> createI();
		Dict<Test3> getJ();
	}

	interface Error1 extends Transactional
	{
		Collection getA();
	}

	interface Error2 extends Transactional
	{
		void setA(Test1 b);
	}

	interface Error4 extends Transactional
	{
		Test1 getA();
		void setA(Test1 b);
	}

	interface Error3 extends Transactional
	{
		Integer createA();
	}

	interface Test3 extends Transactional
	{
		Object getTest();
		void setTest(Object value);
		Array<Object> getObjects();
	}

	interface Test4 extends Transactional
	{
		ByteArray createBytes();
		ByteArray getBytes();
	}

	@SuppressWarnings({"unchecked"})
    public void testObject() throws Exception
	{
		Test3 t = Transactionality.createRoot(Test3.class);
		assertEquals(null, t.getTest());
		t.setTest("Bar");
		t.toPlist();
		assertEquals("Bar", t.getTest());
		t.getObjects().add(1);
		t.getObjects().add("Foo");
		assertEquals("{objects=[1, Foo], test=Bar}",
		             new TreeMap(Plist.fromXml(t.toPlist())).toString());
		Test3 t2 = Transactionality.fromXml(Test3.class, t.toPlist());
		assertEquals(t, t2);
	}
	public void testIllegalGet()
	{
		try
		{
			Transactionality.createRoot(Error1.class);
			fail();
		}
		catch (ValidationException e)
		{
			assertEquals("Unsupported return type: interface java.util.Collection", e.getMessage());
		}
	}

	public void testIllegalSet()
	{
		try
		{
			Transactionality.createRoot(Error2.class);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Setter setA without getter in interface org.aegik.transactionality.TransactionalityTest$Error2", e.getMessage());
		}
	}

	public void testIllegalSet2()
	{
		try
		{
			Transactionality.createRoot(Error4.class);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Non-primitive setter setA in interface org.aegik.transactionality.TransactionalityTest$Error4", e.getMessage());
		}
	}

	public void testIllegalNew()
	{
		try
		{
			Transactionality.createRoot(Error3.class);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(e.getMessage().startsWith("Primitive new"));
		}
	}

	interface Error5 extends Transactional
	{
		void ft();
	}

	public void testIllegalCommands()
	{
		try
		{
			Transactionality.createRoot(Error5.class);
			fail();
		}
		catch (ValidationException e)
		{
			assertEquals("interface org.aegik.transactionality.TransactionalityTest$Error5 has illegal methods.", e.getMessage());
		}
	}

	interface Error6 extends Transactional
	{
		Integer getA();
		void deleteA();
	}

	public void testIllegalDelete()
	{
		try
		{
			Transactionality.createRoot(Error6.class);
			fail();
		}
		catch (ValidationException e)
		{
			assertEquals("Delete without boolean return value: deleteA in interface org.aegik.transactionality.TransactionalityTest$Error6", e.getMessage());
		}
	}

	public void testTransactions2()
	{
		Test4 test = Transactionality.createRoot(Test4.class);
		test.createBytes();
		test.beginTransaction();
		test.getBytes().replace(new byte[1]);
		assertEquals(true, test.isInTransaction());
		test.commit();
		assertEquals(false, test.isInTransaction());
		assertEquals("00", test.getBytes().toString());
		test.beginTransaction();
		test.getBytes().setBit(2, true);
		assertEquals("04", test.getBytes().toString());
		assertEquals(true, test.isInTransaction());
		test.rollback();
		assertEquals("00", test.getBytes().toString());
		assertEquals("{bytes=00}", test.toString());
		assertEquals("[bytes]", test.keySet().toString());
		try
		{
			test.keySet().clear();
			fail();
		}
		catch (UnsupportedOperationException e)
		{
		}
	}
	public void testTransactions()
	{
		Test1 test = Transactionality.createRoot(Test1.class);
		test.root().beginTransaction();
		test.setA(4);
		assertEquals((Integer) 4, test.getA());
		test.root().rollback();
		assertEquals((Integer) 0, test.getA());
	}

	private interface ByteInterface extends Transactional
	{
		ByteArray getBytes();
		ByteArray createBytes();
	}

	public void testByteArray() throws Exception
	{
		ByteInterface b = Transactionality.createRoot(ByteInterface.class);
		assertEquals(null, b.getBytes());
		b.createBytes();
		b.getBytes().replace(new byte[] { 1, 2 });
		assertEquals(2, b.getBytes().get(1));
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		             "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
		             "<plist version=\"1.0\">" +
		             "<dict><key>bytes</key><data>AQI=</data></dict></plist>", b.toPlist());
		ByteInterface b2 = Transactionality.fromXml(ByteInterface.class, b.toPlist());
		assertTrue(Arrays.equals(new byte[] {1, 2}, b2.getBytes().toByteArray()));
		Test4 t = Transactionality.createRoot(Test4.class);
		t.createBytes();
		assertEquals("", t.getBytes().toBinaryString());
		t.getBytes().replace(new byte[] { 1, 2, 3});
		assertEquals(3, t.getBytes().get(2));
		assertEquals("10000000 01000000 11000000", t.getBytes().toBinaryString());
	}

	public void testLoadRoot() throws XmlParseException
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		             "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
		             "<plist version=\"1.0\"><dict>" +
		             "<key>a</key><integer>1</integer>" +
		             "<key>b</key><string>Foo</string>" +
		             "<key>c</key><date>1999-01-01T02:03:04Z</date>" +
		             "<key>d</key><true/>" +
		             "<key>enum</key><string>B</string>" +
		             "<key>f</key><array><true/><false/></array>" +
		             "<key>i</key><dict><key>test</key><integer>4</integer></dict>" +
		             "<key>j</key><dict><key>foo</key><dict><key>objects</key><array><string>bar</string></array></dict></dict>" +
		             "<key>h</key><array><array><true/></array></array></dict></plist>";
		Test1 test = Transactionality.fromXml(Test1.class, xml);
		assertEquals((Integer) 1, test.getA());
		assertEquals("Foo", test.getB());
		assertEquals(Test.B, test.getEnum());
		assertEquals(Boolean.TRUE, test.getD());
		assertEquals(Arrays.asList(true, false), test.getF());
		assertEquals((Integer) 4, test.getI().get("test"));
		assertEquals(Boolean.TRUE, test.getH().get(0).get(0));
		assertEquals("bar", test.getJ().get("foo").getObjects().get(0));
		String xmlCopy = test.toPlist();
		assertEquals(xml.length(), xmlCopy.length());
		Test1 loaded = Transactionality.fromXml(Test1.class, test.toPlist());
		assertEquals(loaded, test);
	}

	public void testCreateRoot() throws Exception
	{
		Test1 test = Transactionality.createRoot(Test1.class);
		assertEquals((Integer) 0, test.getA());
		assertEquals("", test.getB());
		assertEquals(0, test.getF().size());
		test.setB("Test");
		assertEquals("Test", test.getB());
		assertEquals(null, test.getE());
		test.createE();
		assertNotNull(test.getE());
		assertEquals(null, test.getG());
		test.createG();
		assertEquals(0, test.getG().size());
		test.getG().create();
		assertEquals(1, test.getG().size());
		assertEquals(null, test.getH());
		test.createH();
		test.getH().create().add(true);
		assertEquals((Boolean) true, test.getH().get(0).get(0));
		test.remove("h");
		assertEquals(Test.A, test.getEnum());
		assertNull(test.getH());
		try
		{
			test.remove("a");
			fail();
		}
		catch (UnsupportedOperationException e)
		{
			assertEquals("Mandatory entry 'a' cannot be deleted.", e.getMessage());
		}
		try
		{
			test.put("a", null);
			fail();
		}
		catch (UnsupportedOperationException e)
		{
			assertEquals("Mandatory entry 'a' cannot be deleted.", e.getMessage());
		}
		Dict<Integer> test2 = test.createI();
		assertEquals(null, test2.get("Test"));
		test2.put("Test", 1);
		assertEquals((Integer) 1, test2.get("Test"));

		Dict<Test3> test3 = test.getJ();
		Test3 element = test3.create("test");
		assertEquals(0, element.getObjects().size());
	}
	

}