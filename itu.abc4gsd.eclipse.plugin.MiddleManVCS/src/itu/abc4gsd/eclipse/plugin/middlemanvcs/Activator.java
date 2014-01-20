package itu.abc4gsd.eclipse.plugin.middlemanvcs;

import itu.abc4gsd.eclipse.plugin.middlemanvcs.abc4gsdConnector.Connector;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "itu.abc4gsd.eclipse.plugin.MiddleManVCS"; //$NON-NLS-1$
	private static Activator plugin;
	private Connector connector = null;
	public Activator() {}
	
	public static Activator getDefault() { return plugin; }

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		log( "Creating connector" );
		connector = new Connector("eclipse");
	}
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}
	
	public void log( String msg ) {
		MessageConsole myConsole = findConsole("Console");
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(msg);
	}

}

