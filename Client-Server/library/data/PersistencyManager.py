#!/usr/bin/env python
# -*- coding: utf-8 -*-

from ModelManager import ModelManager
from EntityManager import EntityManager



class PersistencyManager( object ):
    def __init__( self ):
        self._entity = EntityManager()
        self._model = ModelManager()



    def addModel( self, fileName ):
		# TODO > not yet implemented
        if self._model._checkModelCorrectness(fileName):
            name = self._model.addModel(fileName)
            if name == None:
                return
            for entity in self._model._model[name].entities():
                self._entity.addEntity( entity, self._model._model[name][entity]['_signature'] )



    def removeModel(self, modelName):
        self._model.removeModel(modelName)



    def add(self, model, entity):
        if not model in self._model._model.keys():
            return
        wip = self._entity.add(entity)
        self._model.add(model, entity, wip)
        return wip



    def remove(self, model, entity, id):
        if not model in self._model._model.keys():
            return
        self._entity.remove(entity, id)
        self._model.remove(model, entity, id)



    def update(self, model, entity, id, field, val):
        if not self._model.exist(model, entity):
            return
        self._entity.update(entity, id, field, val)



    def ref(self, model, entity, id, field, val):
        if not( self._model.exist(model, entity, id) and self._entity.exist(entity) ):
            return
        self._model.ref(model, entity, id, field, val)



    def deref(self, model, entity, id, field, val):
        self._model.deref(model, entity, id, field, val)



    def get(self, model, entity, id, field):
        if self._model.fieldInfo(model, entity, field) != None:
            return self._model.get(model, entity, id, field)
        elif self._entity.hasField(entity, field):
            return self._entity.get(entity, id, field)
        else:
            return None

    def getFieldInfo(self, model, entity, id, field ):
        tmp = self._model.fieldInfo( model, entity, field )
        if tmp == None:
            tmp = []
        return tmp

    def getInfo(self, model=None, entity=None, id=None, field=None):
        if model == None:
            return self._model.getInfo()
        if entity == None:
            return self._model.getInfo(model)
        if id == None:
            return self._entity.getInfo(entity)
        if field == None:
            ret = self._model.getInfo(model, entity)
            ret += [x[0] for x in self._model.entitySignature(model, entity)]
            ret.remove('_signature')
            return ret
        return self.get(model, entity, id, field)
        #return self[model][entity][id].keys()






if __name__ == "__main__":
    wip = PersistencyManager()
    wip.addModel('abc.schema')
    act = wip.add('abc', 'activity')
    usr = wip.add('abc', 'user')
    app = wip.add('abc', 'application')
    app2 = wip.add('abc', 'application')
    appS = wip.add('abc', 'application_state')
    wip.update('abc','user', usr, 'name', 'Paolo')
    print wip.get('abc', 'user', usr, 'name')
    wip.ref('abc','activity', act, 'user', usr)
    print wip.get('abc', 'activity', act, 'user')
    wip.ref('abc','application_state', appS, 'application', app)
    wip.ref('abc','application_state', appS, 'user', usr)
    print wip.get('abc', 'application_state', appS, 'application')
    wip.ref('abc','application_state', appS, 'application', app)
    wip.ref('abc','application_state', appS, 'application', app2)
    print wip.get('abc', 'application_state', appS, 'application')    
    wip.deref('abc','application_state', appS, 'application', app2)
    try:
        print wip.get('abc', 'application_state', appS, 'application')    
    except:
        pass
    wip.ref('abc','application_state', appS, 'application', app2)
    print wip.get('abc', 'application_state', appS, 'application')    
    try:
        print wip.get('abc', 'user', usr, 'nname')    
    except:
        pass









