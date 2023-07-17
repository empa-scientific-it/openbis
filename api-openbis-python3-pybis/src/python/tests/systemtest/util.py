#   Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#        http://www.apache.org/licenses/LICENSE-2.0
#   
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
import filecmp
import inspect
import os
import os.path
import re
import shutil
import subprocess
import sys
import time
import zipfile

USER=os.environ['USER']
DEFAULT_WHO_AM_I_TEMPLATE="""

/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\
\\/\\/\/ %s
/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\

"""

def printWhoAmI(levels = 1, template = DEFAULT_WHO_AM_I_TEMPLATE):
    """
    Prints the names of the functions in the caller chain of this function up to the specified number of levels.
    """
    stack = inspect.stack()
    chain = ''
    for i in range(1, min(levels + 1, len(stack))):
        stack_entry = stack[i]
        location = "%s:%s" % (stack_entry[3], stack_entry[2])
        chain = "%s > %s" % (location, chain) if chain != '' else location
    printAndFlush(template % chain)

def printAndFlush(data):
    """ 
    Prints argument onto the standard console and flushes output.
    This is necessary to get Python output and bash output in sync on CI server.
    """
    print(data)
    sys.stdout.flush()
    

def readProperties(propertiesFile):
    """
    Reads a Java properties file and returns the key-value pairs as a dictionary.
    """
    with open(propertiesFile, "r") as f:
        result = {}
        for line in f.readlines():
            trimmedLine = line.strip()
            if len(trimmedLine) > 0 and not trimmedLine.startswith('#'):
                splittedLine = line.split('=', 1)
                key = splittedLine[0].strip()
                value = splittedLine[1].strip()
                result[key] = value
        return result
    
def writeProperties(propertiesFile, dictionary):
    """
    Saves the specified dictionary as a Java properties file.
    """
    with open(propertiesFile, "w") as f:
        for key in sorted(dictionary):
            f.write("%s=%s\n" % (key, dictionary[key]))
            
def executeCommand(commandWithArguments, failingMessage = None, consoleInput = None, suppressStdOut = False, 
                   workingDir = None):
    """
    Executes specified command with arguments. 
    If the exit value of the command is not zero and a failing message has been specified 
    an exception with the failing message will be thrown. 
    Optionally a string for console input can be specified.
    If flag suppressStdOut is set standard output will be suppressed but returned as a list of output lines.
    If workingDir is specified a change to workingDir is done for execution.
    """
    printAndFlush("\n------- START: %s" % commandWithArguments)
    currentDir = None
    if workingDir != None:
        printAndFlush("change to working directory '%s'" % workingDir)
        currentDir = os.getcwd()
        os.chdir(workingDir)
    try:
        processIn = subprocess.PIPE if consoleInput != None else None
        processOut = subprocess.PIPE if suppressStdOut else None
        # Setting the time zone is needed for sprint server otherwise Java log files have wrong time zone
        os.environ['TZ'] = time.tzname[0]
        p = subprocess.Popen(commandWithArguments, stdin = processIn, stdout = processOut, encoding='utf8')
        if consoleInput != None:
            p.communicate(consoleInput)
        lines = []
        if suppressStdOut:
            for line in iter(p.stdout.readline,''):
                lines.append(line.strip())
        exitValue = p.wait()
        if currentDir != None:
            printAndFlush("change back to previous working directory '%s'" % currentDir)
        if exitValue != 0 and failingMessage != None: 
            printAndFlush("---- FAILED %d: %s" % (exitValue, commandWithArguments))
            raise Exception(failingMessage)
        printAndFlush("---- FINISHED: %s" % commandWithArguments)
        return lines
    finally:
        if currentDir != None:
            os.chdir(currentDir)
            
        
    
def killProcess(pidFile):
    """
    Kills the process in specified PID file. Does nothing if PID file doesn't exist.
    """
    pid = getPid(pidFile)
    if pid is None:
        return
    executeCommand(['kill', pid])
    
def isAlive(pidFile, pattern):
    """
    Checks if the process with PID in specified file is alive. The specified regex
    is used to check that the process of expected PID is the process expected.
    """
    pid = getPid(pidFile)
    if pid is None:
        return False
    lines = executeCommand(['ps', '-p', pid], suppressStdOut=True)
    if len(lines) < 2:
        return False
    return re.compile(pattern).search(lines[1]) is not None
    
    
def getPid(pidFile):
    if not os.path.exists(pidFile):
        return None
    return readFirstLine(pidFile)

def readFirstLine(textFile):
    """
    Returns the first line of the specified textFile.
    """
    with open(textFile, 'r') as handle:
        return handle.readline().rstrip()
        
def unzip(zipFile, destination):
    """
    Unzips specified ZIP file at specified destination.
    """
    executeCommand(['unzip', '-q', '-o', zipFile, '-d', destination], "Couldn't unzip %s at %s" % (zipFile, destination))
    
def unzipSubfolder(zipFile, subfolder, destination):
    """
    Unzips the specified subtree from the specified ZIP file into the specified destination
    """
    zf = zipfile.ZipFile(zipFile)
    parent, name = os.path.split(subfolder)
    if name == '': 
        parent = os.path.dirname(parent)
    for entry in zf.namelist():
        if entry.startswith(subfolder):
            newPath = entry.replace(parent, destination)
            newPathParent = os.path.dirname(newPath)
            if not os.path.exists(newPathParent):
                os.makedirs(newPathParent)
            if not newPath.endswith('/'):
                data = zf.read(entry)
                with open(newPath, 'wb') as out:
                    out.write(data)
    
def deleteFolder(folderPath):
    """
    Deletes the specified folder.
    Raises an exception in case of error.
    """
    printAndFlush("Delete '%s'" % folderPath)
    def errorHandler(*args):
        _, path, _ = args
        raise Exception("Couldn't delete '%s'" % path)
    shutil.rmtree(folderPath, onerror = errorHandler)
    
def copyFromTo(sourceFolder, destinationFolder, relativePathInSourceFolder):
    source = "%s/%s" % (sourceFolder, relativePathInSourceFolder)
    destination = "%s/%s" % (destinationFolder, relativePathInSourceFolder)
    if os.path.isfile(source):
        shutil.copyfile(source, destination)
    else:
        shutil.copytree(source, 
                    destination, ignore = shutil.ignore_patterns(".*"))
    printAndFlush("'%s' copied from '%s' to '%s'" % (relativePathInSourceFolder, sourceFolder, destinationFolder))

def getDatabaseHost():
    host = os.environ.get('FORCE_OPENBIS_POSTGRES_HOST')
    if (host is None):
        host = "localhost"
    return host

def dropDatabase(psqlExe, database):
    """
    Drops the specified database by using the specified path to psql.
    """
    executeCommand([psqlExe,
    '-h', getDatabaseHost(),
    '-U', 'postgres', '-c' , "drop database if exists %s" % database],
                   "Couldn't drop database %s" % database)
    
def createDatabase(psqlExe, database, scriptPath = None):
    """
    Creates specified database and run (if defined) the specified SQL script. 
    """
    executeCommand([psqlExe,
    '-h', getDatabaseHost(),
    '-U', 'postgres', '-c' , "create database %s with owner %s" % (database, USER)],
                   "Couldn't create database %s" % database)
    if scriptPath == None:
        return
    executeCommand([psqlExe,
    '-h', getDatabaseHost(),
    '-q', '-U', USER, '-d', database,  '-f', scriptPath], suppressStdOut=True,
                   failingMessage="Couldn't execute script %s for database %s" % (scriptPath, database))
    
def queryDatabase(psqlExe, database, queryStatement, showHeaders = False):
    """
    Queries specified database by applying specified SQL statement and returns the result set as a list
    where each row is a list, too.
    """
    printingOption = '-A' if showHeaders else '-tA'
    lines = executeCommand([psqlExe,
    '-h', getDatabaseHost(),
    '-U', 'postgres', printingOption, '-d', database, '-c', queryStatement],
                           "Couldn't execute query: %s" % queryStatement, suppressStdOut = True)
    result = []
    for line in lines:
        result.append(line.split('|'))
    return result

def printResultSet(resultSet):
    """
    Prints the specified result set.
    """
    for row in resultSet:
        printAndFlush(row)

def getNumberOfDifferences(fileOrFolder1, fileOrFolder2):
    """
    Gets and reports differences in file system structures between both arguments.
    """
    result = filecmp.dircmp(fileOrFolder1, fileOrFolder2, ignore=['.svn'])
    result.report()
    return len(result.left_only) + len(result.right_only) + len(result.diff_files)

def getContent(path):
    """
    Returns the content at specified path as an array of lines. Trailing white spaces (including new line)
    has been stripped off.
    """
    with open(path, "r") as f:
        return [ l.rstrip() for l in f.readlines()]
    
def renderDuration(duration):
    renderedDuration = renderNumber(duration, 'second')
    if duration > 80:
        minutes = duration / 60
        seconds = duration % 60
        if seconds > 0:
            renderedDuration = "%s and %s" % (renderNumber(minutes, 'minute'), renderNumber(seconds, 'second'))
        else:
            renderedDuration = renderNumber(minutes, 'minute')
    return renderedDuration

def renderNumber(number, unit):
    return ("1 %s" % unit) if number == 1 else ("%d %ss" % (number, unit))

    
class LogMonitor():
    """
    Monitor of a log file. Conditions can be specified for printing a notification and waiting. 
    
    A condition has to be a class with method 'match' which has two string arguments: 
    Event type and log message. It returns 'None' in case of no match and 
    a tuple with zero or more matching elements found in log message.
    """
    def __init__(self, logName, logFilePath, timeOutInMinutes = 5):
        """
        Creates an instance with specified log name (used in notification), log file, and time out.
        """
        self.logName = logName
        self.logFilePath = logFilePath
        self.timeOutInMinutes = timeOutInMinutes
        self.conditions = []
        self.timeProvider = time
        class SystemPrinter:
            def printMsg(self, msg):
                printAndFlush(msg)
        self.printer = SystemPrinter()
    
    def addNotificationCondition(self, condition):
        """
        Adds a notification condition
        """
        self.conditions.append(condition)

    def getFormattedTime(self, timeSec):
        if timeSec is None:
            self.printer.printMsg("Error: Provided time is None!")
        return time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(timeSec))


    def waitUntilEvent(self, condition, startTime = None, delay = 0):
        """
        Waits until an event matches the specified condition. 
        Returns tuple with zero or more elements of matching log message.
        """
        startTime = self.timeProvider.time() if startTime is None else startTime
        self.conditions.append(condition)
        renderedStartTime = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(startTime))
        self.printer.printMsg("\n>>>>> Start monitoring %s log at %s >>>>>>>>>>>>>>>>>>>>" 
                              % (self.logName, renderedStartTime))
        finalTime = startTime + self.timeOutInMinutes * 60
        if delay > 0:
            time.sleep(delay)
        try:
            alreadyPrintedLines = set()
            while True:
                log = open(self.logFilePath, 'r')
                while True:
                    actualTime = self.timeProvider.time()
                    if actualTime > finalTime:
                        self.printer.printMsg(f"Time out detected! start time: {renderedStartTime}, calculated end time: {self.getFormattedTime(finalTime)}, current time: {self.getFormattedTime(actualTime)}, timeout (min): {self.timeOutInMinutes}")
                        raise Exception("Time out after %d minutes for monitoring %s log." 
                                        % (self.timeOutInMinutes, self.logName))
                    line = log.readline()
                    if line == '':
                        break
                    match = re.match('(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}),(\d{3}) (.{6})(.*)', line)
                    if match == None:
                        continue
                    timestamp = match.group(1)
                    milliseconds = int(match.group(2))
                    eventType = match.group(3).strip()
                    message = match.group(4)
                    eventTime = time.mktime(time.strptime(timestamp, '%Y-%m-%d %H:%M:%S')) + 0.001 * milliseconds
                    if eventTime < startTime:
                        continue
                    for c in self.conditions:
                        if c.match(eventType, message) != None and not line in alreadyPrintedLines:
                            alreadyPrintedLines.add(line)
                            self.printer.printMsg(">> %s" % line.strip())
                            break
                    elements = condition.match(eventType, message)
                    if elements != None:
                        return elements
                log.seek(0, os.SEEK_CUR)
                time.sleep(2)
        finally:
            self.printer.printMsg(">>>>> Finished monitoring %s log >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" 
                                  % self.logName)
            
class EventTypeCondition():
    """ A condition which matches in case of specified event type. """
    def __init__(self, eventType):
        self.eventType = eventType
        
    def match(self, eventType, message):
        return () if self.eventType == eventType else None
        
class StartsWithCondition():
    """
    A condition which matches if the message starts with a specified string.
    """
    def __init__(self, startsWithString):
        self.startsWithString = startsWithString
        
    def match(self, eventType, message):
        return () if message.startswith(self.startsWithString) else None
    
class RegexCondition():
    """
    A condition which matches if the message matches a specified regular expression.
    """
    def __init__(self, regex):
        self.regex = regex
        
    def match(self, eventType, message):
        match = re.search(self.regex, message)
        return match.groups() if match else None
    
class ConditionSequence():
    def __init__(self, conditions):
        self.conditions = conditions
        self.matches = []
        
    def match(self, eventType, message):
        match_count = len(self.matches)
        if match_count == len(self.conditions):
            return self.matches
        result = self.conditions[match_count].match(eventType, message)
        if result is not None:
            self.matches.append(result)
            if len(self.matches) == len(self.conditions):
                return self.matches
        return None