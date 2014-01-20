package itu.abc4gsd.eclipse.plugin.middlemanvcs.wrapper;

import org.eclipse.swt.widgets.Shell;

public interface IVCSWrapper {
	public void checkout( String repository, String project );
	public void commit( String project );
	public void update( String project );
	
	public boolean isProject( String url );
	
	public void setCurrentRepository( String repository );
	public void setCurrentProject( String project );
	public String getCurrentProject();
	public String getCurrentRepository();
	
	public Shell getShell();
}
