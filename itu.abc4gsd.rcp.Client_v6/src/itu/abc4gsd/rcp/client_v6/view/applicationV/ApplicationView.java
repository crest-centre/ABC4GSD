package itu.abc4gsd.rcp.client_v6.view.applicationV;

import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;



public class ApplicationView extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.application";

	private TableViewer viewer;
	private TableColumn nameColumn;
	private long currentApplication = 0;
	private ApplicationViewContentProvider cp;

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			ABC4GSDItem wip = (ABC4GSDItem) obj;
			return String.valueOf(wip.getId());
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ApplicationView() {
	}
	
	public void createPartControl(Composite parent) {
		createTableViewer(parent);
	}
	
	private void createTableViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL 
										| SWT.MULTI | SWT.FULL_SELECTION);
		final Table table = viewer.getTable();

		TableColumnLayout layout = new TableColumnLayout();
		parent.setLayout(layout);

		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		layout.setColumnData(nameColumn, new ColumnWeightData(9));

		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		cp = new ApplicationViewContentProvider("ApplicationView");
		viewer.setContentProvider(cp);
		viewer.setLabelProvider(new ApplicationViewLabelProvider());
		viewer.setInput(ABC4GSDItemManager.getManager("Application"));

		viewer.addSelectionChangedListener( new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				
				ABC4GSDItem item = (ABC4GSDItem) sel.getFirstElement();
				if(item != null){
					if( currentApplication == item.getId() ) return;
					System.out.println("Selected : "+ item.getId() + " - " + item.get("name") );
					currentApplication = item.getId();
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