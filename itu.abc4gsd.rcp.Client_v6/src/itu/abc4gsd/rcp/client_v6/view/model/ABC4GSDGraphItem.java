

package itu.abc4gsd.rcp.client_v6.view.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.zest.core.widgets.GraphConnection;



public class ABC4GSDGraphItem implements IABC4GSDItem {
	public List<ABC4GSDGraphItem> in;
	public List<ABC4GSDGraphItem> out;
	public List<GraphConnection> connections;
	private String label;
	public List<String> tags;
	public ABC4GSDItem content;
	public boolean placeHolder;
	
	public ABC4GSDGraphItem( ABC4GSDItem content ) { 
		in = new ArrayList<ABC4GSDGraphItem>();
		out = new ArrayList<ABC4GSDGraphItem>();
		connections = new ArrayList<GraphConnection>();
		this.content = content;
		label = content != null ? content.get("name").toString() : "";
		tags = new ArrayList<String>();
		placeHolder = content == null;
	}
	
	public String toString() { return label; }
	public String getLabel() { return label; }
	public void setLabel( String label ) { this.label = label; }
	public String getInfo() {
		String ret = "additional info";
		return ret; 
	}


	public String getBaseQuery() { return content.getBaseQuery(); }
	public void setBaseQuery(String q) { content.setBaseQuery(q); }
	public long getId() { return content == null ? -1 : content.getId(); }
	public void setId(long id) { content.setId(id); }
	public void setId(String id) { content.setId(id); }
	public boolean hasKey(String key) { return content.hasKey(key); }
	public String[] getKeys() { return content.getKeys(); }
	public String[] getValues() { return content.getValues(); }
	public void setKeys(String[] keys) { content.setKeys(keys); }
	public Object get(String key) { return content.get(key); }
	public void set(String key, long value, boolean remote) { content.set(key, value, remote); }
	public void set(String key, String value, boolean remote) { content.set(key, value, remote); }
	public void set(String key, long value) { content.set(key, value ); }
	public void set(String key, String value) { content.set(key, value); }
	public boolean simpleType(String key) { return content.simpleType(key); }
	public String[] getType() { return content.getType(); }
	public String[] getType(String key) { return content.getType(key); }
	public void attach(String key, String val, boolean remote) { content.attach(key, val, remote); }
	public void attach(String key, Long val, boolean remote) { content.attach(key, val, remote); }
	public void attach(String key, String val) { content.attach(key, val); }
	public void attach(String key, Long val) { content.attach(key, val); }
	public void update(String[] toLoad) { content.update(toLoad); }
	public void update() { content.update(); }
}
