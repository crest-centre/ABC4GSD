package itu.abc4gsd.rcp.client_v6.view;

import java.util.ArrayList;
import java.util.List;

 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.model.ABC4GSDStructuredSelection;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.json.simple.JSONArray;


public class ViewAttributes extends ViewPart implements ISelectionListener {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.attributes";

	private class Element {
		public String property;
		public String value;
		public Element( String property, String value ) { this.property = property; this.value = value; }
		public String toString() { return property + ": " + value; }
	}
	private class AttrContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) { return ((List<?>)inputElement).toArray();  }
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	private class AttrLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) { return null; }
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0: // propName
				if (element instanceof Element)
					return ((Element) element).property;
				if (element != null)
					return element.toString();
				return "";
			case 1: // propValue
				if (element instanceof Element)
					return ((Element) element).value;
				return "";
			default:
				return "";
			}
		}
	}

	private TableViewer entityViewer;
	private List<Element> content = new ArrayList<Element>();
	private String baseQuery;
	private int managerOldSize;

	public void createPartControl(Composite parent) {
		
		getSite().getPage().addSelectionListener(this);
		entityViewer = new TableViewer(parent, SWT.BORDER|SWT.FULL_SELECTION);
		
		entityViewer.setLabelProvider(new AttrLabelProvider());
		entityViewer.setContentProvider(new AttrContentProvider());
		entityViewer.setCellModifier(new ICellModifier() {
            public boolean canModify(Object element, String property) { 
            	if( property == "value" )
            		return true;
            	return false;
            }
            public Object getValue(Object element, String property) { return ((Element)element).value; }
            public void modify(Object element, String property, Object value) {
            	TableItem item = (TableItem) element;
                if( ((Element)item.getData()).value == value.toString() ) return;

                ((Element)item.getData()).value = value.toString();
                String tmp = baseQuery + "." + ((Element)item.getData()).property + ".=." + Constants.VAL_DELIM_PRE + ((Element)item.getData()).value + Constants.VAL_DELIM_POST; 
                MasterClientWrapper.getInstance().run(tmp);
                entityViewer.update(item.getData(), null);
            }
        });
		entityViewer.setColumnProperties(new String[] { "property", "value" });
		entityViewer.setCellEditors(new CellEditor[] { new TextCellEditor(entityViewer.getTable()),new TextCellEditor(entityViewer.getTable()) });
        
        TableColumn column = new TableColumn(entityViewer.getTable(),SWT.LEFT);
        column.setWidth(100);
        column.setText("Property");
        
        column = new TableColumn(entityViewer.getTable(),SWT.LEFT);
        column.setWidth(100);
        column.setText("Value");
        
        content = new ArrayList<Element>();
        entityViewer.setInput(content);
        entityViewer.getTable().setLinesVisible(true);
        entityViewer.getTable().setHeaderVisible(true);
	}

	
	public void updateContent( String[] wip ) {
		baseQuery = wip[0] + "." + wip[1] + "." + wip[2];
		System.out.println( "Attribute> " + baseQuery );
		managerOldSize = content.size();
		String tmp;
		content.clear();
		
		JSONArray entities = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(baseQuery).get(Constants.MSG_A)).get(2));
		for( int x=0; x< entities.size(); x++ ) {
			tmp = baseQuery + "." + entities.get(x) + ".?";
			JSONArray fieldInfo = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
			if( fieldInfo.size() == 0 ) {
				tmp = baseQuery + "." + entities.get(x);
				JSONArray tmp2 = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
				String tmpString = "";
				if(tmp2.size()>0)
					tmpString = tmp2.get(0).toString();
				content.add( new Element( (String)entities.get(x), tmpString ) );
			}
		}
	}

	public void updateViewer() {
		entityViewer.getTable().setRedraw(false);
	       try {
	    	   for( int i=managerOldSize-1; i>=0; i-- )
	    		   entityViewer.remove(entityViewer.getElementAt(i));
	    	   entityViewer.add( content.toArray() );
	       }
	       finally {
	    	   entityViewer.getTable().setRedraw(true);
	       }
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if( selection instanceof ABC4GSDStructuredSelection ) {
			if( ((ABC4GSDStructuredSelection) selection).size() != 3 ) return;
			String[] sel = new String[ ((ABC4GSDStructuredSelection) selection).size() ];
			for( int x=0; x<sel.length; x++ )
				sel[x] = (String)((StructuredSelection) selection).toArray()[x];
			updateContent( sel );

			// If this is the UI thread, then make the change.
			if (Display.getCurrent() != null) {
				updateViewer();
				return;
			}
			
			// otherwise, redirect to execute on the UI thread.
			Display.getDefault().asyncExec(new Runnable() {
				public void run() { updateViewer(); }
			});
		}
	}
	public void setFocus() {}
}
