#!/usr/bin/env python
# -*- coding: utf8 -*-

# Project       0mq based Forwarder
# file name:    Forwarder.py
# Version:      1.0
#
# DESCRIPTION
#
# EXAMPLE: python Forwarder.py tcp://localhost:5550 tcp://*:5560
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


import zmq
import sys



def main():
	context = zmq.Context()
	frontend = context.socket(zmq.XREP)
	backend = context.socket(zmq.XREQ)
	frontend.bind(sys.argv[2])
	backend.connect(sys.argv[1])

	zmq.device(zmq.QUEUE, frontend, backend)

	# We never get here...
	frontend.close()
	backend.close()
	context.term()
	
	
	
if __name__ == "__main__":
	if not len(sys.argv) == 3:
		print "Usage:\n\t%s <backend> <frontend>" % (sys.argv[0],)
		print "\tExample: Queue.py tcp://localhost:5551 tcp://*:5561"
		sys.exit(1)
	main()






