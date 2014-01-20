#!/usr/bin/env python
# -*- coding: utf8 -*-

# Project       ABC client
# file name:    TextEditor.py
# Version:      0.1-alpha
#
# DESCRIPTION
# Scenario 1
#
# Developer:
# Paolo Tell <pate@itu.dk>
#
# History/Start :
# creation: 15/oct/2010 - Paolo Tell
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


import wx
import sys, os
sys.path.append(os.getcwd())
import library.constants as CO
from library.client.ABCAppInterface import ABCAppInterface



class TextEditorFrame(wx.Frame, ABCAppInterface):
    def __init__(self, parent, id,  title, param): 
        wx.Frame.__init__(self, parent, id,  title)
        ABCAppInterface.__init__(self, 'TextEditor')
        self._definedProperties = ['pos_x', 'pos_y', 'dim_x', 'dim_y']
        self._syncSubscription = []
        self._tmpDir = None
        self.__order = [ "txtMain", "btnSave" ]
        self.__obj_property = {}
        self.__obj_content = {}
        
        self.__obj_property["fileName"] = [ "string", "" ]
        self.__obj_property["dirName"] = [ "string", "" ]        
        self.__obj_property["activityPin"] = [ "boolean", False ]
        self.__obj_property["activityFullSync"] = [ "boolean", False ]
        
        self.__obj_content["txtMain"] = [ "TextCtrl", wx.TextCtrl(self, style = wx.TE_MULTILINE|wx.VSCROLL) ]
        self.__obj_content["btnSave"] = [ "Button", wx.Button(self, id=wx.ID_SAVE) ]
        self.__obj_content["btnSave"][1].Bind( wx.EVT_BUTTON, self.onSaveBtn )

        self.CreateStatusBar()
        
        box = wx.BoxSizer( wx.VERTICAL )        
        box.Add( self.__obj_content[ "txtMain" ][1], 1, wx.EXPAND )
        box.Add( self.__obj_content[ "btnSave" ][1], 0, wx.EXPAND )

        self.SetSizer( box )
        self.SetAutoLayout(1) 
        box.Fit(self) 
        self.SetSize(tuple([x/4 for x in wx.DisplaySize()]))

        self.Bind(wx.EVT_CLOSE, self.onClose)
        
        self.Center()
        self.resume()
        self.Show(1) 

    def onSaveBtn( self, e): 
        self._save()
    def _save(self, dir=None, file=None):
        if dir == None and file == None:
            file = self.__obj_property["fileName"][1]
            if not len(file):
                return
            dir = os.path.join( self._tmpDir, self.__obj_property["dirName"][1])
        tmp = os.path.join(dir, file)
        f = open( tmp, 'w' )
        f.write( self.__obj_content["txtMain"][1].GetValue() )
        f.close()
    def _open(self, dir, file, userSelected=False):
        if not userSelected:
            self.__obj_property["fileName"][1] = file
            self.__obj_property["dirName"][1] = dir
            file = os.path.join(self._tmpDir, os.path.join(dir, file))
        f=open(file,'r') 
        self.__obj_content["txtMain"][1].SetValue( f.read() ) 
        f.close() 


    def onClose(self,e):
        self.Destroy()
    def onExit(self,e):
        self.Close( True )

    def onOpen(self,e):
        if not self.__obj_property["dirName"][1]:
            self.__obj_property["dirName"][1] = os.getcwd()
        dlg = wx.FileDialog(self, "Choose a file", self.__obj_property["dirName"][1], "", "*", wx.OPEN) 
        if dlg.ShowModal() == wx.ID_OK: 
            self.__obj_property["fileName"][1] = dlg.GetFilename() 
            self.__obj_property["dirName"][1] = dlg.GetDirectory() 
            f=open(os.path.join(self.__obj_property["dirName"][1], self.__obj_property["fileName"][1] ),'r') 
            self.__obj_content["txtMain"][1].SetValue( f.read() ) 
            f.close() 
            dlg.Destroy()

    def onPin(self,e):
        self.__obj_property["activityPin"][1] = not self.__obj_property["activityPin"][1]
        if self.__obj_property["activityPin"][1]:
            self.__obj_content["txtMain"][1].SetBackgroundColour("lightgrey")
        else:
            self.__obj_content["txtMain"][1].SetBackgroundColour("white")
        

    def onUpdate(self,e):
        pass

    def onFullSync(self,e):
        return

    def killOperation(self):
        self.Close(True)
    
    def suspendOperation(self):
        self._save()
        self.setProperty('pos_x', self.GetPosition()[0])
        self.setProperty('pos_y', self.GetPosition()[1])
        self.setProperty('dim_x', self.GetSize()[0])
        self.setProperty('dim_y', self.GetSize()[1])
    
    def resumeOperation(self):
        try:
            self.SetPosition( (int(self.getProperty('pos_x')), int(self.getProperty('pos_y'))) )
            self.SetSize( (int(self.getProperty('dim_x')), int(self.getProperty('dim_y'))) )
        except:
            pass
        q = 'abc.user.%s.tmp_dir' % (self._userId,  )
        self._tmpDir = self._query(q)
        q = 'abc.activity_state.[abc.activity_state.[abc.activity_state.[].activity.==.%s].user.==.%s]._id' % (self._actId, self._userId,  )
        appStateId = self._query(q)
        q = 'abc.activity_state.%s.artifact' % (appStateId,  )
        resp = self._query(q)
        if resp != None:
            toOpen = self._query('abc.artifact.%s.location.&.abc.artifact.%s.name' % (resp, resp, ))
            self._open(toOpen[0], toOpen[1])
        state = self._query('abc.application.%s.state'% (self._appId, ))
        self._changeState(state)
        #self.subscribe('abc.application.%s.state'% (self._appId, ), self.changeState )
        self.subscribe('abc.activity_state.%s.artifact' % (appStateId,  ), self.changeFile )
    
    def changeFile(self, wip):
        wip = wip.split('.')
        file = int(wip[-1])
        self._save()
        toOpen = self._query('abc.artifact.%s.location.&.abc.artifact.%s.name' % (file, file, ))
        print toOpen
        self._open(toOpen[0], toOpen[1])

    def personalHandler(self, ch, msg):
        type = msg[0]
        msg = msg[1]
        if type == 'INFO':
            pass
        if type == 'CMD':
            if 'INIT' in msg:
                pass

    def changeState(self, wip):
        wip = wip.split('.')
        state = int(wip[-1])
        self._changeState(state)

    def _changeState(self, state):
        if state == CO.application_FULLSYNC:
            pass
            self.__obj_property["activityFullSync"][1] = True
            self.__obj_content["txtMain"][1].SetBackgroundColour("lightgrey")
        elif state == CO.application_PINNED:
            self.__obj_property["activityFullSync"][1] = False
            self.__obj_content["txtMain"][1].SetBackgroundColour("white")

    def OnTextChange(self, e):
        pass

def main( param = None ):
    global app
    app = wx.PySimpleApp()
    frame = TextEditorFrame(None,-1,"ABC TxtEditor", param) 
    app.MainLoop() 


if __name__ == "__main__":
    param = []
    if len(sys.argv)>1:
        param = sys.argv[1]
    main( param )



