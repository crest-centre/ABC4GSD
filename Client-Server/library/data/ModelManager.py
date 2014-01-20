#!/usr/bin/env python
# -*- coding: utf-8 -*-


import ConfigParser
import os
from library.constants.co_data import *



class ModelManager( dict ):
    def __init__( self ):
        self._model = {}



    def addModel(self, fileName):
        if not os.path.exists(fileName):
            return
        cfg = ConfigParser.ConfigParser()
        cfg.read(fileName)
        tmp = Model( cfg )
        self._model[ tmp.name ] = tmp
        return tmp.name



    def removeModel( self, modelName ):
        del self._model[ modelName ]



    def getInfo(self, model=None, entity=None):
        try:
            if model == None:
                return self._model.keys()
            if entity == None:
                return self._model[model].keys()
            return self._model[model][entity].keys()
            # return [self.fieldInfo(model,entity,x) for x in self._model[model][entity].keys()]
        except KeyError:
			# print 'KeyError in ModelManager.getInfo'
			raise ModelError( "ModelManager.getInfo", (model, entity) )



	# Add new entity
    def add(self, model, entity, id):
        if model not in self.keys():
            self[model] = {}
        if entity not in self[model].keys():
            self[model][entity] = {}
        self[model][entity][id] = {}



	# Remove entity
    def remove(self, model, entity, id):
        if self.exist(model, entity, id):
			# TODO > what if the entity is shared by schemas
            del self[model][entity][id]



    def get( self, model=None, entity=None, id=None, field=None ):
        try:
            return self[model][entity][id][field]
        except KeyError:
			# print 'KeyError in ModelManager.getInfo'
			raise ModelError( "ModelManager.get", (model, entity, id, field) )
    


	# Add relation
    def ref( self, model, entity, id, field, value ):
        fieldInfo = self.fieldInfo(model, entity, field)
        if fieldInfo != None:
            if not self.exist(model, entity, id, field):
                self[model][entity][id][field] = []
            # check data type and convert
            if fieldInfo[F_MULTIPLE]:
                # check that there is no duplicate
                self[model][entity][id][field].append(value)
            else:
                self[model][entity][id][field] = [value]



	# Remove relation
    def deref( self, model, entity, id, field, value ):
        if self.fieldInfo(model, entity, field)[F_MULTIPLE]:
            if value in self.get(model, entity, id, field):
                self[model][entity][id][field].remove(value)
                if not len(self[model][entity][id][field]):
                    del self[model][entity][id][field]
        elif value in self[model][entity][id][field]:
            del self[model][entity][id][field]
    


    def exist(self, model, entity=None, id=None, field=None):
        if entity == None:
            return model in self.keys()
        if id == None:
            return model in self.keys() and entity in self[model].keys()
        if field == None:
            return model in self.keys() and entity in self[model].keys() and id in self[model][entity].keys()
        return model in self.keys() and entity in self[model].keys() and id in self[model][entity].keys() and field in self[model][entity][id].keys()
    # if yes returns field info



    def fieldInfo(self, model, entity, field):
        if not field in self._model[model][entity].keys():
            return None
        tmp = self._model[model][entity][field].split('.')
        resp = [tmp[0], 
                tmp[1] if tmp[1][-1] != MULTIPLICITY_IDENTIFIER else tmp[1][:-1], 
                False if tmp[1][-1] != MULTIPLICITY_IDENTIFIER else True]
        print resp
        return resp



    def entitySignature(self, model, entity):
        return self._model[model][entity]['_signature']



    def _checkModelCorrectness(self, fileName):
        return True






class ModelError(Exception):
	def __init__(self, method, msg=''):
		self.method = method
		self.msg = msg
	def __str__(self):
		msg = "" + repr(self.method) + " failed."
		if self.msg:
			msg += " Message: " + repr(self.msg)
		return msg






class Model( dict ):
    def __init__(self, schema, *args, **kw):
        self.name = schema.get('schema', 'name')
        self.version = schema.get('schema', 'version')
        for entity in schema.options('entities'):
            self[entity] = {}
            self[entity]['_signature'] = [ tuple( x.split(':')) for x in schema.get('entities', entity).split(',')]
            for relation in schema.options(entity):
                self[entity][relation] = schema.get(entity, relation)



    def entities(self):
        return self.keys()






if __name__ == "__main__":
    pass
