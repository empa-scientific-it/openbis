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

import datetime
import time
import uuid

import pytest


def test_create_delete_experiment(space):
    o = space.openbis
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    new_code = 'test_experiment_' + timestamp

    with pytest.raises(TypeError):
        # experiments must be assigned to a project
        e_new = o.new_experiment(
            code=new_code,
            type='UNKNOWN',
        )

    project = o.get_projects()[0]

    e_new = o.new_experiment(
        code=new_code,
        project=project,
        type='UNKNOWN',
    )
    assert e_new.project is not None
    assert e_new.permId == ''

    e_new.save()

    assert e_new.permId is not None
    assert e_new.code == new_code.upper()

    e_exists = o.get_experiment(e_new.permId)
    assert e_exists is not None

    e_new.delete('delete test experiment ' + new_code.upper())

    with pytest.raises(ValueError):
        e_no_longer_exists = o.get_experiment(e_exists.permId)


def test_get_experiments(space):
    # test paging
    o = space.openbis
    current_datasets = o.get_experiments(start_with=1, count=1)
    assert current_datasets is not None
    # we cannot assert == 1, because search is delayed due to lucene search...
    assert len(current_datasets) <= 1


def test_experiment_property_in_isoformat_date(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    # Create custom TIMESTAMP property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp for experiment',
        description='custom property created in unit test',
        dataType='TIMESTAMP',
    )
    pt_date.save()

    type_code = "test_experiment_type_" + timestamp + "_" + str(uuid.uuid4())
    experiment_type = o.new_experiment_type(
        type_code,
        description=None,
        validationPlugin=None,
    )
    experiment_type.save()
    experiment_type.assign_property(property_type_code)

    project = o.get_projects()[0]
    code = "my_experiment_{}".format(timestamp)
    timestamp_property = datetime.datetime.now().isoformat()
    props = {property_type_code: timestamp_property}

    exp = o.new_experiment(code=code, project=project, type=type_code, props=props)
    exp.save()

    # New experiment case
    assert len(exp.p()) == 1
    assert exp.p[property_type_code] is not None

    # Update experiment case
    exp.p[property_type_code] = timestamp_property
    exp.save()

    assert len(exp.p()) == 1
    assert exp.p[property_type_code] is not None


def create_array_properties(openbis, code_prefix):
    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_INTEGER',
        label='integer array',
        description='integer array property',
        dataType='ARRAY_INTEGER',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_REAL',
        label='real array',
        description='real array property',
        dataType='ARRAY_REAL',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_STRING',
        label='string array',
        description='string array property',
        dataType='ARRAY_STRING',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_ARRAY_TIMESTAMP',
        label='timestamp array',
        description='timestamp array property',
        dataType='ARRAY_TIMESTAMP',
    )
    pt.save()

    pt = openbis.new_property_type(
        code=code_prefix + '_JSON',
        label='json',
        description='json type property',
        dataType='JSON',
    )
    pt.save()


def test_experiment_array_properties(space):

    create_array_properties(space.openbis, "EXPERIMENT")

    collection_code = 'TEST_ARRAY_COLLECTION'
    experiment_type = space.openbis.new_experiment_type(
        collection_code,
        description=None,
        validationPlugin=None,
    )
    experiment_type.save()
    experiment_type.assign_property('EXPERIMENT_ARRAY_INTEGER')
    experiment_type.assign_property('EXPERIMENT_ARRAY_REAL')
    experiment_type.assign_property('EXPERIMENT_ARRAY_STRING')
    experiment_type.assign_property('EXPERIMENT_ARRAY_TIMESTAMP')
    experiment_type.assign_property('EXPERIMENT_JSON')

    exp = space.openbis.new_experiment(
        code = 'EXP_PYTHON',
        type = collection_code,
        project = 'DEFAULT',
        props = { 'experiment_array_integer': [1, 2, 3]})
    exp.save()

    exp.props['experiment_array_integer'] = [3, 2, 1]
    exp.props['experiment_array_real'] = [3.1, 2.2, 1.3]
    exp.props['experiment_array_string'] = ["aa", "bb", "cc"]
    exp.props['experiment_array_timestamp'] = ['2023-05-18 11:17:03', '2023-05-18 11:17:04',
                                               '2023-05-18 11:17:05']
    exp.props['experiment_json'] = "{ \"key\": [1, 1, 1] }"
    exp.save()
    