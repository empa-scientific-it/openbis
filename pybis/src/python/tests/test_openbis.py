import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_token(openbis_instance):
    assert openbis_instance.token is not None
    assert openbis_instance.is_token_valid(openbis_instance.token) is True
    assert openbis_instance.is_session_active() is True


def test_http_only(openbis_instance):
    with pytest.raises(Exception):
        new_instance = Openbis("http://localhost")
        assert new_instance is None

    new_instance = Openbis(
        url="http://localhost",
        allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=True,
    )
    assert new_instance is not None


def test_cached_token(other_openbis_instance):
    assert other_openbis_instance.is_token_valid() is True

    other_openbis_instance.logout()
    assert other_openbis_instance.is_token_valid() is False


def test_create_permId(openbis_instance):
    permId = openbis_instance.create_permId()
    assert permId is not None
    m = re.search("([0-9]){17}-([0-9]*)", permId)
    ts = m.group(0)
    assert ts is not None
    count = m.group(1)
    assert count is not None


def test_get_samples_update_in_transaction(openbis_instance):
    '''
        Update samples in transaction without overriding parents/children
    '''
    name_suffix = str(time.time())
    # Create new space
    space = openbis_instance.new_space(code='space_name' + name_suffix, description='')
    space.save()

    # Create new project
    project = space.new_project(code='project_code' + name_suffix)
    project.save()

    # Create new experiment
    experiment = openbis_instance.new_experiment(
        code='MY_NEW_EXPERIMENT',
        type='DEFAULT_EXPERIMENT',
        project=project.code
    )
    experiment.save()

    # Create parent sample
    sample1 = openbis_instance.new_sample(
        type='YEAST',
        space=space.code,
        experiment=experiment.identifier,
        parents=[],
        children=[],
        props={"$name": "sample1"}
    )
    sample1.save()

    # Create child sample
    sample2 = openbis_instance.new_sample(
        type='YEAST',
        space=space.code,
        experiment=experiment.identifier,
        parents=[sample1],
        children=[],
        props={"$name": "sample2"}
    )
    sample2.save()

    # Verify samples parent/child relationship
    sample1 = openbis_instance.get_sample(
        sample_ident=sample1.identifier,
        space=space.code,
        props="*"
    )
    sample2 = openbis_instance.get_sample(
        sample_ident=sample2.identifier,
        space=space.code,
        props="*"
    )
    assert sample1.children == [sample2.identifier]
    assert sample2.parents == [sample1.identifier]

    trans = openbis_instance.new_transaction()
    # get samples that have parents and update name
    samples = openbis_instance.get_samples(space=space.code, props="*", withParents="*")
    for sample in samples:
        sample.props["$name"] = 'new name for sample2'
        trans.add(sample)
    # get samples that have children and update name
    samples = openbis_instance.get_samples(space=space.code, props="*", withChildren="*")
    for sample in samples:
        sample.props["$name"] = 'new name for sample1'
        trans.add(sample)
    trans.commit()

    # Verify that name has been changed and parent/child relationship remains
    sample1 = openbis_instance.get_sample(
        sample_ident=sample1.identifier,
        space=space.code,
        props="*"
    )
    sample2 = openbis_instance.get_sample(
        sample_ident=sample2.identifier,
        space=space.code,
        props="*"
    )
    assert sample1.props["$name"] == 'new name for sample1'
    assert sample1.children == [sample2.identifier]
    assert sample2.props["$name"] == 'new name for sample2'
    assert sample2.parents == [sample1.identifier]

    trans = openbis_instance.new_transaction()
    # get samples with attributes and change name
    samples = openbis_instance.get_samples(space=space.code, attrs=["parents", "children"])
    for sample in samples:
        sample.props["$name"] = "default name"
        trans.add(sample)
    trans.commit()

    # Verify that name has been changed and parent/child relationship remains
    sample1 = openbis_instance.get_sample(
        sample_ident=sample1.identifier,
        space=space.code,
        props="*"
    )
    sample2 = openbis_instance.get_sample(
        sample_ident=sample2.identifier,
        space=space.code,
        props="*"
    )
    assert sample1.props["$name"] == 'default name'
    assert sample1.children == [sample2.identifier]
    assert sample2.props["$name"] == 'default name'
    assert sample2.parents == [sample1.identifier]

    sample3 = openbis_instance.new_sample(
        type='YEAST',
        space=space.code,
        experiment=experiment.identifier,
        parents=[],
        children=[],
        props={"$name": "sample3"}
    )
    sample3.save()

    trans = openbis_instance.new_transaction()
    # get sample1 without attributes and add sample3 as a parent
    samples = openbis_instance.get_samples(space=space.code, identifier=sample1.identifier)
    for sample in samples:
        sample.add_parents([sample3.identifier])
        trans.add(sample)
    # get sample2 without attributes and remove sample1 as a parent
    samples = openbis_instance.get_samples(space=space.code, identifier=sample2.identifier)
    for sample in samples:
        sample.del_parents([sample1.identifier])
        trans.add(sample)
    trans.commit()

    # Verify changes
    sample1 = openbis_instance.get_sample(
        sample_ident=sample1.identifier,
        space=space.code,
        props="*"
    )
    sample2 = openbis_instance.get_sample(
        sample_ident=sample2.identifier,
        space=space.code,
        props="*"
    )
    sample3 = openbis_instance.get_sample(
        sample_ident=sample3.identifier,
        space=space.code,
        props="*"
    )
    assert sample1.children == []
    assert sample1.parents == [sample3.identifier]
    assert sample2.parents == []
    assert sample3.children == [sample1.identifier]
