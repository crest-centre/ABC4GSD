package itu.abc4gsd.rcp.client_v6.view.activityV;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.IImageKeys;
import itu.abc4gsd.rcp.client_v6.command.HandlerCreateActivity;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDTreeItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ActivityViewH extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.activityH";

	private TreeViewer viewer;
	private long currentActivity = 0;
	private ActivityViewHContentProvider cp;
	public ActivityViewH() {}
	public void createPartControl(Composite parent) { createTreeViewer(parent); }
		
	private void createTreeViewer(Composite parent) {
		final Tree tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);
		viewer = new TreeViewer(tree);
		ColumnViewerToolTipSupport.enableFor(viewer);
		createColumns( parent, viewer );

		
		
		cp = new ActivityViewHContentProvider("ActivityViewHierarchy", tree);
		viewer.setContentProvider( cp );
		viewer.setInput(ABC4GSDItemManager.getManager("ActivityH"));

		viewer.addDoubleClickListener( new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				
				ABC4GSDTreeItem item = (ABC4GSDTreeItem) sel.getFirstElement();
				if(item != null){
					if( currentActivity == item.getId() ) return;
					System.out.println("Selected : "+ item.getId() + " - " + item.get("name") );
					
					// Check if you are user of selected activity, if not ask to be linked
					String[] users = MasterClientWrapper.getInstance().query( "abc.user.[abc.state.[abc.state.[].activity.==." +item.getId()+ "].user]._id" );
					boolean present = false;
					for( String wip : users )
						if( wip.equals( ""+MasterClientWrapper.getInstance().getMyId() ) )
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
							MasterClientWrapper.getInstance().attachNewAsset(newEcology, asset, ""+MasterClientWrapper.getInstance().getMyId() );
//							newEcology.attach( "asset", asset );
					}
					
					// Suspending the eventual current activity and resuming the selected one
//					if( currentActivity != 0 ) cp._suspend( currentActivity );
					cp._resume( item.getId() );
					currentActivity = item.getId();

					BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
			        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
			        EventAdmin eventAdmin = ctx.getService(ref);
			        Map<String,Object> properties = new HashMap<String, Object>();
			        properties.put("ACT_ID", ""+item.getId());
			        eventAdmin.postEvent( new Event("Activity/Activated", properties) );
				} 
			}
		});
		
		getSite().setSelectionProvider(viewer);
		
        final MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                Action a,b,c,d,e;
                final ABC4GSDTreeItem selected;
            	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            	if (selection.isEmpty()) {
        			a = new Action("Create") {
						public void run() {
					        BundleContext ctx = FrameworkUtil.getBundle(HandlerCreateActivity.class).getBundleContext();
					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
					        EventAdmin eventAdmin = ctx.getService(ref);
					        Map<String,Object> properties = new HashMap<String, Object>();        
					    	properties.put("ACT_ID", "");
					    	eventAdmin.postEvent( new org.osgi.service.event.Event("activityCreate/asyncEvent", properties) );
						}
					};
            		mgr.add(a);            		
            	} else {
            		selected = ((ABC4GSDTreeItem) selection.getFirstElement());
        			a = new Action("Modify") {
						public void run() {
							
					        BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
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
					        BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
					        EventAdmin eventAdmin = ctx.getService(ref);
					        Map<String,Object> properties = new HashMap<String, Object>();
					        properties.put("ACT_ID", ""+selected.getId());
					        eventAdmin.postEvent( new Event("activityClone/asyncEvent", properties) );
						}
					};
        			d = new Action("Remove") {
						public void run() {
							cp._remove(selected.getId());
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
            		mgr.add(d);
            	}
            }
        });
        viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		viewer.expandAll();
		
	    EventHandler handler = new EventHandler() {
	    	public void handleEvent(final Event event) {
	    		final String currentMode = event.getTopic();
	    		final String tmpActivity = event.getProperty("ACT_ID").toString();
	    		System.out.println( "EVENT " + currentMode );
	    		System.out.println( "ID " + tmpActivity );
	    		modifyUI( currentMode, Long.parseLong(tmpActivity) );
	    	}
	    };

	    BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
	    Dictionary<String,String> properties = new Hashtable<String, String>();
	    properties.put(EventConstants.EVENT_TOPIC, "Activity/*");
	    ctx.registerService(EventHandler.class, handler, properties);
	}
	
	private void createColumns(final Composite parent, final TreeViewer viewer) {
		String[] titles = { "Name", "" };
		int[] bounds = { 150, 22 };
		TreeViewerColumn col;

		// column for the name
		col = createTreeViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider( new NameColumnLabelProvider( this ) );		
		col.setEditingSupport( new EditingSupportSingleClick( viewer ) );
		
		// column for the status icon
		col = createTreeViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object obj) { return null; }
			public Image getImage(Object obj) {
				if (obj instanceof IABC4GSDItem) {
					String wip = ((IABC4GSDItem) obj).get("state").toString();
					if( wip.equals( Constants.ACT_INITIALIZED ) )
						return Activator.getImageDescriptor( IImageKeys.ACT_INITIALIZED ).createImage();
					if( wip.equals( Constants.ACT_ONGOING ) )
						return Activator.getImageDescriptor( IImageKeys.ACT_ONGOING ).createImage();
					if( wip.equals( Constants.ACT_UNKNOWN ) )
						return Activator.getImageDescriptor( IImageKeys.ACT_UNKNOWN ).createImage();
					if( wip.equals( Constants.ACT_FINALIZED ) )
						return Activator.getImageDescriptor( IImageKeys.ACT_FINALIZED ).createImage();
				}
				return null;
			}
			public String getToolTipText(Object obj) { 
				if (obj instanceof IABC4GSDItem) {
					String wip = ((IABC4GSDItem) obj).get("state").toString();
					if( wip.equals( Constants.ACT_INITIALIZED ) )
						return "Status: Initialized";
					if( wip.equals( Constants.ACT_ONGOING ) )
						return "Status: Ongoing";
					if( wip.equals( Constants.ACT_UNKNOWN ) )
						return "Status: Unknown";
					if( wip.equals( Constants.ACT_FINALIZED ) )
						return "Status: Finalized";
				}
				return "";
			}
			public Point getToolTipShift(Object object) { return new Point(5, 5); }
			public int getToolTipDisplayDelayTime(Object object) { return 0; } 
			public int getToolTipTimeDisplayed(Object object) { return 5000; }
		});

		// column for the chat btn
		col = createTreeViewerColumn(titles[1], bounds[1], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object obj) { return null; }
			public Image getImage(Object obj) {
				return Activator.getImageDescriptor( IImageKeys.CHAT ).createImage();
			}
			public String getToolTipText(Object element) { return "Open chat"; }
			public Point getToolTipShift(Object object) { return new Point(5, 5); }
			public int getToolTipDisplayDelayTime(Object object) { return 0; } 
			public int getToolTipTimeDisplayed(Object object) { return 5000; }
		});
		col.setEditingSupport( new EditingSupportBtnChat( viewer ) );

		// column for the remove btn
		col = createTreeViewerColumn(titles[1], bounds[1], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object obj) { return null; }
			public Image getImage(Object obj) {
				return Activator.getImageDescriptor( IImageKeys.DELETE_2 ).createImage();
			}
			public String getToolTipText(Object element) { return "Delete activity"; }
			public Point getToolTipShift(Object object) { return new Point(5, 5); }
			public int getToolTipDisplayDelayTime(Object object) { return 0; } 
			public int getToolTipTimeDisplayed(Object object) { return 5000; }
		});
		col.setEditingSupport( new EditingSupportBtnDelete( viewer ) );

	}
	
	private TreeViewerColumn createTreeViewerColumn(String title, int bound, final int colNumber) {
		final TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		final TreeColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
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
	
	private void modifyUI( final String event, final long id ) {
		if( PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().getThread() == Thread.currentThread() ) {
			if( event.equals("Activity/Activated") )
				activate(id);
			if( event.equals("Activity/Selected") )
				try { select(id); } catch (Exception e) {}
				
		} else {
			PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
	    			if( event.equals("Activity/Activated") )
	    				activate(id);
	    			if( event.equals("Activity/Selected") )
	    				try { select(id); } catch (Exception e) {}
				}
			});
		}
	}

	private void activate( long id ) {
		currentActivity = id;
		cp.activate(id);
	}

	private void select( long id ) {
		cp.select(id);
	}
	
	public class NameColumnLabelProvider extends ColumnLabelProvider implements IColorProvider, IFontProvider {
		final ActivityViewH viewer;
		FontRegistry registry; 

		public NameColumnLabelProvider( ActivityViewH viewer ) {
			this.registry = new FontRegistry();
			this.viewer = viewer;
		}
	    public Color getBackground(Object element) { 
			if( element instanceof ABC4GSDTreeItem )
				if( viewer.getCurrentActivity() == ((ABC4GSDTreeItem) element).getId() )
					return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			return null; 
		}
	    public Color getForeground(Object element) { return null; }
		public Font getFont(Object element) {
			if( element instanceof ABC4GSDTreeItem )
				if( viewer.getCurrentActivity() == ((ABC4GSDTreeItem) element).getId() )
					return registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
			return null;
		}
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
						"Status: " + convert(((IABC4GSDItem) obj).get("state").toString()) + "\n" +
						"Active participants: " + ((IABC4GSDItem) obj).get("onlineParticipant").toString() + "\n" +
						"Description: " + ((IABC4GSDItem) obj).get("description").toString();
			return text;
		}
		public Point getToolTipShift(Object object) { return new Point(5, 5); }
		public int getToolTipDisplayDelayTime(Object object) { return 0; } 
		public int getToolTipTimeDisplayed(Object object) { return 5000; }
		
		private String convert(String wip) {
			if( wip.equals( Constants.ACT_INITIALIZED ) )
				return "Initialized";
			if( wip.equals( Constants.ACT_ONGOING ) )
				return "Ongoing";
			if( wip.equals( Constants.ACT_UNKNOWN ) )
				return "Unknown";
			if( wip.equals( Constants.ACT_FINALIZED ) )
				return "Finalized";
			return "";
		}
	}

	public long getCurrentActivity() { return currentActivity; }
}
