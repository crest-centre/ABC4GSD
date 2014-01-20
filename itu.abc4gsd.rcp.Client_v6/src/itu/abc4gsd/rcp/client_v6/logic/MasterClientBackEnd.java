package itu.abc4gsd.rcp.client_v6.logic;

//import socket
//from threading import Thread
//import os
//import copy
//import time
//import subprocess
//import json, zmq
//
//import library.utils.utils as UT
//import library.constants as CO

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.Utils;
import itu.abc4gsd.rcp.client_v6.preferences.ConnectionAdvanced;
import itu.abc4gsd.rcp.client_v6.preferences.Needs;
import itu.abc4gsd.rcp.client_v6.view.abc4gsdPopUpNotification;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Display;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.zeromq.ZMQ;


public class MasterClientBackEnd {
	class StreamGobbler extends Thread
	{
	    InputStream is;
	    String type;
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this.is = is;
	        this.type = type;
	    }
	    
	    public void run()
	    {
	        try
	        {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	                System.out.println(type + ">" + line);    
	            } catch (IOException ioe)
	              {
	                ioe.printStackTrace();  
	              }
	    }
	}

	private class ApplicationDescriber {
		public String resourceId;
		public final String realId;
		public final String name;
		public final String artifact;
		public final String[] command;
		public final boolean hasInterface;
		public final boolean independent; // no need for versioning
		public final String appAssetId;
		public int pid;
		
		public ApplicationDescriber( String resourceId, String realId, String name, String artifact, String[] command, String interf, boolean independent, String appAssetId ) {
			this.resourceId = resourceId;
			this.realId = realId;
			this.name = name;
			this.artifact = artifact;
			this.command = command;
			this.hasInterface = interf.toLowerCase().equals("true");
			this.independent = independent;
			this.appAssetId = appAssetId;
		}
		public ApplicationDescriber( ApplicationDescriber origin ) {
			this.resourceId = origin.resourceId;
			this.realId = origin.realId;
			this.name = origin.name;
			this.artifact = origin.artifact;
			this.command = Arrays.copyOf(origin.command, origin.command.length, String[].class);;
			this.hasInterface = origin.hasInterface;
			this.independent = origin.independent;
			this.appAssetId = origin.appAssetId;	
		}
	}
	
	private static final boolean DBG = true;
	
	private final Lock lock = new ReentrantLock();
	ConnectionManager connectionManager;
	private String[] addrs;
	private boolean resumeInProgress;
	protected long _currAct = -1;
	protected long _me = -1;
	protected int _confirmation = 0;
	protected List<String> _assetToKeep = new ArrayList<String>();
	protected List<String> _actApp = new ArrayList<String>();
	protected List<String> _model = new ArrayList<String>();
	protected HashMap<String, ApplicationDescriber> _appId = new HashMap<String, ApplicationDescriber>();
	protected HashMap<String, ApplicationDescriber> _frames = new HashMap<String, ApplicationDescriber>();
	protected HashMap<String, String[]> _initInfo = new HashMap<String, String[]>();

	private void initManager() {
		connectionManager = new ConnectionManager( addrs );
	}

	private void initReceiver() {
		Executor executor = Executors.newFixedThreadPool(1);
		executor.execute( new Runnable() { public void run() {  __receive(); } } );
	}
	
	public MasterClientBackEnd( String[] addrs ) {
//			self._checkConfirmation = None
		this.addrs = addrs;
		initManager();
		initReceiver();
	}
	
	/*
	 * Connection functions
	 */
	
	private void __receive() {
		connectionManager.locking += 1;
		try {
			ZMQ.Poller poller = connectionManager.getContext().poller(1);
			poller.register( connectionManager.control, ZMQ.Poller.POLLIN );
			while (! connectionManager.close ) {
				poller.poll(); // pass 1000?
				if( poller.pollin(0) ) {
					String data = (String)connectionManager.recv( connectionManager.control, false );
					//TODO log this
					System.out.println( "Received> " + data );
					JSONObject resp = _handleLocalRequest( data );
					connectionManager.send( connectionManager.control, (String) resp.get(Constants.MSG_A) );
				}
			}
			connectionManager.locking -= 1;
		} catch (Exception e) {
			// TODO > here it's a mess
			connectionManager.locking -= 1;
			connectionManager.close();
			initManager();
			initReceiver();
		}
	}

	/*
	 * Applications communication
	 */
	
	@SuppressWarnings("unchecked")
	private JSONObject _handleLocalRequest( String wip ) {
		JSONObject resp = new JSONObject();
		resp.put(Constants.MSG_A, "");
		StringTokenizer msg = new StringTokenizer( wip, " " );
		List<String> data = new ArrayList<String>();
		while( msg.hasMoreTokens() )
			data.add(msg.nextToken());
		String remaining = wip.substring( data.get(0).length() ).trim();

		if( data.get(0).equals( "ABC" ) ) {
			if( data.get(1).equals( "RESUME" ) && data.get(2).equals( "COMPLETED" ) ) 
				_confirmation -= 1; 
			if( data.get(1).equals( "SUSPEND" ) && data.get(2).equals( "COMPLETED" ) ) 
				_confirmation -= 1;
			if( data.get(1).equals( "KILL" ) && data.get(2).equals( "COMPLETED" ) ) 
				_confirmation -= 1;
		}
		try {
			if( data.get(0).equals( "INIT" ) ) {
				resp = new JSONObject();
				resp.put("a", _initApplication( remaining ));
			}
			if( data.get(0).equals( "RESUME" ) ) {
				_resumeActivity( Long.parseLong(remaining) );
				_openChat( Long.parseLong(remaining) );
			}
			if( data.get(0).equals( "SUSPEND" ) ) 
				_suspendActivity( Long.parseLong(remaining) );
			if( data.get(0).equals( "ARTIFACT_LOAD" ) )
				_loadArtifact( Long.parseLong(remaining) );
			if( data.get(0).equals( "ARTIFACT_KILL" ) )
				_killAsset( Long.parseLong(remaining), true );
			if( data.get(0).equals( "ARTIFACT_STORE" ) )
					_storeArtifact( Long.parseLong(remaining) );
			if( data.get(0).equals( "QUERY" ) ) {
				// model = msg[1].split(' ', 1)[0]
				// q = msg[1].split(' ', 1)[1]
				resp = _query( wip );
			}
			if( data.get(0).equals("NOTIFY") )
				_notify( remaining );
			if( data.get(0).equals("CHAT_OPEN") )
				_openChat( Long.parseLong(remaining) );
			if( data.get(0).equals("CHAT_WRITE") )
				_writeInChat( remaining );
		} catch (Exception e) { e.printStackTrace(); }
		return resp;		
	}
	
	private JSONObject _query( String msg ) {
		InternalMessage recv = null;
		lock.lock();
		try {
			_send( msg );
			recv = _receive();
		} finally {
			lock.unlock();
		}
		return recv.data;
	}
	
	protected String[] query( String query ) { return query( query, "abc" ); }
	protected String[] query( String query, String model ) {
		String q = "QUERY " + model + " " + query;
		Object tmp = _query(q);
		List<String> wip = new ArrayList<String>();
		tmp =  ((JSONArray)((JSONObject)tmp).get(Constants.MSG_A)).get(2);
		if( tmp instanceof JSONArray )
			for( int x=0; x<((JSONArray)tmp).size(); x++ )
				wip.add( ((JSONArray)tmp).get(x).toString() );
		else if ( tmp == null ) 
			return new String[]{};
		else 
			wip.add( tmp.toString() );
		return wip.toArray( new String[wip.size()]);
	}
	
	private void _resumeActivity( long actId ) {
		System.out.println( "Resuming ... " + actId );
		resumeInProgress = true;
		// take all application 
		// launch them by sending also uid and act id
		
		//if( _chekcConfirmation != null)
		//	if self._checkConfirmation != None:
		//		print "Still up - %s" % (self._confirmation,) 
		//		self._checkConfirmation[0](self._checkConfirmation[1])
		//		self._checkConfirmation = None

		String msg = "CMD RESUME ALL";
		connectionManager.send( connectionManager.publisher, msg );

		
		// Check if you are user of selected activity, if not ask to be linked
		String[] users = MasterClientWrapper.getInstance().query( "abc.user.[abc.state.[abc.state.[].activity.==." +actId+ "].user]._id" );
		boolean present = false;
		for( String wip : users )
			if( wip.equals( ""+_me ) )
				present = true;
		if( ! present ) {
			// Creating the state
			IABC4GSDItem newState = new ABC4GSDItem( "abc.state" );
			newState.set( "name", actId + ":" + _me );
			newState.set( "state", Constants.STATE_UNKNOWN );
			newState.attach( "activity", actId );
			newState.attach( "user", _me );
			// Creating the ecology
			String[] assets = MasterClientWrapper.getInstance().query("abc.ecology.[abc.ecology.[].name.~=.{{("+actId+"):[0-9]*}}].asset");
			IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology" );
			newEcology.set( "name", actId + ":" + _me );
			newEcology.attach( "activity", actId );
			newEcology.attach( "user", _me );
			for( String asset : assets ) 
				newEcology.attach( "asset", asset );
		}
		
		String q;

		// Set activity state to correct value with current user
		q = "abc.state.[abc.state.[].name.==." + actId + ":" + _me + "].state.=." + Constants.STATE_RESUMED;
		query( q );
		q = "abc.activity." + actId + ".active.+." + _me;
		query( q );

		// Set overall activity state to correct value 
		ABC4GSDItem activity = new ABC4GSDItem("abc.activity", actId, new String[]{"state"});
		if( activity.get("state").equals(Constants.ACT_INITIALIZED.toString()) ) {
			activity.set("state", Constants.ACT_ONGOING);
		}

		// build needed artifacts list
		// if _currAct != from -1 
		if( _currAct != -1 ) {
			buildToKeepList(actId);
			_suspendActivity(_currAct);
		}
		
		// Update for last-used-activity purposes
		_currAct = actId;
		ABC4GSDItem user = new ABC4GSDItem("abc.user", _me);
		user.set("activity", actId);
		
		_resumeAssets();
		resumeInProgress = false;
	}
	
	private String[] getArtifactAsset( long actId ) {
		String q;
		String[] ids;
	
		// Get ids of assets that are artifacts
		q = "abc.ecology.[].name.==." + actId + ":" + _me;
		ids = query( q );
		if( ids.length == 0 ) return ids;
		
		q = "abc.asset.[abc.ecology." + generateQueryString(ids) + ".asset].type.==.artifact";
		ids = query( q );
		return ids;
//		ids = ids.replace("\"", "");
//		ids = ids.replace("[", "");
//		ids = ids.replace("]", "");
//		if( ids.equals("") ) return new String[]{};
//		return ids.split(",");
	}
	
	private void buildToKeepList( long newActId ) {
		HashMap<String, String> oldArti = new HashMap<String, String>();
		String[] nextAsset;

		for( Map.Entry<String, ApplicationDescriber> e : _frames.entrySet() )
			oldArti.put(e.getValue().realId, e.getValue().resourceId);
			
		nextAsset = getArtifactAsset(newActId);
		String q, resp, resp2;		
		_assetToKeep = new ArrayList<String>();
		for( String wip : nextAsset ) {
			q = "abc.asset." + wip + ".ptr";
			resp = query(q)[0];
			
			if( oldArti.containsKey(resp) ) {
				q = "abc.property.[abc.property.[abc.property.[].name.==." + _me + ":" + wip + "].key.==.auto].value";
				// Why is it returning true here and not the id? See #306
				resp2 = query(q)[0];
				if( resp2.toLowerCase().equals("true") ) 
					_assetToKeep.add(resp);		// contains id of artifacts pointed
			}
		}
	}
	
	private void _resumeAssets() {
//		abc.asset.[abc.ecology.[abc.ecology.[].name.==.168081276:168080412].asset].type.==.artifact
//		abc.asset.[abc.asset.[abc.ecology.[abc.ecology.[].name.==.{{159282924:159282492}}].asset].type.==.artifact].ptr
		String q;
		String[] ids, tmp;
		String[] assetIds = getArtifactAsset(_currAct);

		for( int x=0; x<assetIds.length; x++ ) {
			// here it should check artifacts ids not assets
			if( _frames.containsKey(assetIds[x]) ) 
				continue;
			// the property might not be there
			// ... if it is there then the key==auto is there
			q = "abc.property.[].name.==." + _me + ":" + assetIds[x];
			tmp = query(q);
			if( tmp.length == 0 ) return;
			// constructing the list of properties of this asset
			q = "";
			for( String wip : tmp )
				q += wip + ",";
			q = "abc.property.[abc.property.[" + q.substring(0,q.length()-1) + "].key.==.auto].value";
			ids = query(q);
//			ids = ((JSONArray)((JSONArray)query( q ).get(Constants.MSG_A)).get(2)).get(0).toString();
			if((ids.length > 0) && (ids[0].toLowerCase().equals("true") )) _loadArtifact(Long.parseLong(assetIds[x]));
		}
	}
	
	private void _suspendActivity( long actId ) {
		System.out.println( "Suspending ... " + actId );
		query( "abc.state.[abc.state.[].name.==." + actId + ":" + _me + "].state.=." + Constants.STATE_SUSPENDED );
		query( "abc.activity." + actId + ".active.-." + _me );

		// send suspend to processes
		if( !resumeInProgress ) {
			String msg = "CMD SUSPEND ALL";
			connectionManager.send( connectionManager.publisher, msg );
		}

		// kill apps with no interface connection
		killApplications();
		pushArtifacts();
		
		// remove info about artifact
		query( "abc.user." + _me + ".artifact.=.-1" );
		query( "abc.user." + _me + ".activity.=.-1" );
		System.out.println( "Suspension done!" );
	}

	private void _killAsset( String assetId, boolean single ) { _killAsset( Long.parseLong(assetId), single ); }
	private void _killAsset( long assetId, boolean single ) {
		System.out.println( "Killing asset ... " + assetId );

		// Storing and killing specific app/art
		Runtime run = Runtime.getRuntime();
		Process pr;
		String[] cmd;
		String cmd2;
		List<String> toRemove = new ArrayList<String>();
		ApplicationDescriber entry = _frames.get(""+assetId);
		// No record of the application to be killed
		if( entry == null ) return;
		// To avoid Eclipse to shut down 
		if( ! entry.hasInterface ) {
			if( _appId.containsKey(assetId) ) {
				cmd = Utils.terminateProcessCommand( ""+_appId.get(assetId).pid );
				_appId.remove(assetId);
			} else {
				cmd = Utils.terminateWindowCommand( entry.name, entry.artifact );
			}
			cmd2 = "";
			for( String wipwip : cmd ) {
				if( cmd2.length() > 0 ) cmd2 += " ";
				cmd2 += wipwip;
			}
			System.out.println( cmd2 );
			try {
				pr = run.exec( cmd );
				pr.waitFor();
			} catch (IOException e) { e.printStackTrace(); } catch (InterruptedException e) { e.printStackTrace(); }
		} else
			if( _appId.containsKey(assetId) ) _appId.remove(assetId);				
		_frames.remove( ""+assetId );
		if( single ) pushArtifacts();
	}

	private void _loadArtifact( long artifactId ) {
		// it is asset id
		System.out.println( "Loading artifact ... " + artifactId );

		String q;
		q = "abc.user."+_me+".artifact.=."+artifactId;
		query(q);
		q = "abc.asset." + artifactId + ".ptr";
		String realId = query(q)[0];

		for( Map.Entry<String, ApplicationDescriber> e : _frames.entrySet() )
			// if the artifact is found in the frames 
			// ... update the descriptor and return
			if( e.getValue().realId.equals(realId) ) {
				_frames.remove(e);
				ApplicationDescriber tmp = new ApplicationDescriber(e.getValue());
				tmp.resourceId = ""+artifactId;
				_frames.put(tmp.resourceId, tmp );
				return;
			}

		// else create the new frame
		String location, name, type;
		boolean independent;
		ABC4GSDItem asset = new ABC4GSDItem( "abc.asset", artifactId, new String[]{"ptr"} );
		realId = asset.get("ptr").toString();
		ABC4GSDItem artifact = new ABC4GSDItem( "abc.artifact", realId, new String[]{"name","location","independent","type"} );
		name = artifact.get("name").toString();
		location = artifact.get("location").toString();
		independent = artifact.get("independent").toString().toLowerCase().equals("true");
		type = artifact.get("type").toString();
		
		if( !independent ) getUpdatedArtifact( location, name );
		else checkLocation( location, name );
		launchArtifact( location, name, type, ""+artifactId, independent, realId );
	}
	
	private void checkLocation( String location, String name ) {
		if( !location.startsWith("git://") ) return;
		// the name needs to be the same as the ending of location for git
		File f = new File( Platform.getLocation().toFile(), name );
		Runtime run = Runtime.getRuntime();
		Process pr;
		BufferedReader buf;
		String wip, line;
		if( !f.exists() ) {
			try {
				wip = "git clone " + location;
				pr = run.exec( wip, null, Platform.getLocation().toFile() );
				pr.waitFor();
				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				while ((line=buf.readLine())!=null)
					System.out.println(line);
			} catch (IOException e) { e.printStackTrace(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		} else {
			try {
				pr = run.exec( "git pull", null, f );
				pr.waitFor();
				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				while ((line=buf.readLine())!=null)
					System.out.println(line);
			} catch (IOException e) { e.printStackTrace(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	private void getUpdatedArtifact( String location, String name ) {
//		TODO > make it specific for the file
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
		File f = new File( Platform.getLocation().toFile(), baseDir );
		Runtime run = Runtime.getRuntime();
		Process pr;
		BufferedReader buf;
		String wip, line;
		if( !f.exists() ) {
			try {
				
				wip = "git clone " + prefs.get(ConnectionAdvanced.ADDR_REPOSITORY, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_REPOSITORY, ""));
				pr = run.exec( wip, null, Platform.getLocation().toFile() );
				pr.waitFor();
				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				while ((line=buf.readLine())!=null)
					System.out.println(line);
			} catch (IOException e) { e.printStackTrace(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		} else {
			try {
				pr = run.exec( "git pull", null, f );
				pr.waitFor();
				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				while ((line=buf.readLine())!=null)
					System.out.println(line);

				
			} catch (IOException e) { e.printStackTrace(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		// TODO> understand better this horrible hack.
		File[] fList = f.listFiles();
		for( File xx : fList )
			if( xx.getName().startsWith(".~lock") )
				xx.delete();
		System.out.println( f.toString() );
	}
	
	private void launchArtifact( String location, String name, String type, String resourceId, boolean independent, String realId ) {
		String q, need;
		String[] propertyId, wip;
		String appAssetId = "";
		q = "abc.property.[].name.==." + _me + ":" + resourceId;
		propertyId = query(q);
		if( propertyId.length == 1 ) { // only the autoload
			launchArtifactFirst(location, name, type, resourceId);
			q = "abc.property.[].name.==." + _me + ":" + resourceId;
			propertyId = query(q);
		}
		q = "abc.property.[abc.property." + generateQueryString(propertyId) + ".key.==.needId].value";
		
		wip = query(q);
		if( wip != null && wip.length > 0 ) {
			appAssetId = wip[0];
			q = "abc.application.[abc.asset." + generateQueryString(wip) + ".ptr].need";
			wip = query(q);
			need = wip.length > 0 ? wip[0] : "";
		} else need = "";
//		need = ((JSONArray)((JSONArray)query(q).get(Constants.MSG_A)).get(2)).get(0).toString();
		
		String[] needInfo = getNeedInfo(need);
		String[] wip2;
		wip2 = needInfo[2].split(" ");
		if( needInfo.length > 3 )
			for( int x=0; x<wip2.length; x++ )
				if( wip2[x].equals(needInfo[3]) )
					if(!independent) wip2[x] = location;
					else wip2[x] = (new File( Platform.getLocation().toFile(), name)).toString();
		
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
		File f;		
		if( !independent ) {
			location = (new File(location)).getName();
			f = new File( Platform.getLocation().toFile(), baseDir );
		} else {
			f = null;
		}
		ApplicationDescriber describer = new ApplicationDescriber(resourceId, realId, needInfo[0], location, wip2, needInfo[1], independent, appAssetId);
		
		if( !_assetToKeep.contains(describer.realId) )
			_execute( describer, f );
	}

	private String[] getNeedInfo( String need ) {
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		Ini ini = new Ini();
		String command = "";
		String[] parameters = null;
		String name = "";
		String interf = "";
		
		// loading general need
		// TODO > load previously linked needAsset
		try {
			ini.load( new ByteArrayInputStream(prefs.get(Needs.NEEDS, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.NEEDS, "ERROR")).getBytes() ) );
			Map<String, String> map;
			map = ini.get("needs");
			if( !map.containsKey(need) ) need = "*";
			map = ini.get( map.get(need) );
			command = map.get("command");
			name = map.get("name");
			interf = map.get("interface");
			parameters = new String[ Integer.parseInt(map.get("paramNr")) ];
			for( int x=0; x< parameters.length; x++ )
				parameters[x] = map.get("param" + x);
		} catch (InvalidFileFormatException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		
		String[] resp = new String[ parameters.length + 3 ];
		resp[0] = name;
		resp[1] = interf;
		resp[2] = command;
		for( int x=0; x<parameters.length; x++ )
			resp[x+3] = parameters[x];
		return resp;
	}
	
	private void launchArtifactFirst( String location, String name, String type, String resourceId ) {			
		String q;
		IABC4GSDItem application;
		q = "abc.application.[].need.==.{{" + type + "}}";
		String[] tmp = query(q);
		if( tmp == null || tmp.length == 0 ) {
			// creating the application if never used so far
			application = new ABC4GSDItem("abc.application");
			application.set("need", type);
		} else
			application = new ABC4GSDItem("abc.application",tmp[0]);
		
		// creating the asset to wrap the need
		IABC4GSDItem asset = new ABC4GSDItem("abc.asset");
		asset.set("ptr", application.getId());
		asset.set("type", "application");
		asset.set("name", "APP");
		
		// creating property for linking resource with need running it
		IABC4GSDItem property = new ABC4GSDItem("abc.property");
		property.attach("asset", resourceId);
		property.attach("user", _me);
		property.set("name", _me + ":" + resourceId);
		property.set("key", "needId");
		property.set("value", asset.getId());
		
	}
	
	private void _storeArtifact( long artifactId ) {}

	private void pushArtifacts() {
//		TODO > make it specific for the file
//		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Application.PLUGIN_ID);
//		String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Application.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
		System.out.println("Pushing repo ...");
		
		File[] tmp = Platform.getLocation().toFile().listFiles();
		List<File> dirs = new ArrayList<File>();
		for( int x=0; x<tmp.length; x++ )
			if(tmp[x].isDirectory()) dirs.add(tmp[x]);
		
		for( File x : dirs ) {
			File f = new File( Platform.getLocation().toFile(), x.getName() );
			Runtime run = Runtime.getRuntime();
			Process pr;
			BufferedReader buf;
			String line;
			if( !f.exists() ) {
				continue;
			} else {
				try {
					pr = run.exec( "git add *", null, f );
					pr.waitFor();
					buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					line = "";
					while ((line=buf.readLine())!=null)
						System.out.println(line);

					pr = run.exec( "git commit -m aaa -a", null, f );
					pr.waitFor();
					buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					line = "";
					while ((line=buf.readLine())!=null)
						System.out.println(line);

					pr = run.exec( "git push origin master", null, f );
					pr.waitFor();
					buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					line = "";
					while ((line=buf.readLine())!=null)
						System.out.println(line);
				} catch (IOException e) { e.printStackTrace(); }
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			System.out.println( f.toString() );
		}
	}

	
	/*
	 * Applications execution functions
	 */
	
	private void killApplications() {
		Map<String, ApplicationDescriber> tmpFrames = new HashMap<String, ApplicationDescriber>(_frames);
		for( String k : tmpFrames.keySet() ) {
			if(!_assetToKeep.contains( tmpFrames.get(k).realId ))
				_killAsset(k, false);
		}
	}
	
	private void _execute( final ApplicationDescriber describer, final File environment ) {
		Display.getDefault().asyncExec( new Runnable() {
			public void run() {
		Runtime run = Runtime.getRuntime();
		Process pr;
		BufferedReader buf;
		String line;
		try {
			for( String x : describer.command ) System.out.println(x);
			if( environment != null ) {
				pr = run.exec( describer.command, null, environment );

				// any error message?
	            StreamGobbler errorGobbler = new 
	                StreamGobbler(pr.getErrorStream(), "ERR");            
	            
	            // any output?
	            StreamGobbler outputGobbler = new 
	                StreamGobbler(pr.getInputStream(), "OUT");
	                
	            // kick them off
	            errorGobbler.start();
	            outputGobbler.start();
	                                    
	            // any error???
	            int exitVal = pr.waitFor();
	            System.out.println("ExitValue: " + exitVal);
			} else 
				pr = run.exec( describer.command );
			
			_frames.put( describer.resourceId, describer );
			if( describer.independent ) {
				int pid = Utils.getPid(pr);
				describer.pid = pid;
				_appId.put(describer.resourceId, describer);
			}
		} catch (IOException e) { e.printStackTrace(); } 
		catch (InterruptedException e) { e.printStackTrace(); }
			}});
	}
	
	private String _initApplication( String name ) {
		//u_id [currAct appAssetId artifactAssetId]
		List<String> param = new ArrayList<String>();
		param.add( ""+_me );
		for( Map.Entry<String, ApplicationDescriber> entry : _appId.entrySet() ) {
			if( name.equals( entry.getValue().name ) ) {
				param.add( ""+_currAct );
				param.add( entry.getValue().appAssetId );
				param.add( entry.getValue().resourceId );
				
			}
		}
		String out = "";
		for( String each: param ) {
			if( out.length() > 0 ) out += " ";
			out += each;
		}
		System.out.println(out);
		return out;
	}
	
	/*
	 * Functions to handle server
	 */
	
	protected InternalMessage connect( String name, String model ) {
		if( _model.contains(model) ) 
			return null;
		String mainMsg = "CONNECT " + model + " USER " + name;
		_send(mainMsg);
		InternalMessage resp = _receive();
		System.out.println( resp );
		if( resp.code.length() > 0 ) {
			_me = Long.parseLong((String) ((JSONArray) ((JSONArray) resp.data.get(Constants.MSG_A)).get(2)).get(1));
//			String dirTmp = Utils.getTmpDir();
//			String msg = "QUERY " + model + " abc.user." + _me + ".tmp_dir.=.{{" + dirTmp + "}}";
//			_query( msg );
			System.out.println( "Connected to " + model );
			_model.add( model );
			String msg = "QUERY " + model + " abc.user." + _me + ".state.=." + Constants.USR_CONNECTED;
			_query( msg );
		}
		return new InternalMessage( mainMsg, resp );
	}
	
	protected InternalMessage disconnect( String model ) {
		// rest -> <model>
		if( !_model.contains(model) ) 
			return null;
		String msg = "QUERY " + model + " abc.user." + _me + ".state.=." + Constants.USR_DISCONNECTED; 
		_query( msg );
		msg = "DISCONNECT " + model;
		_send(msg);
		InternalMessage resp = _receive();
		if( resp.code.length() > 0 ) {
			System.out.println( "Disconnected from " + model );
			_model.remove( model );
		}
		return new InternalMessage(msg, resp);
	}
	
	private void _send( String msg ) {
		long code = Utils.getRandomId();
		String wip = "CODE " + code + " FROM " + _me + " " + msg;
		connectionManager.send( connectionManager.backend, wip, true );
	}
	
	private InternalMessage _receive() {
		JSONObject wip = (JSONObject)connectionManager.recv( connectionManager.backend, true );
		return new InternalMessage("Old", wip );
	}
	
	protected InternalMessage run( String wip ) {
		String msg = "RUN " + wip;
		InternalMessage resp = null;
		lock.lock();
		try {
			_send( msg );
			resp = _receive();
		} finally {
			lock.unlock();
		}
		return new InternalMessage( msg, resp );
	}

	public List<String> getModel() { return _model; }
	public ConnectionManager getConnectionManager() { return connectionManager; }

	private String generateQueryString( String[] wip ) {
		String ret = "";
		for( int x=0; x<wip.length; x++ )
			ret += wip[x] + ", ";
		if( ret.length() > 0 )
			ret = "[" + ret.substring(0, ret.length()-2) + "]";
		else ret = "[]";
		return ret;
	}
	
	private void _notify( String msg ) {
		String wip = "CMD NOTIFICATION " + msg;
		connectionManager.send( connectionManager.publisher, wip );
	}

	public void _openChat( long actId ) {
		String wip = "CMD CHAT_OPEN " + actId;
		connectionManager.send( connectionManager.publisher, wip );
	}
	public void _writeInChat( String msg ) {
		String wip = "CMD CHAT_WRITE " + msg;
		connectionManager.send( connectionManager.publisher, wip );
	}
	
	
	public void suspend() {
		if( _currAct != -1 )
			_suspendActivity(_currAct);
	}
}




