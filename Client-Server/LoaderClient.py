import sys
import os
import subprocess


def main():
	procs = []
	directory = os.path.join('library','client')

	procs.append( subprocess.Popen(["python", "Logger.py", "5566", "client.log"]) )
	procs.append( subprocess.Popen(["python", os.path.join(directory,"Forwarder.py"), "tcp://localhost:5550", "tcp://*:5560"]) )
	procs.append( subprocess.Popen(["python", os.path.join(directory,"Queue.py"), "tcp://localhost:5551", "tcp://*:5561"]) )
	
	print " ".join([str(x.pid) for x in procs])
	
if __name__ == "__main__":
	main()

