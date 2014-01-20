package itu.abc4gsd.rcp.client_v6.command;
import java.util.HashMap;
import java.util.Map;

 import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveManagement;
import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveUse;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;


public class HandlerUsePerspective implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		BundleContext ctx = FrameworkUtil.getBundle(HandlerUsePerspective.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        Map<String,Object> properties = new HashMap<String, Object>();
        properties.put("PERP_ID", PerspectiveUse.ID);
        eventAdmin.postEvent( new Event("Perspective/Load", properties) );
        return null;
	}

	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}

}
