package itu.abc4gsd.rcp.client_v6.view.model;

 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ABC4GSDItem implements IABC4GSDItem {
	private String baseQuery;
	private long id;
	private HashMap<String, String> attributes;
	private HashMap<String, ArrayList<Long>> connections;
	private HashMap<String, ArrayList<String>> types;
	
	public ABC4GSDItem() { 
		baseQuery = null; 
		attributes = new HashMap<String, String>();
		connections = new HashMap<String, ArrayList<Long>>();
		types = new HashMap<String, ArrayList<String>>();
	}
	public ABC4GSDItem( String baseQuery ) {
		this.baseQuery = baseQuery;
		Long newId = (Long)((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run( baseQuery + ".+" ).get(Constants.MSG_A)).get(2)).get(0);
		this.id = newId;
		attributes = new HashMap<String, String>();
		connections = new HashMap<String, ArrayList<Long>>();
		types = new HashMap<String, ArrayList<String>>();
	}
	public ABC4GSDItem( String baseQuery, String id ) { this(baseQuery, Long.parseLong(id)); }
	public ABC4GSDItem( String baseQuery, long id ) {
		this.baseQuery = baseQuery; this.id = id;
		attributes = new HashMap<String, String>();
		connections = new HashMap<String, ArrayList<Long>>();
		types = new HashMap<String, ArrayList<String>>();
	}
	public ABC4GSDItem( String baseQuery, String id, String[] fields ) { this(baseQuery, id.charAt(0) == '[' ? Long.parseLong(id.substring( 1, id.length()-1 )) : Long.parseLong(id), fields); }
	public ABC4GSDItem( String baseQuery, long id, String[] fields ) {
		this(baseQuery,id);
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
		else if( attributes.containsKey(key) )
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
			MasterClientWrapper.getInstance().run( baseQuery + "." + id + "." + key + ".=.{{" + value + "}}" );
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
				MasterClientWrapper.getInstance().run( baseQuery + "." + id + "." + key + ".+." + val.toString() );
		}
	}
	public void attach( String key, String val ) { attach( key, Long.parseLong(val) ); }
	public void attach( String key, Long val ) { attach( key, val, true ); }
	
	public void update( String[] toLoad ) {
		JSONArray wipwip;
		ArrayList<String> tmp;
		for( int x=0; x<toLoad.length; x++ ) {

			// merge when you are sure
			JSONObject ttt = MasterClientWrapper.getInstance().run( baseQuery + "." + id + "." + toLoad[x].toString() + ".?" );
			JSONArray ttt2 = ((JSONArray)ttt.get(Constants.MSG_A));
			wipwip = (JSONArray)ttt2.get(2);

			tmp = new ArrayList<String>(); 
			for( int y=0; y<wipwip.size(); y++ )
				tmp.add( wipwip.get(y).toString() );
			types.put(toLoad[x].toString(), tmp);
			wipwip = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run( baseQuery + "." + id + "." + toLoad[x].toString() ).get(Constants.MSG_A)).get(2));
			if( tmp.size() == 0 ) {
				String val = "";
				if( wipwip.size()>0 )
					val = wipwip.get(0).toString();
				set( toLoad[x].toString(), val, false );
			} else {
				for( int y=0; y<wipwip.size(); y++ ) {
					Long val = null;
					try {
						val = Long.parseLong( wipwip.get(y).toString() );
						attach( toLoad[x].toString(), val, false );
					} catch (Exception e) {} 
				}
			}
		}
	}
	
	public void update() {
		JSONArray wip = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run( baseQuery + "." + id ).get(Constants.MSG_A)).get(2));
		String[] tmp = new String[wip.size()];
		for( int x=0; x<wip.size(); x++ )
			tmp[x] = wip.get(x).toString();
		update(tmp);
	}
}
