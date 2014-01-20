package itu.abc4gsd.rcp.client_v6.view.model;


 import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

public class ABC4GSDItemManager implements IResourceChangeListener {
	private List<IABC4GSDItem> items = null;
	private List<ABC4GSDItemManagerListener> listeners = new ArrayList<ABC4GSDItemManagerListener>();

	private static HashMap<String, ABC4GSDItemManager> manager = new HashMap<String, ABC4GSDItemManager>();
	
	private ABC4GSDItemManager() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener( this, IResourceChangeEvent.POST_CHANGE );
	}
	
	// Singleton
	public static ABC4GSDItemManager getManager( String key ) {
		if (!manager.containsKey(key))
			manager.put(key, new ABC4GSDItemManager());
		return manager.get(key);
	}
	public static void shutdown( String key ) {
		if (manager.containsKey(key)) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(manager.get(key));
			manager.get(key).saveItems();
			manager.remove(key);
		}
	}
	public static void shutdownAll() {
		ArrayList<String> wip = new ArrayList<String>( manager.keySet() );
		for( String key : wip ) { 
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(manager.get(key));
			manager.get(key).saveItems();
			manager.remove(key);
		}
	}

	private long[] getIDs() {
		long[] wip = new long[items.size()];
		for( int i=0; i<items.size(); i++)
			wip[i] = items.get(i).getId();
		return wip;
	}
	
	public int size() {
		return items.size();
	}
	
	public IABC4GSDItem getAt( int idx ) {
		if( idx < items.size() )
			return items.get(idx);
		return null;
	}

	public IABC4GSDItem get( long id ) {
		for( IABC4GSDItem wip : items )
			if( wip.getId() == id )
				return wip;
		return null;
	}

	public int getIdx( IABC4GSDItem obj ) {
		for( int i=0; i<items.size(); i++ )
			if( items.get(i).getId() == obj.getId() )
				return i;
		return -1;
	}
	
	public boolean contains( IABC4GSDItem obj ) {
		long[] wip = getIDs();
		for( int x=0; x<wip.length; x++ )
			if( wip[x] == obj.getId() )
				return true;
		return false;
	}
	
	public IABC4GSDItem[] getItems() {
		if (items == null)
			initItems();
		if ( items.size() > 0 && items.get(0) instanceof ABC4GSDGraphItem ) {
			List<ABC4GSDGraphItem> tmp = ((ABC4GSDGraphItem) items.get(0)).out;
			return tmp.toArray( new IABC4GSDItem[ tmp.size() ]);
		} else if ( items.size() > 0 && items.get(0) instanceof ABC4GSDTreeItem ) {
			List<ABC4GSDTreeItem> tmp = ((ABC4GSDTreeItem) items.get(0)).children;
			return tmp.toArray( new IABC4GSDItem[ tmp.size() ]);
		} else
			return items.toArray( new IABC4GSDItem[items.size()] );
	}

	public void incomingMessage( RemoteMessage cmd ) {
		if( cmd == null )
			return;
		if( items == null )
			initItems();
		IABC4GSDItem wip = get(cmd.id); 
		if( wip == null )
			wip = new ABC4GSDItem( "", cmd.id );
		wip.set(cmd.key, cmd.value);
		fireItemsChanged(cmd);
	}
	
	public void addItem( IABC4GSDItem obj ) {
		if (obj == null)
			return;
//		System.out.println(obj.get("name"));
		if (items == null)
			initItems();
		if( obj instanceof ABC4GSDTreeItem ) {
			items.add(obj);
		} else {
			items.add(obj);
		}
	}

	public void removeItem(IABC4GSDItem obj) {
		if (obj == null) return;
		if (items == null) return;
		int idx = getIdx(obj);
		if( idx == -1 ) return;
		items.remove(idx);
	}

//	public void modifyItem(IABC4GSDItem obj) {
//		if (obj == null)
//			return;
//		if (items == null)
//			loadItems();
//		int idx = getIdx(obj);
//		if( idx == -1 )
//			return;
//		items.remove(idx);
//		fireItemsChanged(ABC4GSDItem.NONE, obj, ABC4GSDItem.NONE );
//	}

	public void addABC4GSDItemManagerListener( ABC4GSDItemManagerListener listener ) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeABC4GSDItemManagerListener( ABC4GSDItemManagerListener listener) {
		listeners.remove(listener);
	}

	private void fireItemsChanged( RemoteMessage cmd ) {
		ABC4GSDItemManagerEvent event = new ABC4GSDItemManagerEvent(this, cmd );
		for( Iterator<ABC4GSDItemManagerListener> iter = listeners.iterator(); iter.hasNext(); )
			iter.next().itemsChanged(event);
	}



	public void initItems() {
		items = new ArrayList<IABC4GSDItem>();
	}
	public void initItems( boolean tree ) {
		initItems();
	}

	public void saveItems() {
		if (items == null)
	         return;
	}


	
	public void resourceChanged(IResourceChangeEvent event) {
		System.out.println( "ItemsManager - resource change event" );
//	      try {
//	         event.getDelta().accept(new IResourceDeltaVisitor() {
//	            public boolean visit(IResourceDelta delta)
//	               throws CoreException
//	            {
//	               StringBuffer buf = new StringBuffer(80);
//	               switch (delta.getKind()) {
//	                  case IResourceDelta.ADDED:
//	                     buf.append("ADDED");
//	                     break;
//	                  case IResourceDelta.REMOVED:
//	                     buf.append("REMOVED");
//	                     break;
//	                  case IResourceDelta.CHANGED:
//	                     buf.append("CHANGED");
//	                     break;
//	                  default:
//	                     buf.append("[");
//	                     buf.append(delta.getKind());
//	                     buf.append("]");
//	                     break;
//	               }
//	               buf.append(" ");
//	               buf.append(delta.getResource());
//	               System.out.println(buf);
//	               return true;
//	            }
//	         });
//	      }
//	      catch (CoreException ex) {
//	         FavoritesLog.logError(ex);
//	      }
	}
}

