package com.aegik.transactionality;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Christoffer Lerno */
abstract class ElementType<C>
{
	private final static HashMap<Type, ElementType> PRIMITIVE_TYPES = new HashMap<Type, ElementType>();

	static
	{
		PRIMITIVE_TYPES.put(Integer.class, new PrimitiveElementType<Integer>(0));
		PRIMITIVE_TYPES.put(String.class, new PrimitiveElementType<String>(""));
		PRIMITIVE_TYPES.put(Double.class, new PrimitiveElementType<Double>(0.0));
		PRIMITIVE_TYPES.put(Boolean.class, new PrimitiveElementType<Boolean>(false));
		PRIMITIVE_TYPES.put(Date.class, new PrimitiveElementType<Date>(new Date(0)));
		PRIMITIVE_TYPES.put(ByteArray.class, new ElementType<ByteArray>(ByteArray.class)
		{
			public ByteArray newObject(Root root, Object data)
			{
				return data == null ? new ByteArray(root) : new ByteArray(root, (byte[]) data);
			}
		});
		PRIMITIVE_TYPES.put(Object.class, new ElementType<Object>(Object.class)
		{
			public Object newObject(Root root, Object object)
			{
				return object;
			}
		});
	}

	private final Type m_type;
	private ElementType(Type type)
	{
		m_type = type;
	}

	public abstract C newObject(Root root, Object object);

	public Type getType()
	{
		return m_type;
	}

	@SuppressWarnings({"unchecked"})
    public static <C> ElementType<C> getReturnType(Type returnType)
	{
		ElementType type = PRIMITIVE_TYPES.get(returnType);
		if (type != null) return type;
		if (returnType instanceof Class)
		{
			if (Enum.class.isAssignableFrom((Class) returnType))
			{
				return new EnumObjectType((Class) returnType);
			}
			if (Transactional.class.isAssignableFrom((Class) returnType))
			{
				return new ProxyElementType(returnType);
			}
		}
		else if (returnType instanceof ParameterizedType)
		{
			ParameterizedType parameterizedType = (ParameterizedType) returnType;
			if (parameterizedType.getRawType().equals(Dict.class))
			{
				return new DictElementType(returnType,
				                           getReturnType(parameterizedType.getActualTypeArguments()[0]));
			}
			else if (parameterizedType.getRawType().equals(Array.class))
			{
				return new ArrayElementType(returnType,
				                           getReturnType(parameterizedType.getActualTypeArguments()[0]));
			}
			throw new ValidationException("Unsupported generic class " + returnType);
		}
		throw new ValidationException("Unsupported return type: " + returnType);
	}

	private static class EnumObjectType<C extends Enum<C>> extends ElementType<C>
	{
		private final C[] m_enumValues;

		public EnumObjectType(Class<C> enumClass)
		{
			super(enumClass);
			m_enumValues = enumClass.getEnumConstants();
		}

		public C newObject(Root root, Object object)
		{
			if (object == null) return m_enumValues[0];
			for (C e : m_enumValues)
			{
				if (e.name().equals(object))
				{
					return e;
				}
			}
			throw new ValidationException("Illegal enum value: " + object);
		}
	}

	private static class PrimitiveElementType<C> extends ElementType<C>
	{
		private final C m_initialValue;

		public PrimitiveElementType(C initialValue)
		{
			super(initialValue.getClass());
			m_initialValue = initialValue;
		}

		@SuppressWarnings({"unchecked"})
        public C newObject(Root root, Object o)
		{
			if (o != null)
			{
				if (!o.getClass().equals(getType()))
				{
					throw new ValidationException("Expected class " + getType() + " was " + o.getClass());
				}
				return (C) o;
			}
			return m_initialValue;
		}

	}

	private static class ProxyElementType<T extends Transactional> extends ElementType<T>
	{
		public ProxyElementType(Type type)
		{
			super(type);
		}

		@SuppressWarnings({"unchecked"})
        public T newObject(Root root, Object o)
		{
			T t = Transactionality.createProxy(root, (Class<T>) getType());
			if (o != null)
			{
				t.init((Map<String, Object>) o);
			}
			return t;
		}
	}

	private static class ArrayElementType<C> extends ElementType<Array<C>>
	{
		private final ElementType<C> m_internalType;

		public ArrayElementType(Type returnType, ElementType<C> internalType)
		{
			super(returnType);
			m_internalType = internalType;
		}

		public Array<C> newObject(Root root, Object value)
		{
			Array<C> array = new Array<C>(root, m_internalType);
			if (value != null)
			{
				List list = (List) value;
				for (Object o : list)
				{
					array.add(m_internalType.newObject(root, o));
				}
			}
			return array;
		}
	}

	private static class DictElementType<C> extends ElementType<Dict<C>>
	{
		private final ElementType<C> m_internalType;

		public DictElementType(Type returnType, ElementType<C> internalType)
		{
			super(returnType);
			m_internalType = internalType;
		}

		@SuppressWarnings({"unchecked"})
        public Dict<C> newObject(Root root, Object value)
		{
			Dict<C> dict = new Dict<C>(root, m_internalType);
			if (value != null)
			{
				Map map = (Map) value;
				dict.init(map);
			}
			return dict;
		}
	}



}
