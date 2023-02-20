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
from random import randint


def test_crud_tag(openbis_instance):
    tag_name = "test_tag_{}".format(randint(0, 1000)).upper()
    description = "description of tag " + tag_name

    tag = openbis_instance.new_tag(code=tag_name, description=description)

    assert tag.code == tag_name
    assert tag.description == description
    assert tag.permId == ""

    tag.save()

    assert tag.permId is not None

    tag_exists = openbis_instance.get_tag(tag.permId)
    assert tag_exists is not None

    tag_by_code = openbis_instance.get_tag(tag.code)
    assert tag_by_code is not None
    assert tag_by_code.permId == tag_exists.permId

    altered_description = "altered description of tag " + tag_name
    tag.description = altered_description
    tag.save()
    assert tag.description == altered_description

    tag.delete("test")

    with pytest.raises(ValueError):
        tag_does_not_exists = openbis_instance.get_tag(tag.permId, use_cache=False)


def test_get_tags(openbis_instance):
    tags = openbis_instance.get_tags()
    assert tags is not None
    assert tags.__class__.__name__ == "Things"
    assert tags.df.__class__.__name__ == "DataFrame"

    if len(tags) > 0:
        assert tags[0].__class__.__name__ == "Tag"

    if len(tags) > 1:
        tag1 = tags[0]
        tag2 = tags[1]

        tag_coll = openbis_instance.get_tag([tag1.permId, tag2.permId])
        assert len(tag_coll) == 2
        assert tag_coll.__class__.__name__ == "Things"
        assert tag_coll.df.__class__.__name__ == "DataFrame"
