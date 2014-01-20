package itu.abc4gsd.rcp.client_v6.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.PlatformUI;


public class HandlerResetPerspective implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();
		return null; 
	}

	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}

}
