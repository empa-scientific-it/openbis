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
import socket
from .commands.openbis_command import CommandResult
from .utils import run_shell


def copy_repository(ssh_user, host, path):
    # abort if local folder already exists
    repository_folder = path.split('/')[-1]
    if os.path.exists(repository_folder):
        return CommandResult(returncode=-1, output="Folder for repository to clone already exists: " + repository_folder)
    # check if local or remote
    location = get_repository_location(ssh_user, host, path)
    # copy repository
    return run_shell(["rsync", "--progress", "-av", location, "."])


def delete_repository(ssh_user, host, path):
    if is_local(host):
        result = run_shell(["chmod", "-R",  "u+w", path])
        if result.failure():
            return result
        return run_shell(["rm", "-rf", path])
    else:
        location = ssh_user + "@" if ssh_user is not None else ""
        location += host
        return run_shell(["ssh", location, "rm -rf " + path])


def is_local(host):
    return host == socket.gethostname()


def get_repository_location(ssh_user, host, path):
    if is_local(host):
        location = path
    else:
        location = ssh_user + "@" if ssh_user is not None else ""
        location += host + ":" + path
    return location
