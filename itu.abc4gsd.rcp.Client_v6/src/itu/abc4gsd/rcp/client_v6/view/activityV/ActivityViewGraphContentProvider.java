package itu.abc4gsd.rcp.client_v6.view.activityV;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;
import itu.abc4gsd.rcp.client_v6.draw2d.NodeFigure;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDGraphConnection;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDGraphItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerEvent;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerListener;
import itu.abc4gsd.rcp.client_v6.logic.Constants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphEntityRelationshipContentProvider;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;


class ActivityViewGraphContentProvider extends AppInterface implements ABC4GSDItemManagerListener, IGraphEntityRelationshipContentProvider {
	/* 
	 * AppInterface
	 *  - classes responding to events arriving from the middleware 
	 */
	private class CmdUpdate implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdate");
			log(data);
			initData( a_id );
			itemsChanged(null);
		}
	}
	private class CmdRelationUpdate implements ICommand {
		public void execute(String data) {
			log("Executing CmdRelationUpdate");
			initData( a_id );
			itemsChanged(null);
		}
	}
	private class CmdUserStatusChanged implements ICommand {
		public void execute(String data) {
			log("Executing CmdUserStatusChanged");
			log(data);
			initData( a_id );
			itemsChanged(null);
		}
	}

	private class CmdNewActivityCreated implements ICommand {
		public void execute(String data) {
			// "abc.activity.[].creator.+." + u_id;
			log("Executing CmdNewActivityCreated");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1 || wip.id == lastIdReceived ) 
				return;
			// In case something needs to be done with the activity
			// final IABC4GSDItem act = new ABC4GSDItem("abc.activity", wip.id, new String[]{"name","description"});
			initData( a_id );
			itemsChanged(null);
			lastIdReceived = wip.id;
		}
	}

	private class CmdNewActivityDetected implements ICommand {
		public void execute(String data) {
			// "abc.state.[].user.+." + u_id;
			log("Executing CmdNewActivityDetected");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1 ) 
				return;
			String q = "abc.state." + wip.id + ".activity";
			long actId = Long.parseLong(query(q)[0]);
			if( actId == lastIdReceived) 
				return;
			// In case something needs to be done with the activity
			// final IABC4GSDItem act = new ABC4GSDItem("abc.activity", wip.id, new String[]{"name","description"});
			initData( a_id );
			itemsChanged(null);
			lastIdReceived = actId;
		}
	}

	private class CmdActivityChanged implements ICommand {
		public void execute(String data) {
			log("Executing CmdActivityChanged");
			RemoteMessage wip = new RemoteMessage(data);
			final long currentActivity = Long.parseLong(wip.value);
			if( a_id == currentActivity ) return;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setSelection(currentActivity);
				}
			});
			a_id = currentActivity; 
//			itemsChanged(null);
		}
	}

	/* 
	 * AppInterface
	 */
	private String u_name;
	private long u_id;
	private long a_id;
	private List<String> subscriptionPerm = new ArrayList<String>();
	private List<String> subscriptionTemp = new ArrayList<String>();
	private Graph graph;
	private long lastIdReceived;
	
	
	public ActivityViewGraphContentProvider(String name, Graph graph) {
		super(name);
		this.graph = graph;
		_init();
	}
	
	private void _init() {
		String q = "";
		log("--> Initializing data <--");
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		u_name = prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, ""));
		q = "abc.user.[].name.==." + u_name;
		String[] wip = query(q);
		if( wip == null || wip.length == 0 ) return;
		u_id = Long.parseLong( wip[0] );
		
		initAllData();
	}
	
	public void initAllData() {
		log("--> Initializing ... <--");
		String q; 
		ABC4GSDItemManager.getManager("Activity").initItems(true);
		itemsChanged(null);
		q = "abc.user." + u_id + ".activity.=.";		// Activity Changed
		subscriptionPerm.add(q);
		subscribe( q, new CmdActivityChanged() );
	}
	
	public void initData(long id) {
		log("--> Initializing from " + id + " <--");
		String q;
		String[] qq;
		unsubscribe();
		subscriptionPerm.clear();
		subscriptionTemp.clear();
		ABC4GSDItemManager.getManager("Activity").initItems(true);
		refreshActivities(id);
		String[] activities = getActivitiesIds();

		q = "abc.state.[].user.+." + u_id;				// Added to new activity
		subscriptionPerm.add( q );
		subscribe( q, new CmdNewActivityDetected() );

		q = "abc.activity.[].creator.+." + u_id;		// You created a new activity
		subscriptionPerm.add( q );
		subscribe( q, new CmdNewActivityCreated() );
		

		q = "abc.relation.-.";						// Generic activity relation removed
		subscriptionPerm.add(q);					// When the entity is removed it is tricky
		subscribe( q, new CmdRelationUpdate() );

		q = "abc.relation.+";						// Generic Activity relation created
		subscriptionPerm.add(q);
		subscribe( q, new CmdRelationUpdate() );

		q = "abc.user." + u_id + ".activity.=.";		// Activity Changed
		subscriptionPerm.add(q);
		subscribe( q, new CmdActivityChanged() );

		for( String act : activities ) {
			q = "abc.activity." + act + ".name";							// Activity name changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );
	
			q = "abc.activity." + act + ".description";						// Activity description changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );

			q = "abc.activity.-." + act;									// Activity removed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );

			q = "abc.activity." + act + ".active.+";					// User joined online
			subscriptionTemp.add(q);
			subscribe( q, new CmdUserStatusChanged() );			

			q = "abc.activity." + act + ".active.-";					// User left
			subscriptionTemp.add(q);
			subscribe( q, new CmdUserStatusChanged() );
		}
	}
	
	public void refreshActivities( long id ) {
		ABC4GSDGraphItem tmp = new ABC4GSDGraphItem(null);
		fetchActivities( tmp, ""+id );
		ABC4GSDItemManager.getManager("Activity").addItem(tmp);
	}
	
	public void killOperation() {}
	public boolean personalHandler(String ch, String msg) { return true; }
	public void resumeOperation() { setBlockReactions(false); }
	public void suspendOperation() { setBlockReactions(true); initAllData(); }

	
	protected void _suspend( long activity ) { sendCommand( "SUSPEND " + activity ); }
	protected void _resume( long activity ) {
		a_id = activity;
		sendCommand( "RESUME " + activity );
	}

	/*
	 * Manager
	 */
	private GraphViewer viewer;
	private ABC4GSDItemManager manager;

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (GraphViewer) viewer;
      if (manager != null)
         manager.removeABC4GSDItemManagerListener(this);
      manager = (ABC4GSDItemManager) newInput;
      if (manager != null)
         manager.addABC4GSDItemManagerListener(this);
   }

   public void dispose() {}

   public void itemsChanged(final ABC4GSDItemManagerEvent event) {
	   // If this is the UI thread, then make the change.
	   if (Display.getCurrent() != null) {
		   updateViewer(event);
		   return;
	   }
	   
	   // otherwise, redirect to execute on the UI thread.
	   Display.getDefault().asyncExec(new Runnable() {
		   public void run() {
			   updateViewer(event);
		   }
	   });
   }
   
   private void updateViewer(ABC4GSDItemManagerEvent event) {
	   // To avoid closing errors
	   if( ! (Boolean)OSGIEventHandler.getInstance().get("stateConnected?") ) return;

	   System.out.println("Updating view");
	   viewer.refresh();
	   
	   if (viewer != null && !viewer.getControl().isDisposed())
		   viewer.applyLayout();
   }

	@Override
	public Object[] getElements(Object inputElement) {
	   if( inputElement instanceof ABC4GSDGraphItem )
		   return ((ABC4GSDGraphItem) inputElement).out.toArray();
	   else
		   return manager.getItems();
	}

	@Override
	public Object[] getRelationships(Object source, Object dest) {
		if( !((ABC4GSDGraphItem)source).out.contains(dest) ) return null;
		List<GraphNode> nodes = graph.getNodes();
		GraphNode from = null;
		GraphNode to = null;
		for( GraphNode n : nodes ) {
			if( ((ABC4GSDGraphItem)n.getData()).getId() == ((ABC4GSDGraphItem)source).getId() )
				from = n;
			if( ((ABC4GSDGraphItem)n.getData()).getId() == ((ABC4GSDGraphItem)dest).getId() )
				to = n;
		}
		GraphConnection wip = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, from, to );
		wip.setData(new ABC4GSDGraphConnection((ABC4GSDGraphItem)source, (ABC4GSDGraphItem)dest));
		wip.setTooltip( new Label( ((ABC4GSDGraphConnection)wip.getData()).label) );
		return new Object[]{};
	}


	private void fetchActivities( ABC4GSDGraphItem root, String start ) {
		// Element.info contains the id
		// Element.label contains the name
		final String tCREATOR = "creator";
		final String tUSER_Active = "active";
		final String tUSER_Dormant = "dormant";
		final String tUSER_Passive = "passive";
		
		String q;
		List<ABC4GSDGraphItem> res = new ArrayList<ABC4GSDGraphItem>();
		List<ABC4GSDGraphItem> action = new ArrayList<ABC4GSDGraphItem>();
		List<ABC4GSDGraphItem> operation = new ArrayList<ABC4GSDGraphItem>();
		ABC4GSDGraphItem tmpGraphItem;
		ABC4GSDItem tmpItem;
		String[] toCheckA;
		String me = "" + MasterClientWrapper.getInstance().getMyId();
		
		tmpItem = new ABC4GSDItem("abc.activity", start, new String[]{ "name", "description" });
		tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
		tmpGraphItem.tags.add( tUSER_Active );
		res.add(tmpGraphItem);

		ABC4GSDGraphItem obj = res.get(0);
		// Check if it is not empty thus all of them
		q = "abc.relation.[].from.==." + obj.getId();
		String[] wip1 = MasterClientWrapper.getInstance().query(q);
		
		if( wip1.length != 0 ) {
			q = "abc.relation." + wip1[0] + ".from.==.";
			String[] tmp = MasterClientWrapper.getInstance().query(q);
			if( tmp.length != 0 && !tmp[0].equals(""+obj.getId()) ) return;
			
			//Actions
			q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + obj.getId() + "].to]._id";
			toCheckA = MasterClientWrapper.getInstance().query(q);
			for( String wip : toCheckA ) {
				if( wip.equals( "" ) ) continue;
				tmpItem = new ABC4GSDItem("abc.activity", wip, new String[]{ "name", "description" });
				tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
				tmpGraphItem.tags.add( tUSER_Dormant );
				obj.out.add(tmpGraphItem);
				tmpGraphItem.in.add(obj);
				action.add(tmpGraphItem);
				res.add(tmpGraphItem);
			}
	
			for( ABC4GSDGraphItem obj1 : action ) {
				q = "abc.relation.[].from.==." + obj1.getId();
				wip1 = MasterClientWrapper.getInstance().query(q);
				if( wip1.length == 0 ) continue;
				q = "abc.relation." + wip1[0] + ".from.==.";
				tmp = MasterClientWrapper.getInstance().query(q);
				if( tmp.length != 0 && !tmp[0].equals(""+obj1.getId()) ) continue;
				
				//Operations
				q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + obj1.getId() + "].to]._id";
				toCheckA = MasterClientWrapper.getInstance().query(q);
				for( String wip : toCheckA ) {
					if( wip.equals( "" ) ) continue;
					tmpItem = new ABC4GSDItem("abc.activity", wip, new String[]{ "name", "description" });
					tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
					tmpGraphItem.tags.add( tUSER_Passive );
					obj1.out.add(tmpGraphItem);
					tmpGraphItem.in.add(obj1);
					operation.add( tmpGraphItem );
					res.add(tmpGraphItem);					

				}
				//Additinal
				for( ABC4GSDGraphItem obj2 : operation ) {
					q = "abc.relation.[].from.==." + obj2.getId();
					toCheckA = query(q);
					if( toCheckA.length == 0 ) continue;
					tmpGraphItem = new ABC4GSDGraphItem( null );
					tmpGraphItem.setLabel("...");
//					tmpGraphItem.placeHolder = true;
					obj2.out.add(tmpGraphItem);
					tmpGraphItem.in.add(obj2);
					res.add(tmpGraphItem);
				}
			}
		}
		for( ABC4GSDGraphItem wip : res ) {
			if( !wip.placeHolder ) 
				wip.set("onlineParticipant", ""+MasterClientWrapper.getInstance().getOnlineParticipant(wip.getId()), false );
			root.out.add(wip);
		}

	}
	private void fetchAllActivities( ABC4GSDGraphItem root ) {
		// Element.info contains the id
		// Element.label contains the name
		final String tCREATOR = "creator";
		final String tUSER_Active = "active";
		final String tUSER_Dormant = "dormant";
		final String tUSER_Passive = "passive";
		
		String q;
		List<ABC4GSDGraphItem> res = new ArrayList<ABC4GSDGraphItem>();
		List<ABC4GSDGraphItem> action = new ArrayList<ABC4GSDGraphItem>();
		ABC4GSDGraphItem tmpGraphItem;
		ABC4GSDItem tmpItem;
		List<String> toCheck = new ArrayList<String>();
		String[] toCheckA;
		String me = "" + MasterClientWrapper.getInstance().getMyId();
		
		q = "abc.activity.[abc.state.[abc.state.[].user.==."+ me +"].activity]._id";
		toCheckA = MasterClientWrapper.getInstance().query(q);
		for( String wip : toCheckA ) {
			if( wip.equals( "" ) ) continue;
			if( !toCheck.contains(wip) ) {
				tmpItem = new ABC4GSDItem("abc.activity", wip, new String[]{ "name", "description" });
				tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
				tmpGraphItem.tags.add( tUSER_Active );
				toCheck.add(wip);
				res.add(tmpGraphItem);
			} else
				for( ABC4GSDGraphItem w : res )
					if( wip.equals( "" + w.getId() ) )
						w.tags.add(tUSER_Active);
		}

		q = "abc.activity.[].creator.==."+ me;
		toCheckA = MasterClientWrapper.getInstance().query(q);
		for( String wip : toCheckA ) {
			if( wip.equals( "" ) ) continue;
			if( !toCheck.contains(wip) ) {
				tmpItem = new ABC4GSDItem("abc.activity", wip, new String[]{ "name", "description" });
				tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
				tmpGraphItem.tags.add( tCREATOR );
				toCheck.add(wip);
				res.add(tmpGraphItem);
			} else
				for( ABC4GSDGraphItem w : res )
					if( wip.equals( "" + w.getId() ) )
						w.tags.add(tCREATOR);
		}

		for( ABC4GSDGraphItem obj : res ) {
			// Check if it is not empty thus all of them
			q = "abc.relation.[].from.==." + obj.getId();
			String[] wip1 = MasterClientWrapper.getInstance().query(q);
			if( wip1.length == 0 ) continue;
			q = "abc.relation." + wip1[0] + ".from.==.";
			String[] tmp = MasterClientWrapper.getInstance().query(q);
			if( tmp.length != 0 && !tmp[0].equals(""+obj.getId()) ) continue;
			
			//Actions
			q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + obj.getId() + "].to]._id";
			toCheckA = MasterClientWrapper.getInstance().query(q);
			for( String wip : toCheckA ) {
				if( wip.equals( "" ) ) continue;
				if( !toCheck.contains(wip) ) {
					tmpItem = new ABC4GSDItem("abc.activity", wip, new String[]{ "name", "description" });
					tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
					tmpGraphItem.tags.add( tUSER_Dormant );
					toCheck.add(wip);
					obj.out.add(tmpGraphItem);
					tmpGraphItem.in.add(obj);
					action.add(tmpGraphItem);
					res.add(tmpGraphItem);
				} else
					for( ABC4GSDGraphItem w : res )
						if( wip.equals( "" + w.getId() ) ) {
							w.tags.add(tUSER_Dormant);
							obj.out.add(w);
							w.in.add(obj);
							action.add(w);
						}
			}
		}
		
		for( ABC4GSDGraphItem obj : action ) {
			// Check if it is not empty thus all of them
			q = "abc.relation.[].from.==." + obj.getId();
			String[] wip1 = MasterClientWrapper.getInstance().query(q);
			if( wip1.length == 0 ) continue;
			q = "abc.relation." + wip1[0] + ".from.==.";
			String[] tmp = MasterClientWrapper.getInstance().query(q);
			if( tmp.length != 0 && !tmp[0].equals(""+obj.getId()) ) continue;
			
			//Operations
			q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + obj.getId() + "].to]._id";
			toCheckA = MasterClientWrapper.getInstance().query(q);
			for( String wip : toCheckA ) {
				if( wip.equals( "" ) ) continue;
				if( !toCheck.contains(wip) ) {
					tmpItem = new ABC4GSDItem("abc.activity", wip, new String[]{ "name", "description" });
					tmpGraphItem = new ABC4GSDGraphItem( tmpItem );
					tmpGraphItem.tags.add( tUSER_Passive );
					toCheck.add(wip);
					res.add(tmpGraphItem);
				} else
					for( ABC4GSDGraphItem w : res )
						if( wip.equals( "" + w.getId() ) ) {
							if(!w.tags.contains(tUSER_Active) && w.tags.contains(tUSER_Dormant))
								w.tags.remove(tUSER_Dormant);
							w.tags.add(tUSER_Passive);
							obj.out.add(w);
							w.in.add(obj);
						}
			}
		}
		
		for( ABC4GSDGraphItem wip : res ) {
			wip.set("onlineParticipant", ""+MasterClientWrapper.getInstance().getOnlineParticipant(wip.getId()), false );
			root.out.add(wip);
		}
	}

	private void getActivities(ABC4GSDGraphItem root, ArrayList<ABC4GSDGraphItem> resp) {
		if( root.content != null ) {
			for( ABC4GSDGraphItem w: resp )
				if( w.getId() == root.getId() )
					return;
			resp.add(root);
		}
		int i = 0;
		while(root.out.size() > i) {
			getActivities(root.out.get(i), resp);
			i += 1;
		}
	}
	private String[] getActivitiesIds() {
		ArrayList<ABC4GSDGraphItem> activities = new ArrayList<ABC4GSDGraphItem>();
		getActivities((ABC4GSDGraphItem)ABC4GSDItemManager.getManager("Activity").getAt(0), activities);
		List<String> ret = new ArrayList<String>();
		for(int i=0; i<activities.size(); i++)
			if(!activities.get(i).placeHolder)
				ret.add( ""+activities.get(i).getId() );
		return ret.toArray( new String[ret.size()]);
	}

	public void setSelection( String id ) { setSelection(Long.parseLong(id)); }
	
	public void setSelection( long id ) {
		List<GraphNode> nodes = graph.getNodes();
		for( GraphNode n : nodes ) {
			NodeFigure fig = (NodeFigure)n.getNodeFigure();
			ABC4GSDGraphItem tmp = (ABC4GSDGraphItem)n.getData();
			if( fig.isActive() ) fig.setActive(false);
			if( fig.isSelected() ) fig.setSelected(false);
			if( !tmp.placeHolder && tmp.getId() == id ) {
				fig.setActive(true);
				fig.setSelected(true); }
		}
	}
}


