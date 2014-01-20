package itu.abc4gsd.rcp.client_v6.command;
import java.util.HashMap;
import java.util.Map;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;


public class HandlerCreateActivity implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
        BundleContext ctx = FrameworkUtil.getBundle(HandlerCreateActivity.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        Map<String,Object> properties = new HashMap<String, Object>();        
    	properties.put("ACT_ID", "");
    	eventAdmin.postEvent( new org.osgi.service.event.Event("activityCreate/asyncEvent", properties) );
		return null;
	}
	
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}
