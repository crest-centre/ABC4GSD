#!/usr/bin/env python
# -*- coding: utf-8 -*-

import zmq, json
from datetime import datetime as DT

import sys, os, traceback
sys.path.append(os.getcwd())
from library.data.PersistencyManager import PersistencyManager
from library.constants.co_data import *
from library.utils.utils import encrypt, decrypt
import re



class PersistencyInterface( object ):
	def __init__( self ):
		self._data = PersistencyManager()
		self._mods = []
		self.lastGeneratedId = {}



	def query(self, wip):
		try:
			if wip == 'MODS':
				tmp = self._mods
				self._mods = []
				return tmp
			return self._query(wip, wip)
		# except EntityError as e:
		# 	print 'Error in method\n\tQuery: ' + wip + '\n\tMethod: ' + e.method + '\n\tMessage: ' + e.msg
		# 	raise DataLayerError('Error in the DataLayer. Query: ' + wip)
		# except ModelError as e:
		# 	print 'Error in method\n\tQuery: ' + wip + '\n\tMethod: ' + e.method + '\n\tMessage: ' + e.msg
		# 	raise DataLayerError('Error in the DataLayer. Query: ' + wip)
		except KeyError:
			print 'Unknown KeyError'
			raise DataLayerError('Error in the DataLayer. Query: ' + wip)
		except:
			print 'Unknown OtherError'
			print '-'*60
			traceback.print_exc(file=sys.stdout)
			print '-'*60
			e = 'DataLayer: Unknown error'
			raise DataLayerError('Error in the DataLayer. Query: ' + wip)


	def _query(self, wip, original):
		# print '--> %s -> %s' % (original.__repr__(), wip.__repr__(), )
		resp = []
		
		# Handling of multiple commands
		# example: '<expr>.&.<expr>'
		wip = wip.split('.&.')
		if len(wip)>1:
			resp = [self._query(x, original) for x in wip]
			resp = [x for x in resp if x != None]
			return resp
		wip = wip[0]
		
		# Handling request for schemas names
		# example: ''
		# return [schema names]
		if len(wip) == 0:
			return self._data.getInfo()
		
		# Handling insertion of new schema
		# example '+.fileName'
		if len(wip) and wip[0] == '+':
			self._data.addModel(wip[2:])
			self._mods.append(wip)
			return

		# Handling removal of existing schema
		# example '-.modelName'
		if len(wip) and wip[0] == '-':
			self._data.removeModel(wip[2:])
			self._mods.append(wip)
			return
		
		# Handling ids requests
		# example '!.abc.ecology'
		if len(wip) and wip[0] == '!':
			return self.lastGeneratedId[wip[2:]]

		# Handling subscription
		#    resending query with no subscribtion token
		if Q_SUBSCRIBE in wip:
			return self._query(wip[len(Q_SUBSCRIBE):], original)
		
		# Handling basic commands
		else:
			data = wip.split('.')
			
			# Return if some issues arose in the computations
			# Need to return None for the '.&.' to work
			if 'None' in data:
				return None
			
			self._checkConsistency( data )
			expansion = self._getExpansionArray(data)
			
			# Branch executed if query contains intra queries
			if True in expansion:
				idx = expansion.index(True)
				before = '.'.join( data[:idx] )
				after = '.'.join( data[idx+1:] )
				if len(before):
					before += '.'
				if len(after):
					after = '.'+ after
				# example: '<expr1>[]<expr>'
				# becomes: '<expr1>[eval(expr1)]<expr>'
				if data[idx] == '[]':
					expanded = self._query( before[:-1], wip )
					# if original[0] == '#' and exapnded == '[]':
					#     exapnded = None
					return self._query(  before + str(expanded) + after, original )
				else:
					# just to be faster ... check the case of concrete inner query to avoid the try ... except and eval ...
					# same as except branch
					if '.' in data[idx]:
						if ']:[' in data[idx]:
							tokens = data[idx][1:-1].split(':')
							expanded = ''
							for tok in tokens:
								if tok[-1] == ']': tok = tok[:-1]
								if tok[0] == '[': tok = tok[1:]
								tmp = self._query(tok, wip)
								if isinstance(tmp,list):
									# print '<><>' +str(tmp)
									tmp = tmp[0]
								expanded += str(tmp)
								expanded += ':'
							return self._query( before + str(expanded[:-1]) + after, original )
						expanded = self._query( data[idx][1:-1], wip )
						# if not len(expanded):
						# 	return expanded
						return self._query(  before + str(expanded) + after, original )
					try:
						# example: '<expr>[id1, ... ,idN]<expr>'
						# becomes: '<expr>.id1.<expr>.&. ... .&.<expr>.idN.<expr>' <-- Handled by the previous if
						# in case of strange errors during debug uncomment the following line 
						# wip = long(data[idx][1:-1].split(',')[0])
						wip = eval(data[idx])
						if not isinstance(wip, list):
							raise
						return self._query( '.&.'.join( before + str(x) + after for x in wip), original )
					except:
						# example: '<expr>[<expr1>]<expr>'
						# becomes: '<expr>[eval(expr1)]<expr>'
						expanded = self._query( data[idx][1:-1], wip )
						# if not len(expanded):
						# 	return expanded
						return self._query(  before + str(expanded) + after, original )
			else:
				# In case no expansions need to be evaluated, terminate the evaluation of the query in the case of a subscribtion
				if Q_SUBSCRIBE in original:
					return wip

			# Handle request of entities name
			# example: 'abc'
			# return [entity names]
			if len(data) == 1:
				return self._data.getInfo(data[0])

			# Handle request of ids present for an entity
			# example: 'abc.activity'
			# return [ids]
			if len(data) == 2:
				return self._data.getInfo(data[0], data[1])

			# Handle request of fields/relations names or the insertion of a new entity entry
			# example: 'abc.activity.123' or 'abc.activity.+'
			# return [fields + refs] or id 
			if len(data) == 3:
				if data[2] == '+':
					self._mods.append(wip)
					currId = self._data.add(data[0], data[1])
					self.lastGeneratedId[data[0]+'.'+data[1]] = currId
					return currId
				else:
					return self._data.getInfo(data[0], data[1], long(data[2]))

			# Handle the request of a concrete field/relation or the removal of an entity entry
			# example: 'abc.activity.123.name' or 'abc.activity.123.user' or 'abc.activity.-.123'
			# return val or return [ids] or nothing
			if len(data) == 4:
				if data[2] == '-':
					self._data.remove(data[0], data[1], long(data[3]))
					self._mods.append(wip)
					return
				return self._data.getInfo(data[0], data[1], long(data[2]), data[3])

			# Handle the request for field info
			# example: 'abc.activity.123.name.?'
			# return val or return [field info]
			if len(data) == 5:
				return self._data.getFieldInfo(data[0], data[1], long(data[2]), data[3])
			

			# Handle the assignment of a value to a field or the insertion/removal of a relation as well as the comparison of a value
			# example: 'abc.activity.123.name.=.val' or 'abc.activity.123.user.+.val' or
			#          'abc.activity.123.user.-.val' or' abc.application_state.[].user.==.val'
			# no return 
			if len(data) == 6:
				if data[4] == '=':
					self._data.update(data[0], data[1], long(data[2]), data[3], self._cleanValue(data[5]))
					self._mods.append(wip)
				elif data[4] == '+':
					self._data.ref(data[0], data[1], long(data[2]), data[3], long(data[5]))
					self._mods.append(wip)
				elif data[4] == '-':
					self._data.deref(data[0], data[1], long(data[2]), data[3], long(data[5]))
					self._mods.append(wip)
				# TODO > Check == is it a comparison?
				elif data[4] == '==':
					try:
						wip = self._data.get(data[0], data[1], long(data[2]), data[3]) 
					# TODO > CHECK might be a problem
					except EntityError:
						return None
					except ModelError:
						return None
					resp = False
					tmp = self._cleanValue(data[5])
					try:
						tmp = long(tmp)
						wip = long(wip)
					except:
						pass
					if isinstance(wip, list):
						resp = tmp in wip
					else:
						resp = wip == tmp
					return long(data[2]) if resp else None
				elif data[4] == '~=':
					# >>> a = ['12345:67812345:678','123454:67812345:678','12345:67812345:000']
					# >>> check = '(12345):[0-9]*:[0-9]*'
					# >>> list( x.group() for x in list( re.search(check,y) for y in a ) if x != None )
					# ['12345:67812345:678', '12345:67812345:000']
					# abc.ecology.[].name.~=.{{(12345):[0-9]*:[0-9]*}}
					regexp = self._cleanValue(data[5])
					# check = self._data.get(data[0], data[1], long(data[2]), data[3])
					check = self._query( '.'.join(data[:4]), original )
					ret = check if re.search(regexp,check) else None
					return ret if not ret else self._query( '.'.join(data[:4]) + '.==.' + ret, original )
				return



	# Method to remove delimiters, default: value delimiter
	def _cleanValue(self, val, delimiter=Q_VALUE_DELIMITER):
		tmp = val
		if tmp in Q_PLACE_HOLDERS:
			print '>> changing' + val + ' - ' + Q_PLACE_HOLDERS[0]
			if tmp == Q_PLACE_HOLDERS[0]:
				return str(DT.now())
		if delimiter[0] in tmp:
			tmp = tmp[len(delimiter[0]):]
		if delimiter[1] in tmp:
			tmp = tmp[:-len(delimiter[1])]
		return tmp



	# Method to control parenthesis and value delimiter consistency 
	def _checkConsistency(self, data):
		toCheck = [Q_EXPANSION_DELIMITER, Q_VALUE_DELIMITER]
		currentIdx = 0
		
		while currentIdx < len(toCheck):
			modified = False
			delim = toCheck[currentIdx]
			#expA = [ len(x)>=len(delim[0]) and x[0:len(delim[0])]==delim[0] for x in data]
			#expB = [ len(x)>=len(delim[1]) and x[-len(delim[1]):]==delim[1]  for x in data]
			r = []
			for x in data:
				r.append(0)
				if len(x)<len(delim[0]):
					continue
				while r[-1]<len(data[len(r)-1]) and data[len(r)-1][r[-1]:r[-1]+len(delim[0])] == delim[0]:
					r[-1] += 1
			expA = r
			r = []
			for x in data:
				r.append(0)
				if len(x)<len(delim[1]):
					continue
				while r[-1]<len(data[len(r)-1]):
					check = False
					if not r[-1]:
						check = data[len(r)-1][-(len(delim[1])+r[-1]):] == delim[1]
					else:
						check = data[len(r)-1][-(len(delim[1])+r[-1]):-(r[-1])] == delim[1]
					if not check:
						break
					r[-1] += 1
			expB = r			

			idx = 0
			other = 0

			for i, x in enumerate(expA):
				other += expA[i]
				other -= expB[i]
				if expA[i]!=expB[i]:
					idx = i
					break
			# print expA
			# print expB
			# print other
			# print data
			if idx and other > 0:
				modified = True
				while other:
					data[i] += '.'+data[i+1]
					del data[i+1]
					idx += 1
					other += expA[idx]
					other -= expB[idx]

			if not modified:
				currentIdx += 1


	# Returns a machine readable representation of the query with information about the possible inter queries
	def _getExpansionArray(self, data):
		return [ x[0]==Q_EXPANSION_DELIMITER[0] and x[-1]==Q_EXPANSION_DELIMITER[1] for x in data]






class DataLayerError(Exception):
	def __init__(self, query):
		self.query = query
	def __str__(self):
		return repr(self.query)






def __main__():
	# Create persistency layer
	dataLayer = PersistencyInterface()
	
	# Create communication infrastructure by attaching the logger and opening a port for external availability
	context = zmq.Context()
	socket = context.socket(zmq.XREP)
	socket.bind("tcp://*:%s" % (sys.argv[1],))
	logger = context.socket(zmq.PUSH)
	logger.connect(sys.argv[2])

	# Main loop to handle requests
	# Requests are json messages { 'q': <request> }
	#    to which a reply will be attached containing also an error field { 'q': <request>, 'a': <reply>, 'e': <error> }
	# Error field will be empty in case of successfull request, containing the error message otherwise
	while True:
		message = socket.recv_multipart()
		# print message
		identity = message[0]
		a = []
		e = ''
		q = ''
		if not identity:
			break          #  Interrupted
		try:
			wip = decrypt( message[1] )
			wip = json.loads( wip )
			q = wip['q']
			logger.send( 'DataLayer> Query>\t%s' % (str(q), ) )
			a = dataLayer.query( q )
			if not isinstance( a, list ) and not a == None:
				a = [a]
		except DataLayerError as err:
			e = err.query
		except:
			print '-'*60
			traceback.print_exc(file=sys.stdout)
			print '-'*60
			e = 'DataLayer: Unknown error'
		finally:
			wip['a'] = a
			wip['e'] = e			
		wip = json.dumps(wip)
		wip = encrypt( wip )
		socket.send_multipart( [identity,identity,wip] )
		logger.send( 'DataLayer> Reply>\t%s' % (str(a), ) )
		if len(e):
			logger.send( 'DataLayer> Error>\t%s' % (str(e), ) )



def __smoothExit__():
	print "Usage:\n\t%s <port> <logger_addr>" % (sys.argv[0],)
	print "\tExample: %s 5540 tcp://localhost:5541\n" % (sys.argv[0],)
	sys.exit(1)



if __name__ == "__main__":
	if not len(sys.argv) == 3: __smoothExit__()
	__main__()








