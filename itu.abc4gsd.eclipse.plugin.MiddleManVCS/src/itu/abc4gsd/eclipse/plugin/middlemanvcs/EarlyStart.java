package itu.abc4gsd.eclipse.plugin.middlemanvcs;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;


public class EarlyStart implements IStartup {
	public void earlyStartup() {
//		Activator activator = Activator.getDefault();
//		activator.getLog().log( new Status(0, "AA", "ALIVE Early") );
//		System.out.println("");
	}
}

//public class MyEarlyActivator implements IStartup {
//	public void earlyStartup() {
////		OSGiAgentServiceFactory factory = new OSGiAgentServiceFactory();
////		try {
////			OSGiAgentService service = factory.getAgentServiceObject();
////			if (service != null) {
////				service.connectToManagementServer();
////			} else {
////				System.err.println("No OSGiAgentService Available");
////			}
////		} catch (BundleException e) {
////			e.printStackTrace();
////		}
//	}
//}
