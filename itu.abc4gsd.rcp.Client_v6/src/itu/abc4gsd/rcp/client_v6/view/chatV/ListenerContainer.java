package itu.abc4gsd.rcp.client_v6.view.chatV;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;

import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

public class ListenerContainer extends AppInterface {	
	private class CmdOpenChat implements ICommand {
		public void execute(String data) {
			log("Executing CmdMessageAdded");
			RemoteMessage wip = new RemoteMessage(data);
			final long roomId = wip.id;
			String q = "chat.room."+ roomId +".activity";
			String[] wip1 = MasterClientWrapper.getInstance().query(q);
			final long actId = Long.parseLong(wip1[0]);
			if( chatFolder.containsChat(actId) ) return;
			
			q = "abc.ecology.[].name.==."+ actId +":"+ MasterClientWrapper.getInstance().getMyId();
			wip1 = MasterClientWrapper.getInstance().query(q);
			if( wip1.length == 0 ) return;
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					openChat(actId);
				}
			});
		}
	}
	
	
	private ChatViewContainer chatFolder;
	public ListenerContainer( ChatViewContainer chatFolder ) {
		super( "ListenerContainer" );
		this.chatFolder = chatFolder; 
		_init();
	}
	private void _init() {
		String q = "";
		log("--> Starting listener <--");

			q = "chat.room.[].msg.+";		// Message added ... check to pop up the chat
			subscribe( q, new CmdOpenChat() );
	}
	public void killOperation() { }
	public void suspendOperation() { }
	public void resumeOperation() { }
	public boolean personalHandler(String ch, String msg) {
		if( !(ch.equals("EVT") && msg.startsWith("CMD CHAT_") ) ) return true;
		String[] st = msg.split(" ");
		if( st[1].equals("CHAT_OPEN") ) {
			long actId = Long.parseLong((String)msg.substring( msg.indexOf("CHAT_OPEN") + "CHAT_OPEN".length() ).trim());
			openChat(actId);
		} else if( st[1].equals("CHAT_WRITE") ) {
			JSONObject content = (JSONObject)JSONValue.parse( (String)msg.substring( msg.indexOf("CHAT_WRITE") + "CHAT_WRITE".length() ));
			String actId = content.containsKey("activity") ? content.get("activity").toString() : null;
			String message = content.containsKey("message") ? content.get("message").toString() : null;
			if( actId == null ) return true;
			if( message == null ) return true;

			IABC4GSDItem wip = new ABC4GSDItem( "chat.message" );
			MasterClientWrapper.getInstance().query("chat.message." + wip.getId() + ".timestamp.=.?TIME?");
			
			wip.set("text", message);
			wip.attach("user", MasterClientWrapper.getInstance().getMyId());

			// attaching new message to the room
			MasterClientWrapper.getInstance().query("chat.room.[chat.room.[].name.==." + actId + "].msg.+." + wip.getId() );

		} 
		return true;
	}

	private void openChat( final long actId ) {
		log("Opening chat due to internal message ...");

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				chatFolder.addChat(actId);
				
		        BundleContext ctx = FrameworkUtil.getBundle(ListenerContainer.class).getBundleContext();
		        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
		        EventAdmin eventAdmin = ctx.getService(ref);
		        Map<String,Object> properties = new HashMap<String, Object>();
		        
	        	properties.put("VIEW_ID", ChatViewContainer.ID);
	        	eventAdmin.postEvent( new org.osgi.service.event.Event("View/Load", properties) );

			}
		});		
	}
}








