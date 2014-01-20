package itu.abc4gsd.rcp.client_v6;


import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import itu.abc4gsd.rcp.client_v6.command.HandlerConnect;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;
import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveUse;
import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewH;
import itu.abc4gsd.rcp.client_v6.view.chatV.ChatViewContainer;
import itu.abc4gsd.rcp.client_v6.view.notificationV.NotificationView;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private Image statusImage;
	private TrayItem trayItem;
	private Image trayImage;
	private ApplicationActionBarAdvisor actionBarAdvisor;

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
//		// Hack to have preferences working correctly
//		DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
//		// Init zmq infrastructure
//		MasterClientWrapper.getInstance();
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
		return actionBarAdvisor;
    }


	public void preWindowOpen() {		
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(550, 850));
        
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(true);
        configurer.setShowPerspectiveBar(false);
        configurer.setTitle("ABC4GSD Client");
//        configurer.setShowMenuBar(true);
   	}
	
	public void postWindowOpen() {
		// Set main window position
		Shell s = getWindowConfigurer().getWindow().getShell();
		s.setLocation(0,0);

		// Removing unwanted pref page
		PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
		pm.remove("org.eclipse.ui.preferencePages.Workbench");

		s.setVisible(true);
		
		initStatusLine();
		loadHiddenViews();
		
//		final IWorkbenchWindow window = getWindowConfigurer().getWindow();
//		trayItem = initTaskItem(window);
//		if (trayItem != null) {
//			hookPopupMenu(window);
//			hookMinimize(window);
//		}

		// To print the ids of the pages
		//		PreferenceManager pm = PlatformUI.getWorkbench( ).getPreferenceManager();
		//		IPreferenceNode[] arr = pm.getRootSubNodes();		        
		//		for(IPreferenceNode pn:arr){
		//		    System.out.println("Label:" + pn.getLabelText() + " ID:" + pn.getId());
		//		}
		
//		try {
//			window.getActivePage().showView(ViewManagement.ID);
//		} catch (PartInitException e) {
//			e.printStackTrace();
//		}		
		// org.eclipse.equinox.event is required in the config and set it to autostart=true

		super.postWindowCreate();

		BundleContext ctx = FrameworkUtil.getBundle(ApplicationWorkbenchWindowAdvisor.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        eventAdmin.postEvent( new Event("State/Connected", new HashMap<String, Object>() ) );		
	}
	
//	private void hookMinimize(final IWorkbenchWindow window) {
//		window.getShell().addShellListener(new ShellAdapter() {
//			public void shellIconified(ShellEvent e) {
//				window.getShell().setVisible(false);
//			}
//		});
//		trayItem.addListener(SWT.DefaultSelection, new Listener() {
//			public void handleEvent(Event event) {
//				Shell shell = window.getShell();
//				if (!shell.isVisible()) {
//					shell.setVisible(true);
//					window.getShell().setMinimized(false);
//				}
//			}
//		});
//	}

//	private void hookPopupMenu(final IWorkbenchWindow window) {
//		// Add listener for menu pop-up
//		trayItem.addListener(SWT.MenuDetect, new Listener() {
//			public void handleEvent(Event event) {
//				MenuManager trayMenu = new MenuManager();
//				Menu menu = trayMenu.createContextMenu(window.getShell());
//				actionBarAdvisor.fillTrayItem(trayMenu);
//				menu.setVisible(true);
//			}
//		});
//	}

	public void dispose() {
		if(statusImage != null)
			statusImage.dispose();
		if(trayImage != null)
			trayImage.dispose();
		if(trayItem != null)
			trayItem.dispose();
	}

//	private TrayItem initTaskItem(IWorkbenchWindow window) {
//		final Tray tray = window.getShell().getDisplay().getSystemTray();
//		if (tray == null)
//			return null;
//		trayItem = new TrayItem(tray, SWT.NONE);
//		trayImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
////		trayImage = (AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, IImageKeys.ABC_16)).createImage();
//		trayItem.setImage(trayImage);
//		trayItem.setToolTipText("Client");
//		return trayItem;
//	}
	
	private void initStatusLine() {
		BundleContext ctx = FrameworkUtil.getBundle(ApplicationWorkbenchWindowAdvisor.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        EventAdmin eventAdmin = ctx.getService(ref);
        eventAdmin.postEvent( new org.osgi.service.event.Event("State/Disconnected", new HashMap<String, Object>()) );
    }

	private void loadHiddenViews() {
		final IWorkbenchPage page = getWindowConfigurer().getWindow().getActivePage();
		try {
			page.showView(NotificationView.ID);
			page.showView(ChatViewContainer.ID);
		} catch (PartInitException e) { e.printStackTrace(); }
		
		//		int s = page.getPartState(ref);
		// state will be restored not minimized ... forcing view hide
		// hack to make notification minimize by forcing a focus
		IViewReference ref = page.findViewReference(ActivityViewH.ID);
		((ActivityViewH)ref.getView(false)).setFocus();

	}
}
