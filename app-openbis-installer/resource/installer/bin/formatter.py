import json
import sys
from datetime import datetime
import time
import collections
import copy

class Event(object):
    def __init__(self, validFrom, type, key, value, entityType, validTimeStampType):
        self.validFrom = validFrom
        self.type = type
        self.key = key
        self.value = value
        self.entityType = entityType
        self.validTimeStampType = validTimeStampType
        
    def renderKey(self):
        return "%s [%s]" % (self.key, self.type)
        
    def renderValue(self):
        result = self.value
        if self.entityType is not None:
            result += "[%s]" % self.entityType
        return result
        
def timestamptonumber(s):
    if s is None:
        return 0
    return (datetime.strptime(s,"%Y-%m-%d %H:%M:%S.%f") - epoch).total_seconds() * 1000.0

entity_id = sys.argv[1]

payload = ""
for line in sys.stdin:
    payload = payload + line.strip()

if payload == "":
    print "Entity %s not found " % (entity_id)
    sys.exit(-1)

content = json.loads(payload)
epoch = datetime.utcfromtimestamp(0)


data = dict()

for entry in content[entity_id]:
    type = entry['type'] if 'type' in entry else '?'
    key = entry['key']
    value = entry['value']
    entityType = entry['entityType'] if 'entityType' in entry else None
    validfrom = entry['validFrom'] if 'validFrom' in entry else None
    validuntil = entry['validUntil'] if 'validUntil' in entry else None
    
    if validfrom in data:
        data[validfrom].append(Event(validfrom, type, key, value, entityType, "begin"))
    else:
        data[validfrom] = [Event(validfrom, type, key, value, entityType, "begin")]

    if validuntil is not None:
        if validuntil in data:
            data[validuntil].append(Event(validfrom, type, key, value, entityType, "end"))
        else:
            data[validuntil] = [Event(validfrom, type, key, value, entityType, "end")]
            

sorted_data = collections.OrderedDict(sorted(data.items(), key=lambda t: timestamptonumber(t[0])))

history = []
currenttime = next(iter(sorted_data))
currententity = dict()

for timestamp, events in sorted_data.iteritems():
    if currenttime != timestamp:
        element = copy.deepcopy(currententity)
        element['time'] = currenttime
        currenttime = timestamp
        history.append(element)
    
    
    for event in filter(lambda event: event.validTimeStampType == "end", events):
        key = event.renderKey()
        value = event.renderValue()
        currententity[key].remove(value)
        if len(currententity[key]) == 0:
            del currententity[key]
            
    for event in filter(lambda event: event.validTimeStampType == "begin", events):
        key = event.renderKey()
        value = event.renderValue()
        if key not in currententity:
            currententity[key] = set([value])
        else:
            currententity[key].add(value)

if len(history) == 0 or currententity != history[-1]:
    currententity['time'] = currenttime
    history.append(currententity)

for state in history:
    print "-- %s --" % state['time']
    
    for key, value in collections.OrderedDict(reversed(list(state.items()))).iteritems():
        if key != 'time':
            for element in value:
                print "%s: %s" % (key, element)
    print "\n"

