package itu.abc4gsd.rcp.client_v6.preferences;

 import itu.abc4gsd.rcp.client_v6.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferencesInitializer extends AbstractPreferenceInitializer {

	public PreferencesInitializer() {
		super();
	}

	public void initializeDefaultPreferences() {
		IEclipsePreferences p = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		p.put(ConnectionAdvanced.ADDR_BACKEND, "tcp://test2lab3.itu.dk:5552");
		p.put(ConnectionAdvanced.ADDR_LOGGER, "tcp://test2lab3.itu.dk:5566");
//		p.put(ConnectionAdvanced.ADDR_BACKEND, "tcp://localhost:5552");
//		p.put(ConnectionAdvanced.ADDR_LOGGER, "tcp://localhost:5566");
//		p.put(ConnectionAdvanced.ADDR_PUBLISHER, "tcp://*:5562");
//		p.put(ConnectionAdvanced.ADDR_CONTROL, "tcp://*:5563");
		p.put(ConnectionAdvanced.ADDR_REPOSITORY, "git://test2lab3.itu.dk/gitsrv");
//		p.put(ConnectionAdvanced.ADDR_BACKEND, "tcp://success1.ict.swin.edu.au:5552");
//		p.put(ConnectionAdvanced.ADDR_LOGGER, "tcp://success1.ict.swin.edu.au:5566");
		p.put(ConnectionAdvanced.ADDR_PUBLISHER, "tcp://*:5562");
		p.put(ConnectionAdvanced.ADDR_CONTROL, "tcp://*:5563");
//		p.put(ConnectionAdvanced.ADDR_REPOSITORY, "ptell@success1.ict.swin.edu.au:/home/research/ptell/gitRepo");
		
		p.put(Connection.USER_NAME, "Paolo");
		p.put(Connection.USER_MODEL, "abc");
		p.putBoolean(Connection.AUTO_LOGIN, false);
		p.put(Needs.LOCAL_REPO, "gitsrv" );
//		p.put(Needs.NEEDS, "[needs]\n" +
//				".java = java_editor\n" +
//				".pdf = pdf_viewer\n" + 
//				".key = key_viewer\n" + 
//				".ods = spreadsheet\n" + 
//				"eclipse_repo = eclipse\n" +
//				"* = default\n\n" + 
//				"[java_editor]\n" +
//				"command = see <target> &\n" +
//				"name = textmate\n" +
//				"interface = false\n" +
//				"control = default\n" +
//				"paramNr = 1\n" +
//				"param0 = <target>\n\n" + 
//				"[pdf_viewer]\n" +
//				"command = see <file>\n" +
//				"name = Skim\n" +
//				"interface = false\n" +
//				"control = default\n" +
//				"paramNr = 1\n" +
//				"param0 = <file>\n\n" +
//				"[key_viewer]\n" +
//				"command = open -a Keynote -g <file>\n" +
//				"name = Keynote\n" +
//				"interface = false\n" +
//				"control = default\n" +
//				"paramNr = 1\n" +
//				"param0 = <file>\n\n" +
//				"[spreadsheet]\n" +
//				"command = see <file>\n" +
//				"name = LibreOffice\n" +
//				"interface = false\n" +
//				"control = default\n" +
//				"paramNr = 1\n" +
//				"param0 = <file>\n\n" +
//				"[eclipse]\n" +							
//				"command = /Applications/eclipseABC/eclipse &\n" +
//				"name = eclipse\n" +
//				"interface = true\n" +
//				"paramNr = 0\n\n" +
//				"[default]\n" +
//				"command = see <target>\n" +
//				"name = textmate\n" +
//				"interface = false\n" +
//				"control = default\n" +
//				"paramNr = 1\n" +
//				"param0 = <target>\n\n"
//				);
		p.put(Needs.NEEDS, "[needs]\n" +
				".java = java_editor\n" +
				".pdf = pdf_viewer\n" + 
				".key = key_viewer\n" + 
				".ods = spreadsheet\n" + 
				".odt = document\n" + 
				"eclipse_repo = eclipse\n" +
				"* = default\n\n" + 
				"[java_editor]\n" +
				"command = open -a textmate -g <target>\n" +
				"name = textmate\n" +
				"interface = false\n" +
				"control = default\n" +
				"paramNr = 1\n" +
				"param0 = <target>\n\n" + 
				"[pdf_viewer]\n" +
				"command = open -a Skim -g <file>\n" +
				"name = Skim\n" +
				"interface = false\n" +
				"control = default\n" +
				"paramNr = 1\n" +
				"param0 = <file>\n\n" +
				"[key_viewer]\n" +
				"command = open -a Keynote -g <file>\n" +
				"name = Keynote\n" +
				"interface = false\n" +
				"control = default\n" +
				"paramNr = 1\n" +
				"param0 = <file>\n\n" +
				"[spreadsheet]\n" +
				"command = open -a LibreOffice -g <file>\n" +
				"name = LibreOffice\n" +
				"interface = false\n" +
				"control = default\n" +
				"paramNr = 1\n" +
				"param0 = <file>\n\n" +
				"[document]\n" +
				"command = open -a LibreOffice -g <file>\n" +
				"name = LibreOffice\n" +
				"interface = false\n" +
				"control = default\n" +
				"paramNr = 1\n" +
				"param0 = <file>\n\n" +
				"[eclipse]\n" +							
				"command = /Applications/eclipseABC/eclipse &\n" +
				"name = eclipse\n" +
				"interface = true\n" +
				"paramNr = 0\n\n" +
				"[default]\n" +
				"command = open -a textmate -g <target>\n" +
				"name = textmate\n" +
				"interface = false\n" +
				"control = default\n" +
				"paramNr = 1\n" +
				"param0 = <target>\n\n"
				);

		p.put(Scripts.SCRIPT, "When###Condition###Delay(s)###Script\n" +
				"INIT###'' length == 0###0###'+.library/data/abcV2.schema.&.+.library/data/chat.schema.&.+.library/data/notification.schema.&.abc.user.[abc.user.+].name.=.Paolo.&.abc.user.[abc.user.[].name.==.Paolo].state.=.11101'\n" +		
				"OFF_Scenario2_LOGIN###None None None None###60###'abc.activity.[abc.activity.+].name.=.{{Bug}}.&.abc.activity.[abc.activity.[].name.==.{{Bug}}].creator.+.[abc.user.[].name.==.Paolo].&.abc.activity.[abc.activity.[].name.==.{{Bug}}].state.=.11002.&.abc.activity.[abc.activity.[].name.==.{{Bug}}].description.=.{{This is urgent. Please take care of this ASAP.}}.&.abc.state.[abc.state.+].name.=.<WIP>.&.abc.state.[abc.state.[].name.==.<WIP>].state.=.12001.&.abc.state.[abc.state.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.state.[abc.state.[].name.==.<WIP>].user.+.[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.state.[abc.state.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.state.[abc.state.+].name.=.<WIP>.&.abc.state.[abc.state.[].name.==.<WIP>].state.=.12001.&.abc.state.[abc.state.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.state.[abc.state.[].name.==.<WIP>].user.+.[abc.user.[].name.==.Paolo].&.abc.state.[abc.state.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.Paolo].&.abc.ecology.[abc.ecology.+].name.=.<WIP>.&.abc.ecology.[abc.ecology.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].user.+.[abc.user.[].name.==.Paolo].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.Prj2].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.BugDesc].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.Paolo].&.abc.ecology.[abc.ecology.+].name.=.<WIP>.&.abc.ecology.[abc.ecology.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].user.+.[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.Prj2].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.BugDesc].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.user.[abc.user.[].name.==.Paolo].activity.=.[abc.activity.[].name.==.{{Bug}}].&.abc.user.[abc.user.[].name.==.Paolo].artifact.=.[abc.asset.[].name.==.{{BugDesc}}].&.abc.user.[abc.user.[].name.==.Paolo].state.=.11102'\n" +		
				"OFF_LOGOUT###'abc.user.[abc.user.[].name.==.{{Gian}}].state' in == 11103###60###'abc.activity.[abc.activity.+].name.=.{{Bug}}.&.abc.activity.[abc.activity.[].name.==.{{Bug}}].creator.+.[abc.user.[].name.==.Paolo].&.abc.activity.[abc.activity.[].name.==.{{Bug}}].state.=.11002.&.abc.activity.[abc.activity.[].name.==.{{Bug}}].description.=.{{This is urgent. Please take care of this ASAP.}}.&.abc.state.[abc.state.+].name.=.<WIP>.&.abc.state.[abc.state.[].name.==.<WIP>].state.=.12001.&.abc.state.[abc.state.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.state.[abc.state.[].name.==.<WIP>].user.+.[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.state.[abc.state.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.state.[abc.state.+].name.=.<WIP>.&.abc.state.[abc.state.[].name.==.<WIP>].state.=.12001.&.abc.state.[abc.state.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.state.[abc.state.[].name.==.<WIP>].user.+.[abc.user.[].name.==.Paolo].&.abc.state.[abc.state.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.Paolo].&.abc.ecology.[abc.ecology.+].name.=.<WIP>.&.abc.ecology.[abc.ecology.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].user.+.[abc.user.[].name.==.Paolo].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.Prj2].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.BugDesc].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.Paolo].&.abc.ecology.[abc.ecology.+].name.=.<WIP>.&.abc.ecology.[abc.ecology.[].name.==.<WIP>].activity.+.[abc.activity.[].name.==.{{Bug}}].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].user.+.[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.Prj2].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].asset.+.[abc.asset.[].name.==.BugDesc].&.abc.ecology.[abc.ecology.[].name.==.<WIP>].name.=.[abc.activity.[].name.==.{{Bug}}]:[abc.user.[].name.==.{{PARTICIPANT}}].&.abc.user.[abc.user.[].name.==.Paolo].activity.=.[abc.activity.[].name.==.{{Bug}}].&.abc.user.[abc.user.[].name.==.Paolo].artifact.=.[abc.asset.[].name.==.{{BugDesc}}].&.abc.user.[abc.user.[].name.==.Paolo].state.=.11102'\n" +		
				"OFF_INIT###'abc.user' length == 0###0###'abc.user.[abc.user.+].name.=.Paolo.&.abc.user.[abc.user.[].name.==.Paolo].state.=.11101'\n" +		
				"OFF_ACT_CREATED###None None None None###10###'abc.user.[abc.user.+].name.=.minna.&.abc.user.[abc.user.[].name.==.minna].state.=.11101'\n" +		
				"OFF_ACT_CHANGED###None None None None###10###'abc.user.[abc.user.+].name.=.gina.&.abc.user.[abc.user.[].name.==.gina].state.=.11101'\n"		
				);
	}
}
