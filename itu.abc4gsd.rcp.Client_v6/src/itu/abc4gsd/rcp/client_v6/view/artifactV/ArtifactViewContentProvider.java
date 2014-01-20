package itu.abc4gsd.rcp.client_v6.view.artifactV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.IHandlerService;
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
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
 import itu.abc4gsd.rcp.client_v6.preferences.Connection;
 import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;

class ArtifactViewContentProvider extends AppInterface implements IStructuredContentProvider, ABC4GSDItemManagerListener {

	/* 
	 * AppInterface
	 *  - classes responding to events arriving from the middleware 
	 */
	private class CmdUpdate implements ICommand {
		public void execute(String data) {
			log("Executing CmdUpdate");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			
			IABC4GSDItem tmp = ABC4GSDItemManager.getManager("Artifact").get(wip.id);
			tmp.update( new String[]{wip.key} );
			itemsChanged(null);
		}
	}

	private class CmdNewArtifact implements ICommand {
		public void execute(String data) {
			log("Executing CmdNewArtifact");
			RemoteMessage wip = new RemoteMessage(data);
			if( wip.id == -1) 
				return;
			
			IABC4GSDItem tmp = new ABC4GSDItem("abc.asset", wip.value, new String[]{"ptr"});
			getArtifact(tmp.getId(), Long.parseLong((String)tmp.get("ptr")));
			
			IABC4GSDItem artifact = new ABC4GSDItem("abc.artifact", tmp.get("ptr").toString(), new String[]{"name"});

			String q = "abc.artifact." + tmp.get("ptr") + ".name";							// Activity artifact name changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );

			q = "abc.artifact." + tmp.get("ptr") + ".location";						// Activity artifact location changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );

			q = "abc.artifact." + tmp.get("ptr") + ".type";							// Activity artifact type changed
			subscriptionTemp.add(q);
			subscribe( q, new CmdUpdate() );

			JSONObject content = new JSONObject();
			content.put("title", "An artifact has been added to the current activity");
			content.put("body", "Name> " + artifact.get("name") );
			sendCommand("NOTIFY " + content.toJSONString() );
			
			itemsChanged(null);
		}
	}

	private class CmdListModified implements ICommand {
		public void execute(String data) {
			log("Executing CmdListModified");

			String q;
			unsubscribe( subscriptionTemp );
			subscriptionTemp.clear();
			ABC4GSDItemManager.getManager("Artifact").initItems();

			q = "abc.ecology.[].name.==." +currentActivity+ ":" +u_id;
			String[] ecology = query(q);
			q = "abc.ecology." + ecology[0] + ".asset"; // getting list of related assets
			String[] artifacts = query(q);
			// keep artifacts
			List<String> resultsAsset = new ArrayList<String>();
			List<String> resultsPtr = new ArrayList<String>();
			ABC4GSDItem tmpItem;
			for( int x=0; x< artifacts.length; x++ ) {
				tmpItem = new ABC4GSDItem( "abc.asset", artifacts[x].toString(), new String[]{"type","ptr"} );
				if( tmpItem.get("type").equals("artifact") ) {
					resultsPtr.add( (String)tmpItem.get("ptr") );
					resultsAsset.add( artifacts[x] );
				}
			}
			
			String ecoId = query("abc.ecology.[].name.==." +currentActivity+ ":" +u_id)[0];
			
			q = "abc.ecology." + ecoId + ".asset.+";	//Artifact added to curr activity
			subscriptionTemp.add(q);
			subscribe( q, new CmdNewArtifact() );

			q = "abc.ecology." + ecoId + ".asset.-";	//Artifact removed from curr activity
			subscriptionTemp.add(q);
			subscribe( q, new CmdListModified() );
			
			for( int x=0; x< resultsPtr.size(); x++ ) {
				long artifact = Long.parseLong(resultsPtr.get(x).toString());
				long asset = Long.parseLong(resultsAsset.get(x).toString());
				
				getArtifact( asset, artifact );
				
				q = "abc.artifact." + artifact + ".name";							// Activity artifact name changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdate() );

				q = "abc.artifact." + artifact + ".location";						// Activity artifact location changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdate() );

				q = "abc.artifact." + artifact + ".type";							// Activity artifact type changed
				subscriptionTemp.add(q);
				subscribe( q, new CmdUpdate() );
			}

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

	/* 
	 * AppInterface
	 */
	private Button btnAdd;
	private Button btnUpload;
	private String u_name;
	private long u_id;
	private long currentActivity = -1;
	private long currentArtifact = 0;
	private List<String> subscriptionPerm = new ArrayList<String>();
	private List<String> subscriptionTemp = new ArrayList<String>();

	
	public ArtifactViewContentProvider(String name) {
		super(name);
		_init();
	}
	
	private void _init() {
		String q = "";
		log("--> Initializing data <--");
		ABC4GSDItemManager.getManager("Artifact").initItems();
		itemsChanged(null);
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		u_name = prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, ""));
		q = "abc.user.[].name.==." + u_name;
		u_id = Long.parseLong( query(q)[0] );
		
//		q = "abc.asset.[abc.asset.[].type.==.artifact]._id";				// Get list of all artifacts
//		JSONArray artifacts = (JSONArray) ((JSONObject) JSONValue.parse( query(q) ) ).get(Constants.MSG_A);
//		for( int x=0; x< artifacts.size(); x++ ) {
//			q = "abc.asset." + Long.parseLong(artifacts.get(x).toString()) + ".ptr";
//			String ptrId = ((JSONObject) JSONValue.parse( query(q) )).get(Constants.MSG_A).toString();
//			getArtifact( Long.parseLong(artifacts.get(x).toString()), Long.parseLong(ptrId) );
//		}
		//TODO> missing subscription for artifact added
		q = "abc.user." + u_id + ".activity.=.";
		subscriptionPerm.add(q);
		subscribe( q, new CmdActivityChanged() );
	}
	
	private void getArtifact( long assetId, long ptrId ) {
//        String[] fields = new String[]{ "name", "location", "type" };
//        String tmpResp;
//        ABC4GSDItem wip = new ABC4GSDItem("abc.artifact."+ptrId, assetId);
//        String wipwip;
//		for( String field : fields ) {
//			wipwip = query( "abc.artifact." + ptrId+ "." + field );
//			if( wipwip == null ) tmpResp = "";
//			else tmpResp = ((JSONObject) JSONValue.parse( wipwip )).get(Constants.MSG_A).toString();
//			wip.set(field, tmpResp);
//		}
        ABC4GSDItem wip = new ABC4GSDItem("abc.artifact", ptrId, new String[]{ "name", "location", "type" });
        String[] resp = query( "abc.property.[abc.property.[abc.property.[].name.==." + u_id + ":" + assetId + "].key.==.auto].value.==.true");
        String wipwip = resp.length > 0 ? resp[0] : "false";
		try { 
			Long.parseLong(wipwip); 
			wipwip = "true"; 
		} catch (Exception e) { 
			wipwip = "false"; 
		}
		wip.set("autoLoad", wipwip, false);
		wip.set("__asset__", assetId, false);
		ABC4GSDItemManager.getManager("Artifact").addItem(wip);
	}
	
	public void killOperation() {}
	public boolean personalHandler(String ch, String msg) { return true; }
	public void resumeOperation() { setBlockReactions( false ); }
	public void suspendOperation() { setBlockReactions( true ); _init(); }

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
	   if( currentActivity != -1 ) {
		   btnAdd.setEnabled(true);
		   btnUpload.setEnabled(true);
	   } else { 
		   btnAdd.setEnabled(true);
		   btnUpload.setEnabled(true);
	   }
	   viewer.refresh();
   }

	public void changeArtifact(long oldId, long newId) {
		sendCommand( "ARTIFACT_LOAD " + newId );
	}

	public void setCheckedState(ABC4GSDItem element, boolean checked) {
		String q;
		String[] resp;
		// Check id property is actually there
		q = "abc.property.[].name.==." + u_id + ":" + element.get("__asset__");
		resp = query(q);
		if( resp.length == 0 ) return;
		q = "";
		for( String tmp : resp )
			q += tmp + ",";
		query( "abc.property.[abc.property.[" + q.substring(0, q.length()-1) + "].key.==.auto].value.=." + checked );
	}

   public void operationDelete(IABC4GSDItem element) {
	   System.out.println("Delete " + element.get("name")); 
		// Confirmation required
		MessageDialog messageDialog = new MessageDialog(viewer.getControl().getShell(), "MessageDialog", null,
		        "Please confirm the removal of " + element.get("name") + " from the current activity. The artifact will be saved and closed if loaded.", MessageDialog.CONFIRM,
		        new String[] { "Confirm", "Cancel" }, 0);
		if (messageDialog.open() != 0) 
			return;

		// Killing the artifact if loaded ... but is it loaded?
		sendCommand( "ARTIFACT_KILL " + element.get("__asset__") );

		// Removing link in ecology and asset
		query("abc.ecology.[abc.ecology.[].name.~=.{{("+ currentActivity +"):[0-9]*}}].asset.-."+ element.get("__asset__"));
		query("abc.asset.-."+ element.get("__asset__"));
   }
   public void operationLoad(IABC4GSDItem item) {
	   System.out.println("Load " + item.get("name")); 
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if(item != null) {
			Long wip = Long.parseLong(item.get("__asset__").toString());
			System.out.println("Selected : "+ wip + " - " + item.get("name") );
			// crea property
			changeArtifact( currentArtifact, wip );
			currentArtifact = wip;
		}
   }
   
	public void operationAdd() {
		// Getting list of artifacts not in the activity
		
		List<String> currentArtifacts = Arrays.asList( query( "abc.asset.[abc.ecology.[abc.ecology.[].name.==." +currentActivity+ ":" +u_id + "].asset].ptr" ) );
		List<String> availableArtifacts = new ArrayList<String>(Arrays.asList( query( "abc.artifact.[]._id" ) ));
		
		availableArtifacts.removeAll(currentArtifacts);
		
		String[] artifactsId = availableArtifacts.toArray(new String[availableArtifacts.size()]);
		String[] artifactsName = new String[ artifactsId.length ]; 
		for( int i=0; i<artifactsId.length; i++ )
			artifactsName[i] = query( "abc.artifact." + artifactsId[i] + ".name" )[0];
		
		// Create the dialog box for the selection
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), new LabelProvider());
		dialog.setElements(artifactsName);
		dialog.setMultipleSelection(true);
		dialog.setTitle("Select the artifact(s) to add");
		if (dialog.open() != Window.OK)
			return;
		Object[] tmp = dialog.getResult();
		String[] selected = Arrays.copyOf(tmp, tmp.length, String[].class);
		
		List<String> names = Arrays.asList( artifactsName );
		String currUID;
		for( String u : selected ) {
			currUID = artifactsId[ names.indexOf(u) ];
			
			// ... creating Asset
			IABC4GSDItem newAsset = new ABC4GSDItem( "abc.asset" );
			newAsset.set( "type", "artifact" );
			newAsset.set( "ptr", currUID );
			
			// ... updating ecology
			String[] ecologies = MasterClientWrapper.getInstance().query(
					"abc.ecology.[].activity.==."+ 
					MasterClientWrapper.getInstance().getCurrentActivity() );
			IABC4GSDItem tmpEco = null;
			for( String s : ecologies ) {
				tmpEco = new ABC4GSDItem( "abc.ecology", s );
				tmpEco.update();
				String usr = ((ArrayList<Long>)tmpEco.get("user")).get(0).toString();
				MasterClientWrapper.getInstance().attachNewAsset(tmpEco, ""+newAsset.getId(), usr );
			}

		}
	}
	
	public void operationUpload() {
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		try {
			handlerService.executeCommand("itu.abc4gsd.rcp.client_v6.command.addArtifact", null);
		} catch (Exception ex) {
			throw new RuntimeException("itu.abc4gsd.rcp.client_v6.command.addArtifact not found");
		}
	}
   
	public void linkAddBtn( Button btn ) { this.btnAdd = btn; }
	public void linkUploadBtn( Button btn ) { this.btnUpload = btn; }
}


