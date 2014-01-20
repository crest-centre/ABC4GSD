#!/usr/bin/env python
# -*- coding: utf8 -*-

# Project       0mq based Logger
# file name:    Logger.py
# Version:      1.0
#
# DESCRIPTION
#
# EXAMPLE: python Logger.py 5541 server.log
#
# Developer:
# Paolo Tell <pate@itu.dk>
#
# History/Start :
# creation: 30/jun/2011 - Paolo Tell
#
# License:
#         
#
# -*- coding: utf-8 -*-


import sys
import zmq
from datetime import datetime




class Logger:
	def __init__(self,filename):
		self.f = open(filename, 'a',  0)
		self.buffer = ""
	def write(self, string):
		self.buffer += string
	def flush(self):
		self.buffer = str(datetime.now()) + ' --> ' + self.buffer
		self.f.write(self.buffer)
		self.buffer = ""
	def close(self):
		self.f.close()
	
def main():
	
	logger = Logger(sys.argv[2])
	sys.stdout = logger
	
	context = zmq.Context()
	incoming = context.socket(zmq.PULL)	
	incoming.bind("tcp://*:%s" % (sys.argv[1],))
	
	while 1:
		print incoming.recv()
		more = incoming.getsockopt(zmq.RCVMORE)
		if not more:
			logger.flush()
	# We never get here...
	logger.close()
	incoming.close()
	context.term()


	
	
if __name__ == "__main__":
	if not len(sys.argv) == 3:
		print "Usage:\n\t%s <port> <output_file>" % (sys.argv[0],)
		print "\tExample: Logger.py 5541 server.log\n"
		sys.exit(1)
	main()

