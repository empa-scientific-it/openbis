#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
pybis.py

Work with openBIS from Python.

"""

import os
import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

import time
from datetime import datetime
import json
import re
from urllib.parse import urlparse
import zlib

import pandas as pd
from pandas import DataFrame, Series

import threading
from threading import Thread
from queue import Queue
DROPBOX_PLUGIN = "jupyter-uploader-api"

search_for_type = {
    "space":      "as.dto.space.search.SpaceSearchCriteria",
    "project":    "as.dto.project.search.ProjectSearchCriteria",
    "experiment": "as.dto.experiment.search.ExperimentSearchCriteria",
    "code":       "as.dto.common.search.CodeSearchCriteria",
    "sample_type":"as.dto.sample.search.SampleTypeSearchCriteria",
}

fetch_option = {
    "space":        { "@type": "as.dto.space.fetchoptions.SpaceFetchOptions" },
    "project":      { "@type": "as.dto.project.fetchoptions.ProjectFetchOptions" },
    "experiment":   { "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions" },
    "sample":       { "@type": "as.dto.sample.fetchoptions.SampleFetchOptions" },
    "dataset":      { "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions" },

    "physicalData": { "@type": "as.dto.dataset.fetchoptions.PhysicalDataFetchOptions" },
    "linkedData":   { "@type": "as.dto.dataset.fetchoptions.LinkedDataFetchOptions" },

    "type":         { "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions" },

    "properties":   { "@type": "as.dto.property.fetchoptions.PropertyFetchOptions" },
    "tags":         { "@type": "as.dto.tag.fetchoptions.TagFetchOptions" },

    "registrator":  { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "modifier":     { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
    "leader":       { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },

    "attachments":  { "@type": "as.dto.attachment.fetchoptions.AttachmentFetchOptions" },
    "history":      { "@type": "as.dto.history.fetchoptions.HistoryEntryFetchOptions" },
    "dataStore":    { "@type": "as.dto.datastore.fetchoptions.DataStoreFetchOptions" },
}


def format_timestamp(ts):
    return datetime.fromtimestamp(round(ts/1000)).strftime('%Y-%m-%d %H:%M:%S')

def extract_code(obj):
    return obj['code']

def extract_identifier(ident):
    if not isinstance(ident, dict): 
        return str(ident)
    return ident['identifier']

def extract_permid(permid):
    if not isinstance(permid, dict):
        return str(permid)
    return permid['permId']

def extract_nested_permid(permid):
    if not isinstance(permid, dict):
        return str(permid)
    return permid['permId']['permId']

def extract_nested_identifier(ident):
    if not isinstance(ident, dict): 
        return str(ident)
    return ident['identifier']['identifier']

def extract_person(person):
    if not isinstance(person, dict):
        return str(person)
    if 'email' in person and person['email'] is not '':
        return "%s %s <%s>" % (person['firstName'], person['lastName'], person['email'])
    else:
        return "%s %s" % (person['firstName'], person['lastName'])

def extract_properties(prop):
    if isinstance(prop, dict):
        newline = "; "
        props = []
        for key in prop:
            props.append("%s: %s" % (key, prop[key]))
        return newline.join(props)

def crc32(fileName):
    prev = 0
    for eachLine in open(fileName,"rb"):
        prev = zlib.crc32(eachLine, prev)
    return prev
    # return as hex
    #return "%X"%(prev & 0xFFFFFFFF)

def _create_tagIds(tags=None):

    if tags is None:
        return None

    tagIds = []
    for tag in tags:
        tagIds.append({ "code": tag, "@type": "as.dto.tag.id.TagCode" })

    return tagIds


def _create_typeId(type):
    return {
        "permId": type.upper(),
        "@type": "as.dto.entitytype.id.EntityTypePermId"
    }


def _create_projectId(ident):
    match = re.match('/', ident)
    if match:
        return {
            "identifier": ident,
            "@type": "as.dto.project.id.ProjectIdentifier"
        }
    else:
        return { 
            "permId": ident,
            "@type": "as.dto.project.id.ProjectPermId"
        }


def _criteria_for_code(code):
    return {
        "fieldName": "code",
        "fieldType": "ATTRIBUTE",
        "fieldValue": {
            "value": code,
            "@type": "as.dto.common.search.StringEqualToValue"
        },
        "@type": "as.dto.common.search.CodeSearchCriteria"
    }
    


def _subcriteria_for_code(code, object_type):
    criteria = {
        "criteria": [
            {
                "fieldName": "code",
                "fieldType": "ATTRIBUTE",
                "fieldValue": {
                    "value": code,
                    "@type": "as.dto.common.search.StringEqualToValue"
                },
                "@type": "as.dto.common.search.CodeSearchCriteria"
            }
        ],
        "@type": search_for_type[object_type],
        "operator": "AND"
    }
    return criteria

class Openbis:
    """Interface for communicating with openBIS. A current version of openBIS is needed (at least version 16.05).

    """

    def __init__(self, url='https://localhost:8443', verify_certificates=True, token=None):
        """Initialize a new connection to an openBIS server.

        :param host:
        """

        url_obj = urlparse(url)
        if  url_obj.netloc is None:
            raise ValueError("please provide the url in this format: https://openbis.host.ch:8443")

        self.url_obj = url_obj
        self.url     = url_obj.geturl()
        self.port    = url_obj.port
        self.hostname = url_obj.hostname
        self.as_v3 = '/openbis/openbis/rmi-application-server-v3.json'
        self.as_v1 = '/openbis/openbis/rmi-general-information-v1.json'
        self.reg_v1 = '/openbis/openbis/rmi-query-v1.json'
        self.verify_certificates = verify_certificates
        self.token = token
        self.datastores = []

        self.dataset_types = None
        self.sample_types = None
        self.files_in_wsp = []
        self.token_path = None

        # some default settings for working with samples etc.
        self.default_space = None
        self.default_project = None
        self.default_experiment = None
        self.default_sample_type = None

        # use an existing token, if available
        if self.token is None:
            self.token = self._get_cached_token()

    @property
    def spaces(self):
        return self.get_spaces()


    @property
    def projects(self):
        return self.get_projects()


    def _get_cached_token(self):
        """Read the token from the cache, and set the token ivar to it, if there, otherwise None.
        If the token is not valid anymore, delete it. 
        """
        token_path = self.gen_token_path()
        if not os.path.exists(token_path):
            return None
        try:
            with open(token_path) as f:
                token = f.read()
                if not self.is_token_valid(token):
                    os.remove(token_path)
                    return None
                else:
                    return token
        except FileNotFoundError:
            return None


    def gen_token_path(self, parent_folder=None):
        """generates a path to the token file.
        The token is usually saved in a file called
        ~/.pybis/hostname.token
        """
        if parent_folder is None:
            # save token under ~/.pybis folder
            parent_folder = os.path.join(
                os.path.expanduser("~"),
                '.pybis'
            )
        path = os.path.join(parent_folder, self.hostname + '.token')
        return path


    def save_token(self, token=None, parent_folder=None):
        """ saves the session token to the disk, usually here: ~/.pybis/hostname.token. When a new Openbis instance is created, it tries to read this saved token by default.
        """
        if token is None:
            token = self.token

        token_path = None;
        if parent_folder is None:
            token_path = self.gen_token_path()
        else:
            token_path = self.gen_token_path(parent_folder)

        # create the necessary directories, if they don't exist yet
        os.makedirs(os.path.dirname(token_path), exist_ok=True)
        with open(token_path, 'w') as f:
            f.write(token)
            self.token_path = token_path


    def delete_token(self, token_path=None):
        """ deletes a stored session token.
        """
        if token_path is None:
            token_path = self.token_path
        os.remove(token_path)


    def _post_request(self, resource, data):
        """ internal method, used to handle all post requests and serializing / deserializing
        data
        """
        if "id" not in data:
            data["id"] = "1"
        if "jsonrpc" not in data:
            data["jsonrpc"] = "2.0"

        resp = requests.post(
            self.url + resource, 
            json.dumps(data), 
            verify=self.verify_certificates
        )

        if resp.ok:
            data = resp.json()
            if 'error' in data:
                raise ValueError('an error has occured: ' + data['error']['message'] )
            elif 'result' in data:
                return data['result']
            else:
                raise ValueError('request did not return either result nor error')
        else:
            raise ValueError('general error while performing post request')


    def logout(self):
        """ Log out of openBIS. After logout, the session token is no longer valid.
        """
        if self.token is None:
            return

        logout_request = {
            "method":"logout",
            "params":[self.token],
        }
        resp = self._post_request(self.as_v3, logout_request)
        self.token = None
        self.token_path = None
        return resp


    def login(self, username=None, password=None, save_token=False):
        """Log into openBIS.
        Expects a username and a password and updates the token (session-ID).
        The token is then used for every request.
        Clients may want to store the credentials object in a credentials store after successful login.
        Throw a ValueError with the error message if login failed.
        """

        login_request = {
            "method":"login",
            "params":[username, password],
        }
        result = self._post_request(self.as_v3, login_request)
        if result is None:
            raise ValueError("login to openBIS failed")
        else:
            self.token = result
            if save_token:
                self.save_token()
            return self.token


    def get_datastores(self):
        """ Get a list of all available datastores. Usually there is only one, but in some cases
        there might be more. If you upload a file, you need to specifiy the datastore you want
        the file uploaded to.
        """
        if len(self.datastores) == 0: 
            request = {
                "method": "listDataStores",
                "params": [ self.token ],
            }
            resp = self._post_request(self.as_v1, request)
            if resp is not None:
                self.datastores = DataFrame(resp)[['code','downloadUrl', 'hostUrl']]
                return self.datastores
            else:
                raise ValueError("No datastore found!")
        else:
            return self.datastores


    def get_spaces(self, code=None):
        """ Get a list of all available spaces (DataFrame object). To create a sample or a
        dataset, you need to specify in which space it should live.
        """
     
        criteria = {}
        options = {}
        request = {
            "method": "searchSpaces",
            "params": [ self.token, 
                criteria,
                options,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            spaces = DataFrame(resp['objects'])
            spaces['registrationDate']= spaces['registrationDate'].map(format_timestamp)
            spaces['modificationDate']= spaces['modificationDate'].map(format_timestamp)
            sp = Things(
                self,
                'space',
                spaces[['code', 'description', 'registrationDate', 'modificationDate']]
            )
            return sp
        else:
            raise ValueError("No spaces found!")


    def get_space(self, spaceId):
        """ Returns a Space object for a given identifier (spaceId).
        """

        spaceId = str(spaceId).upper()
        request = {
        "method": "getSpaces",
            "params": [ 
            self.token,
            [{ 
                "@id": 0,
                "permId": spaceId,
                "@type": "as.dto.space.id.SpacePermId"
            }],
            {
                "@id": 0,
                "@type": "as.dto.space.fetchoptions.SpaceFetchOptions",
                "registrator": None,
                "samples": None,
                "projects": None,
                "sort": None
            } 
            ],
        } 
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No such space: %s" % spaceId)
        return Space(self, resp[spaceId])


    def get_samples(self, space=None, project=None, experiment=None, sample_type=None):
        """ Get a list of all samples for a given space/project/experiment (or any combination)
        """

        if space is None:
            space = self.default_space
        if project is None:
            project = self.default_project
        if experiment is None:
            experiment = self.default_experiment
        if sample_type is None:
            sample_type = self.default_sample_type

        sub_criteria = []
        if space:
            sub_criteria.append(_subcriteria_for_code(space, 'space'))
        if project:
            exp_crit = _subcriteria_for_code(experiment, 'experiment')
            proj_crit = _subcriteria_for_code(project, 'project')
            exp_crit['criteria'] = []
            exp_crit['criteria'].append(proj_crit)
            sub_criteria.append(exp_crit)
        if experiment:
            sub_criteria.append(_subcriteria_for_code(experiment, 'experiment'))
        if sample_type:
            sub_criteria.append(_subcriteria_for_code(sample_type, 'sample_type'))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.sample.search.SampleSearchCriteria",
            "operator": "AND"
        }

        options = {
            "properties": {
                "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
            },
            "tags": {
                "@type": "as.dto.tag.fetchoptions.TagFetchOptions"
            },
            "registrator": {
                "@type": "as.dto.person.fetchoptions.PersonFetchOptions"
            },
            "modifier": {
                "@type": "as.dto.person.fetchoptions.PersonFetchOptions"
            },
            "experiment": {
                "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions"
            },
            "type": {
                "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
            },
            "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        }

        request = {
            "method": "searchSamples",
            "params": [ self.token, 
                criteria,
                options,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            objects = resp['objects']
            cache = {}
            for obj in objects:
                for key in obj.keys():
                    if key in ('modifier','registrator','project','experiment','space','type'):
                        if isinstance(obj[key], dict):
                            cache[ obj[key]['@id'] ] = obj[key]
                        else:
                            if obj[key] in cache:
                                obj[key] = cache[ obj[key] ]

            samples = DataFrame(objects)
            if len(samples) is 0:
                raise ValueError("No samples found!")

            samples['registrationDate']= samples['registrationDate'].map(format_timestamp)
            samples['modificationDate']= samples['modificationDate'].map(format_timestamp)
            samples['registrator'] = samples['registrator'].map(extract_person)
            samples['modifier'] = samples['modifier'].map(extract_person)
            samples['identifier'] = samples['identifier'].map(extract_identifier)
            samples['experiment'] = samples['experiment'].map(extract_nested_identifier)
            samples['sample_type'] = samples['type'].map(extract_nested_permid)

            return samples[['code', 'identifier', 'experiment', 'sample_type', 'registrator', 'registrationDate', 'modifier', 'modificationDate']]
        else:
            raise ValueError("No samples found!")

    def get_experiments(self, code=None, space=None, project=None):
        """ Get a list of all experiment for a given space or project (or any combination)
        """

        if space is None:
            space = self.default_space
        if project is None:
            project = self.default_project

        sub_criteria = []
        if space:
            sub_criteria.append(_subcriteria_for_code(space, 'space'))
        if project:
            sub_criteria.append(_subcriteria_for_code(project, 'project'))
        if code:
            sub_criteria.append(_criteria_for_code(code))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.experiment.search.ExperimentSearchCriteria",
            "operator": "AND"
        }
        options = {
            "properties": { "@type": "as.dto.property.fetchoptions.PropertyFetchOptions" },
            "tags": { "@type": "as.dto.tag.fetchoptions.TagFetchOptions" },
            "registrator": { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
            "modifier": { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
            "project": { "@type": "as.dto.project.fetchoptions.ProjectFetchOptions" },
            "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions" 
        }

        request = {
            "method": "searchExperiments",
            "params": [ self.token, 
                criteria,
                options,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            objects = resp['objects']
            cache = {}
            for obj in objects:
                for key in obj.keys():
                    if key in ('modifier','registrator','project','experiement','space'):
                        if isinstance(obj[key], dict):
                            cache[ obj[key]['@id'] ] = obj[key]
                        else:
                            if obj[key] in cache:
                                obj[key] = cache[ obj[key] ]

            experiments = DataFrame(objects)
            experiments['registrationDate']= experiments['registrationDate'].map(format_timestamp)
            experiments['modificationDate']= experiments['modificationDate'].map(format_timestamp)
            experiments['project']= experiments['project'].map(extract_code)
            experiments['registrator'] = experiments['registrator'].map(extract_person)
            experiments['modifier'] = experiments['modifier'].map(extract_person)
            experiments['identifier'] = experiments['identifier'].map(extract_identifier)

            exps = experiments[['code', 'identifier', 'project', 'registrator', 'registrationDate', 'modifier', 'modificationDate']]
            return Things(self, 'experiment', exps, 'identifier')
        else:
            raise ValueError("No experiments found!")


    def get_experiment(self, expId):
        """ Returns an experiment object for a given identifier (expId).
        """

        fo = {
            "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions"
        }
        for option in ['tags', 'properties']:
            fo[option] = fetch_option[option]


        expId = str(expId).upper()
        request = {
        "method": "getExperiments",
            "params": [ 
            self.token,
            [{ 
                "identifier": expId,
                "@type": "as.dto.experiment.id.ExperimentIdentifier"
            }],
            fo
            ],
        } 
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No such experiment: %s" % expId)
        return Experiment(self, resp[expId])


    def new_experiment(self, project_ident, code, type, properties=None, attachments=None, tags=None):

        tagIds = _create_tagIds(tags)
        typeId = _create_typeId(type)
        projectId = _create_projectId(project_ident)
        if properties is None:
            properties = {}
        
        request = {
            "method": "createExperiments",
            "params": [
                self.token,
                [
                    {
                        "properties": properties,
                        "code": code,
                        "typeId" : typeId,
                        "projectId": projectId,
                        "tagIds": tagIds,
                        "attachments": attachments,
                        "@type": "as.dto.experiment.create.ExperimentCreation",
                    }
                ]
            ],
        }
        resp = self._post_request(self.as_v3, request)
        return resp[0]['permId']


    def get_projects(self, space=None):
        """ Get a list of all available projects (DataFrame object).
        """

        if space is None:
            space = self.default_space

        sub_criteria = []
        if space:
            sub_criteria.append(_subcriteria_for_code(space, 'space'))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.project.search.ProjectSearchCriteria",
            "operator": "AND"
        }

        options = {
            "registrator": { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
            "modifier": { "@type": "as.dto.person.fetchoptions.PersonFetchOptions" },
            "experiments": { "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions", },
            "space": { "@type": "as.dto.space.fetchoptions.SpaceFetchOptions" },
            "@type": "as.dto.project.fetchoptions.ProjectFetchOptions"
        }

        request = {
            "method": "searchProjects",
            "params": [ self.token, 
                criteria,
                options,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            objects = resp['objects']
            cache = {}
            for obj in objects:
                for key in obj.keys():
                    if key in ('registrator','modifier', 'experiment','space'):
                        if isinstance(obj[key], dict):
                            cache[ obj[key]['@id'] ] = obj[key]
                        else:
                            if obj[key] in cache:
                                obj[key] = cache[ obj[key] ]

            projects = DataFrame(objects)
            if len(projects) is 0:
                raise ValueError("No projects found!")

            projects['registrationDate']= projects['registrationDate'].map(format_timestamp)
            projects['modificationDate']= projects['modificationDate'].map(format_timestamp)
            projects['registrator'] = projects['registrator'].map(extract_person)
            projects['modifier'] = projects['modifier'].map(extract_person)
            projects['permid'] = projects['permId'].map(extract_permid)
            projects['identifier'] = projects['identifier'].map(extract_identifier)
            projects['space'] = projects['space'].map(extract_code)

            pros=projects[['code', 'space', 'registrator', 'registrationDate', 
                            'modifier', 'modificationDate', 'permid', 'identifier']]
            return Things(self, 'project', pros, 'identifier')
        else:
            raise ValueError("No projects found!")


    def _create_get_request(self, method_name, entity_type, permids, options):

        if not isinstance(permids, list):
            permids = [permids]

        type = "as.dto.{}.id.{}".format(entity_type.lower(), entity_type.capitalize())
        search_params = []
        for permid in permids:
            # decide if we got a permId or an identifier
            match = re.match('/', permid)
            if match:
                search_params.append(
                    { "identifier" : permid, "@type" : type + 'Identifier' }
                )
            else: 
                search_params.append(
                    { "permId" : permid, "@type": type + 'PermId' }
                )

        fo = {}
        for option in options:
            fo[option] = fetch_option[option]

        request = {
            "method": method_name,
            "params": [
                self.token,
                search_params,
                fo
            ],
        }
        return request


    def get_project(self, projectId):
        request = self._create_get_request('getProjects', 'project', projectId, ['attachments'])
        resp = self._post_request(self.as_v3, request)
        return resp


    def new_project(self, space_code, code, description, leaderId):
        request = {
            "method": "createProjects",
            "params": [
                self.token,
                [
                    {
                        "code": code,
                        "spaceId": {
                            "permId": space_code,
                            "@type": "as.dto.space.id.SpacePermId"
                        },
                        "@type": "as.dto.project.create.ProjectCreation",
                        "description": description,
                        "leaderId": leaderId,
                        "attachments": None
                    }
                ]
            ],
        }
        resp = self._post_request(self.as_v3, request)
        return resp


    def get_sample_types(self):
        """ Returns a list of all available sample types
        """
        return self.get_types_of("searchSampleTypes", ["generatedCodePrefix"])


    def get_experiment_types(self):
        """ Returns a list of all available experiment types
        """
        return self.get_types_of("searchExperimentTypes")


    def get_material_types(self):
        """ Returns a list of all available material types
        """
        return self.get_types_of("searchMaterialTypes")


    def get_dataset_types(self):
        """ Returns a list (DataFrame object) of all currently available dataset types
        """
        return self.get_types_of("searchDataSetTypes")


    def get_file_types(self):
        """ Returns a list (DataFrame object) of all currently available file types
        """
        pass
        #return self.get_types_of("searchFileTypes")


    def get_types_of(self, method_name, additional_attributes=[]):
        """ Returns a list of all available experiment types
        """

        attributes = ['code', 'description', 'modificationDate', *additional_attributes]

        request = {
            "method": method_name,
            "params": [ self.token, {}, {} ],
        }
        resp = self._post_request(self.as_v3, request)
        if len(resp['objects']) >= 1:
            types = DataFrame(resp['objects'])
            types['modificationDate']= types['modificationDate'].map(format_timestamp)
            return types[attributes]
        else:
            raise ValueError("Nothing found!")


    def is_session_active(self):
        """ checks whether a session is still active. Returns true or false.
        """
        return self.is_token_valid(self.token)


    def is_token_valid(self, token=None):
        """Check if the connection to openBIS is valid.
        This method is useful to check if a token is still valid or if it has timed out,
        requiring the user to login again.
        :return: Return True if the token is valid, False if it is not valid.
        """
        if token is None:
            token = self.token
        
        if token is None:
            return False

        request = {
            "method": "isSessionActive",
            "params": [ token ],
        }
        resp = self._post_request(self.as_v1, request)
        return resp


    def get_dataset(self, permid):
        """fetch a dataset and some metadata attached to it:
        - properties
        - sample
        - parents
        - children
        - containers
        - dataStore
        - physicalData
        - linkedData
        :return: a DataSet object
        """

        dataset_request = {
            "method": "getDataSets",
            "params": [
                self.token,
                [
                    {
                        "permId": permid,
                        "@type": "as.dto.dataset.id.DataSetPermId"
                    }
                ],
                {
                "parents": {
                    "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                },
                "type": {
                    "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions"
                },
                "children": {
                  "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                },
                "containers": {
                    "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                },
                "physicalData": {
                    "@type": "as.dto.dataset.fetchoptions.PhysicalDataFetchOptions"
                },
                "linkedData": {
                    "@type": "as.dto.dataset.fetchoptions.LinkedDataFetchOptions",
                },
                "dataStore": {
                    "@type": "as.dto.datastore.fetchoptions.DataStoreFetchOptions",
                },
                "experiment": {
                    "@type": "as.dto.experiment.fetchoptions.ExperimentFetchOptions",
                    "project": {
                        "@type": "as.dto.project.fetchoptions.ProjectFetchOptions"
                    },
                },
                "sample": {
                    "@type": "as.dto.sample.fetchoptions.SampleFetchOptions"
                },
                "properties": {
                    "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                },
                "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions"
                }
            ],
        }

        resp = self._post_request(self.as_v3, dataset_request)
        if resp is not None:
            for permid in resp:
                return DataSet(self, resp[permid])
                #return DataSet(self, permid, resp[permid])


    def get_sample(self, sample_ident):
        """Retrieve metadata for the sample.
        Get metadata for the sample and any directly connected parents of the sample to allow access
        to the same information visible in the ELN UI. The metadata will be on the file system.
        :param sample_identifiers: A list of sample identifiers to retrieve.
        """

        if self.token is None:
            raise ValueError("Please login first")

        search_request = None

        # assume we got a sample identifier e.g. /TEST/TEST-SAMPLE
        match = re.match('/', sample_ident)
        if match:
            search_request = {
                "identifier": sample_ident,
                "@type": "as.dto.sample.id.SampleIdentifier"
            }
        else:
            # look if we got a PermID eg. 234567654345-123
            match = re.match('\d+\-\d+', sample_ident)
            if match:
                search_request = {
                    "permId": sample_ident,
                    "@type": "as.dto.sample.id.SamplePermId"
                }
            else:
                raise ValueError(
                    '"' + sample_ident + '" is neither a Sample Identifier nor a PermID'
                )
        fetch_options = {
            "type": {
                "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
            },
            "parents": {
                "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
                "properties": {
                    "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                }
            },
            "children": {
                "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
                "properties": {
                    "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                }
            },
            "dataSets": {
                "@type": "as.dto.dataset.fetchoptions.DataSetFetchOptions",
                "properties": {
                    "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
                },
                "type": {
                    "@type": "as.dto.dataset.fetchoptions.DataSetTypeFetchOptions"
                },
            },
            "properties":  fetch_option['properties'],
            "registrator": fetch_option['registrator'],
            "tags":        fetch_option['tags'],
        }

        sample_request = {
            "method": "getSamples",
            "params": [
                self.token,
                [
                    search_request, 
                ],
                fetch_options
            ],
        }

        resp = self._post_request(self.as_v3, sample_request)
        if resp is None or len(resp) == 0:
            raise ValueError('no such sample found: '+sample_ident)
        else:
            for sample_ident in resp:
                return Sample(self, resp[sample_ident])


    def delete_sample(self, permid, reason):
        """ Deletes a given sample.
        """
        sample_delete_request = {
            "method": "deleteSamples",
            "params": [
                self.token,
                [
                    {
                        "permId": permid,
                        "@type": "as.dto.sample.id.SamplePermId"
                    }
                ],
                {
                    "reason": reason,
                    "@type": "as.dto.sample.delete.SampleDeletionOptions"
                }
            ],
        }
        resp = self._post_request(self.as_v3, sample_delete_request)
        return


    def new_space(self, name, description=None):
        """ Creates a new space in the openBIS instance. Returns a list of all spaces
        """
        request = {
            "method": "createSpaces",
            "params": [
                self.token,
                [ {
                    "@id": 0,
                    "code": name,
                    "description": description,
                    "@type": "as.dto.space.create.SpaceCreation"
                } ]
            ],
        }
        resp = self._post_request(self.as_v3, request)
        return self.get_spaces(refresh=True)


    def new_analysis(self, name, description=None, sample=None, dss_code=None, result_files=None,
    notebook_files=None, parents=[]):

        """ An analysis contains the Jupyter notebook file(s) and some result files.
            Technically this method involves uploading files to the session workspace
            and activating the dropbox aka dataset ingestion service "jupyter-uploader-api"
        """

        if dss_code is None:
            dss_code = self.get_datastores()['code'][0]

        # if a sample identifier was given, use it as a string.
        # if a sample object was given, take its identifier
        # TODO: handle permId's 
        sample_identifier = None
        if isinstance(sample, str):
            sample_identifier = sample
        else:
            sample_identifier = sample.ident
        
        datastore_url = self._get_dss_url(dss_code)
        folder = time.strftime('%Y-%m-%d_%H-%M-%S')

        # upload the files
        data_sets = []
        if notebook_files is not None:
            notebooks_folder = os.path.join(folder, 'notebook_files')
            self.upload_files(
                datastore_url = datastore_url,
                files=notebook_files,
                folder= notebooks_folder, 
                wait_until_finished=True
            )
            data_sets.append({
                "dataSetType" : "JUPYTER_NOTEBOOk",
                "sessionWorkspaceFolder": notebooks_folder,
                "fileNames" : notebook_files,
                "properties" : {}
            })
        if result_files is not None:
            results_folder = os.path.join(folder, 'result_files')
            self.upload_files(
                datastore_url = datastore_url,
                files=result_files,
                folder=results_folder,
                wait_until_finished=True
            )
            data_sets.append({
                "dataSetType" : "JUPYTER_RESULT",
                "sessionWorkspaceFolder" : results_folder,
                "fileNames" : result_files,
                "properties" : {}
            })

        # register the files in openBIS
        request = {
          "method": "createReportFromAggregationService",
          "params": [
            self.token,
            dss_code,
            DROPBOX_PLUGIN,
            { 
            	"sample" : {
                	"identifier" : sample.identifier
                },
                "containers" : [ 
                    {
                    	"dataSetType" : "JUPYTER_CONTAINER",
                    	"properties" : {
                			"NAME" : name,
                			"DESCRIPTION" : description
                    	}
                    }
                ],
                "dataSets" : data_sets,
                "parents" : parents,
            }
          ],
        }
        
        resp = self._post_request(self.reg_v1, request)
        try:
            if resp['rows'][0][0]['value'] == 'OK':
                return resp['rows'][0][1]['value']
        except:
            return resp


    def new_sample(self, sample_name, space_name, sample_type, tags=[], **kwargs):
        """ Creates a new sample of a given sample type. sample_name, sample_type and space are
        mandatory arguments.
        """

        if isinstance(tags, str):
            tags = [tags]
        tag_ids = []
        for tag in tags:
            tag_dict = {
                "code":tag,
                "@type":"as.dto.tag.id.TagCode"
            }
            tag_ids.append(tag_dict)


        sample_create_request = {
            "method":"createSamples",
            "params":[
                self.token,
                [ {
                    "properties":{},
                    "typeId":{
                        "permId": sample_type,
                        "@type":"as.dto.entitytype.id.EntityTypePermId"
                    },
                    "code": sample_name,
                    "spaceId":{
                        "permId": space_name,
                        "@type":"as.dto.space.id.SpacePermId"
                    },
                    "tagIds":tag_ids,
                    "@type":"as.dto.sample.create.SampleCreation",
                    "experimentId":None,
                    "containerId":None,
                    "componentIds":None,
                    "parentIds":None,
                    "childIds":None,
                    "attachments":None,
                    "creationId":None,
                    "autoGeneratedCode":None
                } ]
            ],
        }
        resp = self._post_request(self.as_v3, sample_create_request)
        if 'permId' in resp[0]:
            return self.get_sample(resp[0]['permId'])
        else:
            raise ValueError("error while trying to fetch sample from server: " + str(resp))



    def _get_dss_url(self, dss_code=None):
        """ internal method to get the downloadURL of a datastore.
        """
        dss = self.get_datastores()
        if dss_code is None:
            return dss['downloadUrl'][0]
        else:
            return dss[dss['code'] == dss_code]['downloadUrl'][0]
        


    def upload_files(self, datastore_url=None, files=None, folder=None, wait_until_finished=False):

        if datastore_url is None:
            datastore_url = self._get_dss_url()

        if files is None:
            raise ValueError("Please provide a filename.")

        if folder is None:
            # create a unique foldername
            folder = time.strftime('%Y-%m-%d_%H-%M-%S')

        if isinstance(files, str):
            files = [files]

        self.files = files
        self.startByte = 0
        self.endByte   = 0
    
        # define a queue to handle the upload threads
        queue = DataSetUploadQueue()

        real_files = []
        for filename in files:
            if os.path.isdir(filename):
                real_files.extend([os.path.join(dp, f) for dp, dn, fn in os.walk(os.path.expanduser(filename)) for f in fn])
            else:
                real_files.append(os.path.join(filename))

        # compose the upload-URL and put URL and filename in the upload queue 
        for filename in real_files:
            file_in_wsp = os.path.join(folder, filename)
            self.files_in_wsp.append(file_in_wsp)
            upload_url = (
                datastore_url + '/session_workspace_file_upload'
                + '?filename=' + os.path.join(folder,filename)
                + '&id=1'
                + '&startByte=0&endByte=0'
                + '&sessionID=' + self.token
            )
            queue.put([upload_url, filename, self.verify_certificates])

        # wait until all files have uploaded
        if wait_until_finished:
            queue.join()

        # return files with full path in session workspace
        return self.files_in_wsp


class DataSetUploadQueue:
   
    def __init__(self, workers=20):
        # maximum files to be uploaded at once
        self.upload_queue = Queue()

        # define number of threads and start them
        for t in range(workers):
            t = Thread(target=self.upload_file)
            t.daemon = True
            t.start()


    def put(self, things):
        """ expects a list [url, filename] which is put into the upload queue
        """
        self.upload_queue.put(things)


    def join(self):
        """ needs to be called if you want to wait for all uploads to be finished
        """
        self.upload_queue.join()


    def upload_file(self):
        while True:
            # get the next item in the queue
            upload_url, filename, verify_certificates = self.upload_queue.get()

            filesize = os.path.getsize(filename)

            # upload the file to our DSS session workspace
            with open(filename, 'rb') as f:
                resp = requests.post(upload_url, data=f, verify=verify_certificates)
                resp.raise_for_status()
                data = resp.json()
                assert filesize == int(data['size'])

            # Tell the queue that we are done
            self.upload_queue.task_done()


class DataSetDownloadQueue:
    
    def __init__(self, workers=20):
        # maximum files to be downloaded at once
        self.download_queue = Queue()

        # define number of threads
        for t in range(workers):
            t = Thread(target=self.download_file)
            t.daemon = True
            t.start()


    def put(self, things):
        """ expects a list [url, filename] which is put into the download queue
        """
        self.download_queue.put(things)


    def join(self):
        """ needs to be called if you want to wait for all downloads to be finished
        """
        self.download_queue.join()


    def download_file(self):
        while True:
            url, filename, file_size, verify_certificates = self.download_queue.get()
            # create the necessary directory structure if they don't exist yet
            os.makedirs(os.path.dirname(filename), exist_ok=True)

            # request the file in streaming mode
            r = requests.get(url, stream=True, verify=verify_certificates)
            with open(filename, 'wb') as f:
                for chunk in r.iter_content(chunk_size=1024): 
                    if chunk: # filter out keep-alive new chunks
                        f.write(chunk)

            assert os.path.getsize(filename) == int(file_size)
            self.download_queue.task_done()


class DataSet(dict):
    """ DataSet are openBIS objects that contain the actual files.
    """

    def __init__(self, openbis_obj, *args, **kwargs):
        super(DataSet, self).__init__(*args, **kwargs)
        self.__dict__ = self
        self.permid = self["permId"]["permId"]
        self.openbis = openbis_obj
        if self['physicalData'] is None:
            self.shareId = None
            self.location = None
        else:
            self.shareId = self['physicalData']['shareId']
            self.location = self['physicalData']['location']


    def download(self, files=None, wait_until_finished=True, workers=10):
        """ download the actual files and put them by default in the following folder:
        __current_dir__/hostname/dataset_permid/
        If no files are specified, all files of a given dataset are downloaded.
        Files are usually downloaded in parallel, using 10 workers by default. If you want to wait until
        all the files are downloaded, set the wait_until_finished option to True.
        """

        if files == None:
            files = self.file_list()
        elif isinstance(files, str):
            files = [files]

        base_url = self['dataStore']['downloadUrl'] + '/datastore_server/' + self.permid + '/'

        queue = DataSetDownloadQueue(workers=workers)

        # get file list and start download
        for filename in files:
            file_info = self.get_file_list(start_folder=filename)
            file_size = file_info[0]['fileSize']
            download_url = base_url + filename + '?sessionID=' + self.openbis.token 
            filename = os.path.join(self.openbis.hostname, self.permid, filename)
            queue.put([download_url, filename, file_size, self.openbis.verify_certificates])

        # wait until all files have downloaded
        if wait_until_finished:
            queue.join()


        print("Files downloaded to: %s" % os.path.join(self.openbis.hostname, self.permid))


    def get_parents(self):
        """ Returns an array of the parents of the given dataset. Returns an empty array if no
        parents were found.
        """
        parents = []
        for item in self['parents']:
            parent = self.openbis.get_dataset(item['code'])
            if parent is not None:
                parents.append(parent)
        return parents


    def get_children(self):
        """ Returns an array of the children of the given dataset. Returns an empty array if no
        children were found.
        """
        children = []
        for item in self['children']:
            child = self.openbis.get_dataset(item['code'])
            if child is not None:
                children.append(child)
        return children


    def file_list(self):
        files = []
        for file in self.get_file_list(recursive=True):
            if file['isDirectory']:
                pass
            else:
                files.append(file['pathInDataSet'])
        return files


    def get_files(self, start_folder='/'):
        """ Returns a DataFrame of all files in this dataset
        """

        def createRelativePath(pathInDataSet):
            if self.shareId is None:
                return ''
            else:
                return os.path.join(self.shareId, self.location, pathInDataSet)
            
        files = self.get_file_list(start_folder=start_folder)
        df = DataFrame(files)
        df['relativePath'] = df['pathInDataSet'].map(createRelativePath)
        df['crc32Checksum'] = df['crc32Checksum'].fillna(0.0).astype(int)
        return df[['isDirectory', 'pathInDataSet', 'fileSize', 'crc32Checksum']]
        

    def get_file_list(self, recursive=True, start_folder="/"):
        """ Lists all files of a given dataset. You can specifiy a start_folder other than "/".
        By default, all directories and their containing files are listed recursively. You can
        turn off this option by setting recursive=False.
        """
        request = {
            "method" : "listFilesForDataSet",
            "params" : [ 
                self.openbis.token,
                self.permid, 
                start_folder,
                recursive,
            ],
            "id":"1"
        }

        resp = requests.post(
            self["dataStore"]["downloadUrl"] + '/datastore_server/rmi-dss-api-v1.json',
            json.dumps(request), 
            verify=self.openbis.verify_certificates
        )

        if resp.ok:
            data = resp.json()
            if 'error' in data:
                raise ValueError('Error from openBIS: ' + data['error'] )
            elif 'result' in data:
                return data['result']
            else:
                raise ValueError('request to openBIS did not return either result nor error')
        else:
            raise ValueError('internal error while performing post request')


class Sample(dict):
    """ A Sample is one of the most commonly used objects in openBIS.
    """

    def __init__(self, openbis_obj, *args, **kwargs):
        super(Sample, self).__init__(*args, **kwargs)
        self.__dict__ = self
        self.openbis = openbis_obj
        self.permid = self.permId['permId']
        self.ident = self.identifier['identifier']


    def delete(self, reason):
        self.openbis.delete_sample(self.permid, reason)


    def get_datasets(self):
        objects = self.dataSets
        cache = {}
        for obj in objects:
            for key in obj.keys():
                if key in ('type'):
                    if isinstance(obj[key], dict):
                        cache[ obj[key]['@id'] ] = obj[key]
                    else:
                        if obj[key] in cache:
                            obj[key] = cache[ obj[key] ]
        datasets = DataFrame(objects)
        datasets['registrationDate'] = datasets['registrationDate'].map(format_timestamp)
        datasets['properties'] = datasets['properties'].map(extract_properties)
        datasets['type'] = datasets['type'].map(extract_code)
        return datasets
        #return datasets[['code','properties', 'type', 'registrationDate']]


    def get_parents(self):
        parents = []
        for item in self.parents:
            parent = self.openbis.get_sample(item['permId']['permId'])
            if parent is not None:
                parents.append(parent)
        return parents


    def get_children(self):
        children = []
        for item in self.children:
            child = self.openbis.get_sample(item['permId']['permId'])
            if child is not None:
                children.append(child)
        return children

        
class Space(dict):
    """ managing openBIS spaces
    """

    def __init__(self, openbis_obj, *args, **kwargs):
        super(Space, self).__init__(*args, **kwargs)
        self.__dict__ = self
        self.openbis = openbis_obj


    def get_samples(self, *args, **kwargs):
        """ Lists all samples in a given space. A pandas DataFrame object is returned.
        """

        return self.openbis.get_samples(space=self.code, *args, **kwargs)

    @property
    def projects(self):
        return self.openbis.get_projects(space=self.code)
    
    def new_project(self, **kwargs):
        return self.openbis.new_project(space=self.code, **kwargs)

    @property
    def experiments(self):
        return self.openbis.get_experiments(space=self.code)


class Things():
    """An object that contains a DataFrame object about an entity  available in openBIS.
       
    """

    def __init__(self, openbis_obj, what, df, identifier_name='code'):
        self.openbis = openbis_obj
        self.what = what
        self.df = df
        self.identifier_name = identifier_name

    def _repr_html_(self):
        return self.df._repr_html_()

    def __getitem__(self, key):
        if self.df is not None and len(self.df) > 0:
            row = None
            if isinstance(key, int):
                # get thing by rowid
                row = self.df.loc[[key]]
            else:
                # get thing by code
                row = self.df[self.df[self.identifier_name]==key.upper()]

            if row is not None:
                # invoke the openbis.get_what() method
                return getattr(self.openbis, 'get_'+self.what)(row[self.identifier_name].values[0])


class Experiment(dict):
    """ managing openBIS experiments
    """

    def __init__(self, openbis_obj, *args, **kwargs):
        super(Experiment, self).__init__(*args, **kwargs)
        self.__dict__ = self
        self.openbis = openbis_obj

    def __repr__(self):
        data = {}
        data["identifier"] = self['identifier']['identifier']
        data["permId"] = self['permId']['permId']
        data["properties"] = self['properties']
        return repr(data)

