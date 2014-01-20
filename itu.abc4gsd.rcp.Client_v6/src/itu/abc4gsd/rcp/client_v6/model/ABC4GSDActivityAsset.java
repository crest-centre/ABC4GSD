package itu.abc4gsd.rcp.client_v6.model;


public class ABC4GSDActivityAsset extends ABC4GSDActivityElement {
	public ABC4GSDActivityAsset(long id, long artifactId, String name) {
		super(id, name);
		setArtifactId(artifactId);
	}
	public ABC4GSDActivityAsset(String id, String artifactId, String name) {
		super(id, name);
		setArtifactId(artifactId);
	}
	private long artifactId;

	public long getArtifactId() { return this.artifactId; }
	public void setArtifactId(long id) { this.artifactId = id; }
	public void setArtifactId(String id) { this.artifactId = Long.parseLong(id); }
}
