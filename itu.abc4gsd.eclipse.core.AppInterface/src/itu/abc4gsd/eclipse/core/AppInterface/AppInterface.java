package itu.abc4gsd.eclipse.core.AppInterface;

import itu.abc4gsd.eclipse.core.AppInterface.ConnectionManager;
import itu.abc4gsd.eclipse.core.AppInterface.Constants;
import itu.abc4gsd.eclipse.core.AppInterface.IAppInterface;
import itu.abc4gsd.eclipse.core.AppInterface.ICommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.zeromq.ZMQ;



public abstract class AppInterface implements IAppInterface {
	
	private class SubscriptionDescriber {
		private final String query;
		private final ICommand command;
		
		public SubscriptionDescriber( String q, ICommand c ) {
			query = q;
			command = c;
		}
		
		public String getQuery() { return query; }
		public ICommand getCommand() { return command; }
	}

	private static final boolean DBG = false;  // does not work

	ConnectionManager connectionManager;
	private final Lock lock = new ReentrantLock();
	protected List<String> _definedProperties = new ArrayList<String>();
	protected int _id = -1;
	protected String _defaultModel = "abc";
	protected String _name; 
	protected long _userId = -1;
	protected long _appId = -1;
	protected long _actId = -1;
	protected long _artId = -1;
	protected HashMap<String, String> _propertyVal = new HashMap<String, String>();
	protected HashMap<String, String> _propertyId = new HashMap<String, String>();
	protected HashMap<String, SubscriptionDescriber> _simpleSubscription = new HashMap<String, SubscriptionDescriber>();
	protected HashMap<String, SubscriptionDescriber> _complexSubscription = new HashMap<String, SubscriptionDescriber>();

	private void initManager() {
		connectionManager = new ConnectionManager();
	}
	
	private void initReceiver() {
		Executor executor = Executors.newFixedThreadPool(1);
		executor.execute( new Runnable() { public void run() {  _receive(); } } );
	}
	
	public AppInterface( String name ) {
		//u_id [currAct appAssetId artifactAssetId]
//		_name = "eclipse_" + name;
		_name = name;
		log("--> Initializing connections <--");
		initManager();

		String wip = sendCommand( "INIT " + _name );
		String[] msg = wip.split(" ");
		System.out.println(msg);

		_userId = Long.parseLong(msg[0]);
		if( msg.length >1 ) {
			_actId = Long.parseLong(msg[1]);
			_appId = Long.parseLong(msg[2]);
			_artId = Long.parseLong(msg[3]);
		}
		
		initReceiver();
//		Job job = new Job("_receive") {
//			protected IStatus run(IProgressMonitor monitor) {
//				connectionManager.locking += 1;
//				try {
//					ZMQ.Poller poller = connectionManager.getContext().poller(2);
//					poller.register( connectionManager.subscriber, ZMQ.Poller.POLLIN );
//					poller.register( connectionManager.event, ZMQ.Poller.POLLIN );
//					while (! connectionManager.close ) {
//						String msg = null;
//						poller.poll(); // pass 1000?
//						if( poller.pollin(0) ) {
//							JSONObject wip = (JSONObject)connectionManager.recv( connectionManager.subscriber, true );
//							//TODO handle the json obj
//							handleMessage("SUB", wip.get(Constants.MSG_A).toString());
//						}
//						if( poller.pollin(1) ) {
//							msg = connectionManager.recv( connectionManager.event );
//							handleMessage("EVT", msg);
//						}
//					}
//				} catch ( Exception e ) { System.out.println("Issue with the Job"); }
//				finally { connectionManager.locking -= 1; }
//				return Status.OK_STATUS;
//			}
//		};
//		job.setPriority(Job.SHORT);
//		job.schedule(); 
	}
	
	public abstract void killOperation();
	public abstract void suspendOperation();
	public abstract void resumeOperation();
	public abstract boolean personalHandler(String ch, String msg);

	@Override
	public void closeConnections() { connectionManager.terminate(); }

	@Override
	public String sendCommand( String cmd ) {
		lock.lock();
		String tmp = null;
		try {
			connectionManager.send( connectionManager.backend, cmd );
			tmp = connectionManager.recv( connectionManager.backend );
		} finally {
			lock.unlock();
		}
		return tmp;
	}

	@Override
	public void send(ZMQ.Socket ch, String msg, boolean encryption) {
		// TODO check if real string or JSONObject
		// if JSONObject than .toString()
		if( encryption ) {
			// TODO encrytp
		}
		connectionManager.send(ch, msg);
	}

	@Override
	public String recv( ZMQ.Socket ch, boolean encryption ) {
		// TODO check if real string or JSONObject
		// if JSONObject than .toString()
		String wip = connectionManager.recv( ch );
		if( encryption ) {
			// TODO encrytp
		}
		return wip;
	}

	@Override
	public String[] query(String q, String model) {
//		model is included for future purposes
		lock.lock();
		Object tmp = null;
		try {
			connectionManager.send( connectionManager.query, q, true );
			tmp = (JSONObject)connectionManager.recv( connectionManager.query, true );
		} finally {
			lock.unlock();
		}
		List<String> wip = new ArrayList<String>();
		tmp =  ((JSONArray)((JSONObject)tmp).get(Constants.MSG_A));
		if( tmp instanceof JSONArray )
			for( int x=0; x<((JSONArray)tmp).size(); x++ )
				wip.add( ((JSONArray)tmp).get(x).toString() );
		else if ( tmp == null ) 
			return new String[]{};
		else 
			wip.add( tmp.toString() );
		return wip.toArray( new String[wip.size()]);
	}

	@Override
	public String[] query(String q) {
		return query( q, _defaultModel );
	}

	@Override
	public void _receive() {
		connectionManager.locking += 1;
		try {
			ZMQ.Poller poller = connectionManager.getContext().poller(2);
			poller.register( connectionManager.subscriber, ZMQ.Poller.POLLIN );
			poller.register( connectionManager.event, ZMQ.Poller.POLLIN );
			while (! connectionManager.close ) {
				String msg = null;
				poller.poll(); // pass 1000?
				if( poller.pollin(0) ) {
					JSONObject wip = (JSONObject)connectionManager.recv( connectionManager.subscriber, true );
					//TODO handle the json obj
					handleMessage("SUB", wip.get(Constants.MSG_A).toString());
				}
				if( poller.pollin(1) ) {
					msg = connectionManager.recv( connectionManager.event );
					handleMessage("EVT", msg);
				}
			}
		} catch (Exception e) {
			initManager();
			initReceiver();
		} finally { connectionManager.locking -= 1; }		
	}

	@Override
	public void handleMessage(String ch, String msg) {
		log("App Interface - Received: " + msg);

		if( ! personalHandler( ch, msg ) ) return;
		
		if( ch.equals("EVT") ) {
			String[] st = msg.split(" ");
			if( st[0].equals( "CMD" ) ) {
				log("1");
				if( st[1].equals( "SUSPEND" ) ) {
					log("2");
					if( st[2].equals( "ALL" ) ) 
						suspend();
					if( st[2].equals( "ACT" ) && !(_name.equals( "activities")) )
						suspend();
				}
				if( st[1].equals( "INIT" ) ) {}
			}
		}
		if( ch.equals( "SUB" ) )
			_checkSubscription( msg );
	}

	@Override
	public void subscribe(String q, ICommand cmd) {
		log( "Subscribing: " + q );
		StringTokenizer wip = new StringTokenizer(q, ".");
		List<String> data = new ArrayList<String>();
		while( wip.hasMoreTokens() )
			data.add(wip.nextToken());
		boolean[] expansion = _getExpansionArray( data );
		int stopIdx = -1;
		for( int x=0; x<expansion.length; x++ )
			if( expansion[x] ) {
				stopIdx = x;
				break;
			}
		if( stopIdx == -1 )
			_simpleSubscription.put( q, new SubscriptionDescriber(q,cmd) );
		else {
			String tmp = "";
			for( int x=0; x<stopIdx; x++ ) {
				if( tmp.length() > 0 )
					tmp += ".";
				tmp += data.get(x);
			}
			_complexSubscription.put( tmp, new SubscriptionDescriber(q,cmd) );
		}
	}

	@Override
	public void unsubscribe() { unsubscribe( null ); }
	
	@Override
	public void unsubscribe( List<String> listOfEvents ) { unsubscribe(listOfEvents, ""); }
	
	@Override
	public void unsubscribe(List<String> listOfEvents, String model) {
		List<String> simpleSub =  new ArrayList<String>();
		List<String> complexSub = new ArrayList<String>();
		for( SubscriptionDescriber o : _simpleSubscription.values() )
			simpleSub.add( o.query );
		for( SubscriptionDescriber o : _complexSubscription.values() )
			complexSub.add( o.query );
		if( listOfEvents == null ) {
			listOfEvents = new ArrayList<String>();
			for( String wip : simpleSub )
				listOfEvents.add(wip);
			for( String wip : complexSub )
				listOfEvents.add(wip);
		}
		for( String wip : listOfEvents ){
			if( simpleSub.contains(wip) )
				_simpleSubscription.remove(wip);
			else
				for( Map.Entry<String, SubscriptionDescriber> entry : _complexSubscription.entrySet() )
					if( wip.equals( entry.getValue().query ) ) {
						_complexSubscription.remove( entry.getKey() );
						break;
					}}
	}

	@Override
	public boolean[] _getExpansionArray( List<String> data ) {
		boolean[] wip = new boolean[ data.size() ];
		for( int x=0; x<data.size(); x++ )
			wip[x] = ( data.get(x).startsWith( Constants.Q_EXPANSION_DELIMITER[0] ) );
		return wip;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void _checkSubscription(String msg) {
//		log( "received " + msg );
		Map<String,SubscriptionDescriber> tmpsSubs = new HashMap<String,SubscriptionDescriber>(_simpleSubscription);
		Map<String,SubscriptionDescriber> tmpcSubs = new HashMap<String,SubscriptionDescriber>(_complexSubscription);

		try{
		for( SubscriptionDescriber o : tmpsSubs.values() ){
//			log("checking " + o.getQuery() );
			if( msg.startsWith(o.getQuery()) )
				o.getCommand().execute(msg);
			}
		for( Map.Entry<String, SubscriptionDescriber > entry : tmpcSubs.entrySet() ) {
//			log("checking "+ entry.getValue().getQuery() );
			if( msg.startsWith( entry.getKey() ) ) {
				String[] wip = query( "#+" + entry.getValue().getQuery());
				if( wip != null )
					for( int x=0; x< wip.length; x++ )
						if( msg.startsWith(wip[x]) )
							entry.getValue().getCommand().execute(msg);
			}}
		} catch (Exception e) { 
			e.printStackTrace(); }
	}

	@Override
	public void suspend() {
		System.out.println( "suspending " + _name );
		unsubscribe();
		suspendOperation();
//		storeProperty();
		lock.lock();
		try {
			connectionManager.send( connectionManager.backend, "ABC SUSPEND COMPLETED " + _name );
			connectionManager.recv( connectionManager.backend );
		} finally {
			lock.unlock();
//			TODO All is left up and running 
//			closeConnections();
		}
	}

	@Override
	public void resume() {
//		loadProperty();
		resumeOperation();
		lock.lock();
		try {
			connectionManager.send( connectionManager.backend, "ABC RESUME COMPLETED" );
			connectionManager.recv( connectionManager.backend );
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void set(String type, int id, String name, String value) {
		set( type, id, name, value, _defaultModel );
	}

	@Override
	public void set(String type, int id, String name, String value, String model) {
		String q = "" + model + "." + type + "." + id + "." + name + ".=." + value;
//		System.out.println(q);
		query(q);
	}

	@Override
	public String get(String type, int id, String name) {
		return get(type, id, name, _defaultModel);
	}

	@Override
	public String get(String type, int id, String name, String model) {
		String q = "" + model + "." + type + "." + id + "." + name;
		String[] wip = query(q);
		return wip.length>0 ? wip[0] : "";
	}

	@Override
	public String[] _getAttributesList(String type, int id) {
		return _getAttributesList(type, id, _defaultModel);
	}

	@Override
	public String[] _getAttributesList(String type, int id, String model) {
		String q = "" + model + "." + type + "." + id; 
		String[] wip = query(q);		
		return wip;
	}

	@Override
	public void loadProperty() {
//		application_state = property:str,value:str,name:str
		if( _appId == -1 )
			return;

		String q = "abc.application_state.[].name.==." + _userId + _appId;
		String[] resp = query(q);
		String[] wip;
		//if( resp == null || resp == "OtherError" )
		//	resp = null;
		_propertyId = new HashMap<String, String>();
		for( int x=0; x<resp.length; x++ ) {
			q = "abc.application_state." + resp[x].toString() + ".property.&.abc.application_state." + resp[x].toString() + ".value";
			wip = query(q);
			//if( resp == null || resp == "OtherError" )
			//	continue;
			if( wip.length != 2 )
				continue;
			_propertyVal.put( wip[0].toString(), wip[1].toString() );
			_propertyId.put( wip[0].toString(), resp[x].toString() );
		}
	}

	@Override
	public void storeProperty() {
		if( _appId == -1 )
			return;
		List<String> wip = new ArrayList<String>();
		for( Map.Entry<String, String> entry : _propertyVal.entrySet() ) {
			if( _propertyId.containsKey( entry.getKey() ) )
				wip.add( "abc.application_state." + _propertyId.get( entry.getKey() ) + ".value.=." + _propertyVal.get( entry.getKey() ) );
			else {
				IABC4GSDItem appState = new ABC4GSDItem(this, "abc.application_state");
				appState.attach("user", _userId);
				appState.attach("application", _appId);
				appState.set("name", _userId + _appId, true);
				appState.set("property", entry.getKey(), true);
				appState.set("value", _propertyVal.get( entry.getKey() ), true);
			}
		}
		String q = "";
		for( String tmp : wip ) {
			if( q.length() != 0 )
				q += ".&.";
			q += tmp;
		}
		if( q.length() > 0 )
			query( q );
	}

	@Override
	public void _defineProperties() {
		for( String x : _definedProperties ) {
			if( _propertyId.containsKey(x) )
				continue;
			try {
				List<String> wip = new ArrayList<String>();

				IABC4GSDItem appState = new ABC4GSDItem(this, "abc.application_state");
				appState.attach("user", _userId);
				appState.attach("application", _appId);
				appState.set("name", _userId + _appId, true);
				appState.set("property", x, true);
				appState.set("value", "<UNDEF>", true);
				
				String q = "";
				for( String tmp : wip ) {
					if( q.length() != 0 )
						q += ".&.";
					q += tmp;
				}
				if( q.length() > 0 )
					query( q );
				_propertyVal.put(x, "<UNDEF>");
				_propertyId.put(x, ""+appState.getId());
			} catch (Exception e) {}
		}
	}

	@Override
	public String getProperty(String name) throws ClassNotFoundException {
		if( _propertyVal.containsKey(name) ) {
			String x = _propertyVal.get(name);
			if( x.equals( "<UNDEF>" ) )
				throw new ClassNotFoundException();
			return x;
		}
		_defineProperties();
		throw new ClassNotFoundException();
	}

	@Override
	public void setProperty(String name, String val) {
		_propertyVal.put(name, val);
	}

	@Override
	public ABC4GSDItem getItem( int id ) {
		String base = getBase( id );
		if( base.length() == 0 ) return null;
		ABC4GSDItem ret = new ABC4GSDItem( this, base, id );
		ret.update();
		return ret;
	}
	
	@Override
	public String getBase( int id ) {
		String[] wip, wipwip;
		String tmp;
		// Getting all schemas
		List<String> schemas = new ArrayList<String>();
		wip = query("");
		for( int x=0; x< wip.length; x++ )
			schemas.add( wip[x] );

		// Cycling through all schemas for entities 
		for( int x=0; x< schemas.size(); x++ ) {
			wip = query(schemas.get(x));
			for( int l=0; l< wip.length; l++ ) {
				tmp = schemas.get(x) + "." + wip[l];
				wipwip = query(tmp);
				for( int z=0; z<wipwip.length; z++ )
					if( wipwip[z].toString().equals( ""+ id ) )
						return tmp;
			}
		}
		return "";
	}

	
	@Override
	public String getName() { return _name; }

//	public void log(String wip) {
//		if( DBG )
//			System.out.println( _name + "\t\t -> " + wip + " <-" );
//	}

	public String generateQueryString( String[] wip ) {
		String ret = "";
		for( int x=0; x<wip.length; x++ )
			ret += wip[x] + ", ";
		if( ret.length() > 0 )
			ret = "[" + ret.substring(0, ret.length()-2) + "]";
		else ret = "[]";
		return ret;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}
	
	public void log( String msg ) {
		MessageConsole myConsole = findConsole("Console");
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(msg);
	}
}
