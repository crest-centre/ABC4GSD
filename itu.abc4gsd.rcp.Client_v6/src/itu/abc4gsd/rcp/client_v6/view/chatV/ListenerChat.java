package itu.abc4gsd.rcp.client_v6.view.chatV;


import org.eclipse.swt.widgets.Display;

import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

public class ListenerChat extends AppInterface {	
	private class CmdMessageAdded implements ICommand {
		public void execute(String data) {
			log("Executing CmdMessageAdded");
			RemoteMessage wip = new RemoteMessage(data);
			final long newMsg = Long.parseLong(wip.value);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IABC4GSDItem wip = new ABC4GSDItem( "chat.message", newMsg );
					wip.update();
					chat.receiveMessage( wip );
				}
			});
		}
	}

	
	private ChatViewItem chat;
	public ListenerChat( ChatViewItem chat ) {
		super( "ListenerChat" );
		this.chat = chat; 
		_init();
	}
	private void _init() {
		String q = "";
		log("--> Starting listener <--");
		
		q = "chat.room." + chat.getRoom().getId() + ".msg.+";		// Message added
		subscribe( q, new CmdMessageAdded() );


	}
	public void killOperation() { }
	public void suspendOperation() { }
	public void resumeOperation() { }
	public boolean personalHandler(String ch, String msg) { return true; }
}








