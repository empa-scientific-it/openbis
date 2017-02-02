#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
data_mgmt.py

Module implementing data management operations.

Created by Chandrasekhar Ramakrishnan on 2017-02-01.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import subprocess

import pybis
import abc


# noinspection PyPep8Naming
def DataMgmt(echo_func=None, openbis_config={}, git_config={}):
    """Factory method for DataMgmt instances"""

    echo_func = echo_func if echo_func is not None else default_echo

    complete_openbis_config(openbis_config)
    complete_git_config(git_config)

    openbis = pybis.Openbis(**openbis_config)
    git_wrapper = GitWrapper(**git_config)
    if git_wrapper.can_run():
        return GitDataMgmt(echo_func, openbis, git_wrapper)
    return NoGitDataMgmt(echo_func, openbis, git_wrapper)


def complete_openbis_config(config):
    """Add default values for empty entries in the config."""
    if not config.get('url'):
        config['url'] = 'https://localhost:8443'
    if not config.get('verify_certificates'):
        config['verify_certificates'] = True
    if not config.get('token'):
        config['token'] = None


def complete_git_config(config):
    """Add default values for empty entries in the config."""

    find_git = config['find_git'] if config.get('find_git') else False
    if find_git:
        config['git_path'] = locate_command('git')
        config['git_annex_path'] = locate_command('git-annex')


def default_echo(details):
    if details.get('level') != "DEBUG":
        print(details['message'])


def locate_command(command):
    """Return a tuple of (returncode, stdout)."""
    result = subprocess.run(['type', '-p', command], stdout=subprocess.PIPE)
    return result.returncode, result.stdout.decode('utf-8').strip()


class AbstractDataMgmt(metaclass=abc.ABCMeta):
    """Abstract object that implements operations.

    All operations throw an exepction if they fail.
    """

    def __init__(self, echo_func, openbis, git_wrapper):
        self.echo_func = echo_func
        self.openbis = openbis
        self.git_wrapper = git_wrapper

    def echo(self, level, message):
        details = {'level': level, 'message': message}
        self.echo_func(details)

    def info(self, message):
        """Print info to the user."""
        self.echo('info', message)

    def error(self, message):
        """Print an error to the user"""
        self.echo('error', message)

    def error_raise(self, command, reason):
        """Print an error to the user and raise an exception."""
        message = "'{}' failed. {}".format(command, reason)
        self.echo('error', message)
        raise ValueError(reason)

    @abc.abstractmethod
    def init_data(self, path):
        return


class NoGitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations when git is not available -- show error messages."""

    def init_data(self, path):
        self.error_raise("init data", "No git command found.")


class GitDataMgmt(AbstractDataMgmt):
    """DataMgmt operations in normal state."""

    def init_data(self, path):
        return


class GitWrapper(object):
    """A wrapper on commands to git."""

    def __init__(self, git_path=None, git_annex_path=None):
        self.git_path = git_path
        self.git_annex_path = git_annex_path

    def can_run(self):
        """Return true if the perquisites are satisfied to run"""
        if self.git_path is None:
            return False
        if self.git_annex_path is None:
            return False
        return False
