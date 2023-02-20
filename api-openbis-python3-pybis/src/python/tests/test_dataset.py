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
from pybis.things import Things

import json
import random
import re
import os

import pytest
import time


def test_get_datasets(space):
    # test paging
    o = space.openbis
    current_datasets = o.get_datasets(start_with=1, count=1)
    assert current_datasets is not None
    assert len(current_datasets) == 1


def test_create_delete_dataset(space):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    o = space.openbis
    testfile_path = os.path.join(os.path.dirname(__file__), "testfile")

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
        openbis_obj = None,
        entity = 'dataset',
        identifier_name = 'permId',
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