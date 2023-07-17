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

import concurrent.futures

import pandas as pd

from pybis.property_reformatter import is_of_openbis_supported_date_format
from pybis.sample import Sample
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


def _get_datasets_of_samples(get_dataset_method, samples):
    output = []
    with concurrent.futures.ThreadPoolExecutor(
            max_workers=5) as pool_simple:
        output = pool_simple.map(get_dataset_method, samples)

    return output


def flatten(matrix):
    flat_list = []
    for row in matrix:
        flat_list += row
    return flat_list


def _check_date(sign, date1, date2):
    if is_of_openbis_supported_date_format(date1) and is_of_openbis_supported_date_format(date2):
        timestamp1 = pd.to_datetime(date1)
        timestamp2 = pd.to_datetime(date2)
        if sign == "=":
            return timestamp2 == timestamp1
        elif sign == ">":
            return timestamp2 > timestamp1
        elif sign == "<":
            return timestamp2 < timestamp1
        raise ValueError(f"Unknown sign {sign}")
    else:
        raise ValueError("Dates are not in a supported formats!")


def _filter_dataset(dataset, filters):
    if filters.get("space", None) is not None:
        space = filters["space"]
        if dataset.sample is not None and dataset.sample.space.code != space:
            return False
        if dataset.experiment is not None and dataset.experiment.project.space.code != space:
            return False
    if filters.get("type_code", None) is not None:
        if dataset.type.code != filters["type_code"]:
            return False
    if filters.get("project", None) is not None:
        project = filters["project"]
        if dataset.sample is not None and dataset.sample.project.code != project:
            return False
        if dataset.experiment is not None and dataset.experiment.project.code != project:
            return False
    if filters.get("experiment", None) is not None:
        if dataset.experiment is not None and dataset.experiment.code != filters["experiment"]:
            return False
    if filters.get("property_code", None) is not None:
        prop_code = filters["property_code"]
        prop_value = filters["property_value"]
        if dataset.props is not None and dataset.props[prop_code.lower()] != prop_value:
            return False
    if filters.get("registration_date", None) is not None:
        registration_date = filters["registration_date"]
        sign = "="
        if registration_date[0] in [">", "<", "="]:
            sign, registration_date = registration_date[0], registration_date[1:]
        return _check_date(sign, registration_date, dataset.registrationDate)
    if filters.get("modification_date", None) is not None:
        modification_date = filters["modification_date"]
        sign = "="
        if modification_date[0] in [">", "<", "="]:
            sign, modification_date = modification_date[0], modification_date[1:]
        return _check_date(sign, modification_date, dataset.modificationDate)
    return True


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
                                               attrs=["parents", "children", "dataSets"],
                                               props=self.props)
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
                                                                    attrs=["parents", "children",
                                                                           "dataSets"],
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

        main_filters = self.filters.copy()

        object_filters = {k[7:]: v for (k, v) in main_filters.items() if k.startswith('object_')}
        dataset_filters = {k: v for (k, v) in main_filters.items() if not k.startswith('object_')}
        if object_filters:
            if 'id' in object_filters:
                object_filters['object_code'] = object_filters['id']
                del object_filters['id']
            self.filters = object_filters
            search_results = self._search_samples()
            datasets = [x for x in _get_datasets_of_samples(Sample.get_datasets, search_results) if
                        x.totalCount > 0]
            for thing in datasets:
                for obj in thing.objects:
                    if not _filter_dataset(obj, dataset_filters):
                        for i in range(len(thing.response)):
                            if thing.response[i]['permId']['permId'] == obj.permId:
                                del thing.response[i]
                                break
            datasets = [x.response for x in datasets]
            datasets = self.openbis._dataset_list_for_response(props=self.props,
                                                               response=flatten(datasets),
                                                               parsed=True)
        else:
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
                                                                   response=[dataset.data for
                                                                             dataset
                                                                             in output],
                                                                   parsed=True)
            else:
                if "dataset_id" in self.filters:
                    results = self.openbis.get_sample(self.filters['dataset_id']).get_datasets(
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
