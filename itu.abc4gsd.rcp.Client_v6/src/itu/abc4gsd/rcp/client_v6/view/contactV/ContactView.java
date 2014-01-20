package itu.abc4gsd.rcp.client_v6.view.contactV;


 import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.IImageKeys;
import itu.abc4gsd.rcp.client_v6.dialog.AddArtifactDialog;
import itu.abc4gsd.rcp.client_v6.dialog.CreateActivityDialog;
import itu.abc4gsd.rcp.client_v6.dialog.TemplateTextDialog;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityAsset;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityElement;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityInformation;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDStructuredSelection;
import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewH;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItemManager;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDTreeItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;



public class ContactView extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.contact";

	private TableViewer viewer;

	public void createPartControl(Composite parent) {
	    GridLayout layout = new GridLayout(1, false);
	    parent.setLayout(layout);

		createTableViewer(parent);
	    GridData layout2 = new GridData(GridData.VERTICAL_ALIGN_END);
	    layout2.grabExcessHorizontalSpace = true;
	    Button addBtn = new Button(parent, SWT.PUSH);
	    addBtn.setImage( Activator.getImageDescriptor( IImageKeys.ADD ).createImage());
	    addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((ContactViewContentProvider)viewer.getContentProvider()).operationAdd();
			}
		});
	    addBtn.setEnabled(false);
	    ((ContactViewContentProvider)viewer.getContentProvider()).linkAddBtn( addBtn );
	}
	
	private void createTableViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(viewer);
		createColumns( parent, viewer );
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		viewer.setContentProvider(new ContactViewContentProvider("ContactView"));
		viewer.setInput(ABC4GSDItemManager.getManager("Contact"));

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
	    
        final MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                Action a;
                final ABC4GSDItem[] selected;
            	IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            	if (!selection.isEmpty()) {
            		selected = Arrays.copyOf(selection.toArray(), selection.toArray().length, ABC4GSDItem[].class);
        			a = new Action("Chat") {
						public void run() {
							long currAct = MasterClientWrapper.getInstance().getCurrentActivity();
							ABC4GSDActivityInformation info = new ABC4GSDActivityInformation();
							boolean found = false;
							info.creator = MasterClientWrapper.getInstance().getMyId();
							info.name = "Discussion (";
							info.description = "This activity has been created to support the discussion space requested and keep track of it. Participants:\n";
							info.superActivity = currAct == -1 ? "": ""+currAct;
							info.assets = new ABC4GSDActivityAsset[]{};
							// checking if user is there
							for( int i=0; i<selected.length; i++ ) 
								if( selected[i].getId() == MasterClientWrapper.getInstance().getMyId() ) {
									found = true;
									break;
								}

							info.users = new ABC4GSDActivityElement[selected.length + (found ? 0 : 1)];
							for( int i=0; i<selected.length; i++ ) {
								if( selected[i].getId() == MasterClientWrapper.getInstance().getMyId() ) found = true;
								info.users[i] = new ABC4GSDActivityElement(selected[i].getId(), selected[i].get("name").toString());
								info.description += " - " + selected[i].get("name").toString() + "\n";
								info.name += selected[i].get("name").toString() + ", ";
							}
							if( ! found ) {
								info.users[info.users.length - 1] = new ABC4GSDActivityElement(
										MasterClientWrapper.getInstance().getMyId(), 
										MasterClientWrapper.getInstance().query( "abc.user." + 
												MasterClientWrapper.getInstance().getMyId() + ".name" )[0] );
								info.description += " - " + info.users[info.users.length-1].getName() + "\n";
								info.name += info.users[info.users.length-1].getName() + ", ";
							}
							info.name = info.name.substring(0, info.name.length()-2) + ")";

							currAct = MasterClientWrapper.getInstance().createActivity(
									CreateActivityDialog.MODE_CREATE, null, new ABC4GSDActivityInformation(), info );
							
							((ContactViewContentProvider)viewer.getContentProvider()).sendCommand("CHAT_OPEN " + currAct );
							
							final long act = currAct;
							final String desc = info.description;
							new Timer().schedule(new TimerTask() {          
							    public void run() {
									JSONObject content = new JSONObject();
									content.put("activity", act);
									content.put("message", desc);
							    	((ContactViewContentProvider)viewer.getContentProvider()).sendCommand("CHAT_WRITE " + content.toJSONString());							    }
							}, 2000);
//					        BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
//					        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
//					        EventAdmin eventAdmin = ctx.getService(ref);
//					        Map<String,Object> properties = new HashMap<String, Object>();
//					        properties.put("ACT_ID", ""+selected.getId());
//					        eventAdmin.postEvent( new Event("activityModify/asyncEvent", properties) );
						}
					};
            		mgr.add(a);
        			a = new Action("Ping") {
						public void run() {
							String message = "";
							
							String[] fields = new String[] { "Message:" };
							final String[] defaults = new String[] { "" };
							String header = "Insert a message to be delievered together with the notification";
							TemplateTextDialog dialog = new TemplateTextDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), "ABC4GSD Ping System", header, fields, defaults );

							dialog.create();
							if (dialog.open() == Window.OK)
								message = dialog.getValues().get(0); 

							IABC4GSDItem msg = new ABC4GSDItem( "chat.message");
							MasterClientWrapper.getInstance().query("chat.message." + msg.getId() + ".timestamp.=.?TIME?");
							msg.set("text", message);

							
							IABC4GSDItem ping = new ABC4GSDItem( "notification.notification" );
							ping.set( "name", ping.getId() + ":" + MasterClientWrapper.getInstance().getMyId() + ":" + MasterClientWrapper.getInstance().getCurrentActivity() );
							ping.attach( "msg", msg.getId() );
							ping.attach( "activity", MasterClientWrapper.getInstance().getCurrentActivity() );
							ping.attach( "from", MasterClientWrapper.getInstance().getMyId() );
							for( int i=0; i<selected.length; i++ )
								ping.attach( "to", selected[i].getId() );
						}
					};
            		mgr.add(a);
            		
            	}
            }
        });
        viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));

	}

	//This will create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
	  String[] titles = { "", "Name", "Working on...", "", "" };
	  int[] bounds = { 22, 100, 100, 22, 22 };

	  // 1st column is for the state
	  TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
	  col.setLabelProvider(new StatusColumnLabelProvider( this ) );
//	  col.setEditingSupport(new EditingSupportBtnRemove(viewer));

	  // 2nd column is for the name
	  col = createTableViewerColumn(titles[1], bounds[1], 1);
	  col.setLabelProvider(new NameColumnLabelProvider(this));
//	  col.setLabelProvider(new ColumnLabelProvider() {
//		  public String getText(Object obj) { 
//				if (obj instanceof IABC4GSDItem)
//					return ((IABC4GSDItem) obj).get("name").toString();
//				if (obj != null)
//					return obj.toString();
//				return "";
//		  }
//	  });

	  // 3rd column is for the working
	  col = createTableViewerColumn(titles[2], bounds[2], 2);
	  col.setLabelProvider(new ArtifactColumnLabelProvider(this) );
//		  public String getText(Object obj) {
//			  if (obj instanceof IABC4GSDItem)
//				  return ((IABC4GSDItem) obj).get("artifact").toString();
//			  return "";
//		  }
//	  });

	  // 5th column is for the remove btn
	  col = createTableViewerColumn(titles[3], bounds[3], 3);
	  col.setLabelProvider(new RmvButtonColumnLabelProvider(this));
//		  public String getText(Object obj) { return null; }
//		  public Image getImage(Object obj) {
//				return Activator.getImageDescriptor( IImageKeys.DELETE_2 ).createImage();
//		  }
//		  public String getToolTipText(Object element) { return "Remove participant"; }
//		  public Point getToolTipShift(Object object) { return new Point(5, 5); }
//		  public int getToolTipDisplayDelayTime(Object object) { return 0; } 
//		  public int getToolTipTimeDisplayed(Object object) { return 5000; }
//	  });
	  col.setEditingSupport( new EditingSupportBtnDelete( viewer ) );
	}
	
	
	public class StatusColumnLabelProvider extends GeneralColumnLabelProvider {
		public StatusColumnLabelProvider( ContactView viewer ) { super(viewer); }
		  public String getText(Object obj) { return null; }
		  public Image getImage(Object obj) {
				if (obj instanceof IABC4GSDItem) {
					String wip = ((IABC4GSDItem) obj).get("state").toString();
					if( wip.equals( Constants.USR_UNKNOWN ) )
						return Activator.getImageDescriptor( IImageKeys.UNKNOWN ).createImage(); 
					if( wip.equals( Constants.USR_CONNECTED ) )
						return Activator.getImageDescriptor( IImageKeys.ONLINE ).createImage(); 
					if( wip.equals( Constants.USR_DISCONNECTED ) )
						return Activator.getImageDescriptor( IImageKeys.OFFLINE ).createImage();
				}
				return null;
		  }
		  public String getToolTipText(Object obj) { 
			  if (obj instanceof IABC4GSDItem) {
				  String wip = ((IABC4GSDItem) obj).get("state").toString();
				  if( wip.equals( Constants.USR_UNKNOWN ) )
					  return "Unknown"; 
				  if( wip.equals( Constants.USR_CONNECTED ) )
					  return "Connected"; 
				  if( wip.equals( Constants.USR_DISCONNECTED ) )
					  return "Disconnected";
			  }
			  return "";
		  }
		  public Point getToolTipShift(Object object) { return new Point(5, 5); }
		  public int getToolTipDisplayDelayTime(Object object) { return 0; } 
		  public int getToolTipTimeDisplayed(Object object) { return 5000; }
	}
	public class NameColumnLabelProvider extends GeneralColumnLabelProvider {
		public NameColumnLabelProvider( ContactView viewer ) { super(viewer); }
		public String getText(Object obj) { 
			if (obj instanceof IABC4GSDItem)
				return ((IABC4GSDItem) obj).get("name").toString();
			if (obj != null)
				return obj.toString();
			return "";
		}
	}
	public class ArtifactColumnLabelProvider extends GeneralColumnLabelProvider {
		public ArtifactColumnLabelProvider( ContactView viewer ) { super(viewer); }
		public String getText(Object obj) { 
		  if (obj instanceof IABC4GSDItem)
			  return ((IABC4GSDItem) obj).get("artifact").toString();
		  return "";
		}
	}
	public class RmvButtonColumnLabelProvider extends GeneralColumnLabelProvider {
		public RmvButtonColumnLabelProvider( ContactView viewer ) { super(viewer); }
		  public String getText(Object obj) { return null; }
		  public Image getImage(Object obj) {
				return Activator.getImageDescriptor( IImageKeys.DELETE_2 ).createImage();
		  }
		  public String getToolTipText(Object element) { return "Remove participant"; }
		  public Point getToolTipShift(Object object) { return new Point(5, 5); }
		  public int getToolTipDisplayDelayTime(Object object) { return 0; } 
		  public int getToolTipTimeDisplayed(Object object) { return 5000; }
		}
	
	public class GeneralColumnLabelProvider extends ColumnLabelProvider implements IColorProvider {
		final ContactView viewer;
		
		public GeneralColumnLabelProvider( ContactView viewer ) {
			this.viewer = viewer;
		}
		public String getText(Object obj) { 
			return "";
		}
	    public Color getBackground(Object element) { 
			if( element instanceof ABC4GSDItem )
				if( MasterClientWrapper.getInstance().getMyId() == ((ABC4GSDItem) element).getId() )
					return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			return null; 
		}
	    public Color getForeground(Object element) { return null; }

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
	
	public ABC4GSDActivityElement[] getUsers(  ) {
		String[] ids;
		String support;
		ABC4GSDActivityElement[] resp;
		
		support = "abc.user";
		ids = MasterClientWrapper.getInstance().query(support);
		resp = new ABC4GSDActivityElement[ ids.length ];
		for( int i=0; i< ids.length; i++ ) {
			support = MasterClientWrapper.getInstance().query( "abc.user."+ids[i]+".name" )[0];
			resp[i] = new ABC4GSDActivityElement(ids[i], support);
		}    					
		return resp;
	}
	public ABC4GSDActivityAsset[] getAssets() {
		String[] ids;
		String support;
		ABC4GSDActivityAsset[] resp;
		
		support = "abc.artifact";
		ids = MasterClientWrapper.getInstance().query(support);
		resp = new ABC4GSDActivityAsset[ ids.length ];
		for( int i=0; i< ids.length; i++ ) {
			support = MasterClientWrapper.getInstance().query( "abc.artifact."+ids[i]+".name" )[0];
			resp[i] = new ABC4GSDActivityAsset(""+-1, ids[i], support);
		}
		return resp;
	}

	
}


//private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
//SelectionAdapter selectionAdapter = new SelectionAdapter() {
//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		comparator.setColumn(index);
//		int dir = comparator.getDirection();
//		viewer.getTable().setSortDirection(dir);
//		viewer.getTable().setSortColumn(column);
//		viewer.refresh();
//	}
//};
//return selectionAdapter;
//}


//private void hookContextMenu() {
//MenuManager menuMgr = new MenuManager("#PopupMenu");
//menuMgr.setRemoveAllWhenShown(true);
//menuMgr.addMenuListener(new IMenuListener() {
//	public void menuAboutToShow(IMenuManager manager) {
//		ContactView.this.fillContextMenu(manager);
//	}
//});
//Menu menu = menuMgr.createContextMenu(viewer.getControl());
//viewer.getControl().setMenu(menu);
//getSite().registerContextMenu(menuMgr, viewer);
//}

//private void contributeToActionBars() {
//IActionBars bars = getViewSite().getActionBars();
//fillLocalPullDown(bars.getMenuManager());
//fillLocalToolBar(bars.getToolBarManager());
//}

//private void fillLocalPullDown(IMenuManager manager) {
//manager.add(action1);
//manager.add(new Separator());
//manager.add(action2);
//}

//private void fillContextMenu(IMenuManager manager) {
//manager.add(action1);
//manager.add(action2);
//// Other plug-ins can contribute there actions here
//manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//}
//
//private void fillLocalToolBar(IToolBarManager manager) {
//manager.add(action1);
//manager.add(action2);
//}

//private void makeActions() {
//action1 = new Action() {
//	public void run() {
//		showMessage("Action 1 executed" + model.size());
//		model.add(new ABC4GSDItem( "abc.user", 789 ));
//	}
//};
//action1.setText("Action 1");
//action1.setToolTipText("Action 1 tooltip");
//action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//	getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
//
//action2 = new Action() {
//	public void run() {
//		showMessage("Action 2 executed");
//	}
//};
//action2.setText("Action 2");
//action2.setToolTipText("Action 2 tooltip");
//action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//		getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
//doubleClickAction = new Action() {
//	public void run() {
//		ISelection selection = viewer.getSelection();
//		Object obj = ((IStructuredSelection)selection).getFirstElement();
//		showMessage("Double-click detected on "+obj.toString());
//	}
//};
//}

//private void hookDoubleClickAction() {
//viewer.addDoubleClickListener(new IDoubleClickListener() {
//	public void doubleClick(DoubleClickEvent event) {
//		doubleClickAction.run();
//	}
//});
//}
//private void showMessage(String message) {
//MessageDialog.openInformation(
//	viewer.getControl().getShell(),
//	"Contact View",
//	message);
//}