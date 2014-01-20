package itu.abc4gsd.rcp.client_v6.view.notificationV;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.logic.Utils;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.view.abc4gsdPopUpNotification;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerEvent;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManagerListener;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDTreeItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

class NotificationViewContentProvider extends AppInterface implements IStructuredContentProvider, ABC4GSDItemManagerListener {
	/* 
	 * AppInterface
	 */
	private String u_name;
	private long u_id;
	private List<String> subscriptionPerm = new ArrayList<String>();
	private List<String> subscriptionTemp = new ArrayList<String>();

	
	public NotificationViewContentProvider(String name) {
		super(name);
		_init();
	}
	
	private void _init() {
		String q = "";
		log("--> Initializing data <--");
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		u_name = prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, ""));
		q = "abc.user.[].name.==." + u_name;
		u_id = Long.parseLong( query(q)[0] );

		eventRegistration();
	}
	
	public void killOperation() {}
	public void resumeOperation() {}
	public void suspendOperation() {}

	public boolean personalHandler(String ch, String msg) {
		String[] st = msg.split(" ");
		if( !(ch.equals("EVT") && st[0].equals("CMD") && st[1].equals("NOTIFICATION") ) ) return true;
	
		JSONObject content = (JSONObject)JSONValue.parse( (String)msg.substring( msg.indexOf("NOTIFICATION") + "NOTIFICATION".length() ));
		final ABC4GSDNotification ntf = new ABC4GSDNotification(content);
		showNotification(ntf);
		ABC4GSDItemManager.getManager("Notification").addItem(ntf);
		itemsChanged(null);
		
		return true;
	}
	
	private void eventRegistration() {
		// user log_in			--> sent after connect button is pressed
		// activity created     --> sent after create is pressed
		
		String q;
		// invited in activity
		
		// changed activity
		q = "abc.user." + u_id +".activity.=.";
		subscriptionPerm.add(q);
		subscribe( q, new ICommand() {
			public void execute(String data) {
				log("Executing SendEvend");
		        BundleContext ctx = FrameworkUtil.getBundle(NotificationViewContentProvider.class).getBundleContext();
		        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
		        EventAdmin eventAdmin = ctx.getService(ref);
		        eventAdmin.postEvent( new Event("State/Act_Changed", new HashMap<String, Object>()) );
			}});
		
		
		// ping received and sent
		q = "notification.notification.[]";
		subscriptionPerm.add(q);
		subscribe( q, new ICommand() {
			public void execute(String data) {
//				"notification.notification.[].from.+." + u_id;
				log("Executing Notification Sent/Received");
				RemoteMessage wip = new RemoteMessage(data);
				if( wip.id == -1)  return;
				if( wip.length != 6 ) return;
				if( !wip.value.equals(""+u_id) ) return;
				
				if( wip.key.equals("from") ) {
					JSONObject content = new JSONObject();
					content.put("title", "Notification sent");
					content.put("body", "Your notification has been received from the server and dispatched.");
					sendCommand("NOTIFY " + content.toJSONString() );					
				}
				if( wip.key.equals("to") ) {
					IABC4GSDItem notification = new ABC4GSDItem("notification.notification", wip.id);
					notification.update();
					
					IABC4GSDItem message = new ABC4GSDItem("chat.message", notification.get("msg").toString(), new String[]{"text","timestamp"});
					IABC4GSDItem sender = new ABC4GSDItem("abc.user", notification.get("from").toString(), new String[]{"name"});
					IABC4GSDItem activity = new ABC4GSDItem("abc.activity", notification.get("activity").toString(), new String[]{"name"});
					
					JSONObject content = new JSONObject();
					// INFO> here to add more info in the notification
					content.put("notification_id", notification.getId());
					content.put("title", "Notification received" );
					content.put("body", "Sender: " + sender.get("name") + "\nFrom: " + activity.get("name") + "\nMessage:\n" + message.get("text"));
					content.put("level", "ping");
					sendCommand("NOTIFY " + content.toJSONString() );
				}
			}});

	}

	/*
	 * Manager
	 */
	private TableViewer viewer;
	private ABC4GSDItemManager manager;

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (TableViewer) viewer;
      if (manager != null)
         manager.removeABC4GSDItemManagerListener(this);
      manager = (ABC4GSDItemManager) newInput;
      if (manager != null)
         manager.addABC4GSDItemManagerListener(this);
   }


   public void itemsChanged(final ABC4GSDItemManagerEvent event) {
      // If this is the UI thread, then make the change.
      if (Display.getCurrent() != null) {
         updateViewer(event);
         return;
      }
      // otherwise, redirect to execute on the UI thread.
      Display.getDefault().asyncExec(new Runnable() {
         public void run() {
            updateViewer(event);
         }
      });
   }

   public void showNotification( final ABC4GSDNotification ntf ) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				abc4gsdPopUpNotification popup = new abc4gsdPopUpNotification(Display.getDefault(), ntf, ntf.level.equals("ping") ? abc4gsdPopUpNotification.TYPE_PING : -1 );

				popup.create();
				popup.open();
			}
		});	   
   }
   
   private void updateViewer(ABC4GSDItemManagerEvent event) { viewer.refresh(); }
   public Object[] getElements(Object parent) {
	   Object[] resp = manager.getItems();
	   Utils.reverse(resp);
	   return resp;
   }
   public void dispose() {}
}


