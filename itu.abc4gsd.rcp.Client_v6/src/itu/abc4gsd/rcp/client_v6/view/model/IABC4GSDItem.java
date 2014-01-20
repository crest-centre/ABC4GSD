package itu.abc4gsd.rcp.client_v6.view.model;




public interface IABC4GSDItem extends IABC4GSDUi {	
	String getBaseQuery();
	void setBaseQuery( String q );
	long getId();
	void setId( long id );
	void setId( String id );
	
	boolean hasKey( String key );
	String[] getKeys();
	String[] getValues();
	void setKeys( String[] keys );
	
	Object get(String key);
	void set(String key, long value, boolean remote );
	void set(String key, String value, boolean remote );
	void set(String key, long value );
	void set(String key, String value);
	boolean simpleType( String key );
	String[] getType();
	String[] getType( String key );
	void attach( String key, String val, boolean remote  );
	void attach( String key, Long val, boolean remote  );
	void attach( String key, String val );
	void attach( String key, Long val );
	void update( String[] toLoad );
	void update();
	
//	String getRefreshAllQuery();
//	String getRefreshKeyQuery( String key );
}

