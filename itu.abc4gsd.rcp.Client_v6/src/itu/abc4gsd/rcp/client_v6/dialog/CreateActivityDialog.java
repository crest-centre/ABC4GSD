package itu.abc4gsd.rcp.client_v6.dialog;

import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityAsset;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityElement;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityInformation;
import itu.abc4gsd.rcp.client_v6.model.IABC4GSDActivityElement;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDTreeItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.json.simple.JSONArray;
import org.eclipse.ui.forms.FormDialog;

public class CreateActivityDialog extends FormDialog {
	private boolean messageSuperShown = false;
	
	private static final int DEPTH = 3;
	private FormToolkit toolkit;
	private Form form;
	private Text ctrlName;
	private Text ctrlDescription;
	private List<TableViewer> viewers = new ArrayList<TableViewer>();
	private List<TreeViewer> treeViewers = new ArrayList<TreeViewer>();
	private List<Table> tables = new ArrayList<Table>();
	private List<Tree> trees = new ArrayList<Tree>();
	private HashMap<String, String> refs = new HashMap<String, String>();
	private String superActivity = "";
	private ABC4GSDTreeItem activities;
	private Label lblCreator;
	private Label lblSelectedSuper;
	private Button ok;
//	private String[] preUsers = new String[]{};
//	private String[] preArtifacts = new String[]{};
//	private String[] preAssets = new String[]{};
//	private String preRelation;
//	private String currentMode;
//	private String currentActivity;

	private long user;
	private String mode;
	private ABC4GSDActivityInformation info;
	private ABC4GSDActivityElement[] availableUsers;
	private ABC4GSDActivityAsset[] availableArtifacts;
	private Section[] sectionBlocks;
	
	public static final String MODE_CLONE = "activityClone";
	public static final String MODE_MODIFY = "activityModify";
	public static final String MODE_CREATE = "activityCreate";
	public static final String MODE_CREATE_SUB = "activityCreateSub";
	
	public CreateActivityDialog(Shell parentShell, String mode, ABC4GSDActivityInformation info, ABC4GSDActivityElement[] users, ABC4GSDActivityAsset[] artifacts ) {
		super(parentShell);
		this.user = MasterClientWrapper.getInstance().getMyId();
		this.mode = mode;
		this.info = info;
		this.availableUsers = users;
		this.availableArtifacts = artifacts;
	}
	public ABC4GSDActivityInformation getInfo() { return this.info; }
	
	protected Control createDialogArea(Composite parent) {
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		GridLayout gl1 = new GridLayout();
		gl1.numColumns = 1;
		TableWrapData td;
		TableWrapLayout layout;
		Section section;
		sectionBlocks = new Section[3];
		Composite sectionClient;
		GridData gd;
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		gd.widthHint = 100;
		Table t;
		Tree tt;
		
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		form.setText("UNDEF                   ");
		  
		layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		layout.numColumns = 2;
		td = new TableWrapData();
		  

		// Creator
		toolkit.createLabel(form.getBody(), "Creator:");
		lblCreator = toolkit.createLabel(form.getBody(), "UNDEF");
		
		// Name
		toolkit.createLabel(form.getBody(), "Name:");
		ctrlName = toolkit.createText(form.getBody(), "");
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		ctrlName.setLayoutData(td);
		
		// Description
		toolkit.createLabel(form.getBody(), "Description:");
		ctrlDescription = toolkit.createText(form.getBody(), "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.heightHint = 50;
		ctrlDescription.setLayoutData(td);

		// Hack for size
		toolkit.createLabel(form.getBody(), "");
		Label tmp = toolkit.createLabel(form.getBody(), "__________________________________________________________");
		tmp.setForeground(form.getBackground());
		tmp.setEnabled(false);


		// Section Super activity
		sectionBlocks[0] = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TREE_NODE);//|Section.EXPANDED);
		section = sectionBlocks[0];
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.colspan = 2;
		section.setLayoutData(td);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() { public void expansionStateChanged(ExpansionEvent e) { expandBlock( e.getState()? 0 : -1 ); } });
		section.setText("Super-activity");
		section.setDescription("If any, select the super-activity. The provided tree contains the activities in which you are either involved or the creator. Each activity satisfaying these requirements, is explored to a depth of 3 (i.e., activity, action, and opearation).");
		sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(gl1);
		Hyperlink link = toolkit.createHyperlink(sectionClient, "Click to remove selection", SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() { 
			public void linkActivated(HyperlinkEvent e) { 
				trees.get(0).deselectAll(); superActivity = "";
				lblSelectedSuper.setText("None");
			} });
		lblSelectedSuper = toolkit.createLabel(sectionClient, "None                ");
		tt = toolkit.createTree(sectionClient, SWT.NONE);
		tt.setLayoutData(gd);
		trees.add(tt);
		toolkit.paintBordersFor(sectionClient);
		section.setClient(sectionClient);
		
		// Section Participants
		sectionBlocks[1] = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TREE_NODE);//|Section.EXPANDED);
		section = sectionBlocks[1];
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.setExpanded(false);
		section.addExpansionListener(new ExpansionAdapter() { public void expansionStateChanged(ExpansionEvent e) { expandBlock( e.getState()? 1 : -1 ); } });
		section.setText("Participants");
		section.setDescription("Include users participating to this activity. Click items to move between lists.");
		sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(gl);
		toolkit.createLabel(sectionClient, "> Included <");
		toolkit.createLabel(sectionClient, "> Available <");
		t = toolkit.createTable(sectionClient, SWT.NULL);
		t.setLayoutData(gd);
		tables.add(t);
		t = toolkit.createTable(sectionClient, SWT.NULL);
		t.setLayoutData(gd);
		tables.add(t);
		toolkit.paintBordersFor(sectionClient);
		section.setClient(sectionClient);
		
		// Section Artifacts
		sectionBlocks[2] = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TREE_NODE);//|Section.EXPANDED);
		section = sectionBlocks[2];
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.setExpanded(false);
		section.addExpansionListener(new ExpansionAdapter() { public void expansionStateChanged(ExpansionEvent e) { expandBlock( e.getState()? 2 : -1 ); } });
		section.setText("Artifacts");
		section.setDescription("Include artifacts that will be visible in this activity. Click items to move between lists.");
		sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(gl);
		toolkit.createLabel(sectionClient, "> Included <");
		toolkit.createLabel(sectionClient, "> Available <");
		t = toolkit.createTable(sectionClient, SWT.NULL);
		t.setLayoutData(gd);
		tables.add(t);
		t = toolkit.createTable(sectionClient, SWT.NULL);
		t.setLayoutData(gd);
		tables.add(t);
		toolkit.paintBordersFor(sectionClient);
		section.setClient(sectionClient);

		// Behavior for lists
		for( int x=0; x<tables.size(); x++ ) {
			final int from = x;
			TableViewer ttmp =  new TableViewer( tables.get(x) );
			viewers.add( ttmp );
			tables.get(x).addSelectionListener( new SelectionListener() {
				public void widgetSelected(SelectionEvent e) { widgetDefaultSelected(e); }
				public void widgetDefaultSelected(SelectionEvent e) {
					moveElement( from, (from%2==0)? from+1 : from-1 );
				}
			});
			ttmp.setLabelProvider( new ColumnLabelProvider(){
				public String getText(Object element) {
					IABC4GSDActivityElement p = (IABC4GSDActivityElement) element;
					return p.getName();
				}
			} );
		}
		  
		// Behavior for trees
		for( int x=0; x<trees.size(); x++ ) {
			treeViewers.add( new TreeViewer( trees.get(x) ) );
			treeViewers.get(x).addSelectionChangedListener( new ISelectionChangedListener() {
			   public void selectionChanged(SelectionChangedEvent event) {
			       // if the selection is empty clear the label
			       if(event.getSelection().isEmpty()) {
			           superActivity = "";
			           lblSelectedSuper.setText("None");
			           return;
			       }
			       if(event.getSelection() instanceof IStructuredSelection) {
			           IStructuredSelection selection = (IStructuredSelection)event.getSelection();
			           ABC4GSDTreeItem selected = ((ABC4GSDTreeItem) selection.getFirstElement());
			           if(selected == null) return;
			           if( selected.label.equals(info.name) ) {
			        	   if( ! messageSuperShown ) {
			        		   MessageDialog messageDialog = new MessageDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), 
			        				   "MessageDialog", null, "Relations can only be established between different activities. Please select an activity different from the current one.", MessageDialog.ERROR,
			        				   new String[] { "Continue" }, 0);
				   				if (messageDialog.open() == 0) {}
				   				messageSuperShown = true;
			        	   }
							lblSelectedSuper.setText("None");
							return;
			           }
			           superActivity = selected.getInfo();
			           lblSelectedSuper.setText( superActivity );
			       }
			   }
			});
		}

		initContent();
		return parent;
	}
	
	
	protected Button createOkButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		ok = new Button(parent, SWT.PUSH);
		ok.setText(label);
		ok.setFont(JFaceResources.getDialogFont());
		ok.setData(new Integer(id));
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(ok);
			}
		}
		setButtonLayoutData(ok);
		return ok;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);
		String label = "";
		if( mode.equals(MODE_CREATE) ) label = "Create a&ctivity";
		if( mode.equals(MODE_CLONE) ) label = "Create a&ctivity";
		if( mode.equals(MODE_CREATE_SUB) ) label = "Create suba&ctivity";
		if( mode.equals(MODE_MODIFY) ) label = "Modify a&ctivity";
		createOkButton(parent, OK, label, true);
		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancelPressed();
			}
		});
	}

	protected void cancelPressed() {
		setReturnCode(CANCEL);
		super.cancelPressed();
	}
	protected void okPressed() {
		saveInput();		
		super.okPressed();
	}
	private void saveInput() {
		info.name = ctrlName.getText().trim();
		info.description = ctrlDescription.getText().trim();
		info.superActivity = lblSelectedSuper.getText().trim() != "None" ? lblSelectedSuper.getText().trim() : "";
		
		info.users = new ABC4GSDActivityElement[ tables.get(0).getItemCount() ];
		info.assets = new ABC4GSDActivityAsset[ tables.get(2).getItemCount() ];
		for( int x=0; x<tables.get(0).getItemCount(); x++ )
			info.users[x] = (ABC4GSDActivityElement)tables.get(0).getItem(x).getData();
		for( int x=0; x<tables.get(2).getItemCount(); x++ )
			info.assets[x] = (ABC4GSDActivityAsset)tables.get(2).getItem(x).getData();
	}
	
	private void moveElement(int from, int to) {
		int idx = tables.get(from).getSelectionIndex();
		if( idx < 0 ) return;
		moveElement(from, to, idx);
	}
	private void moveElement(int from, int to, int idx) {
		IABC4GSDActivityElement wip = (IABC4GSDActivityElement)tables.get(from).getItem(idx).getData();
		viewers.get(to).add(wip);
		tables.get(from).remove(idx);
	}

	private boolean isValidInput() {
		String tmp;
		JSONArray results;

		if( ctrlName.getText().trim().length() == 0 ) {
			MessageDialog messageDialog = new MessageDialog(super.getShell(), "MessageDialog", null,
			        "A name has to be chosen for the activity", MessageDialog.ERROR,
			        new String[] { "Continue" }, 0);
			if (messageDialog.open() == 0) {
				ctrlName.selectAll();
			    ctrlName.setFocus();
			}
			return false;
		}
		if( mode.equals(MODE_CREATE) || mode.equals(MODE_CLONE) || mode.equals(MODE_CREATE_SUB) ) {
			tmp = "abc.activity.[].name";
			try {
				results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
			} catch (Exception e) {
				results = new JSONArray();
			}
			for( Object wip : results )
				if( ctrlName.getText().trim().equals( wip.toString() ) ) {
					MessageDialog messageDialog = new MessageDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), "MessageDialog", null,
					        "An activity with the same name already exists", MessageDialog.ERROR,
					        new String[] { "Continue" }, 0);
					if (messageDialog.open() == 0) {
						ctrlName.selectAll();
					    ctrlName.setFocus();
					}
					return false;
				}
		}
		// Validating the fields ... an activity needs to have at least one user
		if( tables.get(0).getItemCount() == 0 ) {
			MessageDialog messageDialog = new MessageDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), "MessageDialog", null,
			        "An activity needs to have a participant to exist", MessageDialog.ERROR,
			        new String[] { "Continue" }, 0);
			if (messageDialog.open() == 0) {
				ctrlName.selectAll();
			    ctrlName.setFocus();
			}
			return false;
		}

		return true;
	}


	private void initContent() {
		IABC4GSDItem tmpCreator;

		ctrlName.setText(info.name);
		ctrlDescription.setText(info.description);
		lblSelectedSuper.setText( info.superActivity.length() > 0 ? info.superActivity : "None" );
		if( mode.equals(MODE_MODIFY) ) { 
			form.setText("Activity modification");
			tmpCreator = new ABC4GSDItem("abc.user", info.creator, new String[]{"name"});
		} else if( mode.equals(MODE_CREATE) ) {
			form.setText("Activity creation");
			tmpCreator = new ABC4GSDItem("abc.user", user, new String[]{"name"});			
		} else if( mode.equals(MODE_CREATE_SUB) ) {
			form.setText("Subactivity creation");
			tmpCreator = new ABC4GSDItem("abc.user", user, new String[]{"name"});			
		} else {
//			form.setText("Activity creation by clonation of " + info.name);
			form.setText("Activity clonation");
			tmpCreator = new ABC4GSDItem("abc.user", user, new String[]{"name"});
		}
		lblCreator.setText(tmpCreator.get("name").toString());
		
		activities = new ABC4GSDTreeItem(null,null);
		fetchActivities( activities, DEPTH );
		sortActivities( activities );
		
		treeViewers.get(0).setContentProvider(new ITreeContentProvider() {
		        public Object[] getElements(Object inputElement) { return ((ABC4GSDTreeItem) inputElement).children.toArray(); }
		        public void dispose() {}
		        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		        public Object[] getChildren(Object parentElement) { return getElements(parentElement); }
		        public Object getParent(Object element) {
		            if (element == null) return null;
		            return ((ABC4GSDTreeItem) element).parent;
		        }
		        public boolean hasChildren(Object element) { return ((ABC4GSDTreeItem) element).children.size() > 0; }
		});

		treeViewers.get(0).setInput(activities);
		treeViewers.get(0).expandAll();

		// Initializing list of users and assets 
		boolean found = false;
		for( int x=0; x<availableUsers.length; x++ ) {
			found = false;
			for( int y=0; y<info.users.length; y++ )
				if( availableUsers[x].getId() == info.users[y].getId() ) {
					found = true;
					break;
				}
			if( found )
				viewers.get(0).add( availableUsers[x] );
			else
				viewers.get(1).add( availableUsers[x] );
		}
		for( int x=0; x<availableArtifacts.length; x++ ) {
			found = false;
			for( int y=0; y<info.assets.length; y++ ) 
				if( availableArtifacts[x].getArtifactId() == info.assets[y].getArtifactId() ) {
					found = true;
					break;
				}
			if( found )
				viewers.get(2).add( availableArtifacts[x] );
			else
				viewers.get(3).add( availableArtifacts[x] );
		}
	}
	
	private void fetchActivities( ABC4GSDTreeItem current, int depth ) {
		// Element.info contains the id
		// Element.label contains the name
		if( depth == 0 ) return;
		String q;
		String[] toCheck;
		
		if(current.parent == null) {
			q = "abc.activity.[abc.state.[abc.state.[].user.==."+ info.creator +"].activity]._id";
			toCheck = MasterClientWrapper.getInstance().query(q);
			List<String> wip1 = new ArrayList<String>();
			for( String x : toCheck )
				wip1.add(x);
			q = "abc.activity.[].creator.==."+ info.creator;
			toCheck = MasterClientWrapper.getInstance().query(q);
			for( String x : toCheck )
				if(!wip1.contains(x))
					wip1.add(x);
			toCheck = wip1.toArray( new String[ wip1.size() ] );
		} else {
			q = "abc.relation.[].from.==." + current.getInfo();
			String[] wip1 = MasterClientWrapper.getInstance().query(q);
			q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + current.getInfo() + "].to]._id";
			toCheck = MasterClientWrapper.getInstance().query(q);
			if(wip1.length!=toCheck.length) return;
		}
		for( String x : toCheck ) {
			if( !x.equals( "" ) ) {
				ABC4GSDItem tmptmp = new ABC4GSDItem("abc.activity", x, new String[]{ "name", "description" });
				ABC4GSDTreeItem child = new ABC4GSDTreeItem( current, tmptmp );
				fetchActivities(child, depth - 1);
				current.add(child);
			}
		}
	}

	public void sortActivities( ABC4GSDTreeItem current ) {
		boolean found = false;
		List<ABC4GSDTreeItem> results = new ArrayList<ABC4GSDTreeItem>();
		for( ABC4GSDTreeItem wip : current.children ) {
			for( ABC4GSDTreeItem wip2 : current.children ) {
				if( wip.additionalInfo[0].equals( wip2.additionalInfo[0] ) )
					continue;
				if( ABC4GSDTreeItem.isPresent(wip2, wip.additionalInfo[0]) != null ) {
					found = true;
					break;
				}
			}
			if( !found )
				results.add( wip );
			found = false;
		}
		current.children = results;
	}	
	
	public void create() { super.create(); }
	protected boolean isResizable() { return false; }
	public boolean close() {
		try {
			super.close();
			if( !form.isDisposed() ) form.dispose();
			// this generates an error cause the form is already disposed ... dunno how to fix it it is a problem of the form dialog that does not check
		} catch (NullPointerException e) {
		} finally { return true; }
	}

	private void expandBlock( int idx ) {
		for( int x=0; x<sectionBlocks.length; x++ )
			sectionBlocks[x].setExpanded(x==idx);
	}
}





//package itu.abc4gsd.rcp.client_v6.view;
//
//
//import itu.abc4gsd.rcp.client_v6.logic.Constants;
//import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
//import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveCreateActivity;
//import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveNotification;
//import itu.abc4gsd.rcp.client_v6.perspective.PerspectiveUse;
//import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewH;
//import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
//import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDTreeItem;
//import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;
//import itu.abc4gsd.rcp.client_v6.view.notificationV.NotificationView;
//
//import java.util.ArrayList;
//import java.util.Dictionary;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Hashtable;
//import java.util.List;
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.viewers.IContentProvider;
//import org.eclipse.jface.viewers.ISelectionChangedListener;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.ITreeContentProvider;
//import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.jface.viewers.SelectionChangedEvent;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.jface.window.Window;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.Tree;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.part.ViewPart;
//import org.eclipse.ui.dialogs.ElementListSelectionDialog;
//import org.eclipse.ui.forms.events.*;
//import org.eclipse.ui.forms.widgets.Form;
//import org.eclipse.ui.forms.widgets.FormToolkit;
//import org.eclipse.ui.forms.widgets.Hyperlink;
//import org.eclipse.ui.forms.widgets.Section;
//import org.eclipse.ui.forms.widgets.TableWrapData;
//import org.eclipse.ui.forms.widgets.TableWrapLayout;
//import org.json.simple.JSONArray;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.FrameworkUtil;
//import org.osgi.framework.ServiceReference;
//import org.osgi.service.event.Event;
//import org.osgi.service.event.EventAdmin;
//import org.osgi.service.event.EventConstants;
//import org.osgi.service.event.EventHandler;
//
//
//
//public class CreateActivity extends ViewPart {
//	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.createActivity";
//	private static final int DEPTH = 3;
//	private FormToolkit toolkit;
//	private Form form;
//	private Text ctrlName;
//	private Text ctrlDescription;
//	private List<TableViewer> viewers = new ArrayList<TableViewer>();
//	private List<TreeViewer> treeViewers = new ArrayList<TreeViewer>();
//	private List<Table> tables = new ArrayList<Table>();
//	private List<Tree> trees = new ArrayList<Tree>();
//	private HashMap<String, String> refs = new HashMap<String, String>();
//	private long creator = -1; 
//	private String superActivity = "";
//	private ABC4GSDTreeItem activities;
//	private Label lblCreator;
//	private Label lblSelectedSuper;
//	private Button ok;
//	private String[] preUsers = new String[]{};
//	private String[] preArtifacts = new String[]{};
//	private String[] preAssets = new String[]{};
//	private String preRelation;
//	private String currentMode;
//	private String currentActivity;
//	private static final String MODE_CLONE = "activityClone/asyncEvent";
//	private static final String MODE_MODS = "activityModify/asyncEvent";
//	private static final String MODE_CREATE = "activityCreate/asyncEvent";
//	
//	public CreateActivity() {}
//	
//	public void createPartControl(final Composite parent) {
//		createView(parent);
//		
//	    BundleContext ctx = FrameworkUtil.getBundle(CreateActivity.class).getBundleContext();
//	    EventHandler handler = new EventHandler() {
//	    	public void handleEvent(final Event event) {
//	    		currentMode = event.getTopic();
//	    		currentActivity = event.getProperty("ACT_ID").toString();
//	    		System.out.println( "EVENT " +event.getTopic() );
//	    		System.out.println( "ID " + event.getProperty("ACT_ID").toString() );
//	    		Display.getDefault().asyncExec(new Runnable() {
//	    				public void run() {
//	    					final IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
//							if (page.findViewReference(CreateActivity.ID) == null) {
//								try {
//									page.showView(CreateActivity.ID);
//								} catch (PartInitException e) { e.printStackTrace();
//								}
//							}
//	    					initContent();
//	    				}
//	    		});
//	    	}
//	    };
//		
////	    Dictionary<String,String> properties = new Hashtable<String, String>();
////	    properties.put(EventConstants.EVENT_TOPIC, "activityModify/*");
////	    ctx.registerService(EventHandler.class, handler, properties);
////	    properties.put(EventConstants.EVENT_TOPIC, "activityCreate/*");
////	    ctx.registerService(EventHandler.class, handler, properties);
////	    properties.put(EventConstants.EVENT_TOPIC, "activityClone/*");
////	    ctx.registerService(EventHandler.class, handler, properties);
//
//		initContent();
//	}
//	
//	public void createView(final Composite parent) {
//		GridLayout gl = new GridLayout();
//		gl.numColumns = 2;
//		GridLayout gl1 = new GridLayout();
//		gl1.numColumns = 1;
//		TableWrapData td;
//		TableWrapLayout layout;
//		Section section;
//		Composite sectionClient;
//		GridData gd;
//		gd = new GridData(GridData.FILL_BOTH);
//		gd.heightHint = 100;
//		gd.widthHint = 100;
//		Table t;
//		Tree tt;
//		
//		toolkit = new FormToolkit(parent.getDisplay());
//		form = toolkit.createForm(parent);
//		form.setText("UNDEF                   ");
//		  
//		// Menu
//		form.getMenuManager().add(new Action("Clone activity") { public void run() { 
//			System.out.println("Cloning");
//			cloneActivity(); } });
//		form.getMenuManager().add(new Action("Refresh") { public void run() { initContent(); } });
//		  
//		layout = new TableWrapLayout();
//		form.getBody().setLayout(layout);
//		layout.numColumns = 2;
//		td = new TableWrapData();
//		  
//		// Creator
//		toolkit.createLabel(form.getBody(), "Creator:");
//		lblCreator = toolkit.createLabel(form.getBody(), "UNDEF                     ");
//		
//		// Name
//		toolkit.createLabel(form.getBody(), "Name:");
//		ctrlName = toolkit.createText(form.getBody(), "");
//		td = new TableWrapData(TableWrapData.FILL_GRAB);
//		ctrlName.setLayoutData(td);
//		
//		// Description
//		toolkit.createLabel(form.getBody(), "Description:");
//		ctrlDescription = toolkit.createText(form.getBody(), "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
//		td = new TableWrapData(TableWrapData.FILL_GRAB);
//		td.heightHint = 50;
//		ctrlDescription.setLayoutData(td);
//
//		// Section Super activity
//		section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TREE_NODE);//|Section.EXPANDED);
//		td = new TableWrapData(TableWrapData.FILL);
//		td.colspan = 2;
//		section.setLayoutData(td);
//		section.addExpansionListener(new ExpansionAdapter() { public void expansionStateChanged(ExpansionEvent e) {} });
//		section.setText("Super-activity");
//		section.setDescription("If any, select the super-activity. The provided tree contains the activities in which you are either involved or the creator. Each activity satisfaying these requirements, is explored to a depth of 3 (i.e., activity, action, and opearation).");
//		sectionClient = toolkit.createComposite(section);
//		sectionClient.setLayout(gl1);
//		Hyperlink link = toolkit.createHyperlink(sectionClient, "Click to remove selection", SWT.WRAP);
//		link.addHyperlinkListener(new HyperlinkAdapter() { 
//			public void linkActivated(HyperlinkEvent e) { 
//				trees.get(0).deselectAll(); superActivity = "";
//				lblSelectedSuper.setText("None");
//			} });
//		lblSelectedSuper = toolkit.createLabel(sectionClient, "None                ");
//		tt = toolkit.createTree(sectionClient, SWT.NONE);
//		tt.setLayoutData(gd);
//		trees.add(tt);
//		toolkit.paintBordersFor(sectionClient);
//		section.setClient(sectionClient);
//		
//		// Section Participants
//		section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TREE_NODE);//|Section.EXPANDED);
//		td = new TableWrapData(TableWrapData.FILL);
//		td.colspan = 2;
//		section.setLayoutData(td);
//		section.addExpansionListener(new ExpansionAdapter() { public void expansionStateChanged(ExpansionEvent e) {} });
//		section.setText("Participants");
//		section.setDescription("Include users participating to this activity. Click items to move between lists.");
//		sectionClient = toolkit.createComposite(section);
//		sectionClient.setLayout(gl);
//		toolkit.createLabel(sectionClient, "> Included <");
//		toolkit.createLabel(sectionClient, "> Available <");
//		t = toolkit.createTable(sectionClient, SWT.NULL);
//		t.setLayoutData(gd);
//		tables.add(t);
//		t = toolkit.createTable(sectionClient, SWT.NULL);
//		t.setLayoutData(gd);
//		tables.add(t);
//		toolkit.paintBordersFor(sectionClient);
//		section.setClient(sectionClient);
//		
//		// Section Artifacts
//		section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TREE_NODE);//|Section.EXPANDED);		
//		td = new TableWrapData(TableWrapData.FILL);
//		td.colspan = 2;
//		section.setLayoutData(td);
//		section.addExpansionListener(new ExpansionAdapter() { public void expansionStateChanged(ExpansionEvent e) {} });
//		section.setText("Artifacts");
//		section.setDescription("Include artifacts that will be visible in this activity. Click items to move between lists.");
//		sectionClient = toolkit.createComposite(section);
//		sectionClient.setLayout(gl);
//		toolkit.createLabel(sectionClient, "> Included <");
//		toolkit.createLabel(sectionClient, "> Available <");
//		t = toolkit.createTable(sectionClient, SWT.NULL);
//		t.setLayoutData(gd);
//		tables.add(t);
//		t = toolkit.createTable(sectionClient, SWT.NULL);
//		t.setLayoutData(gd);
//		tables.add(t);
//		toolkit.paintBordersFor(sectionClient);
//		section.setClient(sectionClient);
//
//		// Behavior for lists
//		for( int x=0; x<tables.size(); x++ ) {
//			final int from = x;
//			viewers.add( new TableViewer( tables.get(x) ) );
//			tables.get(x).addSelectionListener( new SelectionListener() {
//				public void widgetSelected(SelectionEvent e) { widgetDefaultSelected(e); }
//				public void widgetDefaultSelected(SelectionEvent e) {
//					moveElement( from, (from%2==0)? from+1 : from-1 );
//				}
//			});
//		}
//		  
//		// Behavior for trees
//		for( int x=0; x<trees.size(); x++ ) {
//			treeViewers.add( new TreeViewer( trees.get(x) ) );
//			treeViewers.get(x).addSelectionChangedListener( new ISelectionChangedListener() {
//			   public void selectionChanged(SelectionChangedEvent event) {
//			       // if the selection is empty clear the label
//			       if(event.getSelection().isEmpty()) {
//			           superActivity = "";
//			           lblSelectedSuper.setText("None");
//			           return;
//			       }
//			       if(event.getSelection() instanceof IStructuredSelection) {
//			           IStructuredSelection selection = (IStructuredSelection)event.getSelection();
//			           ABC4GSDTreeItem selected = ((ABC4GSDTreeItem) selection.getFirstElement());
//			           if(selected == null) return;
//			           superActivity = selected.getInfo();
//			           lblSelectedSuper.setText( superActivity );
//			       }
//			   }
//			});
//		}
//
//		// Final Button
//		ok = toolkit.createButton(form.getBody(), "None   ", SWT.PUSH);
//		td = new TableWrapData();
//		td.colspan = 2;
//		ok.setLayoutData(td);
//		ok.addSelectionListener(new SelectionAdapter() { 
//			public void widgetSelected(SelectionEvent e) { 
//				createActivity();
//			} });
//		
//		toolkit.paintBordersFor(form.getBody());
//	}
//
//	private void initContent() {
//		String tmp;
//		JSONArray entities;
//		String value;
//		ABC4GSDItem current = null;
//
//		for( Table t : tables )
//			t.removeAll();
//		activities = new ABC4GSDTreeItem(null,null);
//		fetchActivities( activities, DEPTH );
//		sortActivities( activities );
//		
//		treeViewers.get(0).setContentProvider(new ITreeContentProvider() {
//		        public Object[] getElements(Object inputElement) { return ((ABC4GSDTreeItem) inputElement).children.toArray(); }
//		        public void dispose() {}
//		        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
//		        public Object[] getChildren(Object parentElement) { return getElements(parentElement); }
//		        public Object getParent(Object element) {
//		            if (element == null) return null;
//		            return ((ABC4GSDTreeItem) element).parent;
//		        }
//		        public boolean hasChildren(Object element) { return ((ABC4GSDTreeItem) element).children.size() > 0; }
//		});
//
//		treeViewers.get(0).setInput(activities);
//		treeViewers.get(0).expandAll();
//
//		tmp = "abc.user";
//		entities = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
//		for( int x=0; x< entities.size(); x++ ) {
//			current = new ABC4GSDItem( tmp, entities.get(x).toString(), new String[]{"name"} );
//			value = (current.get("name").toString().length()>0) ? current.get("name").toString() : entities.get(x).toString();
//			viewers.get(1).add( value );
//			refs.put(value, entities.get(x).toString());
//		}
//		
//		tmp = "abc.artifact";
//		entities = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
//		for( int x=0; x< entities.size(); x++ ) {
//			current = new ABC4GSDItem( tmp, entities.get(x).toString(), new String[]{"name"} );
//			value = (current.get("name").toString().length()>0) ? current.get("name").toString() : entities.get(x).toString();
//			viewers.get(3).add( value );
//			refs.put(value, entities.get(x).toString());
//		}
//
//		if( currentMode == MODE_CREATE ) {
//			// Activity Creation
//			form.setText("Activity creation");
//			ok.setText("Create Activity");
//			creator = MasterClientWrapper.getInstance().getMyId();
//			IABC4GSDItem tmpCreator = new ABC4GSDItem("abc.user", creator, new String[]{"name"});
//			lblCreator.setText( tmpCreator.get("name").toString() );
//			ctrlName.setText("");
//			ctrlDescription.setText("");
//			preRelation = "";
//			lblSelectedSuper.setText( "None" );
//			superActivity = preRelation;
//			preUsers = new String[]{ ""+MasterClientWrapper.getInstance().getMyId() };
//			preAssets = new String[]{};
//			preArtifacts = new String[]{};
//		} else if( (currentMode == MODE_MODS)||(currentMode == MODE_CLONE) ) {
//			// Activity Modification
//			current = new ABC4GSDItem( "abc.activity", currentActivity );
//			current.update();
//			lblCreator.setText( "" + ((ArrayList<Long>)current.get("creator")).get(0) );
//			ctrlName.setText( current.get("name").toString() );
//			ctrlDescription.setText( (String)current.get("description") );
//			form.setText("Activity modification");
//			ok.setText("Modify Activity");
//
//			String[] resp;
//			String support = "";			
//			// super activity
//			resp = MasterClientWrapper.getInstance().query("abc.relation.[].to.==." + current.getId() );
//			if( resp.length == 0 ) {
//				preRelation = "";
//				lblSelectedSuper.setText( "None" );
//			} else {
//				resp = MasterClientWrapper.getInstance().query("abc.relation." + resp[0] + ".from");
//				preRelation = resp[0];
//				lblSelectedSuper.setText( preRelation );
//			}
//			superActivity = preRelation;
//			// users
//			preUsers = MasterClientWrapper.getInstance().query( "abc.user.[abc.state.[abc.state.[].activity.==." +current.getId()+ "].user]._id" );
//			// assets
//			preAssets = MasterClientWrapper.getInstance().query( "abc.ecology.[abc.ecology.[].name.==."+current.getId()+":"+preUsers[0]+"].asset" );
//			// artifacts
//			if( preAssets.length == 0 )
//				preArtifacts = new String[]{};
//			else {
//				for( String s : preAssets ) {
//					if( support.length() != 0 )
//						support += ",";
//					support += s;
//				}
//				preArtifacts = MasterClientWrapper.getInstance().query( "abc.asset.["+support+"].ptr" );
//			}
//		}
//		
//		// Initializing list of users and assets 
//		for( String s : preUsers )
//			for( Map.Entry<String, String> r : refs.entrySet() )
//				if( r.getValue().equals(s) )
//					for( int t=0; t<tables.get(1).getItems().length; t++ )
//						if( tables.get(1).getItem(t).getText().equals(r.getKey()) )
//							moveElement(1, 0, t);
//		for( String s : preArtifacts )
//			for( Map.Entry<String, String> r : refs.entrySet() )
//				if( r.getValue().equals(s) )
//					for( int t=0; t<tables.get(3).getItems().length; t++ )
//						if( tables.get(3).getItem(t).getText().equals(r.getKey()) )
//							moveElement(3, 2, t);
//		
//		// zeroing out all varibles as in creation for cloning they become dirty
//		if( currentMode == MODE_CLONE ) {
//			creator = MasterClientWrapper.getInstance().getMyId();
//			IABC4GSDItem tmpCreator = new ABC4GSDItem("abc.user", creator, new String[]{"name"});
//			lblCreator.setText( tmpCreator.get("name").toString() );
//			preUsers = new String[]{};
//			preAssets = new String[]{};
//			preArtifacts = new String[]{};
//			preRelation = "";
//			form.setText("Activity creation by clonation of " + current.get("name").toString());
//			ctrlName.setText( current.get("name").toString() + " COPY" );
//			ok.setText("Create Activity");
//		}
//		
//		// zeroing out preuser for new activity
//		if( currentMode == MODE_CREATE ) preUsers = new String[]{};
//	}
//
//	private void moveElement(int from, int to) {
//		int idx = tables.get(from).getSelectionIndex();
//		if( idx < 0 ) return;
//		moveElement(from, to, idx);
//	}
//	private void moveElement(int from, int to, int idx) {
//		String wip = tables.get(from).getItem(idx).getText();
//		viewers.get(to).add(wip);
//		tables.get(from).remove(idx);
//	}
//
//	private void createActivity() {
//		// Initializing local variables to avoid referencing controls
//		String name = ctrlName.getText();
//		String description = ctrlDescription.getText();
//		String[] users = new String[ tables.get(0).getItemCount() ];
//		String[] artifacts = new String[ tables.get(2).getItemCount() ];
//		
//		for( int x=0; x<tables.get(0).getItemCount(); x++ )
//			users[x] = refs.get(tables.get(0).getItem(x).getText());
//		for( int x=0; x<tables.get(2).getItemCount(); x++ )
//			artifacts[x] = refs.get(tables.get(2).getItem(x).getText());
//
//		String tmp;
//		JSONArray results;
//		Long id;
//		
//		// Validating the fields ... name needs to be not empty and unique.
//		if( name.trim().length() == 0 ) {
//			MessageDialog messageDialog = new MessageDialog(getSite().getShell(), "MessageDialog", null,
//			        "A name has to be chosen for the activity", MessageDialog.ERROR,
//			        new String[] { "Continue" }, 0);
//			if (messageDialog.open() == 0) {
//				ctrlName.selectAll();
//			    ctrlName.setFocus();
//			}
//			return;
//		}
//		if( currentMode == MODE_CREATE || currentMode == MODE_CLONE ) {
//			tmp = "abc.activity.[].name";
//			try {
//				results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
//			} catch (Exception e) {
//				results = new JSONArray();
//			}
//			for( Object wip : results )
//				if( name.equals( wip.toString() ) ) {
//					MessageDialog messageDialog = new MessageDialog(getSite().getShell(), "MessageDialog", null,
//					        "An activity with the same name already exists", MessageDialog.ERROR,
//					        new String[] { "Continue" }, 0);
//					if (messageDialog.open() == 0) {
//						ctrlName.selectAll();
//					    ctrlName.setFocus();
//					}
//					return;
//				}
//		}
//		// Validating the fields ... an activity needs to have at least one user
//		if( users.length == 0 ) {
//			MessageDialog messageDialog = new MessageDialog(getSite().getShell(), "MessageDialog", null,
//			        "An activity needs to have a participant to exist", MessageDialog.ERROR,
//			        new String[] { "Continue" }, 0);
//			if (messageDialog.open() == 0) {
//				ctrlName.selectAll();
//			    ctrlName.setFocus();
//			}
//			return;
//		}
//		
//		// Create activity
//		IABC4GSDItem newAct;
//		if( currentMode == MODE_CREATE || currentMode == MODE_CLONE )
//			newAct = new ABC4GSDItem( "abc.activity" );
//		else {
//			newAct = new ABC4GSDItem( "abc.activity", currentActivity );
//			newAct.update();
//		}
//		
//		// Setting the straightforward fields if modified (in case of new activity the base is empty field)
//		if( !name.equals(newAct.get("name")) ) newAct.set("name", name);
////		TODO> check
//		if( !description.equals(newAct.get("description")) ) newAct.set("description", description);
//		if( ((String)newAct.get("state")).length()==0 ) newAct.set("state", Constants.ACT_UNKNOWN);
//		if( !(""+creator).equals(newAct.get("creator")) ) newAct.attach("creator", creator);
//
//		// Creating the relations
//		if( preRelation.length() > 0 && !superActivity.equals(preRelation) )
//			MasterClientWrapper.getInstance().query("abc.relation.-.[abc.relation.[].name.==."+preRelation+":"+newAct.getId()+"]");
//		if( superActivity.length() > 0 && !superActivity.equals(preRelation) ) {
//			IABC4GSDItem newRelation = new ABC4GSDItem( "abc.relation" );
//			newRelation.set("name", superActivity+":"+newAct.getId());
//			newRelation.set("type", 0);
//			newRelation.attach("from", superActivity);
//			newRelation.attach("to", newAct.getId());
//		} 
//		
//		// Checking if users have been removed
//		for( String wip : preUsers ) {
//			boolean found = false;
//			// Checking if user is still in ...
//			for( String u : users )
//				if( wip.equals(u) ) {
//					found = true;
//					break;
//				}
//			// ... if not remove state/info relation class instance and ecology
//			if( !found ) {
//				MasterClientWrapper.getInstance().query("abc.state.-.[abc.state.[].name.==."+newAct.getId()+":"+wip+"]");
//				MasterClientWrapper.getInstance().query("abc.ecology.-.[abc.ecology.[].name.==."+newAct.getId()+":"+wip+"]");
//				// TODO > Properties are still there
//				// NOTE > If the last user was removed, the activity should not be created and this is taken care above at the very beginning.
//			}
//		}
//		// Adding new user in a similar manner
//		for( String wip : users ) {
//			boolean found = false;
//			for( String u : preUsers )
//				if( wip.equals(u) ) {
//					found = true;
//					break;
//				}
//			if( !found ) {
//				IABC4GSDItem newState = new ABC4GSDItem( "abc.state" );
//				id = newState.getId();
//				newState.set( "name", newAct.getId() + ":" + wip );
//				newState.set( "state", Constants.STATE_UNKNOWN );
//				newState.attach( "activity", newAct.getId() );
//				newState.attach( "user", wip );
//			}
//		}
//		
//		// Wrap artifacts with assets. Same as with users.
//		for( int wip=0; wip<preArtifacts.length; wip++ ) {
//			boolean found = false;
//			for( String u : artifacts )
//				if( preArtifacts[wip].equals(u) ) {
//					found = true;
//					break;
//				}
//			if( !found ) {
//				// TODO > Properties are still there
//				MasterClientWrapper.getInstance().query("abc.ecology.[abc.ecology.[].name.~=.{{("+newAct.getId()+"):[0-9]*}}].asset.-."+preAssets[wip]);
//				MasterClientWrapper.getInstance().query("abc.asset.-."+preAssets[wip]);
//			}
//		}
//
//		List<String> assets = new ArrayList<String>(); 
//		for( String wip : artifacts ) {
//			boolean found = false;
//			for( String u : preArtifacts )
//				if( wip.equals(u) ) {
//					found = true;
//					break;
//				}
//			if( !found ) {
//				IABC4GSDItem newAsset = new ABC4GSDItem( "abc.asset" );
//				id = newAsset.getId();
//				newAsset.set( "type", "artifact" );
//				newAsset.set( "ptr", wip );
//				assets.add(id.toString());
//			}
//		}
//		
//		// Bind everything with Ecology entities
//		for( String user : users ) {
//			boolean found = false;
//			for( String u : preUsers )
//				if( user.equals(u) ) {
//					found = true;
//					break;
//				}
//			if( !found ) {
//				IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology" );
//				newEcology.set( "name", newAct.getId() + ":" + user );
//				newEcology.attach( "activity", newAct.getId() );
//				newEcology.attach( "user", user );
//				for( String asset : assets ) 
//					newEcology.attach( "asset", asset );
//			} else {
//				IABC4GSDItem newEcology = new ABC4GSDItem( "abc.ecology", MasterClientWrapper.getInstance().query("abc.ecology.[].name.==."+newAct.getId()+":"+user )[0] );
//				for( String asset : assets ) { 
//					boolean found2 = false;
//					for( String u : preAssets )
//						if( asset.equals(u) ) {
//							found2 = true;
//							break;
//						}
//					if( !found2 )
//						newEcology.attach( "asset", asset );
//				}
//			}
//		}
//		// If activity state is unknown, set to initialized
//		// This is to separate the creation stage from the period in which it has not been resumed
//		if( ((String)newAct.get("state")).equals( Constants.ACT_UNKNOWN ) ) newAct.set("state", Constants.ACT_INITIALIZED);
//		
//		BundleContext ctx = FrameworkUtil.getBundle(ActivityViewH.class).getBundleContext();
//      ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
//      EventAdmin eventAdmin = ctx.getService(ref);
//      Map<String,Object> properties = new HashMap<String, Object>();
//      properties.put("VIEW_ID", CreateActivity.ID);
//      eventAdmin.postEvent( new Event("View/Hide", properties) );
//      
//		// Sending Events
//		if( currentMode == MODE_CREATE ) {
//	        eventAdmin.postEvent( new Event("State/Act_Created", new HashMap<String, Object>()) );
//		}
//
//      
//		return;
//	}
//
//	@Override
//	public void setFocus() {
////		viewer.getControl().setFocus();
//	}
//
//	
//	private void fetchActivities( ABC4GSDTreeItem current, int depth ) {
//		// Element.info contains the id
//		// Element.label contains the name
//		if( depth == 0 ) return;
//		String q;
//		String[] toCheck;
//		
//		if(current.parent == null) {
//			q = "abc.activity.[abc.state.[abc.state.[].user.==."+ creator +"].activity]._id";
//			toCheck = MasterClientWrapper.getInstance().query(q);
//			List<String> wip1 = new ArrayList<String>();
//			for( String x : toCheck )
//				wip1.add(x);
//			q = "abc.activity.[].creator.==."+ creator;
//			toCheck = MasterClientWrapper.getInstance().query(q);
//			for( String x : toCheck )
//				if(!wip1.contains(x))
//					wip1.add(x);
//			toCheck = wip1.toArray( new String[ wip1.size() ] );
//		} else {
//			q = "abc.relation.[].from.==." + current.getInfo();
//			String[] wip1 = MasterClientWrapper.getInstance().query(q);
//			q = "abc.activity.[abc.relation.[abc.relation.[].from.==." + current.getInfo() + "].to]._id";
//			toCheck = MasterClientWrapper.getInstance().query(q);
//			if(wip1.length!=toCheck.length) return;
//		}
//		for( String x : toCheck ) {
//			if( !x.equals( "" ) ) {
//				ABC4GSDItem tmptmp = new ABC4GSDItem("abc.activity", x, new String[]{ "name", "description" });
//				ABC4GSDTreeItem child = new ABC4GSDTreeItem( current, tmptmp );
//				fetchActivities(child, depth - 1);
//				current.add(child);
//			}
//		}
//	}
//
//	private boolean checkActivityId( String check ) {
//		String[] toCheck = MasterClientWrapper.getInstance().query( "abc.activity" );
//		for( String wip : toCheck )
//			if( wip.equals(check) )
//				return true;
//		return false;
//	}
//
//	public void sortActivities( ABC4GSDTreeItem current ) {
//		boolean found = false;
//		List<ABC4GSDTreeItem> results = new ArrayList<ABC4GSDTreeItem>();
//		for( ABC4GSDTreeItem wip : current.children ) {
//			for( ABC4GSDTreeItem wip2 : current.children ) {
//				if( wip.additionalInfo[0].equals( wip2.additionalInfo[0] ) )
//					continue;
//				if( ABC4GSDTreeItem.isPresent(wip2, wip.additionalInfo[0]) != null ) {
//					found = true;
//					break;
//				}
//			}
//			if( !found )
//				results.add( wip );
//			found = false;
//		}
//		current.children = results;
//	}
//	
//	public void cloneActivity() {
//		JSONArray results;
//		String tmp = "abc.activity.[].name";
//		try { results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
//		} catch (Exception e) { results = new JSONArray(); }
//		String[] currentActivities = new String[ results.size() ];
//		for( int i=0; i<currentActivities.length; i++ )
//			currentActivities[i] = results.get(i).toString();
//
//		ElementListSelectionDialog dialog = new ElementListSelectionDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), new LabelProvider());
//		dialog.setElements(currentActivities);
//		dialog.setTitle("Select the activity to clone");
//		if (dialog.open() != Window.OK)
//			return;
//		String selected = dialog.getResult()[0].toString();
//		String idx = MasterClientWrapper.getInstance().query("abc.activity.[].name.==."+selected)[0];
//
//		currentMode = MODE_CLONE;
//		currentActivity = idx;
//		initContent();
//		ctrlName.selectAll();
//	}
//}
//
//
//
//
//