package itu.abc4gsd.rcp.client_v6.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class CreateUserDialog extends TitleAreaDialog {	
	protected String[] fieldsName;
	protected List<String> fieldsValue = new ArrayList<String>();
	protected String[] defaults;
	protected String[][] invalid;
	protected List<Text> fieldsCtrl = new ArrayList<Text>();
	protected String title;
	protected String message;
	private List<Combo> fieldsCombos = new ArrayList<Combo>();
	
	public CreateUserDialog(Shell parentShell, String title, String message, String[] fieldsName ) {
		this(parentShell, title, message, fieldsName, null, null);
	}
	public CreateUserDialog(Shell parentShell, String title, String message, String[] fieldsName, String[] defaults ) {
		this(parentShell, title, message, fieldsName, defaults, null);
	}
	public CreateUserDialog(Shell parentShell, String title, String message, String[] fieldsName, String[] defaults, String[][] invalid ) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.fieldsName = fieldsName;
		this.defaults = defaults;
		this.invalid = invalid;
	}

	protected Control createDialogArea(Composite parent) {
		Text tmpTxt;
		Label tmpLbl;
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		int i = 0;
		for(String entry : fieldsName) {
			GridData gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
			tmpLbl = new Label(parent, SWT.NONE);
			tmpLbl.setText(entry);
			tmpTxt = new Text(parent, SWT.BORDER);
			tmpTxt.setLayoutData(gridData);
			if( defaults!= null ) tmpTxt.setText(defaults[i]);
			fieldsCtrl.add( tmpTxt );
			i++;
		}
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tmpLbl = new Label(parent, SWT.NONE);
		tmpLbl.setText("UTC");
		Combo tmpCmb = new Combo(parent, SWT.READ_ONLY);
		tmpCmb.setLayoutData(gridData);
		tmpCmb.add( "- 11" );
		tmpCmb.add( "- 10" );
		tmpCmb.add( "-  9" );
		tmpCmb.add( "-  8" );
		tmpCmb.add( "-  7" );
		tmpCmb.add( "-  6" );
		tmpCmb.add( "-  5" );
		tmpCmb.add( "-  4" );
		tmpCmb.add( "-  3" );
		tmpCmb.add( "-  2" );
		tmpCmb.add( "-  1" );
		tmpCmb.add( "   0" );
		tmpCmb.add( "+  1" );
		tmpCmb.add( "+  2" );
		tmpCmb.add( "+  3" );
		tmpCmb.add( "+  4" );
		tmpCmb.add( "+  5" );
		tmpCmb.add( "+  6" );
		tmpCmb.add( "+  7" );
		tmpCmb.add( "+  8" );
		tmpCmb.add( "+  9" );
		tmpCmb.add( "+ 10" );
		tmpCmb.add( "+ 11" );
		tmpCmb.add( "+ 12" );
		tmpCmb.select( 11 );
		fieldsCombos.add(tmpCmb);
		
		return parent;
	}

	protected void okPressed() {
		saveInput();
		super.okPressed();
	}
	
	private void saveInput() {
		fieldsValue.clear();
		for(Text entry : fieldsCtrl)
			fieldsValue.add(entry.getText());
		for(Combo entry : fieldsCombos)
			fieldsValue.add(entry.getText());
	}
	

	public void create() {
		super.create();
		setTitle(title);
		setMessage(message, IMessageProvider.INFORMATION);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

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
		for(Text entry : fieldsCtrl) {
			if (entry.getText().length() == 0) {
				setErrorMessage("Please fill in all fields");
				valid = false;
			}
		}
		if( invalid != null ) {
			int idx = 0;
			for(Text entry : fieldsCtrl) {
				for( int i=0; i<invalid[idx].length; i++)
					if ( entry.getText().equals(invalid[idx][i]) ) {
						setErrorMessage("Please fill in all fields");
						valid = false;
					}
				idx += 1;
			}
		}
		return valid;
	}
	
	protected boolean isResizable() { return false; }

		public List<String> getValues() {
		return fieldsValue;
	}
	
	public void selectAll( int idx ) {
		fieldsCtrl.get(idx).selectAll();
		fieldsCtrl.get(idx).setFocus();
	}
}

