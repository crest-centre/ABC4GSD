#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import zmq, json
import library.constants as CO
import library.data as DATA
import unicodedata


class SrvHandler():
	class UserHandler( object ):
		def __init__(self, id, name, addr):
			self.name = name
			self.id = id
			self.identity = addr
			self.connection = {}
			
	def __init__(self, connection, data):
		self.connectionManager = connection
		self.dataManager = data
		self._user = {}
		self._id = {}

	def run(self):
		poller = zmq.Poller()
		poller.register( self.connectionManager.replier, zmq.POLLIN )
		poller.register( self.connectionManager.command, zmq.POLLIN )

		while 1:
			socks = dict(poller.poll())
			if self.connectionManager.replier in socks and socks[self.connectionManager.replier] == zmq.POLLIN:
				data = self.connectionManager.replier.recv_multipart()
				print 'Replier ' + data.__repr__()
				q = json.loads(data[-1])
				q['a'] = self.dataManager.query(q['q'])['a']
				self._propagate()
				data[-1] = json.dumps(q)
				self.connectionManager.replier.send_multipart(data)
			if self.connectionManager.command in socks and socks[self.connectionManager.command] == zmq.POLLIN:
				data = self.connectionManager.command.recv_multipart()
				print 'Command ' + data.__repr__()
				[identity, data] = data
				q = json.loads(data)
				print data
				clearString = q['q']
				clearString = clearString.replace('\\u2026','...')
				clearString = unicodedata.normalize('NFKD',clearString).encode('ascii','ignore')
				q['a'] = self.handle(identity,clearString)
				data = json.dumps(q)
				self.connectionManager.command.send_multipart([identity,data])

	def handle(self, sender_addr, request):
		print request
		wip = request
		# CODE x FROM y <CMD> the_remainder
		wip = wip.split(' ', 5)
		code = long(wip[1])
		sender = wip[3]
		if sender == 'None':
			sender = None
		else:
			sender = long(sender)
		cmd = wip[4]
		rest =''
		if len(wip) == 6:
			rest = wip[5]
		response = None
		
		if cmd == 'CONNECT':
			# rest -> <model> USER <name>
			wip = rest.split(' ', 2)
			model = wip[0]
			name = wip[2]
			response = self.connect(sender_addr, model, name)
		elif cmd == 'DISCONNECT':
			# rest -> <model>
			model = rest
			response = self.disconnect(model, sender)
		elif cmd == 'QUERY':
			# rest -> <model> <query>
			if sender not in self._id.keys():
				return ( code, sender, ( False, 'User not recognized' ) )
			wip = rest.split(' ', 1)
			model = wip[0]
			query = wip[1]
			response = self.dataManager.query(query)['a']
			self._propagate()
		elif cmd == 'RUN':
			# rest -> <query>
			response = self.dataManager.query(rest)['a']
			self._propagate()
		else:
			response = (False, 'Command not recognized')
		return (code, sender, response)

	def connect(self, addr, model, name ):
		q = '%s' % (model,)
		resp = self.dataManager.query(q)
		if len(resp['e']):
			return  (False, 'Model not present' )
		id = False
		for x in self._id.keys():
			if name == self._id[x]:
				id = x
				break
		if not id:
			q = '%s.user.[].name.==.%s' % (model, self._box(name), )
			id = self.dataManager.query(q)['a']
			if isinstance(id, list):
				if len(id) == 0:
					id = None
				else:
					id = id[0]
			if id == None:
				id = self.dataManager.query('%s.user.+' % ( model, ))['a']
				self.dataManager.query('%s.user.%s.name.=.%s' % ( model, id, self._box(name) ))
			self._user[id] = self.UserHandler(id, name, addr)
			self._id[id] = name

		self._user[id].connection[model] = True
		return (True, str(self._user[id].id))

	def disconnect(self, model, id):
		if not id in self._id:
			return
		self._user[id].connection.pop( model )
		if not len(self._user[id].connection):
			self._user.pop(id)
			self._id.pop(id)
		return (True, '')

	def query(self, sender, model, q):
		if sender == None and model == None:
			return self.dataManager.query(q)['a'].__repr__()
		if sender not in self._id.keys():
			return
		return self.dataManager.query(q)['a']

	def _propagate(self):
		wip = self.dataManager.getMods()
		for x in wip['a']:
			print 'Publishing> '  + x.__repr__()
			self.connectionManager.publisher.send(json.dumps({'a':x}))

	"""
		utilities
	"""
	def _box(self, wip):
		return CO.Q_VALUE_DELIMITER[0] + wip + CO.Q_VALUE_DELIMITER[1]



