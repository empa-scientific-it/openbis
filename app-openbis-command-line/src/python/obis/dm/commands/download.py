#   Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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

from .openbis_command import OpenbisCommand, ContentCopySelector
from ..checksum import validate_checksum
from ..command_result import CommandResult
from ..utils import cd


class Download(OpenbisCommand):
    """
    Command to download files of a data set. Uses the big data link server to access the files.
    As opposed to the clone, the user does not need to be able to access the files via ssh 
    and no new content copy is created in openBIS.
    """

    def __init__(self, dm, data_set_id, content_copy_index, file, skip_integrity_check):
        """
        :param dm: data management
        :param data_set_id: permId of the data set to be cloned
        :param content_copy_index: in case of multiple content copied
        :param skip_integrity_check: Checksums are not validated if True
        """
        self.data_set_id = data_set_id
        self.content_copy_index = content_copy_index
        self.files = [file] if file is not None else None
        self.skip_integrity_check = skip_integrity_check
        self.load_global_config(dm)
        super(Download, self).__init__(dm)

    def run(self):

        if self.fileservice_url() is None:
            return CommandResult(returncode=-1,
                                 output="Configuration fileservice_url needs to be set for download.")

        data_set = self.openbis.get_dataset(self.data_set_id)
        content_copy_index = ContentCopySelector(data_set, self.content_copy_index,
                                                 get_index=True).select()
        files = self.files if self.files is not None else data_set.file_list

        with cd(self.data_mgmt.invocation_path):
            destination, invalid_files = data_set.download(files,
                                                           linked_dataset_fileservice_url=self.fileservice_url(),
                                                           content_copy_index=content_copy_index)
            target_folder = os.path.join(destination, data_set.permId)
            if self.skip_integrity_check != True:
                files = [file for file in files if file not in invalid_files]
                invalid_files += validate_checksum(self.openbis, files, data_set.permId,
                                                   target_folder, None)
                self.redownload_invalid_files_on_demand(invalid_files, target_folder)
            return CommandResult(returncode=0, output="Files downloaded to: %s" % target_folder)

    def redownload_invalid_files_on_demand(self, invalid_files, target_folder):
        if len(invalid_files) == 0:
            return
        yes_or_no = None
        while yes_or_no != "yes" and yes_or_no != "no":
            print("Integrity check failed for following files:\n" +
                  str(invalid_files) + "\n" +
                  "Either the download failed or the files where changed after committing to openBIS.\n" +
                  "Should the files be downloaded again? (yes/no)")
            yes_or_no = input('> ')
        if yes_or_no == "yes":
            for file in invalid_files:
                filename_dest = os.path.join(target_folder, file)
                os.remove(filename_dest)
            self.files = invalid_files
            return self.run()
