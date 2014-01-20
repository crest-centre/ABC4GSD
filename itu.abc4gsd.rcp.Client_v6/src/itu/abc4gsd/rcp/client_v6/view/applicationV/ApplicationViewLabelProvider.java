package itu.abc4gsd.rcp.client_v6.view.applicationV;

import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class ApplicationViewLabelProvider extends LabelProvider implements ITableLabelProvider {
	public String getColumnText(Object obj, int index) {
//		artifact = name:str,location:str,state:str
		switch (index) {
			case 0: // nameColumn
				if (obj instanceof IABC4GSDItem)
					return ((IABC4GSDItem) obj).get("name").toString();
				if (obj != null)
					return obj.toString();
				return "";
			default:
				return "";
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}