#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import thread
import zmq, json
import threading
from datetime import datetime as DT

import sys, os
sys.path.append(os.getcwd())
import library.constants as CO



class ConnectionManager(object):
	def __init__(self):
		self.close = False
		self.locking = 0
		self.context = zmq.Context()
		self.subscriber = self.context.socket( zmq.SUB )
		self.subscriber.connect( "tcp://localhost:5560" )
		self.subscriber.setsockopt(zmq.SUBSCRIBE, "")
		self.query = self.context.socket( zmq.REQ )
		self.query.connect( "tcp://localhost:5561" )
		self.event = self.context.socket( zmq.SUB )
		self.event.connect( "tcp://localhost:5562" )
		self.event.setsockopt(zmq.SUBSCRIBE, "")
		self.backend = self.context.socket( zmq.REQ )
		self.backend.connect( "tcp://localhost:5563" )
	def terminate(self):
		self.close = True
		while self.locking:
			continue
		self.subscriber.close()
		self.query.close()
		self.event.close()
		self.backend.close()
		self.context.term()



class ABCAppInterface(object):
	def __init__(self, name):
		self.__lock = threading.Lock()
		self._connectionManager = ConnectionManager()
		self._definedProperties = []
		self._id = -1
		self._defaultModel = 'abc'
		self._name = name
		self._userId = None
		self._appId = None
		self._actId = None
		self._propertyVal = {}
		self._propertyId = {}
		self._simpleSubscription = {}
		self._complexSubscription = {}
		msg = self.sendCommand( 'INIT %s' % (self._name,) )
		msg = msg.split()
		self._userId = long(msg[0])
		if len(msg)>1:
			self._appId = long(msg[1])
			self._actId = long(msg[2])
		
		threading.Thread(target=self._receive).start()

	def closeConnections( self ):
		self._connectionManager.terminate()

	def sendCommand( self, cmd ):
		self.__lock.acquire()
		self.send( self._connectionManager.backend, str(cmd) )
		tmp = self.recv( self._connectionManager.backend )
		self.__lock.release()
		return tmp

	def send( self, ch, msg, encryption=False):
		if encryption:
			msg = json.dumps({'q':msg})
		ch.send(msg)

	def recv( self, ch, encryption=False):
		msg = ch.recv()
		if encryption:
			msg = json.loads(msg)['a']
		return msg

	def query( self, q ):
		# print 'sending .. ' + q
		self.__lock.acquire()
		self.send( self._connectionManager.query, q, True )
		tmp = self.recv( self._connectionManager.query, True )
		self.__lock.release()
		# print 'received .. ' + tmp.__repr__()	
		return tmp

	def _receive(self):
		self._connectionManager.locking += 1
		poller = zmq.Poller()
		poller.register( self._connectionManager.subscriber, zmq.POLLIN )
		poller.register( self._connectionManager.event, zmq.POLLIN )
		while not self._connectionManager.close:
			socks = dict(poller.poll(1000))
			if self._connectionManager.subscriber in socks and socks[self._connectionManager.subscriber] == zmq.POLLIN:
				msg = self.recv( self._connectionManager.subscriber, True )
				# print "RECV " + msg
				self.handleMessage('SUB', msg)
			if self._connectionManager.event in socks and socks[self._connectionManager.event] == zmq.POLLIN:
				msg = self.recv( self._connectionManager.event )
				# print "RECV EVT " + msg
				self.handleMessage('EVT', msg)
		self._connectionManager.locking -= 1

	"""
	Activity interface
	"""
	def handleMessage( self, ch, msg ):
		#self.personalHandler( ch, msg )
		if ch == 'EVT':
			msg = msg.split()
			if msg[0] == 'CMD':
				if msg[1] == 'SUSPEND':
					if msg[2] == 'ALL':
						self.suspend()
					if msg[2] == 'ACT' and not self._name == 'activities':
						self.suspend()
				if msg[1] == 'INIT':
					pass
		if ch == 'SUB':
			# return
			self._checkSubscription( msg )

	def subscribe(self, q, method=None):
		data = q.split('.')
		expansion = self._getExpansionArray(data)
		if True not in expansion:
			self._simpleSubscription[q] = [q, method]
		else:
			self._complexSubscription['.'.join( data[:expansion.index(True)] )] = [q, method]

	def unsubscribe(self, listOfEvents=[], model=''):
		simpleSub = self._simpleSubscription.keys()
		complexSub = [self._complexSubscription[x][0] for x in self._complexSubscription.keys()]
		if not len(listOfEvents):
			listOfEvents = simpleSub + complexSub
		for wip in listOfEvents:
			if wip in simpleSub:
				del self._simpleSubscription[wip]
			else:
				for x in self._complexSubscription.keys():
					if wip in self._complexSubscription[x][0]:
						del self._complexSubscription[x]
						break

	def _getExpansionArray(self, data):
		return [ x[0]==CO.Q_EXPANSION_DELIMITER[0] for x in data]

	def _checkSubscription( self, msg ):
		for x in self._simpleSubscription.keys():
			if x in msg and msg.index(x) == 0:
				if self._simpleSubscription[x][1] != None:
					self._simpleSubscription[x][1]( msg )
		for x in self._complexSubscription.keys():
			if x in msg and msg.index(x) == 0:
				wip = self.query( '#+' + self._complexSubscription[x][0] )
				for y in wip:
					if y in msg and msg.index(y) == 0:
						if self._complexSubscription[x][1] != None:
							self._complexSubscription[x][1]( msg )
	
	def suspend(self):
		print 'suspending ' + self._name
		self.unsubscribe()
		self.suspendOperation()
		self.storeProperty()
		self.__lock.acquire()
		self.send( self._connectionManager.backend, 'ABC SUSPEND COMPLETED ' + self._name )
		self.recv( self._connectionManager.backend )
		self.__lock.release()
		self.closeConnections()
	
	def resume(self):
		self.loadProperty()
		self.resumeOperation()
		self.__lock.acquire()
		self.send( self._connectionManager.backend, 'ABC RESUME COMPLETED' )
		self.recv( self._connectionManager.backend )
		self.__lock.release()
	
	def killOperation(self):
		raise "Method not implemented"
	
	def suspendOperation(self):
		raise "Method not implemented"
	
	def resumeOperation(self):
		raise "Method not implemented"
	
	def personalHandler(self, ch, msg):
		raise "Method not implemented"
	
	def set(self, type, id, name, value, model=''):
		if not len(model):
			model = self._defaultModel
		q = '%s.%s.%s.%s.=.%s' % (model, type, id, name, value)
		# print q
		self.query(q)
		# self.query('QUERY %s %s' % (model, q, ))
	def get(self, type, id, name, model=''):
		if not len(model):
			model = self._defaultModel
		q = '%s.%s.%s.%s' % (model, type, id, name)
		return self.query(q)
		# return self.query('QUERY %s %s' % (model, q, ))
	def _getAttributesList(self, type, id, model=''):
		if not len(model):
			model = self._defaultModel
		# q =  'QUERY %s %s.%s.%s'  % (model, model, type, id)
		q =  '%s.%s.%s'  % (model, type, id)
		r = self.query(q)
		return r
	def _query(self, q, model=''):
		# print q
		if not len(model):
			model = self._defaultModel
		# resp = self.query('QUERY %s %s' % (model, q, ))
		resp = self.query(q)
		# print resp
		# if len(resp) > 1:
		# 	resp = ''.join(resp)
		# if len(resp) == 1:
		# 	resp = resp[0]
		# try:
		# 	resp = eval(resp)
		# except:
		# 	pass
		return resp
	def loadProperty(self):
		#application_state = property:str,value:str,name:str
		if self._appId == None:
			return
		q = 'abc.application_state.[].name.==.%s%s' % (self._userId, self._appId, )
		resp = self._query(q)
		if resp == None or not len(resp) or resp[0] == 'OtherError':
			resp = []
		self._propertyId = {}
		for x in resp:
			q = 'abc.application_state.{0}.property.&.abc.application_state.{0}.value'.format(x)
			resp = self._query(q)
			#resp = eval(resp[0])
			if len(resp) != 2:
				continue
			self._propertyVal[resp[0]] = resp[1]
			self._propertyId[resp[0]] = x
	def storeProperty(self):
		# print 'store properties "%s"' % (self._appId,)
		if self._appId == None:
			return
		q = []
		for p in self._propertyVal.keys():
			if p in self._propertyId.keys():
				q.append( 'abc.application_state.%s.value.=.%s'  % (self._propertyId[p], self._propertyVal[p] ))
			else:
				id = self._query('abc.application_state.+')
				q.append('abc.application_state.%s.user.+.%s' % (id, self._userId ) )
				q.append('abc.application_state.%s.application.+.%s' % (id, self._appId, ) )
				q.append('abc.application_state.%s.name.=.%s%s' % (id, self._userId, self._appId, ) )
				q.append('abc.application_state.%s.property.=.%s' % (id, p, ) )
				q.append('abc.application_state.%s.value.=.%s' % (id, self._propertyVal[p], ) )
		q = '.&.'.join(q)
		if len(q):
			self._query( q )
	def _defineProperties(self):
		for x in self._definedProperties:
			if x in self._propertyId.keys():
				continue
			try:
				q = []
				id = self._query('abc.application_state.+')
				id = long(id)
				q.append('abc.application_state.%s.user.+.%s' % (id, self._userId ) )
				q.append('abc.application_state.%s.application.+.%s' % (id, self._appId, ) )
				q.append('abc.application_state.%s.name.=.%s%s' % (id, self._userId, self._appId, ) )
				q.append('abc.application_state.%s.property.=.%s' % (id, x, ) )
				q.append('abc.application_state.%s.value.=.%s' % (id, '<UNDEF>', ) )
				q = '.&.'.join(q)
				if len(q):
					self._query( q )
				self._propertyVal[x] = '<UNDEF>'
				self._propertyId[x] = id
			except:
				pass
	def getProperty(self, name):
		if name in self._propertyVal.keys():
			x = self._propertyVal[name]
			if x == '<UNDEF>':
				raise
			return x
		self._defineProperties()
		raise
	def setProperty(self, name, val):
		self._propertyVal[name] = val





