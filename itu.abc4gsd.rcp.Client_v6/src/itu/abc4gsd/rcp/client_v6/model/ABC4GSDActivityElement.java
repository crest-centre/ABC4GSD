package itu.abc4gsd.rcp.client_v6.model;


public class ABC4GSDActivityElement implements IABC4GSDActivityElement {
	private long id;
	private String name;
	
	public ABC4GSDActivityElement( long id, String name ) {
		setId(id);
		setName(name);
	}
	public ABC4GSDActivityElement( String id, String name ) {
		setId(id);
		setName(name);
	}
	public long getId() { return this.id; }
	public void setId(long id) { this.id = id; }
	public void setId(String id) { this.id = Long.parseLong(id); }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}
