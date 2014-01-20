package itu.abc4gsd.rcp.client_v6.view.notificationV;


import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class NotificationViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	public String getColumnText(Object obj, int index) {
		switch (index) {
			case 0: // nImage
				if (obj instanceof ABC4GSDNotification)
					return ((ABC4GSDNotification) obj).time;
				if (obj != null)
					return obj.toString();
				return "";
			case 1: // nImage
				if (obj instanceof ABC4GSDNotification)
					return ((ABC4GSDNotification) obj).image;
				if (obj != null)
					return obj.toString();
				return "";
			case 2: // nDescription
				if (obj instanceof IABC4GSDItem)
					return ((ABC4GSDNotification) obj).title + " --- " + ((ABC4GSDNotification) obj).body;
				return "";
			default:
				return "";
		}
	}

	public Image getColumnImage(Object obj, int index) {
		switch (index) {
		case 1: // nImage
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_WARNING);
		default:
			return null;
		}
	}

}

