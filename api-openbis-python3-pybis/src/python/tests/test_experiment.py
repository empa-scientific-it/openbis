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


def test_create_delete_experiment(space):
    o=space.openbis
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    new_code='test_experiment_'+timestamp

    with pytest.raises(TypeError):
        # experiments must be assigned to a project
        e_new = o.new_experiment(
            code=new_code,
            type='UNKNOWN',
        )

    project = o.get_projects()[0]

    e_new = o.new_experiment(
        code=new_code,
        project=project,
        type='UNKNOWN',
    )
    assert e_new.project is not None
    assert e_new.permId == ''

    e_new.save()

    assert e_new.permId is not None
    assert e_new.code == new_code.upper()

    e_exists = o.get_experiment(e_new.permId)
    assert e_exists is not None

    e_new.delete('delete test experiment '+new_code.upper())

    with pytest.raises(ValueError):
        e_no_longer_exists = o.get_experiment(e_exists.permId)


def test_get_experiments(space):
    # test paging
    o=space.openbis
    current_datasets = o.get_experiments(start_with=1, count=1)
    assert current_datasets is not None
    # we cannot assert == 1, because search is delayed due to lucene search...
    assert len(current_datasets) <= 1

