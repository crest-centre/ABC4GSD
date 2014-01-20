package itu.abc4gsd.rcp.client_v6.command;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

 import itu.abc4gsd.rcp.client_v6.Activator;
 import itu.abc4gsd.rcp.client_v6.dialog.AddArtifactDialog;
 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.preferences.Needs;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONArray;


public class HandlerAddArtifact implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		AddArtifactDialog dialog = new AddArtifactDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell());
		dialog.create();
		if (dialog.open() == Window.OK) {
			IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
			File folder = new File( Platform.getLocation().toFile(), baseDir );
			
			if( dialog.uploadUsed() ) {
				Runtime run = Runtime.getRuntime();
				Process pr;
				BufferedReader buf;
				String line;
				String wip;
				String[] wip2;
				try {
					wip2 = new String[]{"git", "add", dialog.getFileName()};
					pr = run.exec( wip2, null, new File( dialog.getDirName() ) );
					pr.waitFor();
					buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					line = "";
					while ((line=buf.readLine())!=null)
						System.out.println(line);
					
					wip = "git commit -a -m someText";
					pr = run.exec( wip, null, new File( dialog.getDirName() ) );
					pr.waitFor();
					buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					line = "";
					while ((line=buf.readLine())!=null)
						System.out.println(line);
					
					wip = "git push origin master";
					pr = run.exec( wip, null, new File( dialog.getDirName() ) );
					pr.waitFor();
					buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					line = "";
					while ((line=buf.readLine())!=null)
						System.out.println(line);
					
					dialog.setDirName( dialog.getDirName().substring(folder.toString().length()) );
				} catch (IOException e) { e.printStackTrace(); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			}

			String loc; 
			if( dialog.uploadUsed() ) loc = dialog.getDirName().length() > 0 ? ( new File( new File(dialog.getDirName()), dialog.getFileName() ) ).toString() : dialog.getFileName();
			else loc = dialog.getDirName();
			IABC4GSDItem wip = new ABC4GSDItem("abc.artifact");
			wip.set("name", dialog.getFileName() );
			wip.set("location", loc );
			wip.set("type", dialog.getType() );
			wip.set("independent", dialog.getIndependent() ? "true" : "false" );
			if( dialog.getAttached() ) {
				IABC4GSDItem ass = new ABC4GSDItem( "abc.asset" );
				ass.set("type", "artifact");
				ass.set("ptr", wip.getId());
				String[] ecologies = MasterClientWrapper.getInstance().query(
						"abc.ecology.[].activity.==."+ 
						MasterClientWrapper.getInstance().getCurrentActivity() );
				IABC4GSDItem tmpEco = null;
				for( String s : ecologies ) {
					tmpEco = new ABC4GSDItem( "abc.ecology", s );
					tmpEco.update();
					String usr = ((ArrayList<Long>)tmpEco.get("user")).get(0).toString();
					MasterClientWrapper.getInstance().attachNewAsset(tmpEco, ""+ass.getId(), usr );
				}
			}
		}
		return null; 
	}

	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}

}
