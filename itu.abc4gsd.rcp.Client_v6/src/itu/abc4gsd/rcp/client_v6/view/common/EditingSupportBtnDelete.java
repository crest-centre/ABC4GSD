package itu.abc4gsd.rcp.client_v6.view.common;

import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;


public class EditingSupportBtnDelete extends EditingSupport {

  private final TableViewer viewer;
  private final IABC4GSDViewerProviders cp;

  public EditingSupportBtnDelete(TableViewer viewer, IABC4GSDViewerProviders cp) {
    super(viewer);
    this.viewer = viewer;
    this.cp = cp;
  }

  protected Object getValue(Object element) { return true; }
  protected boolean canEdit(Object element) { return true; }
  protected CellEditor getCellEditor(Object element) { 
	  return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
  }
  protected void setValue(Object element, Object value) {
	  cp.operationDelete((IABC4GSDItem)element);
	  viewer.update(element, null);
  }

} 