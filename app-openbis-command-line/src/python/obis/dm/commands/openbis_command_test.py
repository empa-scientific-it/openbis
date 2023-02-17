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

import getpass
from unittest.mock import Mock, MagicMock, ANY

from .openbis_command import OpenbisCommand
from .. import data_mgmt


def test_prepare_run(monkeypatch):
    # given
    dm = data_mgmt.DataMgmt(openbis=Mock(), git_config={
        'data_path': '',
        'metadata_path': '',
        'invocation_path': ''
    })
    openbis_command = OpenbisCommand(dm)
    define_config(openbis_command)
    monkeypatch.setattr(getpass, 'getpass', lambda s: 'password')
    dm.openbis.is_session_active.return_value = False
    # when
    openbis_command.prepare_run()
    # then
    dm.openbis.is_session_active.assert_called()
    dm.openbis.login.assert_called_with('watney', 'password', save_token=True)


def define_config(openbis_command):
    openbis_command.config_dict = {
        'config': {
            'user': 'watney'
        }
    }
    
