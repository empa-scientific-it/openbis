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
from . import utils


def test_locate_command():
    result = utils.locate_command("bash")
    assert result.returncode == 0
    assert result.output == "/bin/bash"

    result = utils.locate_command("this_is_not_a_real_command")
    # Bash returns 127 if a command is not found
    assert ((result.returncode == 1) or (result.returncode == 127))