package itu.abc4gsd.rcp.client_v6.view.activityV;

 import itu.abc4gsd.rcp.client_v6.Activator;
 import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
 import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
 import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
 import itu.abc4gsd.rcp.client_v6.preferences.Connection;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerEvent;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerListener;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDTreeItem;
 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

class ActivityViewHContentProvider extends AppInterface implements ITreeContentProvider, ABC4GSDItemManagerListener {
	/* 
	 * AppInterface
	 *  - classes responding to events arriving from the middleware 
	 */
	private class CmdUpdate implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdate");
			initData();		
			itemsChanged(null);
		}
	}
	private class CmdUserStatusChanged implements ICommand {
		public void execute(String data) {
			log("Executing CmdUserStatusChanged");
			initData();
			itemsChanged(null);
		}
	}
	private class CmdUpdateWithWait implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdateWithWait");
			new Timer().schedule(new TimerTask() {          
			    @Override
			    public void run() {
					initData();		
					itemsChanged(null);
			    }
			}, 1000);
		}
	}
	private class CmdRemovedFromActivity implements ICommand {
		public void execute(String data) {
			log("Executing CmdRemovedFromActivity");
			RemoteMessage wip = new RemoteMessage(data);
			final IABC4GSDItem act = new ABC4GSDItem("abc.activity", wip.id);
			act.update( new String[]{"name","description"});

			String title = "";
			String text = "Your activity list will be updated.\n";
			if( act.get("name").equals("") )
				title = "You have been removed from a no longer available activity. Possible reason: activity removal.";
			else {
				title = u_name + " you have been removed from";
				text +=	"Name: " + act.get("name") + "\n" +
						"Description: \n" + act.get("description");
			}
			JSONObject content = new JSONObject();
			content.put("title", title);
			content.put("body", text);
			sendCommand("NOTIFY " + content.toJSONString() );
			
			initData();		
			itemsChanged(null);
		}
	}
	private class CmdNewActivityDetected implements ICommand {
		public void execute(String data) {
			// "abc.state.[].user.+." + u_id;
			log("Executing CmdNewActivityDetected");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			String q = "abc.state." + wip.id + ".activity";

			final IABC4GSDItem state = new ABC4GSDItem("abc.state", wip.id);
			state.update( new String[]{"activity"});
			final IABC4GSDItem act = new ABC4GSDItem("abc.activity", ( (ArrayList<Long>)state.get("activity")).get(0));
			act.update( new String[]{"name","description"});

			String title = "";
			String text = "Your activity list will be updated and the state set to\ninitialized until its resumption.\n";
			if( act.get("name").equals("") )
				title = "Activity successfully created";
			else {
				title = u_name + " you are invited to a new Activity";
				text +=	"Name: " + act.get("name") + "\n" +
						"Description: \n" + act.get("description");
			}
			JSONObject content = new JSONObject();
			content.put("title", title);
			content.put("body", text);
			sendCommand("NOTIFY " + content.toJSONString() );
			
			long actId = Long.parseLong(query(q)[0]);
			if( actId == lastIdReceived) 
				return;
			// in case the message has already been received there is no need to update the view but only send notifications
			initData();		
			itemsChanged(null);
			lastIdReceived = actId;
		}
	}
	private class CmdNewActivityCreated implements ICommand {
		public void execute(String data) {
			// "abc.activity.[].creator.+." + u_id;
			log("Executing CmdNewActivityCreated");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1 || wip.id == lastIdReceived ) 
				return;
			final IABC4GSDItem act = new ABC4GSDItem("abc.activity", wip.id);
			act.update( new String[]{"name","description"});

			String title = "";
			String text = "Your activity list will be updated and the state set to\ninitialized until its resumption.\n";
			if( act.get("name").equals("") )
				title = "Activity successfully created";
			else {
				title = u_name + " you have created a new Activity";
				text +=	"Name: " + act.get("name") + "\n" +
						"Description: \n" + act.get("description");
			}
			JSONObject content = new JSONObject();
			content.put("title", title);
			content.put("body", text);
			sendCommand("NOTIFY " + content.toJSONString() );
			
			initData();		
			itemsChanged(null);
			lastIdReceived = wip.id;
		}
	}
	private class CmdActivityRemoved implements ICommand {
		public void execute(String data) {
			log("Executing CmdActivityRemoved");
			RemoteMessage wip = new RemoteMessage(data);

			JSONObject content = new JSONObject();
			content.put("title", "An activity you were involved has been removed");
			content.put("body", "Your activity list will be updated.\nID: " + wip.id + "\n");
			sendCommand("NOTIFY " + content.toJSONString() );
			
			initData();		
			itemsChanged(null);
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
//				itemsChanged(null);
		}
	}

	/* 
	 * AppInterface
	 */
	private static final int DEPTH = 3;
	private String u_name;
	private long u_id;
	private long a_id; 
	private long currentSelected = -1;
	private long lastIdReceived;
	private List<String> subscriptionPerm = new ArrayList<String>();
	private List<String> subscriptionTemp = new ArrayList<String>();
	private Tree tree;
	
	public ActivityViewHContentProvider(String name, Tree tree) {
		super(name);
		this.tree = tree;
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

		initData();
	}
	
	public void initData() {
		log("--> Initializing data <--");
		String q; 
		String[] qq;
		unsubscribe();
		subscriptionPerm.clear();
		subscriptionTemp.clear();
		ABC4GSDItemManager.getManager("ActivityH").initItems(true);
		refreshActivities();
		String[] activities = getActivitiesIds();

		q = "abc.activity.[].creator.+." + u_id;		// You created a new activity
		subscriptionPerm.add( q );
		subscribe( q, new CmdNewActivityCreated() );
		
		q = "abc.state.[].user.+." + u_id;				// Added to new activity
		subscriptionPerm.add( q );
		subscribe( q, new CmdNewActivityDetected() );
		
		q = "abc.relation.-.";						// Generic activity relation removed
		subscriptionPerm.add(q);					// When the entity is removed it is tricky
		subscribe( q, new CmdUpdate() );

		q = "abc.relation.+";			// Generic Activity relation created
		subscriptionPerm.add(q);
		subscribe( q, new CmdUpdate() );
		
		q = "abc.user." + u_id + ".activity.=.";		// Activity Changed
		subscriptionPerm.add(q);
		subscribe( q, new CmdActivityChanged() );

		for( String act : activities ) {
			q = "abc.activity." + act + ".name";							// Activity name changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );
	
			q = "abc.activity." + act + ".state";							// Activity state changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );
	
			q = "abc.activity." + act + ".description";						// Activity description changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );

			q = "abc.activity.-." + act;									// Activity removed
			subscriptionTemp.add(q);
			subscribe( q, new CmdActivityRemoved() );

			q = "abc.activity." + act + ".active.+";					// User joined online
			subscriptionTemp.add(q);
			subscribe( q, new CmdUserStatusChanged() );			

			q = "abc.activity." + act + ".active.-";					// User left
			subscriptionTemp.add(q);
			subscribe( q, new CmdUserStatusChanged() );
		}
//		TODO> missing real data for online participants
	}
	
	private void refreshActivities() {
		ABC4GSDTreeItem tmp = new ABC4GSDTreeItem(null,null);
		fetchActivities( tmp );
		sortActivities( tmp );
		System.out.println("Done");
		ABC4GSDItemManager.getManager("ActivityH").addItem(tmp);
		
	}
		
	public void killOperation() {}
	public boolean personalHandler(String ch, String msg) { return true; }
	public void resumeOperation() { }
	public void suspendOperation() { }

	
	protected void _suspend( long activity ) {
		sendCommand( "SUSPEND " + activity );
	}

	protected void _resume( long activity ) {
		a_id = activity;
		sendCommand( "RESUME " + activity );
	}

	/*
	 * Manager
	 */
	private TreeViewer viewer;
	private ABC4GSDItemManager manager;

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (TreeViewer) viewer;
      if (manager != null)
         manager.removeABC4GSDItemManagerListener(this);
      manager = (ABC4GSDItemManager) newInput;
      if (manager != null)
         manager.addABC4GSDItemManagerListener(this);
   }

   public void dispose() {}

   public Object[] getElements(Object parent) {
	   if( parent instanceof ABC4GSDTreeItem )
		   return ((ABC4GSDTreeItem) parent).children.toArray();
	   else
		   return manager.getItems();
   }

	public Object[] getChildren(Object parentElement) {
		return getElements( parentElement);
	}

	public Object getParent(Object element) {
	    if (element == null) return null;
	    return ((ABC4GSDTreeItem) element).parent;
	}

	public boolean hasChildren(Object element) {
		 return ((ABC4GSDTreeItem) element).children.size() > 0;
	}


	private void fetchActivities( ABC4GSDTreeItem current ) {
		// Element.info contains the id
		// Element.label contains the name
		String q;
		String[] toCheck;
		String me = "" + MasterClientWrapper.getInstance().getMyId();
		if(current.parent == null) {
			// Get all activities where the user participates
			q = "abc.activity.[abc.state.[abc.state.[].user.==."+ me +"].activity]._id";
			toCheck = MasterClientWrapper.getInstance().query(q);
			List<String> wip1 = new ArrayList<String>();
			for( String x : toCheck )
				wip1.add(x);
			// Get all activities created by the user
			q = "abc.activity.[].creator.==."+ me;
			toCheck = MasterClientWrapper.getInstance().query(q);
			for( String x : toCheck )
				if(!wip1.contains(x))
					wip1.add(x);
			toCheck = wip1.toArray( new String[ wip1.size() ] );
		} else {
			// Get all the actions
			q = "abc.relation.[].from.==." + current.getInfo();
			String[] wip1 = MasterClientWrapper.getInstance().query(q);
			q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + current.getInfo() + "].to]._id";
			toCheck = MasterClientWrapper.getInstance().query(q);
			if(wip1.length!=toCheck.length) return;
		}
		if( toCheck.length == 0 ) return;
		for( String x : toCheck ) {
			if( !x.equals( "" ) ) {
				ABC4GSDItem tmptmp = new ABC4GSDItem("abc.activity", x, new String[]{ "name", "description", "state" });
				ABC4GSDTreeItem child = new ABC4GSDTreeItem( current, tmptmp );
				fetchActivities(child);
				child.set("onlineParticipant", ""+MasterClientWrapper.getInstance().getOnlineParticipant(Long.parseLong(x)), false );
				current.add(child);
			}
		}

	}

	private void getActivities(ABC4GSDTreeItem root, ArrayList<ABC4GSDTreeItem> resp) {
		if( root.content != null )
			resp.add(root);
		int i = 0;
		while(root.children.size() > i) {
			if( root.isLeaf() ) return;
			getActivities(root.children.get(i), resp);
			i += 1;
		}
	}
	
	private String[] getActivitiesIds() {
		ArrayList<ABC4GSDTreeItem> activities = new ArrayList<ABC4GSDTreeItem>();
		getActivities((ABC4GSDTreeItem)ABC4GSDItemManager.getManager("ActivityH").getAt(0), activities);
		String[] ret = new String[activities.size()];
		for(int i=0; i<activities.size(); i++)
			ret[i] = ""+activities.get(i).getId();
		return ret;
	}

	public void sortActivities( ABC4GSDTreeItem current ) {
		boolean found = false;
		List<ABC4GSDTreeItem> results = new ArrayList<ABC4GSDTreeItem>();
		for( ABC4GSDTreeItem wip : current.children ) {
			for( ABC4GSDTreeItem wip2 : current.children ) {
				if( wip.additionalInfo[0].equals( wip2.additionalInfo[0] ) )
					continue;
				if( ABC4GSDTreeItem.isPresent(wip2, wip.additionalInfo[0]) != null ) {
					found = true;
					break;
				}
			}
			if( !found )
				results.add( wip );
			found = false;
		}
		current.children = results;
	}

	public void setSelection( String id ) { setSelection(Long.parseLong(id)); }
	public void setSelection( long id ) { select(id); }
		
	public void activate( long id ) {
		if (viewer == null || viewer.getControl().isDisposed())
			return;

		IABC4GSDItem tmp = new ABC4GSDItem( "abc.activity", id );
		tmp.update(new String[]{"name"});

		int idx = 0;
		List<TreeItem> nodes = new ArrayList<TreeItem>();
		nodes.addAll( Arrays.asList(viewer.getTree().getItems()) );
		while( idx < nodes.size() ) {
			nodes.addAll( Arrays.asList(nodes.get(idx).getItems()) );
			idx += 1;
		}
		
		for( TreeItem node : nodes )
			if( node.getText().equals(tmp.get("name"))) {
				viewer.getTree().select(node);
				break; }
		updateViewer(null);
	}

	public void select( long id ) {
		if (viewer == null || viewer.getControl().isDisposed())
			return;
		
		IABC4GSDItem tmp = new ABC4GSDItem( "abc.activity", id );
		tmp.update(new String[]{"name"});

		viewer.expandAll();
		int idx = 0;
		List<TreeItem> nodes = new ArrayList<TreeItem>();
		nodes.addAll( Arrays.asList(viewer.getTree().getItems()) );
		while( idx < nodes.size() ) {
			nodes.addAll( Arrays.asList(nodes.get(idx).getItems()) );
			idx += 1;
		}
		
		List<String> path = new ArrayList<String>();
		for( TreeItem node : nodes )
			if( node.getText().equals(tmp.get("name"))) {
				viewer.getTree().select(node);
				path.add(node.getText());
				while(node.getParentItem()!=null) {
					node = node.getParentItem();
					path.add(node.getText()); }
				updateViewer(null);
				break; }
		IABC4GSDItem[] iNodes = ABC4GSDItemManager.getManager("ActivityH").getItems();
		boolean found = false;
		for( int l=0; l<iNodes.length; l++ ) {
			for( int i=path.size()-1; i>=0; i-- ) 
				if( ((ABC4GSDTreeItem)iNodes[l]).label.equals(path.get(i))) {
					found = true;
					break;
				}
			if( !found )
				viewer.setExpandedState(iNodes[l], false);
			else
				viewer.setExpandedState(iNodes[l], true);
			found = false;
		}
	}

	public void _remove(long actId) {
		String[] resp = null;
		// Checking if the activity has sub activities. In such case they need to be deleted first as in some dbmses.
		resp = MasterClientWrapper.getInstance().query( "abc.relation.[].from.==." + actId ); 
		if( resp.length > 0 ) {
			MessageDialog msg = new MessageDialog(Display.getDefault().getActiveShell(), "MessageDialog", null,
					"Other activities descend from this one. Remove them first.", MessageDialog.WARNING,
			        new String[] { "Continue" }, 0);
			if (msg.open() == 0);
			return;
		}

		// Checking if all participants are disconnected. If not, the activity cannot be removed
		resp = MasterClientWrapper.getInstance().query( "abc.user.[].activity.==." + actId );
		if( resp.length > 0 ) {
			MessageDialog msg = new MessageDialog(Display.getDefault().getActiveShell(), "MessageDialog", null,
					"Users still connected to this activity. Removal cannot be performed.", MessageDialog.WARNING,
			        new String[] { "Continue" }, 0);
			if (msg.open() == 0);
			return;
		}
		MessageDialog msg = new MessageDialog(Display.getDefault().getActiveShell(), "MessageDialog", null,
				"Continue with the removal of the activity?\nThis procedure cannot be undone.", MessageDialog.WARNING,
		        new String[] { "Ok", "Cancel" }, 0);
		if (msg.open() == 1) return;
		
		// Removing all relations with super activities
		resp = MasterClientWrapper.getInstance().query( "abc.relation.[].to.==." + actId );
		for( String wip : resp )
			MasterClientWrapper.getInstance().query( "abc.relation.-." + wip );
		
		// Removing all state instances activity:user
		resp = MasterClientWrapper.getInstance().query( "abc.state.[].name.~=.{{" + actId + ":[0-9]*}}" );
		for( String wip : resp )
			MasterClientWrapper.getInstance().query( "abc.state.-." + wip );

		// Removing ecologies by incrementally removing all attached assets.
		String[] ecologies = MasterClientWrapper.getInstance().query( "abc.ecology.[].name.~=.{{" + actId + ":[0-9]*}}" );
		for( String wip : ecologies ) {
			resp = MasterClientWrapper.getInstance().query( "abc.ecology."+wip+".asset" );
			for( String tmp : resp ) 
				MasterClientWrapper.getInstance().query( "abc.asset.-."+ tmp );
			MasterClientWrapper.getInstance().query( "abc.ecology.-."+ wip );
		}
		
		// Removing the activity
		MasterClientWrapper.getInstance().query( "abc.activity.-."+ actId );

		// TODO > As in creation, Properties are still there
	}


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

	   viewer.getTree().redraw();
	   viewer.refresh();
   }

	public void operationDelete(IABC4GSDItem element) {
		System.out.println("Delete " + element.get("name"));
		final long actToRemove = element.getId();

		// Check if there are online participants
		String q = "abc.state.[abc.state.[].activity.==." + actToRemove + "].state.==." + Constants.STATE_RESUMED;
		String[] remaining = query(q);
		if( remaining.length > 1 ) {
			MessageDialog msg = new MessageDialog(Display.getDefault().getActiveShell(), "MessageDialog", null,
					"The activity cannot be deleted as other participants are connected.", MessageDialog.WARNING,
			        new String[] { "Continue" }, 0);
			return;
		}
		
		// If activity has subactivities they will be removed incrementally as well
		String[] resp = query( "abc.relation.[].from.==." + actToRemove ); 
		if( resp.length > 0 ) {
			MessageDialog msg = new MessageDialog(Display.getDefault().getActiveShell(), "MessageDialog", null,
					"Other activities descend from this one. Confirm the removal of all subactivities.", MessageDialog.WARNING,
					new String[] { "Confirm", "Cancel" }, 0);
			if (msg.open() == 0) 
				for( String wip : resp ) {
					IABC4GSDItem toDelete = new ABC4GSDItem( "abc.activity", MasterClientWrapper.getInstance().query("abc.relation." + wip + ".to")[0], new String[]{"name"} );
					operationDelete(toDelete);
				}
			else
				return;
		}

		// Confirmation required
		MessageDialog messageDialog = new MessageDialog(viewer.getControl().getShell(), "MessageDialog", null,
		        "Please confirm the removal of " + element.get("name") + ". The operation is not reversable.", MessageDialog.CONFIRM,
		        new String[] { "Confirm", "Cancel" }, 0);
		if (messageDialog.open() != 0) 
			return;

		if( actToRemove == MasterClientWrapper.getInstance().getCurrentActivity() ) {
			sendCommand( "SUSPEND " + actToRemove );
			new Timer().schedule(new TimerTask() {          
			    public void run() { _removeActivity( actToRemove ); } }, 200); // Wait to get the suspend signals dispatched
		} else
			_removeActivity( actToRemove );
	}

	public void operationChat(IABC4GSDItem element) {
		final long actId = element.getId();
		sendCommand("CHAT_OPEN " + actId );
	}

	public void operationSingleClick(IABC4GSDItem item) {
		System.out.println("Selected " + item.get("name"));
		if(item != null){
			if( currentSelected == item.getId() ) return;
			currentSelected = item.getId();
			System.out.println("Selected : "+ item.getId() + " - " + item.get("name") );

			BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
	        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
	        EventAdmin eventAdmin = ctx.getService(ref);
	        Map<String,Object> properties = new HashMap<String, Object>();
	        properties.put("ACT_ID", ""+item.getId());
	        eventAdmin.postEvent( new Event("Activity/Selected", properties) );
		}
	}
	// A faster removal of the activity than the usual one with checks as there are no attached users
	private void _removeActivity(long actId) {
		String[] resp = null;

		// Removing all relations with super activities
		resp = query( "abc.relation.[].to.==." + actId );
		for( String wip : resp )
			query( "abc.relation.-." + wip );
		
		// Removing all state instances activity:user
		resp = query( "abc.state.[].name.~=.{{" + actId + ":[0-9]*}}" );
		for( String wip : resp )
			query( "abc.state.-." + wip );

		// Removing ecologies by incrementally removing all attached assets.
		String[] ecologies = query( "abc.ecology.[].name.~=.{{" + actId + ":[0-9]*}}" );
		for( String wip : ecologies ) {
			resp = query( "abc.ecology."+wip+".asset" );
			for( String tmp : resp ) 
				query( "abc.asset.-."+ tmp );
			query( "abc.ecology.-."+ wip );
		}
		
		// Removing the activity
		query( "abc.activity.-."+ actId );

		// TODO > As in creation, Properties are still there
	}
}


