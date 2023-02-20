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
import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis

# setup: tests require the dataset type GIT_REPO with the property description


def create_external_data_management_system(openbis_instance):
    code = "TEST-GIT-{:04d}".format(random.randint(0, 9999))
    result = openbis_instance.create_external_data_management_system(code, 'Test git', 'localhost:~openbis/repo')
    return code, result


def test_create_external_data_management_system(openbis_instance):
    code, result = create_external_data_management_system(openbis_instance)
    assert result is not None
    assert result.code == code
    assert result.label == 'Test git'
    assert result.addressType == 'FILE_SYSTEM'
    assert result.address == 'localhost:~openbis/repo'


def test_new_git_data_set(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT")
    assert result is not None
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)
    # TODO Delete the externaldms (deleteExternalDataManagementSystems)
    # see http://svnsis.ethz.ch/doc/openbis/S250.0/ch/ethz/sis/openbis/generic/asapi/v3/IApplicationServerApi.html


def test_new_git_data_set_with_code(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    data_set_code = openbis_instance.create_perm_id()
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT",
                                               data_set_code=data_set_code)
    assert result is not None
    assert result.code == data_set_code
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)


def test_new_git_data_set_with_parent(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT")
    assert result is not None
    parent_code = result.code
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '23456', dms_code, "/DEFAULT/DEFAULT",
                                               parents=parent_code)
    assert result.code != parent_code
    assert len(result.parents) == 1
    assert result.parents[0] == parent_code
    openbis_instance.delete_entity('DataSet', parent_code, 'Testing.', capitalize=False)
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)


def test_new_git_data_set_with_property(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    data_set_code = openbis_instance.create_perm_id()
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT",
                                               data_set_code=data_set_code,
                                               properties={"DESCRIPTION": 'This is a description'})
    assert result is not None
    assert result.code == data_set_code
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)


def test_new_git_data_set_with_contents(openbis_instance):
    dms_code, dms = create_external_data_management_system(openbis_instance)
    data_set_code = openbis_instance.create_perm_id()
    contents = [{'crc32': 1234, 'directory': False, 'fileLength': 4321, 'path': 'path/to/the/file.txt'},
                {'directory': True, 'path': 'path/to/empty/directory'}]
    result = openbis_instance.new_git_data_set("GIT_REPO", "./", '12345', dms_code, "/DEFAULT/DEFAULT",
                                               data_set_code=data_set_code,
                                               properties={"DESCRIPTION": 'This is a description'},
                                               contents=contents)
    assert result is not None
    assert result.code == data_set_code
    openbis_instance.delete_entity('DataSet', result.code, 'Testing.', capitalize=False)
