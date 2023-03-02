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
import csv
import os

from .openbis_command import OpenbisCommand
from ..checksum import validate_checksum
from ..command_result import CommandResult
from ..utils import cd
from ...scripts.click_util import click_echo


class DownloadPhysical(OpenbisCommand):
    """
    Command to download physical files of a data set.
    """

    def __init__(self, dm, data_set_id, from_file, file, skip_integrity_check):
        """
        :param dm: data management.
        :param data_set_id: permId of the data set to be cloned.
        :param from_file: Path to a CSV file with a list of datasets to download.
        :param file: path to a specific file to download from a dataset.
        :param skip_integrity_check: boolean flag indicating whether to skip checksum validation.
        """
        self.data_set_id = data_set_id
        self.from_file = from_file
        self.files = [file] if file is not None else None
        self.skip_integrity_check = skip_integrity_check
        self.load_global_config(dm)
        super(DownloadPhysical, self).__init__(dm)

    def run(self):
        if self.fileservice_url() is None:
            return CommandResult(returncode=-1,
                                 output="Configuration fileservice_url needs to be set for download.")

        if self.from_file is not None:
            with cd(self.data_mgmt.invocation_path):
                with open(self.from_file, newline='') as csvfile:
                    reader = csv.DictReader(csvfile)
                    for row in reader:
                        self.download_dataset(row['permId'])
                    if reader.line_num == 1:  # First row contains headers
                        click_echo("No data sets were found in provided file!")
        else:
            self.download_dataset(self.data_set_id)
        return CommandResult(returncode=0, output="Download completed.")

    def download_dataset(self, perm_id):
        data_set = self.openbis.get_dataset(perm_id)
        files = self.files if self.files is not None else data_set.file_list
        with cd(self.data_mgmt.invocation_path):
            target_folder = data_set.download(files, destination=self.data_mgmt.invocation_path)
            if self.skip_integrity_check is not True:
                invalid_files = validate_checksum(self.openbis, files, data_set.permId,
                                                  target_folder, None)
                self.redownload_invalid_files_on_demand(invalid_files, target_folder, perm_id)
                click_echo(f"Files from dataset {perm_id} has been downloaded to {target_folder}")

    def redownload_invalid_files_on_demand(self, invalid_files, target_folder, perm_id):
        if len(invalid_files) == 0:
            return
        yes_or_no = None
        while yes_or_no != "yes" and yes_or_no != "no":
            click_echo(f"Integrity check failed for following files in dataset {perm_id}:\n" +
                       str(invalid_files) + "\n" +
                       "Either the download failed or the files where changed in the OpenBIS.\n" +
                       "Should the files be downloaded again? (yes/no)")
            yes_or_no = input('> ')
        if yes_or_no == "yes":
            for file in invalid_files:
                filename_dest = os.path.join(target_folder, file)
                os.remove(filename_dest)
            self.files = invalid_files
            return self.download_dataset(perm_id)
