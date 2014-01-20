#!/usr/bin/env python
# -*- coding: utf-8 -*-

import wx

import sys, os
sys.path.append(os.getcwd())
import library.constants as CO
from library.utils import utils as UT
from library.client.ABCAppInterface import ABCAppInterface
import commands
import os
import time



class ArtifactList(wx.Frame, ABCAppInterface):
    def __init__(self, parent, id,  title, param): 
        wx.Frame.__init__(self, parent, id,  title)
        ABCAppInterface.__init__(self, 'ArtifactList')
        
        self.dirTmp = None
        self.repo = None
        self.__order = [ ('ID', '_id'), ('Name', 'name'), ('Dir', 'location'), ('Status', 'state'),  ]
        self.__conversion = {}
        #self.__conversion['state'] = {CO.activity_UNKNOWN:'Unknown', CO.activity_RESUMED:'Resumed', CO.activity_SUSPENDED:'Suspended', CO.activity_INITIALIZED:'Initialized'}
        self.__obj_content = {}
        self.__obj_property = {}
        self.__obj_content["list"] = [ "ListCtrl",  wx.ListCtrl(self, style=wx.LC_REPORT 
                                                                        | wx.BORDER_NONE
                                                                        | wx.LC_EDIT_LABELS
                                                                        | wx.LC_SORT_ASCENDING) ]
        for i, x in enumerate(self.__order):
            self.__obj_content["list"][1].InsertColumn(i, x[0])

        self.CreateStatusBar()

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
        self.setProperty('pos_x', self.GetPosition()[0])
        self.setProperty('pos_y', self.GetPosition()[1])
        self.setProperty('dim_x', self.GetSize()[0])
        self.setProperty('dim_y', self.GetSize()[1])
        # time.sleep(1)
        self._push()
    
    def resumeOperation(self):
        try:
            self.SetPosition( (int(self.getProperty('pos_x')), int(self.getProperty('pos_y'))) )
            self.SetSize( (int(self.getProperty('dim_x')), int(self.getProperty('dim_y'))) )
        except:
            pass
        self.deleteAllItems(True)
        self.refreshArtifacts()
        self.setArtifactList()

        self.repo = self._query('abc.activity.%s.repo' % (self._actId, ))
        if self.repo == None:
            return
        self.dirTmp = self._query('abc.user.%s.tmp_dir' % (self._userId, ))
        if self.dirTmp == None:
            return
        self._getRemoteFiles()
        #self.subscribe('abc.activity.%s.artifact.+'%(self._actId, ), self.artifactAdded )

    def _getRemoteFiles(self):
        commands.getoutput('rm -rf %s' % self.dirTmp)
        commands.getoutput('mkdir %s' % self.dirTmp)
        c_dir = os.getcwd()
        os.chdir(self.dirTmp)
        commands.getoutput('git clone %s' % (self.repo, ) )
        from urlparse import urlparse
        tmp = urlparse(self.repo).path
        tmp = os.path.split(tmp)[-1]
        cmd = 'mv %s ./' % (os.path.join(tmp, '*'), )
        commands.getoutput( cmd )
        cmd = 'mv %s ./' % (os.path.join(tmp, '.git'), )
        commands.getoutput( cmd )
        commands.getoutput('rm -rf %s' % tmp)
        os.chdir(c_dir)
    
    def _push(self):
        c_dir = os.getcwd()
        os.chdir(self.dirTmp)
        commands.getoutput('git commit -m aaa -a')
        print commands.getoutput('git push origin master' )
        os.chdir(c_dir)

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
                

    def refreshArtifacts(self):
        resp = self._query('abc.activity.%s.artifact'%(self._actId, ))
        if isinstance(resp, long):
            resp = [resp]
        elif isinstance(resp, str):
            resp = eval(resp)
        for id in resp:
            if id in self.__obj_property.keys():
                del self.__obj_property[id]
            self.__obj_property[id] = [id]
            for y in self.__order[1:]:
                self.__obj_property[id].append( self._query('abc.artifact.%s.%s'%(id, y[1])) )

    def deleteAllItems(self, content = False):
        self.__obj_content["list"][1].DeleteAllItems()
        if content:
            self.__obj_property = {}

    def setArtifactList(self):
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
                    self.__obj_content["list"][1].SetStringItem(i, l, str(val))


    def artifactAdded(self, wip):
        self.deleteAllItems(True)
        self.refreshContacts()
        self.setContactList()

    def onSelect(self, event):
        current = event.m_itemIndex
        self._selection = current
        id = self.__obj_content["list"] [1].GetItemText(current)
        self._query('abc.activity_state.[abc.activity_state.[abc.activity_state.[].activity.==.%s].user.==.%s].artifact.+.%s'%(self._actId, self._userId, id, ) )
        time.sleep(1)
        self._push()
        self._getRemoteFiles()

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
    frame = ArtifactList(None,-1,"Artifact List", param) 
    app.SetExitOnFrameDelete(True)
    app.MainLoop() 



if __name__ == "__main__":
    main()



