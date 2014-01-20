package itu.abc4gsd.rcp.client_v6.view.descriptionV;

import itu.abc4gsd.rcp.client_v6.appInterface.AppInterface;
import itu.abc4gsd.rcp.client_v6.appInterface.ICommand;
import itu.abc4gsd.rcp.client_v6.appInterface.RemoteMessage;
import itu.abc4gsd.rcp.client_v6.draw2d.notification.cache.ColorCache;
import itu.abc4gsd.rcp.client_v6.draw2d.notification.notifier.NotifierDialog;
import itu.abc4gsd.rcp.client_v6.draw2d.notification.notifier.NotificationType;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class ListenerDescription extends AppInterface {	
	private class CmdActivityChanged implements ICommand {
		public void execute(String data) {
			log("Executing Activity Changed");
			RemoteMessage wip = new RemoteMessage(data);
			final long a_id = Long.parseLong(wip.value);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IABC4GSDItem wip = new ABC4GSDItem( "abc.activity", a_id, new String[]{"description"} );
					String tmp = wip.get("description").toString();
					changeText( tmp.length() > 0 ? tmp : "<no description provided>" );
				}
			});
		}
	}
	
	private Text description;
	private String current = "";
	public ListenerDescription( final Text description ) {
		super( "ListenerDescription" );
		this.description = description;
		changeText( "No activity selected!" );
		this.description.addFocusListener( new FocusListener() {
			public void focusLost(FocusEvent e) {
//				NotifierDialog.notify("TITLE", "BODY", NotificationType.WARN);
//				NotifierDialog.notify("TITLE", "BODY", NotificationType.WARN2, ColorCache.getColor(255, 0,0));

				String tmp = description.getText();
				if( tmp.equals(current) ) return;
				if( MasterClientWrapper.getInstance().getCurrentActivity() == -1 ) {
					changeText(current);
					return;
				}
				IABC4GSDItem wip = new ABC4GSDItem( "abc.activity", MasterClientWrapper.getInstance().getCurrentActivity() );
				wip.set("description", tmp);
			}
			public void focusGained(FocusEvent e) {}
		});
		_init();
	}
	private void _init() {
		String q = "";
		log("--> Starting listener <--");
		q = "abc.user." + MasterClientWrapper.getInstance().getMyId() + ".activity.=.";
		subscribe( q, new CmdActivityChanged() );	
	}
	public void killOperation() { }
	public void suspendOperation() { }
	public void resumeOperation() { }
	public boolean personalHandler(String ch, String msg) { return true; }
	
	private void changeText( String newText ) {
		if( description.isDisposed() ) return;
		description.setText( newText );
		current = newText;
	}
}








