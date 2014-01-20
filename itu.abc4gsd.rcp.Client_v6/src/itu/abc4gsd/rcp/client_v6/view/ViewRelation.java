package itu.abc4gsd.rcp.client_v6.view;

 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.model.ABC4GSDStructuredSelection;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.ViewPart;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ViewRelation extends ViewPart implements ISelectionListener {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.relation";
	
	private class Element {
		public String label;
		public Element parent;
		public List<Element> children;
		public String[] info;
		public Element( Element parent, String label ) { 
			this.parent = parent; 
			this.label = label;
			this.children = new ArrayList<Element>();
		}
		public boolean isCategory() { return parent.label == null; }
		public boolean isLeaf() { return children == null; }
		public String getInfo() {
			String ret = "";
			for( String wip : info ) {
				if(ret.length()!=0) ret += ", ";
				ret += wip;
			}
			return ret; 
		}
		public String toString() { return label; }
	}

	private class RelContentProvider implements ITreeContentProvider {
        public Object[] getElements(Object inputElement) { return ((Element) inputElement).children.toArray(); }
        public void dispose() {}
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
        public Object[] getChildren(Object parentElement) { return getElements(parentElement); }
        public Object getParent(Object element) {
            if (element == null) return null;
            return ((Element) element).parent;
        }
        public boolean hasChildren(Object element) { return ((Element) element).children.size() > 0; }

    }

	private TreeViewer relationViewer;
	private Element content = new Element(null,null);
	private String baseQuery;
	private String[] baseQueryList;

	public void createPartControl(final Composite parent) {
		
		getSite().getPage().addSelectionListener(this);
		relationViewer = new TreeViewer(parent, SWT.BORDER|SWT.FULL_SELECTION);
		relationViewer.getTree().setLinesVisible(true);
		relationViewer.getTree().setHeaderVisible(true);
		
        ColumnViewerToolTipSupport.enableFor(relationViewer);

        CellLabelProvider labelProvider = new StyledCellLabelProvider() {
            public String getToolTipText(Object element) {
            	String res = ((Element)element).getInfo();
            	if( ((Element)element).isCategory() ) return res;
            	res += "\n\n";
            	IABC4GSDItem wip = new ABC4GSDItem(((Element)element).getInfo(), ((Element)element).toString());
            	wip.update();
            	String[] keys = wip.getKeys();
        		for( int x=0; x< keys.length; x++ )
        			res += "" + keys[x] + ": " + wip.get(keys[x]) + "\n";
            	return res;
            }
            public Point getToolTipShift(Object object) { return new Point(5,5); }
            public int getToolTipDisplayDelayTime(Object object) { return 0; }
            public int getToolTipTimeDisplayed(Object object) { return 10000; }
            public void update(ViewerCell cell) {
            	StyledString text = new StyledString();
            	text.append(cell.getElement().toString());
            	if(((Element)cell.getElement()).isCategory())
            		text.append(" ( " + ((Element)cell.getElement()).children.size() + " ) ", StyledString.COUNTER_STYLER);
        		cell.setText(text.toString());
        		cell.setStyleRanges(text.getStyleRanges());
        		super.update(cell);
            }
        };
        relationViewer.setLabelProvider(labelProvider);
        
        relationViewer.setContentProvider(new RelContentProvider());
        relationViewer.setInput(content);
        
        final MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                Action a;
                final Element selected;
            	IStructuredSelection selection = (IStructuredSelection) relationViewer.getSelection();
            	if (!selection.isEmpty()) {
            		selected = ((Element) selection.getFirstElement());
            		if( selected.isCategory() ) {
            			a = new Action("Attach") {
									public void run() {
										String msg = "Select elemtents to attach";
										String tmp = baseQuery.substring(0, baseQuery.indexOf(".")) +"."+ selected.info[0];
										JSONArray wipwip = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
										List<String> ids = new ArrayList<String>();
										for( int xx=0; xx<wipwip.size(); xx++ )
											ids.add(wipwip.get(xx).toString());
										List<String> already = new ArrayList<String>();
										for(int x=0; x<selected.children.size(); x++)
											already.add(selected.children.get(x).label);
										for( String el : already )
											ids.remove(el);

										ElementListSelectionDialog dialog = new ElementListSelectionDialog( parent.getShell(), new LabelProvider());
										dialog.setElements(ids.toArray(new String[0]));
										dialog.setTitle(msg);
										dialog.setMultipleSelection(true);

										if (dialog.open() == Window.OK) {
											tmp = baseQuery + "." + selected.label + ".+.";
											for( Object el : dialog.getResult() )
												MasterClientWrapper.getInstance().run( tmp + el.toString() );
										}										
		            					updateContent(baseQueryList);
										updateViewer();
									}
            					};
            		} else {  
						a = new Action("Detach") {
								public void run() {
										MasterClientWrapper.getInstance().run(baseQuery + "." + selected.parent.label + ".-." + selected.label);									
		            					updateContent(baseQueryList);
		            					updateViewer();
									}
								};
            		}
            		mgr.add(a);
            	}
            }
        });
        relationViewer.getControl().setMenu(mgr.createContextMenu(relationViewer.getControl()));
	}

	
	public void updateContent( String[] wip ) {
    	baseQueryList = wip;
		baseQuery = wip[0] + "." + wip[1] + "." + wip[2];
		System.out.println( "Relation> " + baseQuery );
		content = new Element(null,null); 
		String tmp;
		String model = wip[0];

		IABC4GSDItem current = new ABC4GSDItem(wip[0] + "." + wip[1], wip[2]);
		current.update();
		String[] keys = current.getType();
		for( int x=0; x< keys.length; x++ ) {
			if( !current.simpleType(keys[x]) ) {
				Element cat = new Element(content,keys[x]);
				cat.info = current.getType(keys[x]);
				content.children.add(cat);
				tmp = baseQuery + "." + keys[x];
				try {
					Object tmptmp = current.get(keys[x]);
					ArrayList<Long> tmp2 = (ArrayList<Long>)current.get(keys[x]);
					for( int y=0; y< tmp2.size(); y++ ) {
						Element child = new Element(cat, tmp2.get(y).toString());
						child.info = new String[1];
						child.info[0] = model + "." + current.getType(keys[x])[0];
						cat.children.add(child);
					}
				} catch (Exception e) {}
			}
		}
	}
	
	public void updateViewer() {
		relationViewer.setInput(content);
		relationViewer.expandAll();
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
