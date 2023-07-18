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

import difflib
import os
import os.path
import re
import shutil
import time
import traceback

import util as util

INSTALLER_PROJECT = 'app-openbis-installer'
OPENBIS_STANDARD_TECHNOLOGIES_PROJECT = 'core-plugin-openbis'
DATAMOVER_PROJECT = 'datamover'

PSQL_EXE = 'psql'

PLAYGROUND = 'targets/playground'
TEMPLATES = 'templates'
TEST_DATA = 'testData'

DEFAULT_TIME_OUT_IN_MINUTES = 5


class TestCase(object):
    """
    Abstract superclass of a test case. 
    Subclasses have to override execute() and optionally executeInDevMode(). 
    The test case is run by invoking runTest(). 
    Here is a skeleton of a test case:
    
    #!/usr/bin/python
    import settings
    import jenkinstest.testcase

    class TestCase(jenkinstest.testcase.TestCase):
    
        def execute(self):
            ....
            
        def executeInDevMode(self):
            ....
            
    TestCase(settings, __file__).runTest()

    There are two execution modes (controlled by command line option -d and -rd):
    
    Normal mode: 
        1. Cleans up playground: Kills running servers and deletes playground folder of this test case.
        2. Invokes execute() method.
        3. Release resources: Shuts down running servers.
        
    Developing mode:
        1. Invokes executeInDevMode() method.
        
    The developing mode allows to reuse already installed servers. 
    Servers might be restarted. This mode leads to fast development 
    of test code by doing incremental development. Working code
    can be moved from executeInDevMode() to execute(). 
    """

    def __init__(self, settings, filePath):
        self.artifactRepository = settings.REPOSITORY
        self.project = None
        fileName = os.path.basename(filePath)
        self.name = fileName[0:fileName.rfind('.')]
        self.playgroundFolder = "%s/%s" % (PLAYGROUND, self.name)
        self.numberOfFailures = 0
        self.devMode = settings.devMode
        self.runningInstances = []

    def runTest(self):
        """
        Runs this test case. This is a final method. It should not be overwritten.
        """
        startTime = time.time()
        util.printAndFlush("\n/''''''''''''''''''' %s started at %s %s ''''''''''"
                           % (self.name, time.strftime('%Y-%m-%d %H:%M:%S'),
                              'in DEV MODE' if self.devMode else ''))
        try:
            if not self.devMode:
                if os.path.exists(self.playgroundFolder):
                    self._cleanUpPlayground()
                os.makedirs(self.playgroundFolder)
                self.execute()
            else:
                self.executeInDevMode()
            success = self.numberOfFailures == 0
        except:
            util.printAndFlush(traceback.format_exc())
            success = False
        finally:
            duration = util.renderDuration(time.time() - startTime)
            if not self.devMode:
                self.releaseResources()
            if success:
                util.printAndFlush(
                    "\...........SUCCESS: %s executed in %s .........." % (self.name, duration))
            else:
                util.printAndFlush(
                    "\............FAILED: %s executed in %s .........." % (self.name, duration))
                raise Exception("%s failed" % self.name)

    def execute(self):
        """
        Executes this test case in normal mode. 
        This is an abstract method which has to be overwritten in subclasses.
        """
        pass

    def executeInDevMode(self):
        """
        Executes this test case in developing mode. 
        This method can be overwritten in subclasses.
        """
        pass

    def releaseResources(self):
        """
        Releases resources. It shuts down all running servers.
        This method can be overwritten in subclasses. 
        Note, this method can be invoked in subclasses as follows:
        
                super(type(self), self).releaseResources()
        
        """
        self._shutdownSevers()

    def assertPatternInLog(self, log, pattern):
        if not re.search(pattern, log):
            self.fail("Pattern doesn't match: %s" % pattern)

    def assertSmaller(self, itemName, expectedUpperLimit, actualValue, verbose=True):
        """
        Asserts that actualValue <= expectedUpperLimit. If not the test will be continued but counted as failed.
        Returns False if assertion fails otherwise True.
        """
        if actualValue > expectedUpperLimit:
            self.fail("%s\n  actual value <%s> exceeds the expected upper limit <%s>" % (
                itemName, actualValue, expectedUpperLimit))
            return False
        elif verbose:
            util.printAndFlush("%s actual value <%s> is below the expected upper limit <%s>" % (
                itemName, actualValue, expectedUpperLimit))
        return True

    def assertEquals(self, itemName, expected, actual, verbose=True):
        """
        Asserts that expected == actual. If not the test will be continued but counted as failed.
        Returns False if assertion fails otherwise True.
        """
        rendered_expected = self._render(expected)
        if expected != actual:
            rendered_actual = self._render(actual)
            diff = difflib.ndiff(rendered_expected.splitlines(), rendered_actual.splitlines())
            self.fail("%s\n  Differences:\n%s" % (itemName, '\n'.join(diff)))
            return False
        elif verbose:
            util.printAndFlush("%s as expected: <%s>" % (itemName, rendered_expected))
        return True

    def assertType(self, variableName, expectedType, variable):
        self.assertEquals("Type of %s" % variableName, expectedType, type(variable))

    def assertIn(self, itemsName, items, item):
        if item not in items:
            self.fail("Item %s not in %s" % (item, itemsName))
        util.printAndFlush("%s as expected: contains <%s>" % (itemsName, item))

    def assertNone(self, itemName, item):
        self.assertEquals(itemName, None, item)

    def assertNotNone(self, itemName, item):
        if item is None:
            self.fail("Item %s is None" % itemName)
        util.printAndFlush("%s as expected: not None" % itemName)

    def assertTrue(self, itemName, item):
        self.assertEquals(itemName, True, item)

    def assertFalse(self, itemName, item):
        self.assertEquals(itemName, False, item)

    def assertLength(self, itemsName, length, items):
        self.assertEquals("Length of %s" % itemsName, length, len(items))

    def assertEmpty(self, itemsName, items):
        self.assertLength(itemsName, 0, items)

    def assertNotEmpty(self, itemsName, items):
        if len(items) == 0:
            self.fail("%s is empty" % itemsName)
        util.printAndFlush("%s as expected: not empty" % itemsName)

    def _render(self, item):
        if not isinstance(item, list):
            return str(item)
        result = ""
        for e in item:
            if len(result) > 0:
                result += "\n"
            result += str(e)
        return result

    def fail(self, errorMessage):
        """
        Prints specified error message and mark test case as failed.
        """
        self.numberOfFailures += 1
        util.printWhoAmI(levels=10, template="ERROR found (caller chain: %s)")
        util.printAndFlush("ERROR causing test failure: %s" % errorMessage)

    def installScriptBasedServer(self, templateName, instanceName,
                                 startCommand=['./start.sh'], stopCommand=['./stop.sh']):
        installPath = self._getInstallPath(instanceName)
        if os.path.exists(installPath):
            shutil.rmtree(installPath)
        shutil.copytree("%s/%s" % (self.getTemplatesFolder(), templateName), installPath)
        return ScriptBasedServerController(self, self.name, installPath, instanceName, startCommand,
                                           stopCommand)

    def createScriptBasedServerController(self, instanceName, startCommand=['./start.sh'],
                                          stopCommand=['./stop.sh']):
        return ScriptBasedServerController(self, self.name, self._getInstallPath(instanceName),
                                           instanceName,
                                           startCommand, stopCommand)

    def installDatamover(self, instanceName='datamover'):
        zipFile = self.artifactRepository.getPathToArtifact(DATAMOVER_PROJECT, 'datamover')
        installPath = self._getInstallPath(instanceName)
        util.unzip(zipFile, self.playgroundFolder)
        os.rename("%s/datamover" % (self.playgroundFolder), installPath)
        return DatamoverController(self, self.name, installPath, instanceName)

    def createDatamoverController(self, instanceName='datamover'):
        return DatamoverController(self, self.name, self._getInstallPath(instanceName),
                                   instanceName)

    def installOpenbis(self, instanceName='openbis', technologies=[]):
        """
        Installs openBIS from the installer. 
        
        The instanceName specifies the subfolder in the playground folder 
        where the instance will be installed. 
        In addition it is also part of the database names.
        The technologies are an array of enabled technologies.
        """
        installerPath = self.artifactRepository.getPathToArtifact(INSTALLER_PROJECT,
                                                                  'openBIS-installation')
        installerFileName = os.path.basename(installerPath).rpartition('.tar')[0]
        util.executeCommand(['tar', '-zxf', installerPath, '-C', self.playgroundFolder],
                            "Couldn't untar openBIS installer.")
        consolePropertiesFile = "%s/%s/console.properties" % (
            self.playgroundFolder, installerFileName)
        consoleProperties = util.readProperties(consolePropertiesFile)
        installPath = self._getInstallPath(instanceName)
        consoleProperties['INSTALL_PATH'] = installPath
        consoleProperties['DSS_ROOT_DIR'] = "%s/data" % installPath
        for technology in technologies:
            consoleProperties[technology.upper()] = True
        print(f"CONSOLE_PROPERTIES:{consoleProperties}")
        util.writeProperties(consolePropertiesFile, consoleProperties)
        util.executeCommand("%s/%s/run-console.sh" % (self.playgroundFolder, installerFileName),
                            "Couldn't install openBIS", consoleInput='admin\nadmin')
        shutil.rmtree("%s/%s" % (self.playgroundFolder, installerFileName))

    def cloneOpenbisInstance(self, nameOfInstanceToBeCloned, nameOfNewInstance,
                             dataStoreServerOnly=False):
        """ Clones an openBIS instance. """

        oldInstanceInstallPath = "%s/%s" % (self.playgroundFolder, nameOfInstanceToBeCloned)
        newInstanceInstallPath = "%s/%s" % (self.playgroundFolder, nameOfNewInstance)
        paths = ['bin', 'data', 'servers/core-plugins', 'servers/datastore_server']
        if not dataStoreServerOnly:
            paths.append('servers/openBIS-server')
        for path in paths:
            util.copyFromTo(oldInstanceInstallPath, newInstanceInstallPath, path)
        dssPropsFile = "%s/servers/datastore_server/etc/service.properties" % newInstanceInstallPath
        dssProps = util.readProperties(dssPropsFile)
        dssProps['root-dir'] = dssProps['root-dir'].replace(nameOfInstanceToBeCloned,
                                                            nameOfNewInstance)
        util.writeProperties(dssPropsFile, dssProps)

    def createOpenbisController(self, instanceName='openbis', port='8443', dropDatabases=True,
                                databasesToDrop=[]):
        """
        Creates an openBIS controller object assuming that an openBIS instance for the specified name is installed.
        """
        return OpenbisController(self, self.name, self._getInstallPath(instanceName), instanceName,
                                 port,
                                 dropDatabases, databasesToDrop)

    def installScreeningTestClient(self):
        """ Installs the screening test client and returns an instance of ScreeningTestClient. """
        zipFile = self.artifactRepository.getPathToArtifact(OPENBIS_STANDARD_TECHNOLOGIES_PROJECT,
                                                            'openBIS-screening-API')
        installPath = "%s/screeningAPI" % self.playgroundFolder
        util.unzip(zipFile, installPath)
        return ScreeningTestClient(self, installPath)

    def installPybis(self):
        # install the local pybis in editable-mode (-e)
        util.executeCommand(['pip3', 'install', '-e', '../api-openbis-python3-pybis/src/python'],
                            "Installation of pybis failed.")

    def installObis(self):
        # install the local obis in editable-mode (-e)
        util.executeCommand(['pip3', 'install', '-e', '../app-openbis-command-line/src/python'],
                            "Installation of obis failed.")

    def getTemplatesFolder(self):
        return "%s/%s" % (TEMPLATES, self.name)

    def _getInstallPath(self, instanceName):
        return os.path.abspath("%s/%s" % (self.playgroundFolder, instanceName))

    def _cleanUpPlayground(self):
        for f in os.listdir(self.playgroundFolder):
            path = "%s/%s" % (self.playgroundFolder, f)
            if not os.path.isdir(path):
                continue
            util.printAndFlush("clean up %s" % path)
            util.killProcess("%s/servers/datastore_server/datastore_server.pid" % path)
            util.killProcess("%s/servers/openBIS-server/jetty/openbis.pid" % path)
            util.killProcess("%s/datamover.pid" % path)
        util.deleteFolder(self.playgroundFolder)

    def _addToRunningInstances(self, controller):
        self.runningInstances.append(controller)

    def _removeFromRunningInstances(self, controller):
        if controller in self.runningInstances:
            self.runningInstances.remove(controller)

    def _shutdownSevers(self):
        for instance in reversed(self.runningInstances):
            instance.stop()


class _Controller(object):
    def __init__(self, testCase, testName, installPath, instanceName):
        self.testCase = testCase
        self.testName = testName
        self.instanceName = instanceName
        self.installPath = installPath
        util.printAndFlush("Controller created for instance '%s'. Installation path: %s" % (
            instanceName, installPath))

    def createFolder(self, folderPath):
        """
        Creates a folder with specified path relative to installation directory.
        """
        path = "%s/%s" % (self.installPath, folderPath)
        os.makedirs(path)

    def assertEmptyFolder(self, pathRelativeToInstallPath):
        """
        Asserts that the specified path (relative to the installation path) is an empty folder.
        """
        relativePath = "%s/%s" % (self.installPath, pathRelativeToInstallPath)
        files = self._getFiles(relativePath)
        if len(files) == 0:
            util.printAndFlush("Empty folder as expected: %s" % relativePath)
        else:
            self.testCase.fail(
                "%s isn't empty. It contains the following files:\n  %s" % (relativePath, files))

    def assertFiles(self, folderPathRelativeToInstallPath, expectedFiles):
        """
        Asserts that the specified path (relative to the installation path) contains the specified files.
        """
        relativePath = "%s/%s" % (self.installPath, folderPathRelativeToInstallPath)
        files = self._getFiles(relativePath)
        self.testCase.assertEquals("Files in %s" % relativePath, expectedFiles, sorted(files))

    def _getFiles(self, relativePath):
        if not os.path.isdir(relativePath):
            self.testCase.fail("Doesn't exist or isn't a folder: %s" % relativePath)
        files = os.listdir(relativePath)
        return files


class ScriptBasedServerController(_Controller):
    def __init__(self, testCase, testName, installPath, instanceName, startCommand, stopCommand):
        super(ScriptBasedServerController, self).__init__(testCase, testName, installPath,
                                                          instanceName)
        self.startCommand = startCommand
        self.stopCommand = stopCommand

    def start(self):
        self.testCase._addToRunningInstances(self)
        util.executeCommand(self.startCommand, "Couldn't start server '%s'" % self.instanceName,
                            workingDir=self.installPath)

    def stop(self):
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand(self.stopCommand, "Couldn't stop server '%s'" % self.instanceName,
                            workingDir=self.installPath)


class DatamoverController(_Controller):
    def __init__(self, testCase, testName, installPath, instanceName):
        super(DatamoverController, self).__init__(testCase, testName, installPath, instanceName)
        self.servicePropertiesFile = "%s/etc/service.properties" % self.installPath
        self.serviceProperties = util.readProperties(self.servicePropertiesFile)
        self.serviceProperties['check-interval'] = 2
        self.serviceProperties['quiet-period'] = 5
        self.serviceProperties['inactivity-period'] = 15
        dataCompletedScript = "%s/%s/data-completed.sh" % (
            testCase.getTemplatesFolder(), instanceName)
        if os.path.exists(dataCompletedScript):
            self.serviceProperties['data-completed-script'] = "../../../../%s" % dataCompletedScript

    def setPrefixForIncoming(self, prefix):
        """ Set service property 'prefix-for-incoming'. """
        self.serviceProperties['prefix-for-incoming'] = prefix

    def setTreatIncomingAsRemote(self, flag):
        """ Set service property 'treat-incoming-as-remote'. """
        self.serviceProperties['treat-incoming-as-remote'] = flag

    def setOutgoingTarget(self, path):
        """ 
        Set service property 'outgoing-target'. 
        This has to be a path relative to installation path of the datamover. 
        """
        self.serviceProperties['outgoing-target'] = path

    def setExtraCopyDir(self, path):
        """ 
        Set service property 'extra-copy-dir'. 
        This has to be a path relative to installation path of the datamover. 
        """
        self.serviceProperties['extra-copy-dir'] = path

    def start(self):
        """ Starts up datamover server. """
        util.writeProperties(self.servicePropertiesFile, self.serviceProperties)
        self.testCase._addToRunningInstances(self)
        output = util.executeCommand(["%s/datamover.sh" % self.installPath, 'start'],
                                     suppressStdOut=True)
        joinedOutput = '\n'.join(output)
        if 'FAILED' in joinedOutput:
            util.printAndFlush(
                "Start up of datamover %s failed:\n%s" % (self.instanceName, joinedOutput))
            raise Exception("Couldn't start up datamover '%s'." % self.instanceName)

    def stop(self):
        """ Stops datamover server. """
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand(["%s/datamover.sh" % self.installPath, 'stop'],
                            "Couldn't shut down datamover '%s'." % self.instanceName)

    def drop(self, testDataSetName):
        """ Drops the specified test data set into incoming folder. """
        util.copyFromTo("%s/%s" % (TEST_DATA, self.testName), "%s/data/incoming" % self.installPath,
                        testDataSetName)


class ScreeningTestClient(object):
    """
    Class representing the screeing test client.
    """

    def __init__(self, testCase, installPath):
        self.testCase = testCase
        self.installPath = installPath

    def run(self):
        """ Runs the test client and returns the console output as a list of strings. """
        output = util.executeCommand(['java',
                                      '-Djavax.net.ssl.trustStore=../openbis/servers/openBIS-server/jetty/etc/openBIS.keystore',
                                      '-jar', 'openbis_screening_api.jar', 'admin', 'admin',
                                      'https://localhost:8443'], suppressStdOut=True,
                                     workingDir=self.installPath)
        with open("%s/log.txt" % self.installPath, 'w') as log:
            for line in output:
                log.write("%s\n" % line)
        return output


class DataSet(object):
    def __init__(self, resultSetRow):
        self.id = resultSetRow[0]
        self.dataStore = resultSetRow[1]
        self.experimentCode = resultSetRow[2]
        self.code = resultSetRow[3]
        self.type = resultSetRow[4]
        self.location = resultSetRow[5]
        self.status = resultSetRow[6]
        self.presentInArchive = resultSetRow[7]
        self.producer = resultSetRow[8]
        self.productionTimeStamp = resultSetRow[9]
        self.parents = []
        self.children = []

    def __str__(self):
        parents = [d.id for d in self.parents]
        children = [d.id for d in self.children]
        return "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s" % (
            self.id, self.dataStore, self.code, self.type,
            self.location, self.status, self.presentInArchive,
            parents, children, self.experimentCode,
            self.producer, self.productionTimeStamp)


class OpenbisController(_Controller):
    """
    Class to control AS and DSS of an installed openBIS instance.
    """

    def __init__(self, testCase, testName, installPath, instanceName, port='8443',
                 dropDatabases=True, databasesToDrop=[]):
        """
        Creates a new instance for specifies test case with specified test and instance name, installation path.
        """
        super(OpenbisController, self).__init__(testCase, testName, installPath, instanceName)
        self.templatesFolder = testCase.getTemplatesFolder()
        self.binFolder = "%s/bin" % installPath
        self.bisUpScript = "%s/bisup.sh" % self.binFolder
        self.bisDownScript = "%s/bisdown.sh" % self.binFolder
        self.dssUpScript = "%s/dssup.sh" % self.binFolder
        self.dssDownScript = "%s/dssdown.sh" % self.binFolder
        self.databaseKind = "%s_%s" % (testName, instanceName)
        self.asServicePropertiesFile = "%s/servers/openBIS-server/jetty/etc/service.properties" % installPath
        self.asProperties = None
        if os.path.exists(self.asServicePropertiesFile):
            self.asProperties = util.readProperties(self.asServicePropertiesFile)
            self.asProperties['database.kind'] = self.databaseKind
            self.asPropertiesModified = True
        self.dssServicePropertiesFile = "%s/servers/datastore_server/etc/service.properties" % installPath
        self.dssProperties = util.readProperties(self.dssServicePropertiesFile)
        self.dssProperties['path-info-db.databaseKind'] = self.databaseKind
        self.dssProperties['imaging-database.kind'] = self.databaseKind
        self.dssPropertiesModified = True
        self.passwdScript = "%s/servers/openBIS-server/jetty/bin/passwd.sh" % installPath
        if port != '8443':
            self.sslIniFile = "%s/servers/openBIS-server/jetty/start.d/ssl.ini" % installPath
            if os.path.exists(self.sslIniFile):
                self.sslIni = util.readProperties(self.sslIniFile)
                self.sslIni['jetty.ssl.port'] = port
                util.writeProperties(self.sslIniFile, self.sslIni)
        if dropDatabases:
            util.dropDatabase(PSQL_EXE, "openbis_%s" % self.databaseKind)
            util.dropDatabase(PSQL_EXE, "pathinfo_%s" % self.databaseKind)
            util.dropDatabase(PSQL_EXE, "imaging_%s" % self.databaseKind)
            self._setUpStore()
            self._setUpFileServer()
        for databaseToDrop in databasesToDrop:
            util.dropDatabase(PSQL_EXE, "%s_%s" % (databaseToDrop, self.databaseKind))
        self._applyCorePlugins()

    def setDummyAuthentication(self):
        """ Disables authentication. """
        self.asProperties['authentication-service'] = 'dummy-authentication-service'

    def setOpenbisPortDataStoreServer(self, port):
        as_url = self.dssProperties['server-url']
        util.printAndFlush('as_url' + as_url)
        parts = as_url.split(':')
        s = ""
        for idx, part in enumerate(parts):
            if (idx < len(parts) - 1):
                s = s + part + ":"
        self.dssProperties['server-url'] = s + port

    def setDataStoreServerCode(self, code):
        """ Sets the code of the Data Store Server. """
        self.dssProperties['data-store-server-code'] = code

    def getDataStoreServerCode(self):
        return self.dssProperties['data-store-server-code']

    def setDataStoreServerPort(self, port):
        """ Sets the port of the Data Store Server. """
        self.dssProperties['port'] = port

    def setDataStoreServerUsername(self, username):
        """ Sets the username of the Data Store Server. """
        self.dssProperties['username'] = username

    def setDataStoreServerProperty(self, prop, val):
        """ Can be used to set the value of any property in DSS service.properties """
        self.dssProperties[prop] = val

    def setAsMaxHeapSize(self, maxHeapSize):
        self._setMaxHeapSize("openBIS-server/jetty/etc/openbis.conf", maxHeapSize)

    def setDssMaxHeapSize(self, maxHeapSize):
        self._setMaxHeapSize("datastore_server/etc/datastore_server.conf", maxHeapSize)

    def enableProjectSamples(self):
        self.asProperties['project-samples-enabled'] = "true"

    def assertFileExist(self, pathRelativeToInstallPath):
        """
        Asserts that the specified path (relative to the installation path) exists.
        """
        relativePath = "%s/%s" % (self.installPath, pathRelativeToInstallPath)
        if os.path.exists(relativePath):
            util.printAndFlush("Path exists as expected: %s" % relativePath)
        else:
            self.testCase.fail("Path doesn't exist: %s" % relativePath)

    def assertDataSetContent(self, pathToOriginal, dataSet):
        path = "%s/data/store/1/%s/original" % (self.installPath, dataSet.location)
        path = "%s/%s" % (path, os.listdir(path)[0])
        numberOfDifferences = util.getNumberOfDifferences(pathToOriginal, path)
        if numberOfDifferences > 0:
            self.testCase.fail("%s differences found." % numberOfDifferences)

    def assertNumberOfDataSets(self, expectedNumberOfDataSets, dataSets):
        """
        Asserts that the specified number of data sets from the specified list of DataSet instances 
        are in the data store. 
        """
        count = 0
        for dataSet in dataSets:
            if dataSet.dataStore != self.getDataStoreServerCode() or dataSet.location == '':
                continue
            count += 1
            self.assertFileExist("data/store/1/%s" % dataSet.location)
        self.testCase.assertEquals(
            "Number of data sets in data store %s" % self.getDataStoreServerCode(),
            expectedNumberOfDataSets, count)

    def storeDirectory(self):
        """
        Return the path to the data/store directory
        """
        return "data/store"

    def getDataSets(self):
        """
        Returns all data sets as a list (ordered by data set ids) of instances of class DataSet.
        """
        resultSet = self.queryDatabase('openbis',
                                       "select data.id,ds.code,e.code,data.code,t.code,location,status,present_in_archive,"
                                       + "    data.data_producer_code,data.production_timestamp from data"
                                       + " left join external_data as ed on ed.id = data.id"
                                       + " join data_set_types as t on data.dsty_id = t.id"
                                       + " join experiments as e on data.expe_id = e.id"
                                       + " join data_stores as ds on data.dast_id = ds.id order by data.id")
        dataSets = []
        dataSetsById = {}
        for row in resultSet:
            dataSet = DataSet(row)
            dataSets.append(dataSet)
            dataSetsById[dataSet.id] = dataSet
        relationships = self.queryDatabase('openbis',
                                           "select data_id_parent, data_id_child from data_set_relationships"
                                           + " order by data_id_parent, data_id_child")
        for parent_id, child_id in relationships:
            parent = dataSetsById[parent_id]
            child = dataSetsById[child_id]
            parent.children.append(child)
            child.parents.append(parent)
        util.printAndFlush(
            "All data sets:\nid,dataStore,code,type,location,status,presentInArchive,parents,children,experiment,producer,productionTimeStamp")
        for dataSet in dataSets:
            util.printAndFlush(dataSet)
        return dataSets

    def createTestDatabase(self, databaseType):
        """
        Creates a test database for the specified database type.
        """
        database = "%s_%s" % (databaseType, self.databaseKind)
        scriptPath = "%s/%s.sql" % (self.templatesFolder, database)
        util.createDatabase(PSQL_EXE, database, scriptPath)

    def dropDatabase(self, databaseType):
        """
        Drops the database for the specified database type.
        """
        util.dropDatabase(PSQL_EXE, "%s_%s" % (databaseType, self.databaseKind))

    def queryDatabase(self, databaseType, queryStatement, showHeaders=False):
        """
        Executes the specified SQL statement for the specified database type. Result set is returned
        as a list of lists.
        """
        database = "%s_%s" % (databaseType, self.databaseKind)
        return util.queryDatabase(PSQL_EXE, database, queryStatement, showHeaders)

    def allUp(self):
        """ Starts up AS and DSS if not running. """
        if not util.isAlive("%s/servers/openBIS-server/jetty/openbis.pid" % self.installPath,
                            "openBIS.keystore"):
            self._saveAsPropertiesIfModified()
            util.executeCommand([self.bisUpScript],
                                "Starting up openBIS AS '%s' failed." % self.instanceName)
        self.dssUp()

    def stop(self):
        self.allDown()

    def allDown(self):
        """ Shuts down AS and DSS. """
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand([self.dssDownScript],
                            "Shutting down openBIS DSS '%s' failed." % self.instanceName)
        if self.asProperties:
            util.executeCommand([self.bisDownScript],
                                "Shutting down openBIS AS '%s' failed." % self.instanceName)

    def dssUp(self):
        """ Starts up DSS if not running. """
        if not util.isAlive("%s/servers/datastore_server/datastore_server.pid" % self.installPath,
                            "openBIS.keystore"):
            self._saveDssPropertiesIfModified()
            self.testCase._addToRunningInstances(self)
            util.executeCommand([self.dssUpScript],
                                "Starting up openBIS DSS '%s' failed." % self.instanceName)

    def dssDown(self):
        """ Shuts down DSS. """
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand([self.dssDownScript],
                            "Shutting down openBIS DSS '%s' failed." % self.instanceName)

    def dropAndWait(self, dataName, dropBoxName, numberOfDataSets=1,
                    timeOutInMinutes=DEFAULT_TIME_OUT_IN_MINUTES):
        """
        Drops the specified data into the specified drop box. The data is either a folder or a ZIP file
        in TEST_DATA/<test name>. A ZIP file will be unpacked in the drop box. After dropping the method waits
        until the specified number of data sets have been registered.
        """
        self.drop(dataName, dropBoxName)
        self.waitUntilDataSetRegistrationFinished(numberOfDataSets=numberOfDataSets,
                                                  timeOutInMinutes=timeOutInMinutes)

    def dataFile(self, dataName):
        """
        Returns the path to the given test data
        """
        return "%s/%s/%s" % (TEST_DATA, self.testName, dataName)

    def drop(self, dataName, dropBoxName):
        """
        Drops the specified test data into the specified drop box. The test data is either a folder or a ZIP file
        in TEST_DATA/<test name>. A ZIP file will be unpacked in the drop box. 
        """
        destination = "%s/data/%s" % (self.installPath, dropBoxName)
        self.dropIntoDestination(dataName, destination)

    def dropIntoDestination(self, dataName, destination):
        """
        Drops the specified test data into the destination. The test data is either a folder or a ZIP file
        in TEST_DATA/<test name>. A ZIP file will be unpacked in the drop box. 
        """
        testDataFolder = "%s/%s" % (TEST_DATA, self.testName)
        if dataName.endswith('.zip'):
            util.unzip("%s/%s" % (testDataFolder, dataName), destination)
        else:
            util.copyFromTo(testDataFolder, destination, dataName)

    def waitUntilDataSetRegistrationFinished(self, numberOfDataSets=1,
                                             timeOutInMinutes=DEFAULT_TIME_OUT_IN_MINUTES):
        """ Waits until the specified number of data sets have been registrated. """
        monitor = self.createLogMonior(timeOutInMinutes)
        monitor.addNotificationCondition(util.RegexCondition('Incoming Data Monitor'))
        monitor.addNotificationCondition(util.RegexCondition('post-registration'))
        numberOfRegisteredDataSets = 0
        while numberOfRegisteredDataSets < numberOfDataSets:
            condition1 = util.RegexCondition('Post registration of (\\d*). of \\1 data sets')
            condition2 = util.RegexCondition(
                'Paths inside data set .* successfully added to database')
            elements = monitor.waitUntilEvent(util.ConditionSequence([condition1, condition2]))
            numberOfRegisteredDataSets += int(elements[0][0])
            util.printAndFlush(
                "%d of %d data sets registered" % (numberOfRegisteredDataSets, numberOfDataSets))

    def waitUntilDataSetRegistrationFailed(self, timeOutInMinutes=DEFAULT_TIME_OUT_IN_MINUTES):
        """ Waits until data set registration failed. """
        self.waitUntilConditionMatched(util.EventTypeCondition('ERROR'), timeOutInMinutes)
        util.printAndFlush("Data set registration failed as expected.")

    def waitUntilConditionMatched(self, condition, timeOutInMinutes=DEFAULT_TIME_OUT_IN_MINUTES):
        """
        Waits until specified condition has been detected in DSS log.
        """
        monitor = self.createLogMonior(timeOutInMinutes)
        monitor.addNotificationCondition(util.RegexCondition('Incoming Data Monitor'))
        monitor.addNotificationCondition(util.RegexCondition('post-registration'))
        monitor.waitUntilEvent(condition)

    def createLogMonior(self, timeOutInMinutes=DEFAULT_TIME_OUT_IN_MINUTES):
        logFilePath = "%s/servers/datastore_server/log/datastore_server_log.txt" % self.installPath
        return util.LogMonitor("%s.DSS" % self.instanceName, logFilePath, timeOutInMinutes)

    def assertFeatureVectorLabel(self, featureCode, expectedFeatureLabel):
        data = self.queryDatabase('imaging',
                                  "select distinct label from feature_defs where code = '%s'" % featureCode)
        self.testCase.assertEquals("label of feature %s" % featureCode, [[expectedFeatureLabel]],
                                   data)

    def _applyCorePlugins(self):
        source = "%s/core-plugins/%s" % (self.templatesFolder, self.instanceName)
        if os.path.exists(source):
            corePluginsFolder = "%s/servers/core-plugins" % self.installPath
            destination = "%s/%s" % (corePluginsFolder, self.instanceName)
            shutil.rmtree(destination, ignore_errors=True)
            shutil.copytree(source, destination)
            self.enableCorePlugin(self.instanceName)

    def enableCorePlugin(self, pluginName):
        corePluginsFolder = "%s/servers/core-plugins" % self.installPath
        corePluginsPropertiesFile = "%s/core-plugins.properties" % corePluginsFolder
        corePluginsProperties = util.readProperties(corePluginsPropertiesFile)
        enabledModules = corePluginsProperties['enabled-modules']
        enabledModules = "%s, %s" % (enabledModules, pluginName) if len(
            enabledModules) > 0 else pluginName
        corePluginsProperties['enabled-modules'] = enabledModules
        util.writeProperties(corePluginsPropertiesFile, corePluginsProperties)

    def addUser(self, name, password):
        util.executeCommand([self.passwdScript, 'add', name, '-p', password],
                            "Could not add user '%s' to instance '%s'." % (name, self.instanceName))

    def _setUpStore(self):
        templateStore = "%s/stores/%s" % (self.templatesFolder, self.instanceName)
        if os.path.isdir(templateStore):
            storeFolder = "%s/data/store" % self.installPath
            util.printAndFlush("Set up initial data store by copying content of %s to %s" % (
                templateStore, storeFolder))
            shutil.rmtree(storeFolder, ignore_errors=True)
            shutil.copytree(templateStore, storeFolder)

    def _setUpFileServer(self):
        templateFileServer = "%s/file-servers/%s" % (self.templatesFolder, self.instanceName)
        if os.path.isdir(templateFileServer):
            fileServiceFolder = "%s/data/file-server" % self.installPath
            util.printAndFlush("Set up initial file server by copying content of %s to %s" % (
                templateFileServer, fileServiceFolder))
            shutil.rmtree(fileServiceFolder, ignore_errors=True)
            shutil.copytree(templateFileServer, fileServiceFolder)

    def _saveAsPropertiesIfModified(self):
        if self.asPropertiesModified:
            util.writeProperties(self.asServicePropertiesFile, self.asProperties)
            self.asPropertiesModified = False

    def _saveDssPropertiesIfModified(self):
        if self.dssPropertiesModified:
            util.writeProperties(self.dssServicePropertiesFile, self.dssProperties)
            self.dssPropertiesModified = False

    def _setMaxHeapSize(self, configFile, maxHeapSize):
        path = "%s/servers/%s" % (self.installPath, configFile)
        lines = []
        for line in util.getContent(path):
            if line.strip().startswith('JAVA_MEM_OPTS'):
                line = re.sub(r'(.*)-Xmx[^ ]+(.*)', r"\1-Xmx%s\2" % maxHeapSize, line)
            lines.append(line)
        with open(path, "w") as f:
            for line in lines:
                f.write("%s\n" % line)
