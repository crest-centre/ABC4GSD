package itu.abc4gsd.rcp.client_v6.model;


public class ABC4GSDActivityInformation {	
	public long creator;
	public String name;
	public String description;
	public String superActivity;
	public ABC4GSDActivityElement[] users;
	public ABC4GSDActivityAsset[] assets;

	public ABC4GSDActivityInformation( ) {
		this.creator = -1;
		this.name = "";
		this.description = "";
		this.superActivity = "";
		this.users = new ABC4GSDActivityElement[]{};
		this.assets = new ABC4GSDActivityAsset[]{};
	}
	public ABC4GSDActivityInformation( ABC4GSDActivityInformation tmp ) {
		this.creator = tmp.creator;
		this.name = tmp.name;
		this.description = tmp.description;
		this.superActivity = tmp.superActivity;
		this.users = new ABC4GSDActivityElement[tmp.users.length];
		this.assets = new ABC4GSDActivityAsset[tmp.assets.length];
		System.arraycopy(tmp.users, 0, this.users, 0, tmp.users.length);
		System.arraycopy(tmp.assets, 0, this.assets, 0, tmp.assets.length);
	}
}
