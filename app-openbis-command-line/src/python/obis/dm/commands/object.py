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
from ..utils import is_valid_perm_id, OperationType
from ...scripts.click_util import click_echo


class Object(OpenbisCommand):
    """
    Command to operate on parent object of downloaded physical datasets.
    """

    def __init__(self, dm, operation_type, prop, value):
        """
        :param dm: data management
        :param operation_type: type of operation to perform: get/set
        :param prop: property to operate on
        :param value: value to set for property prop
        """
        self.operation_type = operation_type
        self.prop = prop
        self.value = value
        self.load_global_config(dm)
        super(Object, self).__init__(dm)

    def run(self):
        if self.operation_type is OperationType.GET:
            return self.get()
        if self.operation_type is OperationType.SET:
            return self.set()
        else:
            click_echo(f"Operation {self.operation_type} is not supported!")
            return -1

    def get(self):
        dataset_perm_ids = self.get_downloaded_datasets()
        datasets = []
        for perm_id in dataset_perm_ids:
            ds = self.get_dataset(perm_id)
            datasets += [ds] if ds is not None and ds.sample is not None else []
        if not datasets:
            click_echo(f"No parent objects found.")
        else:
            datasets = set(datasets)
            for dataset in datasets:
                sample = dataset.sample
                if self.prop == "parents":
                    click_echo(f"Object: {sample.permId} '{self.prop}' = {sample.parents}")
                elif self.prop == "children":
                    click_echo(f"Object: {sample.permId} '{self.prop}' = {sample.children}")
                else:
                    click_echo(f"Object: {sample.permId} '{self.prop}' = {sample.props[self.prop]}")
        return 0

    def set(self):
        dataset_perm_ids = self.get_downloaded_datasets()
        datasets = []
        for perm_id in dataset_perm_ids:
            ds = self.get_dataset(perm_id)
            datasets += [ds] if ds is not None and ds.sample is not None else []
        if not datasets:
            click_echo(f"No parent objects found.")
        else:
            datasets = set(datasets)
            for dataset in datasets:
                sample = dataset.sample
                if self.prop == "parents":
                    sample.parents = self.empty_or_split()
                    click_echo(
                        f"Setting object: {sample.permId} parents to {self.empty_or_split()}")
                elif self.prop == "children":
                    sample.children = self.empty_or_split()
                    click_echo(
                        f"Setting object: {sample.permId} children to {self.empty_or_split()}")
                else:
                    sample.props[self.prop] = self.value
                    click_echo(
                        f"Setting object: {sample.permId} property '{self.prop}' to '{sample.props[self.prop]}'")
                sample.save()
        return 0

    def empty_or_split(self):
        if self.value == "":
            return []
        return self.value.split(',')

    def get_downloaded_datasets(self):
        result = set()
        for root, dirs, files in os.walk(self.data_mgmt.invocation_path):
            for dir_name in dirs:
                if is_valid_perm_id(dir_name) is True:
                    result.add(dir_name)
        return result

    def get_dataset(self, perm_id):
        try:
            return self.openbis.get_dataset(perm_id, props="*")
        except ValueError as e:
            click_echo(f"Could not get dataset! {e}")
            return None
