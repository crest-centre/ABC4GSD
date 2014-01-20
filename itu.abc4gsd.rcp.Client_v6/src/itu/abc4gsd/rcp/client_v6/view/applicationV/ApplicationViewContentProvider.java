package itu.abc4gsd.rcp.client_v6.view.applicationV;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerEvent;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerListener;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;

class ApplicationViewContentProvider extends AppInterface implements IStructuredContentProvider, ABC4GSDItemManagerListener {

	/* 
	 * AppInterface
	 *  - classes responding to events arriving from the middleware 
	 */
	private class CmdApplicationAdded implements ICommand {
		public void execute(String data) {
			log("Executing CmdApplicationAdded");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;

			// check if it is an app
			String q, resp, assetId, appId;
			q = "abc.asset.[abc.ecology." + wip.id + ".asset].type";
			resp = query(q)[0];
			if( ! resp.equals("application") ) return; 
			
			assetId = query("abc.ecology." + wip.id + ".asset")[0];
			appId = query("abc.asset." + assetId + ".ptr")[0];
			
			getApplication( Long.parseLong(assetId), Long.parseLong(appId) );
			
			q = "abc.application." + appId + ".name";							// App name changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );
		}
	}

	private class CmdUpdate implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdate");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			
			IABC4GSDItem tmp = ABC4GSDItemManager.getManager("Application").get(wip.id);
			if( tmp == null )
				tmp = new ABC4GSDItem(wip.model + "." + wip.type, wip.id);
			tmp.set(wip.key, wip.value);
			itemsChanged(null);
		}
	}

	private class CmdActivityChanged implements ICommand {
		@SuppressWarnings("unchecked")
		public void execute(String data) {
//			log( ""+setBlockReception(true) );
			log("Executing CmdActivityChanged");
			RemoteMessage wip = new RemoteMessage(data);
			currentActivity = Long.parseLong(wip.value);
			String q;
			unsubscribe( subscriptionTemp );
			subscriptionTemp.clear();
			ABC4GSDItemManager.getManager("Application").initItems();

			q = "abc.ecology.[abc.ecology.[].name.==." +currentActivity+ ":" +u_id+ "].asset"; // getting list of related assets
			String[] applications = query(q);

			// keep apps
			JSONArray resultsAsset = new JSONArray();
			JSONArray resultsPtr = new JSONArray();
			ABC4GSDItem tmpItem;
			for( int x=0; x< applications.length; x++ ) {
				tmpItem = new ABC4GSDItem( "abc.asset", applications[x], new String[]{"type"} );
				if( tmpItem.get("type").equals("application") ) {
					resultsPtr.add( tmpItem.get("ptr") );
					resultsAsset.add( applications[x] );
				}
			}
						
			for( int x=0; x< resultsPtr.size(); x++ ) {
				long application = Long.parseLong(resultsPtr.get(x).toString());
				long asset = Long.parseLong(resultsAsset.get(x).toString());
				
				getApplication( asset, application );
				
				q = "abc.application." + application + ".name";							// App name changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdate() );

			}

//			log( ""+setBlockReception(false) );
			itemsChanged(null);
		}
	}

	/* 
	 * AppInterface
	 */
	private String u_name;
	private long u_id;
	private long currentActivity = -1;
	private List<String> subscriptionPerm = new ArrayList<String>();
	private List<String> subscriptionTemp = new ArrayList<String>();

	
	public ApplicationViewContentProvider(String name) {
		super(name);
		_init();
	}
	
	private void _init() {
		String q = "";
		log("--> Initializing data <--");
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		u_name = prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, ""));
		q = "abc.user.[].name.==." + u_name;
		u_id = Long.parseLong( query(q)[0] );
		
		q = "abc.ecology.[].user.+." + u_id;
		subscriptionPerm.add(q);
		subscribe( q, new CmdApplicationAdded() );

		q = "abc.user." + u_id + ".activity.=.";
		subscriptionPerm.add(q);
		subscribe( q, new CmdActivityChanged() );
	}
	
	private void getApplication( long assetId, long ptrId ) {
//        String[] fields = new String[]{ "name" };
//        String tmpResp;
//        ABC4GSDItem wip = new ABC4GSDItem("abc.application."+ptrId, assetId);  <<<---------------------
//        String wipwip;
//		for( String field : fields ) {
//			wipwip = query( "abc.application." + ptrId+ "." + field );
//			if( wipwip == null ) tmpResp = "";
//			else tmpResp = ((JSONObject) JSONValue.parse( wipwip )).get(Constants.MSG_A).toString();
//			wip.set(field, tmpResp);
//		}
		ABC4GSDItem wip = new ABC4GSDItem("abc.application", ptrId, new String[]{ "name" });
		ABC4GSDItemManager.getManager("Application").addItem(wip);
	}
	
	public void killOperation() {}
	public boolean personalHandler(String ch, String msg) { return true; }
	public void resumeOperation() {}
	public void suspendOperation() {}

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
	   // To avoid closing errors
	   if( ! (Boolean)OSGIEventHandler.getInstance().get("stateConnected?") ) return;

	   viewer.refresh();
   }

//	public void changeArtifact(long oldId, long newId) {
//		sendCommand( "ARTIFACT_LOAD " + newId );
//	}
}