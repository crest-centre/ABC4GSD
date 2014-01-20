package itu.abc4gsd.eclipse.plugin.middlemanvcs.abc4gsdConnector;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.handlers.IHandlerService;

import itu.abc4gsd.eclipse.core.AppInterface.ABC4GSDItem;
import itu.abc4gsd.eclipse.core.AppInterface.AppInterface;
import itu.abc4gsd.eclipse.core.AppInterface.IABC4GSDItem;

import itu.abc4gsd.eclipse.plugin.middlemanvcs.Activator;
import itu.abc4gsd.eclipse.plugin.middlemanvcs.wrapper.IVCSWrapper;
import itu.abc4gsd.eclipse.plugin.middlemanvcs.wrapper.Subclipse;

public class Connector extends AppInterface {
	private IVCSWrapper vcsWrapper;

	public Connector(String name) {
		super(name);
		vcsWrapper = Subclipse.getInstance();
		log( "MiddleMan - Loading" );
		
		resumeOperation();
		
		log( "MiddleMan - DONE!" );
	}
	
	public void killOperation() { log( "Killing" ); }
	public void suspendOperation() {
		log( "MiddleMan - Suspending" );
		try {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			final Display display = PlatformUI.getWorkbench().getDisplay();
			if (workbench != null && !workbench.isClosing()) {
				display.asyncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow [] workbenchWindows = workbench.getWorkbenchWindows();
						for(int i = 0;i < workbenchWindows.length;i++) {
							IWorkbenchWindow workbenchWindow = workbenchWindows[i];
							if (workbenchWindow == null) {
								// SIGTERM shutdown code must access
								// workbench using UI thread!!
							} else {
								// *** SAVING ALL DIRTY *** NO FORCED CHECKIN 
								IWorkbenchPage[] pages = workbenchWindow.getPages();
								for (int j = 0; j < pages.length; j++) {
									IEditorPart[] dirtyEditors = pages[j].getDirtyEditors();
									for (int k = 0; k < dirtyEditors.length; k++) {
										dirtyEditors[k].doSave(new NullProgressMonitor());
									}
								}
							}
						}
					}
				});
				display.asyncExec(new Runnable() {
					public void run() {
						PlatformUI.getWorkbench().close();
					}
				});
			}
		} catch (IllegalStateException e) {
			// ignore
		}
	}
	
	public void resumeOperation() {
		log( "MiddleMan - Resuming" );
		String q;
		String[] wip;
		
		q = "abc.asset." + _artId + ".ptr";
		wip = query(q);
		
		IABC4GSDItem artifact = new ABC4GSDItem(this, "abc.artifact", wip[0]);
		artifact.update();
		vcsWrapper.setCurrentRepository(""+artifact.get("location"));
		vcsWrapper.setCurrentProject(""+artifact.get("name"));

		final String location = vcsWrapper.getCurrentRepository();
		final String name = vcsWrapper.getCurrentProject();
		
		try {
			if( !vcsWrapper.isProject( name ) ) {
				log("Checking out " + name + " from " + location);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						// *** CHECKOUT ***						
						Subclipse.getInstance().checkout( location, name );
					}
				});
			} else {
				log("Updating " + name + " from " + location);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						// *** UPDATE ***						
						Subclipse.getInstance().update( name );
					}
				});
			}
		} catch (Exception ex) { throw new RuntimeException("Command not found"); }
	}
	public boolean personalHandler(String ch, String msg) {
		log( "Personal " + ch + " - " + msg );
		return true;
	}
}
