package itu.abc4gsd.rcp.client_v6.command;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;


public class HandlerShowViewPart implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
//		The below was working when command name was activityV.ActivityViewH.
//		However it required one command for each menu. With the new solution it is down to a switch case and one command
//		String className = base + event.getCommand().getName();
		String className = "itu.abc4gsd.rcp.client_v6.view.";
		String menuItem = ((MenuItem) ((Event)event.getTrigger()).widget).getText();

		if( menuItem.equals( "Activity Hierarchy" ) )
			className += "activityV.ActivityViewH";
		
		if( menuItem.equals( "Activity Graph" ) )
			className += "activityV.ActivityViewGraph";
		
		if( menuItem.equals( "Contacts" ) )
			className += "contactV.ContactView";
		
		if( menuItem.equals( "Artifacts" ) )
			className += "artifactV.ArtifactView";
		
		if( menuItem.equals( "Notifications" ) )
			className += "notificationV.NotificationView";
		
		if( menuItem.equals( "Chats" ) )
			className += "chatV.ChatViewContainer";

		try {
			// Getting the id of the view part
			final Field field = Class.forName(className).getDeclaredField("ID");
	        field.setAccessible(true);
	        String fID = (String) field.get(Class.forName(className));

	        BundleContext ctx = FrameworkUtil.getBundle(HandlerShowViewPart.class).getBundleContext();
	        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
	        EventAdmin eventAdmin = ctx.getService(ref);
	        Map<String,Object> properties = new HashMap<String, Object>();
	        
        	properties.put("VIEW_ID", fID);
        	eventAdmin.postEvent( new org.osgi.service.event.Event("View/Load", properties) );
		} catch (IllegalArgumentException e) { e.printStackTrace();
		} catch (IllegalAccessException e) { e.printStackTrace();
		} catch (ClassNotFoundException e) { e.printStackTrace();
		} catch (SecurityException e) { e.printStackTrace();
		} catch (NoSuchFieldException e) { e.printStackTrace();
		}

		return null;
	}

	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}

}

