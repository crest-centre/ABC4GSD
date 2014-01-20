package itu.abc4gsd.rcp.client_v6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import itu.abc4gsd.rcp.client_v6.command.HandlerConnect;
import itu.abc4gsd.rcp.client_v6.dialog.LoginDialog;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.preferences.ConnectionAdvanced;
import itu.abc4gsd.rcp.client_v6.preferences.Needs;
import itu.abc4gsd.rcp.client_v6.view.abc4gsdPopUpNotification;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;


public class Application implements IApplication {
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			
			// Hack to have preferences working correctly
			DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			// Init zmq infrastructure
			MasterClientWrapper.getInstance();
			// Init OSGI listener
			OSGIEventHandler.getInstance();
			sendInitBroadcast();

			
			if (!login())
				return IApplication.EXIT_OK;
			
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) 
				return IApplication.EXIT_RESTART;
			return IApplication.EXIT_OK;
		} finally { display.dispose();
		}
	}
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	private boolean login() { 
		LoginDialog loginDialog = new LoginDialog(null);
		if (loginDialog.open() != Window.OK) return false;

		MasterClientWrapper.getInstance().connect( loginDialog.getConnectionDetails(), DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_MODEL, ""));
		handleRemoteRepo();

		return true;
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
	
	private void sendInitBroadcast() {
		BundleContext ctx = FrameworkUtil.getBundle(ApplicationWorkbenchWindowAdvisor.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        eventAdmin.postEvent( new org.osgi.service.event.Event("State/Init", new HashMap<String, Object>()) );
	}

}
