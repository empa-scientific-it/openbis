import json
import random
import re

import pytest
import time


def test_get_datasets(space):
    # test paging
    o = space.openbis
    current_datasets = o.get_datasets(start_with=1, count=1)
    assert current_datasets is not None
    # we cannot assert == 1, because search is delayed due to lucene search...
    assert len(current_datasets) <= 1


def test_create_delete_dataset(space):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    o = space.openbis

    dataset = o.new_dataset(
        type="RAW_DATA",
        sample="/DEFAULT/DEFAULT/DEFAULT",
        files=["testfile"],
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
    dataset.delete("dataset creation test on " + timestamp)

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
