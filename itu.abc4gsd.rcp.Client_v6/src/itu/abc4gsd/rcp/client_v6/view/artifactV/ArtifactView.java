package itu.abc4gsd.rcp.client_v6.view.artifactV;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.IImageKeys;
import itu.abc4gsd.rcp.client_v6.view.artifactV.EditingSupportBtnDelete;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;



public class ArtifactView extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.artifact";

	private CheckboxTableViewer viewer;
	private ArtifactViewContentProvider cp;
	private ArtifactViewDropListener dl;
	
	/**
	 * The constructor.
	 */
	public ArtifactView() {
	}
	
	public void createPartControl(Composite parent) {
	    GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		layout.makeColumnsEqualWidth = true;
		parent.setLayout(layout);

		createTableViewer(parent);

		GridData oneColInner = new GridData(GridData.VERTICAL_ALIGN_END);
		oneColInner.grabExcessHorizontalSpace = true;
		oneColInner.horizontalAlignment = GridData.CENTER;
		
		GridData twoColInner = new GridData(GridData.VERTICAL_ALIGN_END);
		twoColInner.horizontalSpan = 2;
		twoColInner.grabExcessHorizontalSpace = true;
		twoColInner.horizontalAlignment = GridData.FILL;

		Button addBtn = new Button(parent, SWT.PUSH);
	    addBtn.setImage( Activator.getImageDescriptor( IImageKeys.ADD ).createImage());
	    addBtn.setToolTipText("Add artifact");
	    addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((ArtifactViewContentProvider)viewer.getContentProvider()).operationAdd();
			} 
		});
	    addBtn.setEnabled(false);
	    ((ArtifactViewContentProvider)viewer.getContentProvider()).linkAddBtn( addBtn );
	    addBtn.setLayoutData(oneColInner);

	    Button uploadBtn = new Button(parent, SWT.PUSH);
	    uploadBtn.setImage( Activator.getImageDescriptor( IImageKeys.UPLOAD_2 ).createImage());
	    uploadBtn.setToolTipText("Upload from local drive");
	    uploadBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((ArtifactViewContentProvider)viewer.getContentProvider()).operationUpload();
			} 
		});
	    uploadBtn.setEnabled(false);
	    ((ArtifactViewContentProvider)viewer.getContentProvider()).linkUploadBtn( uploadBtn );
	    uploadBtn.setLayoutData(oneColInner);
	}
	
	private void createTableViewer(Composite parent) {
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(viewer);
		createColumns( parent, viewer );
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		int operations = DND.DROP_MOVE;
	    Transfer[] transferTypes = new Transfer[]{FileTransfer.getInstance()};
	    dl = new ArtifactViewDropListener(viewer);
	    viewer.addDropSupport(operations, transferTypes, dl);

		cp = new ArtifactViewContentProvider("ArtifactView");
		viewer.setContentProvider(cp);
		viewer.setCheckStateProvider(new ArtifactViewCheckedProvider() );
		viewer.setInput(ABC4GSDItemManager.getManager("Artifact"));

		viewer.addCheckStateListener( new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				cp.setCheckedState((ABC4GSDItem)event.getElement(), event.getChecked() );
			} 
		});
		
	    // Make the selection available to other views
		getSite().setSelectionProvider(viewer);
		
	    // Layout the viewer
	    GridData gridData = new GridData();
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.horizontalSpan = 2;
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.horizontalAlignment = GridData.FILL;
	    viewer.getControl().setLayoutData(gridData);
	}

	//This will create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Name", "Location", "Type", "" };
		int[] bounds = { 150, 100, 30, 22 };

	  // 1nd column is for the name
	  TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
	  col.setLabelProvider(new ColumnLabelProvider() {
		  public String getText(Object obj) { 
				if (obj instanceof IABC4GSDItem)
					return ((IABC4GSDItem) obj).get("name").toString();
				if (obj != null)
					return obj.toString();
				return "";
		  }
		  public String getToolTipText(Object obj) {
			  String text = "";
			  if (obj instanceof IABC4GSDItem)
				  text = "Name: " + ((IABC4GSDItem) obj).get("name").toString() + "\n" +
				  "Location: " + ((IABC4GSDItem) obj).get("location").toString() + "\n" +
				  "Type: " + ((IABC4GSDItem) obj).get("type").toString() + "\n";
			  return text;
		  }
		  public Point getToolTipShift(Object object) { return new Point(5, 5); }
		  public int getToolTipDisplayDelayTime(Object object) { return 0; } 
		  public int getToolTipTimeDisplayed(Object object) { return 5000; }
	  });
	  col.setEditingSupport( new EditingSupportBtnLoad( viewer ) );

	  // Location has been put in the tooltip on the name

	  // 3th column is for the type
	  col = createTableViewerColumn(titles[2], bounds[2], 2);
	  col.setLabelProvider(new ColumnLabelProvider() {
		  public String getText(Object obj) {
			  if (obj instanceof IABC4GSDItem)
				  return ((IABC4GSDItem) obj).get("type").toString();
			  return "";
		  }
	  });

	  // 4th column is for the remove btn
	  col = createTableViewerColumn(titles[3], bounds[3], 3);
	  col.setLabelProvider(new ColumnLabelProvider() {
		  public String getText(Object obj) { return null; }
		  public Image getImage(Object obj) {
				return Activator.getImageDescriptor( IImageKeys.DELETE_2 ).createImage();
		  }
		  public String getToolTipText(Object element) { return "Remove artifact"; }
		  public Point getToolTipShift(Object object) { return new Point(5, 5); }
		  public int getToolTipDisplayDelayTime(Object object) { return 0; } 
		  public int getToolTipTimeDisplayed(Object object) { return 5000; }
	  });
	  col.setEditingSupport( new EditingSupportBtnDelete( viewer ) );
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
//		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}
	
	public void setFocus() { viewer.getControl().setFocus(); }
	public IStructuredSelection getSelection() { return (IStructuredSelection) viewer.getSelection(); }
	public void addSelectionChangedListener( ISelectionChangedListener listener ) { viewer.addSelectionChangedListener(listener); }
}