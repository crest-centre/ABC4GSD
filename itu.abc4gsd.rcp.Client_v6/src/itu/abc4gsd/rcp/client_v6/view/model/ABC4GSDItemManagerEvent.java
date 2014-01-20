package itu.abc4gsd.rcp.client_v6.view.model;


 import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;

import java.util.EventObject;


public class ABC4GSDItemManagerEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private final RemoteMessage cmd;
	
	public ABC4GSDItemManagerEvent( ABC4GSDItemManager source, RemoteMessage cmd ) {
		super(source);
		this.cmd = cmd;
	}

	public RemoteMessage getCmd()	{ return cmd; }
}
