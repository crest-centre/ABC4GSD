import sys, os
import subprocess


def main():
	procs = []
	directory = os.path.join('library','data')

	procs.append( subprocess.Popen(["python", "Logger.py", "5541", "server.log"]) )
	procs.append( subprocess.Popen(["python", os.path.join(directory,"PersistencyInterface.py"), "5540", "tcp://localhost:5541"]) )
	procs.append( subprocess.Popen(["python", "Server.py"]) )
	
	print " ".join([str(x.pid) for x in procs])
	
if __name__ == "__main__":
	main()

