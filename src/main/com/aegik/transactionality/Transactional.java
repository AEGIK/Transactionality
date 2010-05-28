package com.aegik.transactionality;

import java.util.Set;
import java.util.Map;

/** @author Christoffer Lerno */
public interface Transactional extends NonPrimitive
{
	Object init(Map<String, Object> map) throws ValidationException;
	String toPlist();
	Set<String> keySet();
	Object get(String key);
	Object put(String key, Object o);
	Object remove(String key);
	Map<String, Object> primitive();
	void beginTransaction();
	void commit();
	void rollback();
	boolean isInTransaction();
}
