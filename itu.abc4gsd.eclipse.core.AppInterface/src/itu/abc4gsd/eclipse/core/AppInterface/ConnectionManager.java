package itu.abc4gsd.eclipse.core.AppInterface;

import itu.abc4gsd.eclipse.core.AppInterface.Constants;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.zeromq.ZMQ;

public class ConnectionManager {
	
	private final String addr_sub = "tcp://localhost:5560";
	private final String addr_query = "tcp://localhost:5561";
	private final String addr_event = "tcp://localhost:5562";
	private final String addr_backend = "tcp://localhost:5563";

	public boolean close = false;
	public int locking = 0;
	
	ZMQ.Socket subscriber;
	ZMQ.Socket query;
	ZMQ.Socket event;
	ZMQ.Socket backend;

	public ConnectionManager() {	
		subscriber = ContextManager.get().socket(ZMQ.SUB);
		subscriber.connect(addr_sub);
		subscriber.subscribe("".getBytes());
		
		query = ContextManager.get().socket(ZMQ.REQ);
		query.connect(addr_query);

		event = ContextManager.get().socket(ZMQ.SUB);
		event.connect(addr_event);
		event.subscribe("".getBytes());
		
		backend = ContextManager.get().socket(ZMQ.REQ);
		backend.connect(addr_backend);
	}
	
	public void terminate() {
		setClose(true);
		while( locking  > 0 ) {}
		subscriber.close();
		query.close();
		event.close();
		backend.close();
		ContextManager.get().term();
	}
	
	public void send( ZMQ.Socket ch, String msg ) {
		send( ch, msg, false );
	}

	@SuppressWarnings("unchecked")
	public void send( ZMQ.Socket ch, String msg, boolean encryption ) {
		String sending = msg;
		if( encryption ) {
			JSONObject wip = new JSONObject();
			wip.put(Constants.MSG_Q, msg);
			sending = wip.toString(); 
		}
		ch.send( sending.getBytes(), 0 );	
	}
	
	public String recv( ZMQ.Socket ch ) {
		String wip = null;
		try {
			wip = (String) recv( ch, false ); 
		} catch (Exception e) {
			System.out.println("Exception in ConnectionManager.recv");
		}
		return wip;
	}
	
	public Object recv( ZMQ.Socket ch, boolean encryption ) {
		Object wip = new String( ch.recv(0) ).trim();
		if( encryption ) {
			wip = (JSONObject)JSONValue.parse( (String)wip );
		}
		return wip;
	}

	public boolean isClose() { return close; }
	public void setClose(boolean close) { this.close = close; }
	public ZMQ.Context getContext() { return ContextManager.get(); }
}


