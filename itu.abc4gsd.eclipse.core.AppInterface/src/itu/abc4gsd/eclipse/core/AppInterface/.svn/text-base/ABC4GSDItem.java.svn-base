package itu.abc4gsd.eclipse.core.AppInterface;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class ABC4GSDItem implements IABC4GSDItem {
	private IAppInterface bridge;
	private String baseQuery;
	private long id;
	private HashMap<String, String> attributes;
	private HashMap<String, ArrayList<Long>> connections;
	private HashMap<String, ArrayList<String>> types;
	
	public ABC4GSDItem( IAppInterface bridge ) {
		this.bridge = bridge;
		baseQuery = null; 
		attributes = new HashMap<String, String>();
		connections = new HashMap<String, ArrayList<Long>>();
		types = new HashMap<String, ArrayList<String>>();
	}
	public ABC4GSDItem( IAppInterface bridge, String baseQuery ) {
		this.bridge = bridge;
		this.baseQuery = baseQuery;
		Long newId = Long.parseLong(bridge.query( baseQuery + ".+" )[0]);
		this.id = newId;
		attributes = new HashMap<String, String>();
		connections = new HashMap<String, ArrayList<Long>>();
		types = new HashMap<String, ArrayList<String>>();
	}
	public ABC4GSDItem( IAppInterface bridge, String baseQuery, String id ) { this(bridge, baseQuery, Long.parseLong(id)); }
	public ABC4GSDItem( IAppInterface bridge, String baseQuery, long id ) {
		this.bridge = bridge;
		this.baseQuery = baseQuery; this.id = id;
		attributes = new HashMap<String, String>();
		connections = new HashMap<String, ArrayList<Long>>();
		types = new HashMap<String, ArrayList<String>>();
	}
	public ABC4GSDItem( IAppInterface bridge, String baseQuery, String id, String[] fields ) { this(bridge, baseQuery, Long.parseLong(id), fields); }
	public ABC4GSDItem( IAppInterface bridge, String baseQuery, long id, String[] fields ) {
		this(bridge, baseQuery,id);
		update(fields);
	}

	public String getBaseQuery() { return baseQuery; }
	public void setBaseQuery(String q) { this.baseQuery = q; }
	public long getId() { return id; }
	public void setId( long id ) { this.id = id; }
	public void setId( String v ) { this.id = Long.parseLong(v); }
	
	public boolean hasKey( String key ) { return attributes.keySet().contains(key); }
	public String[] getKeys() { return attributes.keySet().toArray( new String[attributes.keySet().size()]); }
	public String[] getValues() { return attributes.values().toArray( new String[attributes.values().size()]); }
	public void setKeys(String[] keys) {
		for( int i = 0; i<keys.length; i++ )
			attributes.put(keys[i], null);
	}
	
	public Object get(String key) { 
		if( types.containsKey(key) && !simpleType(key) && connections.containsKey(key) )
			return connections.get(key);
//			if( connections.get(key).size() == 1 ) {
//				modified, in theory no one else uses it
//				System.out.println("USING SINGLE GET -> " + baseQuery + " -> " + key);
//				return connections.get(key).get(0).toString();
//			}else
//				return connections.get(key);
		else
			if( attributes.containsKey(key) )
				return attributes.get(key);
			else
				return "";
	}

	public void set(String key, long value, boolean remote ) { set( key, ""+value, remote ); }
	public void set(String key, String value, boolean remote) {
		if( attributes.containsKey( key ) && get( key ).equals(value) )
			return;
		attributes.put(key, value);
		if( remote )
			bridge.query( baseQuery + "." + id + "." + key + ".=.{{" + value + "}}" );
	}
	public void set(String key, long value) { set( key, ""+value ); }
	public void set(String key, String value) { set( key, value, true ); } 

	public boolean simpleType( String key ) {
		if( types.containsKey(key) )
			return types.get(key).size() == 0;
		return false;
	}
	public String[] getType() { return types.keySet().toArray( new String[ types.keySet().size() ]); }
	public String[] getType( String key ) {
		if(types.containsKey(key))
			return types.get(key).toArray( new String[ types.get(key).size() ] );
		return new String[]{};
	}
	
	public void attach( String key, String val, boolean remote ) { attach( key, Long.parseLong(val), remote ); }
	public void attach( String key, Long val, boolean remote ) {
		if( ! connections.containsKey(key) )
			connections.put(key, new ArrayList<Long>() );
		if( ! connections.get(key).contains( val ) ) {
			connections.get(key).add(val);
			if( remote )
				bridge.query( baseQuery + "." + id + "." + key + ".+." + val.toString() );
		}
	}
	public void attach( String key, String val ) { attach( key, Long.parseLong(val) ); }
	public void attach( String key, Long val ) { attach( key, val, true ); }
	
	public void update( String[] toLoad ) {
		String[] wipwip;
		ArrayList<String> tmp;
		for( int x=0; x<toLoad.length; x++ ) {
			wipwip = bridge.query( baseQuery + "." + id + "." + toLoad[x] + ".?" );
			tmp = new ArrayList<String>(Arrays.asList(wipwip)); 
			types.put(toLoad[x], tmp);
			wipwip = bridge.query( baseQuery + "." + id + "." + toLoad[x] );
			if( tmp.size() == 0 ) {
				String val = "";
				if( wipwip.length>0 )
					val = wipwip[0];
				set( toLoad[x], val, false );
			} else {
				for( int y=0; y<wipwip.length; y++ ) {
					Long val = null;
					try {
						val = Long.parseLong( wipwip[y] );
						attach( toLoad[x], val, false );
					} catch (Exception e) {} 
				}
			}
		}
	}
	
	public void update() {
		String[] tmp = bridge.query( baseQuery + "." + id );
		update(tmp);
	}
}
