#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt.py

Module implementing data management operations.

Created by Chandrasekhar Ramakrishnan on 2017-02-01.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import abc
import json
import os
import shutil
import traceback
import pybis
import requests
import signal
import sys
from . import config as dm_config
from .commands.addref import Addref
from .commands.removeref import Removeref
from .commands.clone import Clone
from .commands.move import Move
from .commands.openbis_sync import OpenbisSync
from .commands.download import Download
from .command_log import CommandLog
from .command_result import CommandResult
from .command_result import CommandException
from .git import GitWrapper
from .utils import default_echo
from .utils import complete_git_config
from .utils import complete_openbis_config
from .utils import cd
from ..scripts import cli
from ..scripts.click_util import click_echo, check_result


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, settings_resolver=None, openbis_config={}, git_config={}, openbis=None, log=None, debug=False):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    complete_git_config(git_config)
    git_wrapper = GitWrapper(**git_config)
    if not git_wrapper.can_run():
        return NoGitDataMgmt(settings_resolver, None, git_wrapper, openbis, log)

    if settings_resolver is None:
        settings_resolver = dm_config.SettingsResolver()
        result = git_wrapper.git_top_level_path()
        if result.success():
            settings_resolver.set_resolver_location_roots('data_set', result.output)
    complete_openbis_config(openbis_config, settings_resolver)

    return GitDataMgmt(settings_resolver, openbis_config, git_wrapper, openbis, log, debug)


class AbstractDataMgmt(metaclass=abc.ABCMeta):
    """Abstract object that implements operations.

    All operations throw an exepction if they fail.
    """

    def __init__(self, settings_resolver, openbis_config, git_wrapper, openbis, log, debug=False):
        self.settings_resolver = settings_resolver
        self.openbis_config = openbis_config
        self.git_wrapper = git_wrapper
        self.openbis = openbis
        self.log = log
        self.debug = debug

    def error_raise(self, command, reason):
        """Raise an exception."""
        message = "'{}' failed. {}".format(command, reason)
        raise ValueError(message)

    @abc.abstractmethod
    def get_settings_resolver(self):
        """ Get the settings resolver """
        return

    @abc.abstractmethod
    def init_data(self, desc=None, create=True):
        """Initialize a data repository at the path with the description.
        :param path: Path for the repository.
        :param desc: An optional short description of the repository (used by git-annex)
        :param create: If True and the folder does not exist, create it. Defaults to true.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def init_analysis(self, path, parent, desc=None, create=True, apply_config=False):
        """Initialize an analysis repository at the path.
        :param path: Path for the repository.
        :param parent: (required when outside of existing repository) Path for the parent repositort
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def commit(self, msg, auto_add=True, ignore_missing_parent=False, sync=True):
        """Commit the current repo.

        This issues a git commit and connects to openBIS and creates a data set in openBIS.
        :param msg: Commit message.
        :param auto_add: Automatically add all files in the folder to the repo. Defaults to True.
        :param sync: If true, sync with openBIS server.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def sync(self, ignore_missing_parent=False):
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
    def download(self, data_set_id, content_copy_index, file, skip_integrity_check):
        """Download files of a repository without adding a content copy.
        :param data_set_id: Id of the data set to download from.
        :param content_copy_index: Index of the content copy to download from.
        :param file: Path of a file in the data set to download. All files are downloaded if it is None.
        :param skip_integrity_check: Checksums of files are not verified if true.
        """
        return


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def get_settings_resolver(self):
        self.error_raise("get settings resolver", "No git command found.")

    def init_data(self, desc=None, create=True):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, path, parent, desc=None, create=True, apply_config=False):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True, ignore_missing_parent=False, sync=True):
        self.error_raise("commit", "No git command found.")

    def sync(self, ignore_missing_parent=False):
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

    def download(self, data_set_id, content_copy_index, file, skip_integrity_check):
        self.error_raise("download", "No git command found.")


def restore_signal_handler(data_mgmt):
    data_mgmt.restore()
    sys.exit(0)


def with_log(f):
    def f_with_log(self, *args):
        try:
            result = f(self, *args)
        except Exception as e:
            self.log.log_error(str(e))
            raise e
        if result.failure() ==  False:
            self.log.success()
        else:
            self.log.log_error(result.output)
        return result
    return f_with_log


def with_restore(f):
    def f_with_restore(self, *args):
        self.set_restorepoint()
        try:
            signal.signal(signal.SIGINT, lambda signal, frame: restore_signal_handler(self))
            result = f(self, *args)
            if result.failure():
                self.restore()
            return result
        except Exception as e:
            self.restore()
            if self.debug == True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))
    return f_with_restore


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def get_settings_resolver(self):
        return self.settings_resolver


    def setup_local_settings(self, all_settings):
        self.settings_resolver.set_resolver_location_roots('data_set', '.')
        for resolver_type, settings in all_settings.items():
            resolver = getattr(self.settings_resolver, resolver_type)
            for key, value in settings.items():
                resolver.set_value_for_parameter(key, value, 'local')


    def check_repository_state(self):
        """Checks if the repo already exists and has uncommitted files."""
        git_status = self.git_wrapper.git_status()
        if git_status.failure():
            return ('NOT_INITIALIZED', None)
        if git_status.output is not None and len(git_status.output) > 0:
            return ('PENDING_CHANGES', git_status.output)
        return ('SYNCHRONIZED', None)


    def get_data_set_id(self, relative_path):
        with cd(relative_path):
            return self.settings_resolver.repository.config_dict().get('data_set_id')

    def get_repository_id(self, relative_path):
        with cd(relative_path):
            return self.settings_resolver.repository.config_dict().get('id')

    def init_data(self, desc=None, create=True, apply_config=False):
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
        self.commit_metadata_updates('local with global')
        return CommandResult(returncode=0, output="")


    def init_analysis(self, relative_path, parent, desc=None, create=True, apply_config=False):

        # get data_set_id of parent from current folder or explicit parent argument
        parent_folder = parent if parent is not None and len(parent) > 0 else "."
        parent_data_set_id = self.get_data_set_id(parent_folder)
        # check that parent repository has been added to openBIS
        if self.get_repository_id(parent_folder) is None:
            return CommandResult(returncode=-1, output="Parent data set must be committed to openBIS before creating an analysis data set.")
        # check that analysis repository does not already exist
        if os.path.exists(relative_path):
            return CommandResult(returncode=-1, output="Data set already exists: " + relative_path)
        # init analysis repository
        result = self.init_data(relative_path, desc, create, apply_config)
        if result.failure():
            return result
        # add analysis repository folder to .gitignore of parent
        if os.path.exists('.obis'):
            self.git_wrapper.git_ignore(relative_path)
        elif parent is None:
            return CommandResult(returncode=-1, output="Not within a repository and no parent set.")
        # set data_set_id to analysis repository so it will be used as parent when committing
        with cd(relative_path):
            self.set_property(self.settings_resolver.repository, "data_set_id", parent_data_set_id, False, False)
        return result


    @with_restore
    def sync(self, ignore_missing_parent=False):
        return self._sync(ignore_missing_parent)


    def _sync(self, ignore_missing_parent=False):
        cmd = OpenbisSync(self, ignore_missing_parent)
        return cmd.run()


    @with_restore
    def commit(self, msg, auto_add=True, ignore_missing_parent=False, sync=True):
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
            result = self._sync(ignore_missing_parent)
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

    def commit_metadata_updates(self, msg_fragment=None, omit_usersettings=True):
        properties_paths = self.settings_resolver.local_public_properties_paths(omit_usersettings=omit_usersettings)
        total_status = ''
        for properties_path in properties_paths:
            status = self.git_wrapper.git_status(properties_path).output.strip()
            total_status += status
            if len(status) > 0:
                self.git_wrapper.git_add(properties_path)
        if len(total_status) < 1:
            # Nothing to commit
            return CommandResult(returncode=0, output="")
        if msg_fragment is None:
            msg = "OBIS: Update openBIS metadata cache."
        else:
            msg = "OBIS: Update {}.".format(msg_fragment)
        return self.git_wrapper.git_commit(msg)

    def set_restorepoint(self):
        self.previous_git_commit_hash = self.git_wrapper.git_commit_hash().output

    def restore(self):
        self.git_wrapper.git_reset_to(self.previous_git_commit_hash)
        properties_paths = self.settings_resolver.local_public_properties_paths()
        for properties_path in properties_paths:
            self.git_wrapper.git_checkout(properties_path)
            self.git_wrapper.git_delete_if_untracked(properties_path)

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

    def download(self, data_set_id, content_copy_index, file, skip_integrity_check):
        cmd = Download(self, data_set_id, content_copy_index, file, skip_integrity_check)
        return cmd.run()

    #
    # settings
    #

    def config(self, resolver, is_global, is_data_set_property, prop=None, value=None, set=False, get=False, clear=False):
        if set == True:
            assert get == False
            assert clear == False
            assert prop is not None
            assert value is not None
        elif get == True:
            assert set == False
            assert clear == False
            assert value is None
        elif clear == True:
            assert get == False
            assert set == False
            assert value is None

        assert set == True or get == True or clear == True
        if is_global:
            resolver.set_location_search_order(['global'])
        else:
            top_level_path = self.git_wrapper.git_top_level_path()
            if top_level_path.success():
                resolver.set_resolver_location_roots('data_set', top_level_path.output)
                resolver.set_location_search_order(['local'])
            else:
                resolver.set_location_search_order(['global'])

        config_dict = resolver.config_dict()
        if is_data_set_property:
            config_dict = config_dict['properties']
        if get == True:
            if prop is None:
                config_str = json.dumps(config_dict, indent=4, sort_keys=True)
                click_echo("{}".format(config_str), with_timestamp=False)
            else:
                if not prop in config_dict:
                    raise ValueError("Unknown setting {} for {}.".format(prop, resolver.categoty))
                little_dict = {prop: config_dict[prop]}
                config_str = json.dumps(little_dict, indent=4, sort_keys=True)
                click_echo("{}".format(config_str), with_timestamp=False)
        elif set == True:
            return check_result("config", self.set_property(resolver, prop, value, is_global, is_data_set_property))
        elif clear == True:
            if prop is None:
                returncode = 0
                for prop in config_dict.keys():
                    returncode += check_result("config", self.set_property(resolver, prop, None, is_global, is_data_set_property))
                return returncode
            else:
                return check_result("config", self.set_property(resolver, prop, None, is_global, is_data_set_property))

    def set_property(self, resolver, prop, value, is_global, is_data_set_property=False):
        """Helper function to implement the property setting semantics."""
        loc = 'global' if is_global else 'local'
        try:
            if is_data_set_property:
                resolver.set_value_for_json_parameter('properties', prop, value, loc, apply_rules=True)
            else:
                resolver.set_value_for_parameter(prop, value, loc, apply_rules=True)
        except ValueError as e:
            if self.debug ==  True:
                raise e
            return CommandResult(returncode=-1, output="Error: " + str(e))
        if not is_global:
            return self.commit_metadata_updates(prop)
        else:
            return CommandResult(returncode=0, output="")
