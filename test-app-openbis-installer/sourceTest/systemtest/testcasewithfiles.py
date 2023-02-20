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
