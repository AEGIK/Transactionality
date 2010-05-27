package transactionality;

import xmlwise.Plist;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A prototype class handling proxy actions on a specific class.
 *
 * @author Christoffer Lerno */
public class Klass
{
	private final static Set<Class> PRIMITIVES = new HashSet<Class>(Arrays.asList(String.class,
	                                                                              Object.class,
	                                                                              Integer.class,
	                                                                              Double.class,
	                                                                              Boolean.class,
	                                                                              Date.class));
	private final static Map<Class, Klass> ACTIONS = new HashMap<Class, Klass>();
	private static final String ANY_KEY = "*";

	/**
	 * Returns a Klass instance for the given class, recycling
	 * an a previous instance if one has already been constructed for
	 * the class
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param c the class to create a Klass for.
	 * @return the Klass for the incoming class.
	 * @throws IllegalArgumentException if the class is not a suitable class.
	 */
	public static Klass getKlass(Class<? extends Transactional> c)
	{
		Klass template;
		synchronized (ACTIONS)
		{
			template = ACTIONS.get(c);
		}
		if (template == null)
		{
			template = new Klass(c);
			synchronized (ACTIONS)
			{
				ACTIONS.put(c, template);
			}
		}
		return template;
	}

	private final Map<String, KlassMethod> m_actions;
	private final Map<String, ElementType> m_objectTypes;
	private final TreeSet<String> m_optional;

	private Klass(Class<? extends Transactional> c)
	{
		m_actions = new HashMap<String, KlassMethod>();
		m_objectTypes = new HashMap<String, ElementType>();
		m_optional = new TreeSet<String>();
		initialize(c);
	}

	public void init(Root root, Map<String, Object> uninitializedMap)
	{
		HashSet<String> keys = new HashSet<String>(m_objectTypes.keySet());
		keys.removeAll(m_optional);
		keys.remove(ANY_KEY);
		for (String key : keys)
		{
			uninitializedMap.put(key, m_objectTypes.get(key).newObject(root, null));
		}
	}

	private void initialize(Class<? extends Transactional> c)
	{
		for (Method method : c.getMethods())
		{
			if (isAccessorMethodName(method, "get"))
			{
				addGetter(method);
			}
		}
		for (Method method : c.getMethods())
		{
			if (isAccessorMethodName(method, "set"))
			{
				createSetter(method);
			}
			if (isAccessorMethodName(method, "create"))
			{
				createCreate(method);
			}
			if (isAccessorMethodName(method, "delete"))
			{
				createDelete(method);
			}
		}
		if (m_actions.size() != c.getMethods().length - Transactional.class.getMethods().length)
		{
			throw new ValidationException(c.toString() + " has illegal methods." );
		}
		m_actions.put("init", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				initFromData(proxy, (Map<String, Object>) args[0]);
				return null;
			}
		});
		m_actions.put("equals", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.equals(args[0]);
			}
		});
		m_actions.put("put", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args)
			{
				if (args[1] == null && !m_optional.contains(args[0]))
				{
					throw new UnsupportedOperationException("Mandatory entry '" + args[0] + "' cannot be deleted.");
				}
				return proxy.put((String) args[0], args[1]);
			}
		});
		m_actions.put("remove", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args)
			{
				if (!m_optional.contains(args[0]))
				{
					throw new UnsupportedOperationException("Mandatory entry '" + args[0] + "' cannot be deleted.");
				}
				return proxy.put((String) args[0], null) != null;
			}
		});
		m_actions.put("toString", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.toString();
			}
		});
		m_actions.put("root", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.root();
			}
		});
		m_actions.put("keySet", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.keySet();
			}
		});
		m_actions.put("get", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.get((String) args[0]);
			}
		});
		m_actions.put("primitive", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.primitive();
			}
		});
		m_actions.put("toPlist", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return Plist.toXml(proxy.primitive());
			}
		});
		m_actions.put("commit", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				proxy.root().commit();
				return null;
			}
		});
		m_actions.put("rollback", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				proxy.root().rollback();
				return null;
			}
		});
		m_actions.put("isInTransaction", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.root().isInTransaction();
			}
		});

		m_actions.put("beginTransaction", new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				proxy.root().beginTransaction();
				return null;
			}
		});

	}

	private boolean isAccessorMethodName(Method method, String name)
	{
		return method.getName().startsWith(name) && !method.getName().equals(name);
	}

	private void initFromData(ProxyInvocationHandler proxy, Map<String, Object> map) throws ValidationException
	{
		Set<String> valuesSet = new HashSet<String>(m_objectTypes.keySet());
		for (Map.Entry<String, Object> entry : map.entrySet())
		{
			ElementType type = m_objectTypes.get(entry.getKey());
			if (type == null)
			{
				type = m_objectTypes.get(ANY_KEY);
			}
			valuesSet.remove(entry.getKey());
			if (type == null) throw new ValidationException("Unexpected value '" + entry.getKey() + "'");
			try
			{
				proxy.put(entry.getKey(), type.newObject(proxy.root(), entry.getValue()));
			}
			catch (Exception e)
			{
				throw new ValidationException("Invalid data for key " + entry.getKey(), e);
			}
		}
		valuesSet.removeAll(m_optional);
		valuesSet.remove(ANY_KEY);
		if (valuesSet.size() > 0) throw new ValidationException("Missing required value(s): " + valuesSet);
	}

	private void createDelete(Method method)
	{
		verifyParameters(method);
		String name = method.getName();
		final String key = keyFromMethod(name.substring(6));
		m_optional.add(key);
		ElementType objectType = m_objectTypes.get(key);
		if (objectType == null) throw new IllegalArgumentException(name + " without getter in "
		                                                           + method.getDeclaringClass());
		if (!method.getReturnType().equals(boolean.class))
			throw new ValidationException("Delete without boolean return value: " + name
			                                   + " in " + method.getDeclaringClass());
		m_actions.put(name, new KlassMethod()
		{
			public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			{
				return proxy.put(key, null) != null;
			}
		});
	}

	private void createCreate(Method method)
	{
		String name = method.getName();
		verifyParameters(method);
		if (isPrimitive(method.getReturnType())) throw new IllegalArgumentException("Primitive new " +
		                                                                            name + " in "
		                                                                            + method.getDeclaringClass());
		final String key = keyFromMethod(name.substring(6));
		final ElementType objectType = m_objectTypes.get(key);
		if (objectType == null) throw new IllegalArgumentException(name + " without getter in "
		                                                           + method.getDeclaringClass());
		if (!objectType.getType().equals(method.getGenericReturnType()))
			throw new IllegalArgumentException("Argument mismatch for " + name + " in "
			                                   + method.getDeclaringClass());
		m_optional.add(key);
		m_actions.put(name,
		              new KlassMethod()
		              {
			              public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			              {
				              Object o = objectType.newObject(proxy.root(), null);
				              proxy.put(key, o);
				              return o;
			              }
		              });
	}

	private void createSetter(Method method)
	{
		final String key = keyFromMethod(method.getName().substring(3));
		ElementType objectType = m_objectTypes.get(key);
		if (objectType == null)
			throw new IllegalArgumentException("Setter " + method.getName() + " without getter in "
			                                   + method.getDeclaringClass());
		verifyParameters(method, objectType.getType());
		if (!isPrimitive(method.getParameterTypes()[0]))
			throw new IllegalArgumentException("Non-primitive setter " + method.getName()
			                                   + " in " + method.getDeclaringClass());
		m_actions.put(method.getName(), new KlassMethod()
	                  {
		                  public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
		                  {
			                  return proxy.put(key, args[0]);
		                  }
	                  });
	}

	private void addGetter(Method method)
	{
		verifyParameters(method);
		final String key = keyFromMethod(method.getName().substring(3));
		ElementType type = ElementType.getReturnType(method.getGenericReturnType());
		m_objectTypes.put(key, type);
		if (method.getAnnotation(Optional.class) != null || type.getType().equals(Object.class))
		{
			m_optional.add(key);
		}
		m_actions.put(method.getName(), new KlassMethod()
		              {
			              public Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception
			              {
				              return proxy.get(key);
			              }
		              });
	}


	private void verifyParameters(Method method, Type... expectedParameters)
	{
		if (method.getParameterTypes().length != expectedParameters.length)
		{
			throw new IllegalArgumentException("Unexpected # of arguments to "
			                                   + method.getName() + " in " + method.getDeclaringClass());
		}
		for (int i = 0; i < expectedParameters.length; i++)
		{
			if (!expectedParameters[i].equals(method.getGenericParameterTypes()[i]))
			{
				throw new IllegalArgumentException("Mismatch on parameter " + (i + 1) + " for " + method.getName()
				                                   + " in " + method.getDeclaringClass());
			}
		}
	}

	public Object invoke(ProxyInvocationHandler proxy, Method method, Object[] args) throws Exception
	{
		return m_actions.get(method.getName()).invoke(proxy, args);
	}

	public static boolean isPrimitive(Class c)
	{
		return PRIMITIVES.contains(c) || Enum.class.isAssignableFrom(c);
	}

	public static String keyFromMethod(String name)
	{
		if (name.length() == 0) throw new IllegalArgumentException("Generic accessor");
		StringBuilder builder = new StringBuilder();
		for (char c : name.toCharArray())
		{
			if (Character.isUpperCase(c))
			{
				if (builder.length() > 0)
				{
					builder.append("-");
				}
				builder.append(Character.toLowerCase(c));
			}
			else
			{
				builder.append(c);
			}
		}
		return builder.toString();
	}

	public interface KlassMethod
	{
		Object invoke(ProxyInvocationHandler proxy, Object[] args) throws Exception;
	}

	public static Object getPrimitive(Object o)
	{
		if (o instanceof Enum)
		{
			return ((Enum) o).name();
		}
		else if (o instanceof NonPrimitive)
		{
			return ((NonPrimitive) o).primitive();
		}
		return o;
	}
}
