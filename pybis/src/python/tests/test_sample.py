import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_create_delete_sample(space):
    o = space.openbis

    sample_type = "UNKNOWN"
    sample = o.new_sample(
        code="illegal sample name with spaces", type=sample_type, space=space
    )
    with pytest.raises(ValueError):
        sample.save()
        assert "should not have been created" is None

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    sample_code = "test_sample_" + timestamp + "_" + str(random.randint(0, 1000))
    sample = o.new_sample(code=sample_code, type=sample_type, space=space)
    assert sample is not None
    assert sample.space.code == space.code
    assert sample.code == sample_code
    assert sample.permId == ""
    sample.save()

    # now there should appear a permId
    assert sample.permId is not None

    # get it by permId
    sample_by_permId = o.get_sample(sample.permId)
    assert sample_by_permId is not None

    sample_by_permId = space.get_sample(sample.permId)
    assert sample_by_permId is not None

    assert sample_by_permId.registrator is not None
    assert sample_by_permId.registrationDate is not None
    # check date format: 2019-03-22 11:36:40
    assert (
        re.search(
            r"^\d{4}\-\d{2}\-\d{2} \d{2}\:\d{2}\:\d{2}$",
            sample_by_permId.registrationDate,
        )
        is not None
    )

    # get sample by identifier
    sample_by_identifier = o.get_sample(sample.identifier)
    assert sample_by_identifier is not None

    sample_by_identifier = space.get_sample(sample.identifier)
    assert sample_by_identifier is not None

    sample.delete("sample creation test on " + timestamp)


def test_create_delete_space_sample(space):
    o = space.openbis
    sample_type = "UNKNOWN"
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    sample_code = "test_sample_" + timestamp + "_" + str(random.randint(0, 1000))

    sample = space.new_sample(code=sample_code, type=sample_type)
    assert sample is not None
    assert sample.space.code == space.code
    assert sample.code == sample_code
    sample.save()
    assert sample.permId is not None
    sample.delete("sample space creation test on " + timestamp)


def test_parent_child(space):
    o = space.openbis
    sample_type = "UNKNOWN"
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    parent_code = (
        "parent_sample_{}".format(timestamp) + "_" + str(random.randint(0, 1000))
    )
    sample_parent = o.new_sample(code=parent_code, type=sample_type, space=space)
    sample_parent.save()

    child_code = "child_sample_{}".format(timestamp)
    sample_child = o.new_sample(
        code=child_code, type=sample_type, space=space, parent=sample_parent
    )
    sample_child.save()
    time.sleep(5)

    ex_sample_parents = sample_child.get_parents()
    ex_sample_parent = ex_sample_parents[0]
    assert (
        ex_sample_parent.identifier == "/{}/{}".format(space.code, parent_code).upper()
    )

    ex_sample_children = ex_sample_parent.get_children()
    ex_sample_child = ex_sample_children[0]
    assert ex_sample_child.identifier == "/{}/{}".format(space.code, child_code).upper()

    sample_parent.delete("sample parent-child creation test on " + timestamp)
    sample_child.delete("sample parent-child creation test on " + timestamp)
