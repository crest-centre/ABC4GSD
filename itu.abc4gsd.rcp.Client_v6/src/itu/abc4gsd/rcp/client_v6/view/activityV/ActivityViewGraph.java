package itu.abc4gsd.rcp.client_v6.view.activityV;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import itu.abc4gsd.rcp.client_v6.command.HandlerCreateActivity;
 import itu.abc4gsd.rcp.client_v6.draw2d.NodeFigure;
 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDGraphConnection;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDGraphItem;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ActivityViewGraph extends ViewPart implements IZoomableWorkbenchPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.activityGraph";

	private GraphViewer viewer;

	public long currentActivity = 0;
	public long selected = 0;
	private ActivityViewGraphContentProvider cp;
	private LayoutAlgorithm layout;
	
	public ActivityViewGraph() {}
	public void createPartControl(Composite parent) { createGraphViewer(parent); }
	
	private void createGraphViewer(Composite parent) {
		viewer = new GraphViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		final Graph graph = viewer.getGraphControl();
		cp = new ActivityViewGraphContentProvider("ActivityViewGraph", graph);
		viewer.setContentProvider( cp );
		viewer.setLabelProvider(new ActivityViewGraphLabelProvider( this ));
		viewer.setInput(ABC4GSDItemManager.getManager("Activity"));
		layout = setLayout();
		viewer.setLayoutAlgorithm(layout, true);
		viewer.applyLayout();
		fillToolBar();
		
		viewer.addDoubleClickListener( new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object wip = sel.getFirstElement();
				
				if( wip instanceof ABC4GSDGraphItem ) {
					ABC4GSDGraphItem item = (ABC4GSDGraphItem) sel.getFirstElement();
					if( item != null && !item.placeHolder ){
						if( currentActivity == item.getId() ) return;
						
						// Check if you are user of selected activity, if not ask to be linked
						String[] users = MasterClientWrapper.getInstance().query( "abc.user.[abc.state.[abc.state.[].activity.==." +item.getId()+ "].user]._id" );
						boolean present = false;
						for( String tt : users )
							if( tt.equals( ""+MasterClientWrapper.getInstance().getMyId() ) )
								present = true;
						if( ! present ) {
							MessageDialog messageDialog = new MessageDialog(getSite().getShell(), "MessageDialog", null,
							        "You are not a member of the selected activity. Do you want to be included?", MessageDialog.INFORMATION,
							        new String[] { "Yes", "No" }, 0);
							if (messageDialog.open() != 0) return;

							// Creating the state
							IABC4GSDItem newState = new ABC4GSDItem( "abc.state" );
							newState.set( "name", item.getId() + ":" + MasterClientWrapper.getInstance().getMyId() );
							newState.set( "state", Constants.STATE_UNKNOWN );
							newState.attach( "activity", item.getId() );
							newState.attach( "user", MasterClientWrapper.getInstance().getMyId() );
							// Creating the ecology
							String[] assets = MasterClientWrapper.getInstance().query("abc.ecology.[abc.ecology.[].name.~=.{{("+item.getId()+"):[0-9]*}}].asset");
							IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology" );
							newEcology.set( "name", item.getId() + ":" + MasterClientWrapper.getInstance().getMyId() );
							newEcology.attach( "activity", item.getId() );
							newEcology.attach( "user", MasterClientWrapper.getInstance().getMyId() );
							for( String asset : assets ) 
								newEcology.attach( "asset", asset );
						}

						// Suspending the eventual current activity and resuming the selected one
						if( currentActivity != 0 ) cp._suspend( currentActivity );
						cp._resume( item.getId() );
						currentActivity = item.getId();

						BundleContext ctx = FrameworkUtil.getBundle(ActivityViewGraph.class).getBundleContext();
				        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
				        EventAdmin eventAdmin = ctx.getService(ref);
				        Map<String,Object> properties = new HashMap<String, Object>();
				        properties.put("ACT_ID", ""+item.getId());
				        eventAdmin.postEvent( new Event("Activity/Activated", properties) );
						System.out.println("DClick : "+ item.getId() + " - " + item.get("name") );
					}
				} else if( wip instanceof ABC4GSDGraphConnection ) {
					System.out.println("DClick : "+ ((ABC4GSDGraphConnection)wip).label );
				}
			}
		});
		viewer.addSelectionChangedListener( new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object wip = sel.getFirstElement();
				
				if( wip instanceof ABC4GSDGraphItem ) {
					ABC4GSDGraphItem item = (ABC4GSDGraphItem) sel.getFirstElement();
					if( item != null && !item.placeHolder ){
						BundleContext ctx = FrameworkUtil.getBundle(ActivityViewGraph.class).getBundleContext();
				        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
				        EventAdmin eventAdmin = ctx.getService(ref);
				        Map<String,Object> properties = new HashMap<String, Object>();
				        properties.put("ACT_ID", ""+item.getId());
				        eventAdmin.postEvent( new Event("Activity/Selected", properties) );
						System.out.println("Selected: "+ item.getId() + " - " + item.get("name") );
					}
				} else if( wip instanceof ABC4GSDGraphConnection ) {
					System.out.println("Selected : "+ ((ABC4GSDGraphConnection)wip).label );
				}
			}
		});		
		getSite().setSelectionProvider(viewer);
		
        final MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                Action a,b,c,d,e;
                final ABC4GSDGraphItem selected;
            	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            	if (!selection.isEmpty()) {
            		selected = ((ABC4GSDGraphItem) selection.getFirstElement());
            		a = new Action("Modify") {
						public void run() {
					        BundleContext ctx = FrameworkUtil.getBundle(ActivityViewGraph.class).getBundleContext();
					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
					        EventAdmin eventAdmin = ctx.getService(ref);
					        Map<String,Object> properties = new HashMap<String, Object>();
					        properties.put("ACT_ID", ""+selected.getId());
					        eventAdmin.postEvent( new Event("activityModify/asyncEvent", properties) );
						}
					};
        			b = new Action("Create subactivity") {
						public void run() {
					        BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
					        EventAdmin eventAdmin = ctx.getService(ref);
					        Map<String,Object> properties = new HashMap<String, Object>();
					        properties.put("ACT_ID", ""+selected.getId());
					        eventAdmin.postEvent( new Event("activityCreateSub/asyncEvent", properties) );
						}
					};
        			c = new Action("Clone") {
						public void run() {
					        BundleContext ctx = FrameworkUtil.getBundle(ActivityViewGraph.class).getBundleContext();
					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
					        EventAdmin eventAdmin = ctx.getService(ref);
					        Map<String,Object> properties = new HashMap<String, Object>();
					        properties.put("ACT_ID", ""+selected.getId());
					        eventAdmin.postEvent( new Event("activityClone/asyncEvent", properties) );
						}
					};
        			e = new Action("Create") {
						public void run() {
					        BundleContext ctx = FrameworkUtil.getBundle(HandlerCreateActivity.class).getBundleContext();
					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
					        EventAdmin eventAdmin = ctx.getService(ref);
					        Map<String,Object> properties = new HashMap<String, Object>();        
					    	properties.put("ACT_ID", "");
					    	eventAdmin.postEvent( new org.osgi.service.event.Event("activityCreate/asyncEvent", properties) );
						}
					};
            		mgr.add(e);
            		mgr.add(b);
            		mgr.add(c);
            		mgr.add(a);
            	}
            }
        });
        viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		
	    BundleContext ctx = FrameworkUtil.getBundle(ActivityViewGraph.class).getBundleContext();
	    EventHandler handler = new EventHandler() {
	    	public void handleEvent(final Event event) {
	    		final String currentMode = event.getTopic();
	    		final String tmpActivity = event.getProperty("ACT_ID").toString();
	    		System.out.println( "EVENT " + currentMode );
	    		System.out.println( "ID " + tmpActivity );
	    		modifyUI( currentMode, Long.parseLong(tmpActivity) );
	    	}
	    };
	    Dictionary<String,String> properties = new Hashtable<String, String>();
	    properties.put(EventConstants.EVENT_TOPIC, "Activity/*");
	    ctx.registerService(EventHandler.class, handler, properties);
	}

	private LayoutAlgorithm setLayout() {
		LayoutAlgorithm layout;
		layout = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		// RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
		return layout;
	}

	public void setFocus() {}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}

	public void addSelectionChangedListener( ISelectionChangedListener listener ) {
		viewer.addSelectionChangedListener(listener);
	}
	
	public AbstractZoomableViewer getZoomableViewer() { return viewer; }
	private void fillToolBar() {
		ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);
	}

	private void modifyUI( final String event, final long id ) {
		if( PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().getThread() == Thread.currentThread() ) {
			if( event.equals("Activity/Activated") )
				activate(id);
			if( event.equals("Activity/Selected") )
				select(id);
				
		} else {
			PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
	    			if( event.equals("Activity/Activated") )
	    				activate(id);
	    			if( event.equals("Activity/Selected") )
	    				select(id);
				}
			});
		}
	}

	private void activate( long id ) {
//		if(currentActivity == id) return; 
		
		currentActivity = id;
		List<GraphNode> nodes = viewer.getGraphControl().getNodes();
		for( GraphNode n : nodes ) {
			NodeFigure fig = (NodeFigure)n.getNodeFigure();
			if( fig.isActive() ) fig.setActive(false);
			if( ((ABC4GSDGraphItem)n.getData()).getId() == id )
				fig.setActive(true); 
		}
		cp.refreshActivities(id);
		cp.initData(id);
		cp.itemsChanged(null);
	}
	private void select( long id ) {
		if(selected == id) return;
		selected = id;
		List<GraphNode> nodes = viewer.getGraphControl().getNodes();
		for( GraphNode n : nodes ) {
			NodeFigure fig = (NodeFigure)n.getNodeFigure();
			if( fig.isSelected() ) fig.setSelected(false);
			if( ((ABC4GSDGraphItem)n.getData()).getId() == id )
				fig.setSelected(true); 
		}
	}
}
