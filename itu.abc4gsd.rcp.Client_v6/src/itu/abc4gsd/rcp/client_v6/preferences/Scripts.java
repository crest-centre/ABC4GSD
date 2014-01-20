package itu.abc4gsd.rcp.client_v6.preferences;

 import itu.abc4gsd.rcp.client_v6.Activator;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class Scripts extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private ScopedPreferenceStore preferences;
	public static final String SCRIPT		= "prefs_scriprs";

	public Scripts() {
		super(GRID);
		preferences = new ScopedPreferenceStore( ConfigurationScope.INSTANCE, Activator.PLUGIN_ID );
		setPreferenceStore(preferences);
	}

	public void init(IWorkbench workbench) {}

	protected void createFieldEditors() {
		addField( new MultiLineTextField(SCRIPT, "Script:", getFieldEditorParent()) );
	}
}
