package itu.abc4gsd.rcp.client_v6.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import itu.abc4gsd.rcp.client_v6.dialog.CreateActivityDialog;
 import itu.abc4gsd.rcp.client_v6.logic.Constants;
 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityAsset;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityElement;
import itu.abc4gsd.rcp.client_v6.model.ABC4GSDActivityInformation;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDNotification;
 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;


//MyNotificationPopup popup = new MyNotificationPopup(window.getShell().getDisplay());
//popup.create();
//popup.open();


// TODO> Look into http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
// TODO> Change position theoretically I should keep track of timers and override in AbstractNot... the create to change the addregion




@SuppressWarnings("restriction")
public class abc4gsdPopUpNotification extends org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotificationPopup {
	private final ABC4GSDNotification notification;
	private final String time;
	private final String title;
	private final String text;
	private final int type;
	private String info;
	private RGB color;
	public static int DELAY_CLOSE_NEVER = -1;
	public static int DELAY_CLOSE_DEFAULT = 5;
	public RGB COLOR_DEFAULT;
	public static RGB COLOR_GREEN = new RGB(120,222,108);
	public static RGB COLOR_YELLOW = new RGB(255,255,0);
	public static int TYPE_NOTIFICATION = 1;
	public static int TYPE_PING = 2;
	
	


	public abc4gsdPopUpNotification(Display display) {
		super(display);
		notification = null;
		time = "";
		title = "";
		text = "";
		color = null;
		type = -1;
	}
 
	public abc4gsdPopUpNotification(Display display, String time, String title, String text ) {
		this( display, new ABC4GSDNotification(time, title, text, "", "", ""), -1, null); }
	public abc4gsdPopUpNotification(Display display, String time, String title, String text, int type ) {
		this( display, new ABC4GSDNotification(time, title, text, "", "", ""), type, null); }
	public abc4gsdPopUpNotification(Display display, String time, String title, String text, int type, RGB color ) {
		this( display, new ABC4GSDNotification(time, title, text, "", "", ""), type, color); }
	public abc4gsdPopUpNotification(Display display, ABC4GSDNotification ntf, int type ) {
		this( display, ntf, type, null ); }
	public abc4gsdPopUpNotification(Display display, ABC4GSDNotification ntf, int type, RGB color ) {
		super(display);
		this.notification = ntf;
		this.time = ntf.time;
		this.title = ntf.title;
		this.text = ntf.body;
		COLOR_DEFAULT =  display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
		this.type = type;
		if( type == -1 ) type = TYPE_NOTIFICATION;
		if( type == TYPE_NOTIFICATION && color == null ) this.color = COLOR_DEFAULT;
		if( type == TYPE_PING && color == null ) this.color = COLOR_YELLOW;
		if( color != null ) this.color = color;
		
		if( type == TYPE_PING ) setDelay(DELAY_CLOSE_NEVER);
		else setDelay(DELAY_CLOSE_DEFAULT);
	}

	public void setDelay( int seconds ) { 
		setDelayClose(seconds * 1000); 
		}
	
	@Override
	protected void createContentArea(Composite composite) {
		composite.setBackground(new Color(Display.getCurrent(), color));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		composite.setLayout(new GridLayout(1, true));
		Label testLabel = new Label(composite, SWT.WRAP);
		testLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		if( text.contains("$$") )
//		Link test = new Link(composite, 0);
//		String aaa = "<a href=\"abc.activity.288403360\">aaa</a>";
//		test.setText(aaa);
//		test.setSize(400, 200);
//		String txt = "abc.activity.288403360";
//		String[] tmptmp = txt.split("\\.");
//		String id = tmptmp[tmptmp.length-1];
//		String baseQuery = txt.substring(0, txt.indexOf(id)-1);
//		System.out.println("Selected " + test.getText() + " //base " + baseQuery + " //id " + id);
//		ABC4GSDItem wip = new ABC4GSDItem(baseQuery, id);
//		wip.update();
//		String out = "";
//		for( String aa : wip.getType() )
//			out += aa + ": " + wip.get(aa) + "\n";
//		test.setToolTipText(out);
		
		testLabel.setText( "Time> "+ time + "\n" + text );
		if( this.type == TYPE_PING )
			testLabel.addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					// create subactivity ...
					
					long currAct = MasterClientWrapper.getInstance().getCurrentActivity();
					ABC4GSDActivityInformation info = new ABC4GSDActivityInformation();
					boolean found = false;
					info.creator = MasterClientWrapper.getInstance().getMyId();
					info.name = "Discussion (";
					info.description = "This activity has been created to support the discussion space requested and keep track of it. Participants:\n";
					info.superActivity = "";
					info.assets = new ABC4GSDActivityAsset[]{};
					
					// getting ping message 
					IABC4GSDItem wip = new ABC4GSDItem( "notification.notification", notification.notification_id );
					wip.update();
					
					List<String> participant = new ArrayList<String>();
					if( wip.get("from").toString().length()>0 ) participant.add( ((ArrayList<Long>)wip.get("from")).get(0).toString() );
					if( ((ArrayList<Long>)wip.get("to")).size()>0 )
						for( Long u : (ArrayList<Long>)wip.get("to") )
							if( !participant.contains(u.toString()) )
								participant.add(u.toString());

					info.users = new ABC4GSDActivityElement[participant.size()];
					for( int i=0; i<participant.size(); i++ ) {
						String tmp_id = participant.get(i);
						String tmp_name = MasterClientWrapper.getInstance().query("abc.user." + participant.get(i) + ".name")[0];
						info.users[i] = new ABC4GSDActivityElement(tmp_id, tmp_name);
						info.description += " - " + tmp_name + "\n";
						info.name += tmp_name + ", ";
					}
					info.name = info.name.substring(0, info.name.length()-2) + ")";

					currAct = MasterClientWrapper.getInstance().createActivity(
							CreateActivityDialog.MODE_CREATE, null, new ABC4GSDActivityInformation(), info );
					
					MasterClientWrapper.getInstance()._getHandler()._openChat(currAct);
										
			        BundleContext ctx = FrameworkUtil.getBundle(abc4gsdPopUpNotification.class).getBundleContext();
			        ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
			        EventAdmin eventAdmin = ctx.getService(ref);
			        eventAdmin.postEvent( new Event("State/Act_Changed", new HashMap<String, Object>()) );

			        final String initialMessage = MasterClientWrapper.getInstance().query( "chat.message." + ((ArrayList<Long>)wip.get("msg")).get(0).toString() + ".text" )[0];
			        final long user = ((ArrayList<Long>)wip.get("from")).get(0);
			        final long act = currAct;
					final String desc = info.description;
					
					new Timer().schedule(new TimerTask() {
					    public void run() {
					    	JSONObject content = new JSONObject();
							content.put("activity", act);
							content.put("message", desc);
							MasterClientWrapper.getInstance()._getHandler()._writeInChat(content.toJSONString());
						}
					}, 1000);

					new Timer().schedule(new TimerTask() {
					    public void run() {
					    	IABC4GSDItem wip = new ABC4GSDItem( "chat.message" );
							MasterClientWrapper.getInstance().query("chat.message." + wip.getId() + ".timestamp.=.?TIME?");
							wip.set("text", initialMessage);
							wip.attach("user", user);
							// attaching new message to the room
							String roomId = MasterClientWrapper.getInstance().query( "chat.room.[].activity.==." + act )[0];
							IABC4GSDItem room = new ABC4GSDItem( "chat.room", roomId );
							room.attach("msg", wip.getId());
					    }
					}, 2000);

					close();
					setReturnCode(CANCEL);
				}
			});
	}
 

	@Override
	protected Control createContents(Composite parent) {
		((GridLayout) parent.getLayout()).marginWidth = 1;
		((GridLayout) parent.getLayout()).marginHeight = 1;

		/* Outer Composite holding the controls */
		final Composite outerCircle = new Composite(parent, SWT.NO_FOCUS);
		outerCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outerCircle.setBackgroundMode(SWT.INHERIT_FORCE);

		outerCircle.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				Rectangle clArea = outerCircle.getClientArea();
				Image lastUsedBgImage = new Image(outerCircle.getDisplay(), clArea.width, clArea.height);
				GC gc = new GC(lastUsedBgImage);

				/* Gradient */
				drawGradient(gc, clArea);

				/* Fix Region Shape */
				fixRegion(gc, clArea);

				gc.dispose();

				Image oldBGImage = outerCircle.getBackgroundImage();
				outerCircle.setBackgroundImage(lastUsedBgImage);

				if (oldBGImage != null) {
					oldBGImage.dispose();
				}
			}

			private void drawGradient(GC gc, Rectangle clArea) {
				gc.setForeground(new Color(Display.getCurrent(), new RGB(255, 255, 255) ));
				gc.setBackground(new Color(Display.getCurrent(), color ));
				gc.fillGradientRectangle(clArea.x, clArea.y, clArea.width, clArea.height, true);
			}

			private void fixRegion(GC gc, Rectangle clArea) {
//				gc.setForeground(color);

				/* Fill Top Left */
				gc.drawPoint(2, 0);
				gc.drawPoint(3, 0);
				gc.drawPoint(1, 1);
				gc.drawPoint(0, 2);
				gc.drawPoint(0, 3);

				/* Fill Top Right */
				gc.drawPoint(clArea.width - 4, 0);
				gc.drawPoint(clArea.width - 3, 0);
				gc.drawPoint(clArea.width - 2, 1);
				gc.drawPoint(clArea.width - 1, 2);
				gc.drawPoint(clArea.width - 1, 3);

				/* Fill Bottom Left */
				gc.drawPoint(2, clArea.height - 0);
				gc.drawPoint(3, clArea.height - 0);
				gc.drawPoint(1, clArea.height - 1);
				gc.drawPoint(0, clArea.height - 2);
				gc.drawPoint(0, clArea.height - 3);

				/* Fill Bottom Right */
				gc.drawPoint(clArea.width - 4, clArea.height - 0);
				gc.drawPoint(clArea.width - 3, clArea.height - 0);
				gc.drawPoint(clArea.width - 2, clArea.height - 1);
				gc.drawPoint(clArea.width - 1, clArea.height - 2);
				gc.drawPoint(clArea.width - 1, clArea.height - 3);
			}
		});
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;

		outerCircle.setLayout(layout);

		/* Title area containing label and close button */
		final Composite titleCircle = new Composite(outerCircle, SWT.NO_FOCUS);
		titleCircle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleCircle.setBackgroundMode(SWT.INHERIT_FORCE);

		layout = new GridLayout(4, false);
		layout.marginWidth = 3;
		layout.marginHeight = 0;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 3;

		titleCircle.setLayout(layout);

		/* Create Title Area */
		createTitleArea(titleCircle);

		/* Outer composite to hold content controlls */
		Composite outerContentCircle = new Composite(outerCircle, SWT.NONE);
		outerContentCircle.setBackgroundMode(SWT.INHERIT_FORCE);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		outerContentCircle.setLayout(layout);
		outerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outerContentCircle.setBackground(outerCircle.getBackground());

		/* Middle composite to show a 1px black line around the content controls */
		Composite middleContentCircle = new Composite(outerContentCircle, SWT.NO_FOCUS);
		middleContentCircle.setBackgroundMode(SWT.INHERIT_FORCE);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 1;

		middleContentCircle.setLayout(layout);
		middleContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		middleContentCircle.setBackground(new Color(Display.getCurrent(), color ));

		/* Inner composite containing the content controls */
		Composite innerContent = new Composite(middleContentCircle, SWT.NO_FOCUS);
		innerContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		innerContent.setBackgroundMode(SWT.INHERIT_FORCE);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		innerContent.setLayout(layout);

		innerContent.setBackground(new Color(Display.getCurrent(), new RGB(255, 0, 0) ));

		/* Content Area */
		createContentArea(innerContent);

		
		return outerCircle;
	}
	
	@Override
	protected String getPopupShellTitle() { return title; }
 
	@Override
	protected Image getPopupShellImage(int maximumHeight) {
		// Use createResource to use a shared Image instance of the ImageDescriptor
		return null;
//		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
	}

}
