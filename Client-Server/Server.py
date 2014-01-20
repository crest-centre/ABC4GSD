#!/usr/bin/env python
# -*- coding: utf8 -*-

# Project       ABC4GSD Server
# file name:    Server.py
# Version:      1.1.0
#
# DESCRIPTION
# 
#
# Developer:
# Paolo Tell <pate@itu.dk>
#
# History/Start :
# creation: 24/mar/2011 - Paolo Tell
#
# License:
#         
#
# -*- coding: utf-8 -*-


import zmq, json
import sys
# import socket
# import thread
from datetime import datetime

import library
import library.constants as CO
import library.utils.utils as UT
from library.server.SrvHandler import SrvHandler 


class Logger:
	def __init__(self,logger):
		self.logger = logger
		self.buff = ""
	def write(self, string):
		if len(string)>0 and string[-1] == '\n':
			self.buff = 'Server>\t' + self.buff.strip()
			self.logger.send( self.buff )
			self.buff = ""
		self.buff += string
	
class ConnectionManager:
	def __init__( self, context, cfgFile ):
		cfg = UT.load_cfg( cfgFile )
		self.publisher = context.socket(zmq.PUB)
		self.publisher.bind("tcp://*:%s" % (cfg.get( 'General', 'serverport_pub' ),))
		self.replier = context.socket(zmq.XREP)
		self.replier.bind("tcp://*:%s" % (cfg.get( 'General', 'serverport_rep' ),))
		self.command = context.socket(zmq.XREP)
		self.command.bind("tcp://*:%s" % (cfg.get( 'General', 'serverport_cmd' ),))
		self.logger = context.socket(zmq.PUSH)
		self.logger.connect(cfg.get( 'General', 'serveraddr_logger' ))
		
	def read(self, channel):
		wip = channel.recv()
		while channel.getsockopt(zmq.RCVMORE):
			wip += channel.recv()
		return wip

	def terminate():
		self.logger.close()
		self.data.close()
		self.command.close()
		self.replier.close()
		self.publisher.close()

class DataManager:
	def __init__( self, context, cfgFile ):
		cfg = UT.load_cfg( cfgFile )
		self.data = context.socket(zmq.XREQ)
		self.data.connect(cfg.get( 'General', 'serveraddr_data' ))
	def query( self, q ):
		self.data.send(json.dumps({'q':q}))
		wip = self.data.recv_multipart()
		print wip
		wip = json.loads(wip[1])
		return wip
	def getMods( self ):
		return self.query('MODS')
	def terminate():
		self.data.close()

def main():
	context = zmq.Context()
	connection = ConnectionManager(context, 'Server.cfg')
	data = DataManager(context, 'Server.cfg')
	
	newLogger = Logger(connection.logger)
	sys.stdout = newLogger
	
	handler = SrvHandler(connection, data)
	handler.run()
	
	# Never get here but if so, this would be how it ends
	connection.terminate()
	context.term()



if __name__ == "__main__":
	main()



