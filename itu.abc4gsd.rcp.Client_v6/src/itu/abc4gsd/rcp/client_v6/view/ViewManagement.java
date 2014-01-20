package itu.abc4gsd.rcp.client_v6.view;

import java.util.ArrayList;
import java.util.List;

 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.model.ABC4GSDStructuredSelection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.json.simple.JSONArray;

public class ViewManagement extends ViewPart implements ISelectionProvider, ISelectionListener {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.management";

	private Combo schema;
	private Combo entity;
	private org.eclipse.swt.widgets.List entityViewer;
	private Text filter;
	private String[] previous = {"","",""};
	private boolean[] dirty = new boolean[3];
	private boolean forceUpdate = false;
	
	ListenerList listeners = new ListenerList();

	public void createPartControl(Composite parent) {
		createLeftControl(parent);
		getSite().setSelectionProvider(this);
//		getSite().getPage().addSelectionListener(this);
		setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(0) ) );
	}
	
	private void _initSchema() {
		schema.removeAll();
		JSONArray schemas = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run("").get(Constants.MSG_A)).get(2));
		for( int x=0; x< schemas.size(); x++ )
			schema.add( schemas.get(x).toString(), schema.getItems().length );
	}
	private void _initEntity( String tmp ) {
		int idx = entity.getSelectionIndex();
		String wipwip = "";
		if( idx>=0 ) wipwip = entity.getItem(idx);
		entity.clearSelection();
		entity.removeAll();
		JSONArray entities = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int x=0; x< entities.size(); x++ )
			entity.add( entities.get(x).toString(), entity.getItems().length );
		if( idx > 0 && entity.getItems().length > idx && entity.getItem(idx).equals(wipwip)) entity.select(idx);
	}
	private void _initEntityViewer( String tmp ) {
		int idx = entityViewer.getSelectionIndex();
		String wipwip = "";
		if( idx>=0 ) wipwip = entityViewer.getItem(idx);
		entityViewer.deselectAll();
		entityViewer.removeAll();
		JSONArray entities = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int x=0; x< entities.size(); x++ )
			entityViewer.add( entities.get(x).toString(), entityViewer.getItems().length );
		if( idx > 0 && entityViewer.getItems().length > idx && entityViewer.getItem(idx).equals(wipwip)) entityViewer.select(idx);
	}
	
	public void createLeftControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		layout.makeColumnsEqualWidth = true;
		parent.setLayout(layout);

		GridData oneColInner = new GridData(GridData.VERTICAL_ALIGN_END);
		oneColInner.grabExcessHorizontalSpace = true;
		oneColInner.horizontalAlignment = GridData.CENTER;
		
		GridData twoColInner = new GridData(GridData.VERTICAL_ALIGN_END);
		twoColInner.horizontalSpan = 2;
		twoColInner.grabExcessHorizontalSpace = true;
		twoColInner.horizontalAlignment = GridData.FILL;

		schema = new Combo(parent, SWT.NONE);
		schema.setLayoutData(twoColInner);
		entity = new Combo(parent, SWT.NONE);
		entity.setLayoutData(twoColInner);

		filter = new Text(parent, SWT.BORDER | SWT.SEARCH);
		filter.setLayoutData(twoColInner);
		
		entityViewer = new org.eclipse.swt.widgets.List(parent, SWT.BORDER);
		GridData twoColInnerLast = new GridData(GridData.VERTICAL_ALIGN_END);
		twoColInnerLast.horizontalSpan = 2;
		twoColInnerLast.grabExcessHorizontalSpace = true;
		twoColInnerLast.grabExcessVerticalSpace = true;
		twoColInnerLast.verticalAlignment = GridData.FILL;
		twoColInnerLast.horizontalAlignment = GridData.FILL;
		entityViewer.setLayoutData( twoColInnerLast );
		
		schema.addSelectionListener( new SelectionListener() {
			public void widgetSelected(SelectionEvent e) { widgetDefaultSelected(e); }
			public void widgetDefaultSelected(SelectionEvent e) { 
				if( schema.getSelectionIndex() == -1 )
					setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(0) ) );
				else
					setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(1) ) ); 
			}
		});

		entity.addSelectionListener( new SelectionListener() {
			public void widgetSelected(SelectionEvent e) { widgetDefaultSelected(e); }
			public void widgetDefaultSelected(SelectionEvent e) { 
				if( entity.getSelectionIndex() == -1 )
					setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(1) ) );
				else
					setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(2) ) ); 
			}
		});

		entityViewer.addSelectionListener( new SelectionListener() {
			public void widgetSelected(SelectionEvent e) { widgetDefaultSelected(e); }
			public void widgetDefaultSelected(SelectionEvent e) {
				if( entityViewer.getSelectionIndex() == -1 )
					setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(2) ) );
				else
					setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(3) ) ); 
			}
		});

		Button remove = new Button(parent, SWT.PUSH);
		remove.setLayoutData(oneColInner);
		remove.setText("Remove");
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<String> wip = getCurrentSelection(3);
				if( wip.size() != 3 ) return;
				String q = wip.get(0) + "." + wip.get(1) + ".-." + wip.get(2);
				MasterClientWrapper.getInstance().run( q );
				forceUpdate = true;
				setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(3) ) );
//				TODO> What about if linked in other resources???
			}
		});

		Button add = new Button(parent, SWT.PUSH);
		add.setLayoutData(oneColInner);
		add.setText("Add");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<String> wip = getCurrentSelection(2);
				if( wip.size() != 2 ) return;
				MasterClientWrapper.getInstance().run( wip.get(0) + "." + wip.get(1) + ".+" );
				forceUpdate = true;
				setSelection( new ABC4GSDStructuredSelection( getCurrentSelection(2) ) );
			}
		});
	}
	
	public void setFocus() {  }

	public void addSelectionChangedListener(ISelectionChangedListener listener) { listeners.add(listener); }
	public void removeSelectionChangedListener( ISelectionChangedListener listener) { listeners.remove(listener); }
	public ISelection getSelection() { return null; }
	public void setSelection(ISelection selection) {
//		List<String> wip = getCurrentSelection(3);
//		if( wip.size()==0 ) return;
//		getListeners();
		selectionChanged(null, selection);
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++)
			((ISelectionChangedListener) list[i]).selectionChanged(new SelectionChangedEvent(this, (ABC4GSDStructuredSelection)selection )); //new ABC4GSDStructuredSelection( wip)));
	}
	private List<String> getCurrentSelection( int depth ) {
		List<String> resp = new ArrayList<String>();
		for( int x=0; x<depth; x++ ) {
			try {
				if(x==0) {
					if( schema.getSelectionIndex() != -1 )
						resp.add( schema.getItem( schema.getSelectionIndex() ) );
					else
						resp.add( schema.getText() );
				}					
				else if(x==1) {
					if( entity.getSelectionIndex() != -1 )
						resp.add( entity.getItem( entity.getSelectionIndex() ) );
					else
						resp.add( entity.getText() );
				}
				else if(x==2) resp.add( entityViewer.getItem( entityViewer.getSelectionIndex() ) );
			} catch (Exception e) { resp.clear(); }
		}
		return resp;
	}
//	private String getCurrentSelectionString( int depth ) {
//		String resp = "";
//		List<String> wip = getCurrentSelection(depth);
//		for( int x=0; x<depth; x++ ) {
//			if( resp.length() != 0 ) resp += ".";
//			resp += wip.get(x);
//		}
//		return resp;
//	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if( selection instanceof ABC4GSDStructuredSelection ) {
			updateContent((ABC4GSDStructuredSelection) selection);

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

	private void updateContent(ABC4GSDStructuredSelection selection) {
		Object[] wip = selection.toArray();
		if( selection.size() == 0 ) 
			_initSchema();
		for( int x=0; x<wip.length; x++ ) {
			if( !(previous[x].equals((String)wip[x]) ) ) {
				previous[x] = (String)wip[x];
				dirty[x] = true;
			}
		}
	}

	private void updateViewer() {
		for( int x=0; x<dirty.length; x++ ) {
			if( !dirty[x] && !forceUpdate ) continue;
			if( x==0 ) _initEntity( previous[0] );
			if( x==1 ) _initEntityViewer( previous[0] + "." + previous[1] );
			dirty[x] = false;
		}
		forceUpdate = false;
	}

}


















