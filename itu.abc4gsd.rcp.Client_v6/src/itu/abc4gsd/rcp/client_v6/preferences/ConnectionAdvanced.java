package itu.abc4gsd.rcp.client_v6.preferences;

 import itu.abc4gsd.rcp.client_v6.Activator;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class ConnectionAdvanced extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private ScopedPreferenceStore preferences;

	public static final String ADDR_BACKEND		= "prefs_addr_backend";
	public static final String ADDR_LOGGER		= "prefs_addr_logger"; 
	public static final String ADDR_PUBLISHER	= "prefs_addr_publisher";
	public static final String ADDR_CONTROL		= "prefs_addr_control";
	public static final String ADDR_REPOSITORY	= "prefs_addr_repository";
	
	public ConnectionAdvanced() {
		super(GRID);
		preferences = new ScopedPreferenceStore( ConfigurationScope.INSTANCE, Activator.PLUGIN_ID );
		setPreferenceStore(preferences);
	}

	public void createFieldEditors() {
		addField( new StringFieldEditor(ADDR_BACKEND, "Server address:", getFieldEditorParent()) );
		addField( new StringFieldEditor(ADDR_LOGGER, "Logger address:", getFieldEditorParent()) );
		addField( new StringFieldEditor(ADDR_PUBLISHER, "Publisher address:", getFieldEditorParent()) );
		addField( new StringFieldEditor(ADDR_CONTROL, "Control address:", getFieldEditorParent()) );
		addField( new StringFieldEditor(ADDR_REPOSITORY, "General git repository address:", getFieldEditorParent()) );
	}

	public void init(IWorkbench workbench) {}
}
