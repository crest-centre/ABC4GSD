package itu.abc4gsd.rcp.client_v6.appInterface;


public class RemoteMessage extends BaseRemoteMessage {
	public final long id;
	public final String model;
	public final String type;
	public final String base;
	public final String key;
	public final String operation;
	public final String value;
	//model.type.id.key.operation.value
	public RemoteMessage( String msg ) {
		super( msg );
		if( length != 6 ) {
			this.id = -1;
			this.model = null; 
			this.type = null; 
			this.base = null;
			this.key = null;
			this.operation = null; 
			this.value = null;
		} else {
			this.model = tokens[0];
			this.type = tokens[1];
			this.id = Long.parseLong( tokens[2] );
			this.key = tokens[3];
			this.operation = tokens[4];
			String tmp = tokens[5];
			for( int i=6; i<length; i++ )
				tmp += "." + tokens[i];
			this.value = tmp;
			this.base = model + "." + type + "." + this.id;
		}
	}
}
