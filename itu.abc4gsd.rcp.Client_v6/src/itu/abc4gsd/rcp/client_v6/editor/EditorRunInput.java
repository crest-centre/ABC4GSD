package itu.abc4gsd.rcp.client_v6.editor;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EditorRunInput implements IEditorInput {
	private String name;

	public EditorRunInput(String name) {
		super();
		this.name = name;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return name;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		if (!(obj instanceof EditorRunInput))
			return false;
		EditorRunInput other = (EditorRunInput) obj;
		return this.name.equals(other.name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String getToolTipText() {
		return name;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}
}

