#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
import abc
import json
import os
import shutil
import signal
import sys
from pathlib import Path

import requests

from . import config as dm_config
from .command_result import CommandResult
from .commands.addref import Addref
from .commands.clone import Clone
from .commands.collection import Collection
from .commands.download_physical import DownloadPhysical
from .commands.move import Move
from .commands.object import Object
from .commands.openbis_sync import OpenbisSync
from .commands.removeref import Removeref
from .commands.search import Search
from .commands.upload import Upload
from .git import GitWrapper
from .utils import Type, OperationType
from .utils import cd
from .utils import complete_git_config
from .utils import complete_openbis_config
from .utils import default_echo
from ..scripts.click_util import click_echo, check_result


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, settings_resolver=None, openbis_config={}, git_config={},
             openbis=None, log=None, debug=False, login=True, repository_type=Type.UNKNOWN):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    data_path = git_config['data_path']
    metadata_path = git_config['metadata_path']
    invocation_path = git_config['invocation_path']

    if settings_resolver is None:
        settings_resolver = dm_config.SettingsResolver()

    if repository_type == Type.UNKNOWN:
        if os.path.exists('.obis'):
            config_dict = settings_resolver.config.config_dict()
            if config_dict['is_physical'] is True:
                repository_type = Type.PHYSICAL
            else:
                repository_type = Type.LINK
        else:
            repository_type = Type.LINK

    if repository_type == Type.PHYSICAL:
        return PhysicalDataMgmt(settings_resolver, None, None, openbis, log, data_path,
                                metadata_path, invocation_path)
    else:
        complete_git_config(git_config)
        git_wrapper = GitWrapper(**git_config)
        if not git_wrapper.can_run():
            # TODO We could just as well throw an error here instead of creating
            #      creating the NoGitDataMgmt which will fail later.
            return NoGitDataMgmt(settings_resolver, None, git_wrapper, openbis, log, data_path,
                                 metadata_path, invocation_path)

        complete_openbis_config(openbis_config, settings_resolver)
        return GitDataMgmt(settings_resolver, openbis_config, git_wrapper, openbis, log, data_path,
                           metadata_path, invocation_path, debug, login)


class AbstractDataMgmt(metaclass=abc.ABCMeta):
    """Abstract object that implements operations.

    All operations throw an exepction if they fail.
    """

    def __init__(self, settings_resolver, openbis_config, git_wrapper, openbis, log, data_path,
                 metadata_path, invocation_path, debug=False, login=True):
        self.settings_resolver = settings_resolver
        self.openbis_config = openbis_config
        self.git_wrapper = git_wrapper
        self.openbis = openbis
        self.log = log
        self.data_path = data_path
        self.metadata_path = metadata_path
        self.invocation_path = invocation_path
        self.debug = debug
        self.login = login

        # setting flags
        self.restore_on_sigint = True
        self.ignore_missing_parent = False

    def error_raise(self, command, reason):
        """Raise an exception."""
        message = "'{}' failed. {}".format(command, reason)
        raise ValueError(message)

    @abc.abstractmethod
    def get_settings_resolver(self):
        """ Get the settings resolver """
        return

    @abc.abstractmethod
    def setup_local_settings(self, all_settings):
        """ Setup local settings - for using obis as a library """
        return

    @abc.abstractmethod
    def init_data(self, desc=None):
        """Initialize a data repository at the path with the description.
        :param path: Path for the repository.
        :param desc: An optional short description of the repository (used by git-annex)
        :param create: If True and the folder does not exist, create it. Defaults to true.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def init_analysis(self, parent_folder, desc=None):
        """Initialize an analysis repository at the path.
        :param parent_folder: Path for the repository.
        :param parent: (required when outside of existing repository) Path for the parent repositort
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def commit(self, msg, auto_add=True, sync=True):
        """Commit the current repo.

        This issues a git commit and connects to openBIS and creates a data set in openBIS.
        :param msg: Commit message.
        :param auto_add: Automatically add all files in the folder to the repo. Defaults to True.
        :param sync: If true, sync with openBIS server.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def sync(self):
        """Sync the current repo.

        This connects to openBIS and creates a data set in openBIS.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def status(self):
        """Return the status of the current repository.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def clone(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        """Clone / copy a repository related to the given data set id.
        :param data_set_id: 
        :param ssh_user: ssh user for remote system (optional)
        :param content_copy_index: index of content copy in case there are multiple copies (optional)
        :param skip_integrity_check: if true, the file checksums will not be checked
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def move(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        """Move a repository related to the given data set id.
        :param data_set_id: 
        :param ssh_user: ssh user for remote system (optional)
        :param content_copy_index: index of content copy in case there are multiple copies (optional)
        :param skip_integrity_check: if true, the file checksums will not be checked
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def addref(self):
        """Add the current folder as an obis repository to openBIS.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def removeref(self, data_set_id=None):
        """Remove the current folder / repository from openBIS.
        :param data_set_id: Id of the data from which a reference should be removed.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def download(self, data_set_id, from_file, file, skip_integrity_check):
        """Download files of a repository without adding a content copy.
        :param data_set_id: Id of the data set to download from.
        :param from_file: Path of a file with a list of datasets to download.
        :param file: Path of a file in the data set to download. All files are downloaded if it is None.
        :param skip_integrity_check: Checksums of files are not verified if true.
        """
        return

    @abc.abstractmethod
    def upload(self, sample_id, data_set_type, files):
        """Upload files/directories into a new data set.
        :param sample_id: permId or sample path of the parent sample
        :param data_set_type: type of created data set
        :param files: list of files/directories to upload
        """
        return

    @abc.abstractmethod
    def search_object(self, type_code, space, project, experiment, property_code, property_value,
                      save):
        """Search for objects in openBIS using filtering criteria.
        :param type_code: Type of searched object.
        :param space: Space path to filter object.
        :param project: Project path to filter object.
        :param experiment: Experiment path to filter object.
        :param property_code: Custom property code to search by, property_value must be set as well.
        :param property_value: Custom property value to search by, property_code must be set as well.
        :param save: File path to save results. If missing, search results will not be saved.
        """
        return

    @abc.abstractmethod
    def search_data_set(self, type_code, space, project, experiment, property_code, property_value,
                        save):
        """Search for datasets in openBIS using filtering criteria.
        :param type_code: Type of searched object.
        :param space: Space path to filter object.
        :param project: Project path to filter object.
        :param experiment: Experiment path to filter object.
        :param property_code: Custom property code to search by, property_value must be set as well.
        :param property_value: Custom property value to search by, property_code must be set as well.
        :param save: File path to save results. If missing, search results will not be saved.
        """
        return

    def update_config(self, resolver, debug, is_global, is_data_set_property, operation_type,
                      prop=None,
                      value=None):
        if is_global:
            resolver.set_location_search_order(['global'])
        else:
            resolver.set_location_search_order(['local'])

        config_dict = resolver.config_dict()
        if is_data_set_property:
            config_dict = config_dict['properties']

        if operation_type is OperationType.GET:
            if prop is None:
                config_str = json.dumps(config_dict, indent=4, sort_keys=True)
                click_echo("{}".format(config_str), with_timestamp=False)
            else:
                if not prop in config_dict:
                    raise ValueError(
                        "Unknown setting {} for {}.".format(prop, resolver.categoty))
                little_dict = {prop: config_dict[prop]}
                config_str = json.dumps(little_dict, indent=4, sort_keys=True)
                click_echo("{}".format(config_str), with_timestamp=False)
        elif operation_type is OperationType.SET:
            return check_result("config",
                                self.set_property(debug, resolver, prop, value, is_global,
                                                  is_data_set_property))
        elif operation_type is OperationType.CLEAR:
            if prop is None:
                return_code = 0
                for prop in config_dict.keys():
                    return_code += check_result("config",
                                                self.set_property(debug, resolver, prop, None,
                                                                  is_global, is_data_set_property))
                return return_code
            else:
                return check_result("config",
                                    self.set_property(debug, resolver, prop, None, is_global,
                                                      is_data_set_property))

    @staticmethod
    def set_property(debug, resolver, prop, value, is_global, is_data_set_property=False):
        """Helper function to implement the property setting semantics."""
        loc = 'global' if is_global else 'local'
        try:
            if is_data_set_property:
                resolver.set_value_for_json_parameter('properties', prop, value, loc,
                                                      apply_rules=True)
            else:
                resolver.set_value_for_parameter(prop, value, loc, apply_rules=True)
        except Exception as e:
            if debug is True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))
        else:
            return CommandResult(returncode=0, output="")


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def get_settings_resolver(self):
        self.error_raise("get settings resolver", "No git command found.")

    def setup_local_settings(self, all_settings):
        self.error_raise("setup local settings", "No git command found.")

    def init_data(self, desc=None):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, parent_folder, desc=None):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True, sync=True):
        self.error_raise("commit", "No git command found.")

    def sync(self):
        self.error_raise("sync", "No git command found.")

    def status(self):
        self.error_raise("status", "No git command found.")

    def clone(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        self.error_raise("clone", "No git command found.")

    def move(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        self.error_raise("move", "No git command found.")

    def addref(self):
        self.error_raise("addref", "No git command found.")

    def removeref(self, data_set_id=None):
        self.error_raise("removeref", "No git command found.")

    def download(self, *_):
        self.error_raise("download", "No git command found.")

    def search_object(self, *_):
        self.error_raise("search", "No git command found.")

    def search_data_set(self, *_):
        self.error_raise("search", "No git command found.")

    def upload(self, *_):
        self.error_raise("upload", "No git command found.")


def restore_signal_handler(data_mgmt):
    data_mgmt.restore()
    sys.exit(0)


def with_log(f):
    """ To be used with commands that use the CommandLog. """

    def f_with_log(self, *args):
        try:
            result = f(self, *args)
        except Exception as e:
            self.log.log_error(str(e))
            raise e
        if result.failure() == False:
            self.log.success()
        else:
            self.log.log_error(result.output)
        return result

    return f_with_log


def with_restore(f):
    """ Sets the restore point and restores on error. """

    def f_with_restore(self, *args):
        self.set_restorepoint()
        try:
            if self.restore_on_sigint:
                signal.signal(signal.SIGINT, lambda signal, frame: restore_signal_handler(self))
            result = f(self, *args)
            if result.failure():
                self.restore()
            self.clear_restorepoint()
            return result
        except Exception as e:
            self.restore()
            if self.debug == True:
                raise e
            self.clear_restorepoint()
            return CommandResult(returncode=-1, output="Error: " + str(e))

    return f_with_restore


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def get_settings_resolver(self, relative_path=None):
        if relative_path is None:
            return self.settings_resolver
        else:
            settings_resolver = dm_config.SettingsResolver()
            settings_resolver.set_resolver_location_roots('data_set', relative_path)
            return settings_resolver

    # TODO add this to abstract / other class
    def setup_local_settings(self, all_settings):
        self.settings_resolver.set_resolver_location_roots('data_set', '.')
        for resolver_type, settings in all_settings.items():
            resolver = getattr(self.settings_resolver, resolver_type)
            for key, value in settings.items():
                resolver.set_value_for_parameter(key, value, 'local')

    def get_data_set_id(self, relative_path):
        settings_resolver = self.get_settings_resolver(relative_path)
        return settings_resolver.repository.config_dict().get('data_set_id')

    def get_repository_id(self, relative_path):
        settings_resolver = self.get_settings_resolver(relative_path)
        return settings_resolver.repository.config_dict().get('id')

    def init_data(self, desc=None):
        # check that repository does not already exist
        if os.path.exists('.obis'):
            return CommandResult(returncode=-1, output="Folder is already an obis repository.")
        result = self.git_wrapper.git_init()
        if result.failure():
            return result
        git_annex_backend = self.settings_resolver.config.config_dict().get('git_annex_backend')
        result = self.git_wrapper.git_annex_init(desc, git_annex_backend)
        if result.failure():
            return result
        result = self.git_wrapper.initial_commit()
        if result.failure():
            return result
        # Update the resolvers location
        self.settings_resolver.set_resolver_location_roots('data_set', '.')
        self.settings_resolver.copy_global_to_local()
        return CommandResult(returncode=0, output="")

    def init_analysis(self, parent_folder, desc=None):
        # get data_set_id of parent from current folder or explicit parent argument
        parent_data_set_id = self.get_data_set_id(parent_folder)
        # check that parent repository has been added to openBIS
        if self.get_repository_id(parent_folder) is None:
            return CommandResult(returncode=-1,
                                 output="Parent data set must be committed to openBIS before creating an analysis data set.")
        # init analysis repository
        result = self.init_data(desc)
        if result.failure():
            return result

        # add analysis repository folder to .gitignore of parent
        parent_folder_abs = os.path.join(os.getcwd(), parent_folder)
        analysis_folder_abs = os.getcwd()
        if Path(analysis_folder_abs) in Path(parent_folder_abs).parents:
            analysis_folder_relative = os.path.relpath(analysis_folder_abs, parent_folder_abs)
            with cd(parent_folder):
                self.git_wrapper.git_ignore(analysis_folder_relative)

        # set data_set_id to analysis repository so it will be used as parent when committing
        try:
            self.settings_resolver.repository.set_value_for_parameter("data_set_id",
                                                                      parent_data_set_id, "local",
                                                                      apply_rules=True)
        except Exception as e:
            if self.debug is True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))
        else:
            return CommandResult(returncode=0, output="")

    @with_restore
    def sync(self):
        return self._sync()

    def _sync(self):
        cmd = OpenbisSync(self, self.ignore_missing_parent)
        return cmd.run()

    @with_restore
    def commit(self, msg, auto_add=True, sync=True):
        """ Git add, commit and sync with openBIS. """
        if auto_add:
            result = self.git_wrapper.git_top_level_path()
            if result.failure():
                return result
            result = self.git_wrapper.git_add(result.output)
            if result.failure():
                return result
        result = self.git_wrapper.git_commit(msg)
        if result.failure():
            # TODO If no changes were made check if the data set is in openbis. If not, just sync.
            return result
        if sync:
            result = self._sync()
        return result

    def status(self):
        git_status = self.git_wrapper.git_status()
        try:
            sync_status = OpenbisSync(self).run(info_only=True)
        except requests.exceptions.ConnectionError:
            sync_status = CommandResult(returncode=-1, output="Could not connect to openBIS.")
        output = git_status.output
        if sync_status.failure():
            if len(output) > 0:
                output += '\n'
            output += sync_status.output
        return CommandResult(returncode=0, output=output)

    def set_restorepoint(self):
        """ Stores the git commit hash and copies the obis metadata. """
        self.previous_git_commit_hash = self.git_wrapper.git_commit_hash().output
        self.clear_restorepoint()
        shutil.copytree('.obis', '.obis_restorepoint')

    def restore(self):
        """ Resets to the stored git commit hash and restores the copied obis metadata. """
        self.git_wrapper.git_reset_to(self.previous_git_commit_hash)
        shutil.rmtree('.obis')
        shutil.copytree('.obis_restorepoint', '.obis')

    def clear_restorepoint(self):
        """ Deletes the obis metadata copy. This must always be done. """
        if os.path.exists('.obis_restorepoint'):
            shutil.rmtree('.obis_restorepoint')

    def clone(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        cmd = Clone(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check)
        return cmd.run()

    @with_log
    def move(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        cmd = Move(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check)
        return cmd.run()

    def addref(self):
        cmd = Addref(self)
        return cmd.run()

    def removeref(self, data_set_id=None):
        cmd = Removeref(self, data_set_id=data_set_id)
        return cmd.run()

    def download(self, data_set_id, from_file, file, skip_integrity_check):
        self.error_raise("download", "This command is only available for Manager Data.")

    #
    # settings
    #

    def config(self, category, is_global, is_data_set_property, operation_type, prop=None,
               value=None):
        """
        :param category: config, object, collection, data_set or repository
        :param is_global: act on global settings - local if false
        :param is_data_set_property: true if prop / value are a data set property
        :param operation_type: type of operation to perform. It can be GET, SET, CLEAR
        :param prop: setting key
        :param value: setting value
        """
        resolver = self.settings_resolver.get(category)
        if resolver is None:
            raise ValueError('Invalid settings category: ' + category)
        if operation_type is OperationType.SET:
            assert prop is not None
            assert value is not None
        elif operation_type is OperationType.GET:
            assert value is None
        elif operation_type is OperationType.CLEAR:
            assert value is None

        return self.update_config(resolver, self.debug, is_global, is_data_set_property,
                                  operation_type, prop, value)

    def search_object(self, *_):
        self.error_raise("search", "This command is only available for Manager Data.")

    def search_data_set(self, *_):
        self.error_raise("search", "This command is only available for Manager Data.")

    def upload(self, *_):
        self.error_raise("upload", "This command is only available for Manager Data.")


class PhysicalDataMgmt(AbstractDataMgmt):
    """DataMgmt operations for DSS-stored data."""

    def get_settings_resolver(self):
        return dm_config.SettingsResolver()

    def setup_local_settings(self, all_settings):
        self.error_raise("setup local settings",
                         "This command is only available for External Manager Data")

    def init_data(self, desc=None):
        if os.path.exists('.obis'):
            return CommandResult(returncode=-1, output="Folder is already an obis repository.")
        self.settings_resolver.config.copy_global_to_local()
        self.settings_resolver.config.set_value_for_parameter("is_physical", True, "local")
        openbis_url = self.settings_resolver.config.config_dict()['openbis_url']
        self.settings_resolver.config.set_value_for_parameter("fileservice_url",
                                                              openbis_url, "local")
        return CommandResult(returncode=0, output="Managed data obis repository initialized.")

    def init_analysis(self, parent_folder, desc=None):
        self.error_raise("init analysis",
                         "This command is only available for External Manager Data")

    def commit(self, msg, auto_add=True, sync=True):
        self.error_raise("commit", "This command is only available for External Manager Data")

    def sync(self):
        self.error_raise("sync", "This command is only available for External Manager Data")

    def status(self):
        self.error_raise("status", "This command is only available for External Manager Data")

    def clone(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        self.error_raise("clone", "This command is only available for External Manager Data")

    def move(self, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        self.error_raise("move", "This command is only available for External Manager Data")

    def addref(self):
        self.error_raise("addref", "This command is only available for External Manager Data")

    def removeref(self, data_set_id=None):
        self.error_raise("removeref", "This command is only available for External Manager Data")

    def download(self, data_set_id, from_file, file, skip_integrity_check):
        cmd = DownloadPhysical(self, data_set_id, from_file, file, skip_integrity_check)
        return cmd.run()

    def upload(self, sample_id, data_set_type, files):
        cmd = Upload(self, sample_id, data_set_type, files)
        return cmd.run()

    def search_object(self, type_code, space, project, experiment, property_code, property_value,
                      save):
        cmd = Search(self, type_code, space, project, experiment, property_code, property_value,
                     save)
        return cmd.search_samples()

    def search_data_set(self, type_code, space, project, experiment, property_code, property_value,
                        save):
        cmd = Search(self, type_code, space, project, experiment, property_code, property_value,
                     save)
        return cmd.search_data_sets()

    def config(self, category, is_global, is_data_set_property, operation_type, prop=None,
               value=None):
        """
        :param category: config, object, collection, data_set or repository
        :param is_global: act on global settings - local if false
        :param is_data_set_property: true if prop / value are a data set property
        :param operation_type: type of operation to perform. It can be GET, SET, CLEAR
        :param prop: setting key
        :param value: setting value
        """
        resolver = self.settings_resolver.get(category)
        if resolver is None:
            raise ValueError('Invalid settings category: ' + category)
        if operation_type is OperationType.SET:
            assert prop is not None
            assert value is not None
        elif operation_type is OperationType.GET:
            assert value is None
        elif operation_type is OperationType.CLEAR and category != "config":
            self.error_raise(f"{category} clear",
                             "This command is only available for External Manager Data")

        if category == "object":
            cmd = Object(self, operation_type, prop, value)
            return cmd.run()
        elif category == "collection":
            cmd = Collection(self, operation_type, prop, value)
            return cmd.run()
        elif category == "config":
            return self.update_config(resolver, self.debug, is_global, is_data_set_property,
                                      operation_type, prop, value)
        else:
            self.error_raise(f"{category} {operation_type}",
                             "This command is only available for External Manager Data")
