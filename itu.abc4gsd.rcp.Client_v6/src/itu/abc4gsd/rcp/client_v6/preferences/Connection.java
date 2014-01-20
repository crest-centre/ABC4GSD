package itu.abc4gsd.rcp.client_v6.preferences;

 import itu.abc4gsd.rcp.client_v6.Activator;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class Connection extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private ScopedPreferenceStore preferences;
	public static final String USER_NAME		= "prefs_user_name";
	public static final String USER_MODEL		= "prefs_user_model";
	public static final String AUTO_LOGIN		= "prefs_auto_login";

	public Connection() { 
		super(GRID); 
		preferences = new ScopedPreferenceStore( ConfigurationScope.INSTANCE, Activator.PLUGIN_ID );
		setPreferenceStore(preferences);
	}

	public void createFieldEditors() {
		addField( new StringFieldEditor(USER_NAME, "User name:", getFieldEditorParent()) );
		addField( new StringFieldEditor(USER_MODEL, "Models to connect to:", getFieldEditorParent()) );
		addField( new BooleanFieldEditor(AUTO_LOGIN, "Automatically login at startup", getFieldEditorParent()) );
	}

	public void init(IWorkbench workbench) {}
}
