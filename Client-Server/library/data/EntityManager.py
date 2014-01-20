#!/usr/bin/env python
# -*- coding: utf-8 -*-



# Specialization of a python dictionary
class EntityManager( dict ):
    def __init__( self, *args, **kw ):
        self._entitySchema = {}



    def addEntity( self, name, signature ):
		# DUNNO > If not name in self._entitySchema.keys():
		# TODO > need to allow different signatures for different schemas
        self._entitySchema[ name ] = [ ('_id', 'long') ] + [ x for x in signature ]
        if name not in self.keys():
            self[name] = {}



	# Add new entry to entity named <name> and returns unique id
    def add( self, name ):
        tmp = Entity()
        self[name][ tmp['_id'] ] = tmp
        return tmp['_id']



	# Remove entry from entity named <name>
    def remove( self, name, id ):
        # if self.exist(name, id):
		try:
			del self[name][id]
		except KeyError:
			raise EntityError( "EntityManager.remove", (name, id) )
		



	# Get field
    def get( self, entity, id, field ):
        try:
            return self[ entity ][ id ][ field ]
        except KeyError:
            # print 'KeyError EntityManager.get'
			raise EntityError( "EntityManager.get", (entity, id, field) )



	# Update field
    def update( self, entity, id, field, value ):
        if self.exist(entity, id) and self.hasField(entity, field):
            # TODO > check data type and convert
            self[ entity ][ id ][ field ] = value


    
	# Check if entity name exists OR if entry with id <id> exists for entity <entity>
    def exist(self, entity, id=None):
        if id == None:
            return entity in self.keys()
        return entity in self.keys() and id in self[entity].keys()



	# Check if field exists in the given entity
    def hasField(self, entity, field):
        return entity in self._entitySchema.keys() and field in self.getFields(entity)



	# Get list of available fields
	# TODO > schema independent, is it an issue?
    def getFields(self, entity):
        return [x[0] for x in self._entitySchema[entity]]



	# Get list of available entries for a given entity
    def getInfo(self, entity):
        return self[entity].keys()






# Specialization of a python dictionary
class Entity( dict ):
    def __init__(self, *args, **kw):
        self._fields = ['_id']
		# Assignment of unique id
        self['_id'] = id(self)






class EntityError(Exception):
	def __init__(self, method, msg=''):
		self.method = method
		self.msg = msg
	def __str__(self):
		msg = "" + repr(self.method) + " failed."
		if self.msg:
			msg += " Message: " + repr(self.msg)
		return msg






if __name__ == "__main__":
    a=EntityManager()
    a.addEntity( 'activity',  [ ('name','str') ] )
    x = a.add('activity')
    a.update('activity', x, 'name', 'Paolo')
    print a.get('activity', x, 'name')
    







