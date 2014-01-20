package itu.abc4gsd.rcp.client_v6.view.activityV;

import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;


public class EditingSupportBtnChat extends EditingSupport {

  private final TreeViewer viewer;

  public EditingSupportBtnChat(TreeViewer viewer) {
    super(viewer);
    this.viewer = viewer;
  }

  protected Object getValue(Object element) { return true; }
  protected boolean canEdit(Object element) { return true; }
  protected CellEditor getCellEditor(Object element) { 
	  return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
  }
  protected void setValue(Object element, Object value) {
	  ((ActivityViewHContentProvider)viewer.getContentProvider()).operationChat((IABC4GSDItem)element);
  }

} 