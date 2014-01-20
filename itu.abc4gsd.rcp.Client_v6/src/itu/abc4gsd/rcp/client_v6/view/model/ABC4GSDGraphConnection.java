

package itu.abc4gsd.rcp.client_v6.view.model;




public class ABC4GSDGraphConnection {
	final public ABC4GSDGraphItem from;
	final public ABC4GSDGraphItem to;
	public String label;
	
	public ABC4GSDGraphConnection( ABC4GSDGraphItem from, ABC4GSDGraphItem to ) { 
		this.from = from;
		this.to = to;
		label = from.getLabel() + "-->" + to.getLabel();
	}
}
