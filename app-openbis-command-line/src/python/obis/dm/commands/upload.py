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
from ...scripts.click_util import click_echo


class Upload(OpenbisCommand):
    """
    Command to upload physical files to form a data set.
    """

    def __init__(self, dm, sample_id, data_set_type, files):
        """
        :param dm: data management
        :param sample_id: permId or sample path of the parent sample
        :param data_set_type: type of newly created data set.
        :param files: list of files/directories to upload
        """
        self.data_set_type = data_set_type
        self.files = files
        self.sample_id = sample_id
        self.load_global_config(dm)
        super(Upload, self).__init__(dm)

    def run(self):
        with cd(self.data_mgmt.invocation_path):
            click_echo(f"Uploading files {self.files} under {self.sample_id}")
            ds = self.openbis.new_dataset(type=self.data_set_type, sample=self.sample_id,
                                          files=self.files)
            result = ds.save()
            return CommandResult(returncode=0, output=f"Upload finished. New dataset: {result}")
