package itu.abc4gsd.rcp.client_v6.dialog;

 import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.IImageKeys;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.logic.Utils;
 import itu.abc4gsd.rcp.client_v6.preferences.Needs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

public class AddArtifactDialog extends TitleAreaDialog {

	private String[] fieldsName;
	private List<String> fieldsValue = new ArrayList<String>();
	public List<Text> fieldsCtrlTxt = new ArrayList<Text>(); 
	private List<Combo> fieldsCtrlCmb = new ArrayList<Combo>(); 
	private String title;
	private String message;
	private boolean uploadUsed = false;
	private List<Integer> trueFalse;
	
	public AddArtifactDialog( Shell parentShell ) {
		super(parentShell);
		this.fieldsName = new String[] { "Name:", "Location:", "Type:", "Attach to current activity:" };  // , "Schema:", "Independent:"
		this.trueFalse = Arrays.asList(3);
		this.title = "Resource information";
		this.message = "Some info about local or uri";
	}

	public void create() {
		super.create();
		setTitle(title);
		setMessage(message, IMessageProvider.INFORMATION);
	}

	protected Control createDialogArea(Composite parent) {
		Text tmpTxt;
		Label tmpLbl;
		Combo tmpCmb;
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		tmpLbl = new Label(parent, SWT.NONE);
		tmpLbl.setText("To upload from local drive.");
		createUploadButton(parent, "Upload");

		for(int i=0; i<fieldsName.length; i++) { //String entry : fieldsName) {
			GridData gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
			tmpLbl = new Label(parent, SWT.NONE);
			tmpLbl.setText(fieldsName[i]);
			if( trueFalse.contains(i) ) {
				tmpCmb = new Combo(parent, SWT.READ_ONLY);
				tmpCmb.add("true");
				tmpCmb.add("false");
				tmpCmb.setLayoutData(gridData);
				fieldsCtrlCmb.add( tmpCmb );
			} else {
				tmpTxt = new Text(parent, SWT.BORDER);
				tmpTxt.setLayoutData(gridData);
				fieldsCtrlTxt.add( tmpTxt );
			}
		}
		if( MasterClientWrapper.getInstance().getCurrentActivity() == -1 ) {
			fieldsCtrlCmb.get(0).setText("false");
			fieldsCtrlCmb.get(0).setEnabled(false);
		} else {
			fieldsCtrlCmb.get(0).setText("true");
		}
		return parent;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.RIGHT;

		parent.setLayoutData(gridData);
		createOkButton(parent, OK, "Ok", true);
		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	protected Button createUploadButton(final Composite parent, String label) {
		// increment the number of columns in the button bar
		GridData twoColInner = new GridData(GridData.VERTICAL_ALIGN_END);
		twoColInner.horizontalSpan = 1;
		twoColInner.grabExcessHorizontalSpace = true;
		twoColInner.horizontalAlignment = SWT.CENTER;

		Button button = new Button(parent, SWT.PUSH);
		button.setImage( Activator.getImageDescriptor( IImageKeys.UPLOAD_2 ).createImage() );
		button.setFont(JFaceResources.getDialogFont());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				uploadPressed(parent);
			} });
		button.setLayoutData(twoColInner);
		return button;
	}

	protected Button createOkButton(Composite parent, int id, String label,
			boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		setButtonLayoutData(button);
		return button;
	}

	private boolean isValidInput() {
		boolean valid = true;
		for(Text entry : fieldsCtrlTxt)
			if (entry.getText().length() == 0) {
				setErrorMessage("Please fill in all fields");
				valid = false;
			}
		for(Combo entry : fieldsCtrlCmb)
			if (entry.getText().length() == 0) {
				setErrorMessage("Please fill in all fields");
				valid = false;
			}
		return valid;
	}
	
	protected boolean isResizable() { return false; }

	private void saveInput() {
		fieldsValue.clear();
		for(Text entry : fieldsCtrlTxt)
			fieldsValue.add(entry.getText());
		for(Combo entry : fieldsCtrlCmb)
			fieldsValue.add(entry.getText());
	}

	public void okPressed() {
		saveInput();
		super.okPressed();
	}

	protected void uploadPressed( Composite parent ) {
		FileDialog fileDialog = new FileDialog(parent.getShell());
		fileDialog.setText("Select File");
		// Open Dialog and save result of selection
		String selected = fileDialog.open();
		if( selected == null ) return;
		upload(selected);
	}
	public void upload(String selected) {
		this.uploadUsed = true;
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String baseDir = prefs.get(Needs.LOCAL_REPO, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Needs.LOCAL_REPO, ""));
		File folder = new File( Platform.getLocation().toFile(), baseDir );
		File f = new File(selected);
		File dest = new File( folder, f.getName() );
		if( dest.exists() ) System.out.println( "File already exists and will be overwritten." );
		try {
			Utils.copyFile( f, dest );
		} catch (IOException e) { e.printStackTrace(); }
		
		fieldsCtrlTxt.get(0).setText(dest.getName());
		fieldsCtrlTxt.get(1).setText(dest.getParent());
		fieldsCtrlTxt.get(2).setText( dest.getName().lastIndexOf(".")>0 ? dest.getName().substring(dest.getName().lastIndexOf(".")) : "");		
		
		System.out.println(selected);
	}

	public List<String> getValues() {
		return fieldsValue;
	}
	
	public String getFileName() { return ( fieldsValue.size() > 0 ? fieldsValue.get(0) : "" ); }
	public String getDirName() { return ( fieldsValue.size() > 1 ? fieldsValue.get(1) : "" ); }
	public void setDirName( String wip ) { fieldsValue.set(1, wip); }
	public String getType() { return ( fieldsValue.size() > 2 ? fieldsValue.get(2) : "" ); }
	public boolean getIndependent() { return !uploadUsed; }
	public boolean getAttached() { return ( fieldsValue.size() > 3 ? (fieldsValue.get(3).toLowerCase().equals("true") ? true : false) : false ); }
	public boolean uploadUsed() { return this.uploadUsed; }
}
