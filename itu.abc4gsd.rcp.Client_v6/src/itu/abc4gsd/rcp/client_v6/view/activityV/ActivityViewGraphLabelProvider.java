package itu.abc4gsd.rcp.client_v6.view.activityV;


import itu.abc4gsd.rcp.client_v6.draw2d.NodeFigure;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDGraphItem;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.IFigureProvider;

public class ActivityViewGraphLabelProvider extends LabelProvider implements IFigureProvider {
	final Image image = Display.getDefault().getSystemImage(SWT.ICON_WARNING);
	final ActivityViewGraph viewer;
	
	public ActivityViewGraphLabelProvider( ActivityViewGraph viewer ) {
		this.viewer = viewer;
	}

	public Image getImage(Object element) {
		return image;
	}
	
	public String getText(Object obj) {
		if (obj instanceof ABC4GSDGraphItem) {
			ABC4GSDGraphItem myNode = (ABC4GSDGraphItem) obj;
			return myNode.getLabel();
		}
//		// Not called with the IGraphEntityContentProvider
//		if (obj instanceof MyConnection) {
//			MyConnection myConnection = (MyConnection) obj;
//			return myConnection.getLabel();
//		}

		if (obj instanceof EntityConnectionData) {
			EntityConnectionData test = (EntityConnectionData) obj;
			return "";
		}
		throw new RuntimeException("Wrong type: " + obj.getClass().toString());
	}

	@Override
	public IFigure getFigure(Object element) {
		boolean active = false;
		boolean selected = false;
		NodeFigure result = null;
		
		if( element instanceof ABC4GSDGraphItem ) {
			ABC4GSDGraphItem tmp = (ABC4GSDGraphItem) element;
		    result = new NodeFigure( tmp );
		    result.setSize( -1, -1 );
		    
		    if( !tmp.placeHolder && viewer.currentActivity == tmp.getId() )
		    	active = true;
		    if( !tmp.placeHolder && viewer.selected == tmp.getId() )
		    	selected = true;		    
		    
		    result.setActive(active);
	    	result.setSelected(selected);
		}
		return result;
	}
}


