package itu.abc4gsd.eclipse.core.AppInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;



public class BaseRemoteMessage implements IRemoteMessage {
	public final String original;
	public final String[] tokens; 
	public final int length;
	
	public String getData() { return this.original; }
	public int getSize() { return this.length; }
	public String[] getTokens() { return this.tokens; }

	public BaseRemoteMessage( String wip ) {
		List<String> tmptmp = new ArrayList<String>();
		this.original = wip;
		StringTokenizer tmp = new StringTokenizer( wip,	"." );
		while( tmp.hasMoreTokens() )
			tmptmp.add( clearValue( tmp.nextToken() ) );
			
		tokens = tmptmp.toArray( new String[ tmptmp.size() ] );
		length = tokens.length;
	}
	
	private String clearValue( String wip ) {
		if( wip.startsWith(Constants.VAL_DELIM_PRE) && wip.endsWith(Constants.VAL_DELIM_POST) )
			return wip.substring( Constants.VAL_DELIM_PRE.length(), Constants.VAL_DELIM_POST.length() );
		return wip;
	}
}
