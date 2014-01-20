# -*- coding: utf8 -*-

# Generic State
abc_UNKNOWN = 11021
abc_INITIALIZED = 11022

# Activity State
activity_UNKNOWN = 11001
activity_INITIALIZED = 11002
activity_RESUMED = 11003
activity_SUSPENDED = 11004
activity_FINALIZED = 11005
activity_STATES_ALLOWED = [
                           activity_UNKNOWN, 
                           activity_INITIALIZED, 
                           activity_RESUMED, 
                           activity_SUSPENDED, 
                           activity_FINALIZED, 
                           ]

# Application State
application_DETACHED = 11051
application_PINNED = 11052
application_FULLSYNC = 11053
application_STATES_ALLOWED = [
                              application_DETACHED, 
                              application_PINNED, 
                              application_FULLSYNC, 
                              ]

# Activity State
user_UNKNOWN = 11101
user_CONNECTED = 11102
user_DISCONNECTED = 11103
