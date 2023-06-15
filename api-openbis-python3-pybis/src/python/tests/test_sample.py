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
import random
import re
import time
import uuid

import pandas as pd
import pytest


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


def test_empty_data_frame(openbis_instance):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    sample_type_code = "test_sample_type_" + timestamp + "_" + str(uuid.uuid4())

    sample_type = openbis_instance.new_sample_type(
        code=sample_type_code,
        generatedCodePrefix="S",
        autoGeneratedCode=True,
        subcodeUnique=False,
        listable=True,
        showContainer=False,
        showParents=True,
        showParentMetadata=False
    )
    sample_type.save()

    s = openbis_instance.get_sample_type(sample_type_code)
    pa = s.get_property_assignments()

    pd.testing.assert_frame_equal(pa.df, pd.DataFrame())


def test_sample_property_in_isoformat_date(space):
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

    # Create custom sample type
    sample_type_code = "test_sample_type_" + timestamp + "_" + str(uuid.uuid4())
    sample_type = o.new_sample_type(
        code=sample_type_code,
        generatedCodePrefix="S",
        autoGeneratedCode=True,
        listable=True,
    )
    sample_type.save()

    # Assign created property to new sample type
    sample_type.assign_property(
        prop=property_type_code,
        section='',
        ordinal=5,
        mandatory=False,
        showInEditView=True,
        showRawValueInForms=True
    )

    sample_code = "my_sample_{}".format(timestamp)
    # Create new sample with timestamp property in non-supported format
    timestamp_property = datetime.datetime.now().isoformat()
    sample = o.new_sample(code=sample_code,
                          type=sample_type_code,
                          space=space,
                          props={
                              property_type_code: timestamp_property})
    sample.save()

    # New item case
    assert len(sample.props()) == 1
    key, val = sample.props().popitem()
    assert key == property_type_code

    # Update item case
    sample.props = {property_type_code: timestamp_property}
    sample.save()

    assert len(sample.props()) == 1
    key, val = sample.props().popitem()
    assert key == property_type_code


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


def test_sample_array_properties(space):

    create_array_properties(space.openbis, "SAMPLE")

    sample_code = 'TEST_ARRAY_SAMPLE'
    sample_type = space.openbis.new_sample_type(
        sample_code,
        generatedCodePrefix = 'S-',
        autoGeneratedCode=True,
        validationPlugin=None,
    )
    sample_type.save()

    sample_type.assign_property('SAMPLE_ARRAY_INTEGER')
    sample_type.assign_property('SAMPLE_ARRAY_REAL')
    sample_type.assign_property('SAMPLE_ARRAY_STRING')
    sample_type.assign_property('SAMPLE_ARRAY_TIMESTAMP')
    sample_type.assign_property('SAMPLE_JSON')

    sample = space.openbis.new_sample(
        type = sample_code,
        experiment = '/DEFAULT/DEFAULT/DEFAULT',
        props = { 'sample_array_integer': [1, 2, 3]})
    sample.save()

    assert sample.props['sample_array_integer'] == [1, 2, 3]

    sample.props['sample_array_integer'] = [3, 2, 1]
    sample.props['sample_array_real'] = [3.1, 2.2, 1.3]
    sample.props['sample_array_string'] = ["aa", "bb", "cc"]
    sample.props['sample_array_timestamp'] = ['2023-05-18 11:17:03', '2023-05-18 11:17:04',
                                              '2023-05-18 11:17:05']
    sample.props['sample_json'] = "{ \"key\": [1, 1, 1] }"
    sample.save()

    assert sample.props['sample_array_integer'] == [3, 2, 1]


def test_create_sample_type_assign_property(space):
    name_suffix = str(time.time())
    sc = "TEST_" + name_suffix
    pc = "ESFA_" + name_suffix
    ptc1 = "START_DATE_" + name_suffix
    ptc2 = "EXP_DESCRIPTION_" + name_suffix
    stc = "EXPERIMENTAL_STEP_MILAR_" + name_suffix

    # Create the new space and project
    sp = space.openbis.new_space(code=sc, description="Test space")
    sp.save()
    pr = space.openbis.new_project(code=pc, space=sc, description="ESFA experiments")
    pr.save()

    # Create the experiment
    exp = space.openbis.new_collection(code=pc, project="/" + sc + "/" + pc, type="COLLECTION")
    exp.save()

    # Create the sample type
    date_prop = space.openbis.new_property_type(code=ptc1, dataType="TIMESTAMP",
                                                   label="Start date",
                                                   description="Date of the measurement")
    date_prop.save()
    date_prop = space.openbis.new_property_type(code=ptc2, dataType="MULTILINE_VARCHAR",
                                                   label="Experimental description",
                                                   description="Experimental description")
    date_prop.save()
    st = space.openbis.new_sample_type(code=stc, generatedCodePrefix="EXSTEPMILAR")
    st.save()

    if st is None:
        print(space.openbis.get_sample_types())
        st = space.openbis.get_sample_type(stc)
        st.save()

    st.assign_property(ptc1)
    st.assign_property(ptc2)
    st.assign_property("$NAME")
    st.save()

