package itu.abc4gsd.rcp.client_v6;

import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveUse;

import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

		public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor( IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() { return PerspectiveUse.ID; }
	
	public boolean preShutdown() { 
		if( (Boolean)OSGIEventHandler.getInstance().get("stateConnected?") ) {
			// Suspend activity if any AND disconnect as user
			MasterClientWrapper.getInstance().quickShutDown();
		}
		return true;
	}
	
	public void initialize(final IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
//		configurer.setSaveAndRestore(true);
	}
}
