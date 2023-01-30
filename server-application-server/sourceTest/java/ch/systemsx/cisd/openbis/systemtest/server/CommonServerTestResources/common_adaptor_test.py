def assertEqual(actual, expected, fieldName):
    if(actual != expected):
        raise Exception('Expected ' + str(fieldName) + ': "' + str(expected) + '" but was: "' + str(actual) + '"')

def assertEntity(entity, expectedEntityCode, fieldName):
    if(entity.code() != expectedEntityCode):
        raise Exception('Expected ' + str(fieldName) + ': "' + str(expectedEntityCode) + '" but was: "' + str(entity.code()) + '"')

def assertEntities(iterable, expectedEntityCodes, fieldName):
    actualEntityCodes = []
    
    for entity in iterable:
        actualEntityCodes.append(entity.code())
    
    if(set(actualEntityCodes) != set(expectedEntityCodes)):
        raise Exception('Expected ' + str(fieldName) + ': "' + str(expectedEntityCodes) + '" but were: "' + str(actualEntityCodes) + '"')
    
def assertEntitiesCount(iterable, expectedEntityCount, fieldName):
    actualEntityCount = 0
    
    for entity in iterable:
        actualEntityCount += 1
    
    if(actualEntityCount != expectedEntityCount):
        raise Exception('Expected ' + str(fieldName) + ': "' + str(expectedEntityCount) + '" but were: "' + str(actualEntityCount) + '"')
