package itu.abc4gsd.rcp.client_v6.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.dialog.CreateActivityDialog;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityAsset;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityElement;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityInformation;
import itu.abc4gsd.rcp.client_v6.preferences.ConnectionAdvanced;
import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewH;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class MasterClientWrapper {
	private ConnectionManager connectionManager = null;
	private MasterClientBackEnd _handler = null;
	
	private static MasterClientWrapper INSTANCE;
	public static MasterClientWrapper getInstance() {
		if (INSTANCE == null)
			INSTANCE = new MasterClientWrapper();
		return INSTANCE;
	}

	private MasterClientWrapper() {
//		newLogger = Logger(self.connectionManager.logger)
//		# Remove to start the logging
//		sys.stdout = newLogger
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String[] addrs = new String[] { 
				prefs.get(ConnectionAdvanced.ADDR_BACKEND, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_BACKEND, "ERROR")), 
				prefs.get(ConnectionAdvanced.ADDR_LOGGER, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_LOGGER, "ERROR")), 
				prefs.get(ConnectionAdvanced.ADDR_PUBLISHER, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_PUBLISHER, "ERROR")),
				prefs.get(ConnectionAdvanced.ADDR_CONTROL, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_CONTROL, "ERROR")) };
		
		_handler = new MasterClientBackEnd( addrs );
		connectionManager = _handler.getConnectionManager();
	}
	
	public MasterClientBackEnd _getHandler() { return _handler; }

	public void log( String msg ) { connectionManager.send(connectionManager.logger, msg); }

	/*
	 * ActivityManager functionalities
	 */
	public String connect( String name, String model ) {
		String wip = "";
		InternalMessage tmp = null;
//		self._handler._clearGUI()
		tmp = _handler.connect(name, model);
		for( String entry : _handler.getModel() ) {
			if( wip.length() > 0 ) wip += " ";
			wip += entry;
		}
		return tmp.message;
		// TODO what if no connection ... handle here
		// TODO update gui in response to true
//		self._handler._frames['main'].SetStatusText(self._cfg.get('General', 'ServerIp') + ':' + str(self._cfg.get('General', 'ServerPort')), 1)
//		self._handler._frames['main'].SetStatusText(self._cfg.get('General', 'UserName'), 2)
//		self._handler._frames['main'].SetStatusText(repr(self._handler._model), 3)
	}

	public String disconnect( String model ) {
		InternalMessage tmp = null;
		tmp = _handler.disconnect(model);
		log( tmp.message );
		return tmp.message;
		// TODO what if no connection ... handle here
		// TODO update gui in response to true
//		self._handler._frames['main'].SetStatusText(repr(self._handler._model), 3)
	}
	
	public String setServer( String addr, String port) {
		//TODO need to restart/start connections
//		self._log(self._handler.setServer( addr, port ) )
		return "";
	}
	
	public JSONObject run( String msg ) {
		InternalMessage tmp = null;
		tmp = _handler.run(msg);
		return tmp.data;
	}
	
	public String[] query( String msg ) { return _handler.query(msg); }
	public String[] query( String msg, String model ) { return _handler.query( msg, model ); }
	
	public long getMyId() { return _handler._me; }
	public long getCurrentActivity() { return _handler._currAct; }

	public void quickShutDown() {
		BundleContext ctx = FrameworkUtil.getBundle(MasterClientWrapper.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        eventAdmin.postEvent( new org.osgi.service.event.Event("State/Disconnected", new HashMap<String, Object>()) );

        query( "abc.user." + getMyId() + ".state.=." + Constants.USR_DISCONNECTED );
		System.out.println( "## User status set ...");
		
		if( getCurrentActivity() != -1 ) {
			_handler.suspend();
			System.out.println( "## Activity status set ...");
		}
	}
	

	public long createActivity( String currentMode, String activity, ABC4GSDActivityInformation preInfo, ABC4GSDActivityInformation info ) {
		Long id;
		// Create activity
		IABC4GSDItem newAct;
		if( currentMode.equals(CreateActivityDialog.MODE_CREATE) ) {
			newAct = new ABC4GSDItem( "abc.activity" );
			preInfo.users = new ABC4GSDActivityElement[]{};
		} else if( currentMode.equals(CreateActivityDialog.MODE_CLONE) ) {
			newAct = new ABC4GSDItem( "abc.activity" );
			preInfo = new ABC4GSDActivityInformation();
			info.creator = MasterClientWrapper.getInstance().getMyId();
		} else if( currentMode.equals(CreateActivityDialog.MODE_CREATE_SUB) ) {
			newAct = new ABC4GSDItem( "abc.activity" );
			preInfo = new ABC4GSDActivityInformation();
			info.creator = MasterClientWrapper.getInstance().getMyId();
		} else {
			newAct = new ABC4GSDItem( "abc.activity", activity );
			newAct.update();
		}
		
		// Setting the straightforward fields if modified (in case of new activity the base is empty field)
		if( !info.name.equals(newAct.get("name")) ) newAct.set("name", info.name);
		// Put unique names for copy and sub
		if( currentMode.equals(CreateActivityDialog.MODE_CREATE_SUB) )
			newAct.set("name", newAct.get("name") + ". " + newAct.getId());
		if( currentMode.equals(CreateActivityDialog.MODE_CLONE) )
			newAct.set("name", newAct.get("name") + ". " + newAct.getId());
//		TODO> check
		if( !info.description.equals(newAct.get("description")) ) newAct.set("description", info.description);
		if( ((String)newAct.get("state")).length()==0 ) newAct.set("state", Constants.ACT_UNKNOWN);
		if( !(""+info.creator).equals(newAct.get("creator")) ) newAct.attach("creator", info.creator);

		// Creating the relations
		if( preInfo.superActivity.length() > 0 && !info.superActivity.equals(preInfo.superActivity) )
			MasterClientWrapper.getInstance().query("abc.relation.-.[abc.relation.[].name.==."+preInfo.superActivity+":"+newAct.getId()+"]");
		if( info.superActivity.length() > 0 && !info.superActivity.equals(preInfo.superActivity) ) {
			IABC4GSDItem newRelation = new ABC4GSDItem( "abc.relation" );
			newRelation.set("name", info.superActivity+":"+newAct.getId());
			newRelation.set("type", 0);
			newRelation.attach("from", info.superActivity);
			newRelation.attach("to", newAct.getId());
		} 
		
		// Checking if users have been removed
		for( ABC4GSDActivityElement wip : preInfo.users ) {
			boolean found = false;
			// Checking if user is still in ...
			for( ABC4GSDActivityElement u : info.users )
				if( wip.getId() == u.getId() ) {
					found = true;
					break;
				}
			// ... if not remove state/info relation class instance and ecology
			if( !found ) {
				MasterClientWrapper.getInstance().query("abc.state.-.[abc.state.[].name.==."+newAct.getId()+":"+wip.getId()+"]");
				MasterClientWrapper.getInstance().query("abc.ecology.-.[abc.ecology.[].name.==."+newAct.getId()+":"+wip.getId()+"]");
				// TODO > Properties are still there
				// NOTE > If the last user was removed, the activity should not be created and this is taken care above at the very beginning.
			}
		}
		// Adding new user in a similar manner
		for( ABC4GSDActivityElement wip : info.users ) {
			boolean found = false;
			for( ABC4GSDActivityElement u : preInfo.users )
				if( wip.getId() == u.getId() ) {
					found = true;
					break;
				}
			if( !found ) {
				IABC4GSDItem newState = new ABC4GSDItem( "abc.state" );
				id = newState.getId();
				newState.set( "name", newAct.getId() + ":" + wip.getId() );
				newState.set( "state", Constants.STATE_UNKNOWN );
				newState.attach( "activity", newAct.getId() );
				newState.attach( "user", wip.getId() );
			}
		}
		
		// Wrap artifacts with assets. Same as with users.
		for( ABC4GSDActivityAsset wip : preInfo.assets ) {
			boolean found = false;
			for( ABC4GSDActivityAsset u : info.assets )
				if( wip.getArtifactId() == u.getArtifactId() ) {
					found = true;
					break;
				}
			if( !found ) {
				// TODO > Properties are still there
				MasterClientWrapper.getInstance().query("abc.ecology.[abc.ecology.[].name.~=.{{("+newAct.getId()+"):[0-9]*}}].asset.-."+wip.getId());
				MasterClientWrapper.getInstance().query("abc.asset.-."+wip.getId());
			}
		}

		List<String> assets = new ArrayList<String>(); 
		for( ABC4GSDActivityAsset wip : info.assets ) {
			boolean found = false;
			for( ABC4GSDActivityAsset u : preInfo.assets )
				if( wip.getArtifactId() == u.getArtifactId() ) {
					found = true;
					break;
				}
			if( !found ) {
				IABC4GSDItem newAsset = new ABC4GSDItem( "abc.asset" );
				newAsset.set( "type", "artifact" );
				newAsset.set( "ptr", wip.getArtifactId() );
				wip.setId( newAsset.getId() );
				assets.add( ""+newAsset.getId() );
			}
		}
		
		// Bind everything with Ecology entities
		for( ABC4GSDActivityElement user : info.users ) {
			boolean found = false;
			for( ABC4GSDActivityElement u : preInfo.users )
				if( user.getId() == u.getId() ) {
					found = true;
					break;
				}
			if( !found ) {
				IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology" );
				newEcology.set( "name", newAct.getId() + ":" + user.getId() );
				newEcology.attach( "activity", newAct.getId() );
				newEcology.attach( "user", user.getId() );
				for( String asset : assets ) 
					attachNewAsset( newEcology, asset, ""+user.getId() );
			} else {
				IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology", MasterClientWrapper.getInstance().query("abc.ecology.[].name.==."+newAct.getId()+":"+user.getId())[0] );
				for( String asset : assets ) { 
					boolean found2 = false;
					for( ABC4GSDActivityAsset u : preInfo.assets )
						if( asset.equals(""+u.getId()) ) {
							found2 = true;
							break;
						}
					if( !found2 ) 
						attachNewAsset( newEcology, asset, ""+user.getId() );
				}
			}
		}
		// If activity state is unknown, set to initialized
		// This is to separate the creation stage from the period in which it has not been resumed
		if( ((String)newAct.get("state")).equals( Constants.ACT_UNKNOWN ) ) newAct.set("state", Constants.ACT_INITIALIZED);
		
		BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        
		// Sending Events
		if( currentMode.equals(CreateActivityDialog.MODE_CREATE) )
	        eventAdmin.postEvent( new Event("State/Act_Created", new HashMap<String, Object>()) );

		// return the id of the Activity
		return newAct.getId();
	}
	// attach the asset and creates the property
	public void attachNewAsset( IABC4GSDItem ecology, String asset, String userId ) {
		ecology.attach( "asset", asset );
		// creating property for telling if resource has to be loaded on resume
		// NOTE > to change this parameter the artifact view needs to be used
		IABC4GSDItem property = new ABC4GSDItem("abc.property");
		property.attach("asset", asset);
		property.attach("user", userId);
		property.set("name", userId + ":" + asset);
		property.set("key", "auto");
		property.set("value", "false");
	}
	
	public ABC4GSDActivityElement[] getUsers(  ) {
		String[] ids;
		String support;
		ABC4GSDActivityElement[] resp;
		
		support = "abc.user";
		ids = MasterClientWrapper.getInstance().query(support);
		resp = new ABC4GSDActivityElement[ ids.length ];
		for( int i=0; i< ids.length; i++ ) {
			support = MasterClientWrapper.getInstance().query( "abc.user."+ids[i]+".name" )[0];
			resp[i] = new ABC4GSDActivityElement(ids[i], support);
		}    					
		return resp;
	}
	
	public ABC4GSDActivityAsset[] getAssets() {
		String[] ids;
		String support;
		ABC4GSDActivityAsset[] resp;
		
		support = "abc.artifact";
		ids = MasterClientWrapper.getInstance().query(support);
		resp = new ABC4GSDActivityAsset[ ids.length ];
		for( int i=0; i< ids.length; i++ ) {
			support = MasterClientWrapper.getInstance().query( "abc.artifact."+ids[i]+".name" )[0];
			resp[i] = new ABC4GSDActivityAsset(""+-1, ids[i], support);
		}
		return resp;
	}
	
	public int getOnlineParticipant( long actId ) { 
		String q = "abc.activity." + actId + ".active";
		String[] resp = MasterClientWrapper.getInstance().query(q);
		return resp.length; 
	}


}

/*
	





def __initGUI(self):
	if self._mode == -1:
		# normal usage
		self._log('Normal usage')
		for model in self._cfg.get('General', 'model').split(','):
			if len(model):            
				self._log( self._handler.connect( self._cfg.get('General', 'UserName'), model ))
				if self._handler._me == None:
					return
		self._executeGUI()
	
	elif self._mode == 1:
		# ctrl - only text for query 
		self._log('Ctrl usage')
	
	elif self._mode == 0:
		# shift - ?
		self._log('Shift usage')

def _executeGUI(self):
	folder = self._cfg.get('General', 'frameFolder')
	toLoad = self._cfg.items('Frames')
	for name, file in toLoad:
		if name in ['activities']:
			self._handler._execute(name,'python', [os.path.join(folder, file)])                

def CloseGUI(self):
	self._handler.suspend(True)
	time.sleep(1)
	for x in self._handler._model:
		self.disconnect(x)
	return


*/