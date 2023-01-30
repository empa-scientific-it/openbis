#!/usr/bin/python

import settings
import systemtest.testcase
import systemtest.util as util

import hashlib
import inspect
import os
import os.path
import shutil
import tarfile
import time

DATASET_MAPPING_DB = 'multi_dataset_archive'

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        openbisController = self.setUpAndStartOpenbis()
        self.registerOneDataSetAndWaitOnNothingArchived(openbisController)
        self.registerSecondDataSetAndWaitForArchiving(openbisController)
        self.registerThreeDataSetsAndCheckArchivingWithExperimentPolicyOnProjectLevel(openbisController)
        self.unarchiveOneDataSetFromAContainerWithTwoDataSets(openbisController)
        self.registerOneDataSetAndRequestArchiving(openbisController)
        self.checkFolders(openbisController)

    def executeInDevMode(self):
#        openbisController = self.createOpenbisController()
        openbisController = self.createOpenbisController(dropDatabases=False)
#        openbisController.allUp()
        self.registerOneDataSetAndRequestArchiving(openbisController)
#        self.checkFolders(openbisController)
        
    def setUpAndStartOpenbis(self):
        util.printWhoAmI()
        self.installOpenbis()
        openbisController = self.createOpenbisController(databasesToDrop=['openbis', 'pathinfo', DATASET_MAPPING_DB])
        openbisController.asProperties['project-samples-enabled'] = 'true'
        openbisController.dssProperties['data-set-command-queue-mapping'] = \
                'archiving:Archiving|Copying data sets to archive|Archiving Finalizer, unarchiving:Unarchiving'
        os.makedirs("%s/data/archive/tmp" % openbisController.installPath)
        os.makedirs("%s/data/archive/stage" % openbisController.installPath)
        os.makedirs("%s/data/archive/final" % openbisController.installPath)
        os.makedirs("%s/data/archive/backup" % openbisController.installPath)
        os.makedirs("%s/data/store/9" % openbisController.installPath)
        util.writeProperties("%s/data/store/9/share.properties" % openbisController.installPath, 
                             {'unarchiving-scratch-share':'true'})
        openbisController.createTestDatabase("openbis")
        openbisController.allUp()
        return openbisController
    
    def registerOneDataSetAndRequestArchiving(self, openbisController):
        util.printWhoAmI()
        self.dropExampleAndWait(openbisController, 'T1', '/SIS_ALL/P1/E1')
        self.waitForAutoArchiverToDoNothing(openbisController)
        data_set_codes = self.getDataSetCodesByExperiments(openbisController, ['E1'])
        self.requestToArchive(openbisController, data_set_codes)
        self.checkStatusAndGetLocation(openbisController, data_set_codes[0], 'AVAILABLE', 'f', 't', '1')
        self.waitForArchiving(openbisController, data_set_codes, 't')
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'ARCHIVED', 't', 'f', '1')
        self.assertDataSetsAreArchivedInSameContainer(openbisController, data_set_codes)

    def registerOneDataSetAndWaitOnNothingArchived(self, openbisController):
        util.printWhoAmI()
        self.dropExampleAndWait(openbisController, 'T1', '/S1/P1-1/E111')
        self.waitForAutoArchiverToDoNothing(openbisController)
        data_set_codes = self.getDataSetCodes(openbisController)
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'AVAILABLE', 'f', 'f', '1')
        self.triggerArchiving(openbisController, data_set_codes)
        self.waitForAutoArchiverToDoNothing(openbisController)

    def registerSecondDataSetAndWaitForArchiving(self, openbisController):
        util.printWhoAmI()
        self.dropExampleAndWait(openbisController, 'T1', '/S1/P1-1/E111')
        data_set_codes = self.getDataSetCodes(openbisController)
        self.waitForAutoArchiverToDoNothing(openbisController)
        self.triggerArchiving(openbisController, self.getDataSetCodes(openbisController))
        self.waitForArchiving(openbisController, data_set_codes, 'f')
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'ARCHIVED', 't', 'f', '1')
        self.assertDataSetsAreArchivedInSameContainer(openbisController, data_set_codes)

    def registerThreeDataSetsAndCheckArchivingWithExperimentPolicyOnProjectLevel(self, openbisController):
        util.printWhoAmI()
        self.dropExample(openbisController, 'T2', '/S1/P1-1/E112')
        self.dropExample(openbisController, 'T1', '/S1/P2-1/E122')
        self.dropExample(openbisController, 'T2', '/S1/P2-1/E121')
        openbisController.waitUntilDataSetRegistrationFinished(numberOfDataSets=3)
        data_set_codes = self.getDataSetCodesByExperiments(openbisController, ['E121', 'E122'])
        self.waitForAutoArchiverToDoNothing(openbisController)
        self.triggerArchiving(openbisController, self.getDataSetCodes(openbisController))
        self.waitForArchiving(openbisController, data_set_codes, 'f')
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'ARCHIVED', 't', 'f', '1')
        self.assertDataSetsAreArchivedInSameContainer(openbisController, data_set_codes)
        data_set_codes = self.getDataSetCodesByExperiments(openbisController, ['E112'])
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'AVAILABLE', 'f', 'f', '1')

    def unarchiveOneDataSetFromAContainerWithTwoDataSets(self, openbisController):
        util.printWhoAmI()
        data_set_codes = self.getDataSetCodesByExperiments(openbisController, ['E121', 'E122'])
        self.unarchive(openbisController, data_set_codes[1:])
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'AVAILABLE', 't', 'f', '9')


    def triggerArchiving(self, openbisController, data_set_codes):
        timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time() - 2 * 24 * 60 * 60))
        util.printAndFlush("Set access_timestamp for data sets %s to '%s'" % (data_set_codes, timestamp))
        openbisController.queryDatabase("openbis", ("update data set access_timestamp = '%s' " +
                                        "where code in ('%s')") % (timestamp, "', '".join(data_set_codes)))

    def dropExampleAndWait(self, openbisController, dataSetType, identifier):
        self.dropExample(openbisController, dataSetType, identifier)
        openbisController.waitUntilDataSetRegistrationFinished()

    def dropExample(self, openbisController, dataSetType, identifier):
        destination = "%s/data/incoming-simple/%s %s" % (openbisController.installPath, dataSetType, 
                                                         identifier.replace('/', ':'))
        openbisController.dropIntoDestination('data-example', destination)

    def requestToArchive(self, openbisController, data_set_codes):
        self.performArchivingOperation(openbisController, 'requestToArchive', data_set_codes)
        operation = 'requestToArchive'
        util.printAndFlush("Waiting for archiving operation '%s' is finshed." % operation)
        monitor = util.LogMonitor("%s.DSS" % openbisController.instanceName, 
                                  "%s/servers/datastore_server/log/datastore_server_log.txt" % openbisController.installPath)
        monitor.addNotificationCondition(util.RegexCondition('\[archiving - '))
        monitor.waitUntilEvent(util.RegexCondition('Successfully committed transaction'))

    def unarchive(self, openbisController, data_set_codes):
        self.performArchivingOperationAndWait(openbisController, 'unarchive', data_set_codes)

    def performArchivingOperationAndWait(self, openbisController, operation, data_set_codes):
        self.performArchivingOperation(openbisController, operation, data_set_codes)
        util.printAndFlush("Waiting for archiving operation '%s' is finshed." % operation)
        monitor = util.LogMonitor("%s.DSS" % openbisController.instanceName, 
                                  "%s/servers/datastore_server/log/datastore_server_log.txt" % openbisController.installPath)
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.MultiDataSet'))
        monitor.waitUntilEvent(util.RegexCondition('changed status to'))
        
    def performArchivingOperation(self, openbisController, operation, data_set_codes):
        destination = "%s/data/%s" % (openbisController.installPath, 'incoming-archiving')
        name = operation
        for data_set_code in data_set_codes:
            name = "%s %s" % (name, data_set_code)
        util.printAndFlush(name)
        open("%s/%s" % (destination, name), 'a').close()
        
    def assertDataSetsAreArchivedInSameContainer(self, openbisController, data_set_codes):
        resultSet = openbisController.queryDatabase(DATASET_MAPPING_DB, 
                                ("select distinct ctnr_id from data_sets " +
                                 "where code in ('%s')" % "', '".join(data_set_codes)))
        self.assertEquals("Number of containers for data sets " % data_set_codes, '1', str(len(resultSet)))
        
    def checkFolders(self, openbisController):
        self.assertEquals("Stage folder", [], self._listFiles(openbisController, 'stage'))
        self.assertEquals("Deletion request folder", [], self._listFiles(openbisController, 'deletion-requests'))
        archivesInFinal = self._listFiles(openbisController, 'final')
        self.assertEquals("Number of archives in final", 3, len(archivesInFinal))
        self.assertEquals("Archives in backup", archivesInFinal, self._listFiles(openbisController, 'backup'))

    def checkStatusAndDatabase(self, openbisController, data_set_codes, expected_status, 
                               expected_in_archive_flag, expected_archiving_requested, expected_share_id):
        example_file = 'data-example/R_inferno.pdf'
        example_file_path = openbisController.dataFile(example_file)
        expected_file_size = str(os.path.getsize(example_file_path))
        expected_md5 = self.calculateMD5(example_file_path)
        for data_set_code in data_set_codes:
            location = self.checkStatusAndGetLocation(openbisController, data_set_code, expected_status, 
                                                      expected_in_archive_flag, expected_archiving_requested, expected_share_id)
            path_in_store = "%s/data/store/%s/%s" % (openbisController.installPath, expected_share_id, location)
            if expected_status in ['AVAILABLE', 'ARCHIVE_PENDING']:
                if os.path.exists(path_in_store):
                    util.printAndFlush("Data set %s is in store as expected." % data_set_code)
                    self.assertEquals("MD5 of %s in %s" % (example_file, path_in_store), 
                                      expected_md5, 
                                      self.calculateMD5("%s/original/data/%s" % (path_in_store, example_file)))
                else:
                    self.fail("Data set %s is not in store: %s" % (data_set_code, path_in_store))
            else:
                if os.path.exists(path_in_store):
                    self.fail("Data set %s still in store: %s" % (data_set_code, path_in_store))
                else:
                    util.printAndFlush("Data set %s not in store as expected." % data_set_code)
            if expected_in_archive_flag == 't':
                resultSet = openbisController.queryDatabase(DATASET_MAPPING_DB, 
                                    ("select path,size_in_bytes " + 
                                     "from data_sets ds join containers c on ds.ctnr_id = c.id " +
                                     "where code = '%s'" % data_set_code))
                self.assertEquals("Number of data sets with code %s found in data set mapping database" % data_set_code, 
                                  "1", str(len(resultSet)))
                self.assertEquals("File size", expected_file_size, resultSet[0][1])
                tar_file = "%s/data/archive/final/%s" % (openbisController.installPath, resultSet[0][0])
                path_in_tar = "%s/original/data/%s" % (data_set_code, example_file)
                extracted_file = self.extractFromArchive(openbisController, tar_file, path_in_tar)
                self.assertEquals("MD5 of %s in %s" % (example_file, tar_file), 
                                  expected_md5, self.calculateMD5(extracted_file))
        
    def checkStatusAndGetLocation(self, openbisController, data_set_code, expected_status, 
                                  expected_in_archive_flag, expected_archiving_requested, expected_share_id):
        resultSet = openbisController.queryDatabase('openbis', 
                        "select share_id, location, status, present_in_archive, archiving_requested from data" + 
                        " left join external_data as ed on ed.id = data.id" + " where code = '%s'" % data_set_code)
        self.assertEquals("Number of data sets with code %s found in openbis database" % data_set_code, 
                          "1", str(len(resultSet)))
        share_id = resultSet[0][0]
        location = resultSet[0][1]
        status = resultSet[0][2]
        present_in_archive = resultSet[0][3]
        archiving_requested = resultSet[0][4]
        self.assertEquals("Share id", expected_share_id, share_id)
        self.assertEquals("Status", expected_status, status)
        self.assertEquals("Archiving-requested flag", expected_archiving_requested, archiving_requested)
        self.assertEquals("Present-in-archive flag", expected_in_archive_flag, present_in_archive)
        return location

    def extractFromArchive(self, openbisController, tar_file, path_in_tar):
        temp_dir = "%s/data/tmp" % (openbisController.installPath)
        util.printAndFlush("Extract %s from archive %s" % (path_in_tar, tar_file))
        try:
            tf = tarfile.open(tar_file)
            tf.extract(path_in_tar, temp_dir)
        finally:
            tf.close()
        return "%s/%s" % (temp_dir, path_in_tar)
        
    def calculateMD5(self, file_path):
        md5 = hashlib.md5()
        with open(file_path, "rb") as f:
            buffer = f.read(65536)
            while len(buffer) > 0:
                md5.update(buffer)
                buffer = f.read(65536)
        return md5.hexdigest()
        
    def waitForAutoArchiverToDoNothing(self, openbisController):
        util.printAndFlush("Waiting for auto archiver to do nothing")
        monitor = openbisController.createLogMonior()
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.AutoArchiverTask'))
        monitor.waitUntilEvent(util.RegexCondition("OPERATION.AutoArchiverTask - nothing to archive"))
        
    def waitForArchiving(self, openbisController, data_set_codes, expected_archiving_requested):
        util.printAndFlush("Waiting for finish first archiving step")
        monitor = self._createArchivingMonitor(openbisController)
        monitor.waitUntilEvent(util.RegexCondition("Finished executing 'Archiving'"))
        
        monitoringStartTime = time.time()
        self.checkStatusAndDatabase(openbisController, data_set_codes, 'ARCHIVE_PENDING', 'f', expected_archiving_requested, '1')
        
        util.printAndFlush("Waiting for replication of archive failed")
        monitor = self._createArchivingMonitor(openbisController)
        monitor.waitUntilEvent(util.RegexCondition("Schedule for deletion: (.*)"), startTime = monitoringStartTime)
        
        util.printAndFlush("Waiting for replication of archive started to wait")
        monitor = self._createArchivingMonitor(openbisController)
        archiveInFinal, = monitor.waitUntilEvent(util.RegexCondition("Waiting for replication of archive '(.*)'"))
        
        monitoringStartTime = time.time()
        archiveInBackup = archiveInFinal.replace("final", "backup")
        dir, _ = os.path.split(archiveInBackup)
        if not os.path.isdir(dir):
            os.mkdir(dir)
        util.printAndFlush("Copy %s to %s" % (archiveInFinal, dir))
        shutil.copy(archiveInFinal, dir)
            
        util.printAndFlush("Waiting for archiving finished")
        monitor = self._createArchivingMonitor(openbisController)
        monitor.waitUntilEvent(util.RegexCondition("Finished executing 'Archiving Finalizer'"), startTime = monitoringStartTime)
        
    def _createArchivingMonitor(self, openbisController):
        monitor = openbisController.createLogMonior()
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.AutoArchiverTask'))
        monitor.addNotificationCondition(util.RegexCondition('GroupingPolicy'))
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.MultiDataSet'))
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.FileDeleter'))
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.DataSetCommandExecutor'))
        return monitor

    def _listFiles(self, openbisController, subfolder):
        list = []
        for _, _, files in os.walk("%s/data/archive/%s" % (openbisController.installPath, subfolder)):
            list = list + files
        return sorted(list)

    def getDataSetCodesByExperiments(self, openbisController, experiment_codes):
        codes = set(experiment_codes)
        return [data_set.code for data_set in openbisController.getDataSets() 
                if data_set.type.startswith('T') and data_set.experimentCode in codes]
        
    def getDataSetCodes(self, openbisController):
        return [data_set.code for data_set in openbisController.getDataSets() if data_set.type.startswith('T')]
        
TestCase(settings, __file__).runTest()
