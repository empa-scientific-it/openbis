import os
import os.path
import shutil
import unittest

class TestCaseWithFiles(unittest.TestCase):
    workspace = 'targets/python-test-workspace'
    
    def setUp(self):
        shutil.rmtree("%s/%s" % (self.workspace, self.__class__.__name__))
    
    def createPath(self, relativePath):
        path = "%s/%s/%s" % (self.workspace, self.__class__.__name__, relativePath)
        parent = os.path.dirname(path)
        if not os.path.exists(parent):
            os.makedirs(parent)
        return path
