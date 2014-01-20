package itu.abc4gsd.rcp.client_v6.logic;

import java.util.StringTokenizer;

import org.json.simple.JSONObject;

public class InternalMessage {
	public final String code;
	public final String message;
	public final long m_code;
	public final long m_sender;
	public final String m_msg;
	public final JSONObject data;
	
	public InternalMessage( String code, JSONObject data ) {
		this.code = code;
		this.data = data;
		this.message = "";
		this.m_code = -1;
		this.m_sender = -1;
		this.m_msg = "";		
	}
	public InternalMessage( String code, String message ) {
		this.data = null;
		this.code = code;
		this.m_msg = message;
		String wip = message.substring(1, message.length()-1);
		this.m_code = Long.parseLong(wip.substring(0, wip.indexOf(",")));
		wip = wip.substring(wip.indexOf(",")+1);
		this.m_sender = Long.parseLong(wip.substring(0, wip.indexOf(",")));
		this.message = wip.substring(wip.indexOf(",")+1);
	}
	public InternalMessage( String code, InternalMessage message ) {
		this.data = message.data;
		this.code = code;
		this.message = message.message;
		this.m_code = message.m_code;
		this.m_sender = message.m_sender;
		this.m_msg = message.m_msg;
	}
	public String toString() {
		return "Internal message: " + code + " - " + message;
	}
	public String[] extractList() {
		String wip = this.message.substring(1, this.message.length()-1);
		StringTokenizer t = new StringTokenizer(wip,",");
		String[] ret = new String[t.countTokens()];
		int i = 0;
		while( t.hasMoreTokens() ) {
			ret[i] = t.nextToken();
			i++;
		}
		return ret;
	}
	public String extractString() { return this.message.substring(1, this.message.length()-1); }
}
