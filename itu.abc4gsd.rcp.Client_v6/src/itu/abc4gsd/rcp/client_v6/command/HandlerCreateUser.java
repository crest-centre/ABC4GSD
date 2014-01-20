package itu.abc4gsd.rcp.client_v6.command;

 import itu.abc4gsd.rcp.client_v6.dialog.CreateUserDialog;
 import itu.abc4gsd.rcp.client_v6.dialog.TemplateTextDialog;
 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.json.simple.JSONArray;

public class HandlerCreateUser implements IHandler {

	public HandlerCreateUser() {}
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void dispose() {}
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Getting existing users' names
		JSONArray results;
		String tmp = "abc.user.[].name";
		try { results = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(tmp).get(Constants.MSG_A)).get(2)); } 
		catch (Exception e) { results = new JSONArray(); }
		String[] wip = new String[results.size()];
		for( int i=0; i<wip.length; i++ )
			wip[i] = results.get(i).toString();
		
		String[] fields = new String[] { "User name:"};
		String[] defaults = new String[] { "Paolo" };
		String[][] invalid = new String[][] { wip };
		String users = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run("abc.user.[].name").get(Constants.MSG_A)).get(2)).toString();
		String msg = "Insert the name of the user.\nCurrent users: " + users;
		CreateUserDialog dialog = new CreateUserDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "User information", msg, fields, defaults, invalid );
		dialog.create();
		if (dialog.open() == Window.OK) {
			IABC4GSDItem newUser = new ABC4GSDItem( "abc.user" );
			newUser.set("name", dialog.getValues().get(0));
			newUser.set("state", Constants.USR_UNKNOWN);
		}
		return null;
	}
	public boolean isEnabled() { return true; }
	public boolean isHandled() { return true; }
	public void removeHandlerListener(IHandlerListener handlerListener) {}
}


