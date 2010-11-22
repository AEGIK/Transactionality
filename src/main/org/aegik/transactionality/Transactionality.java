package org.aegik.transactionality;

import xmlwise.XmlParseException;
import xmlwise.Plist;

import java.lang.reflect.Proxy;
import java.io.IOException;
import java.io.File;

/** @author Christoffer Lerno */
public class Transactionality
{
	public static <C extends Transactional> C createRoot(Class<C> c)
	{
		Root root = new Root();
		return createProxy(root, c);
	}

    @SuppressWarnings({"unchecked"})
    static <C extends Transactional> C createProxy(Root root, Class<C> c)
	{
		return (C) Proxy.newProxyInstance(root.getClass().getClassLoader(),
		                                  new Class[]{c},
		                                  new ProxyInvocationHandler(root, Klass.getKlass(c)));
	}

	public static <C extends Transactional> C fromXml(Class<C> inf, String xml) throws XmlParseException
	{
		C object = createRoot(inf);
		object.init(Plist.fromXml(xml));
		return object;
	}

	public static <C extends Transactional> C fromFile(Class<C> inf, String filename) throws XmlParseException, IOException
	{
		return fromFile(inf, new File(filename));
	}

	public static <C extends Transactional> C fromFile(Class<C> inf, File file) throws XmlParseException, IOException
	{
		C object = createRoot(inf);
		object.init(Plist.load(file));
		return object;
	}

}
