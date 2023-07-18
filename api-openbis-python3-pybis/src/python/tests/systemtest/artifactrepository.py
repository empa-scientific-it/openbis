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

import os.path
import re
import xml.dom.minidom
from urllib.request import urlopen

from util import printAndFlush


class ArtifactRepository:
    """
    Abstract artifact repository which keeps artifacts in a local repository folder.
    The main method is getPathToArtifact() which returns the path to the requested artifact in the repository.
    Concrete subclasses have to implement downloadArtifact().
    """

    def __init__(self, localRepositoryFolder):
        """
        Creates a new instance for the specified folder. The folder will be created if it does not exist. 
        """
        self.localRepositoryFolder = localRepositoryFolder
        if not os.path.exists(localRepositoryFolder):
            os.makedirs(localRepositoryFolder)
        printAndFlush("Artifact repository: %s" % localRepositoryFolder)

    def clear(self):
        """
        Removes all artifacts in the local repository folder.
        """
        for f in os.listdir(self.localRepositoryFolder):
            path = "%s/%s" % (self.localRepositoryFolder, f)
            if os.path.isfile(path):
                os.remove(path)
        printAndFlush("Artifact repository cleared.")

    def getPathToArtifact(self, project, pattern='.*'):
        """
        Returns the path to artifact requested by the specified pattern and project.
        The pattern is a regular expression which has to match the beginning of the artifact file name.
        The project specifies the project on CI server to download the artifact.
        
        An Exception is raised if non or more than one artifact matches the pattern.  
        """
        files = [f for f in os.listdir(self.localRepositoryFolder) if re.match(pattern, f)]
        if len(files) > 1:
            raise Exception("More than one artifact in '%s' matches the pattern '%s': %s"
                            % (self.localRepositoryFolder, pattern, files))
        if len(files) == 0:
            f = self.downloadArtifact(project, pattern)
        else:
            f = files[0]
        return "%s/%s" % (self.localRepositoryFolder, f)

    def downloadArtifact(self, project, pattern):
        """
        Abstract method which needs to be implemented by subclasses.
        """
        pass

    def _download(self, readHandle, fileName):
        filePath = "%s/%s" % (self.localRepositoryFolder, fileName)
        writeHandle = open(filePath, 'wb')
        try:
            blockSize = 8192
            while True:
                dataBlock = readHandle.read(blockSize)
                if not dataBlock:
                    break
                writeHandle.write(dataBlock)
        finally:
            writeHandle.close()


class JenkinsArtifactRepository(ArtifactRepository):
    """
    Artifact repository for a CI server based on Jenkins.
    """

    def __init__(self, baseUrl, localRepositoryFolder):
        """
        Creates a new instance for the specified server URL and local repository.
        """
        ArtifactRepository.__init__(self, localRepositoryFolder)
        self.baseUrl = baseUrl

    def downloadArtifact(self, project, pattern):
        """
        Downloads the requested artifact from Jenkins. It uses the Jenkins API.
        """
        projectUrl = "%s/job/%s" % (self.baseUrl, project)
        apiUrl = "%s/lastSuccessfulBuild/api/xml?xpath=//artifact&wrapper=bag" % projectUrl
        printAndFlush("Get artifact info from %s" % apiUrl)
        handle = urlopen(apiUrl)  # urllib.urlopen(apiUrl)
        url = None
        fileName = None
        dom = xml.dom.minidom.parseString(handle.read())
        for element in dom.getElementsByTagName('artifact'):
            elementFileName = element.getElementsByTagName('fileName')[0].firstChild.nodeValue
            if re.match(pattern, elementFileName):
                if fileName != None:
                    raise Exception(
                        "Pattern '%s' matches at least two artifacts in project '%s': %s and %s"
                        % (pattern, project, fileName, elementFileName))
                fileName = elementFileName
                relativePath = element.getElementsByTagName('relativePath')[0].firstChild.nodeValue
                url = "%s/lastSuccessfulBuild/artifact/%s" % (projectUrl, relativePath)
        if url == None:
            raise Exception(
                "For pattern '%s' no artifact found in project '%s'." % (pattern, project))
        printAndFlush("Download %s to %s." % (url, self.localRepositoryFolder))
        self._download(urlopen(url), fileName)
        return fileName


class GitArtifactRepository(ArtifactRepository):
    """
    Artifact repository for a git projects.
    """

    def __init__(self, localRepositoryFolder, host='github.com'):
        ArtifactRepository.__init__(self, localRepositoryFolder)
        self.host = host

    def downloadArtifact(self, project, pattern):
        url = "https://%s/%s/archive/%s" % (self.host, project, pattern)
        printAndFlush("Download %s to %s." % (url, self.localRepositoryFolder))
        self._download(urlopen(url), pattern)
        return pattern
