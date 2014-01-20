package itu.abc4gsd.rcp.client_v6.preferences;

 import itu.abc4gsd.rcp.client_v6.Activator;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class Needs extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private ScopedPreferenceStore preferences;
	public static final String NEEDS		= "prefs_needs";
	public static final String LOCAL_REPO	= "prefs_repo";

	public Needs() {
		super(GRID);
		preferences = new ScopedPreferenceStore( ConfigurationScope.INSTANCE, Activator.PLUGIN_ID );
		setPreferenceStore(preferences);
	}

	public void init(IWorkbench workbench) {}

	protected void createFieldEditors() {
		addField( new MultiLineTextField(NEEDS, "Needs:", getFieldEditorParent()) );
	}
}
