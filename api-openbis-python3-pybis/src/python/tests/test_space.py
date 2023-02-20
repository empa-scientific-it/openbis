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
import re

import pytest
import time
from pybis import DataSet
from pybis import Openbis


def test_create_delete_space(openbis_instance):
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    space_name = "test_space_" + timestamp
    space = openbis_instance.new_space(code=space_name)
    space.save()
    space_exists = openbis_instance.get_space(code=space_name)
    assert space_exists is not None

    space.delete("test on {}".format(timestamp))

    with pytest.raises(ValueError):
        space_not_exists = openbis_instance.get_space(code=space_name, use_cache=False)
