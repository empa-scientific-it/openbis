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
import os
import re
import time
import uuid

import pytest

from pybis.things import Things


def test_get_datasets_count(space):
    o = space.openbis
    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")
    dataset = o.new_dataset(
        type="RAW_DATA",
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={"$name": "some good name"},
    )
    dataset.save()

    try:
        current_datasets = o.get_datasets(count=1)
        assert current_datasets is not None
        assert len(current_datasets) == 1
    finally:
        dataset.delete("test_get_datasets_count", True)


def test_get_datasets_paging(space):
    o = space.openbis
    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")
    dataset1 = o.new_dataset(
        type="RAW_DATA",
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={"$name": "some good name"},
    )
    dataset1.save()

    dataset2 = o.new_dataset(
        type="RAW_DATA",
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={"$name": "some good name"},
    )
    dataset2.save()

    try:
        current_datasets = o.get_datasets(start_with=1, count=1)
        assert current_datasets is not None
        assert len(current_datasets) == 1
    finally:
        dataset1.delete("test_get_datasets_paging", True)
        dataset2.delete("test_get_datasets_paging", True)


def test_create_datasets_no_file(space):
    o = space.openbis
    with pytest.raises(Exception) as exc:
        o.new_dataset(
            type="RAW_DATA",
            experiment="/DEFAULT/DEFAULT/DEFAULT",
            props={"$name": "some good name"},
        )
    assert str(exc.value) == "please provide at least one file"


def test_create_delete_dataset(space):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    o = space.openbis
    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")

    dataset = o.new_dataset(
        type="RAW_DATA",
        sample="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={"$name": "some good name", "notes": "my notes"},
    )

    assert dataset is not None
    assert not dataset.permId
    assert dataset.p is not None
    assert dataset.p["$name"] == "some good name"
    assert dataset.p.notes == "my notes"

    with pytest.raises(Exception):
        dataset.non_existing_attribute = "invalid attribute"
        assert "attribute does not exist, should fail" is None

    with pytest.raises(Exception):
        dataset.p.non_existing_property = "invalid propery"
        assert "property does not exist, should fail" is None

    dataset.save()

    # now there should appear a permId in our object
    assert dataset.permId is not None
    permId = dataset.permId

    # get it by permId
    dataset_by_permId = o.get_dataset(dataset.permId)
    assert dataset_by_permId is not None
    assert dataset_by_permId.permId == permId
    assert dataset_by_permId.type is not None
    assert dataset_by_permId.type.code == "RAW_DATA"
    assert dataset_by_permId.kind == "PHYSICAL"
    assert dataset_by_permId.sample is not None
    assert dataset_by_permId.sample.code == "DEFAULT"
    assert dataset_by_permId.experiment is not None
    assert dataset_by_permId.experiment.code == "DEFAULT"

    assert dataset_by_permId.p is not None
    assert dataset_by_permId.p["$name"] == "some good name"
    assert dataset_by_permId.p.notes == "my notes"

    assert dataset_by_permId.registrator is not None
    assert dataset_by_permId.registrationDate is not None
    # check date format: 2019-03-22 11:36:40
    assert (
            re.search(
                r"^\d{4}\-\d{2}\-\d{2} \d{2}\:\d{2}\:\d{2}$",
                dataset_by_permId.registrationDate,
            )
            is not None
    )

    # delete datasets
    dataset.delete("dataset creation test on " + timestamp, True)

    # Give openbis some time to process it
    time.sleep(1)
    # check that permanent deletion is working
    deletions = o.get_deletions(0, 10)
    assert len(deletions) == 0

    # get by permId should now throw an error
    with pytest.raises(Exception):
        deleted_ds = o.get_dataset(permId)


def test_create_dataset_with_code(space):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    o = space.openbis

    dataset = o.new_dataset(
        type="UNKNOWN",
        code=timestamp,
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        sample="/DEFAULT/DEFAULT/DEFAULT",
        kind="CONTAINER",
    )

    assert dataset is not None
    assert not dataset.permId
    assert dataset.code == timestamp
    dataset.save()

    # our permId is now identical to the code we provided
    assert dataset.permId is not None
    assert dataset.permId == timestamp

    dataset.delete("dataset creation test on {}".format(timestamp))


def test_things_initialization(space):
    data_frame_result = [1, 2, 3]
    objects_result = [4, 5, 6]

    def create_data_frame(attrs, props, response):
        return data_frame_result

    def create_objects(response):
        return objects_result

    things = Things(
        openbis_obj=None,
        entity='dataset',
        identifier_name='permId',
        start_with=0,
        count=10,
        totalCount=10,
        response=None,
        df_initializer=create_data_frame,
        objects_initializer=create_objects
    )

    assert not things.is_df_initialised()
    assert not things.is_objects_initialised()

    assert things.df == data_frame_result
    assert things.objects == objects_result

    assert things.is_df_initialised()
    assert things.is_objects_initialised()


# def test_create_new_dataset_v1(space):
#     """Create dataset and upload file using upload scheme from before 3.6 api version"""
#     openbis_instance = space.openbis
#
#     testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")
#
#     # It is a hack to force old way of upload for testing.
#     openbis_instance.get_server_information()._info["api-version"] = "3.5"
#
#     dataset = openbis_instance.new_dataset(
#         type="RAW_DATA",
#         experiment="/DEFAULT/DEFAULT/DEFAULT",
#         files=[testfile_path],
#         props={"$name": "some good name"},
#     )
#     dataset.save()
#
#     assert dataset.permId is not None
#     assert dataset.file_list == ["original/testfile"]


def test_create_new_dataset_v3_single_file(space):
    openbis_instance = space.openbis

    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")

    dataset = openbis_instance.new_dataset(
        type="RAW_DATA",
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={"$name": "some good name"},
    )
    dataset.save()

    assert dataset.permId is not None
    assert dataset.file_list == ["original/testfile"]


def test_create_new_dataset_v3_directory(space):
    openbis_instance = space.openbis

    testfile_path = os.path.join(os.path.dirname(__file__), "testdir")

    dataset = openbis_instance.new_dataset(
        type="RAW_DATA",
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={"$name": "some good name"},
    )
    dataset.save()

    assert dataset.permId is not None
    assert dataset.file_list == ["original/testdir/testfile"]


def test_dataset_property_in_isoformat_date(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    # Create custom TIMESTAMP property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp',
        description='custom property created in unit test',
        dataType='TIMESTAMP',
    )
    pt_date.save()

    # Create new dataset type
    type_code = "test_dataset_type_" + timestamp + "_" + str(uuid.uuid4())
    dataset_type = o.new_dataset_type(code=type_code)
    dataset_type.save()

    # Assign created property to new dataset type
    dataset_type.assign_property(property_type_code)

    # Create new dataset with timestamp property in non-supported format
    timestamp_property = datetime.datetime.now().isoformat()
    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")

    dataset = o.new_dataset(
        type=type_code,
        experiment="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={property_type_code: timestamp_property},
    )
    dataset.save()

    # New dataset case
    assert len(dataset.p()) == 1
    assert dataset.p[property_type_code] is not None

    # Update dataset case
    dataset.p[property_type_code] = timestamp_property
    dataset.save()

    assert len(dataset.p()) == 1
    assert dataset.p[property_type_code] is not None


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


def test_dataset_array_properties(space):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()
    create_array_properties(space.openbis, f"DATASET_{timestamp}")

    dataset_code = f'TEST_ARRAY_DATASET_{timestamp}'
    dataset_type = space.openbis.new_dataset_type(
        code=dataset_code
    )
    dataset_type.save()

    dataset_type.assign_property('$NAME')
    dataset_type.assign_property(f'DATASET_{timestamp}_ARRAY_INTEGER')
    dataset_type.assign_property(f'DATASET_{timestamp}_ARRAY_REAL')
    dataset_type.assign_property(f'DATASET_{timestamp}_ARRAY_STRING')
    dataset_type.assign_property(f'DATASET_{timestamp}_ARRAY_TIMESTAMP')
    dataset_type.assign_property(f'DATASET_{timestamp}_JSON')

    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")
    dataset = space.openbis.new_dataset(
        type=dataset_code,
        sample="/DEFAULT/DEFAULT/DEFAULT",
        files=[testfile_path],
        props={f'dataset_{timestamp}_array_integer': [1, 2, 3]}
    )
    dataset.save()

    dataset.props[f'dataset_{timestamp}_array_integer'] = [3, 2, 1]
    dataset.props[f'dataset_{timestamp}_array_real'] = [3.1, 2.2, 1.3]
    dataset.props[f'dataset_{timestamp}_array_string'] = ["aa", "bb", "cc"]
    dataset.props[f'dataset_{timestamp}_array_timestamp'] = ['2023-05-18 11:17:03', '2023-05-18 11:17:04',
                                                '2023-05-18 11:17:05']
    dataset.props[f'dataset_{timestamp}_json'] = "{ \"key\": [1, 1, 1] }"
    dataset.save()

    assert dataset.props[f'dataset_{timestamp}_array_integer'] == [3, 2, 1]
    assert dataset.props[f'dataset_{timestamp}_array_real'] == [3.1, 2.2, 1.3]
    assert dataset.props[f'dataset_{timestamp}_array_string'] == ["aa", "bb", "cc"]
    assert dataset.props[f'dataset_{timestamp}_json'] == "{ \"key\": [1, 1, 1] }"
    assert dataset.props[f'dataset_{timestamp}_array_timestamp'] == ['2023-05-18 11:17:03',
                                                        '2023-05-18 11:17:04',
                                                        '2023-05-18 11:17:05']


def test_dataset_assigned_not_multivalue_property_error(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    # Create custom SAMPLE property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp for dataset',
        description='custom property created in unit test',
        dataType='SAMPLE',
    )
    pt_date.save()

    # Create new dataset type
    type_code = "test_dataset_type_" + timestamp + "_" + str(uuid.uuid4())
    dataset_type = o.new_dataset_type(code=type_code)
    dataset_type.save()

    # Assign created property to new dataset type
    dataset_type.assign_property(property_type_code)

    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")
    try:
        dataset = o.new_dataset(
            type=type_code,
            experiment="/DEFAULT/DEFAULT/DEFAULT",
            files=[testfile_path],
            props={property_type_code: ['some_id1', 'some_id2']},
        )
        dataset.save()
        pytest.fail("Dataset creation should should fail!")
    except ValueError as e:
        assert str(e) == f'Property type {property_type_code.upper()} is not a multi-value property!'


def test_dataset_with_multivalue_property_sample(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").lower()

    # Create custom SAMPLE property type
    property_type_code = "test_property_type_" + timestamp + "_" + str(uuid.uuid4())
    pt_date = o.new_property_type(
        code=property_type_code,
        label='custom property of data type timestamp for dataset',
        description='custom property created in unit test',
        dataType='SAMPLE',
        multiValue=True
    )
    pt_date.save()

    # Create new dataset type
    type_code = "test_dataset_type_" + timestamp + "_" + str(uuid.uuid4())
    dataset_type = o.new_dataset_type(code=type_code)
    dataset_type.save()

    # Assign created property to new dataset type
    dataset_type.assign_property(property_type_code)

    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")

    sample_code = "my_sample_{}".format(timestamp)

    test_sample1 = o.new_sample(code=sample_code + "_property1", type='UNKNOWN', space=space)
    test_sample1.save()
    test_sample2 = o.new_sample(code=sample_code + "_property2", type='UNKNOWN', space=space)
    test_sample2.save()
    test_sample3 = o.new_sample(code=sample_code + "_property3", type='UNKNOWN', space=space)
    test_sample3.save()

    props = {property_type_code: [test_sample1.permId, test_sample2.identifier]}

    dataset = o.new_dataset(
        type=type_code,
        sample=test_sample1,
        files=[testfile_path],
        props=props,
    )
    dataset.save()

    # New dataset case
    assert len(dataset.p()) == 1
    assert dataset.p[property_type_code] is not None
    key, val = dataset.props().popitem()
    assert key == property_type_code
    assert type(val) == list
    assert len(val) == 2
    assert test_sample1.permId in val
    assert test_sample2.permId in val

    # Update dataset case
    dataset.p[property_type_code] = [test_sample3.permId]
    dataset.save()

    assert len(dataset.p()) == 1
    assert dataset.p[property_type_code] is not None
    key, val = dataset.props().popitem()
    assert key == property_type_code
    assert val == [test_sample3.permId]


def test_dataset_with_multivalue_property_vocabulary(space):
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
        label='custom property of data type timestamp for dataset',
        description='custom property created in unit test',
        dataType='CONTROLLEDVOCABULARY',
        vocabulary=vocab,
        multiValue=True
    )
    pt_date.save()

    type_code = "test_dataset_type_" + timestamp + "_" + str(uuid.uuid4())
    dataset_type = o.new_dataset_type(code=type_code)
    dataset_type.save()

    # Assign created property to new dataset type
    dataset_type.assign_property(property_type_code)

    sample_code = "my_sample_{}".format(timestamp)

    test_sample = o.new_sample(code=sample_code, type='UNKNOWN', space=space)
    test_sample.save()

    testfile_path = os.path.join(os.path.dirname(__file__), "testdir/testfile")
    props = {property_type_code: ['term_code1', 'term_code2']}

    dataset = o.new_dataset(
        type=type_code,
        sample=test_sample,
        files=[testfile_path],
        props=props,
    )
    dataset.save()

    # New dataset case
    assert len(dataset.p()) == 1
    assert dataset.p[property_type_code] is not None
    key, val = dataset.props().popitem()
    assert key == property_type_code
    assert type(val) == list
    assert len(val) == 2
    assert 'term_code1'.upper() in val
    assert 'term_code2'.upper() in val

    # Update dataset case
    dataset.p[property_type_code] = ['term_code3'.upper()]
    dataset.save()

    assert len(dataset.p()) == 1
    assert dataset.p[property_type_code] is not None
    key, val = dataset.props().popitem()
    assert key == property_type_code
    assert val == ['term_code3'.upper()]
