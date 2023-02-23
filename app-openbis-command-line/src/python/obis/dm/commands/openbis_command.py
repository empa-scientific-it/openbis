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
import getpass
import hashlib
import os
import socket

import pybis

from .. import config as dm_config
from ..command_result import CommandException
from ..command_result import CommandResult
from ..utils import complete_openbis_config


class OpenbisCommand(object):
    """ Superclass for commands connecting to openBIS.
    """

    def __init__(self, dm):
        self.data_mgmt = dm
        self.openbis = dm.openbis
        self.git_wrapper = dm.git_wrapper
        self.settings_resolver = dm.settings_resolver
        self.config_dict = dm.settings_resolver.config_dict()

        if self.openbis is None and dm.openbis_config.get('url') is not None:
            self.openbis = pybis.Openbis(url=dm.openbis_config.get('url'),
                                         verify_certificates=dm.openbis_config.get(
                                             'verify_certificates'),
                                         token=dm.openbis_config.get('token'),
                                         use_cache=dm.openbis_config.get('use_cache'),
                                         allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=dm.openbis_config.get(
                                             'allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks'),
                                         )
            if self.user() is not None:
                result = self.login()
                if result.failure():
                    raise CommandException(result)

    def external_dms_id(self):
        return self.config_dict['repository']['external_dms_id']

    def set_external_dms_id(self, value):
        self.config_dict['repository']['external_dms_id'] = value

    def repository_id(self):
        return self.config_dict['repository']['id']

    def set_repository_id(self, value):
        self.config_dict['repository']['id'] = value

    def data_set_id(self):
        return self.config_dict['repository']['data_set_id']

    def set_data_set_id(self, value):
        self.config_dict['repository']['data_set_id'] = value

    def data_set_type(self):
        return self.config_dict['data_set']['type']

    def set_data_set_type(self, value):
        self.config_dict['data_set']['type'] = value

    def data_set_properties(self):
        return self.config_dict['data_set']['properties']

    def set_data_set_properties(self, value):
        self.config_dict['data_set']['properties'] = value

    def object_id(self):
        return self.config_dict['object']['id']

    def object_permId(self):
        return self.config_dict['object']['permId']

    def set_object_id(self, value):
        self.config_dict['object']['id'] = value

    def collection_id(self):
        return self.config_dict['collection']['id']

    def collection_permId(self):
        return self.config_dict['collection']['permId']

    def set_collection_id(self, value):
        self.config_dict['collection']['id'] = value

    def user(self):
        return self.config_dict['config']['user']

    def set_user(self, value):
        self.config_dict['config']['user'] = value

    def hostname(self):
        return self.config_dict['config']['hostname']

    def set_hostname(self, value):
        self.config_dict['config']['hostname'] = value

    def fileservice_url(self):
        return self.config_dict['config']['fileservice_url']

    def set_fileservice_url(self, value):
        self.config_dict['config']['fileservice_url'] = value

    def git_annex_hash_as_checksum(self):
        return self.config_dict['config']['git_annex_hash_as_checksum']

    def set_git_annex_hash_as_checksum(self, value):
        self.config_dict['config']['git_annex_hash_as_checksum'] = value

    def openbis_url(self):
        return self.config_dict['config']['openbis_url']

    def set_openbis_url(self, value):
        self.config_dict['config']['openbis_url'] = value

    def openbis_token(self):
        return self.config_dict['config']['openbis_token']

    def set_openbis_token(self, value):
        self.config_dict['config']['openbis_token'] = value

    def log(self, message):
        command = type(self).__name__
        self.data_mgmt.log.log(command, message)

    def prepare_run(self):
        result = self.check_configuration()
        if result.failure():
            return result
        result = self.login()
        if result.failure():
            return result
        return CommandResult(returncode=0, output="")

    def check_configuration(self):
        """ overwrite in subclass """
        return CommandResult(returncode=0, output="")

    def login(self):
        """ Restore session token if available. """
        if 'config' in self.config_dict.keys():
            if 'openbis_token' in self.config_dict['config'].keys():
                self.openbis.set_token(self.config_dict['config']['openbis_token'], True)
        """ Checks for valid session and asks user for password
        if login is needed. """
        user = self.user()
        if self.openbis.is_session_active():
            if self.openbis.token.startswith(user) or self.openbis.token.startswith('$pat-' + user):
                return CommandResult(returncode=0, output="")
            else:
                self.openbis.logout()
        if self.data_mgmt.login == False:
            return CommandResult(returncode=-1, output="No active session.")
        passwd = getpass.getpass("Password for {}:".format(user))
        try:
            self.openbis.login(user, passwd, save_token=True)
        except ValueError:
            msg = "Could not log into openbis {}".format(self.openbis_url())
            return CommandResult(returncode=-1, output=msg)
        return CommandResult(returncode=0, output='')

    def prepare_external_dms(self):
        # If there is no external data management system, create one.
        result = self.get_or_create_external_data_management_system()
        if result.failure():
            return result
        external_dms = result.output
        self.settings_resolver.repository.set_value_for_parameter(
            'external_dms_id', external_dms.code, 'local')
        self.set_external_dms_id(external_dms.code)
        return result

    def generate_external_data_management_system_code(self, user, hostname, edms_path):
        path_hash = hashlib.sha1(edms_path.encode("utf-8")).hexdigest()[0:8]
        return "{}-{}-{}".format(user, hostname, path_hash).upper()

    def get_or_create_external_data_management_system(self):
        external_dms_id = self.external_dms_id()
        user = self.user()
        hostname = self.determine_hostname()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        edms_path, path_name = os.path.split(result.output)
        if external_dms_id is None:
            external_dms_id = self.generate_external_data_management_system_code(
                user, hostname, edms_path)
        try:
            external_dms = self.openbis.get_external_data_management_system(
                external_dms_id.upper())
        except ValueError:
            # external dms does not exist - create it
            try:
                external_dms = self.openbis.create_external_data_management_system(external_dms_id,
                                                                                   external_dms_id,
                                                                                   "{}:/{}".format(
                                                                                       hostname,
                                                                                       edms_path))
            except Exception as error:
                return CommandResult(returncode=-1, output=str(error))
        return CommandResult(returncode=0, output=external_dms)

    def determine_hostname(self):
        """ Returns globally defined hostname if available.
            Otherwies, lets the user choose one and stores that globally. """
        # from global config
        hostname = self.hostname()
        if hostname is not None:
            return hostname
        # ask user
        hostname = self.ask_for_hostname(socket.gethostname())
        # store
        self.data_mgmt.config('config', True, False,
                              prop='hostname', value=hostname, set=True)
        return hostname

    def ask_for_hostname(self, hostname):
        """ Asks the user to confirm the suggestes hostname or choose a custom one. """
        hostname_input = input(
            'Enter hostname (empty to confirm \'' + str(hostname) + '\'): ')
        if hostname_input:
            return hostname_input
        else:
            return hostname

    def path(self):
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        return result.output

    def load_global_config(self, dm):
        """
        Use global config only.
        """
        resolver = dm_config.SettingsResolver()
        config = {}
        complete_openbis_config(config, resolver, False)
        dm.openbis_config = config


class ContentCopySelector(object):
    """ In case a command needs information from a content copy, this class
    asks the user to pick one if there are multiple. """

    def __init__(self, data_set, content_copy_index=None, get_index=False):
        self.data_set = data_set
        self.content_copy_index = content_copy_index
        self.get_index = get_index

    def select(self):
        content_copy_index = self.select_index()
        if self.get_index == True:
            return content_copy_index
        else:
            return self.data_set.data['linkedData']['contentCopies'][content_copy_index]

    def select_index(self):
        if self.data_set.data['kind'] != 'LINK':
            raise ValueError('Data set is of type ' +
                             self.data_set.data['kind'] + ' but should be LINK.')
        content_copies = self.data_set.data['linkedData']['contentCopies']
        if len(content_copies) == 0:
            raise ValueError("Data set has no content copies.")
        elif len(content_copies) == 1:
            return 0
        else:
            return self.select_content_copy_index(content_copies)

    def select_content_copy_index(self, content_copies):
        if self.content_copy_index is not None:
            # use provided content_copy_index
            if self.content_copy_index >= 0 and self.content_copy_index < len(content_copies):
                return self.content_copy_index
            else:
                raise ValueError("Invalid content copy index.")
        else:
            # ask user
            while True:
                print('From which location should the files be copied?')
                for i, content_copy in enumerate(content_copies):
                    host = content_copy['externalDms']['address'].split(":")[0]
                    path = content_copy['path']
                    print("  {}) {}:{}".format(i, host, path))

                copy_index_string = input('> ')
                if copy_index_string.isdigit():
                    copy_index_int = int(copy_index_string)
                    if copy_index_int >= 0 and copy_index_int < len(content_copies):
                        return copy_index_int
