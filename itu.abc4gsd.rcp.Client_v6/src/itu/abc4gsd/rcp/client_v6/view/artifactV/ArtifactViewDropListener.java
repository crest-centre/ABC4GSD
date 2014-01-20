package itu.abc4gsd.rcp.client_v6.view.artifactV;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.dialog.AddArtifactDialog;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.preferences.Needs;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.handlers.HandlerUtil;


public class ArtifactViewDropListener extends ViewerDropAdapter {
	private final Viewer viewer;

	public ArtifactViewDropListener(Viewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}
	
	public void drop(DropTargetEvent event) { super.drop(event); }

	public boolean performDrop(Object data) {
		String[] selected = (String[])data;
		long wip;
		for( int i=0; i<selected.length; i++ ) {
			File f = new File(selected[i]);
			List<String> artifacts = Arrays.asList( MasterClientWrapper.getInstance().query( "abc.artifact.[].name" ) );
			if( ! artifacts.contains( f.getName() ) ) {
				AddArtifactDialog dialog = new AddArtifactDialog(viewer.getControl().getShell());
				dialog.create();
				dialog.upload(selected[i]);
				dialog.okPressed();
			
				if( dialog.uploadUsed() )
					uploadArtifact( dialog );
				
				wip = createArtifact(dialog).getId();				
			} else {
				String aa[] = MasterClientWrapper.getInstance().query("abc.artifact.[].name.==.{{" + f.getName() + "}}" ); 
				wip = Long.parseLong( aa[0] );
			}
			if( MasterClientWrapper.getInstance().getCurrentActivity() > 0 )
				attachArtifactToActivity( wip );
		}

		return false;
			
	}
	private void attachArtifactToActivity( long wip ) {
		IABC4GSDItem ass = new ABC4GSDItem( "abc.asset" );
		ass.set("type", "artifact");
		ass.set("ptr", wip);
		String[] ecologies = MasterClientWrapper.getInstance().query("abc.ecology.[abc.ecology.[].name.~=.{{("+ 
				MasterClientWrapper.getInstance().getCurrentActivity() + "):[0-9]*}}]._id");
		IABC4GSDItem tmpEco = null;
		for( String s : ecologies ) {
			tmpEco = new ABC4GSDItem( "abc.ecology", s );
			tmpEco.update();
			String usr = ((ArrayList<Long>)tmpEco.get("user")).get(0).toString();
			MasterClientWrapper.getInstance().attachNewAsset(tmpEco, ""+ass.getId(), usr );
		}		
	}
	private IABC4GSDItem createArtifact( AddArtifactDialog dialog ) {
		String loc; 
		if( dialog.uploadUsed() ) loc = dialog.getDirName().length() > 0 ? ( new File( new File(dialog.getDirName()), dialog.getFileName() ) ).toString() : dialog.getFileName();
		else loc = dialog.getDirName();
		IABC4GSDItem wip = new ABC4GSDItem("abc.artifact");
		wip.set("name", dialog.getFileName() );
		wip.set("location", loc );
		wip.set("type", dialog.getType() );
		wip.set("independent", dialog.getIndependent() ? "true" : "false" );
		return wip;
	}
	private void uploadArtifact( AddArtifactDialog dialog ) {
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
		File folder = new File( Platform.getLocation().toFile(), baseDir );
	
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
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return true;
	}
}

