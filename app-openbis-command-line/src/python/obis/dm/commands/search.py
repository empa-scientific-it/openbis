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

import os

from .openbis_command import OpenbisCommand
from ..command_result import CommandResult
from ..utils import cd
from ...scripts.click_util import click_echo


class Search(OpenbisCommand):
    """
    Command to search data in openBIS.
    """

    def __init__(self, dm, type_code, space, project, experiment, property_code, property_value,
                 save_path):
        """
        :param dm: data management
        :param type_code: Filter by type code
        :param space: Filter by space path
        :param project: Filter by project path
        :param experiment: Filter by experiment
        :param property_code: Filter by property_code, needs to be set together with property_value
        :param property_value: Filter by property_value, needs to be set together with property_code
        :param save_path: Path to save results. If not set, results will not be saved.
        """
        self.property_value = property_value
        self.property_code = property_code
        self.experiment = experiment
        self.project = project
        self.space = space
        self.type_code = type_code
        self.save_path = save_path
        self.load_global_config(dm)
        super(Search, self).__init__(dm)

    def search_samples(self):
        properties = None
        if self.property_code is not None and self.property_value is not None:
            properties = {
                self.property_code: self.property_value,
            }

        search_results = self.openbis.get_samples(
            space=self.space,
            project=self.project,  # Not Supported with Project Samples disabled
            experiment=self.experiment,
            type=self.type_code,
            where=properties,
            props="*"  # Fetch all properties
        )
        click_echo(f"Objects found: {len(search_results)}")
        if self.save_path is not None:
            click_echo(f"Saving search results in {self.save_path}")
            with cd(self.data_mgmt.invocation_path):
                search_results.df.to_csv(self.save_path, index=False)
        else:
            click_echo(f"Search results:\n{search_results}")

        return CommandResult(returncode=0, output="Search completed.")

    def search_data_sets(self):
        if self.save_path is not None and self.fileservice_url() is None:
            return CommandResult(returncode=-1,
                                 output="Configuration fileservice_url needs to be set for download.")

        properties = None
        if self.property_code is not None and self.property_value is not None:
            properties = {
                self.property_code: self.property_value,
            }

        search_results = self.openbis.get_samples(
            space=self.space,
            project=self.project,  # Not Supported with Project Samples disabled
            experiment=self.experiment,
            type=self.type_code,
            where=properties,
            attrs=["parents", "children"],
            props="*"  # Fetch all properties
        )

        collections = self.openbis.get_collections(
            space=self.space,
            project=self.project,
            type=self.type_code,
            where=properties,
            props="*"  # Fetch all properties
        )

        click_echo("Looking for data sets")
        datasets = []
        perm_ids = set()
        for sample in search_results:
            ds = sample.get_datasets()
            for ds_object in ds.objects:
                datasets += [ds_object] if ds_object.permId not in perm_ids else []
                perm_ids.add(ds_object.permId)
        for collection in collections:
            ds = collection.get_datasets()
            for ds_object in ds.objects:
                datasets += [ds_object] if ds_object.permId not in perm_ids else []
                perm_ids.add(ds_object.permId)

        click_echo(f"Data sets found: {len(datasets)}")
        if self.save_path is not None:
            with cd(self.data_mgmt.invocation_path):
                if os.path.exists(self.save_path) is True and os.path.isdir(
                        self.save_path) is False:
                    return CommandResult(returncode=-1,
                                         output=f"File {self.save_path} is not a directory")
                if os.path.isdir(self.save_path) is False:
                    click_echo(f"Creating directory {self.save_path}")
                    os.makedirs(self.save_path)
                click_echo(
                    f"Saving search results in {os.path.join(self.data_mgmt.invocation_path, self.save_path)}")
                for dataset in datasets:
                    dataset.download(destination=self.save_path,
                                     linked_dataset_fileservice_url=self.fileservice_url() + "/download")
        else:
            click_echo(f"Search results:\n{datasets}")

        return CommandResult(returncode=0, output="Search completed.")
