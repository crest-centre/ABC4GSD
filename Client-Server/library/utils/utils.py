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

def load_cfg(cfgFile):
        if not os.path.exists(cfgFile):
            f = open(cfgFile, 'w')
            f.close()
        cfg = ConfigParser.ConfigParser()
        cfg.read(cfgFile)
        return cfg

def save_cfg( cfg, cfgFile ):
        f = open( cfgFile,  'w')
        cfg.write( f )
        f.close()

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

def getTmpDir(p = 'ABC_'):
    dir = tempfile.mkdtemp(prefix=p)
    print '<><><><><><><><><><><><><><><><><><><><><><>' + dir
    return dir

def getRandomId():
    return DT.now().microsecond


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
