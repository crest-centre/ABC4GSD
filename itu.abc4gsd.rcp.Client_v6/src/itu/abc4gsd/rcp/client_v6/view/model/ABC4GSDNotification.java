package itu.abc4gsd.rcp.client_v6.view.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONObject;



public class ABC4GSDNotification implements IABC4GSDItem {
	public final String time;
	public final String image;
	public final String title;
	public final String body;
	public final String level;
	public final String fromAct;
	public final String toAct;
	public final String notification_id;

	public ABC4GSDNotification() {
		time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		image = "";
		title = "EMPTY NOTIFICATION";
		body = "";
		level = "";
		fromAct = "";
		toAct = "";
		notification_id = "";
	}
	public ABC4GSDNotification( String time, String title, String body, String image, String widget, String level ) {
		this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		this.image = image;
		this.title = title;
		this.body = body;
		this.level = level;
		this.fromAct = "";
		this.toAct = "";
		notification_id = "";
	}
	public ABC4GSDNotification( JSONObject content ) {
		this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		this.image = content.containsKey("image") ? content.get("image").toString() : null;
		this.title = content.containsKey("title") ? content.get("title").toString() : null;
		this.body = content.containsKey("body") ? content.get("body").toString() : null;
		this.level = content.containsKey("level") ? content.get("level").toString() : "";
		this.fromAct = content.containsKey("fromAct") ? content.get("fromAct").toString() : "";
		this.toAct = content.containsKey("toAct") ? content.get("toAct").toString() : "";
		this.notification_id = content.containsKey("notification_id") ? content.get("notification_id").toString() : "";
	}
	public ABC4GSDNotification( ABC4GSDNotification content ) {
		this.time = content.time;
		this.image = content.image;
		this.title = content.title;
		this.body = content.body;
		this.level = content.level;
		this.fromAct = content.fromAct;
		this.toAct = content.toAct;
		this.notification_id = content.notification_id;
	}

		
	// The following part is crap... it was just faster to implement
	public String getBaseQuery() { return null; }
	public void setBaseQuery(String q) {}
	public long getId() { return 0; }
	public void setId(long id) {}
	public void setId(String id) {}
	public boolean hasKey(String key) { return false; }
	public String[] getKeys() { return null; }
	public String[] getValues() { return null; }
	public void setKeys(String[] keys) {}
	public Object get(String key) { return null; }
	public void set(String key, long value, boolean remote) {}
	public void set(String key, String value, boolean remote) {}
	public void set(String key, long value) {}
	public void set(String key, String value) {}
	public boolean simpleType(String key) { return false; }
	public String[] getType() { return null; }
	public String[] getType(String key) { return null; }
	public void attach(String key, String val, boolean remote) {}
	public void attach(String key, Long val, boolean remote) {}
	public void attach(String key, String val) {}
	public void attach(String key, Long val) {}
	public void update(String[] toLoad) {}
	public void update() {}
}
