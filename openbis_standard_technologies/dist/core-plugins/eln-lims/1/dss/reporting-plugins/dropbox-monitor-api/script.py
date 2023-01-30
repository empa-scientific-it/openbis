import os
import re
from sets import Set

from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.systemsx.cisd.etlserver import ETLDaemon
from SimpleInfoObject import SimpleInfoObject
from SimpleInfoBuilder import SimpleInfoBuilder
from DetailInfoObject import DetailInfoObject
from DetailInfoBuilder import DetailInfoBuilder


def process(tr, parameters, tableBuilder):
    if parameters.get("checkAuthorization") == True:
        checkAuthorization(tr, tableBuilder)
        return
    assertAuthorization(tr)
    logDirectory = tr.getGlobalState().getDssRegistrationLogDir().getAbsolutePath()
    inProcessLogFiles = getAllLogFiles(logDirectory, "in-process")
    failedLogFiles = getAllLogFiles(logDirectory, "failed")
    succeededLogFiles = getAllLogFiles(logDirectory, "succeeded")

    dropboxName = parameters.get("dropboxName")
    if dropboxName == None:
        simpleInfoBuilder = SimpleInfoBuilder(tableBuilder)
        simpleInfoBuilder.createHeader()
        dropboxesList = listAllDropboxes()
        for dropbox in dropboxesList:
            simpleInfo = SimpleInfoObject(dropbox, inProcessLogFiles, failedLogFiles, succeededLogFiles)
            simpleInfoMap = simpleInfo.getSimpleInfoMap()
            simpleInfoBuilder.buildRow(simpleInfoMap)
    else:
        logN = parameters.get("logN")
        detailInfoObject = DetailInfoObject(logN, dropboxName, inProcessLogFiles, failedLogFiles,
            succeededLogFiles, logDirectory)
        detailInfoMap = detailInfoObject.getDetailInfoMap()
        detailInfoBuilder = DetailInfoBuilder(tableBuilder, dropboxName)
        detailInfoBuilder.createHeader()
        detailInfoBuilder.buildRow(detailInfoMap)


def checkAuthorization(tr, tableBuilder):
    tableBuilder.addHeader("isAuthorized")
    row = tableBuilder.addRow()
    row.setCell("isAuthorized", "true" if isAuthorized(tr) else "false")


def assertAuthorization(tr):
    if not isAuthorized(tr):
        raise UserFailureException("User isn't authorized for using the Dropbox Monitor.")


def isAuthorized(tr):
    authService = tr.getAuthorizationService()
    roleAssignements = authService.listRoleAssignments()
    for ra in roleAssignements:
        if ra.getUser() is not None:
            user = ra.getUser().getUserId()
            role = str(ra.getRoleSetCode())
            if user == userId and (role.endswith("USER") or role.endswith("ADMIN")):
                return True
    groups = Set([g.getCode() for g in authService.listAuthorizationGroupsForUser(userId)])
    for ra in roleAssignements:
        if ra.getAuthorizationGroup() is not None and ra.getAuthorizationGroup().getCode() in groups:
            role = str(ra.getRoleSetCode())
            if role.endswith("USER") or role.endswith("ADMIN"):
                return True
    return False


def listAllDropboxes():
    dropboxes = []
    for p in ETLDaemon.getThreadParameters():
        dropboxName = p.getThreadName()
        if re.match(r'\w', dropboxName):
            dropboxes.append(dropboxName)
    return dropboxes


def getAllLogFiles(logDirectory, subFolder):
    files = os.listdir(os.path.join(logDirectory, subFolder))
    return [file for file in files if isLogFile(file)]


def isLogFile(file):
    return re.match(r'[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}-[0-9]{3}', file)
