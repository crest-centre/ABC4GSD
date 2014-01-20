package itu.abc4gsd.rcp.client_v6.view.descriptionV;

 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class DescriptionView extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.description";

	Text description;
	private ListenerDescription listener;

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			ABC4GSDNotification wip = (ABC4GSDNotification) obj;
			return String.valueOf(wip.getId());
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	public DescriptionView() {}
	public void createPartControl(Composite parent) { 
	    parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    parent.setLayout(new GridLayout(1, true));
//	    createButtons(parent);
	    
	    description = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL );
	    description.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    listener = new ListenerDescription(description);
	}

	public void setFocus() {}
}