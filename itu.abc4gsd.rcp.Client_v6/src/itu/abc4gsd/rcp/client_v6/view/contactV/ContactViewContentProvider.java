package itu.abc4gsd.rcp.client_v6.view.contactV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.json.simple.JSONObject;

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
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

class ContactViewContentProvider extends AppInterface implements IStructuredContentProvider, ABC4GSDItemManagerListener {

	/* 
	 * AppInterface
	 *  - classes responding to events arriving from the middleware 
	 */
	private class CmdUpdateElement implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdate");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			
			IABC4GSDItem tmp = ABC4GSDItemManager.getManager("Contact").get(wip.id);
			if( tmp == null )
				tmp = new ABC4GSDItem(wip.model + "." + wip.type, wip.id);
			tmp.set(wip.key, wip.value, false);
			itemsChanged(null);
		}
	}
	private class CmdUpdateState implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdateState");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			
			IABC4GSDItem tmp = ABC4GSDItemManager.getManager("Contact").get(wip.id);
			if( tmp == null )
				tmp = new ABC4GSDItem(wip.model + "." + wip.type, wip.id);
			tmp.set(wip.key, wip.value, false);

			if( wip.id == tmp.getId() ) return;
			
			if( wip.value == Constants.USR_CONNECTED ) {
				String title = "" + tmp.get("name").toString() + " is connected.";
				JSONObject content = new JSONObject();
				content.put("title", title);
				content.put("body", "");
				sendCommand("NOTIFY " + content.toJSONString() );
			}
			
			if( wip.value == Constants.USR_DISCONNECTED ) {
				String title = "" + tmp.get("name").toString() + " disconnected.";
				JSONObject content = new JSONObject();
				content.put("title", title);
				content.put("body", "");
				sendCommand("NOTIFY " + content.toJSONString() );
			}

			itemsChanged(null);
		}
	}

	

	
	
	private class CmdUpdateArtifact implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdateArtifact");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			getUser(wip.id);
//			// get asset ptr
//			ABC4GSDItem asset = new ABC4GSDItem("abc.asset", wip.value, new String[]{"ptr"});
//			if( asset.get("ptr").toString().length() == 0 ) return;
//			// get artifact name
//			ABC4GSDItem artifact = new ABC4GSDItem("abc.artifact", asset.get("ptr").toString(), new String[]{"name"});
//
//			IABC4GSDItem tmp = ABC4GSDItemManager.getManager("Contact").get(wip.id);
//			if( tmp == null )
//				tmp = new ABC4GSDItem("abc.user", wip.id);
//			tmp.set( "artifact", artifact.get("name").toString(), false );
//			
			itemsChanged(null);
		}
	}

	private class CmdActivityChanged implements ICommand {
		@SuppressWarnings("unchecked")
		public void execute(String data) {
//			log( ""+setBlockReception(true) );
			log("Executing CmdActivityChanged");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.value.equals("-1") ) return ;
			currentActivity = Long.parseLong(wip.value);

			(new CmdListModified()).execute(null);
		}
	}
	
	private class CmdListModified implements ICommand {
		@SuppressWarnings("unchecked")
		public void execute(String data) {
			log("Executing CmdListModified");
			String q;
			unsubscribe( subscriptionTemp );
			subscriptionTemp.clear();
			ABC4GSDItemManager.getManager("Contact").initItems();
			
			q = "abc.ecology.[!.abc.ecology].activity.+." + currentActivity;				// User added
			subscriptionTemp.add(q);
			subscribe( q, new CmdListModified() );
			
			q = "abc.ecology.[abc.ecology.[].name.~=.{{" + currentActivity + ":[0-9]*}}].user";				// User added
//			q = "abc.state.[abc.state.[].activity.==." + currentActivity + "].user";				// Get list of users in this activity
			String[] users = query(q);
			for( int x=0; x< users.length; x++ ) {
				String tmp = users[x];
				if(tmp.startsWith("["))
					tmp = tmp.substring(1, tmp.length()-1);
				long user = Long.parseLong(tmp);
				getUser( user );
				
				q = "abc.ecology.[].name.==." + currentActivity + ":" + user;
				q = "abc.ecology.-." + query(q)[0];					// User removed
				subscriptionTemp.add(q);
				subscribe( q, new CmdListModified() );
				
				q = "abc.user." + user + ".name";							//  user's name changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdateElement() );
	
				q = "abc.user." + user + ".state";							// user's state changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdateState() );
	
				q = "abc.user." + user + ".artifact";							// Activity user's file changed
//				q = "abc.property.[abc.property.[].user.==." + user + "].value.=.true"; // Activity user's file changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdateArtifact() );

				q = "abc.user." + user + ".activity";							//  user's activity changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdateArtifact() );

			}

//			log( ""+setBlockReception(false) );
			itemsChanged(null);
		}
	}

	/* 
	 * AppInterface
	 */
	private Button btnAdd;
	private String u_name;
	private long u_id;
	private long currentActivity = -1;
	private List<String> subscriptionPerm = new ArrayList<String>();
	private List<String> subscriptionTemp = new ArrayList<String>();

	
	public ContactViewContentProvider(String name) {
		super(name);
		_init();
	}
	
	private void _init() {
		String q = "";
		log("--> Initializing data <--");
		ABC4GSDItemManager.getManager("Contact").initItems();
		itemsChanged(null);
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		u_name = prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, ""));
		q = "abc.user.[].name.==." + u_name;
		u_id = Long.parseLong( query(q)[0] );
		
//		q = "abc.user";				// Get list of all users
//		JSONArray users = (JSONArray) ((JSONObject) JSONValue.parse( query(q) ) ).get(Constants.MSG_A);
//		for( int x=0; x< users.size(); x++ )
//			getUser( Long.parseLong(users.get(x).toString()) );

		q = "abc.user." + u_id + ".activity.=.";
		subscriptionPerm.add(q);
		subscribe( q, new CmdActivityChanged() );
	}
	
	private void getUser( long tmpId ) {
        String tmpResp = "";
        String tmpResp2 = "";
        IABC4GSDItem wip = ABC4GSDItemManager.getManager("Contact").get(tmpId); 
        if( wip != null )
        	ABC4GSDItemManager.getManager("Contact").removeItem( wip );

        wip = new ABC4GSDItem("abc.user", tmpId, new String[]{ "name", "state", "artifact", "activity" });
        // ((JSONObject) JSONValue.parse( query( "abc.user." + tmpId+ ".activity" ) ) ).get(Constants.MSG_A).toString();
		if(wip.get("artifact").toString().length()>0) {
			String[] artifact = query("abc.asset."+wip.get("artifact")+".ptr");
			if( artifact.length > 0 )
				tmpResp = query( "abc.artifact.[" + artifact[0] + "].name" )[0];
		}
		if(wip.get("activity").toString().length()>0 && !wip.get("activity").equals(""+currentActivity)) {
			String[] curr = query( "abc.activity."+wip.get("activity")+".name" );
			if( curr.length != 0 )
				tmpResp2 = "**" + curr[0] + "** ";
		} else tmpResp2 = "";
		wip.set("artifact", tmpResp2 + tmpResp, false);
		ABC4GSDItemManager.getManager("Contact").addItem(wip);
	}
	
	public void killOperation() {}
	public boolean personalHandler(String ch, String msg) { return true; }
	public void resumeOperation() { setBlockReactions(false); }
	public void suspendOperation() { setBlockReactions(true); _init(); }

	/*
	 * Manager
	 */
	private TableViewer viewer;
	private ABC4GSDItemManager manager;

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (TableViewer) viewer;
      if (manager != null)
         manager.removeABC4GSDItemManagerListener(this);
      manager = (ABC4GSDItemManager) newInput;
      if (manager != null)
         manager.addABC4GSDItemManagerListener(this);
   }

   public void dispose() {
   }

   public Object[] getElements(Object parent) {
      return manager.getItems();
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
	   if (viewer == null || viewer.getControl().isDisposed())
		   return;

	   if( ! (Boolean)OSGIEventHandler.getInstance().get("stateConnected?") ) return;
	   if( currentActivity != -1 ) btnAdd.setEnabled(true);
	   else btnAdd.setEnabled(true);
	   viewer.refresh();
   }

	public void operationDelete(IABC4GSDItem element) {
		System.out.println("Delete " + element.get("name")); 
		long activityId = currentActivity;
		long userToRemove = element.getId();
		String q;
		boolean removeActivity = false;

	    // Check if there will still participants (currently only one)
		q = "abc.state.[abc.state.[].activity.==." + activityId + "].user";
		String[] remaining = query(q);
		if( remaining.length == 1 ) removeActivity = true; 

		// If activity needs to be removed, it cannot have subactivities.
		if( removeActivity ) {
			String[] resp = query( "abc.relation.[].from.==." + activityId ); 
			if( resp.length > 0 ) {
				MessageDialog msg = new MessageDialog(Display.getDefault().getActiveShell(), "MessageDialog", null,
						"By removing this user the activity would also be removed. Activities descend from this one, delete first subactivities.", MessageDialog.WARNING,
				        new String[] { "Continue" }, 0);
				if (msg.open() == 0);
				return;
			}
		}

		// Confirmation required
		MessageDialog messageDialog = new MessageDialog(viewer.getControl().getShell(), "MessageDialog", null,
		        "Please confirm the removal of " + element.get("name") + " from the current activity.", MessageDialog.CONFIRM,
		        new String[] { "Confirm", "Cancel" }, 0);
		if (messageDialog.open() != 0) 
			return;

		// Remove links of user to activity; hence state and activity
		query("abc.state.-.[abc.state.[].name.==."+ activityId +":"+ userToRemove+"]");
	    query("abc.ecology.-.[abc.ecology.[].name.==."+ activityId +":"+ userToRemove +"]");
	   
		// If activity removal required, suspend it and remove the whole activity.
	    if( removeActivity ) {
	    	sendCommand( "SUSPEND " + activityId );
	    	_removeActivity( activityId );
	    }
	    
	    // update the viewer
//	    itemsChanged(null);
	}
	
	public void operationAdd() {
		// Getting list of users not in the activity
		List<String> currentUsers = Arrays.asList( query( "abc.ecology.[abc.ecology.[].activity.==." + currentActivity + "].user" ) );
		List<String> availableUsers = new ArrayList<String>(Arrays.asList( query( "abc.user.[]._id" ) ));
		availableUsers.removeAll(currentUsers);
		// Getting list of assets
		String[] assets = query( "abc.ecology.[abc.ecology.[].name.==." + currentActivity + ":" + u_id + "].asset");
		
		String[] usersId = availableUsers.toArray(new String[availableUsers.size()]);
		String[] usersName = new String[ usersId.length ]; 
		for( int i=0; i<usersId.length; i++ )
			usersName[i] = query( "abc.user." + usersId[i] + ".name" )[0];
		
		// Create the dialog box for the selection
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), new LabelProvider());
		dialog.setElements(usersName);
		dialog.setMultipleSelection(true);
		dialog.setTitle("Select the user(s) to add");
		if (dialog.open() != Window.OK)
			return;
		Object[] tmp = dialog.getResult();
		String[] selected = Arrays.copyOf(tmp, tmp.length, String[].class);
		
		List<String> names = Arrays.asList( usersName );		
		for( String u : selected ) {
			String currUID = usersId[ names.indexOf(u) ];
			
			// ... creating State/Info
			IABC4GSDItem newState = new ABC4GSDItem( "abc.state" );
			newState.set( "name", currentActivity + ":" + currUID );
			newState.set( "state", Constants.STATE_UNKNOWN );
			newState.attach( "activity", currentActivity );
			newState.attach( "user", currUID );
			
			// ... creating ecology
			IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology" );
			newEcology.set( "name", currentActivity + ":" + currUID );
			newEcology.attach( "activity", currentActivity );
			newEcology.attach( "user", currUID );
			for( String asset : assets ) 
				newEcology.attach( "asset", asset );
		}
		
   }
	
   public void linkAddBtn( Button btn ) { this.btnAdd = btn; }
 
   
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




















