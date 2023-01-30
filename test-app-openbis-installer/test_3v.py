#!/usr/bin/python
import os
import shutil
import settings
import systemtest.testcase
import systemtest.util as util
from systemtest.testcase import TEST_DATA

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis('openbis1')
        self.cloneOpenbisInstance('openbis1', 'openbis2', dataStoreServerOnly=True)
        openbis1 = self.createOpenbisController('openbis1')
        openbis1.asProperties['project-samples-enabled'] = 'true'
        openbis1.setDummyAuthentication()
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.createTestDatabase('openbis')
        openbis2 = self.createOpenbisController('openbis2')
        openbis2.setDataStoreServerCode('DSS2')
        openbis2.setDataStoreServerPort('8445')
        openbis2.setDataStoreServerUsername('etlserver2')
        openbis1.allUp()
        openbis2.dssUp()
        datamover1 = self.installDatamover("datamover1")
        datamover1.setPrefixForIncoming('microX_200801011213_')
        datamover1.setTreatIncomingAsRemote(True)
        datamover1.setOutgoingTarget('../openbis1/data/incoming-raw')
        datamover1.setExtraCopyDir('data/extra_local_copy')
        datamover2 = self.installDatamover("datamover2")
        datamover2.setOutgoingTarget('../openbis1/data/incoming-analysis')
        os.makedirs("%s/data/drop-box1" % openbis2.installPath)
        os.makedirs("%s/data/drop-box2" % openbis2.installPath)
        dummyImageAnalyser = self.installScriptBasedServer('dummy-img-analyser', 'dummy-img-analyser')
        datamover1.start()
        datamover2.start()
        dummyImageAnalyser.start()
        
        datamover1.drop("3VCP1")
        openbis1.waitUntilDataSetRegistrationFinished(2)
        
        datamover1.drop("3VCP1")
        openbis1.waitUntilDataSetRegistrationFailed()
        
        datamover1.drop("3VCP3")
        openbis1.waitUntilDataSetRegistrationFinished(3)
        
        datamover1.drop("UnknownPlate")
        openbis1.waitUntilDataSetRegistrationFailed()
        
        with open("%s/data/incoming/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3.txt" % openbis2.installPath, "w") as f:
            f.write("hello world")
        openbis2.waitUntilDataSetRegistrationFinished()
        
        
        datamover1.assertEmptyFolder('data/incoming')
        datamover1.assertEmptyFolder('data/extra_local_copy')
        datamover2.assertEmptyFolder('data/incoming')
        openbis1.assertFiles('data/incoming-raw', 
                             ['.MARKER_is_finished_microX_200801011213_3VCP1', '.faulty_paths', 
                              'microX_200801011213_3VCP1'])
        openbis1.assertEmptyFolder('data/incoming-analysis')
        openbis2.assertEmptyFolder('data/incoming')
        dataCompletedLog = util.getContent("%s/data-completed-info.txt" % datamover1.installPath)
        self.assertEquals("data completion log", 
                          ['Data complete: /data/incoming/3VCP1', 'Data complete: /data/incoming/3VCP1', 
                           'Data complete: /data/incoming/3VCP3', 'Data complete: /data/incoming/UnknownPlate'], 
                          [l.replace(datamover1.installPath, '') for l in dataCompletedLog])
        
        dataSets = openbis1.getDataSets()
        openbis1.assertNumberOfDataSets(5, dataSets)
        openbis2.assertNumberOfDataSets(1, dataSets)
        self.assertEquals("Experiments", ['EXP1'] * 6, [d.experimentCode for d in dataSets])
        self.assertEquals("Producers", ['microX'] * 2, [d.producer for d in dataSets if d.code.startswith('MIC')])
        self.assertEquals("Production timestamp", ['2008-01-01 12:13:00+01'] * 2, 
                          [d.productionTimeStamp for d in dataSets if d.code.startswith('MIC')])
        dataSet1 = self.getDataSetByCode(dataSets, 'MICROX-3VCP1')
        openbis1.assertDataSetContent("%s/%s" % (TEST_DATA, '3VCP1'), dataSet1)
        dataSet2 = self.getDataSetByCode(dataSets, 'MICROX-3VCP3')
        openbis1.assertDataSetContent("%s/%s" % (TEST_DATA, '3VCP3'), dataSet2)
        child = dataSet1.children[0]
        self.assertEquals("data set type", 'HCS_IMAGE', child.type)
        self.assertEquals("child of %s" % dataSet2.code, child, dataSet2.children[0])
        count = 0
        for dataSet in dataSets:
            if dataSet.type != 'HCS_IMAGE_ANALYSIS_DATA':
                continue
            count += 1
            path = "%s/data/store/1/%s/original" % (openbis1.installPath, dataSet.location)
            code = os.listdir(path)[0].split('_')[-1]
            openbis1.assertDataSetContent("%s/%s" % (TEST_DATA, code), dataSet)
        self.assertEquals("number of analysis data sets", 3, count)
        self.assertUnidentified(openbis1, 'HCS_IMAGE')
        self.assertUnidentified(openbis1, 'HCS_IMAGE_ANALYSIS_DATA')
        content = util.getContent("%s/data/store/1/%s/original/nemo.exp1_MICROX-3VCP1.MICROX-3VCP3.txt" 
                                  % (openbis2.installPath, dataSets[-1].location))
        self.assertEquals("parent data set content", ['hello world'], content)
        self.assertDropBoxDrop(openbis2, 'drop-box1')
        self.assertDropBoxDrop(openbis2, 'drop-box2')
        
    def assertDropBoxDrop(self, openbisController, dropbox):
        path = "%s/data/%s" % (openbisController.installPath, dropbox)
        content = util.getContent("%s/%s" % (path, os.listdir(path)[0]))
        self.assertEquals("Content of %s" % dropbox, ['hello world'], content)
        
    def assertUnidentified(self, openbisController, type):
        path = "%s/data/store/1/unidentified/DataSetType_%s" % (openbisController.installPath, type)
        n = util.getNumberOfDifferences("%s/UnknownPlate" % TEST_DATA, "%s/%s" % (path, os.listdir(path)[0]))
        if n > 0:
            self.fail("unidentified data set of type %s not as expected" % type)
        

    def getDataSetByCode(self, dataSets, code):
        for dataSet in dataSets:
            if dataSet.code == code:
                return dataSet
        self.fail("No data set with code %s found." % code)
        
TestCase(settings, __file__).runTest()