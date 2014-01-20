package itu.abc4gsd.rcp.client_v6.command;
import java.util.ArrayList;
import java.util.StringTokenizer;

import itu.abc4gsd.rcp.client_v6.dialog.LoginDialog;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.json.simple.JSONArray;


public class HandlerAdmin implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "", "Enter password", "", null);
	        if (dlg.open() == Window.OK) {
	          if( dlg.getValue().equals( "koalakoala" ) )  {
	        	  runScript();
	          }
	        }



		return true;
	}
	
	private void runScript() {
		String tmp;
		JSONArray results;
		ArrayList<IABC4GSDItem> users = new ArrayList<IABC4GSDItem>();
		ArrayList<IABC4GSDItem> activities = new ArrayList<IABC4GSDItem>();
		ArrayList<IABC4GSDItem> rooms = new ArrayList<IABC4GSDItem>();
		ArrayList<IABC4GSDItem> messages = new ArrayList<IABC4GSDItem>();
		ArrayList<IABC4GSDItem> users_rooms = new ArrayList<IABC4GSDItem>();
		
		tmp = "abc.user.[]._id";
		results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int z=0; z<results.size(); z++ ) {
			IABC4GSDItem wip = new ABC4GSDItem( "abc.user", results.get(z).toString(), new String[]{"name"} );
			users.add( wip );
		}
		
		tmp = "abc.activity.[]._id";
		results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int z=0; z<results.size(); z++ ) {
			IABC4GSDItem wip = new ABC4GSDItem( "abc.activity", results.get(z).toString(), new String[]{"name"} );
			activities.add( wip );
		}

		tmp = "chat.room.[]._id";
		results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int z=0; z<results.size(); z++ ) {
			IABC4GSDItem wip = new ABC4GSDItem( "chat.room", results.get(z).toString() );
			wip.update();
			rooms.add( wip );
		}

		tmp = "chat.message.[]._id";
		results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int z=0; z<results.size(); z++ ) {
			IABC4GSDItem wip = new ABC4GSDItem( "chat.message", results.get(z).toString() );
			wip.update();
			messages.add( wip );
		}

		tmp = "chat.user_rooms.[]._id";
		results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int z=0; z<results.size(); z++ ) {
			IABC4GSDItem wip = new ABC4GSDItem( "chat.user_rooms", results.get(z).toString() );
			wip.update();
			users_rooms.add( wip );
		}
		
		String q = "";
		String qq;
		StringTokenizer st;
		for( IABC4GSDItem wip : messages ) {
			q += wip.getId() + "\t";
			qq = "" + wip.get("user");
			qq = qq.length()>0 ? qq.substring( 1, qq.length()-1 ) : "";
			for( IABC4GSDItem wip2 : users ) 
				if(qq.equals( ""+wip2.getId() ))
					qq = wip2.get("name").toString();

			q += (qq.length()>0? qq : "-1") + "\t";
			q += wip.get("timestamp") + "\t";
			q += "\"" + wip.get("text") + "\"" + "\t";
			q += "\n";
		}
		System.out.println(">>>>>>> Messages <<<<<<<");
		System.out.println(q);

		q = "";
		for( IABC4GSDItem wip : users ) {
			q += wip.getId() + "\t";
			q += wip.get("name") + "\t";
			q += "\n";
		}
		System.out.println(">>>>>>> Users <<<<<<<");
		System.out.println(q);

		q = "";
		for( IABC4GSDItem wip : rooms ) {
			q += wip.getId() + "\t";
			qq = "" + wip.get("activity");
			qq = qq.length()>0 ? qq.substring( 1, qq.length()-1 ) : "";
			for( IABC4GSDItem wip2 : activities ) 
				if(qq.equals( ""+wip2.getId() ))
					qq = wip2.get("name").toString();

			q += (qq.length()>0? qq : "-1") + "\t";
			qq = "" + wip.get("msg");
			qq = qq.length()>0 ? qq.substring( 1, qq.length()-1 ) : "";
			st = new StringTokenizer(qq, ",");
			qq = "";
			while( st.hasMoreTokens() ) {
				String msg = st.nextToken().trim();
				for( IABC4GSDItem wip2 : messages ) 
					if(msg.equals( ""+wip2.getId() )) {
						String qqq = "" + wip2.get("user");
						qqq = qqq.length()>0 ? qqq.substring( 1, qqq.length()-1 ) : "";
						for( IABC4GSDItem wip3 : users ) 
							if(qqq.equals( ""+wip3.getId() ))
								qq += wip2.get("timestamp").toString() + "\t" + wip3.get("name") + "\t\"" + wip2.get("text").toString() + "\"$$$";
					}
			}
			
			q += qq ;
			q += "\n";
		}
		System.out.println(">>>>>>> Rooms <<<<<<<");
		System.out.println(q);

		q = "";
		for( IABC4GSDItem wip : users_rooms ) {
			qq = "" + wip.get("user");
			qq = qq.length()>0 ? qq.substring( 1, qq.length()-1 ) : "";
			for( IABC4GSDItem wip2 : users ) 
				if(qq.equals( ""+wip2.getId() ))
					qq = wip2.get("name").toString();

			q += (qq.length()>0? qq : "-1") + "\t";

			qq = "" + wip.get("room");
			qq = qq.length()>0 ? qq.substring( 1, qq.length()-1 ) : "";
			st = new StringTokenizer(qq, ",");
			qq = "";
			while( st.hasMoreTokens() ) {
				String msg = st.nextToken().trim();
				for( IABC4GSDItem wip2 : rooms ) 
					if(msg.equals( ""+wip2.getId() ))
						for( IABC4GSDItem wip3 : activities ) 
							if(wip2.get("name").toString().equals( ""+wip3.getId() ))
								qq += wip3.get("name").toString() + "$$$";
			}
			
			q += qq ;
			q += "\n";
		}
		System.out.println(">>>>>>> User-Rooms <<<<<<<");
		System.out.println(q);
	}
	
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}
