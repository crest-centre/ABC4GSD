#!/usr/bin/env python
# -*- coding: utf8 -*-

# Project       ABC client
# file name:    ABCClient.py
# Version:      1.1.0
#
# DESCRIPTION
# 
#
# Developer:
# Paolo Tell <pate@itu.dk>
#
# History/Start :
# creation: 04/gen/2011 - Paolo Tell
#
# License:
#         
#
# -*- coding: utf-8 -*-



import wxversion
try:
	wxversion.select("2.8")
except wxversion.VersionError:
	pass


import wx, os, sys
import time
from datetime import datetime as DT
import subprocess
import zmq, json

import library
import library.constants as CO
from library.utils import utils as UT
from library.client.ClHandler import ClABCHandler 



class Logger:
	def __init__(self,logger):
		self.logger = logger
		self.buff = ""
	def write(self, string):
		if len(string)>0 and string[-1] == '\n':
			self.buff = 'Client>\t' + self.buff.strip().__repr__()
			self.logger.send( self.buff )
			self.buff = ""
		self.buff += string



class ConnectionManager(object):
	def __init__(self, cfg):
		self.close = False
		self.locking = 0
		self.context = zmq.Context()
		self.backend = self.context.socket( zmq.XREQ )
		self.backend.connect( cfg.get('General', 'backend') )
		self.logger = self.context.socket(zmq.PUSH)
		self.logger.connect( cfg.get( 'General', 'client_logger_addr' ) )
		self.publisher = self.context.socket( zmq.PUB )
		self.publisher.bind( "tcp://*:%s" % (cfg.get('General', 'client_pub_port'), ) )
		self.control = self.context.socket( zmq.REP )
		self.control.bind( "tcp://*:%s" % (cfg.get('General', 'client_rep_port'), ) )
	def terminate(self):
		self.close = True
		while self.locking:
			continue
		self.control.close()
		self.publisher.close()
		self.logger.close()
		self.backend.close()
		self.publisher.close()
		self.context.term()



class ABCClient(object):
	def __init__(self):
		self._mode = self._get_AdvPressed()

		cfg = "ABCClient.cfg"
		self._cfg = UT.load_cfg(cfg)
		#self.__cfgCheckDefaults()
		self.connectionManager = ConnectionManager( self._cfg )
		newLogger = Logger(self.connectionManager.logger)
		# Remove to start the logging
		sys.stdout = newLogger

		self._handler = ClABCHandler( self.connectionManager )
		self._handler._frames['main'] = ActivityManager(None,-1,"Activity Manager", self )

		self.__initGUI()

	def _get_AdvPressed(self):
		"""Hack for key hook"""
		ms = wx.GetMouseState()
		if ms.shiftDown: 
			sel = 0
		elif ms.controlDown: 
			# text for query
			sel = 1
		else: 
			# normal usage
			sel = -1        
		return sel

	def __cfgCheckDefaults(self): # TODO> update new stuff
		if 'General' not in self._cfg.sections():
			self._cfg.add_section('General')
			self._cfg.set('General', 'ServerIp', 'localhost')
			self._cfg.set('General', 'ServerPort', '5000')
			self._cfg.set('General', 'ClientIp', 'localhost')
			self._cfg.set('General', 'ClientPort', '5001')
			self._cfg.set('General', 'cfgFile', cfgFile)
			self._cfg.set('General', 'UserName', 'Paolo')
			self._cfg.set('General', 'frameFolder', '../library/basic_frames')
		if 'ActivityManager' not in self._cfg.sections():
			self._cfg.add_section('ActivityManager')
			self._cfg.set('', 'Position_x', '0')
			self._cfg.set('ActivityManager', 'Position_y', '0')
			self._cfg.set('ActivityManager', 'Dimension_x', '100')
			self._cfg.set('ActivityManager', 'Dimension_y', '50')
		if 'Frames' not in self._cfg.sections():
			self._cfg.add_section('Frames')
			self._cfg.set('Frames', 'application', 'ApplicationList.py')
			self._cfg.set('Frames', 'notifications', 'NotificationManager.py')
			self._cfg.set('Frames', 'activities', 'ActivityList.py')
			self._cfg.set('Frames', 'contacts', 'ContactList.py')

	def _getHandler(self):
		return self._handler
	def _getCfg(self):
		return self._cfg

	def _log(self, msg, raw=False):
		if isinstance(msg, str):
			msg = (msg, )
		for x in msg:
			self._handler._frames['main']._log(x, raw)

	def __initGUI(self):
		if self._mode == -1:
			# normal usage
			self._log('Normal usage')
			for model in self._cfg.get('General', 'model').split(','):
				if len(model):            
					self._log( self._handler.connect( self._cfg.get('General', 'UserName'), model ))
					if self._handler._me == None:
						return
			self._executeGUI()

		elif self._mode == 1:
			# ctrl - only text for query 
			self._log('Ctrl usage')

		elif self._mode == 0:
			# shift - ?
			self._log('Shift usage')

	def _executeGUI(self):
		folder = self._cfg.get('General', 'frameFolder')
		toLoad = self._cfg.items('Frames')
		for name, file in toLoad:
			if name in ['activities']:
				self._handler._execute(name,'python', [os.path.join(folder, file)])                

	def CloseGUI(self):
		self._handler.suspend(True)
		time.sleep(1)
		for x in self._handler._model:
			self.disconnect(x)
		return


    # ----------------------------------------------
    # ActivityManager functionalities
    # ----------------------------------------------
	def connect(self, name, model):
		self._handler._clearGUI()
		self._log( self._handler.connect( name, model ))

		self._cfg.set('General', 'UserName', name)
		self._cfg.set('General', 'model', ', '.join(self._handler._model))

		self._handler._frames['main'].SetStatusText(self._cfg.get('General', 'ServerIp') + ':' + str(self._cfg.get('General', 'ServerPort')), 1)
		self._handler._frames['main'].SetStatusText(self._cfg.get('General', 'UserName'), 2)
		self._handler._frames['main'].SetStatusText(repr(self._handler._model), 3)

	def disconnect(self, model):
		self._log( self._handler.disconnect(model) )
		self._handler._frames['main'].SetStatusText(repr(self._handler._model), 3)

	def setServer(self, addr, port):
		self._log(self._handler.setServer( addr, port ) )
	def store(self, wip):
		self._log(self._handler.store( wip ))
	def restore(self, wip='srv.state'):
		self._log(self._handler.restore( wip ))
	def run(self, wip):
		self._log( self._handler.run( wip ), True )
	def getDataCopy(self):
		return self._handler._getDataCopy()



class ActivityManager(wx.Frame):
	def __init__(self, parent, id,  title, engine): 
		wx.Frame.__init__(self, parent, id,  title) 

		self._engine = engine
		self._cfg = self._engine._getCfg()
		self._handler = self._engine._getHandler()

		self.__obj_content = {}
		self.__obj_content["log"] = [ "TextCtrl", wx.TextCtrl(self, style = wx.TE_MULTILINE|wx.VSCROLL) ]
		self._history = []
		self._historyIdx = 0

		self.CreateStatusBar(4)
		menuBar = wx.MenuBar() 
		filemenu= wx.Menu() 

		filemenu.Append(CO.menu_CLOSEGUI,"&Close GUI"," Close Gui")
		filemenu.Append(wx.ID_EXIT,"E&xit"," Terminate the program")
		menuBar.Append(filemenu,"&File")

		filemenu= wx.Menu() 
		filemenu.Append(CO.menu_CONNECT, "&Connect", " Connect to the ABC server")
		filemenu.Append(CO.menu_DISCONNECT, "&Disconnect", " Disconnect from the ABC server")
		filemenu.Append(CO.menu_SUSPEND_ACTIVITY, "Suspend", " Suspend current activity")        
		filemenu.AppendSeparator()
		filemenu.Append(CO.menu_SRV_SET, "&Set", " Set server parameters")
		filemenu.Append(CO.menu_SRV_STORE, "&Store", " Save server state")
		filemenu.Append(CO.menu_SRV_RESTORE, "&Restore", " Load server state")
		filemenu.AppendSeparator()
		filemenu.Append(CO.menu_SRV_LOAD, "&Load", " Load queries from file")
		filemenu.Append(CO.menu_SRV_SAVE, "&Save", " Store content in file")
		filemenu.AppendSeparator()
		filemenu.Append(CO.menu_SRV_RUN, "&Run\tCtrl+R", " Run selected text")        
		filemenu.Append(CO.menu_SRV_RUN_PREV, "&Show Prev\tCtrl+UP", " Show previous command")        
		filemenu.Append(CO.menu_SRV_RUN_NEXT, "&Show Next\tCtrl+DOWN", " Show next command")        
		filemenu.AppendSeparator()
		menuBar.Append(filemenu,"&Server")

		self.SetMenuBar(menuBar)
		wx.EVT_MENU(self, wx.ID_EXIT, self.OnExit)
		self.Bind(wx.EVT_CLOSE, self.OnClose)
		self.Bind(wx.EVT_MENU, self.OnCloseGui, id=CO.menu_CLOSEGUI)

		self.Bind(wx.EVT_MENU, self.OnConnect, id=CO.menu_CONNECT)
		self.Bind(wx.EVT_MENU, self.OnDisconnect, id=CO.menu_DISCONNECT)
		self.Bind(wx.EVT_MENU, self.OnSuspend, id=CO.menu_SUSPEND_ACTIVITY)
		self.Bind(wx.EVT_MENU, self.OnSetServer, id=CO.menu_SRV_SET)
		self.Bind(wx.EVT_MENU, self.OnStore, id=CO.menu_SRV_STORE)
		self.Bind(wx.EVT_MENU, self.OnRestore, id=CO.menu_SRV_RESTORE)
		self.Bind(wx.EVT_MENU, self.OnLoad, id=CO.menu_SRV_LOAD)
		self.Bind(wx.EVT_MENU, self.OnSave, id=CO.menu_SRV_SAVE)
		self.Bind(wx.EVT_MENU, self.OnRun, id=CO.menu_SRV_RUN)
		self.Bind(wx.EVT_MENU, self.OnRunPrev, id=CO.menu_SRV_RUN_PREV)
		self.Bind(wx.EVT_MENU, self.OnRunNext, id=CO.menu_SRV_RUN_NEXT)

		self.SetSize((int(self._cfg.get('ActivityManager', 'Dimension_x')), int(self._cfg.get('ActivityManager', 'Dimension_y'))))
		self.SetPosition((int(self._cfg.get('ActivityManager', 'Position_x')), int(self._cfg.get('ActivityManager', 'Position_y'))))

		self.Show(1) 

	def OnSetServer(self, event):
		dlg = wx.TextEntryDialog(self, 'Connection information ... ','Address')
		dlg.SetValue("localhost:5000")
		if dlg.ShowModal() == wx.ID_OK:
			addr = dlg.GetValue().split(':')[0]
			port = int(dlg.GetValue().split(':')[1] )
			self._cfg.set('General', 'ServerIp', addr)
			self._cfg.set('General', 'ServerPort', port)
			self._engine.setServer( addr, port )
			self.SetStatusText(addr + ':' + str(port), 1)
		dlg.Destroy()

	def OnConnect(self, event):
		dlg = wx.TextEntryDialog(self, 'Connection information ... ','User name - Model')
		dlg.SetValue("Paolo - abc")
		if dlg.ShowModal() == wx.ID_OK:
			tmp = dlg.GetValue()
			name = tmp.split('-')[0].strip()
			model = tmp.split('-')[1].strip()
			self._engine.connect( name, model )
		dlg.Destroy()

	def OnDisconnect(self, event):
		dlg = wx.TextEntryDialog(self, 'Disconnect from ... ','Model')
		dlg.SetValue("abc")
		if dlg.ShowModal() == wx.ID_OK:
			model = dlg.GetValue()
			self._engine.disconnect( model )
		dlg.Destroy()

	def OnSuspend(self, event):
		self._handler.suspend()

	def OnStore(self, event):
		dlg = wx.TextEntryDialog(self, 'Store server state ... ','File name')
		dlg.SetValue("srv.state")
		if dlg.ShowModal() == wx.ID_OK:
			self._engine.store( dlg.GetValue() )
		dlg.Destroy()

	def OnRestore(self, event):
		dlg = wx.TextEntryDialog(self, 'Restore server state ... ','File name')
		dlg.SetValue("srv.state")
		if dlg.ShowModal() == wx.ID_OK:
			self._engine.restore( dlg.GetValue() )
		dlg.Destroy()

	def OnLoad(self, event):
		dlg = wx.TextEntryDialog(self, 'Load text from file ... ','File name')
		dlg.SetValue("query")
		if dlg.ShowModal() == wx.ID_OK:
			filename = dlg.GetValue()
			if not os.path.exists(filename):
				return
			f=open(filename, 'r')
			self.__obj_content["log"][1].AppendText(''.join(f))
			f.close()
		dlg.Destroy()

	def OnSave(self, event):
		dlg = wx.TextEntryDialog(self, 'Save text to file ... ','File name')
		dlg.SetValue("query")
		if dlg.ShowModal() == wx.ID_OK:
			filename = dlg.GetValue()
			txt = self.__obj_content["log"][1].GetValue()
			f = open(filename, 'w')
			f.write(txt)
			f.close()
		dlg.Destroy()

	def OnRun(self, event):
		tmp = self.__obj_content["log"][1].GetStringSelection()
		txt = self.__obj_content["log"][1].GetValue()
		idx = self.__obj_content["log"][1].GetSelection()
		if not len(tmp):
			x = idx[0]
			y = idx[1]
			while x>0:
				if x == len(txt):
					x -= 1
				if txt[x] == '\n':
					x += 1
					break
				x -= 1 
			while y < len(txt):
				if txt[y] == '\n':
					break
				y += 1
			if x<0:
				x=0
			tmp = txt[x:y]
		self._history.append(str(tmp))
		self._historyIdx = len(self._history)-1
		self._engine.run(tmp)

	def OnRunPrev(self, event):
		if len(self._history) ==0:
			return
		self._log(self._history[self._historyIdx], True)
		self._historyIdx -= 1
		if self._historyIdx < 0:
			self._historyIdx = len(self._history) - 1 

	def OnRunNext(self, event):
		if len(self._history) ==0:
			return
		self._log(self._history[self._historyIdx], True)
		self._historyIdx += 1
		if self._historyIdx == len(self._history):
			self._historyIdx = 0

	def _log(self, msg, raw=False):
		if not raw:
			now = DT.now()
			self.__obj_content["log"][1].AppendText( '%2s:%2s:%2s' % (now.hour, now.minute, now.second, ) + ' --> ' + repr(msg) + '\n')
		else:
			if msg == None:
				msg = 'None'
			if isinstance(msg, str):
				self.__obj_content["log"][1].AppendText( msg + '\n' )
			else:
				self.__obj_content["log"][1].AppendText( repr(msg[1]) + '\n' )

	def __storeExit(self):
		self._cfg.set('ActivityManager', 'Position_x', self.GetPosition()[0])
		self._cfg.set('ActivityManager', 'Position_y', self.GetPosition()[1])
		self._cfg.set('ActivityManager', 'Dimension_x', self.GetSize()[0])
		self._cfg.set('ActivityManager', 'Dimension_y', self.GetSize()[1])
		UT.save_cfg(self._cfg, self._cfg.get('General', 'cfgFile'))

	def OnExit(self,e):
		self.Close( True )

	def OnClose(self, e):
		self._handler.connectionManager.terminate()
		self.__storeExit()
		self.Destroy()

	def OnCloseGui(self, e):
		self._engine.CloseGUI()

def main():
	global app
	app = wx.PySimpleApp()

	p = ABCClient()

	app.SetExitOnFrameDelete(True)
	app.MainLoop() 



if __name__ == "__main__":
	main()



