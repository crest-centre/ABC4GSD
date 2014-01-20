package itu.abc4gsd.rcp.client_v6.logic;


import itu.abc4gsd.rcp.client_v6.Activator;
import itu.abc4gsd.rcp.client_v6.IImageKeys;
import itu.abc4gsd.rcp.client_v6.dialog.CreateActivityDialog;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityAsset;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityElement;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityInformation;
import itu.abc4gsd.rcp.client_v6.preferences.Scripts;
import itu.abc4gsd.rcp.client_v6.sourceProvider.StateProvider;
import itu.abc4gsd.rcp.client_v6.view.activityV.ActivityViewH;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.notificationV.NotificationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.PerspectiveHelper;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.texteditor.StatusLineContributionItem;
import org.json.simple.JSONArray;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class OSGIEventHandler {
	private Dictionary<String, Object> data;
	private StatusLineContributionItem statusBarA;
	private final Lock lock = new ReentrantLock();
	
	private static OSGIEventHandler INSTANCE;
	public static OSGIEventHandler getInstance() {
		if (INSTANCE == null)
			INSTANCE = new OSGIEventHandler();
		return INSTANCE;
	}

	private OSGIEventHandler() {
		data = new Hashtable<String, Object>();
		
		// REMINDER> Add org.eclipse.osgi.services to your MANIFEST.MF and make sure org.eclipse.equinox.event is part of your Launch-Config and Product-Definition.
		Dictionary<String,String> properties = new Hashtable<String, String>();	
	    BundleContext ctx = FrameworkUtil.getBundle(OSGIEventHandler.class).getBundleContext();
		
	    data.put("perspectivePrevious", "");
	    data.put("perspectiveCurrent", "");
	    properties.put(EventConstants.EVENT_TOPIC, "Perspective/*");
	    ctx.registerService(EventHandler.class, new PerspectiveHandler(data), properties);

	    data.put("view", "");
	    properties.put(EventConstants.EVENT_TOPIC, "View/*");
	    ctx.registerService(EventHandler.class, new ViewHandler(data), properties);

	    data.put("stateConnected?", false);
	    properties.put(EventConstants.EVENT_TOPIC, "State/*");
	    ctx.registerService(EventHandler.class, new StateHandler(data), properties);
	    
	    properties.put(EventConstants.EVENT_TOPIC, "activityModify/*");
	    ctx.registerService(EventHandler.class, new ActivityHandler(data), properties);
	    properties.put(EventConstants.EVENT_TOPIC, "activityCreate/*");
	    ctx.registerService(EventHandler.class, new ActivityHandler(data), properties);
	    properties.put(EventConstants.EVENT_TOPIC, "activityCreateSub/*");
	    ctx.registerService(EventHandler.class, new ActivityHandler(data), properties);
	    properties.put(EventConstants.EVENT_TOPIC, "activityClone/*");
	    ctx.registerService(EventHandler.class, new ActivityHandler(data), properties);

	    // Status bar
	    statusBarA = new StatusLineContributionItem("LoggedInStatus",true,4);
	    
	}

	public Object get( String key ) { return data.get(key); } 
	
	private class MyHandler {
		Dictionary<String, Object> data;
		
		public MyHandler( Dictionary<String, Object> data ) { 
			this.data = data; 
		}
	}
	
	private class StateHandler extends MyHandler implements EventHandler {
		public StateHandler(Dictionary<String, Object> data) { super(data); }
		public void handleEvent(Event event) {
			System.out.println(">>>>>>>>>>>>>> " + event.getTopic());
	    	  if( "State/Connected".equals(event.getTopic()) ) {
		    	  handleConnectionStatus(true);
	    		  handleScripts("LOGIN");
	    	  } else if( "State/Disconnected".equals(event.getTopic()) ) {
	    		  handleConnectionStatus(false);
	    		  handleScripts("LOGOUT");
	    	  } else if( "State/Init".equals(event.getTopic()) ) {
	    		  handleScripts("INIT");
	    	  } else if( "State/Act_Created".equals(event.getTopic()) ) {
	    		  handleScripts("ACT_CREATED");
		  	  } else if( "State/Act_Changed".equals(event.getTopic()) ) {
				  handleScripts("ACT_CHANGED");
			  }
		}
	}	
	private class PerspectiveHandler extends MyHandler implements EventHandler {
		public PerspectiveHandler(Dictionary<String, Object> data) { super(data); }
		public void handleEvent(Event event) {
			if( "Perspective/Load".equals(event.getTopic()) ) {
	    		  final String toLoad = ("PREV").equals(event.getProperty("PERP_ID")) ? data.get("perspectivePrevious").toString() : event.getProperty("PERP_ID").toString();
	    		  if( toLoad.equals(data.get("perspectiveCurrent") ) ) return;
				  PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getDisplay().asyncExec( new Runnable() {
					  public void run() {
						  System.out.println("<<<<< " + toLoad );
						  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective( PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(toLoad));
					  }
		          });
		    	  data.put("perspectivePrevious", data.get("perspectiveCurrent").toString());
		    	  data.put("perspectiveCurrent", toLoad);
	    	  }
		}
	}	
	private class ViewHandler extends MyHandler implements EventHandler {
		public ViewHandler(Dictionary<String, Object> data) { super(data); }
		public void handleEvent(final Event event) {
			final String fID = event.getProperty("VIEW_ID").toString();
			final Display display = PlatformUI.getWorkbench().getDisplay();

			display.asyncExec( new Runnable() {
				@SuppressWarnings("restriction")
				public void run() {
					final Shell shell = display.getShells()[0];
					final IWorkbenchPage page = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
					
					if( "View/Hide".equals(event.getTopic()) ) {
						final IViewReference ref = page.findViewReference(fID);
						if (ref == null) 
							return;
						display.asyncExec( new Runnable() {
							public void run() {
								System.out.println("<<<<< " + fID );
								page.hideView(ref);
							}
						} );
					}
		
					if( "View/Minimize".equals(event.getTopic()) ) {
						final IViewReference ref = page.findViewReference(fID);
						if (ref == null) 
							return;
						display.asyncExec( new Runnable() {
							public void run() {
								System.out.println("<<<<< " + fID );
								page.setPartState(ref, IWorkbenchPage.STATE_MINIMIZED);
							}
						} );
					}

					if( "View/Detach".equals(event.getTopic()) ) {
						final IViewReference ref = page.findViewReference(fID);
						if (ref == null) 
							return;
//						WorkbenchPage workbenchPage = page.get;
						final WorkbenchPage workbenchPage = (WorkbenchPage)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						display.asyncExec( new Runnable() {
							public void run() {
								System.out.println("<<<<< " + fID );
								Perspective activePerspective = workbenchPage.getActivePerspective();
								PerspectiveHelper presentation = activePerspective.getPresentation();
								presentation.detachPart( ref );

								// TODO> luckily this is given in the right order as there are actually three shells?!? main, chat, and???
								for( Shell s : display.getShells() ) {
									if( s.getText().length() > 0 )
										continue; 
									else 
										s.setLocation(shell.getLocation().x + shell.getSize().x, shell.getLocation().y);										
								}
							}
						} );
					}
		
					if( "View/Load".equals(event.getTopic()) ) {
						System.out.println("<<<<< " + fID );
						try {
							// Calling the view part
							page.showView(fID);
							
						} catch (SecurityException e) { e.printStackTrace();
						} catch (IllegalArgumentException e) { e.printStackTrace();
						} catch (PartInitException e) { e.printStackTrace();
						}
					}
				}
			});
		}
	}
	
	private class ActivityHandler extends MyHandler implements EventHandler {
//		private HashMap<String, String> refs = new HashMap<String, String>();
		private String currentMode;
		private String currentActivity;

		public ActivityHandler(Dictionary<String, Object> data) { super(data); }
		public void handleEvent(final Event event) {
    		Display.getDefault().asyncExec(new Runnable() {
    				public void run() {
//    					refs.clear();
    		    		currentMode = event.getTopic().split("/")[0];
    				    ABC4GSDActivityInformation info = new ABC4GSDActivityInformation();
    				    currentActivity = event.getProperty("ACT_ID").toString();

    				    System.out.println( "EVENT " +event.getTopic() );
    		    		System.out.println( "ID " + event.getProperty("ACT_ID").toString() );
    		    								
    				    ABC4GSDActivityElement[] listUsers = MasterClientWrapper.getInstance().getUsers();
    				    ABC4GSDActivityAsset[] listArtifacts = MasterClientWrapper.getInstance().getAssets();
    		    		initContent( info );
    				    ABC4GSDActivityInformation preInfo = new ABC4GSDActivityInformation(info);

    				    CreateActivityDialog loginDialog = new CreateActivityDialog(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(),
    							currentMode, info, listUsers, listArtifacts );
    					if (loginDialog.open() == Window.OK) 
    						MasterClientWrapper.getInstance().createActivity( currentMode, currentActivity, preInfo, loginDialog.getInfo() );
    				}

    				public void initContent( ABC4GSDActivityInformation info ) {
    					IABC4GSDItem current = null;
    					IABC4GSDItem currUser = new ABC4GSDItem( "abc.user", MasterClientWrapper.getInstance().getMyId(), new String[]{"name"} );
						String[] resp;
						String[] ids;
						String support = "";			
						String support2 = "";			
						
    					if( currentMode.equals(CreateActivityDialog.MODE_CREATE) ) {
    						info.creator = MasterClientWrapper.getInstance().getMyId();
    						info.users = new ABC4GSDActivityElement[]{ new ABC4GSDActivityElement( MasterClientWrapper.getInstance().getMyId(), currUser.get("name").toString() ) };
    					}
    					if( currentMode.equals(CreateActivityDialog.MODE_MODIFY)||currentMode.equals(CreateActivityDialog.MODE_CLONE)||currentMode.equals(CreateActivityDialog.MODE_CREATE_SUB) ) {
    						// Activity Modification
    						current = new ABC4GSDItem( "abc.activity", currentActivity );
    						current.update();
    						info.creator = ((ArrayList<Long>)current.get("creator")).get(0);
    						info.name = current.get("name").toString();
    						info.description = (String)current.get("description");

    						// super activity
    						resp = MasterClientWrapper.getInstance().query("abc.relation.[].to.==." + current.getId() );
    						if( resp.length == 0 ) {
    							info.superActivity = "";
    						} else {
    							resp = MasterClientWrapper.getInstance().query("abc.relation." + resp[0] + ".from");
    							info.superActivity = resp[0];
    						}
    						ids = MasterClientWrapper.getInstance().query( "abc.user.[abc.state.[abc.state.[].activity.==." +current.getId()+ "].user]._id" );
    						info.users = new ABC4GSDActivityElement[ ids.length ];
    						for( int i=0; i<ids.length;i++ ) {
    							support = MasterClientWrapper.getInstance().query( "abc.user." +ids[i]+ ".name" )[0];
        						info.users[i] = new ABC4GSDActivityElement(ids[i], support);
    						}
    						if( info.users.length > 0) {
    							ids = MasterClientWrapper.getInstance().query( "abc.ecology.[abc.ecology.[].name.==."+current.getId()+":"+info.users[0].getId()+"].asset" );
    							info.assets = new ABC4GSDActivityAsset[ ids.length ];
        						for( int i=0; i<ids.length;i++ ) {
        							support = MasterClientWrapper.getInstance().query( "abc.asset."+ids[i]+".ptr" )[0];
        							support2 = MasterClientWrapper.getInstance().query( "abc.artifact."+support+".name" )[0];
        							info.assets[i] = new ABC4GSDActivityAsset(ids[i], support, support2); 
        						}
    						}
    					}
    					if( currentMode.equals(CreateActivityDialog.MODE_CLONE) ) {
    						info.name = "Copy of " + info.name;
    					}
    					if( currentMode.equals(CreateActivityDialog.MODE_CREATE_SUB) ) {
    						info.description = "This activity has been created as a subactivity of \n" + info.name + ".\nThe activity creation form has been created as a copy of the super-activity.";
    						info.name = "Sub of " + info.name;
    						info.superActivity = currentActivity;
    					}
    				}
    		});
		}
	}

	
	
	public StatusLineContributionItem getStatusBarConnectionElement() { return statusBarA; }
	
	private void handleConnectionStatus( final boolean wip ) {
		// Setting for status bar and shared variable for commands
		data.put("stateConnected?", wip);

		ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI.getWorkbench().getWorkbenchWindows()[0].getService(ISourceProviderService.class);
		final StateProvider state = (StateProvider) sourceProviderService.getSourceProvider(StateProvider.MY_STATE);

    	Display.getDefault().asyncExec( new Runnable() {
	            public void run() {
	            	if( ! wip ){
		            	statusBarA.setImage( Activator.getImageDescriptor( IImageKeys.DISCONNECTED_16 ).createImage());
		            	statusBarA.setToolTipText("You are currently disconnected.");
	            	} else {
		            	statusBarA.setImage( Activator.getImageDescriptor( IImageKeys.CONNECTED_16 ).createImage());
		            	statusBarA.setToolTipText("Connected as " + MasterClientWrapper.getInstance().query("abc.user."+MasterClientWrapper.getInstance().getMyId()+".name")[0]);
	            	} 
	            	state.setConnectionState(wip);
	            }});
	}


	private void handleScripts( String event ) {
//		p.put(Scripts.SCRIPT, "When###Condition###Delay(s)###Script\n" +
//				"INIT###'' length == 0###0###'+.library/data/abcV2.schema'\n");
		IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		String[] scripts = prefs.get(Scripts.SCRIPT, DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(Scripts.SCRIPT, "ERROR")).split("\n");

		for( String line : scripts ) {
			System.out.println("Seaking: " + event + ", Checking: ->" + line + "<-");
			String[] current = line.split("###");
			if( current.length == 0 ) continue;
			if( ! current[0].equals(event) ) continue;
			String[] wip;

			// condition
			Boolean holds = false;
			wip = current[1].split(" ");
			String condition = wip[0];
			if( wip[0].indexOf("'") == 0 )
				condition = wip[0].substring(1, wip[0].length()-1);
			JSONArray resp = ((JSONArray)((JSONArray)MasterClientWrapper.getInstance().run(condition).get(Constants.MSG_A)).get(2));
			
			String operation = wip[2];
			String value = wip[3];
			if( wip[1].equals("length") ) {
				if( operation.equals("==") ) {
					holds = resp.size() == Integer.parseInt(value);
				}
			}
			
			if( wip[1].equals("in") ) {
				if( operation.equals("==") ) {
					for( Object tt : resp )
						holds = tt.toString().equals(value);
				}
			}

			if( condition.equals("None") ) holds = true;
			
			if( ! holds ) continue;
			
			// Delay in seconds
			final int delay = Integer.parseInt(current[2]);			
			// Query to execute
			final String toExecute = current[3].substring(1, current[3].length()-1);

			Job task = new Job("job") {
				protected IStatus run(IProgressMonitor monitor) {
					Display.getDefault().asyncExec(new Runnable() {
				          public void run() {
								String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
								System.out.println(time + ": Executing Script: '" + toExecute);
								MasterClientWrapper.getInstance().run(toExecute);
				          }
				        });
					return Status.OK_STATUS;
				}
			};
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			System.out.println(time + ": Scheduled Script: '" + toExecute +"' in: " + delay + " seconds");
			task.schedule(delay * 1000);
		}
		
	}

	

}
//Rectangle monitorSize = display.getBounds();				
//Point siz = shell.getSize();
//Point pos = shell.getLocation();
//
//System.out.println( "" + Platform.getProduct().getName() );
//Shell mainShell = shell;
//if( !mainShell.getText().equals( Platform.getProduct().getName() ) )
//	for( Shell s : PlatformUI.getWorkbench().getDisplay().getShells() )
//		if( s.getText().equals( Platform.getProduct().getName() ) )
//			mainShell = s;
//		else
//			System.out.println("Main shell unavailable.");
//	
//((WorkbenchPage) page).getActivePerspective().getPresentation().detachPart(ref);
//
//ref.getView(false).getViewSite().getShell().setSize(siz.x, siz.y);
//if( siz.x*2>monitorSize.width)
//	ref.getView(false).getViewSite().getShell().setLocation(pos.x+10, pos.y+10);
//else
//	ref.getView(false).getViewSite().getShell().setLocation(pos.x+siz.x/2 < monitorSize.width/2 ? pos.x+siz.x : pos.x-siz.x, pos.y);
//
//ref.getView(false).getViewSite().getShell().setActive();
//ref.getView(false).getViewSite().getShell().setFocus();




