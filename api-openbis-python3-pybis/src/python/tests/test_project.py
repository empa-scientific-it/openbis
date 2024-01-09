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
import os
import random
import time

import pytest


def test_create_delete_project(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    project = o.new_project(space=space, code="illegal title contains spaces")
    with pytest.raises(ValueError):
        project.save()
        assert "should not have been created" is None

    project_name = "project_" + timestamp + "_" + str(random.randint(0, 1000))
    project = o.new_project(space=space, code=project_name)
    project.save()

    project_exists = o.get_project(project_name)
    assert project_exists is not None

    projects_exist = o.get_projects()
    assert len(projects_exist) > 0
    first_project = projects_exist[0]
    assert first_project is not None

    project_exists.delete("test project on " + timestamp)

    with pytest.raises(ValueError):
        project_no_longer_exists = o.get_project(project_name, use_cache=False)


def test_create_project_with_attachment(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    project_name = "project_" + timestamp + "_" + str(random.randint(0, 1000))
    filename = os.path.join(os.path.dirname(__file__), "testdir/testfile")

    if not os.path.exists(filename):
        raise ValueError("File not found: {}".format(filename))

    project = o.new_project(space=space, code=project_name, attachments=filename)
    assert project.attachments is not None
    project.save()

    project_exists = o.get_project(project_name)
    assert project_exists is not None
    assert project_exists.attachments is not None


def test_get_project_by_code(space):
    o = space.openbis

    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()

    space_code_1 = "space_1_" + timestamp + "_" + str(random.randint(0, 1000))
    project_code = "project_" + timestamp

    o.new_space(code=space_code_1).save()

    o.new_project(space=space_code_1, code=project_code).save()
    project_exists = o.get_project(project_code)
    assert project_exists is not None

#  TODO fix this test
# def test_get_project_fail_because_of_multiple_projects_existing(space):
#     o = space.openbis
#
#     timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
#
#     space_code_1 = "space_1_" + timestamp + "_" + str(random.randint(0, 1000))
#     space_code_2 = "space_2_" + timestamp + "_" + str(random.randint(0, 1000))
#     project_code = "project_" + timestamp
#
#     o.new_space(code=space_code_1).save()
#     o.new_space(code=space_code_2).save()
#
#     o.new_project(space=space_code_1, code=project_code).save()
#     o.new_project(space=space_code_2, code=project_code).save()
#
#     # Delay to allow openbis instance to properly register projects
#     time.sleep(5)
#
#     with pytest.raises(ValueError):
#         project_exists = o.get_project(project_code)


