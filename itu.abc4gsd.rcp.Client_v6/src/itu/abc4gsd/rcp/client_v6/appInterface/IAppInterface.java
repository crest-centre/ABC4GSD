package itu.abc4gsd.rcp.client_v6.appInterface;

import org.zeromq.ZMQ;

 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;

import java.util.List;



public interface IAppInterface {
	public abstract void killOperation();
	public abstract void suspendOperation(); 
	public abstract void resumeOperation();
	public abstract boolean personalHandler( String ch, String msg );

	public void subscribe( String q, ICommand cmd );
	public void unsubscribe( List<String> listOfEvents, String model );
	public void unsubscribe( List<String> listOfEvents );
	public void unsubscribe();
	public void send( ZMQ.Socket ch, String msg, boolean encryption );
	public String recv( ZMQ.Socket ch, boolean encryption );
	public void set( String type, int id, String name, String value, String model );
	public void set( String type, int id, String name, String value );
	public String get( String type, int id, String name, String model );
	public String get( String type, int id, String name );
	public String getProperty( String name ) throws ClassNotFoundException;
	public void setProperty( String name, String val );

	public ABC4GSDItem getItem( int id );
	public String getBase( int id );	
	
	public void suspend();	
	public void resume();	
	public String sendCommand( String cmd );
	public String[] query( String q );
	public String[] query( String q, String model );

	public void loadProperty();
	public void storeProperty();
	
	public void closeConnections();
	public void _receive();
	public void handleMessage( String ch, String msg );
	public boolean[] _getExpansionArray( List<String> data ); // check
	public void _checkSubscription( String msg );	
	public String[] _getAttributesList( String type, int id, String model );
	public String[] _getAttributesList( String type, int id );
	public void _defineProperties();
	
	public String getName();
	public String generateQueryString( String[] wip );
}
