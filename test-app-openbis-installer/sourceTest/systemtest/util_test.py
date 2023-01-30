import time
import unittest

import systemtest.util as util
from testcasewithfiles import TestCaseWithFiles


class UtilTest(TestCaseWithFiles):
    def test_readProperties(self):
        example = self.createPath("my.properties")
        with open(example, "w") as out:
            out.write("# a comment\n\n")
            out.write("      \n")
            out.write(" alpha = beta  \n")
            out.write("  non=\n")
            
        keyValuePairs = util.readProperties(example)
        
        self.assertEqual('beta', keyValuePairs['alpha'])
        self.assertEqual('', keyValuePairs['non'])
        self.assertEqual(2, len(keyValuePairs))
        
    def test_writeProperties(self):
        example = self.createPath("my.props")
        
        util.writeProperties(example, {'alpha': 4711, 'beta': 'hello'})
        
        with open(example, "r") as f:
            self.assertEqual(['alpha=4711\n', 'beta=hello\n'], sorted(f.readlines()))
        

    def test_LogMonitor(self):
        logFile = self._createExampleLog()
        monitor = self._createMonitor(logFile)
        monitor.addNotificationCondition(util.RegexCondition('.*'))
        
        elements = monitor.waitUntilEvent(util.RegexCondition('Post registration of (\\d*). of \\1 data sets'))
        
        self.assertEquals(('2',), elements)
        self.assertEqual(['\n>>>>> Start monitoring TEST log at 2013-10-01 10:50:00 >>>>>>>>>>>>>>>>>>>>', 
                          '>> 2013-10-01 10:50:00,025 WARN  [qtp797130442-28] OPERATION', 
                          '>> 2013-10-01 10:50:20,559 INFO  blabla', 
                          '>> 2013-10-01 10:50:25,559 INFO  Post registration of 1. of 2 data sets',
                          '>> 2013-10-01 10:50:30,559 INFO  Post registration of 2. of 2 data sets',
                          '>>>>> Finished monitoring TEST log >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'], 
                         monitor.printer.recorder)

    def test_LogMonitor_for_error_event(self):
        logFile = self._createLogFromEvents(['2013-10-01 10:50:00,025 WARN  [qtp797130442-28] OPERATION',
                                             'ch.systemsx.cisd.common.exceptions.UserFailureException: Experiment',
                                             '2013-10-01 10:50:20,559 ERROR test',
                                             '2013-10-01 10:50:20,559 INFO  blabla']);
        monitor = self._createMonitor(logFile)
        monitor.addNotificationCondition(util.RegexCondition('.*'))
        
        elements = monitor.waitUntilEvent(util.EventTypeCondition('ERROR'))
        
        self.assertEquals((), elements)
        self.assertEqual(['\n>>>>> Start monitoring TEST log at 2013-10-01 10:50:00 >>>>>>>>>>>>>>>>>>>>', 
                          '>> 2013-10-01 10:50:00,025 WARN  [qtp797130442-28] OPERATION', 
                          '>> 2013-10-01 10:50:20,559 ERROR test',
                          '>>>>> Finished monitoring TEST log >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'], 
                         monitor.printer.recorder)

    def test_LogMonitor_timeout(self):
        logFile = self._createExampleLog()
        monitor = self._createMonitor(logFile)
        
        try:
            monitor.waitUntilEvent(util.StartsWithCondition('Too late'))
            self.fail('Exception expected')
        except Exception as e:
            self.assertEqual('Time out after 1 minutes for monitoring TEST log.', str(e))
        self.assertEqual(['\n>>>>> Start monitoring TEST log at 2013-10-01 10:50:00 >>>>>>>>>>>>>>>>>>>>', 
                          '>>>>> Finished monitoring TEST log >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>'], 
                         monitor.printer.recorder)

    def _createExampleLog(self):
        return self._createLogFromEvents(['2013-10-01 10:40:20,559 INFO  blabla',
                                          '2013-10-01 10:50:00,025 WARN  [qtp797130442-28] OPERATION',
                                          'ch.systemsx.cisd.common.exceptions.UserFailureException: Experiment',
                                          '2013-10-01 10:50:20,559 INFO  blabla',
                                          '2013-10-01 10:50:25,559 INFO  Post registration of 1. of 2 data sets',
                                          '2013-10-01 10:50:30,559 INFO  Post registration of 2. of 2 data sets',
                                          '2013-10-01 10:50:40,559 INFO  blabla',
                                          '2013-10-01 10:50:50,559 INFO  blabla',
                                          '2013-10-01 10:51:30,559 INFO  Too late'])
    
    def _createLogFromEvents(self, logEvents):
        logFile = self.createPath("log.txt")
        with open(logFile, 'w') as f:
            for event in logEvents:
                f.write("%s\n" % event)
        return logFile
        
        
    def _createMonitor(self, logFile):
        class MockTimeProvider:
            def __init__(self):
                self.t = time.mktime(time.strptime('2013-10-01 10:50:00', '%Y-%m-%d %H:%M:%S'))
                self.deltaT = 10
                
            def time(self):
                oldT = self.t
                self.t += self.deltaT
                return oldT
        class MockPrinter:
            def __init__(self):
                self.recorder = []
            def printMsg(self, msg):
                self.recorder.append(msg)
        monitor = util.LogMonitor('TEST', logFile, timeOutInMinutes=1)
        monitor.timeProvider = MockTimeProvider()
        monitor.printer = MockPrinter()
        return monitor
        
if __name__ == '__main__':
    unittest.main()