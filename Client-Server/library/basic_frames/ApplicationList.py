#!/usr/bin/env python
# -*- coding: utf8 -*-

import wx

import sys, os
sys.path.append(os.getcwd())
import library.constants as CO
from library.utils import utils as UT
from library.client.ABCAppInterface import ABCAppInterface


class ApplicationList(wx.Frame, ABCAppInterface):
    def __init__(self, parent, id,  title, param): 
        wx.Frame.__init__(self, parent, id,  title)
        ABCAppInterface.__init__(self, 'ApplicationList')
        self._definedProperties = ['pos_x', 'pos_y', 'dim_x', 'dim_y']
        
        self.lastActivity=None
        self.__order = [ ('ID', '_id'), ('Name', 'name'), ('Status', 'state')]

        self.__conversion = {}
        self.__conversion['state'] = {CO.application_DETACHED:'Detached', CO.application_PINNED:'Pinned', CO.application_FULLSYNC:'Fullsync', CO.abc_UNKNOWN:'Unknown', CO.abc_INITIALIZED:'Initialized'}        
        self.__obj_content = {}
        self.__obj_property = {}
        self.__obj_content["list"] = [ "ListCtrl",  wx.ListCtrl(self, style=wx.LC_REPORT 
                                                                        | wx.BORDER_NONE
                                                                        | wx.LC_EDIT_LABELS
                                                                        | wx.LC_SORT_ASCENDING) ]
        for i, x in enumerate(self.__order):
            self.__obj_content["list"][1].InsertColumn(i, x[0])

        self.CreateStatusBar()
        menuBar = wx.MenuBar() 
#        filemenu= wx.Menu() 
#
#        filemenu.Append(wx.ID_EXIT,"E&xit"," Terminate the program")
#        menuBar.Append(filemenu,"&File")
#
#        filemenu= wx.Menu() 
#        filemenu.Append(CO.menu_USER_ADD,"New"," Create new app")
#        filemenu.Append(CO.menu_USER_MODIFY,"Modify"," Modify current app")
#
#        menuBar.Append(filemenu,"&App")
#
#        filemenu= wx.Menu() 
#        filemenu.Append(CO.menu_APPLICATION_PIN,"Pin"," Share the application with the activity partecipants")
#        menuBar.Append(filemenu,"&Action")

        self.SetMenuBar(menuBar)
        wx.EVT_MENU(self, wx.ID_EXIT, self.onExit)
        self.Bind(wx.EVT_CLOSE, self.onClose)

        self.Bind(wx.EVT_MENU, self.onNew, id=CO.menu_USER_ADD)
        self.Bind(wx.EVT_MENU, self.onModify, id=CO.menu_USER_MODIFY)

        box = wx.BoxSizer( wx.VERTICAL )
        
        box.Add( self.__obj_content[ "list" ][1], 1, wx.EXPAND )

        self.SetSizer( box )
        self.SetAutoLayout(1) 
        box.Fit(self) 

        self.resume()
        self.Show(1)

    def killOperation(self):
        self.Close(True)
    
    def suspendOperation(self):
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
        self.deleteAllItems(True)
        self.refreshApplications()
        self.setApplicationList()

        for x in self.__obj_property.keys():
            self.standardSubscribtion(x)

    def standardSubscribtion(self, id):
        self.subscribe('abc.application.%s.state'% (id, ), self.changeState )
#        self.subscribe('abc.application.%s.name'% (id, ), self.changeName )


    def personalHandler(self, ch, msg):
        type = msg[0]
        msg = msg[1]
        if type == 'INFO':
            actId = long(msg.split(' ', 1)[0])
            field = msg.split(' ', 1)[1].split('=')[0]
            value = msg.split('=')[1]
            x = self.getapp(actId)
            if x == None:
                x = {}
                self.__obj_property["apps"][actId] = x
            self.__obj_property["apps"][actId][field] = value
            self.SetappList(actId)
        if type == 'CMD':
            if 'INIT' in msg:
                self.DeleteAllItems(True)


    def refreshApplications(self):
        resp = self._query('abc.activity.%s.application'%(self._actId, ))
        if isinstance(resp, long):
            resp = [resp]
        elif isinstance(resp, str):
            resp = eval(resp)
        if resp == None:
            return
        for id in resp:
            if id in self.__obj_property.keys():
                del self.__obj_property[id]
            self.__obj_property[id] = [id]
            for y in self.__order[1:]:
                self.__obj_property[id].append( self._query('abc.application.%s.%s'%(id, y[1])) )

    def deleteAllItems(self, content = False):
        self.__obj_content["list"][1].DeleteAllItems()
        if content:
            self.__obj_property = {}

    def setApplicationList(self):
        self.deleteAllItems()
        for i, x in enumerate(self.__obj_property.keys()):
            for l, y in enumerate(self.__order):
                val = self.__obj_property[x][l]
                if y[1] in self.__conversion.keys():
                    val = self.__conversion[y[1]][int(val)]
                if isinstance(val, int) or isinstance(val, long):
                    val = str(val)
                if not l:
                    self.__obj_content["list"][1].InsertStringItem(i, val)
                else:
                    self.__obj_content["list"][1].SetStringItem(i, l, val)

    def onNew(self, event):
        pass

    def onModify(self, event):
        pass

    def onExit(self,e):
        self.Close( True )
    
    def onClose(self, e):
        #self.Suspend()
        self.Destroy()


    def changeState(self, wip):
        wip = wip.split('.')
        state = wip[-1]
        id = long(wip[2])
        self.__obj_property[id][2] = state
        self.setApplicationList()




def main( param = None ):
    global app
    app = wx.PySimpleApp()
    frame = ApplicationList(None,-1,"Application List", param) 
    app.SetExitOnFrameDelete(True)
    app.MainLoop() 



if __name__ == "__main__":
    main()




