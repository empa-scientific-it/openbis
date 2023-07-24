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

"""
cli.py

The module that implements the CLI for obis.
"""
import json
import os
from datetime import datetime

import click
from dateutil.relativedelta import relativedelta
from requests import ConnectionError

from pybis import Openbis
from .click_util import click_echo
from .data_mgmt_runner import DataMgmtRunner
from ..dm.command_result import CommandResult
from ..dm.utils import OperationType


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
@click.option('-s', '--skip_verification', default=False, is_flag=True,
              help='Do not verify cerficiates')
@click.option('-d', '--debug', default=False, is_flag=True, help="Show stack trace on error.")
@click.pass_context
def cli(ctx, quiet, skip_verification, debug):
    ctx.obj['quiet'] = quiet
    if skip_verification:
        ctx.obj['verify_certificates'] = False
    ctx.obj['debug'] = debug


def init_data_impl(ctx, repository, desc):
    """Shared implementation for the init_data command."""
    if repository is None:
        repository = "."
    click_echo("init_data {}".format(repository))
    desc = desc if desc != "" else None
    return ctx.obj['runner'].run("init_data", lambda dm: dm.init_data(desc), repository)


def init_analysis_impl(ctx, parent, repository, description):
    click_echo("init_analysis {}".format(repository))
    if parent is not None and os.path.isabs(parent):
        click_echo('Error: The parent must be given as a relative path.')
        return -1
    if repository is not None and os.path.isabs(repository):
        click_echo('Error: The repository must be given as a relative path.')
        return -1
    description = description if description != "" else None
    parent_dir = os.getcwd() if parent is None else os.path.join(os.getcwd(), parent)
    analysis_dir = os.path.join(os.getcwd(), repository)
    parent = os.path.relpath(parent_dir, analysis_dir)
    parent = '..' if parent is None else parent
    return ctx.obj['runner'].run("init_analysis", lambda dm: dm.init_analysis(parent, description),
                                 repository)


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
        self.fail(
            param=param, message='Settings must be in the format: key1, key2, ...')


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
        self.fail(
            param=param, message='Settings must be in the format: key1=value1, key2=value2, ...')


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


def _access_settings(ctx, operation_type, prop=None, value=None):
    is_global = ctx.obj['is_global']
    runner = ctx.obj['runner']
    resolver = ctx.obj['resolver']
    is_data_set_property = False
    if 'is_data_set_property' in ctx.obj:
        is_data_set_property = ctx.obj['is_data_set_property']
    runner.config(resolver, is_global, is_data_set_property, operation_type,
                  prop=prop, value=value)


def _set(ctx, settings):
    settings_dict = _join_settings_set(settings)
    for prop, value in settings_dict.items():
        _access_settings(ctx, OperationType.SET, prop=prop, value=value)
    return CommandResult(returncode=0, output='')


def _get(ctx, settings):
    settings_list = _join_settings_get(settings)
    if len(settings_list) == 0:
        settings_list = [None]
    for prop in settings_list:
        _access_settings(ctx, OperationType.GET, prop=prop)
    return CommandResult(returncode=0, output='')


def _clear(ctx, settings):
    settings_list = _join_settings_get(settings)
    if len(settings_list) == 0:
        settings_list = [None]
    for prop in settings_list:
        _access_settings(ctx, OperationType.CLEAR, prop=prop)
    return CommandResult(returncode=0, output='')


# get all settings

@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Get global or local.')
@click.pass_context
def settings(ctx, is_global):
    """ External Data Store: Get all settings. """
    ctx.obj['is_global'] = is_global


@settings.command('get')
@click.pass_context
def settings_get(ctx):
    """ External Data Store: Get setting. """
    runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    settings = runner.get_settings()
    settings_str = json.dumps(settings, indent=4, sort_keys=True)
    click.echo("{}".format(settings_str))


# repository: repository_id, external_dms_id, data_set_id

@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def repository(ctx, is_global):
    """ External Data Store: Get/set settings related to the repository. """
    runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['runner'] = runner
    ctx.obj['resolver'] = 'repository'


@repository.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def repository_set(ctx, settings):
    """ External Data Store: Set settings related to the repository. """
    return ctx.obj['runner'].run("repository_set", lambda dm: _set(ctx, settings))


@repository.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def repository_get(ctx, settings):
    """ External Data Store: Get settings related to the repository. """
    return ctx.obj['runner'].run("repository_get", lambda dm: _get(ctx, settings))


@repository.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def repository_clear(ctx, settings):
    """ External Data Store: Clear settings related to the repository. """
    return ctx.obj['runner'].run("repository_clear", lambda dm: _clear(ctx, settings))


# data_set: type, properties

_dataset_search_params = [
    click.option('-space', '--space', default=None, help='Space code'),
    click.option('-project', '--project', default=None, help='Project identification code'),
    click.option('-collection', '--collection', default=None, help='Collection code'),
    click.option('-id', '--id', 'dataset_id', default=None,
                 help='Dataset identification information, it can be permId or identifier'),
    click.option('-type', '--type', 'type_code', default=None, help='Dataset type code'),
    click.option('-property', 'property_code', default=None, help='Property code'),
    click.option('-property-value', 'property_value', default=None, help='Property value'),
    click.option('-registration-date', '--registration-date', 'registration_date', default=None,
                 help='Registration date, it can be in the format "oYYYY-MM-DD" (e.g. ">2023-01-01")'),
    click.option('-modification-date', '--modification-date', 'modification_date', default=None,
                 help='Modification date, it can be in the format "oYYYY-MM-DD" (e.g. ">2023-01-01")'),
    click.option('-save', '--save', default=None, help='Filename to save results'),
    click.option('-r', '--recursive', 'recursive', is_flag=True, default=False,
                 help='Search data recursively'),
]

_search_by_sample_params = [
    click.option('-object-type', '--object-type', 'object_type_code', default=None,
                 help='Object type code to filter by'),
    click.option('-object-space', '--object-space', 'object_space', default=None,
                 help='Object space code'),
    click.option('-object-project', '--object-project', 'object_project', default=None,
                 help='Full object project identification code'),
    click.option('-object-collection', '--object-collection', 'object_collection', default=None,
                 help='Full object collection code'),
    click.option('-object-id', '--object-id', 'object_id', default=None,
                 help='Object identification information, it can be permId or identifier'),
    click.option('-object-property', 'object_property_code', default=None,
                 help='Object property code'),
    click.option('-object-property-value', 'object_property_value', default=None,
                 help='Object property value'),
    click.option('-object-registration-date', '--object-registration-date',
                 'object_registration_date', default=None,
                 help='Registration date, it can be in the format "oYYYY-MM-DD" (e.g. ">2023-01-01")'),
    click.option('-object-modification-date', '--object-modification-date',
                 'object_modification_date', default=None,
                 help='Modification date, it can be in the format "oYYYY-MM-DD" (e.g. ">2023-01-01")'),
]


@cli.group('data_set')
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.option('-p', '--is_data_set_property', default=False, is_flag=True,
              help='Configure data set property.')
@click.pass_context
def data_set(ctx, is_global, is_data_set_property):
    """ External Data Store: Get/set settings related to the data set. """
    runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['is_data_set_property'] = is_data_set_property
    ctx.obj['runner'] = runner
    ctx.obj['resolver'] = 'data_set'


@data_set.command('set')
@click.argument('data_set_settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def data_set_set(ctx, data_set_settings):
    """ External Data Store: Set settings related to the data set. """
    return ctx.obj['runner'].run("data_set_set", lambda dm: _set(ctx, data_set_settings))


@data_set.command('get')
@click.argument('data_set_settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def data_set_get(ctx, data_set_settings):
    """ External Data Store: Get settings related to the data set. """
    return ctx.obj['runner'].run("data_set_get", lambda dm: _get(ctx, data_set_settings))


@data_set.command('clear')
@click.argument('data_set_settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def data_set_clear(ctx, data_set_settings):
    """ External Data Store: Clear settings related to the data set. """
    return ctx.obj['runner'].run("data_set_clear", lambda dm: _clear(ctx, data_set_settings))


def _pair_is_not_set(param1, param2):
    return (param1 is None and param2 is not None) or (param1 is not None and param2 is None)


@data_set.command('search', short_help="Search for datasets using a filtering criteria.")
@add_params(_dataset_search_params + _search_by_sample_params)
@click.pass_context
def data_set_search(ctx, type_code, space, project, collection, registration_date,
                    modification_date, dataset_id, property_code, property_value, save, recursive,
                    object_type_code, object_space, object_project, object_collection, object_id,
                    object_property_code, object_property_value, object_registration_date,
                    object_modification_date):
    """Standard Data Store: Search data sets given the filtering criteria or object identifier.
    Results of this command can be used in `obis download`."""
    filtering_arguments = [type_code, space, project, collection, registration_date,
                           modification_date, property_code, property_value,
                           object_type_code, object_space, object_project, object_collection,
                           object_id, object_property_code, object_property_value,
                           object_registration_date, object_modification_date]
    if all(v is None for v in filtering_arguments + [dataset_id]):
        click_echo("You must provide at least one filtering criteria!")
        return -1
    if _pair_is_not_set(property_code, property_value) or _pair_is_not_set(object_property_code,
                                                                           object_property_value):
        click_echo("Property code and property value pair needs to be specified!")
        return -1
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    if dataset_id is not None:
        if any(v is not None for v in filtering_arguments):
            click_echo("Dataset id parameter detected! Other filtering arguments will be omitted!")
        filters = dict(dataset_id=dataset_id)
    else:
        filters = dict(type_code=type_code, space=space,
                       project=project, experiment=collection, property_code=property_code,
                       registration_date=registration_date, modification_date=modification_date,
                       property_value=property_value, object_type_code=object_type_code,
                       object_space=object_space, object_project=object_project,
                       object_collection=object_collection, object_id=object_id,
                       object_property_code=object_property_code,
                       object_property_value=object_property_value,
                       object_registration_date=object_registration_date,
                       object_modification_date=object_modification_date)
    return ctx.obj['runner'].run("data_set_search",
                                 lambda dm: dm.search_data_set(filters, recursive, save)),


# # object: object_id


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def object(ctx, is_global):
    """ External Data Store: Get/set properties related to the object.

    Standard Data Store: Get/set properties of objects connected to downloaded datasets.
    """
    runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['runner'] = runner
    ctx.obj['resolver'] = 'object'


@object.command('set')
@click.argument('object_settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def object_set(ctx, object_settings):
    """ External Data Store: Set properties related to the object.

    Standard Data Store: Set property to all objects connected to downloaded datasets.
    """
    return ctx.obj['runner'].run("object_set", lambda dm: _set(ctx, object_settings))


@object.command('get')
@click.argument('object_settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def object_get(ctx, object_settings):
    """ External Data Store: Set properties related to the object.

    Standard Data Store: Get given properties of all objects connected to downloaded datasets.
    """
    return ctx.obj['runner'].run("object_get", lambda dm: _get(ctx, object_settings))


@object.command('clear')
@click.argument('object_settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def object_clear(ctx, object_settings):
    """ External Data Store: Clear properties related to the object. """
    return ctx.obj['runner'].run("object_clear", lambda dm: _clear(ctx, object_settings))


_object_search_params = [
    click.option('-space', '--space', default=None, help='Space code'),
    click.option('-project', '--project', default=None, help='Full project identification code'),
    click.option('-collection', '--collection', default=None, help='Full collection code'),
    click.option('-object', '--object', 'object_id', default=None,
                 help='Object identification information, it can be permId or identifier'),
    click.option('-type', '--type', 'type_code', default=None, help='Type code'),
    click.option('-property', 'property_code', default=None, help='Property code'),
    click.option('-property-value', 'property_value', default=None,
                 help='Property value'),
    click.option('-registration-date', '--registration-date', 'registration_date', default=None,
                 help='Registration date, it can be in the format "oYYYY-MM-DD" (e.g. ">2023-01-01")'),
    click.option('-modification-date', '--modification-date', 'modification_date', default=None,
                 help='Modification date, it can be in the format "oYYYY-MM-DD" (e.g. ">2023-01-01")'),
    click.option('-save', '--save', default=None, help='Filename to save results'),
    click.option('-r', '--recursive', 'recursive', is_flag=True, default=False,
                 help='Search data recursively'),
]


@object.command('search', short_help="Search for objects using a filtering criteria.")
@add_params(_object_search_params)
@click.pass_context
def object_search(ctx, type_code, space, project, collection, registration_date,
                  modification_date, object_id, property_code, property_value, save, recursive):
    """Standard Data Store: Search for objects using a filtering criteria or object identifier."""
    filtering_arguments = [type_code, space, project, collection, registration_date,
                           modification_date, property_code, property_value]
    if all(v is None for v in filtering_arguments + [object_id]):
        click_echo("You must provide at least one filtering criteria!")
        return -1
    if (property_code is None and property_value is not None) or (
            property_code is not None and property_value is None):
        click_echo("Property code and property value need to be specified!")
        return -1
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    if object_id is not None:
        if any(v is not None for v in filtering_arguments):
            click_echo("Object parameter detected! Other filtering arguments will be omitted!")
        filters = dict(object_code=object_id)
    else:
        filters = dict(type_code=type_code, space=space,
                       project=project, collection=collection, property_code=property_code,
                       registration_date=registration_date, modification_date=modification_date,
                       property_value=property_value)
    return ctx.obj['runner'].run("object_search",
                                 lambda dm: dm.search_object(filters, recursive, save))


# # collection: collection_id


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def collection(ctx, is_global):
    """ External Data Store: Get/set settings related to the collection.

    Standard Data Store: Get/set properties of all collections connected to downloaded datasets.
    """
    runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['runner'] = runner
    ctx.obj['resolver'] = 'collection'


@collection.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def collection_set(ctx, settings):
    """ External Data Store: Set settings related to the collection.

    Standard Data Store: Set given properties of all collections connected to downloaded datasets.
    """
    return ctx.obj['runner'].run("collection_set", lambda dm: _set(ctx, settings))


@collection.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def collection_get(ctx, settings):
    """ External Data Store: Get settings related to the collection.

    Standard Data Store: Get given properties of all collections connected to downloaded datasets.
    """
    return ctx.obj['runner'].run("collection_get", lambda dm: _get(ctx, settings))


@collection.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def collection_clear(ctx, settings):
    """External Data Store: Clear settings related to the collection."""
    return ctx.obj['runner'].run("collection_clear", lambda dm: _clear(ctx, settings))


# config: fileservice_url, git_annex_hash_as_checksum, hostname, openbis_url, user, verify_certificates


@cli.group()
@click.option('-g', '--is_global', default=False, is_flag=True, help='Set/get global or local.')
@click.pass_context
def config(ctx, is_global):
    """External Data Store: Get/set configurations.

    Standard Data Store: Get/set configurations.
    """
    if is_global is True:
        runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False, is_physical=True)
    else:
        runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.obj['is_global'] = is_global
    ctx.obj['runner'] = runner
    ctx.obj['resolver'] = 'config'


@config.command('set')
@click.argument('settings', type=SettingsSet(), nargs=-1)
@click.pass_context
def config_set(ctx, settings):
    """External Data Store: Set configurations.

    Standard Data Store: Set configurations.
    """
    return ctx.obj['runner'].run("config_set", lambda dm: _set(ctx, settings))


@config.command('get')
@click.argument('settings', type=SettingsGet(), nargs=-1)
@click.pass_context
def config_get(ctx, settings):
    """External Data Store: Get configurations.

    Standard Data Store: Get configurations.
    """
    return ctx.obj['runner'].run("config_get", lambda dm: _get(ctx, settings))


@config.command('clear')
@click.argument('settings', type=SettingsClear(), nargs=-1)
@click.pass_context
def config_clear(ctx, settings):
    """External Data Store: Clear configurations.
    """
    return ctx.obj['runner'].run("config_clear", lambda dm: _clear(ctx, settings))


# repository commands: status, sync, commit, init, addref, removeref, init_analysis

# commit

_commit_params = [
    click.option('-m', '--msg', default="obis commit",
                 help='A message explaining what was done.'),
    click.option('-a', '--auto_add', default=True, is_flag=True,
                 help='Automatically add all untracked files.'),
    click.option('-i', '--ignore_missing_parent', default=True,
                 is_flag=True, help='If parent data set is missing, ignore it.'),
    click.argument('repository', type=click.Path(
        exists=True, file_okay=False), required=False),
]


@repository.command("commit", short_help="Commit the repository to git and inform openBIS.")
@click.pass_context
@add_params(_commit_params)
def repository_commit(ctx, msg, auto_add, ignore_missing_parent, repository):
    """External Data Store: Commit the repository to git and inform openBIS.
    """
    return ctx.obj['runner'].run("commit",
                                 lambda dm: dm.commit(msg, auto_add, ignore_missing_parent),
                                 repository)


@cli.command(short_help="Commit the repository to git and inform openBIS.")
@click.pass_context
@add_params(_commit_params)
def commit(ctx, msg, auto_add, ignore_missing_parent, repository):
    """External Data Store: Commit the repository to git and inform openBIS.
    """
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(repository_commit, msg=msg, auto_add=auto_add,
               ignore_missing_parent=ignore_missing_parent, repository=repository)


# init


_init_params = [
    click.argument('repository_path', type=click.Path(
        exists=False, file_okay=False), required=False),
    click.argument('description', default=""),

]


@repository.command("init", short_help="Initialize the folder as a data repository.")
@click.pass_context
@add_params(_init_params)
def repository_init(ctx, repository_path, description):
    """External Data Store: Initialize the folder as a data repository.
    """
    return init_data_impl(ctx, repository_path, description)


_init_params_physical = \
    _init_params + \
    [click.option('-p', '--physical', 'is_physical', default=False, is_flag=True,
                  help='Initialize folder for Standard Data Store data handling.')]


@cli.command(short_help="Initialize the folder as a data repository.")
@click.pass_context
@add_params(_init_params_physical)
def init(ctx, repository_path, description, is_physical):
    """External Data Store: Initialize the folder as a data repository for External Data Store
    data handling.

    Standard Data Store: Initialize the folder as a data repository for Standard Data Store
    data handling.
    """
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False, is_physical=is_physical)
    ctx.invoke(repository_init, repository_path=repository_path, description=description)


# init analysis


_init_analysis_params = [
    click.option('-p', '--parent',
                 type=click.Path(exists=False, file_okay=False)),
]
_init_analysis_params += _init_params


@repository.command("init_analysis", short_help="Initialize the folder as an analysis folder.")
@click.pass_context
@add_params(_init_analysis_params)
def repository_init_analysis(ctx, parent, repository_path, description):
    """External Data Store: Initialize the folder as an analysis folder."""
    return init_analysis_impl(ctx, parent, repository_path, description)


@cli.command(name='init_analysis', short_help="Initialize the folder as an analysis folder.")
@click.pass_context
@add_params(_init_analysis_params)
def init_analysis(ctx, parent, repository_path, description):
    """External Data Store: Initialize the folder as an analysis folder."""
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(repository_init_analysis, parent=parent,
               repository_path=repository_path, description=description)


# status


_status_params = [
    click.argument('repository', type=click.Path(
        exists=True, file_okay=False), required=False),
]


@repository.command("status", short_help="Show the state of the obis repository.")
@click.pass_context
@add_params(_status_params)
def repository_status(ctx, repository):
    """External Data Store: Show the state of the obis repository."""
    return ctx.obj['runner'].run("repository_status", lambda dm: dm.status(), repository)


@cli.command(short_help="Show the state of the obis repository.")
@click.pass_context
@add_params(_status_params)
def status(ctx, repository):
    """External Data Store: Show the state of the obis repository."""
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(repository_status, repository=repository)


# sync


_sync_params = [
    click.option('-i', '--ignore_missing_parent', default=True,
                 is_flag=True, help='If parent data set is missing, ignore it.'),
    click.argument('repository', type=click.Path(
        exists=True, file_okay=False), required=False),
]


def _repository_sync(dm, ignore_missing_parent):
    dm.ignore_missing_parent = ignore_missing_parent
    return dm.sync()


@repository.command("sync", short_help="Sync the repository with openBIS.")
@click.pass_context
@add_params(_sync_params)
def repository_sync(ctx, ignore_missing_parent, repository):
    """External Data Store: Sync the repository with openBIS."""
    return ctx.obj['runner'].run("sync", lambda dm: _repository_sync(dm, ignore_missing_parent),
                                 repository)


@cli.command(short_help="Sync the repository with openBIS.")
@click.pass_context
@add_params(_sync_params)
def sync(ctx, ignore_missing_parent, repository):
    """External Data Store: Sync the repository with openBIS."""
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(repository_sync,
               ignore_missing_parent=ignore_missing_parent, repository=repository)


@cli.group(short_help="create/show a openBIS token")
@click.pass_context
def token(ctx):
    pass


@token.command("get", short_help="Get existing personal access token or create a new one")
@click.argument("session-name", required=False)
@click.option("--validity-days", help="Number of days the token is valid")
@click.option("--validity-weeks", help="Number of weeks the token is valid")
@click.option("--validity-months", help="Number of months the token is valid")
@click.pass_context
def new_token(ctx, session_name=None, **kwargs):
    runner = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    settings = runner.get_settings()

    if not session_name:
        session_name = settings['config']['session_name']
    if session_name:
        click.echo(f"Get personal access token for session «{session_name}»")
    else:
        session_name = click.prompt("Please enter a session name")

    url = settings['config']['openbis_url']
    if not url:
        url = click.prompt("Please enter the openBIS URL")

    username = settings['config']['user']
    if not username:
        username = click.prompt(f"Please enter username for {url}")
    password = click.prompt(f"Password for {username}@{url}", hide_input=True)
    o = Openbis(url, verify_certificates=settings['config'].get(
        "verify_certificates", True))
    try:
        o.login(username, password)
    except (ConnectionError, ValueError) as exc:
        raise click.ClickException(f"Cannot connect to openBIS: {exc}")

    validFrom = datetime.now()
    if kwargs.get("validity_months"):
        validTo = validFrom + \
                  relativedelta(months=int(kwargs.get("validity_months")))
    elif kwargs.get("validity_weeks"):
        validTo = validFrom + \
                  relativedelta(weeks=int(kwargs.get("validity_weeks")))
    elif kwargs.get("validity_days"):
        validTo = validFrom + \
                  relativedelta(days=int(kwargs.get("validity_days")))
    else:
        serverinfo = o.get_server_information()
        seconds = serverinfo.personal_access_tokens_max_validity_period
        validTo = validFrom + relativedelta(seconds=seconds)
    token_obj = o.get_or_create_personal_access_token(
        sessionName=session_name, validFrom=validFrom, validTo=validTo)
    settings = (
        {"user": username},
        {"openbis_url": url},
        {"openbis_token": token_obj.permId},
        {"session_name": session_name},
    )

    ctx.obj['is_global'] = False
    ctx.obj['runner'] = runner
    ctx.obj['resolver'] = 'config'
    runner.run("config_set", lambda dm: _set(ctx, settings))


_addref_params = [
    click.argument('repository', type=click.Path(
        exists=True, file_okay=False), required=False),
]


@repository.command("addref", short_help="Add the given repository as a reference to openBIS.")
@click.pass_context
@add_params(_addref_params)
def repository_addref(ctx, repository):
    """Used for External Data Store only."""
    return ctx.obj['runner'].run("addref", lambda dm: dm.addref(), repository)


@cli.command(short_help="Add the given repository as a reference to openBIS.")
@click.pass_context
@add_params(_addref_params)
def addref(ctx, repository):
    """Used for External Data Store only."""
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(repository_addref, repository=repository)


# removeref


_removeref_params = [
    click.option('-d', '--data_set_id',
                 help='Remove ref by data set id, in case the repository is not available anymore.'),
    click.argument('repository', type=click.Path(
        exists=True, file_okay=False), required=False),
]


@repository.command("removeref",
                    short_help="Remove the reference to the given repository from openBIS.")
@click.pass_context
@add_params(_removeref_params)
def repository_removeref(ctx, data_set_id, repository):
    """Used for External Data Store only."""
    if data_set_id is not None and repository is not None:
        click_echo("Only provide the data_set id OR the repository.")
        return -1
    return ctx.obj['runner'].run("removeref", lambda dm: dm.removeref(data_set_id=data_set_id),
                                 repository)


@cli.command(short_help="Remove the reference to the given repository from openBIS.")
@click.pass_context
@add_params(_removeref_params)
def removeref(ctx, data_set_id, repository):
    """Used for External Data Store only."""
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(repository_removeref, data_set_id=data_set_id,
               repository=repository)


# data set commands: download, upload, clone

# download

_download_params = [
    click.argument('data_set_id', required=False),
    click.option('-from-file', '--from-file', 'from_file',
                 help='An output .CSV file from `obis data_set search` command with the list of' +
                      ' objects to download datasets from'),
    click.option(
        '-f', '--file', 'file',
        help='File in the data set to download - downloading all if not given.'),
    click.option('-s', '--skip_integrity_check', default=False, is_flag=True,
                 help='Flag to skip file integrity check with checksums'),
]


@cli.command("download", short_help="Download files of a data set.")
@add_params(_download_params)
@click.pass_context
def download(ctx, data_set_id, from_file, file, skip_integrity_check):
    """ Downloads dataset files from OpenBIS instance.\n
    DATA_SET    Unique identifier of dataset within OpenBIS instance."""
    if (data_set_id is None and from_file is None) or (
            data_set_id is not None and from_file is not None):
        click_echo("'data_set_id' or 'from_file' must be provided!")
        return -1
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    return ctx.obj['runner'].run("download",
                                 lambda dm: dm.download(data_set_id=data_set_id,
                                                        from_file=from_file, file=file,
                                                        skip_integrity_check=skip_integrity_check))


# upload


_upload_params = [
    click.option(
        '-f', '--file', "files", help='file or directory to upload.', required=True, multiple=True),
    click.argument('sample_id'),
    click.argument('data_set_type'),
]


@cli.command("upload", short_help="Upload files to form a data set.")
@add_params(_upload_params)
@click.pass_context
def upload(ctx, sample_id, data_set_type, files):
    """ Creates data set under object and upload files to it.\n
    SAMPLE_ID       Unique identifier an object in OpenBIS.\n
    DATA_SET_TYPE   Newly created data set type.
    """
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.obj['runner'].run("upload",
                          lambda dm: dm.upload(sample_id, data_set_type, files))


# clone


_clone_move_params = [
    click.option('-u', '--ssh_user', default=None,
                 help='User to connect to remote systems via ssh'),
    click.option('-c', '--content_copy_index', type=int, default=None,
                 help='Index of the content copy to clone from in case there are multiple copies'),
    click.option('-s', '--skip_integrity_check', default=False,
                 is_flag=True, help='Skip file integrity check with checksums.'),
    click.argument('data_set_id'),
]


@data_set.command("clone", short_help="Clone the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def data_set_clone(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    return ctx.obj['runner'].run("clone",
                                 lambda dm: dm.clone(data_set_id, ssh_user, content_copy_index,
                                                     skip_integrity_check))


@cli.command(short_help="Clone the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def clone(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(data_set_clone, ssh_user=ssh_user, content_copy_index=content_copy_index,
               data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)


# move

@data_set.command("move", short_help="Move the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def data_set_move(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    return ctx.obj['runner'].run("move",
                                 lambda dm: dm.move(data_set_id, ssh_user, content_copy_index,
                                                    skip_integrity_check))


@cli.command(short_help="Move the repository found in the given data set id.")
@click.pass_context
@add_params(_clone_move_params)
def move(ctx, ssh_user, content_copy_index, data_set_id, skip_integrity_check):
    ctx.obj['runner'] = DataMgmtRunner(ctx.obj, halt_on_error_log=False)
    ctx.invoke(data_set_move, ssh_user=ssh_user, content_copy_index=content_copy_index,
               data_set_id=data_set_id, skip_integrity_check=skip_integrity_check)


def main():
    cli(obj={})


if __name__ == '__main__':
    main()
