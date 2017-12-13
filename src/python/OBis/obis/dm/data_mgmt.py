#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt.py

Module implementing data management operations.

Created by Chandrasekhar Ramakrishnan on 2017-02-01.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import abc
import os
import shutil
import traceback
import pybis
from . import config as dm_config
from .commands.addref import Addref
from .commands.clone import Clone
from .commands.openbis_sync import OpenbisSync
from .command_result import CommandResult
from .git import GitWrapper
from .utils import default_echo
from .utils import complete_git_config
from .utils import complete_openbis_config
from .utils import cd


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, config_resolver=None, openbis_config={}, git_config={}, openbis=None):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    complete_git_config(git_config)
    git_wrapper = GitWrapper(**git_config)
    if not git_wrapper.can_run():
        return NoGitDataMgmt(config_resolver, None, git_wrapper, openbis)

    if config_resolver is None:
        config_resolver = dm_config.ConfigResolver()
        result = git_wrapper.git_top_level_path()
        if result.success():
            config_resolver.location_resolver.location_roots['data_set'] = result.output
    complete_openbis_config(openbis_config, config_resolver)

    return GitDataMgmt(config_resolver, openbis_config, git_wrapper, openbis)


class AbstractDataMgmt(metaclass=abc.ABCMeta):
    """Abstract object that implements operations.

    All operations throw an exepction if they fail.
    """

    def __init__(self, config_resolver, openbis_config, git_wrapper, openbis):
        self.config_resolver = config_resolver
        self.openbis_config = openbis_config
        self.git_wrapper = git_wrapper
        self.openbis = openbis

    def error_raise(self, command, reason):
        """Raise an exception."""
        message = "'{}' failed. {}".format(command, reason)
        raise ValueError(reason)

    @abc.abstractmethod
    def init_data(self, path, desc=None, create=True):
        """Initialize a data repository at the path with the description.
        :param path: Path for the repository.
        :param desc: An optional short description of the repository (used by git-annex)
        :param create: If True and the folder does not exist, create it. Defaults to true.
        :return: A CommandResult.
        """
        return

    @abc.abstractmethod
    def init_analysis(self, path):
        """Initialize an analysis repository at the path.
        :param path: Path for the repository.
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
    def clone(self, data_set_id, ssh_user, content_copy_index):
        """Clone / copy a repository related to the given data set id.
        :param data_set_id: 
        :param ssh_user: ssh user for remote clone (optional)
        :param content_copy_index: index of content copy in case there are multiple copies (optional)
        :return: A CommandResult.
        """
        return

    def addref(self):
        """Add the current folder as an obis repository to openBIS.
        :return: A CommandResult.
        """
        return


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def init_data(self, path, desc=None, create=True):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, path):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True, sync=True):
        self.error_raise("commit", "No git command found.")

    def sync(self):
        self.error_raise("sync", "No git command found.")

    def status(self):
        self.error_raise("status", "No git command found.")

    def clone(self, data_set_id, ssh_user, content_copy_index):
        self.error_raise("clone", "No git command found.")

    def addref(self):
        self.error_raise("addref", "No git command found.")


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def setup_local_config(self, config, path):
        with cd(path):
            self.config_resolver.location_resolver.location_roots['data_set'] = '.'
            for key, value in config.items():
                self.config_resolver.set_value_for_parameter(key, value, 'local')


    def check_repository_state(self, path):
        """Checks if the repo already exists and has uncommitted files."""
        with cd(path):
            git_status = self.git_wrapper.git_status()
            if git_status.failure():
                return 'NOT_INITIALIZED'
            if git_status.output is not None and len(git_status.output) > 0:
                return 'PENDING_CHANGES'
            return 'SYNCHRONIZED'


    def get_data_set_id(self, path):
        with cd(path):
            return self.config_resolver.config_dict().get('data_set_id')


    def init_data(self, path, desc=None, create=True, apply_config=False):
        if not os.path.exists(path) and create:
            os.mkdir(path)
        result = self.git_wrapper.git_init(path)
        if result.failure():
            return result
        result = self.git_wrapper.git_annex_init(path, desc)
        if result.failure():
            return result
        with cd(path):
            # Update the resolvers location
            self.config_resolver.location_resolver.location_roots['data_set'] = '.'
            self.config_resolver.copy_global_to_local() # TODO yn use config provided to data_mgmt
            self.commit_metadata_updates('local with global')
        return result

    def init_analysis(self, path):
        return self.git_wrapper.git_init(path)

    def sync(self):
        try:
            cmd = OpenbisSync(self)
            return cmd.run()
        except Exception:
            traceback.print_exc()
            return CommandResult(returncode=-1, output="Could not synchronize with openBIS.")


    def commit(self, msg, auto_add=True, sync=True, path=None):
        if path is not None:
            with cd(path):
                return self._commit(msg, auto_add, sync);
        else:
            return self._commit(msg, auto_add, sync);


    def _commit(self, msg, auto_add=True, sync=True):
        self.set_restorepoint()
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
            result = self.sync()
            if result.failure():
                self.restore()
        return result

    def status(self):
        return self.git_wrapper.git_status()

    def commit_metadata_updates(self, msg_fragment=None):
        folder = self.config_resolver.local_public_config_folder_path()
        status = self.git_wrapper.git_status(folder)
        if len(status.output.strip()) < 1:
            # Nothing to commit
            return CommandResult(returncode=0, output="")
        self.git_wrapper.git_add(folder)
        if msg_fragment is None:
            msg = "OBIS: Update openBIS metadata cache."
        else:
            msg = "OBIS: Update {}.".format(msg_fragment)
        return self.git_wrapper.git_commit(msg)

    def set_restorepoint(self):
        self.previous_git_commit_hash = self.git_wrapper.git_commit_hash().output

    def restore(self):
        self.git_wrapper.git_reset_to(self.previous_git_commit_hash)
        folder = self.config_resolver.local_public_config_folder_path()
        self.git_wrapper.git_checkout(folder)

    def clone(self, data_set_id, ssh_user, content_copy_index):
        try:
            cmd = Clone(self, data_set_id, ssh_user, content_copy_index)
            return cmd.run()
        except Exception:
            traceback.print_exc()
            return CommandResult(returncode=-1, output="Could not clone repository.")

    def addref(self):
        try:
            cmd = Addref(self)
            return cmd.run()
        except Exception:
            traceback.print_exc()
            return CommandResult(returncode=-1, output="Could not add reference.")
