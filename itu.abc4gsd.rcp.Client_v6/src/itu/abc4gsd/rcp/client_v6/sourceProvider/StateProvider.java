package itu.abc4gsd.rcp.client_v6.sourceProvider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class StateProvider extends AbstractSourceProvider {
	public final static String MY_STATE = "itu.abc4gsd.rcp.client_v6.var_connected";
	public final static String CONNECTED = "CONNECTED";
	public final static String DISCONNECTED = "DISCONNECTED";

	private boolean connected = true;
	
	public final static String MY_SELECTION = "itu.abc4gsd.rcp.client_v6.sourceProvider.selection";
	public final static int SELECTION_MODEL = 0; 
	public final static int SELECTION_ENTITY = 1; 
	public final static int SELECTION_ENTITY_ID = 2; 
	public final static int SELECTION_FIELD = 3; 
	public final static int SELECTION_VALUE = 4; 
	private String[] selection = {"","","","",""};
		
	public StateProvider() {}
	public void dispose() {}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getCurrentState() { 
		Map map = new HashMap(2);
		String value = connected ? CONNECTED : DISCONNECTED;
		map.put(MY_STATE, value);
		map.put(MY_SELECTION, selection);
		return map;
	}
	
	public String[] getProvidedSourceNames() { return new String[] { MY_STATE, MY_SELECTION }; }
	
	public void setConnectionState( boolean state ) {
		if( connected == state ) return;
		connected = state;
		fireSourceChanged(ISources.WORKBENCH, MY_STATE, connected ? CONNECTED : DISCONNECTED);
	}
	public void setSelection( int field, String value ) {
		selection[field] = value;
		fireSourceChanged(ISources.WORKBENCH, MY_SELECTION, field);
	}
}
