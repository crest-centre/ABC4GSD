package itu.abc4gsd.rcp.client_v6.view.chatV;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;
import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDItem;
import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public class ChatViewItem extends CTabItem {
	private CTabFolder parent;
	private long actId;
	private String title; 
	private boolean initializing;
	
	private ABC4GSDItem room;
	private Text transcript;
	private Text entry;
	private ListenerChat listener;
	private int unreadMsg;

	public ChatViewItem(CTabFolder parent, int style, long actId) {
		super(parent, style);
		this.parent = parent;
		this.actId = actId;
		
		initializing = true;
		_init();
		this.setControl(createContainer());

		// even if trying to, it does not scroll to end.
		transcript.setRedraw(false);
		getExistingMessages();
		transcript.setTopIndex(transcript.getLineCount() - 1);
		transcript.setRedraw(true);
		initializing = false;

	}
	private void _init() {
		// the chat is created only if not already shown
		
		// getting the chat or creating it
		String[] tmp = MasterClientWrapper.getInstance().query( "chat.room.[].name.==." + actId );
		if( tmp.length > 0) {
			room = new ABC4GSDItem("chat.room", tmp[0]);
			room.update();
		} else {
			room = new ABC4GSDItem("chat.room");
			room.set("name", actId);
			room.attach("activity", actId);
		}

		// Updating list of user's chats
		String q = "chat.user_rooms.[].name.==." + MasterClientWrapper.getInstance().getMyId(); 
		tmp = MasterClientWrapper.getInstance().query(q);
		if( tmp.length == 0 ) {
			IABC4GSDItem refs = new ABC4GSDItem( "chat.user_rooms" );
			refs.set("name", MasterClientWrapper.getInstance().getMyId() );
			refs.attach("user", MasterClientWrapper.getInstance().getMyId() );
			tmp = new String[]{ "" + refs.getId() };
		}
		q = "chat.user_rooms." + tmp[0] + ".room.+." + room.getId();
		MasterClientWrapper.getInstance().query(q);
		
		// init unread messages and starts listening for incoming
		unreadMsg = 0;
		title = MasterClientWrapper.getInstance().query("abc.activity." + actId + ".name")[0];
		changeTitle();
		listener = new ListenerChat(this);
	}
	
	private Control createContainer() {
		Composite control = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		control.setLayout(layout);

		transcript = new Text(control, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		transcript.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		transcript.setEditable(false);
		transcript.setBackground(transcript.getDisplay().getSystemColor( SWT.COLOR_INFO_BACKGROUND));
		transcript.setForeground(transcript.getDisplay().getSystemColor( SWT.COLOR_INFO_FOREGROUND));

		entry = new Text(control, SWT.BORDER | SWT.WRAP);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
		gridData.heightHint = entry.getLineHeight() * 2;
		entry.setLayoutData(gridData);
		entry.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.CR) {
					sendMessage();
					event.doit = false;
				}
			}
		});
		
		return control;
	}
	
	private void sendMessage() {
		//message = timestamp:string,text:str
		//[message]
		//	user = user._id
		String msg = entry.getText();
		String msg_conv = null;
		try {
		    byte[] ascii = msg.getBytes("ASCII"); 
		    msg_conv = new String(ascii, "ASCII");
		} catch (UnsupportedEncodingException e) { }
		if( msg_conv.length() == 0 ) return; 
		IABC4GSDItem wip = new ABC4GSDItem( "chat.message" );
		MasterClientWrapper.getInstance().query("chat.message." + wip.getId() + ".timestamp.=.?TIME?");
		
		wip.set("text", msg_conv);
		wip.attach("user", MasterClientWrapper.getInstance().getMyId());

		// attaching new message to the room
		room.attach("msg", wip.getId());
		
		// cleaning the entry field
		entry.setText("");
		
		// hack to avoid considering it a new message
		unreadMsg -= 1;
	}
	public void receiveMessage( String message ) { safeWrite(message); }
	public void receiveMessage( IABC4GSDItem message ) {
		String line = "";
		String time = message.get("timestamp").toString();
		time = time.split(" ")[1];
		time = time.substring(0, 8); // Getting only hh:mm:ss
		line += "[" + time + "] ";
		line += "<" + getUser(message.get("user").toString()) + "> ";
		line += message.get("text").toString();
		safeWrite(line);
		if( !initializing ) { 
			unreadMsg += 1;
			changeTitle();
		}
		// TODO> Invasive visualization
		MasterClientWrapper.getInstance()._getHandler()._openChat(actId);
	}
	private void safeWrite( final String msg ) {
		if (Display.getCurrent() != null) {
			write(msg);
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				write(msg);
			}
		});
	}
	private void write( String msg ) {
		transcript.clearSelection();
//		transcript.showSelection();
		int from = transcript.getCharCount();
		transcript.append(msg);
		transcript.append("\n");
		scrollToEnd( from, -1 );
	}
	private void scrollToEnd( int begin, int end ) {
		int n = transcript.getCharCount();
		if(begin == -1) begin = n;
		if(end == -1) end = n;
		transcript.setSelection(begin, end);
		transcript.showSelection();
	}
	private void getExistingMessages() {
		String q = "chat.room." + room.getId() + ".msg";
		String[] tmp = MasterClientWrapper.getInstance().query(q);
		IABC4GSDItem msg;
		for( String mId : tmp ) {
			msg = new ABC4GSDItem("chat.message", mId);
			msg.update();
			receiveMessage( msg );
		}
		// TODO> this does not work. It does not scroll down newly opened chats
//		final int from = transcript.getCharCount();
//		scrollToEnd( from, -1 );
	}
	public void closeProcedure() {
		listener.unsubscribe();
		String q = "chat.user_rooms.[].name.==." + MasterClientWrapper.getInstance().getMyId();
		String[] tmp = MasterClientWrapper.getInstance().query(q);
		if( tmp.length == 0 ) return;
		q = "chat.user_rooms." + tmp[0] + ".room.-." + room.getId();
		MasterClientWrapper.getInstance().query(q);
	}
	public long getActId() { return actId; }
	public String getUser( String id ) { return MasterClientWrapper.getInstance().query("abc.user."+id+".name")[0]; }	
	public IABC4GSDItem getRoom() { return room; }
	public void setFocus() {
		entry.setFocus();
		unreadMsg = 0;
		changeTitle();
	}
	private void changeTitle() {
		String tmp = title.length()>17 ? title.substring(0,15) + ".." : title;
		if( unreadMsg > 0 ) tmp += " [" + unreadMsg + "]";
		setText( tmp );
		setToolTipText( title );
	}
	
}
