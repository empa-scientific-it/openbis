#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
cli.py

The module that implements the CLI for obis.


Created by Chandrasekhar Ramakrishnan on 2017-01-27.
Copyright (c) 2017 Chandrasekhar Ramakrishnan. All rights reserved.
"""
import json
import sys
from datetime import datetime

import click

from .. import dm
from ..dm.command_result import CommandResult
from ..dm.command_result import CommandException
from ..dm.utils import cd
from ..dm.command_log import CommandLog
from .data_mgmt_runner import DataMgmtRunner
from .click_util import click_echo, check_result
from .config_util import set_property


def click_progress(progress_data):
    if progress_data['type'] == 'progress':
        click_echo(progress_data['message'])


def click_progress_no_ts(progress_data):
    if progress_data['type'] == 'progress':
        click.echo("{}".format(progress_data['message']))


def add_params(params):
    def _add_params(func):
        for param in reversed(params):
            func = param(func)
        return func
    return _add_params


@click.group()
@click.version_option(version=None)
@click.option('-q', '--quiet', default=False, is_flag=True, help='Suppress status reporting.')
@click.option('-s', '--skip_verification', default=False, is_flag=True, help='Do not verify cerficiates')
@click.option('-d', '--debug', default=False, is_flag=True, help="Show stack trace on error.")
@click.pass_context
def cli(ctx, quiet, skip_verification, debug):
    ctx.obj['quiet'] = quiet
    if skip_verification:
        ctx.obj['verify_certificates'] = False
    ctx.obj['debug'] = debug


def init_data_impl(ctx, object_id, collection_id, repository, desc):
    """Shared implementation for the init_data command."""
    if repository is None:
        repository = "."
    click_echo("init_data {}".format(repository))
    runner = DataMgmtRunner(ctx)
    desc = desc if desc != "" else None
    result = runner.run(lambda: runner.dm.init_data(repository, desc, create=True))
    runner.init_handle_cleanup(result, object_id, collection_id, repository, runner.dm)


def init_analysis_impl(ctx, parent, object_id, collection_id, repository, description):
    click_echo("init_analysis {}".format(repository))
    runner = DataMgmtRunner(ctx)
    description = description if description != "" else None
    result = runner.run(lambda: runner.dm.init_analysis(repository, parent, description, create=True))
    runner.init_handle_cleanup(result, object_id, collection_id, repository, runner.dm)


# settings commands


class SettingsGet(click.ParamType):
    name = 'settings_get'

    def convert(self, value, param, ctx):
        try:
            split = list(filter(lambda term: len(term) > 0, value.split(',')))
            return split
        except:
            self._fail(param)

    def _fail(self, param):
            self.fail(param=param, message='Settings must be in the format: key1, key2, ...')


class SettingsClear(SettingsGet):
    pass


class SettingsSet(click.ParamType):
    name = 'settings_set'

    def convert(self, value, param, ctx):
        try:
            value = self._encode_json(value)
            settings = {}
            split = list(filter(lambda term: len(term) > 0, value.split(',')))
            for setting in split:
                setting_split = setting.split('=')
                if len(setting_split) != 2:
                    self._fail(param)
                key = setting_split[0]
                value = setting_split[1]
                settings[key] = self._decode_json(value)
            return settings
        except:
            self._fail(param)

    def _encode_json(self, value):
        encoded = ''
        SEEK = 0
        ENCODE = 1
        mode = SEEK
        for char in value:
            if char == '{':
                mode = ENCODE
            elif char == '}':
                mode = SEEK
            if mode == SEEK:
                encoded += char
            elif mode == ENCODE:
                encoded += char.replace(',', '|')
        return encoded

    def _decode_json(self, value):
        return value.replace('|', ',')

    def _fail(self, param):
            self.fail(param=param, message='Settings must be in the format: key1=value1, key2=value2, ...')


def _join_settings_set(setting_dicts):
    joined = {}
    for setting_dict in setting_dicts:
        for key, value in setting_dict.items():
            joined[key] = value
    return joined


def _join_settings_get(setting_lists):
    joined = []
    for setting_list in setting_lists:
        joined += setting_list
    return joined


def config_internal(data_mgmt, resolver, is_global, is_data_set_property, prop=None, value=None, set=False, get=False, clear=False):
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
        top_level_path = data_mgmt.git_wrapper.git_top_level_path()
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
            click.echo("{}".format(config_str))
        else:
            if not prop in config_dict:
                raise ValueError("Unknown setting {} for {}.".format(prop, resolver.categoty))
            little_dict = {prop: config_dict[prop]}
            config_str = json.dumps(little_dict, indent=4, sort_keys=True)
            click.echo("{}".format(config_str))            
    elif set == True:
        return check_result("config", set_property(data_mgmt, resolver, prop, value, is_global, is_data_set_property))
    elif clear == True:
        if prop is None:
            returncode = 0
            for prop in config_dict.keys():
                returncode += check_result("config", set_property(data_mgmt, resolver, prop, None, is_global, is_data_set_property))
            return returncode
        else:
            return check_result("config", set_property(data_mgmt, resolver, prop, None, is_global, is_data_set_property))


def _access_settings(ctx, prop=None, value=None, set=False, get=False, clear=False):
    is_global = ctx.obj['is_global']
    data_mgmt = ctx.obj['data_mgmt']
    resolver = ctx.obj['resolver']
    is_data_set_property = False
    if 'is_data_set_property' in ctx.obj:
        is_data_set_property = ctx.obj['is_data_set_property']
    config_internal(data_mgmt, resolver, is_global, is_data_set_property, prop=prop, value=value, set=set, get=get, clear=clear)


def _set(ctx, settings):
    settings_dict = _join_settings_set(settings)
    for prop, value in settings_dict.items():
        _access_settings(ctx, prop=prop, value=value, set=True)
    return CommandResult(returncode=0, output='')


def _get(ctx, settings):
    settings_list = _join_settings_get(settings)
    if len(settings_list) == 0:
        settings_list = [None]
    for prop in settings_list:
        _access_settings(ctx, prop=prop, get=True)
    return CommandResult(returncode=0, output='')


def _clear(ctx, settings):
    settings_list = _join_settings_get(settings)
    if len(settings_list) == 0:
        settings_list = [None]
    for prop in settings_list:
        _access_settings(ctx, prop=prop, clear=True)
    return CommandResult(returncode=0, output='')


## get all settings

@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Get global or local.')
@click.pass_context
def settings(ctx, is_global):
    """ Get all settings.
    """
    ctx.obj['is_global'] = is_global


@settings.command('get')
@click.pass_context
def settings_get(ctx):
    runner = DataMgmtRunner(ctx, halt_on_error_log=False)
    settings = runner.dm.settings_resolver.config_dict()
    settings_str = json.dumps(settings, indent=4, sort_keys=True)
    click.echo("{}".format(settings_str))


## repository: repository_id, external_dms_id, data_set_id

@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def repository(ctx, is_global):
    """ Get/set settings related to the repository.
    """
    runner = DataMgmtRunner(ctx, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = runner.dm
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.repository


@repository.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def repository_set(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("repository_set", runner.run(lambda: _set(ctx, settings)))


@repository.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def repository_get(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("repository_get", runner.run(lambda: _get(ctx, settings)))


@repository.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def repository_clear(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("repository_clear", runner.run(lambda: _clear(ctx, settings)))


## data_set: type, properties


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.option('-p', '--is_data_set_property', default=False, is_flag=True, help='Configure data set property.')
@click.pass_context
def data_set(ctx, is_global, is_data_set_property):
    """ Get/set settings related to the data set.
    """
    runner = DataMgmtRunner(ctx, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['is_data_set_property'] = is_data_set_property
    ctx.obj['data_mgmt'] = runner.dm
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.data_set


@data_set.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def data_set_set(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("data_set_set", runner.run(lambda: _set(ctx, settings)))


@data_set.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def data_set_get(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("data_set_get", runner.run(lambda: _get(ctx, settings)))


@data_set.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def data_set_clear(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("data_set_clear", runner.run(lambda: _clear(ctx, settings)))


## object: object_id


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def object(ctx, is_global):
    """ Get/set settings related to the object.
    """
    runner = DataMgmtRunner(ctx, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = runner.dm
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.object


@object.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def object_set(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("object_set", runner.run(lambda: _set(ctx, settings)))


@object.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def object_get(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("object_get", runner.run(lambda: _get(ctx, settings)))


@object.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def object_clear(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("object_clear", runner.run(lambda: _clear(ctx, settings)))


## collection: collection_id


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def collection(ctx, is_global):
    """ Get/set settings related to the collection.
    """
    runner = DataMgmtRunner(ctx, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = runner.dm
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.collection


@collection.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def collection_set(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("collection_set", runner.run(lambda: _set(ctx, settings)))


@collection.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def collection_get(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("collection_get", runner.run(lambda: _get(ctx, settings)))


@collection.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def collection_clear(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("collection_clear", runner.run(lambda: _clear(ctx, settings)))


## config: fileservice_url, git_annex_hash_as_checksum, hostname, openbis_url, user, verify_certificates


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def config(ctx, is_global):
    """ Get/set configurations.
    """
    runner = DataMgmtRunner(ctx, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['data_mgmt'] = runner.dm
    ctx.obj['resolver'] = ctx.obj['data_mgmt'].settings_resolver.config


@config.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def config_set(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("config_set", runner.run(lambda: _set(ctx, settings)))


@config.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def config_get(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("config_get", runner.run(lambda: _get(ctx, settings)))


@config.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def config_clear(ctx, settings):
    runner = DataMgmtRunner(ctx)
    return check_result("config_clear", runner.run(lambda: _clear(ctx, settings)))


# repository commands: status, sync, commit, init, addref, removeref, init_analysis

## commit

_commit_params = [
    click.option('-m', '--msg', prompt=True, help='A message explaining what was done.'),
    click.option('-a', '--auto_add', default=True, is_flag=True, help='Automatically add all untracked files.'),
    click.option('-i', '--ignore_missing_parent', default=True, is_flag=True, help='If parent data set is missing, ignore it.'),
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_commit(ctx, msg, auto_add, ignore_missing_parent):
    runner = DataMgmtRunner(ctx)
    return check_result("commit", runner.run(lambda: runner.dm.commit(msg, auto_add, ignore_missing_parent)))

@repository.command("commit", short_help="Commit the repository to git and inform openBIS.")
@click.pass_context
@add_params(_commit_params)
def repository_commit(ctx, msg, auto_add, ignore_missing_parent, repository):
    if repository is None:
        return _repository_commit(ctx, msg, auto_add, ignore_missing_parent)
    with cd(repository):
        return _repository_commit(ctx, msg, auto_add, ignore_missing_parent)

@cli.command(short_help="Commit the repository to git and inform openBIS.")
@click.pass_context
@add_params(_commit_params)
def commit(ctx, msg, auto_add, ignore_missing_parent, repository):
    ctx.invoke(repository_commit, msg=msg, auto_add=auto_add, ignore_missing_parent=ignore_missing_parent, repository=repository)

## init

_init_params = [
    click.option('-oi', '--object_id', help='Set the id of the owning sample.'),
    click.option('-ci', '--collection_id', help='Set the id of the owning experiment.'),
    click.argument('repository', type=click.Path(exists=False, file_okay=False), required=False),
    click.argument('description', default=""),
]

@repository.command("init", short_help="Initialize the folder as a data repository.")
@click.pass_context
@add_params(_init_params)
def repository_init(ctx, object_id, collection_id, repository, description):
    return init_data_impl(ctx, object_id, collection_id, repository, description)

@cli.command(short_help="Initialize the folder as a data repository.")
@click.pass_context
@add_params(_init_params)
def init(ctx, object_id, collection_id, repository, description):
    ctx.invoke(repository_init, object_id=object_id, collection_id=collection_id, repository=repository, description=description)

## init analysis

_init_analysis_params = [
    click.option('-p', '--parent', type=click.Path(exists=False, file_okay=False)),
]
_init_analysis_params += _init_params

@repository.command("init_analysis", short_help="Initialize the folder as an analysis folder.")
@click.pass_context
@add_params(_init_analysis_params)
def repository_init_analysis(ctx, parent, object_id, collection_id, repository, description):
    return init_analysis_impl(ctx, parent, object_id, collection_id, repository, description)

@cli.command(short_help="Initialize the folder as an analysis folder.")
@click.pass_context
@add_params(_init_analysis_params)
def init_analysis(ctx, parent, object_id, collection_id, repository, description):
    ctx.invoke(repository_init_analysis, parent=parent, object_id=object_id, collection_id=collection_id, repository=repository, description=description)

## status

_status_params = [
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_status(ctx):
    runner = DataMgmtRunner(ctx)
    result = runner.run(runner.dm.status)
    click.echo(result.output)

@repository.command("status", short_help="Show the state of the obis repository.")
@click.pass_context
@add_params(_status_params)
def repository_status(ctx, repository):
    if repository is None:
        return _repository_status(ctx)
    with cd(repository):
        return _repository_status(ctx)        

@cli.command(short_help="Show the state of the obis repository.")
@click.pass_context
@add_params(_status_params)
def status(ctx, repository):
    ctx.invoke(repository_status, repository=repository)

## sync

_sync_params = [
    click.option('-i', '--ignore_missing_parent', default=True, is_flag=True, help='If parent data set is missing, ignore it.'),
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_sync(ctx, ignore_missing_parent):
    runner = DataMgmtRunner(ctx)
    return check_result("sync", runner.run(lambda: runner.dm.sync(ignore_missing_parent)))

@repository.command("sync", short_help="Sync the repository with openBIS.")
@click.pass_context
@add_params(_sync_params)
def repository_sync(ctx, ignore_missing_parent, repository):
    if repository is None:
        return _repository_sync(ctx, ignore_missing_parent)
    with cd(repository):
        return _repository_sync(ctx, ignore_missing_parent)

@cli.command(short_help="Sync the repository with openBIS.")
@click.pass_context
@add_params(_sync_params)
def sync(ctx, ignore_missing_parent, repository):
    ctx.invoke(repository_sync, ignore_missing_parent=ignore_missing_parent, repository=repository)

## addref

_addref_params = [
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_addref(ctx):
    runner = DataMgmtRunner(ctx)
    return check_result("addref", runner.run(runner.dm.addref))

@repository.command("addref", short_help="Add the given repository as a reference to openBIS.")
@click.pass_context
@add_params(_addref_params)
def repository_addref(ctx, repository):
    if repository is None:
        return _repository_addref(ctx)
    with cd(repository):
        return _repository_addref(ctx)

@cli.command(short_help="Add the given repository as a reference to openBIS.")
@click.pass_context
@add_params(_addref_params)
def addref(ctx, repository):
    ctx.invoke(repository_addref, repository=repository)

# removeref

_removeref_params = [
    click.option('-d', '--data_set_id', help='Remove ref by data set id, in case the repository is not available anymore.'),
    click.argument('repository', type=click.Path(exists=True, file_okay=False), required=False),
]

def _repository_removeref(ctx, data_set_id=None):
    runner = DataMgmtRunner(ctx)
    return check_result("removeref", runner.run(lambda: runner.dm.removeref(data_set_id=data_set_id)))

@repository.command("removeref", short_help="Remove the reference to the given repository from openBIS.")
@click.pass_context
@add_params(_removeref_params)
def repository_removeref(ctx, data_set_id, repository):
    if data_set_id is None:
        if repository is None:
            return _repository_removeref(ctx)
        with cd(repository):
            return _repository_removeref(ctx)
    else:
        if repository is not None:
            print(repository)
            click_echo("Only provide the data_set id OR the repository.")
            return -1
        return _repository_removeref(ctx, data_set_id=data_set_id)


@cli.command(short_help="Remove the reference to the given repository from openBIS.")
@click.pass_context
@add_params(_removeref_params)
def removeref(ctx, data_set_id, repository):
    ctx.invoke(repository_removeref, data_set_id=data_set_id, repository=repository)


# data set commands: download / clone

## download

_download_params = [
    click.option('-c', '--content_copy_index', type=int, default=None, help='Index of the content copy to download from.'),
    click.option('-f', '--file', help='File in the data set to download - downloading all if not given.'),
    click.option('-s', '--skip_integrity_check', default=False, is_flag=True, help='Skip file integrity check with checksums.'),
    click.argument('data_set_id'),
]

@data_set.command("download", short_help="Download files of a linked data set.")
@add_params(_download_params)
@click.pass_context 
def data_set_download(ctx, content_copy_index, file, data_set_id, skip_integrity_check):
    runner = DataMgmtRunner(ctx)
    return check_result("download", runner.run(lambda: runner.dm.download(data_set_id, content_copy_index, file, skip_integrity_check)))

@cli.command(short_help="Download files of a linked data set.")
@add_params(_download_params)
@click.pass_context
def download(ctx, content_copy_index, file, data_set_id, skip_integrity_check):
    ctx.invoke(data_set_download, content_copy_index=content_copy_index, file=file, data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)

## clone

_clone_move_params = [
    click.option('-u', '--ssh_user', default=None, help='User to connect to remote systems via ssh'),
    click.option('-c', '--content_copy_index', type=int, default=None, help='Index of the content copy to clone from in case there are multiple copies'),
    click.option('-s', '--skip_integrity_check', default=False, is_flag=True, help='Skip file integrity check with checksums.'),
    click.argument('data_set_id'),
]

@data_set.command("clone", short_help="Clone the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def data_set_clone(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    runner = DataMgmtRunner(ctx)
    return check_result("clone", runner.run(lambda: runner.dm.clone(data_set_id, ssh_user, content_copy_index, skip_integrity_check)))

@cli.command(short_help="Clone the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def clone(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    ctx.invoke(data_set_clone, ssh_user=ssh_user, content_copy_index=content_copy_index, data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)


## move

@data_set.command("move", short_help="Move the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def data_set_move(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    runner = DataMgmtRunner(ctx)
    return check_result("move", runner.run(lambda: runner.dm.move(data_set_id, ssh_user, content_copy_index, skip_integrity_check)))

@cli.command(short_help="Move the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def move(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    ctx.invoke(data_set_move, ssh_user=ssh_user, content_copy_index=content_copy_index, data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)


def main():
    cli(obj={})


if __name__ == '__main__':
    main()
