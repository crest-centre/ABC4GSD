package itu.abc4gsd.rcp.client_v6.model;

import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;

public class ABC4GSDStructuredSelection extends StructuredSelection {
	public ABC4GSDStructuredSelection() {}
	public ABC4GSDStructuredSelection(Object[] elements) { super(elements); }
	public ABC4GSDStructuredSelection(Object element) { super(element); }
	public ABC4GSDStructuredSelection(List elements) { super(elements); }
	public ABC4GSDStructuredSelection(List elements, IElementComparer comparer) { super(elements, comparer); }
}
