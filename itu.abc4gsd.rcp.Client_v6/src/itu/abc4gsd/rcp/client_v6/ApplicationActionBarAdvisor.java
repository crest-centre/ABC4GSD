package itu.abc4gsd.rcp.client_v6;

import itu.abc4gsd.rcp.client_v6.logic.OSGIEventHandler;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;


public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}
//	private IWorkbenchAction exitAction;
//	private IWorkbenchAction aboutAction;
//	private IWorkbenchAction actionShowRun;
//	private IWorkbenchAction actionConnect;
//	private IWorkbenchAction actionAddSchema;
//	private IContributionItem perspectivesMenu;
//	private IAction actionManagementPerspective;
//	private IAction actionShowActivityPerspective;
//	private IAction actionCreateActivityPerspective;
//	private IAction actionShowNotificationPerspective;

	
    protected void fillStatusLine(IStatusLineManager statusLine) {
    	statusLine.add( OSGIEventHandler.getInstance().getStatusBarConnectionElement() );
   	}
    
//	protected void makeActions(IWorkbenchWindow window) {
//		perspectivesMenu = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
//		actionManagementPerspective = new ManagementPerspective(window);
//		register( actionManagementPerspective );
//		actionShowActivityPerspective = new ShowUsePerspective(window);
//		register( actionShowActivityPerspective );
//		actionCreateActivityPerspective = new CreateActivity(window);
//		register( actionCreateActivityPerspective );
//		actionShowNotificationPerspective = new ShowNotificationPerspective(window);
//		register( actionShowNotificationPerspective );
//		exitAction = ActionFactory.QUIT.create(window);
//		register(exitAction);
//		aboutAction = ActionFactory.ABOUT.create(window);
//		register(aboutAction);
//		actionShowRun = new ActionShowRun(window);
//		register(actionShowRun);
//		actionConnect = new ActionConnect(window);
//		register(actionConnect);
//		actionAddSchema = new ActionAddSchema(window);
//		register(actionAddSchema);
//	}

//    protected void fillMenuBar(IMenuManager menuBar) {
//		MenuManager clientMenu = new MenuManager("&ABC4GSD", "mainMenu");
//		clientMenu.add(actionManagementPerspective);
//		clientMenu.add(actionShowActivityPerspective);
//		clientMenu.add(actionCreateActivityPerspective);
//		clientMenu.add(actionShowNotificationPerspective);
//		clientMenu.add(actionConnect);
//		clientMenu.add(actionShowRun);
//		clientMenu.add(actionAddSchema);
//		clientMenu.add(new Separator());
//		clientMenu.add(exitAction);
//		MenuManager helpMenu = new MenuManager("&Help", "help");
//		helpMenu.add(aboutAction);
//		menuBar.add(clientMenu);
//		menuBar.add(helpMenu);
//    }


//    protected void fillTrayItem(IMenuManager trayItem) {
//    	trayItem.add(actionShowActivityPerspective);
//    	trayItem.add(actionConnect);
//		trayItem.add(actionShowRun);
//		trayItem.add(aboutAction);
//		trayItem.add(exitAction);
//	}
    
//	protected void fillCoolBar(ICoolBarManager coolBar) {
//		IToolBarManager toolbar = new ToolBarManager(coolBar.getStyle());
//		coolBar.add(toolbar);
//		toolbar.add(actionConnect);
//		toolbar.add(actionShowRun);
//		toolbar.add(actionAddSchema);
//	}
    
}
