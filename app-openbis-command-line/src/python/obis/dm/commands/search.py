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

# from queue import Queue
# from threading import Thread
import concurrent.futures

from .openbis_command import OpenbisCommand
from ..command_result import CommandResult
from ..utils import cd
from ...scripts.click_util import click_echo


def _dfs(objects, prop, func, func_specific):
    """Helper function that perform DFS search over children graph of objects"""
    with concurrent.futures.ThreadPoolExecutor(
            max_workers=5) as pool_simple, concurrent.futures.ThreadPoolExecutor(
            max_workers=20) as pool_full:
        stack = [getattr(openbis_obj, prop) for openbis_obj in
                 objects]  # datasets and samples provide children in different formats
        visited = set()
        stack.reverse()
        output = []
        while stack:
            simple_results = pool_simple.map(func, stack)
            stack = []
            children = []
            full_download = []
            for obj in simple_results:
                key = obj.df[prop][0]
                children += list(obj.df['children'])[0]
                if key not in visited:
                    visited.add(key)
                    full_download += [key]
            if full_download:
                output += pool_full.map(func_specific, full_download)
            for child in children:
                if child not in visited:
                    stack += [child]

    return output


class Search(OpenbisCommand):
    """
    Command to search samples or datasets in openBIS.
    """

    def __init__(self, dm, filters, recursive, save_path):
        """
        :param dm: data management
        :param filters: Dictionary of filter to be used during search
        :param recursive: Flag indicating recursive search in children
        :param save_path: Path to save results. If not set, results will not be saved.
        """
        self.filters = filters
        self.recursive = recursive
        self.save_path = save_path
        self.load_global_config(dm)
        self.props = "*"
        super(Search, self).__init__(dm)

    def search_samples(self):
        search_results = self._search_samples()

        click_echo(f"Objects found: {len(search_results)}")
        if self.save_path is not None:
            click_echo(f"Saving search results in {self.save_path}")
            with cd(self.data_mgmt.invocation_path):
                search_results.df.to_csv(self.save_path, index=False)
        else:
            click_echo(f"Search results:\n{search_results}")

        return CommandResult(returncode=0, output="Search completed.")

    def _get_samples_children(self, identifier):
        return self.openbis.get_samples(identifier, attrs=["children", "dataSets"])

    def _get_sample_with_datasets(self, identifier):
        return self.openbis.get_sample(identifier, withDataSetIds=True)

    def _search_samples(self):
        """Helper method to search samples"""

        if "object_code" in self.filters:
            results = self.openbis.get_samples(identifier=self.filters['object_code'],
                                               attrs=["parents", "children", "dataSets"], props=self.props)
        else:
            args = self._get_filtering_args(self.props, ["parents", "children", "dataSets"])
            results = self.openbis.get_samples(**args)

        if self.recursive:
            click_echo(f"Recursive search enabled. It may take time to produce results.")
            output = _dfs(results.objects, 'identifier',
                          self._get_samples_children,
                          self._get_sample_with_datasets)  # samples provide identifiers as children
            search_results = self.openbis._sample_list_for_response(props=self.props,
                                                                    response=[sample.data for sample
                                                                              in output],
                                                                    attrs=["parents", "children", "dataSets"],
                                                                    parsed=True)
        else:
            search_results = results
        return search_results

    def _get_datasets_children(self, permId):
        return self.openbis.get_datasets(permId, attrs=["children"])

    def search_data_sets(self):
        if self.save_path is not None and self.fileservice_url() is None:
            return CommandResult(returncode=-1,
                                 output="Configuration fileservice_url needs to be set for download.")

        if self.recursive:
            search_results = self._search_samples()  # Look for samples recursively
            o = []
            for sample in search_results.objects:  # get datasets
                o += sample.get_datasets(
                    attrs=["parents", "children"], props=self.props)
            output = _dfs(o, 'permId',  # datasets provide permIds as children
                          self._get_datasets_children,
                          self.openbis.get_dataset)  # look for child datasets
            datasets = self.openbis._dataset_list_for_response(props=self.props,
                                                               response=[dataset.data for dataset
                                                                         in output],
                                                               parsed=True)
        else:
            if "object_code" in self.filters:
                results = self.openbis.get_sample(self.filters['object_code']).get_datasets(
                    attrs=["parents", "children"], props=self.props)
            else:
                args = self._get_filtering_args(self.props, ["parents", "children"])
                results = self.openbis.get_datasets(**args)
            datasets = results

        click_echo(f"Data sets found: {len(datasets)}")
        if self.save_path is not None:
            click_echo(f"Saving search results in {self.save_path}")
            with cd(self.data_mgmt.invocation_path):
                datasets.df.to_csv(self.save_path, index=False)
        else:
            click_echo(f"Search results:\n{datasets}")

        return CommandResult(returncode=0, output="Search completed.")

    def _get_filtering_args(self, props, attrs):
        where = None
        if self.filters['property_code'] is not None and self.filters['property_value'] is not None:
            where = {
                self.filters['property_code']: self.filters['property_value'],
            }

        args = dict(space=self.filters['space'],
                    project=self.filters['project'],
                    # Not Supported with Project Samples disabled
                    experiment=self.filters['experiment'],
                    type=self.filters['type_code'],
                    where=where,
                    attrs=attrs,
                    props=props)

        if self.filters['registration_date'] is not None:
            args['registrationDate'] = self.filters['registration_date']
        if self.filters['modification_date'] is not None:
            args['modificationDate'] = self.filters['modification_date']
        return args
