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
import subprocess
from contextlib import contextmanager
from . import config as dm_config
import traceback
import getpass
import socket
import uuid
import hashlib

import pybis


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


def complete_openbis_config(config, resolver):
    """Add default values for empty entries in the config."""
    config_dict = resolver.config_dict(local_only=True)
    if config.get('url') is None:
        config['url'] = config_dict['openbis_url']
    if config.get('verify_certificates') is None:
        if config_dict.get('verify_certificates') is not None:
            config['verify_certificates'] = config_dict['verify_certificates']
        else:
            config['verify_certificates'] = True
    if config.get('token') is None:
        config['token'] = None


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


class CommandResult(object):
    """Encapsulate result from a subprocess call."""

    def __init__(self, completed_process=None, returncode=None, output=None):
        """Convert a completed_process object into a ShellResult."""
        if completed_process:
            self.returncode = completed_process.returncode
            self.output = completed_process.stdout.decode('utf-8').strip()
        else:
            self.returncode = returncode
            self.output = output

    def __str__(self):
        return "CommandResult({},{})".format(self.returncode, self.output)

    def __repr__(self):
        return "CommandResult({},{})".format(self.returncode, self.output)

    def success(self):
        return self.returncode == 0

    def failure(self):
        return not self.success()


def run_shell(args, shell=False):
    return CommandResult(subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=shell))


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


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def init_data(self, path, desc=None, create=True):
        self.error_raise("init data", "No git command found.")

    def init_analysis(self, path):
        self.error_raise("init analysis", "No git command found.")

    def commit(self, msg, auto_add=True, sync=True):
        self.error_raise("commit", "No git command found.")

    def sync(self):
        self.error_raise("commit", "No git command found.")

    def status(self):
        self.error_raise("commit", "No git command found.")


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def setup_local_config(self, config, path):
        with cd(path):
            self.config_resolver.location_resolver.location_roots['data_set'] = '.'
            for key, value in config.items():
                self.config_resolver.set_value_for_parameter(key, value, 'local')


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


class GitWrapper(object):
    """A wrapper on commands to git."""

    def __init__(self, git_path=None, git_annex_path=None, find_git=None):
        self.git_path = git_path
        self.git_annex_path = git_annex_path

    def can_run(self):
        """Return true if the perquisites are satisfied to run"""
        if self.git_path is None:
            return False
        if self.git_annex_path is None:
            return False
        if run_shell([self.git_path, 'help']).failure():
            # git help should have a returncode of 0
            return False
        if run_shell([self.git_annex_path, 'help']).failure():
            # git help should have a returncode of 0
            return False
        return True

    def git_init(self, path):
        return run_shell([self.git_path, "init", path])

    def git_status(self, path=None):
        if path is None:
            return run_shell([self.git_path, "status", "--porcelain"])
        else:
            return run_shell([self.git_path, "status", "--porcelain", path])

    def git_annex_init(self, path, desc):
        cmd = [self.git_path, "-C", path, "annex", "init", "--version=6"]
        if desc is not None:
            cmd.append(desc)
        result = run_shell(cmd)
        if result.failure():
            return result

        cmd = [self.git_path, "-C", path, "config", "annex.thin", "true"]
        result = run_shell(cmd)
        if result.failure():
            return result

        attributes_src = os.path.join(os.path.dirname(__file__), "git-annex-attributes")
        attributes_dst = os.path.join(path, ".gitattributes")
        shutil.copyfile(attributes_src, attributes_dst)
        cmd = [self.git_path, "-C", path, "add", ".gitattributes"]
        result = run_shell(cmd)
        if result.failure():
            return result

        cmd = [self.git_path, "-C", path, "commit", "-m", "Initial commit."]
        result = run_shell(cmd)
        return result

    def git_add(self, path):
        return run_shell([self.git_path, "add", path])

    def git_commit(self, msg):
        return run_shell([self.git_path, "commit", '-m', msg])

    def git_top_level_path(self):
        return run_shell([self.git_path, 'rev-parse', '--show-toplevel'])

    def git_commit_hash(self):
        return run_shell([self.git_path, 'rev-parse', '--short', 'HEAD'])

    def git_ls_tree(self):
        return run_shell([self.git_path, 'ls-tree', '--full-tree', '-r', 'HEAD'])

    def git_checkout(self, path):
        return run_shell([self.git_path, "checkout", path])

    def git_reset_to(self, commit_hash):
        return run_shell([self.git_path, 'reset', commit_hash])


class OpenbisSync(object):
    """A command object for synchronizing with openBIS."""

    def __init__(self, dm, openbis=None):
        self.data_mgmt = dm
        self.git_wrapper = dm.git_wrapper
        self.config_resolver = dm.config_resolver
        self.config_dict = dm.config_resolver.config_dict()
        self.openbis = dm.openbis

        if self.openbis is None and dm.openbis_config.get('url') is not None:
            self.openbis = pybis.Openbis(**dm.openbis_config)

    def user(self):
        return self.config_dict.get('user')

    def external_dms_id(self):
        return self.config_dict.get('external_dms_id')

    def repository_id(self):
        return self.config_dict.get('repository_id')

    def data_set_type(self):
        return self.config_dict.get('data_set_type')

    def data_set_id(self):
        return self.config_dict.get('data_set_id')

    def data_set_properties(self):
        return self.config_dict.get('data_set_properties')

    def sample_id(self):
        return self.config_dict.get('sample_id')

    def experiment_id(self):
        return self.config_dict.get('experiment_id')

    def check_configuration(self):
        missing_config_settings = []
        if self.openbis is None:
            missing_config_settings.append('openbis_url')
        if self.user() is None:
            missing_config_settings.append('user')
        if self.data_set_type() is None:
            missing_config_settings.append('data_set_type')
        if self.sample_id() is None and self.experiment_id() is None:
            missing_config_settings.append('sample_id')
            missing_config_settings.append('experiment_id')
        if len(missing_config_settings) > 0:
            return CommandResult(returncode=-1,
                                 output="Missing configuration settings for {}.".format(missing_config_settings))
        return CommandResult(returncode=0, output="")

    def check_data_set_status(self):
        """If we are in sync with the data set on the server, there is nothing to do."""
        # TODO Get the DataSet from the server
        #  - Find the content copy that refers to this repo
        #  - Check if the commit id is the current commit id
        #  - If so, skip sync.
        return CommandResult(returncode=0, output="")

    def login(self):
        if self.openbis.is_session_active():
            return CommandResult(returncode=0, output="")
        user = self.user()
        passwd = getpass.getpass("Password for {}:".format(user))
        try:
            self.openbis.login(user, passwd, save_token=True)
        except ValueError:
            msg = "Could not log into openbis {}".format(self.config_dict['openbis_url'])
            return CommandResult(returncode=-1, output=msg)
        return CommandResult(returncode=0, output='')

    def generate_external_data_management_system_code(self, user, hostname, edms_path):
        path_hash = hashlib.sha1(edms_path.encode("utf-8")).hexdigest()[0:8]
        return "{}-{}-{}".format(user, hostname, path_hash).upper()

    def get_or_create_external_data_management_system(self):
        external_dms_id = self.external_dms_id()
        user = self.user()
        hostname = socket.gethostname()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        edms_path, path_name = os.path.split(result.output)
        if external_dms_id is None:
            external_dms_id = self.generate_external_data_management_system_code(user, hostname, edms_path)
        try:
            external_dms = self.openbis.get_external_data_management_system(external_dms_id.upper())
        except ValueError as e:
            # external dms does not exist - create it
            try:
                external_dms = self.openbis.create_external_data_management_system(external_dms_id, external_dms_id,
                                                                    "{}:/{}".format(hostname, edms_path))
            except ValueError as e:
                return CommandResult(returncode=-1, output=str(e))
        return CommandResult(returncode=0, output=external_dms)

    def create_data_set_code(self):
        try:
            data_set_code = self.openbis.create_permId()
            return CommandResult(returncode=0, output=""), data_set_code
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None

    def create_data_set(self, data_set_code, external_dms, repository_id):
        data_set_type = self.data_set_type()
        parent_data_set_id = self.data_set_id()
        properties = self.data_set_properties()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        result = self.git_wrapper.git_commit_hash()
        if result.failure():
            return result
        commit_id = result.output
        sample_id = self.sample_id()
        experiment_id = self.experiment_id()
        contents = GitRepoFileInfo(self.git_wrapper).contents()
        try:
            data_set = self.openbis.new_git_data_set(data_set_type, top_level_path, commit_id, repository_id, external_dms.code,
                                                     sample_id, experiment_id, data_set_code=data_set_code, parents=parent_data_set_id,
                                                     properties=properties, contents=contents)
            return CommandResult(returncode=0, output=""), data_set
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None

    def commit_metadata_updates(self, msg_fragment=None):
        return self.data_mgmt.commit_metadata_updates(msg_fragment)

    def prepare_run(self):
        result = self.check_configuration()
        if result.failure():
            return result
        result = self.login()
        if result.failure():
            return result
        return CommandResult(returncode=0, output="")

    def prepare_repository_id(self):
        repository_id = self.repository_id()
        if self.repository_id() is None:
            repository_id = str(uuid.uuid4())
            self.config_resolver.set_value_for_parameter('repository_id', repository_id, 'local')
        return CommandResult(returncode=0, output=repository_id)


    def prepare_external_dms(self):
        # If there is no external data management system, create one.
        result = self.get_or_create_external_data_management_system()
        if result.failure():
            return result
        external_dms = result.output
        self.config_resolver.set_value_for_parameter('external_dms_id', external_dms.code, 'local')
        return result

    def run(self):
        # TODO Write mementos in case openBIS is unreachable
        # - write a file to the .git/obis folder containing the commit id. Filename includes a timestamp so they can be sorted.

        result = self.prepare_run()
        if result.failure():
            return result

        result = self.prepare_repository_id()
        if result.failure():
            return result
        repository_id = result.output

        result = self.prepare_external_dms()
        if result.failure():
            return result
        external_dms = result.output

        result, data_set_code = self.create_data_set_code()
        if result.failure():
            return result

        self.commit_metadata_updates()

        # Update data set id as last commit so we can easily revert it on failure
        self.config_resolver.set_value_for_parameter('data_set_id', data_set_code, 'local')
        self.commit_metadata_updates("data set id")

        # create a data set, using the existing data set as a parent, if there is one
        result, data_set = self.create_data_set(data_set_code, external_dms, repository_id)
        return result


class GitRepoFileInfo(object):
    """Class that gathers checksums and file lengths for all files in the repo."""

    def __init__(self, git_wrapper):
        self.git_wrapper = git_wrapper

    def contents(self):
        """Return a list of dicts describing the contents of the repo.
        :return: A list of dictionaries
          {'crc32': checksum,
           'fileLength': size of the file,
           'path': path relative to repo root.
           'directory': False
          }"""
        files = self.file_list()
        cksum = self.cksum(files)
        return cksum

    def file_list(self):
        tree = self.git_wrapper.git_ls_tree()
        if tree.failure():
            return []
        lines = tree.output.split("\n")
        files = [line.split("\t")[-1].strip() for line in lines]
        return files

    def cksum(self, files):
        cmd = ['cksum']
        cmd.extend(files)
        result = run_shell(cmd)
        if result.failure():
            return []
        lines = result.output.split("\n")
        return [self.checksum_line_to_dict(line) for line in lines]

    @staticmethod
    def checksum_line_to_dict(line):
        fields = line.split(" ")
        return {
            'crc32': int(fields[0]),
            'fileLength': int(fields[1]),
            'path': fields[2]
        }
