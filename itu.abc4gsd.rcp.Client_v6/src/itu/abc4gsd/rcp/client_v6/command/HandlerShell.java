package itu.abc4gsd.rcp.client_v6.command;

 import itu.abc4gsd.rcp.client_v6.editor.EditorRun;
 import itu.abc4gsd.rcp.client_v6.editor.EditorRunInput;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class HandlerShell implements IHandler {
	public HandlerShell() {}

	public void addHandlerListener(IHandlerListener handlerListener) {}

	public void dispose() {}

	public Object execute(ExecutionEvent event) throws ExecutionException { 
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		EditorRunInput input = new EditorRunInput("Command");
		try {
			if( page.getActiveEditor() == null ) {
				page.openEditor(input, EditorRun.ID);
			} else {
				if( page.getActiveEditor().getEditorInput().getName().equals("Shell") ) {
					page.closeEditor(page.getActiveEditor(), false);
					page.setEditorAreaVisible(false);
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null; 
	}

	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}
