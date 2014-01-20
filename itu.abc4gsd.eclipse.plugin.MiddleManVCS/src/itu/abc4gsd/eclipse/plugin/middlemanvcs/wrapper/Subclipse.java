package itu.abc4gsd.eclipse.plugin.middlemanvcs.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.commands.GetStatusCommand;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;
import org.tigris.subversion.subclipse.core.resources.RemoteFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.actions.CommitAction;
import org.tigris.subversion.subclipse.ui.actions.UpdateAction;
import org.tigris.subversion.subclipse.ui.authentication.PasswordPromptDialog;
import org.tigris.subversion.subclipse.ui.dialogs.IgnoreResourcesDialog;
import org.tigris.subversion.subclipse.ui.operations.CheckoutAsProjectOperation;
import org.tigris.subversion.subclipse.ui.operations.IgnoreOperation;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;



// TODO > Remember to check the properties of team ... no javahl
public class Subclipse implements IVCSWrapper {
	private SVNRepositories repoMngr = null;
	private static Subclipse instance = null;
	private String currentRepository = "";
	private String currentProject = "";
	
	public static Subclipse getInstance() {
		if( instance == null )
			instance = new Subclipse();
		return instance;
	}
	
	private Subclipse() {
		initRepoMngr();
	}
	
	/* 
	 * Subclipse main
	 */
	public void checkout( String url, String prjName ) {
		IProgressMonitor progressMonitor = null;
		IProject result = null;
		try {
			progressMonitor = new NullProgressMonitor();

			if( !isRepo( url ) ) createRepo( url );
			ISVNRepositoryLocation remoteLocation = getRepo( url );
			
			SVNUrl url2 = new SVNUrl(url + "/" + prjName);
			ISVNRemoteFolder remoteFolder[] = { new RemoteFolder(remoteLocation, url2, SVNRevision.HEAD) };

			if( !isProject( prjName ) ) createProject( prjName );
			IProject[] localProject = { getProject( prjName ) };
			
			CheckoutAsProjectOperation checkoutOperation = new CheckoutAsProjectOperation( null, remoteFolder, localProject);
			checkoutOperation.setSvnRevision(SVNRevision.HEAD);
			checkoutOperation.run(progressMonitor);
			
			result = localProject[0];
            if (result != null)
                result.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);

			
			// get adds, deletes, updates and property updates.
			final List<IResource> modified = new ArrayList<IResource>();
			ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(localProject[0]);
			GetStatusCommand command = new GetStatusCommand(svnResource, true, false);
			command.run(progressMonitor);
			ISVNStatus[] statuses = command.getStatuses();
			for (int j = 0; j < statuses.length; j++) {
			    if (SVNStatusUtils.isReadyForRevert(statuses[j]) || !SVNStatusUtils.isManaged(statuses[j])) {
			        IResource currentResource = SVNWorkspaceRoot.getResourceFor(localProject[0], statuses[j]);
			        if (currentResource != null)
			            modified.add(currentResource);
			    }
			}
			IResource[] wip = (IResource[]) modified.toArray( new IResource[modified.size()] );
			IgnoreOperation ignoreOperation = new IgnoreOperation(null, wip, new IgnoreResourcesDialog( getShell(), wip ));
			ignoreOperation.execute(progressMonitor);
			

			result = localProject[0];
            if (result != null)
                result.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		} catch (MalformedURLException e) { e.printStackTrace(); 
		} catch (InvocationTargetException e) { e.printStackTrace(); 
		} catch (InterruptedException e) { e.printStackTrace();
		} catch (CoreException e) { e.printStackTrace();
		} finally { progressMonitor.done();
		}
	}
	
	public void update( String prjName ) {
		IProgressMonitor progressMonitor = null;
		IProject result = null;
		try {
			progressMonitor = new NullProgressMonitor();

			if( !isProject( prjName ) ) return;
			IProject[] localProject = { getProject( prjName ) };
			
			UpdateAction updateAction = new UpdateAction();
			updateAction.setSelectedResources(localProject);
			updateAction.execute(null);
            result = localProject[0];
            if (result != null)
                result.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		} catch (InvocationTargetException e) { e.printStackTrace(); 
		} catch (InterruptedException e) { e.printStackTrace();
		} catch (CoreException e) { e.printStackTrace();
		} finally { progressMonitor.done();
		}
	}
	
	public void commit( String prjName ) {
		IProgressMonitor progressMonitor = null;
		IProject result = null;
		try {
			progressMonitor = new NullProgressMonitor();

			if( !isProject( prjName ) ) return;
			IProject[] localProject = { getProject( prjName ) };
			
			CommitAction commitAction = new CommitAction();
			commitAction.setSelectedResources(localProject);
			commitAction.execute(null);
			result = localProject[0];
            if (result != null)
                result.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		} catch (InvocationTargetException e) { e.printStackTrace(); 
		} catch (InterruptedException e) { e.printStackTrace();
		} catch (CoreException e) { e.printStackTrace();
		} finally { progressMonitor.done();
		}
	}
	
	/* 
	 * Subclipse helpers
	 */
	private void initRepoMngr() {
		repoMngr = new SVNRepositories();
		repoMngr.startup();
	}
	
	private boolean isRepo( String wip ) {
		if( repoMngr == null ) initRepoMngr();
		return repoMngr.isKnownRepository(wip, true);
	}
	
	private ISVNRepositoryLocation getRepo( String wip ) {
		if( repoMngr == null ) initRepoMngr();
		for( ISVNRepositoryLocation resp : getRepos() )
			if( resp.getRepositoryRoot().toString().equals(wip) )
				return resp;
		return null;
	}
	
	private boolean createRepo( String url ) {
		if( repoMngr == null ) initRepoMngr();
		boolean ok = false;
		ISVNRepositoryLocation repo = null;
		PasswordPromptDialog diag2 = new PasswordPromptDialog( getShell(), url, "", false);
		diag2.open();
		Properties p = new Properties();
		p.setProperty("name", diag2.getUsername());
		p.setProperty("password", diag2.getPassword());
		p.setProperty("url", url);
		diag2.close();
		try {
			repo = repoMngr.createRepository( p );
			ok = true;
			repoMngr.addOrUpdateRepository( repo );
		} catch (SVNException e) { e.printStackTrace(); }
		return ok;
	}
	
	private ISVNRepositoryLocation[] getRepos() {
		if( repoMngr == null ) initRepoMngr();
		return repoMngr.getKnownRepositories(null);
	}

	
	/* 
	 * Workspace Projects healpers
	 */
	private IProject getProject( String name ) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject( name );
	}
	
	private IProject[] getProjectList() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.getProjects();
	}
	private String[] getProjectName() {
		IProject[] wip = getProjectList();
		String[] resp = new String[ wip.length ];
		for( int i=0; i<wip.length; i++ )
			resp[i] = wip[i].getName();
		return resp;
	}
	
	public boolean isProject( String url ) {
		for( String s : getProjectName() )
			if( url.equals(s) )
				return true;
		return false;
	}
	
	private void createProject( String name ) {
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject( name );
		try {
			project.create(progressMonitor);
			project.open(progressMonitor);
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			progressMonitor.done();
		}
	}
	
	public Shell getShell() { return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(); }

	public void setCurrentRepository(String repository) { this.currentRepository = repository; }
	public void setCurrentProject(String project) {	this.currentProject = project; }
	public String getCurrentProject() { return this.currentProject; }
	public String getCurrentRepository() { return this.currentRepository; }
}
