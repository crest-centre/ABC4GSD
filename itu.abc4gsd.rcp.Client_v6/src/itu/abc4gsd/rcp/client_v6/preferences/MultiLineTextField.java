package itu.abc4gsd.rcp.client_v6.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;

public class MultiLineTextField extends FieldEditor {
	private static final int VERTICAL_DIALOG_UNITS_PER_CHAR = 20;
	private static final int LIST_HEIGHT_IN_CHARS = 10;
	private static final int LIST_HEIGHT_IN_DLUS = LIST_HEIGHT_IN_CHARS * VERTICAL_DIALOG_UNITS_PER_CHAR;

	private Composite top;
	private Text text;
	
	public MultiLineTextField( String name, String labelText, Composite parent) { super(name, labelText, parent); }
	
	protected void adjustForNumColumns(int numColumns) { ((GridData)top.getLayoutData()).horizontalSpan = numColumns; }
	
	protected void doFillIntoGrid(Composite parent, int numColumns) { 
		top = parent;
	
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		top.setLayoutData(gd);
	
		Label label = getLabelControl(top);
		GridData labelData = new GridData();
		labelData.horizontalSpan = numColumns;
		label.setLayoutData(labelData);
	
		text = new Text(top, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL );
	
		// Create a grid data that takes up the extra 
		// space in the dialog and spans both columns.
		GridData listData = new GridData(GridData.FILL_HORIZONTAL);
		listData.heightHint = convertVerticalDLUsToPixels(text, LIST_HEIGHT_IN_DLUS);
		listData.horizontalSpan = numColumns;
		text.setLayoutData(listData);		
	}
	
	protected void doLoad() { 
		String items = getPreferenceStore().getString(getPreferenceName());
		setText(items);
	}
	
	protected void doLoadDefault() { 
		String items = getPreferenceStore().getDefaultString(getPreferenceName());
		setText(items);
	}

	private void setText(String items) { text.setText(items); }

	protected void doStore() { 
		String s = text.getText();
		if (s != null)
			getPreferenceStore().setValue(getPreferenceName(), s);
	}
	
	public int getNumberOfControls() { return 1; /* the text field */ }
}

