package itu.abc4gsd.rcp.client_v6.view.chatV;

 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class ChatViewContainer extends ViewPart {
	public static final String ID = "itu.abc4gsd.rcp.client_v6.view.chat";
	private CTabFolder tabFolder;
	private ListenerContainer listener;
	public Map<Long,ChatViewItem> chats = new HashMap<Long, ChatViewItem>();

	public ChatViewContainer() { 
		this.listener = new ListenerContainer( this );
	}

	public void createPartControl(Composite parent) { 
	    parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    parent.setLayout(new GridLayout(1, true));
//	    createButtons(parent);
	    
	    tabFolder = new CTabFolder(parent, SWT.BOTTOM);
	    tabFolder.setBorderVisible(true);
	    tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

	    tabFolder.addSelectionListener( new SelectionListener() {
			public void widgetSelected(SelectionEvent e) { ((ChatViewItem)tabFolder.getSelection()).setFocus(); }
			public void widgetDefaultSelected(SelectionEvent e) {}
			});
	    
	    tabFolder.addCTabFolder2Listener( new CTabFolder2Listener() {
			public void showList(CTabFolderEvent event) {}
			public void restore(CTabFolderEvent event) {}
			public void minimize(CTabFolderEvent event) {}
			public void maximize(CTabFolderEvent event) {}
			public void close(CTabFolderEvent event) { removeChat( (ChatViewItem)event.item ); }
		});
	    
		reopenChats();
	}
	public void setFocus() {
		ChatViewItem sel = (ChatViewItem)tabFolder.getSelection();
		if( sel != null )
			sel.setFocus(); 
	}
	public void reopenChats() {
		String q = "chat.user_rooms.[].name.==." + MasterClientWrapper.getInstance().getMyId();
		String[] tmp = MasterClientWrapper.getInstance().query(q);
		if( tmp.length == 0 ) return;
		String rooms = tmp[0];
		q = "chat.user_rooms." + rooms + ".room";
		tmp = MasterClientWrapper.getInstance().query(q);
		
		String joint = "[";
		for( String x : tmp )
			joint += x + ",";
		joint = joint.substring(0, joint.length()-1) + "]";
		q = "chat.user_rooms." + rooms + ".room.-." + joint;
		MasterClientWrapper.getInstance().query(q);
				
		for( String x : tmp ) {
			q = "chat.room." + x + ".activity";
			addChat( Long.parseLong(MasterClientWrapper.getInstance().query(q)[0]) );
		}
	}
	public void addChat( long actId ) {
		// if chat is not already visible
		if( !chats.containsKey(actId) ) 
			// create a chat instance and load the stuff
			chats.put( actId, new ChatViewItem( tabFolder, SWT.CLOSE, actId ) );
		// bring the last chat to the front
		tabFolder.setSelection( chats.get(actId) );
//		chats.get(actId)
//		tabFolder.forceFocus();
	}
	public void removeChat( ChatViewItem tab ) {
		if( chats.containsKey( tab.getActId() ) ) {
			System.out.println("Closing " + tab.getActId() );
			// delegating the real closure to the chat itself
			chats.get( tab.getActId() ).closeProcedure();
			chats.get( tab.getActId() ).dispose();
			// removing the reference
			chats.remove( tab.getActId() );
		}
	}
	public boolean containsChat( long actId ) { return chats.containsKey(actId); }
}