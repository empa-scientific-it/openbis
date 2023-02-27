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
import subprocess
from contextlib import contextmanager
from datetime import datetime
from enum import Enum

from .command_result import CommandResult, CommandException


class Type(Enum):
    """Enum representing which what type of repository it is"""
    LINK = 1,
    PHYSICAL = 2,
    UNKNOWN = 3


class OperationType(Enum):
    """Enum representing basic operation types"""
    GET = 1,
    SET = 2,
    CLEAR = 3


def complete_openbis_config(config, resolver, local_only=True):
    """Add default values for empty entries in the config."""
    config_dict = resolver.config.config_dict(local_only)
    if config.get('url') is None:
        config['url'] = config_dict['openbis_url']
    if config.get('verify_certificates') is None:
        config['verify_certificates'] = config_dict['verify_certificates']
    if config.get('token') is None:
        config['token'] = None
    if config.get('is_physical') is None:
        config['is_physical'] = None
    if config.get(
            'allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks') is None:
        config['allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks'] = not \
        config_dict['allow_only_https']


def complete_git_config(config):
    """Add default values for empty entries in the config."""

    find_git = config['find_git'] if config.get('find_git') is not None else True
    if find_git:
        git_cmd = locate_command('git')
        if git_cmd.success():
            config['git_path'] = git_cmd.output

        git_annex_cmd = locate_command('git-annex')
        if git_annex_cmd.success():
            config['git_annex_path'] = git_annex_cmd.output


def default_echo(details):
    if details.get('level') != "DEBUG":
        print(details['message'])


def run_shell(args, shell=False, strip_leading_whitespace=True, raise_exception_on_failure=False):
    result = CommandResult(
        subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=shell),
        strip_leading_whitespace=strip_leading_whitespace)
    if raise_exception_on_failure == True and result.failure():
        raise CommandException(result)
    return result


def locate_command(command):
    """Return a tuple of (returncode, stdout)."""
    # Need to call this command in shell mode so we have the system PATH
    result = run_shell(['type {}'.format(command)], shell=True)
    # 'type -p' not supported by all shells, so we do it manually
    if result.success():
        result.output = result.output.split(" ")[-1]
    return result


@contextmanager
def cd(newdir):
    """Safe cd -- return to original dir after execution, even if an exception is raised."""
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)


def is_valid_perm_id(name):
    if "-" not in name:
        return False
    split = name.split("-")
    try:
        datetime.strptime(split[0], "%Y%m%d%H%M%S%f")
        int(split[1])
        return True
    except ValueError:
        return False
