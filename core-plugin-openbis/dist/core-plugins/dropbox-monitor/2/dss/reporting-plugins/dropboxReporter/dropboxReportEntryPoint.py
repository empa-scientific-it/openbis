import os
import re
from ch.systemsx.cisd.etlserver import ETLDaemon
from ch.systemsx.cisd.common.exceptions import UserFailureException
from SimpleInfoObject import SimpleInfoObject
from SimpleInfoBuilder import SimpleInfoBuilder
from DetailInfoObject import DetailInfoObject
from DetailInfoBuilder import DetailInfoBuilder
from java.lang import System


def process(tr, parameters, tableBuilder):

    authService = tr.getAuthorizationService()
    admin = authService.doesUserHaveRole(userId, "ADMIN", None)
    if admin:
        pass
    else:
        raise UserFailureException(
            "The user is not an admin, can't use this tool.")
        return

    rootDirectory = getRootDir(tr)
    dropboxesList = listAllDropboxes()

    logDirectory = getLogsDir(tr)

    dropboxInProcess = os.listdir(os.path.join(logDirectory, "in-process"))
    dropboxFailed = os.listdir(os.path.join(logDirectory, "failed"))
    dropboxSucceeded = os.listdir(os.path.join(logDirectory, "succeeded"))
    dropboxInProcess = validateList(dropboxInProcess)
    dropboxFailed = validateList(dropboxFailed)
    dropboxSucceeded = validateList(dropboxSucceeded)

    if parameters.get("dropboxName") == None:
        simpleInfoBuilder = SimpleInfoBuilder(tableBuilder)
        simpleInfoBuilder.createHeader()

        for dropbox in dropboxesList:
            simpleInfo = SimpleInfoObject(
                dropbox, dropboxInProcess, dropboxFailed, dropboxSucceeded)
            simpleInfoMap = simpleInfo.getSimpleInfoMap()
            simpleInfoBuilder.buildRow(simpleInfoMap)

    else:
        logN = parameters.get("logN")
        dropboxName = parameters.get("dropboxName")

        detailInfoObject = DetailInfoObject(
            logN, dropboxName, dropboxInProcess, dropboxFailed,
            dropboxSucceeded, logDirectory)
        detailInfoMap = detailInfoObject.getDetailInfoMap()

        detailInfoBuilder = DetailInfoBuilder(tableBuilder, dropboxName)
        detailInfoBuilder.createHeader()
        detailInfoBuilder.buildRow(detailInfoMap)


def getLogsDir(tr):
    defaultDir = System.getProperty("user.dir")
    dir = getProperty(tr, "dss-registration-log-dir")
    if dir is None or os.path.isdir(dir) == False:
        return os.path.join(defaultDir, "log-registrations")
    else:
        return dir        


def listAllDropboxes():
    dropboxesList = []
    for p in ETLDaemon.getThreadParameters():
        dropboxName = p.getThreadName()
        if validateName(dropboxName):
            dropboxesList.append(dropboxName)
    return dropboxesList


def getRootDir(tr):
    # root directory ends with /data for weird reason
    rootDirectory = getProperty(tr, "root-dir")
    rootDirectory = rootDirectory[:rootDirectory.rfind("/")]
    return rootDirectory


def getProperty(tr, propertyName):
    properties = tr.getGlobalState().getThreadParameters().getThreadProperties()
    return properties.getProperty(propertyName)


def validateList(list_):
    validatedList = []
    for item in list_:
        if re.match(r'[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}', item):
            validatedList.append(item)
    return validatedList


def validateName(name):
    if re.match(r'\w', name):
        return True
    else:
        return False
