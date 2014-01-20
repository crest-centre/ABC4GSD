#!/usr/bin/env python
# -*- coding: utf-8 -*-

import wx



class TextDialog(wx.Dialog):
    def __init__(self, parent, id, title, data):
        wx.Dialog.__init__(self, parent, id, title)
        
        vbox = wx.BoxSizer(wx.VERTICAL)        
        self.text = wx.TextCtrl(self, size = (1000, 700), style = wx.TE_MULTILINE|wx.VSCROLL)
        self.text.SetValue(data)
        vbox.Add(self.text, 1, wx.EXPAND)
        sizer =  self.CreateButtonSizer(wx.CANCEL|wx.OK)
        vbox.Add(sizer, 0, wx.CENTER)
        self.SetSizerAndFit(vbox)

    def GetValue(self):
        x = self.text.GetValue()
        x = x.split('\n')
        x = [ y for y in x if len(y) and y[0]!='#']
        return x



# class ListDialog(wx.Dialog):
#     def __init__(self, parent, id, title, data, type):
#         wx.Dialog.__init__(self, parent, id, title)
#         
#         self._value = [None, None, None]
#         self.response = []
#         self.selection = [-1, -1]
#         self.type = type
#         self.data = data
#         self.item = None
#         value = [x._name for y, x in self.data[self.type].items()]
# 
#         hbox = wx.BoxSizer(wx.HORIZONTAL)
#         self.list1 = wx.ListBox(self, choices=value)
#         hbox.Add(self.list1, 1, wx.EXPAND)
#         self.list2 = wx.ListBox(self, choices=[])
#         hbox.Add(self.list2, 1, wx.EXPAND)
#         
#         vbox = wx.BoxSizer(wx.VERTICAL)
#         vbox.Add(hbox, 1, wx.EXPAND)
#         
#         self.text = wx.TextCtrl(self)
#         vbox.Add(self.text, 0, wx.EXPAND)
#         
#         sizer =  self.CreateButtonSizer(wx.CANCEL|wx.OK)
#         vbox.Add(sizer, 0, wx.CENTER)
# 
#         
#         self.SetSizer(vbox)
#         self.Bind(wx.EVT_LISTBOX, self.OnClickList1, self.list1)
#         self.Bind(wx.EVT_LISTBOX, self.OnClickList2, self.list2)
#         self.text.Bind(wx.EVT_KILL_FOCUS, self.OnTextLostFocus)
# 
#     def OnClickList1(self, event):
#         self.selection[0] = event.GetSelection()
#         self.selection[1] = -1
#         tmp = self.list1.GetString(self.selection[0])
#         for y, x in self.data[self.type].items():
#             if x._name == tmp:
#                 self.item = x
#         self._value[0] = self.item._id
#         self._value[1] = None
#         self.list2.Clear()
#         value = []
#         for x in self.item.keys():
#             value.append(x)
#         for x in self.item._fields:
#             if len(x)>2 and x[0:2]!='__':
#                 value.append(x)
#         self.list2.Set(value)
# 
#     def OnClickList2(self, event):
#         if self.selection[0]==-1 or event.GetSelection()==-1:
#             return
#         self.selection[1] = event.GetSelection()
#         tmp = self.list2.GetString(self.selection[1])
#         self._value[1] = tmp
#         value = ''
#         if tmp in self.item._fields:
#             value = self.item.__getattribute__(tmp)
#         else:
#             value = self.data[self.type][tmp]
#         self.text.SetValue(str(value))
# 
#     def OnTextLostFocus(self, event):
#         self._value[2] = self.text.GetValue()
#         if None in self._value:
#             return
#         self._value[2] = self.text.GetValue()
#         self.response.append(self._value[:])
#         if self._value[1] in self.item._fields:
#             self.item.__setattr__(self._value[1], self._value[2])
#         else:
#             self.item[self._value[1]] = self._value[2]
# 
#     def GetValue(self):
#         if self._value[2] != self.text.GetValue():
#             self.OnTextLostFocus(None)
#         return self.response
# 
# 
# 
# class ActivityDialog(wx.Dialog):
#     def __init__(self, parent, id, title, data, key):
#         wx.Dialog.__init__(self, parent, id, title)
# 
#         self.key = key
#         self.data = data
#         self.attached = {'user':[], 'application':[]}
#         self.item = self.data['activity'][key]
#         self.attached['user'] = self.item.GetUsers()
#         self.attached['application'] = self.item.GetApplications()
# 
#         userSzr = wx.BoxSizer(wx.VERTICAL)
#         self.listUser = wx.ListBox(self, choices=[x._name for x in self.attached['user']])
#         btnUser = wx.BoxSizer(wx.HORIZONTAL)
#         btnUserAdd = wx.Button(self, -1, ' Add user ')
#         btnUserRemove = wx.Button(self, -1, ' Remove user ')
#         btnUser.Add(btnUserAdd, 0)
#         btnUser.Add(btnUserRemove, 0)
#         userSzr.Add(btnUser, 0, wx.CENTER)
#         userSzr.Add(self.listUser, 1, wx.EXPAND)
#         
#         applicationSzr = wx.BoxSizer(wx.VERTICAL)
#         self.listApplication = wx.ListBox(self, choices=[x._name for x in self.attached['application']])
#         btnApplication= wx.BoxSizer(wx.HORIZONTAL)
#         btnApplicationAdd = wx.Button(self, -1, ' Add application ')
#         btnApplicationRemove = wx.Button(self, -1, ' Remove application ')
#         btnApplication.Add(btnApplicationAdd, 0)
#         btnApplication.Add(btnApplicationRemove, 0)
#         applicationSzr.Add(btnApplication, 0, wx.CENTER)
#         applicationSzr.Add(self.listApplication, 1, wx.EXPAND)
#         
#         hbox = wx.BoxSizer(wx.HORIZONTAL)
#         hbox.Add( userSzr, 1, wx.EXPAND)
#         hbox.Add( applicationSzr, 1, wx.EXPAND)
#         
#         vbox = wx.BoxSizer(wx.VERTICAL)
#         vbox.Add(hbox, 1, wx.EXPAND)
#         sizer =  self.CreateButtonSizer(wx.CANCEL|wx.OK)
#         vbox.Add(sizer, 0, wx.CENTER)
# 
#         self.SetSizerAndFit(vbox)
#         
#         btnUserAdd.Bind(wx.EVT_BUTTON, self.OnUserAdd)
#         self.listUser.Bind(wx.EVT_RIGHT_UP, self.OnEvtRemoveUser)
#         btnUserRemove.Bind(wx.EVT_BUTTON, self.OnUserRemove)
#         btnApplicationAdd.Bind(wx.EVT_BUTTON, self.OnApplicationAdd)
#         btnApplicationRemove.Bind(wx.EVT_BUTTON, self.OnApplicationRemove)
#         self.listApplication.Bind(wx.EVT_RIGHT_UP, self.OnEvtRemoveApplication)
# 
#     def OnUserAdd(self, event):
#         items = [x for y, x in self.data['user'].items()]
#         lst = [x._name for x in items]
#         dlg = wx.MultiChoiceDialog(self, 'Add users', 'Select users to add', lst)
#         if dlg.ShowModal() == wx.ID_OK:
#             selections = dlg.GetSelections()
#             strings = [lst[x] for x in selections]
#             for x in strings:
#                 if x not in self.listUser.Items:
#                     self.listUser.Append( x )
#         dlg.Destroy()
# 
#     def OnUserRemove(self, event):
#         lst = self.listUser.Items
#         dlg = wx.MultiChoiceDialog(self, 'Remove users', 'Select users to remove', lst)
#         if dlg.ShowModal() == wx.ID_OK:
#             for x in dlg.GetSelections():
#                 self.listUser.Delete( x )
#         dlg.Destroy()
# 
#     def OnEvtRemoveUser(self, event):
#         self.listUser.Delete(self.listUser.GetSelection())
# 
#     def OnApplicationAdd(self, event):
#         items = [x for y, x in self.data['application'].items()]
#         lst = [x._name for x in items]
#         dlg = wx.MultiChoiceDialog(self, 'Add applications', 'Select applications to add', lst)
#         if dlg.ShowModal() == wx.ID_OK:
#             selections = dlg.GetSelections()
#             strings = [lst[x] for x in selections]
#             for x in strings:
#                 if x not in self.listApplication.Items:
#                     self.listApplication.Append( x )
#         dlg.Destroy()
# 
#     def OnApplicationRemove(self, event):
#         lst = self.listApplication.Items
#         dlg = wx.MultiChoiceDialog(self, 'Remove applications', 'Select applications to remove', lst)
#         if dlg.ShowModal() == wx.ID_OK:
#             for x in dlg.GetSelections():
#                 self.listApplication.Delete( x )
#         dlg.Destroy()
# 
#     def OnEvtRemoveApplication(self, event):
#         self.listApplication.Delete(self.listApplication.GetSelection())
# 
#     def GetUsers(self):
#         items = [x for y, x in self.data['user'].items()]
#         toPick = self.listUser.Items
#         result = [x._id for x in items if x._name in toPick]
#         return result
#     def GetApplications(self):
#         items = [x for y, x in self.data['application'].items()]
#         toPick = self.listApplication.Items
#         result = [x._id for x in items if x._name in toPick]
#         return result
# 
# 
# 
# class ApplicationDialog(wx.Dialog):
#     def __init__(self, parent, id, title, data, key):
#         wx.Dialog.__init__(self, parent, id, title)
# 
#         self.key = key
#         self.data = data
#         self.item = self.data['application'][key]
#         self.asset = self.item.GetAsset()
# 
#         assetSzr = wx.BoxSizer(wx.VERTICAL)
#         self.listAsset = wx.ListBox(self, choices=[x._name for x in self.asset])
#         btnAsset = wx.BoxSizer(wx.HORIZONTAL)
#         btnAssetAdd = wx.Button(self, -1, ' Add artifact ')
#         btnAssetRemove = wx.Button(self, -1, ' Remove artifact ')
#         btnAsset.Add(btnAssetAdd, 0)
#         btnAsset.Add(btnAssetRemove, 0)
#         assetSzr.Add(btnAsset, 0, wx.CENTER)
#         assetSzr.Add(self.listAsset, 1, wx.EXPAND)
#         
#         vbox = wx.BoxSizer(wx.VERTICAL)
#         vbox.Add( assetSzr, 1, wx.EXPAND)
#         sizer =  self.CreateButtonSizer(wx.CANCEL|wx.OK)
#         vbox.Add(sizer, 0, wx.CENTER)
# 
#         self.SetSizerAndFit(vbox)
#         
#         btnAssetAdd.Bind(wx.EVT_BUTTON, self.OnAssetAdd)
#         self.listAsset.Bind(wx.EVT_RIGHT_UP, self.OnEvtRemoveAsset)
#         btnAssetRemove.Bind(wx.EVT_BUTTON, self.OnAssetRemove)
# 
#     def OnAssetAdd(self, event):
#         items = [x for y, x in self.data['artifact'].items()]
#         lst = [x._name for x in items]
#         dlg = wx.MultiChoiceDialog(self, 'Add asset', 'Select asset to add', lst)
#         if dlg.ShowModal() == wx.ID_OK:
#             selections = dlg.GetSelections()
#             strings = [lst[x] for x in selections]
#             for x in strings:
#                 if x not in self.listAsset.Items:
#                     self.listAsset.Append( x )
#         dlg.Destroy()
# 
#     def OnAssetRemove(self, event):
#         lst = self.listAsset.Items
#         dlg = wx.MultiChoiceDialog(self, 'Remove assets', 'Select assets to remove', lst)
#         if dlg.ShowModal() == wx.ID_OK:
#             for x in dlg.GetSelections():
#                 self.listAsset.Delete( x )
#         dlg.Destroy()
# 
#     def OnEvtRemoveAsset(self, event):
#         self.listAsset.Delete(self.listAsset.GetSelection())
# 
#     def GetAsset(self):
#         items = [x for y, x in self.data['artifact'].items()]
#         toPick = self.listAsset.Items
#         result = [x._id for x in items if x._name in toPick]
#         return result
