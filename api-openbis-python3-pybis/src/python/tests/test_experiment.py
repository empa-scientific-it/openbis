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
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()
    create_array_properties(space.openbis, f"EXPERIMENT_{timestamp}")

    collection_code = f'TEST_ARRAY_COLLECTION_{timestamp}'
    experiment_type = space.openbis.new_experiment_type(
        collection_code,
        description=None,
        validationPlugin=None,
    )
    experiment_type.save()
    experiment_type.assign_property(f'EXPERIMENT_{timestamp}_ARRAY_INTEGER')
    experiment_type.assign_property(f'EXPERIMENT_{timestamp}_ARRAY_REAL')
    experiment_type.assign_property(f'EXPERIMENT_{timestamp}_ARRAY_STRING')
    experiment_type.assign_property(f'EXPERIMENT_{timestamp}_ARRAY_TIMESTAMP')
    experiment_type.assign_property(f'EXPERIMENT_{timestamp}_JSON')

    exp = space.openbis.new_experiment(
        code = 'EXP_PYTHON',
        type = collection_code,
        project = 'DEFAULT',
        props = { f'experiment_{timestamp}_array_integer': [1, 2, 3]})
    exp.save()

    exp.props[f'experiment_{timestamp}_array_integer'] = [3, 2, 1]
    exp.props[f'experiment_{timestamp}_array_real'] = [3.1, 2.2, 1.3]
    exp.props[f'experiment_{timestamp}_array_string'] = ["aa", "bb", "cc"]
    exp.props[f'experiment_{timestamp}_array_timestamp'] = ['2023-05-18 11:17:03', '2023-05-18 11:17:04',
                                               '2023-05-18 11:17:05']
    exp.props[f'experiment_{timestamp}_json'] = "{ \"key\": [1, 1, 1] }"
    exp.save()


def test_experiment_assigned_not_multivalue_property_error(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    # Create custom SAMPLE property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp for experiment',
        description='custom property created in unit test',
        dataType='SAMPLE',
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

    props = {property_type_code: ['some_id1', 'some_id2']}
    try:
        exp = o.new_experiment(code=code, project=project, type=type_code, props=props)
        exp.save()
        pytest.fail("Experiment creation should fail!")
    except ValueError as e:
        assert str(e) == f'Property type {property_type_code.upper()} is not a multi-value property!'


def test_experiment_with_multivalue_property_sample(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    # Create custom SAMPLE property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp for experiment',
        description='custom property created in unit test',
        dataType='SAMPLE',
        multiValue=True
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

    sample_code = "my_sample_{}".format(timestamp)

    test_sample1 = o.new_sample(code=sample_code + "_property1", type='UNKNOWN', space=space)
    test_sample1.save()
    test_sample2 = o.new_sample(code=sample_code + "_property2", type='UNKNOWN', space=space)
    test_sample2.save()
    test_sample3 = o.new_sample(code=sample_code + "_property3", type='UNKNOWN', space=space)
    test_sample3.save()

    props = {property_type_code: [test_sample1.permId, test_sample2.identifier]}

    exp = o.new_experiment(code=code, project=project, type=type_code, props=props)
    exp.save()

    # New experiment case
    assert len(exp.p()) == 1
    assert exp.p[property_type_code] is not None
    key, val = exp.props().popitem()
    assert key == property_type_code
    assert type(val) == list
    assert len(val) == 2
    assert test_sample1.permId in val
    assert test_sample2.permId in val

    # Update experiment case
    exp.p[property_type_code] = [test_sample3.permId]
    exp.save()

    assert len(exp.p()) == 1
    assert exp.p[property_type_code] is not None
    key, val = exp.props().popitem()
    assert key == property_type_code
    assert val == [test_sample3.permId]


def test_experiment_with_multivalue_property_vocabulary(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    vocab = o.new_vocabulary(code=f'test_vocab_{timestamp}_{uuid.uuid4()}',
                             description='test vocab for multi-value property tests',
                             terms=[
                                 {"code": 'term_code1', "label": "term_label1",
                                  "description": "term_description1"},
                                 {"code": 'term_code2', "label": "term_label2",
                                  "description": "term_description2"},
                                 {"code": 'term_code3', "label": "term_label3",
                                  "description": "term_description3"}
                             ])
    vocab.save()

    # Create custom CONTROLLEDVOCABULARY property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp for experiment',
        description='custom property created in unit test',
        dataType='CONTROLLEDVOCABULARY',
        vocabulary=vocab,
        multiValue=True
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

    props = {property_type_code: ['term_code1', 'term_code2']}

    exp = o.new_experiment(code=code, project=project, type=type_code, props=props)
    exp.save()

    # New experiment case
    assert len(exp.p()) == 1
    assert exp.p[property_type_code] is not None
    key, val = exp.props().popitem()
    assert key == property_type_code
    assert type(val) == list
    assert len(val) == 2
    assert 'term_code1'.upper() in val
    assert 'term_code2'.upper() in val

    # Update experiment case
    exp.p[property_type_code] = ['term_code3'.upper()]
    exp.save()

    assert len(exp.p()) == 1
    assert exp.p[property_type_code] is not None
    key, val = exp.props().popitem()
    assert key == property_type_code
    assert val == ['term_code3'.upper()]
