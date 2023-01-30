import unittest

from systemtest.artifactrepository import ArtifactRepository
from testcasewithfiles import TestCaseWithFiles


class ArtifactRepositoryTest(TestCaseWithFiles):
    def setUp(self):
        self.testRepository = ArtifactRepository(self.createPath("test-repository"))
    
    def test_clear(self):
        self.testRepository.clear()
        
if __name__ == '__main__':
    unittest.main()