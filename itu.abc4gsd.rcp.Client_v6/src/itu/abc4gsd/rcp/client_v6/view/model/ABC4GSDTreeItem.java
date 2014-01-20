package itu.abc4gsd.rcp.client_v6.view.model;

import java.util.ArrayList;
import java.util.List;



public class ABC4GSDTreeItem implements IABC4GSDItem {
	public ABC4GSDTreeItem parent;
	public List<ABC4GSDTreeItem> children;
	public String label;
	public String[] additionalInfo;
	public ABC4GSDItem content;
	
	public ABC4GSDTreeItem( ABC4GSDTreeItem parent, ABC4GSDItem content ) { 
		this.parent = parent;
		this.content = content;
		if( content != null ) {
			label = content.get("name").toString();
			additionalInfo = new String[]{ ""+content.getId() };
		} else {
			label = "";
			additionalInfo = new String[]{};
		}
		children = new ArrayList<ABC4GSDTreeItem>();
	}
	
	public boolean isCategory() { return parent.label == null; }
	public boolean isLeaf() { return children == null || children.size() == 0; }
	public String toString() { return label; }
	public String getInfo() {
		String ret = "";
		for( String wip : additionalInfo ) {
			if(ret.length()!=0) ret += ", ";
			ret += wip;
		}
		return ret; 
	}
	
	public void add( ABC4GSDTreeItem obj ) {
		this.children.add( obj );
//		sortActivities();
	}
	
//	public void sortActivities() {
//		System.out.println(this.label);
//		System.out.println("Children = " + this.children);
//		ABC4GSDTreeItem root = this;
//		for( int x=0; x<root.children.size(); x++ )
//			for( int y=root.children.size()-1; y>=0; y-- ) {
//				if( x == y ) continue;
//				ABC4GSDTreeItem child = isPresent( root.children.get(x), root.children.get(y).label );
//				if( child != null ) {
//					ABC4GSDTreeItem wip = root.children.get(y);
//					ABC4GSDTreeItem parent = child.parent; 
//					root.children.remove(y);
//					parent.children.remove(child);
//					wip.parent = parent;
//					parent.children.add(wip);
//				}
//			}
//	}
	
	public static ABC4GSDTreeItem isPresent( ABC4GSDTreeItem root, String el ) {
		if( el.equals(root.additionalInfo[0]) ) return root;
		for( ABC4GSDTreeItem wip : root.children ) {
			ABC4GSDTreeItem tmp = isPresent(wip, el);
			if( tmp != null )
				return tmp;
		}
		return null;
	}

	
	
	
	
	

	public String getBaseQuery() { return content.getBaseQuery(); }
	public void setBaseQuery(String q) { content.setBaseQuery(q); }
	public long getId() { return content.getId(); }
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
