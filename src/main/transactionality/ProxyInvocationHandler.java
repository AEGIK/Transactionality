package transactionality;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** @author Christoffer Lerno */
class ProxyInvocationHandler extends Dict<Object> implements InvocationHandler
{
	private final Klass m_klass;

	public ProxyInvocationHandler(Root root, Klass klass)
	{
		super(root, null);
		m_klass = klass;
		m_klass.init(root, this);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		return m_klass.invoke(this, method, args);
	}

}
