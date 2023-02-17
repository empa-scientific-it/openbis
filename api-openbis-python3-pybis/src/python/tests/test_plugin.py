#   Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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


def test_create_delete_plugin(openbis_instance):
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    pl_name = 'test_plugin_' + timestamp

    plugin = openbis_instance.new_plugin(
	name= pl_name,
	pluginType='ENTITY_VALIDATION',   # or 'DYNAMIC_PROPERTY' or 'MANAGED_PROPERTY',
	entityKind = None,                # or 'SAMPLE', 'MATERIAL', 'EXPERIMENT', 'DATA_SET'
	script = 'def calculate(): pass'  # JYTHON script
    )

    with pytest.raises(ValueError):
        plugin.pluginType = 'Rubbish'
        assert 1 == 0

    with pytest.raises(ValueError):
        plugin.entityKind = 'even more Rubbish'
        assert 1 == 0

    plugin.save()
    pl_exists = openbis_instance.get_plugin(pl_name)
    assert pl_exists is not None

    plugin.delete("test on {}".format(timestamp))

    with pytest.raises(ValueError):
        pl_not_exists = openbis_instance.get_plugin(plugin.name)
        assert pl_not_exists is None

def test_search_plugins(openbis_instance):
    plugins = openbis_instance.get_plugins(start_with=1, count=1)
    assert len(plugins) == 1
    assert plugins.df.__class__.__name__ == 'DataFrame'
    assert len(plugins) == len(plugins.df)

    plugin = plugins[0]
    assert plugin.__class__.__name__ == 'Plugin'

