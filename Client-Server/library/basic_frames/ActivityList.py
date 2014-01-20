#!/usr/bin/env python
# -*- coding: utf-8 -*-

import wx

import sys, os
sys.path.append(os.getcwd())
import library.constants as CO
from library.utils import utils as UT
from library.utils.gui.DialogBox import *
from library.client.ABCAppInterface import ABCAppInterface



class ActivityList(wx.Frame, ABCAppInterface):
    def __init__(self, parent, id,  title, param): 
        wx.Frame.__init__(self, parent, id,  title)
        ABCAppInterface.__init__(self, 'activities')
        self.lastActivity = None
        self._selection = None
        self.__order = [ ('ID', '_id'), ('Name', 'name'), ('Status', 'state'),  ]
        self.__conversion = {}
        self.__conversion['state'] = {CO.activity_UNKNOWN:'Unknown', CO.activity_RESUMED:'Resumed', CO.activity_SUSPENDED:'Suspended', CO.activity_INITIALIZED:'Initialized'}
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
        filemenu= wx.Menu() 

        filemenu.Append(wx.ID_EXIT,"E&xit"," Terminate the program")
        filemenu.Append(CO.menu_ACTIVITY_SUSPEND,"Suspend"," Suspend the program")
        menuBar.Append(filemenu,"&File")

        filemenu= wx.Menu() 
        filemenu.Append(CO.menu_ACTIVITY_ADD,"New"," Create new activity")
        filemenu.Append(CO.menu_ACTIVITY_MODIFY,"Modify"," Modify current activity")
        menuBar.Append(filemenu,"&Activity")

        self.SetMenuBar(menuBar)
        wx.EVT_MENU(self, wx.ID_EXIT, self.onExit)
        self.Bind(wx.EVT_CLOSE, self.onClose)
        self.Bind(wx.EVT_MENU, self.onSuspend, id=CO.menu_ACTIVITY_SUSPEND)

        self.Bind(wx.EVT_MENU, self.onNew, id=CO.menu_ACTIVITY_ADD)
        self.Bind(wx.EVT_MENU, self.onModify, id=CO.menu_ACTIVITY_MODIFY)
        self.__obj_content["list"][1].Bind(wx.EVT_LIST_ITEM_SELECTED, self.onSelect)

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
        self.set('user', self._userId, 'act_pos_x', self.GetPosition()[0])
        self.set('user', self._userId, 'act_pos_y', self.GetPosition()[1])
        self.set('user', self._userId, 'act_dim_x', self.GetSize()[0])
        self.set('user', self._userId, 'act_dim_y', self.GetSize()[1])
        self.set('user', self._userId, 'act_last', self.lastActivity)
    
    def resumeOperation(self):
        q = 'abc.user.%s.' % (self._userId, )
        q = '%s%s%s%s%s%s%s%s%s%s' % (q, 'act_pos_x.&.', q, 'act_pos_y.&.', q, 'act_dim_x.&.', q, 'act_dim_y.&.', q, 'act_last', )
        resp = self._query(q)
#            self._id = self._attrib[0]
        try:
            self.SetPosition( ( int(resp[0]), int(resp[1]) ) )
            self.SetSize( ( int(resp[2]), int(resp[3]) ) )
            self.lastActivity = int(resp[4])
        except:
            pass
        self.deleteAllItems(True)
        self.refreshActivities()
        self.setActivityList()

        self.subscribe('abc.activity.[].user.+.%s'% (self._userId, ), self.newAct )
        for x in self.__obj_property.keys():
            self.standardActivitySubscribtion(x)
            
        if self.lastActivity != None and self.lastActivity in self.__obj_property.keys():
            self._resume(self.lastActivity)
        #print self._query('abc.activity.[abc.activity.[].user.==.%s]._id'%(self._userId, ))
        #print self._query('abc.activity')

    def standardActivitySubscribtion(self, id):
        self.subscribe('abc.activity.%s.state'% (id, ), self.changeState )
        self.subscribe('abc.activity.%s.name'% (id, ), self.changeName )
#        self.subscribe('.&.'.join(['abc.activity.%s.state'%(x, ) for x in self.__obj_property.keys()]), self.changeState )
    def newAct(self, wip):
        wip = wip.split('.')
        self.standardActivitySubscribtion(wip[2])
        self.deleteAllItems(True)
        self.refreshActivities()
        self.setActivityList()        

    def personalHandler(self, ch, msg):
        type = msg[0]
        msg = msg[1]
        if type == 'INFO':
            actId = long(msg.split(' ', 1)[0])
            field = msg.split(' ', 1)[1].split('=')[0]
            value = msg.split('=')[1]
            x = self.getActivity(actId)
            if x == None:
                x = {}
                self.__obj_property["activities"][actId] = x
            self.__obj_property["activities"][actId][field] = value
            self.setActivityList(actId)
        if type == 'CMD':
            if 'INIT' in msg:
                self.deleteAllItems(True)
                

    def refreshActivities(self):
        resp = self._query('abc.activity.[abc.activity.[].user.==.%s]._id'%(self._userId, ))
        if isinstance(resp, int):
            resp = [resp]
        for id in resp:
            if id in self.__obj_property.keys():
                del self.__obj_property[id]
            self.__obj_property[id] = [id]
            for y in self.__order[1:]:
                self.__obj_property[id].append( self._query('abc.activity.%s.%s'%(id, y[1])) )

    def deleteAllItems(self, content = False):
        self.__obj_content["list"][1].DeleteAllItems()
        if content:
            self.__obj_property = {}

    def setActivityList(self):
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


    def _resume(self, id):
        self.sendCommand('RESUME %s' % (id, ))
        self.set('activity', id, 'state', str(CO.activity_RESUMED))
        self.lastActivity = id
        self.set('user', self._userId, 'act_last', self.lastActivity)
    def _suspend(self, id):
        self.sendCommand('SUSPEND %s' % (id, ))
        self.set('activity', id, 'state', str(CO.activity_SUSPENDED))

    def changeState(self, wip):
        wip = wip.split('.')
        state = wip[-1]
        id = long(wip[2])
        self.__obj_property[id][2] = state
        self.setActivityList()

    def changeName(self, wip):
        wip = wip.split('.')
        name = wip[-1]
        id = long(wip[2])
        self.__obj_property[id][1] = name
        self.setActivityList()

    def onSelect(self, event):
        current = event.m_itemIndex
        self._selection = current
        id = self.__obj_content["list"] [1].GetItemText(current)
        if id == self.lastActivity:
            return
        if self.lastActivity != None:
            self._suspend(self.lastActivity)
        self._resume(id)
        self.__obj_content["list"] [1].Select(self._selection)

    def onNew(self, event):
        id = self._query('abc.activity.+')[0]
        text = """
abc.activity.{0}.name.=.<INSERT_NAME>
abc.activity.{0}.state.=.11001
abc.activity.{0}.user.+.{1}
abc.activity.{0}.application.+.[abc.application.[abc.applicatio.[].name.==.ContactList]._id]
abc.activity.{0}.application.+.[abc.application.[abc.applicatio.[].name.==.ApplicationList]._id]
abc.activity.{0}.application.+.[abc.application.[abc.applicatio.[].name.==.NotificationList]._id]
abc.application_state.[abc.application_state.+].name.=.RADAR
abc.application_state.[abc.application_state.[].name.==.RADAR].user.+.{1}
abc.application_state.[].application.+.[abc.application.[abc.applicatio.[].name.==.ContactList]._id]
abc.application_state.[abc.application_state.[].name.==.RADAR].name.=.a
abc.application_state.[abc.application_state.+].name.=.RADAR
abc.application_state.[abc.application_state.[].name.==.RADAR].user.+.{1}
abc.application_state.[].application.+.[abc.application.[abc.applicatio.[].name.==.ApplicationList]._id]
abc.application_state.[abc.application_state.[].name.==.RADAR].name.=.a
abc.application_state.[abc.application_state.+].name.=.RADAR
abc.application_state.[abc.application_state.[].name.==.RADAR].user.+.{1}
abc.application_state.[].application.+.[abc.application.[abc.applicatio.[].name.==.NotificationList]._id]
abc.application_state.[abc.application_state.[].name.==.RADAR].name.=.a
""".format(id, self._userId)
        dlg = TextDialog(None, -1, 'Add activity', text)
        if dlg.ShowModal() == wx.ID_OK:
            value = dlg.GetValue()
            if len(value):
                self._query('.&.'.join(value))
        else:
            self._query('abc.activity.-.%s' %(id, ))
        dlg.Destroy()

    def onModify(self, event):
        pass

    def onSuspend(self,e):
        self.suspend()
    
    def onExit(self,e):
        self.Close( True )
    
    def onClose(self, e):
        #self.suspend()
        self.Destroy()



def main( param = None ):
    global app
    app = wx.PySimpleApp()
    frame = ActivityList(None,-1,"Activity List", param) 
    app.SetExitOnFrameDelete(True)
    app.MainLoop() 



if __name__ == "__main__":
    main()



