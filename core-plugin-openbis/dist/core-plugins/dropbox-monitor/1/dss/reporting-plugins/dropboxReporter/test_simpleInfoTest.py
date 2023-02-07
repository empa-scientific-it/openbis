from SimpleInfoObject import SimpleInfoObject
import os
import datetime

logDirectory = "./testDir"
dropboxesList = ['nothingDb', 'runDbShort', 'runDbForever', 'idleDb', 'massiveDb', 'neverFailedDb', 'neverSuccededDb', 'neverRunDb']

dropboxInProcess = os.listdir(os.path.join(logDirectory, "in-process"))
dropboxFailed = os.listdir(os.path.join(logDirectory, "failed"))
dropboxSucceeded = os.listdir(os.path.join(logDirectory, "succeeded"))

def test_nothingDb():
    simpleInfoObject = SimpleInfoObject('nothingDb', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "nothingDb"
    assert simpleInfoMap["lastSuccess"] == None
    assert simpleInfoMap["currentStatus"] == "idle"
    assert simpleInfoMap["currentStatusTime"] == None
    assert simpleInfoMap["lastStatus"] == None
    assert simpleInfoMap["lastFailure"] == None

def test_runDbShort():
    simpleInfoObject = SimpleInfoObject('runDbShort', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "runDbShort"
    assert simpleInfoMap["lastStatus"] == None
    assert simpleInfoMap["currentStatus"] == "running"
    assert simpleInfoMap["lastSuccess"] == None
    assert simpleInfoMap["lastFailure"] == None

def test_runDbForever():
    simpleInfoObject = SimpleInfoObject('runDbForever', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "runDbForever"
    assert simpleInfoMap["lastStatus"] == None
    assert simpleInfoMap["currentStatus"] == "running"
    assert type(simpleInfoMap["currentStatusTime"]) == datetime.datetime
    assert simpleInfoMap["lastSuccess"] == None
    assert simpleInfoMap["lastFailure"] == None

def test_idleDb():
    simpleInfoObject = SimpleInfoObject('idleDb', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "idleDb"
    assert type(simpleInfoMap["lastStatus"]) == int
    assert simpleInfoMap["currentStatus"] == "idle"
    assert type(simpleInfoMap["currentStatusTime"]) == datetime.datetime
    assert type(simpleInfoMap["lastSuccess"]) == datetime.datetime
    assert type(simpleInfoMap["lastFailure"]) == datetime.datetime

def test_massiveDb():
    simpleInfoObject = SimpleInfoObject('massiveDb', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "massiveDb"
    assert type(simpleInfoMap["lastStatus"]) == int
    assert simpleInfoMap["currentStatus"] == "idle"
    assert type(simpleInfoMap["currentStatusTime"]) == datetime.datetime
    assert type(simpleInfoMap["lastSuccess"]) == datetime.datetime
    assert type(simpleInfoMap["lastFailure"]) == datetime.datetime

def test_neverFailedDb():
    simpleInfoObject = SimpleInfoObject('neverFailedDb', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "neverFailedDb"
    assert type(simpleInfoMap["lastStatus"]) == int
    assert simpleInfoMap["currentStatus"] == "idle"
    assert type(simpleInfoMap["currentStatusTime"]) == datetime.datetime
    assert type(simpleInfoMap["lastSuccess"]) == datetime.datetime
    assert simpleInfoMap["lastFailure"] == None

def test_neverSuccededDb():
    simpleInfoObject = SimpleInfoObject('neverSuccededDb', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "neverSuccededDb"
    assert type(simpleInfoMap["lastStatus"]) == int
    assert simpleInfoMap["currentStatus"] == "idle"
    assert type(simpleInfoMap["currentStatusTime"]) == datetime.datetime
    assert simpleInfoMap["lastSuccess"] == None
    assert type(simpleInfoMap["lastFailure"]) == datetime.datetime

def test_neverRunDb():
    simpleInfoObject = SimpleInfoObject('neverRunDb', dropboxInProcess, dropboxFailed, dropboxSucceeded)
    simpleInfoMap = simpleInfoObject.getSimpleInfoMap()
    print str(simpleInfoMap)
    assert simpleInfoMap["dropboxName"] == "neverRunDb"
    assert simpleInfoMap["lastStatus"] == None
    assert simpleInfoMap["currentStatus"] == 'idle'
    assert simpleInfoMap["currentStatusTime"] == None
    assert simpleInfoMap["lastSuccess"] == None
    assert simpleInfoMap["lastFailure"] == None
