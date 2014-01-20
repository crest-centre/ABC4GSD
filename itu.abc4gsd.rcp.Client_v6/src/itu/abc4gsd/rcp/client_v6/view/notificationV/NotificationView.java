package itu.abc4gsd.rcp.client_v6.view.notificationV;

 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class NotificationView extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.notification";

	private TableViewer viewer;
	private TableColumn nTime;
	private TableColumn nImage;
	private TableColumn nContent;
	private TableColumn nWidget;
	private NotificationViewContentProvider cp;

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
	public NotificationView() {}
	public void createPartControl(Composite parent) { createTableViewer(parent); }
	
	private void createTableViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		final Table table = viewer.getTable();

		TableColumnLayout layout = new TableColumnLayout();
		parent.setLayout(layout);

		nTime = new TableColumn(table, SWT.LEFT);
		nTime.setText("Time");
		layout.setColumnData(nTime, new ColumnWeightData(3));

		nImage = new TableColumn(table, SWT.LEFT);
		layout.setColumnData(nImage, new ColumnWeightData(1));

		nContent = new TableColumn(table, SWT.LEFT);
		nContent.setText("Notification");
		layout.setColumnData(nContent, new ColumnWeightData(9));
		

		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		cp = new NotificationViewContentProvider("NotificationView");
		viewer.setContentProvider(cp);
		viewer.setLabelProvider(new NotificationViewLabelProvider());
		viewer.setInput(ABC4GSDItemManager.getManager("Notification"));
		
		viewer.addDoubleClickListener( new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				ABC4GSDNotification item = (ABC4GSDNotification) sel.getFirstElement();
				if(item != null){
					System.out.println("Selected : "+ item.title + " - " + item.body );
					cp.showNotification(item);
				}
			}
		});

		getSite().setSelectionProvider(viewer);
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}

	public void addSelectionChangedListener( ISelectionChangedListener listener ) {
		viewer.addSelectionChangedListener(listener);
	}
}