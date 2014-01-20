package itu.abc4gsd.rcp.client_v6.command;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

 import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.dialog.TemplateTextDialog;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.preferences.ConnectionAdvanced;
 import itu.abc4gsd.rcp.client_v6.preferences.Needs;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;


public class HandlerConnect implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Fetching strings
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String[] fields = new String[] { "User name:", "Schemas:"};
		final String[] defaults = new String[] { 
				prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, "")), 
				prefs.get(Connection.USER_MODEL, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_MODEL, "")) };
		String msg = "Fill in the form for connecting to the server\n" 
				+ "Server: " + prefs.get(ConnectionAdvanced.ADDR_BACKEND, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_BACKEND, "ERROR")) + "\n" 
				+ "Logger: " + prefs.get(ConnectionAdvanced.ADDR_LOGGER, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_LOGGER, "ERROR")) + "\n" 
				+ "Publisher: " + prefs.get(ConnectionAdvanced.ADDR_PUBLISHER, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_PUBLISHER, "ERROR")) + "\n"
				+ "Control: " + prefs.get(ConnectionAdvanced.ADDR_CONTROL, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_CONTROL, "ERROR"));

		
		TemplateTextDialog dialog = new TemplateTextDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), "ABC4GSD Connection Information", msg, fields, defaults );
		dialog.create();
		if (dialog.open() == Window.OK) {
			MasterClientWrapper.getInstance().connect(dialog.getValues().get(0), dialog.getValues().get(1));
			prefs.put(Connection.USER_NAME, dialog.getValues().get(0)); 
			prefs.put(Connection.USER_MODEL, dialog.getValues().get(1)); 

			handleRemoteRepo();

		    BundleContext ctx = FrameworkUtil.getBundle(HandlerConnect.class).getBundleContext();
		    ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
	        ctx.getService(ref).postEvent( new org.osgi.service.event.Event("State/Connected", new HashMap<String, Object>()) );
		}
		return null;
	}
	
	
		

	private void handleRemoteRepo( ) {
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
		File f = new File( Platform.getLocation().toFile(), baseDir );
		Runtime run = Runtime.getRuntime();
		Process pr;
		BufferedReader buf;
		String wip, line;
		if( !f.exists() ) {
			try {
				wip = "git clone " + prefs.get(ConnectionAdvanced.ADDR_REPOSITORY, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_REPOSITORY, ""));
				pr = run.exec( wip, null, Platform.getLocation().toFile() );
				pr.waitFor();
				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				while ((line=buf.readLine())!=null)
					System.out.println(line);
			} catch (IOException e) { e.printStackTrace(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		} else {
			try {
				pr = run.exec( "git pull", null, f );
				pr.waitFor();
				buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				line = "";
				while ((line=buf.readLine())!=null)
					System.out.println(line);
			} catch (IOException e) { e.printStackTrace(); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		System.out.println( f.toString() );
	}
	
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}
