#   Copyright ETH 2023 Zürich, Scientific IT Services
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

from .openbis_command import OpenbisCommand
from ..command_result import CommandResult
from ..utils import cd


class DownloadPhysical(OpenbisCommand):
    """
    Command to download physical files of a data set.
    """

    def __init__(self, dm, data_set_id, file):
        """
        :param dm: data management
        :param data_set_id: permId of the data set to be cloned
        """
        self.data_set_id = data_set_id
        self.files = [file] if file is not None else None
        self.load_global_config(dm)
        super(DownloadPhysical, self).__init__(dm)

    def run(self):
        if self.fileservice_url() is None:
            return CommandResult(returncode=-1,
                                 output="Configuration fileservice_url needs to be set for download.")

        data_set = self.openbis.get_dataset(self.data_set_id)
        files = self.files if self.files is not None else data_set.file_list

        with cd(self.data_mgmt.invocation_path):
            target_folder = data_set.download(files, destination=self.data_mgmt.invocation_path)
            return CommandResult(returncode=0, output="Files downloaded to: %s" % target_folder)
