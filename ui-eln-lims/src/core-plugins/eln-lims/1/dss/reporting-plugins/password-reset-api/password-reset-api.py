from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
from ch.systemsx.cisd.openbis.generic.shared.api.v1 import IGeneralInformationService;
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.ethz.sis import PersistentKeyValueStore
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider
from ch.systemsx.cisd.openbis.common.api.client import ServiceFinder;
from ch.systemsx.cisd.common.mail import EMailAddress;
from java.lang import String
from java.util import UUID
from random import SystemRandom
import subprocess, os, string, time


passwdShPath = '../openBIS-server/jetty/bin/passwd.sh'
RESET_TOKEN_KEY_POSTFIX = "-reset-token"
OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"

#
# API functions
#

def process(tr, parameters, tableBuilder):
    if (parameters["method"] == "sendResetPasswordEmail"):
        if (parameters["userId"] is not None and parameters["baseUrl"] is not None):
            sendResetPasswordEmail(tr, parameters["userId"], parameters["baseUrl"])
        else:
            raise UserFailureException("When invoking method 'sendResetPasswordEmail', the parameter 'userId' is required.")
    elif (parameters["method"] == "resetPassword"):
        if (parameters["userId"] is not None and parameters["token"] is not None):
            resetPassword(tr, parameters["userId"], parameters["token"])
        else:
            raise UserFailureException("When invoking method 'resetPassword', the parameters 'userId' and 'token' are required.")
    else:
        raise UserFailureException("Unknown method: " + parameters["method"])
    
    tableBuilder.addHeader("STATUS");
    tableBuilder.addHeader("MESSAGE");
    tableBuilder.addHeader("RESULT");
    row = tableBuilder.addRow();
    row.setCell("STATUS","OK");
    row.setCell("MESSAGE", "Operation Successful");
    row.setCell("RESULT", getJsonForData( { "result" : "success" }));    


def sendResetPasswordEmail(tr, userId, baseUrl):
    # generate and store token
    token = UUID.randomUUID().toString()
    timestamp = time.time()
    PersistentKeyValueStore.put(userId + RESET_TOKEN_KEY_POSTFIX, { "token" : token, "timestamp" : timestamp})
    # send email
    emailAddress = getUserEmail(tr, userId)
    sendResetPasswordEmailInternal(tr, emailAddress, userId, token, baseUrl)

def resetPassword(tr, userId, token):
    if tokenIsValid(tr, userId, token):
        email = getUserEmail(tr, userId)
        resetPasswordInternal(tr, email, userId)
        PersistentKeyValueStore.remove(userId + RESET_TOKEN_KEY_POSTFIX)
    else:
        raise UserFailureException("Invalid token.")

#
# internal functions
#

def sendMail(tr, email, subject, body):
    replyTo = None;
    fromAddress = None;
    recipient1 = EMailAddress(email);
    tr.getGlobalState().getMailClient().sendEmailMessage(subject, body, replyTo, fromAddress, recipient1);

def getJsonForData(data):
    jsonValue = ServiceProvider.getObjectMapperV3().writeValueAsString(data);
    return jsonValue;

def tokenIsValid(tr, userId, token):
    tokenAndTimestamp = PersistentKeyValueStore.get(userId + RESET_TOKEN_KEY_POSTFIX)
    if (tokenAndTimestamp != None and tokenAndTimestamp["token"] == token):
        timestampNow = time.time()
        deltaInSeconds = timestampNow - float(tokenAndTimestamp["timestamp"])
        maxDelayInMinutes = float(getProperty(tr, "max-delay-in-minutes"))
        if deltaInSeconds < maxDelayInMinutes * 60:
            return True
        else:
            PersistentKeyValueStore.remove(userId + RESET_TOKEN_KEY_POSTFIX)
    return False

def sendResetPasswordEmailInternal(tr, email, userId, token, baseUrl):
    passwordResetLink = getPasswordResetLink(email, userId, token, baseUrl)
    passwordResetRequestSubject = getProperty(tr, "password-reset-request-subject") % (userId)
    passwordResetRequestBody = getProperty(tr, "password-reset-request-body") % (userId, passwordResetLink)
    sendMail(tr, email, passwordResetRequestSubject, passwordResetRequestBody)

def sendEmailWithNewPassword(tr, email, userId, newPassword):
    newPasswordSubject = getProperty(tr, "new-password-subject") % (userId)
    newPasswordBody = getProperty(tr, "new-password-body") % (newPassword)
    sendMail(tr, email, newPasswordSubject, newPasswordBody)

def getPasswordResetLink(emailAddress, userId, token, baseUrl):
    return "%s?resetPassword=true&userId=%s&token=%s" % (baseUrl, userId, token)

def resetPasswordInternal(tr, email, userId):
    newPassword = getNewPassword()
    updateUserPassword(userId, newPassword)
    sendEmailWithNewPassword(tr, email, userId, newPassword)

def getUserEmail(tr, userId):
    for person in getPersons(tr):
        if person.getUserId() == userId:
            return person.getEmail()
    raise UserFailureException("User email not found.")

def getPersons(tr):
    servFinder = ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
    infService = servFinder.createService(IGeneralInformationService, OPENBISURL);
    return infService.listPersons(tr.getOpenBisServiceSessionToken());

def getNewPassword():
    length = 12
    rng = SystemRandom()
    chars = string.ascii_letters + string.digits + '!@#$%^&*()'
    return ''.join(rng.choice(chars) for i in range(length))

def updateUserPassword(userId, password):
    if os.path.isfile(passwdShPath):
        subprocess.call([passwdShPath, 'change', userId, '-p', password]) #Changes the user pass, works always
        return True;
    else:
        return False;

def getProperty(tr, key):
    threadPropertyDict = {}
    threadProperties = tr.getGlobalState().getThreadParameters().getThreadProperties()
    return threadProperties.getProperty(key)
