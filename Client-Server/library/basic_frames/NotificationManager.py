#!/usr/bin/env python
# -*- coding: utf8 -*-

import wx

import sys, os
sys.path.append(os.getcwd())
import library.constants as CO
from library.utils import utils as UT
from library.client.ABCAppInterface import ABCAppInterface


class NotificationManager(wx.Frame, ABCAppInterface):
    def __init__(self, parent, id,  title, param): 
        wx.Frame.__init__(self, parent, id,  title)
        ABCAppInterface.__init__(self, 'NotificationManager')
        self._definedProperties = ['pos_x', 'pos_y', 'dim_x', 'dim_y']
        
        self.lastActivity = None
        self.__order = [ ('Message', 'Message'), ]
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
#        menuBar.Append(filemenu,"&Action")
#
#        self.SetMenuBar(menuBar)
#        wx.EVT_MENU(self, wx.ID_EXIT, self.onExit)
#        self.Bind(wx.EVT_CLOSE, self.onClose)
#
#        self.Bind(wx.EVT_MENU, self.onNew, id=CO.menu_USER_ADD)
#        self.Bind(wx.EVT_MENU, self.onModify, id=CO.menu_USER_MODIFY)

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

    def personalHandler(self, ch, msg):
        type = msg[0]
        msg = msg[1]
        if type == 'INFO':
            pass
        if type == 'CMD':
            if 'INIT' in msg:
                self.DeleteAllItems(True)

    def deleteAllItems(self, content = False):
        self.__obj_content["list"][1].DeleteAllItems()
        if content:
            self.__obj_property["notifications"] = {}

    def onNew(self, event):
        pass
    def onModify(self, event):
        pass

    def onExit(self,e):
        self.Close( True )
    
    def onClose(self, e):
        #self.Suspend()
        self.Destroy()







def main( param = None ):
    global app
    app = wx.PySimpleApp()
    frame = NotificationManager(None,-1,"Notification Manager", param) 
    app.SetExitOnFrameDelete(True)
    app.MainLoop() 



if __name__ == "__main__":
    main()




