package itu.abc4gsd.rcp.client_v6.dialog;


import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.command.HandlerConnect;
import itu.abc4gsd.rcp.client_v6.logic.Constants;
import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.preferences.Connection;
import itu.abc4gsd.rcp.client_v6.preferences.ConnectionAdvanced;
import itu.abc4gsd.rcp.client_v6.preferences.Needs;
import itu.abc4gsd.rcp.client_v6.view.abc4gsdPopUpNotification;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONArray;

public class LoginDialog extends Dialog {
	private Text userIdText;
	private String uName;

	public LoginDialog(Shell parentShell) { super(parentShell); }
	public boolean close() { return super.close(); }

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ABC4GSD Client Login");
	}
		
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label accountLabel = new Label(composite, SWT.NONE);
		accountLabel.setText("Account details:");
		accountLabel.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, false, false, 2, 1 ) );
	
		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&User ID:");
		userIdLabel.setLayoutData( new GridData( GridData.END, GridData.CENTER, false, false ) );

		userIdText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		gridData.widthHint = convertHeightInCharsToPixels(20);
		userIdText.setLayoutData(gridData);

		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String msg = "Fill in the form for connecting to the server\n" 
				+ "Server: " + prefs.get(ConnectionAdvanced.ADDR_BACKEND, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_BACKEND, "ERROR")) + "\n" 
				+ "Logger: " + prefs.get(ConnectionAdvanced.ADDR_LOGGER, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_LOGGER, "ERROR")) + "\n" 
				+ "Publisher: " + prefs.get(ConnectionAdvanced.ADDR_PUBLISHER, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_PUBLISHER, "ERROR")) + "\n"
				+ "Control: " + prefs.get(ConnectionAdvanced.ADDR_CONTROL, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(ConnectionAdvanced.ADDR_CONTROL, "ERROR"));

		userIdText.setText( prefs.get(Connection.USER_NAME, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Connection.USER_NAME, "")) );

		return composite;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&Login", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void okPressed() {
		// check non blank field
		if (uName.equals("")) {
			MessageDialog.openError(getShell(), "Invalid User ID", "User ID field must not be blank.");
			selectAll();
			return;
		}
		
		// check user name is registered
		boolean found = false;
		String tmp = "abc.user.[].name";
		JSONArray names = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2));
		for( int z=0; z<names.size(); z++ )
			if( names.get(z).toString().equals( ""+ uName ) ) {
				found = true; 
				break;
			}
		if(!found) {
			MessageDialog.openError(getShell(), "Invalid User ID", "User name not present in the system.  Please log in with a registered user.");
			selectAll();
			return;
		}

		
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		prefs.put(Connection.USER_NAME, userIdText.getText()); 

        super.okPressed();
	}

	protected void buttonPressed(int buttonId) {
		uName = userIdText.getText();
		super.buttonPressed(buttonId);
	}

	public String getConnectionDetails() { return uName; }
	private void selectAll() { userIdText.selectAll(); }
}
