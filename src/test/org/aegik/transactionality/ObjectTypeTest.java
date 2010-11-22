package org.aegik.transactionality;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.util.Date;

import org.aegik.transactionality.Transactional;
import org.aegik.transactionality.Dict;
import org.aegik.transactionality.Array;
import org.aegik.transactionality.ElementType;

public class ObjectTypeTest extends TestCase
{
	ElementType m_objectType;

	interface Test extends Transactional
	{
		Integer intMethod();
		Double doubleMethod();
		Boolean boolMethod();
		String stringMethod();
		Date dateMethod();
		Test testMethod();
		Dict<Test> testDictMethod();
		Array<Integer> intListMethod();
	}
	@SuppressWarnings({"RedundantCast"})
    public void testGetReturnType() throws Exception
	{
		assertEquals((Integer) 0, ElementType.getReturnType(Test.class.getMethod("intMethod").getGenericReturnType()).newObject(null, null));
		assertEquals((Double) 0.0, ElementType.getReturnType(Test.class.getMethod("doubleMethod").getGenericReturnType()).newObject(null, null));
		assertEquals((Boolean) false, ElementType.getReturnType(Test.class.getMethod("boolMethod").getGenericReturnType()).newObject(null, null));
		assertEquals("", ElementType.getReturnType(Test.class.getMethod("stringMethod").getGenericReturnType()).newObject(null, null));
		assertEquals(new Date(0), ElementType.getReturnType(Test.class.getMethod("dateMethod").getGenericReturnType()).newObject(null, null));
		assertEquals("class org.aegik.transactionality.ElementType$ProxyElementType",
		             ElementType.getReturnType(Test.class.getMethod("testMethod").getGenericReturnType()).getClass().toString());
		assertEquals("class org.aegik.transactionality.ElementType$DictElementType",
		             ElementType.getReturnType(Test.class.getMethod("testDictMethod").getGenericReturnType()).getClass().toString());
		assertEquals("class org.aegik.transactionality.ElementType$ArrayElementType",
		             ElementType.getReturnType(Test.class.getMethod("intListMethod").getGenericReturnType()).getClass().toString());

	}
}