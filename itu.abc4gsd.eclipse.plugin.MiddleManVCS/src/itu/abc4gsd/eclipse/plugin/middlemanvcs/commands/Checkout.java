package itu.abc4gsd.eclipse.plugin.middlemanvcs.commands;
import itu.abc4gsd.eclipse.plugin.middlemanvcs.wrapper.Subclipse;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Display;


public class Checkout implements IHandler {
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display.getDefault().asyncExec( new Runnable() {
			@Override
			public void run() {
				String repo = Subclipse.getInstance().getCurrentRepository();
				String prj = Subclipse.getInstance().getCurrentProject();
				if( repo.equals("") || prj.equals("") ) return;
				Subclipse.getInstance().checkout( repo, prj );
			}
		} );
		return null;
	}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}
