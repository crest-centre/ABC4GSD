package itu.abc4gsd.rcp.client_v6.command;

 import itu.abc4gsd.rcp.client_v6.dialog.TemplateTextDialog;
 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONArray;

public class HandlerAddSchema implements IHandler {

	public HandlerAddSchema() {}
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String[] fields = new String[] { "Schema name:"};
		String[] defaults = new String[] { "library/data/abcV2.schema" };
		String schema = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run("").get(Constants.MSG_A)).get(2)).toString();
		String msg = "Insert the name of the schema. The schema needs to be already stored server side.\nSchemas already available: " + schema;
		TemplateTextDialog dialog = new TemplateTextDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Schema information", msg, fields, defaults );
		dialog.create();
		if (dialog.open() == Window.OK) {
			String cmd = "+." + dialog.getValues().get(0);
			System.out.println(cmd);
			MasterClientWrapper.getInstance().run(cmd);
		}
		return null;
	}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}
