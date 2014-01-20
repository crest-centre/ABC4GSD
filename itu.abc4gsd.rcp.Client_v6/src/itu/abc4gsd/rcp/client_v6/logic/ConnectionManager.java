package itu.abc4gsd.rcp.client_v6.logic;


import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class ConnectionManager {
	public boolean close = false;
	public int locking = 0;
	
	ZMQ.Socket backend;
	ZMQ.Socket logger;
	ZMQ.Socket publisher;
	ZMQ.Socket control;

	public ConnectionManager( String[] addrs ) {
		System.out.println("Unable to connect!!!");
		for( String s : addrs )
			System.out.println(s);
		
		try {
			backend = ContextManager.get().socket(ZMQ.XREQ);
			backend.connect(addrs[0]);
			logger = ContextManager.get().socket(ZMQ.PUSH);
			logger.connect(addrs[1]);
			publisher = ContextManager.get().socket(ZMQ.PUB);
			publisher.bind(addrs[2]);
			control = ContextManager.get().socket(ZMQ.REP);
			control.bind(addrs[3]);
			System.out.println("Connecting to: " + addrs[0]);
		} catch(ZMQException e) {
			System.out.println("Unable to connect!!!");
			e.printStackTrace();
		}
	}
	
	public void close() {
		setClose(true);
		while( locking  > 0 ) {}
		control.close();
		publisher.close();
		logger.close();
		backend.close();
	}
	
	public void terminate() {
		close();
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





