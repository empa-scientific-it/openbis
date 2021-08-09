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
