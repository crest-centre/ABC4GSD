package itu.abc4gsd.rcp.client_v6.appInterface;

 import itu.abc4gsd.rcp.client_v6.logic.Constants;

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
		String wipwip = "";
		boolean larger = false;
		while( tmp.hasMoreTokens() ) {
			wipwip += tmp.nextToken();
			if( wipwip.startsWith(Constants.VAL_DELIM_PRE) && wipwip.endsWith(Constants.VAL_DELIM_POST) ) {
				wipwip = wipwip.substring( Constants.VAL_DELIM_PRE.length(), wipwip.length()-Constants.VAL_DELIM_POST.length() );
				larger = false;
			}
			if( !wipwip.startsWith(Constants.VAL_DELIM_PRE) && ! larger ) {
				tmptmp.add( wipwip );
				wipwip = "";
			} else {
				larger = ! wipwip.endsWith(Constants.VAL_DELIM_POST);
				wipwip += ".";
			}
		}
			
		tokens = tmptmp.toArray( new String[ tmptmp.size() ] );
		length = tokens.length;
	}
		
}
