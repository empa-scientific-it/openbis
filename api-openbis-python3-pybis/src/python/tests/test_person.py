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
import json
import random
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_admin_person_roles(openbis_instance):
    admin = openbis_instance.get_person(userId="admin")
    assert admin is not None

    # test role assignments
    roles = admin.get_roles()
    assert len(roles) > 0

    admin.assign_role("OBSERVER")

    roles = admin.get_roles()
    observer_role_exists = False
    for role in roles:
        if role.role == "OBSERVER":
            observer_role_exists = True
    assert observer_role_exists

    admin.assign_role(role="OBSERVER", space="DEFAULT")
    space_roles_exist = admin.get_roles(space="DEFAULT")
    assert len(space_roles_exist) == 1

    admin.revoke_role(role="OBSERVER")
    roles_exist = admin.get_roles()
    assert len(roles_exist) > 1

    admin.revoke_role(role="OBSERVER", space="DEFAULT")
    roles_exist = admin.get_roles()
    assert len(roles_exist) == 1
