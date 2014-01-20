package itu.abc4gsd.rcp.client_v6.perspective;

 import java.util.HashMap;
import java.util.Map;

import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewGraph;
import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewH;
import itu.abc4gsd.rcp.client_v6.view.artifactV.ArtifactView;
import itu.abc4gsd.rcp.client_v6.view.chatV.ChatViewContainer;
import itu.abc4gsd.rcp.client_v6.view.contactV.ContactView;
import itu.abc4gsd.rcp.client_v6.view.descriptionV.DescriptionView;
import itu.abc4gsd.rcp.client_v6.view.notificationV.NotificationView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;



public class PerspectiveUse implements IPerspectiveFactory {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.perspective.activities";
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);

		IFolderLayout topR = layout.createFolder("topR", IPageLayout.TOP, 0.60f, layout.getEditorArea());
		topR.addView(ActivityViewGraph.ID);

		IFolderLayout topL = layout.createFolder("topL", IPageLayout.LEFT, 0.40f, "topR");
		topL.addView(ActivityViewH.ID);

		IFolderLayout btmLeft = layout.createFolder("btmLeft", IPageLayout.TOP, 0.50f, layout.getEditorArea());
		btmLeft.addView(ContactView.ID);
		
		IFolderLayout btmRight = layout.createFolder("btmRight", IPageLayout.RIGHT, 0.50f, "btmLeft");
		btmRight.addView(ArtifactView.ID);
//		btmRight.addView(ChatViewContainer.ID);

		IFolderLayout bottom = layout.createFolder("notification", IPageLayout.BOTTOM, 0.50f, layout.getEditorArea());
		bottom.addView(NotificationView.ID);

		IFolderLayout bottomBottom = layout.createFolder("chat", IPageLayout.BOTTOM, 0.50f, "notification");
		bottomBottom.addView(ChatViewContainer.ID);

		IFolderLayout topTop = layout.createFolder("topTop", IPageLayout.TOP, 0.25f, "topR");
		topTop.addView(DescriptionView.ID);
		

		layout.getViewLayout(ActivityViewGraph.ID).setCloseable(false);
		layout.getViewLayout(ActivityViewH.ID).setCloseable(false);
		layout.getViewLayout(ContactView.ID).setCloseable(false);
		layout.getViewLayout(ArtifactView.ID).setCloseable(false);
		layout.getViewLayout(NotificationView.ID).setCloseable(false);
		layout.getViewLayout(ChatViewContainer.ID).setCloseable(false);
		
		// org.eclipse.equinox.event is required in the config and set it to autostart=true
		BundleContext ctx = FrameworkUtil.getBundle(PerspectiveUse.class).getBundleContext();
        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
        Map<String,Object> properties = new HashMap<String, Object>();
        EventAdmin eventAdmin = ctx.getService(ref);
        properties.put("VIEW_ID", ""+NotificationView.ID);
        eventAdmin.postEvent( new Event("View/Minimize", properties ) );
        properties.put("VIEW_ID", ""+ChatViewContainer.ID);
//        eventAdmin.postEvent( new Event("View/Minimize", properties ) );
        eventAdmin.postEvent( new Event("View/Detach", properties ) );
	}

}
