package itu.abc4gsd.rcp.client_v6.perspective;

 import itu.abc4gsd.rcp.client_v6.view.ViewAttributes;
 import itu.abc4gsd.rcp.client_v6.view.ViewManagement;
 import itu.abc4gsd.rcp.client_v6.view.ViewRelation;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveManagement implements IPerspectiveFactory {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.perspective.management";
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);


		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.TOP, 0.25f, layout.getEditorArea());
		topLeft.addView(ViewManagement.ID);
//		layout.addStandaloneView(ViewManagement.ID, false, IPageLayout.LEFT, 0.33f, layout.getEditorArea());

		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.25f, "topLeft");
		topRight.addView(ViewAttributes.ID);
		
		IFolderLayout bottomRight= layout.createFolder("bottomRight", IPageLayout.BOTTOM, 0.50f, "topRight");
		bottomRight.addView(ViewRelation.ID);
	}
}
