#!/usr/bin/env python
# -*- coding: utf-8 -*-

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

"""
config_test.py


Created by Chandrasekhar Ramakrishnan on 2017-02-10.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
import os
import shutil

from . import config


def test_config_location_resolver():
    loc = config.ConfigLocation(['global'], 'user_home', '.obis')
    location_resolver = config.LocationResolver()
    assert location_resolver.resolve_location(loc) == os.path.join(os.path.expanduser("~"), '.obis')


def user_config_test_data_path():
    return os.path.join(os.path.dirname(__file__), '..', 'test-data', 'user_config')


def copy_user_config_test_data(tmpdir):
    config_test_data_src = user_config_test_data_path()
    config_test_data_dst = str(tmpdir.join(os.path.basename(config_test_data_src)))
    shutil.copytree(config_test_data_src, config_test_data_dst)
    return config_test_data_src, config_test_data_dst


def configure_resolver_for_test(resolver, tmpdir):
    resolver.set_resolver_location_roots('user_home', os.path.join(str(tmpdir), 'user_config'))


def test_read_config(tmpdir):
    copy_user_config_test_data(tmpdir)
    resolver = config.SettingsResolver().config
    configure_resolver_for_test(resolver, tmpdir)
    config_dict = resolver.config_dict()
    assert config_dict is not None
    with open(os.path.join(user_config_test_data_path(), ".obis", "config.json")) as f:
        expected_dict = json.load(f)
    assert config_dict['user'] == expected_dict['user']
    assert './.obis/config.json' == resolver.local_public_properties_path()


def test_write_config(tmpdir):
    copy_user_config_test_data(tmpdir)
    resolver = config.SettingsResolver().config
    configure_resolver_for_test(resolver, tmpdir)
    config_dict = resolver.config_dict()
    assert config_dict is not None
    with open(os.path.join(user_config_test_data_path(), ".obis", "config.json")) as f:
        expected_dict = json.load(f)
    assert config_dict['openbis_url'] == expected_dict['openbis_url']
    assert config_dict['user'] == expected_dict['user']

    resolver.set_value_for_parameter('user', 'new_user', 'local')
    config_dict = resolver.config_dict()
    assert config_dict['openbis_url'] == expected_dict['openbis_url']
    assert config_dict['user'] == 'new_user'
