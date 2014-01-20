package itu.abc4gsd.rcp.client_v6.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

public class Utils {
	public static long getRandomId() {
		return System.currentTimeMillis();
	}
	
	public static String getTmpDir() { return getTmpDir( "ABC_" ); }
	public static String getTmpDir( String p ) {
		File dirBase = new File( System.getProperty("java.io.tmpdir") );
		File dir = new File( dirBase, p+getRandomId() );
		System.out.println("<><><><><><><><><><><><><><><><><><><><><><>" + dir.getPath());
		return dir.getPath();
	}

	public static String getBasePath() {
		return Platform.getInstallLocation().getURL().getPath();
	}
	
	public static void reverse(Object[] a) {
	    int l = a.length;
	    for (int j = 0; j < l / 2; j++) {
	        Object temp = a[j];
	        a[j] = a[l - j - 1];
	        a[l - j - 1] = temp;
	    }
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	public static String[] terminateWindowCommand( String program, String title ) {
//		wmctrl -c "__init__.py"
//		osascript -e "tell application \"TextMate\" to close (every window whose name is \"__init__.pyc\")"
//		tell application "Safari" to close (every tab of window 1 whose name is "amar center irma - Google Search")
		String[] replace = new String[] {};
		String[] replacement = new String[] {};
		for( int x=0;x<replace.length;x++ ) {
			program.replaceAll(replace[x], replacement[x]);
			title.replaceAll(replace[x], replacement[x]);
		}

		if( isMac() )
			return new String[] {"osascript", "-e", "tell application \"" + program + "\" to close (every window whose name contains \"" + title + "\")"};
		if( isUnix() )
			return new String[] {"wmctrl -c \"" + title + "\""};
		return new String[]{""};
	}
	// to add basic applescript stuff
	// sudo defaults write /Applications/LibreOffice.app/Contents/Info NSAppleScriptEnabled -bool true
	// sudo chmod 644 /Applications/LibreOffice.app/Contents/Info.plist
	// sudo codesign -f -s - /Applications/LibreOffice.app
	
	// ignoring application responses
	// .. end ignoring
	
	public static boolean isWindows() { return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0); }
	public static boolean isMac() { return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0); }
	public static boolean isUnix() { return (System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0 || System.getProperty("os.name").toLowerCase().indexOf("nux") >= 0); }
	public static boolean isSolaris() { return (System.getProperty("os.name").toLowerCase().indexOf("sunos") >= 0); }
	
	public static String[] terminateProcessCommand( String pid ) {
		if( isMac() || isUnix() )
			return ("kill -9 " + pid).split(" ");
		if( isWindows() )
			// tasklist
			return ("taskkill " + pid).split(" ");
		return new String[]{};
	}
	
	public static int getPid( Process proc ) {
		int pid = -1;
		if(proc.getClass().getName().equals("java.lang.UNIXProcess")) {
			try {
				Field f = proc.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getInt(proc);
			} catch (Throwable e) {}
		}
//		if (proc.getClass().getName().equals("java.lang.Win32Process") || proc.getClass().getName().equals("java.lang.ProcessImpl")) {
//			try {
//				Field f = p.getClass().getDeclaredField("handle");
//				f.setAccessible(true);				
//				long handl = f.getLong(p);
//				Kernel32 kernel = Kernel32.INSTANCE;
//				W32API.HANDLE handle = new W32API.HANDLE();
//				handle.setPointer(Pointer.createConstant(handl));
//				pid = kernel.GetProcessId(handle);
//			} catch (Throwable e) {}
//		}
		return pid;
	}
}


/*
# -*- coding: utf-8 -*-

import ConfigParser
import os
import pickle
import tempfile
from datetime import datetime as DT


def log(msg, dbgLvl = 0):
    print msg

def encrypt(wip):
    return wip

def decrypt(wip):
    return wip

def serialize( wip, location=None ):
    if location == None:
        return pickle.dumps( wip )
    else:
        pickle.dump(wip, location)

def deserialize( wip, location=None ):
    if location == None:
        return pickle.loads( wip )
    else:
        return pickle.load(location)




class Singleton(object):
    __instance = None
    def __new__(cls, *args, **kwargs):
        if not cls.__instance:
            cls.__instance = super(Singleton, cls).__new__(cls,*args, **kwargs)
        return cls.__instance
    def __init__(self, *args, **kw):
        """To overwrite
            Simply create for avoid the:
                "TypeError: default __new__ takes no parameters" error
        """
        super(Singleton, self).__init__(*args, **kw)

*/
//public static Properties load_cfg( String fileName ) { return load_cfg(fileName, true ); }
//public static Properties load_cfg( String fileName, boolean first ) {
//	Properties prop = new Properties();
//    InputStream is = null;
//    System.out.println(System.getProperty("user.dir"));
//    File directory = new File (".");
//    try {
//		System.out.println(directory.getCanonicalPath());
//	} catch (IOException e2) { e2.printStackTrace(); }
//	try {
//		is = new FileInputStream(fileName);
//		prop.load(is);
//	} catch (FileNotFoundException e) {
//		if( !first ) e.printStackTrace();
//		File f = new File( fileName );
//		try {
//			f.createNewFile();
//		} catch (IOException e1) { e1.printStackTrace(); }
//		load_cfg( fileName, false );
//	} catch (IOException e) { e.printStackTrace(); }
//	return prop;
//}
//
//public static void save_cfg( Properties prop, String fileName ) { save_cfg(prop, fileName, true); }
//public static void save_cfg( Properties prop, String fileName, boolean first ) {
//	OutputStream os = null;
//	try {
//		os = new FileOutputStream( fileName );
//		prop.store( os, "" );
//	} catch (FileNotFoundException e) {
//		if( !first ) e.printStackTrace();
//		File f = new File( fileName );
//		try {
//			f.createNewFile();
//		} catch (IOException e1) { e1.printStackTrace(); }
//		save_cfg(prop, fileName, false);
//	} catch (IOException e) { e.printStackTrace(); }
//}
