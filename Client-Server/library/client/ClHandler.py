#!/usr/bin/env python
# -*- coding: utf-8 -*-


#$ touch .git/git-daemon-export-ok
#$ git config daemon.receivepack true


import socket
from threading import Thread
import os
import copy
import time
import subprocess
import json, zmq

import library.utils.utils as UT
import library.constants as CO



class ClABCHandler(object):
	def __init__(self, connection):
		self.connectionManager = connection
		Thread(target=self.__receive).start()

		self._model = []
		#? needed ?

		self._confirmation = 0
		self._checkConfirmation = None
		self._frames = {}
		self._actApp = []
		self._appId = {}
		self._subscribtion = {}
		self._currAct = None
		self._me = None # user id
		# self._process = []

	#
	# Connection functions
	#
	def __receive(self):
		sock = self.connectionManager.control
		self.connectionManager.locking += 1
		poller = zmq.Poller()
		poller.register(sock, zmq.POLLIN)
		while not self.connectionManager.close:
			socks = dict(poller.poll(1000))
			if sock in socks and socks[sock] == zmq.POLLIN:
				data = sock.recv()
				print 'Received> '+ data
				resp = self._handleLocalRequest( data )
				# print 'Sending> '+ resp
				sock.send( resp )
		self.connectionManager.locking -= 1


	#
	# Applications communication 
	#
	def suspend(self, all=False):
		# send suspend to processes
		msg = 'CMD SUSPEND '
		if all:
			tmp = [self._frames[x][0] for x in self._frames if isinstance(self._frames[x], list)]
			msg += 'ALL'
		else:
			tmp = [self._frames[x][0] for x in self._frames if (isinstance(self._frames[x], list) and not x == 'activities')]
			msg += 'ACT'
		self._confirmation = len(tmp)
		print self._confirmation
		self.connectionManager.publisher.send( msg )
		# while self._confirmation:
		# 	continue
		time.sleep(1)
		self.killApplications(tmp)

	def _handleLocalRequest(self, wip):
		resp = ''
		msg = wip.split(' ', 1)
		if (msg[0]=='ABC'):
			tmp = msg[1].split(' ')
			if tmp[0]=='RESUME' and tmp[1]=='COMPLETED':
				self._confirmation -= 1
			if tmp[0]=='SUSPEND' and tmp[1]=='COMPLETED':
				self._confirmation -= 1
				# TODO> check ... should not be here
				# self.killApplications( [self._frames[tmp[2]][0]] )
			return resp
		if (msg[0]=='INIT'):
			resp = self._initApplication(msg[1])
		if (msg[0]=='RESUME'):
			self._resumeActivity(long(msg[1]))
		if (msg[0]=='SUSPEND'):
			self._suspendActivity(long(msg[1]))
		if (msg[0]=='QUERY'):
			model = msg[1].split(' ', 1)[0]
			q = msg[1].split(' ', 1)[1]
			resp = self._query(wip, sender)
		return resp

	def _query(self, msg, sender=None):
		sent = self._send(msg)
		recv = self._receive()
		return recv[1][2]

	def query(self, query, model='abc'):
		q = 'QUERY %s %s' % (model, query, )
		return self._query(q)

	def _resumeActivity(self, actId):
		# take all application 
		# launch them by sending also uid and act id
		if self._checkConfirmation != None:
			print "Still up - %s" % (self._confirmation,) 
			self._checkConfirmation[0](self._checkConfirmation[1])
			self._checkConfirmation = None

		q = 'abc.activity.%s.application' % (actId, )
		resp = self.query( q )
		if not isinstance(resp, list):
			return
		print resp
		self._currAct = actId
		self._actApp = []
		for x in resp:
			fields = ['name','command','file_name','folder']
			param = []
			for y in fields:
				q = 'abc.application.%s.%s' % (x, y, )
				param.append(self.query( q ))
			self._actApp.append(param[0])
			self._execute(param[0],param[1], [ os.path.join(param[3], param[2]) ], x )

	def _suspendActivity(self, actId):
		self.suspend()


	#
	# Applications execution functions
	#
	def killApplications(self, pids=[]):
		if not len(pids):
			pids = [self._frames[x][0] for x in self._frames if isinstance(self._frames[x], list)]
		print 'killing' + str([x.pid for x in pids ])
		for x in pids:
			# if os.path.exists( '/proc/%d'% (x.pid, ) ):
			# 	print x.pid
			# 	os.kill(x.pid, 9)
			x.kill()

	def _execute(self, name, program, argument, id=None):
		argument.insert( 0, program )
		print '....................................' + ' '.join(x for x in argument)
		if program == "python":
			wip = subprocess.Popen( argument )
		else:
			wip = subprocess.Popen( argument,shell=True )
		self._frames[name] = [ wip, [] ]
		if id != None:
			self._appId[name] = id
		# DO NOT REMOVE ... MAGIC
		#time.sleep(1)

	def _initApplication(self, name):
		# u_id self._appId[user] currAct
		param = [self._me]
		if name in self._appId:
			param.append(self._appId[name])
			param.append(self._currAct)
		return ' '.join(str(x) for x in param)

	#
	# Functions to handle server
	#
	def connect(self, name, model):
		if model in self._model:
			return
		msg = 'CONNECT %s USER %s' % (model, name, )
		sent = self._send(msg)
		resp = self._receive()
		print resp.__repr__()
		if resp[1][2][0]:
			self._me = long(resp[1][2][1])
			# TODO > check if really a number  ... resp[1][2][0] should be false ... resp[1][2][1]
			dirTmp = UT.getTmpDir()
			msg = 'QUERY %s abc.user.%s.tmp_dir.=.{{%s}}' % (model, self._me, dirTmp, )
			self._query(msg, self._me)
			print 'Connected to ' + model
			self._model.append(model)
			msg = 'QUERY %s abc.user.%s.state.=.%s' % (model, self._me, CO.user_CONNECTED, )
			self._query(msg, self._me)
		return (sent, resp)
	def disconnect(self, model):
		# rest -> <model>
		if model not in self._model:
			return
		msg = 'QUERY %s abc.user.%s.state.=.%s' % (model, self._me, CO.user_DISCONNECTED, )
		self._query(msg, self._me)
		msg = 'DISCONNECT %s' % (model, )
		sent = self._send(msg)
		resp = self._receive()
		if resp[1][2][0]:
			print 'Disconnected from ' + model
			del self._model[ self._model.index(model) ]
		return (sent, resp)
	def _send(self, msg):
		code = UT.getRandomId()
		msg = 'CODE %s FROM %s %s' % (code, self._me, msg)
		self.connectionManager.backend.send( json.dumps( {'q':msg} ) )
	def _receive(self):
		data = self.connectionManager.backend.recv()
		data = json.loads( data )
		return ( 'Old', data['a'] )
	def run(self, wip):
		msg = 'RUN %s' % (wip, )
		sent = self._send(msg)
		resp = self._receive()
		return (sent, resp)



