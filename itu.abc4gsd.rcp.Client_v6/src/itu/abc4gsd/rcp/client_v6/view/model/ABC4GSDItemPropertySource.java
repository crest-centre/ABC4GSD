package itu.abc4gsd.rcp.client_v6.view.model;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class ABC4GSDItemPropertySource implements IPropertySource {
//	private final ABC4GSDItem item;
//
//	public ABC4GSDItemPropertySource(ABC4GSDItem item) { this.item = item; }
//	public Object getEditableValue() { return this; }
//	public boolean isPropertySet(Object id) { return false; }
//	public void resetPropertyValue(Object id) {}
//
//	public void setPropertyValue(Object key, Object value) {
//		String k = (String) key;
//		String v = (String) value;
//		if( !(item.hasKey(k) || k == "id") )
//			return;
//		if (k == "id")
//			item.setId(v);
//		else
//			item.set(k.toLowerCase(), v);
//	}
//
//	public Object getPropertyValue(Object id) {
//		String k = (String) id;
//		if( k == "id" )
//			return item.getId();
//		if( item.hasKey(k) )
//			return item.get(k);
//		return null;
//	}
//
//	public IPropertyDescriptor[] getPropertyDescriptors() {
//		List<IPropertyDescriptor> wip = new ArrayList<IPropertyDescriptor>();
//		wip.add( new TextPropertyDescriptor("id","ID") );
//		wip.add( new TextPropertyDescriptor("baseQuery","Base Query") );
//		String[] keys = item.getKeys();
//		for( int i=0; i<keys.length; i++ )
//			wip.add( new TextPropertyDescriptor(keys[i],keys[i]) );
//		return wip.toArray( new IPropertyDescriptor[wip.size()] );
//	}
//}
