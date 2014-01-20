package itu.abc4gsd.rcp.client_v6.editor;


 import itu.abc4gsd.rcp.client_v6.logic.MasterClientWrapper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.EditorPart;

public class EditorRun extends EditorPart {
	public static String ID = "itu.abc4gsd.rcp.client_v6.editor.run";
	private Text transcript;
	private Text entry;

	public EditorRun() {}
	public void doSave(IProgressMonitor monitor) {}
	public void doSaveAs() {}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName("Shell");
	}

	public boolean isDirty() { return false; }

	public boolean isSaveAsAllowed() { return true; }

	public void createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		top.setLayout(layout);

		transcript = new Text(top, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		transcript.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));
		transcript.setEditable(false);
		transcript.setBackground(transcript.getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
		transcript.setForeground(transcript.getDisplay().getSystemColor(
				SWT.COLOR_INFO_FOREGROUND));

		entry = new Text(top, SWT.BORDER | SWT.WRAP);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
				false);
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
	}

	public void setFocus() {
		if (entry != null && !entry.isDisposed()) {
			entry.setFocus();
		}
	}

	private String renderMessage(String body) { return "--> " + body; }

//	private void scrollToEnd() { scrollToEnd(-1,-1); }
	private void scrollToEnd( int begin, int end ) {
		int n = transcript.getCharCount();
		if(begin == -1) begin = n;
		if(end == -1) end = n;
		transcript.setSelection(begin, end);
		transcript.showSelection();
	}

	private void sendMessage() {
		String cmd = entry.getText();
//		if (cmd.length() == 0)
//			return;
		transcript.clearSelection();
		transcript.showSelection();
		transcript.append(renderMessage(cmd));
		transcript.append("\n");
		entry.setText("");
		int from = transcript.getCharCount();
		if( !cmd.startsWith("query") )
			transcript.append(MasterClientWrapper.getInstance().run(cmd).toString());
		else {
			cmd = cmd.substring("query".length()+1);
			String[] resp = MasterClientWrapper.getInstance().query(cmd);
			for( String item : resp )
				transcript.append( item.toString() );
		}
		transcript.append("\n");
		scrollToEnd( from, -1 );
	}

	public void dispose() {
		super.dispose();
	}
}








