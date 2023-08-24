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
pybis.py

Work with openBIS using Python.

"""

import json
import os
import re
import subprocess
import time
import zlib
from datetime import datetime
from pathlib import Path
from typing import List
from urllib.parse import urljoin, urlparse

import requests
import urllib3
from dateutil.relativedelta import relativedelta
from pandas import DataFrame

from . import data_set as pbds
from .dataset import DataSet
from .definitions import (
    get_definition_for_entity,
    get_fetchoption_for_entity,
    get_fetchoptions,
    get_method_for_entity,
    get_type_for_entity,
    openbis_definitions,
)
from .entity_type import (
    DataSetType,
    EntityType,
    ExperimentType,
    MaterialType,
    SampleType,
)
from .experiment import Experiment
from .group import Group
from .openbis_object import OpenBisObject, Transaction
from .person import Person
from .project import Project
from .role_assignment import RoleAssignment
from .sample import Sample
from .semantic_annotation import SemanticAnnotation
from .space import Space
from .tag import Tag
from .things import Things
from .utils import (
    VERBOSE,
    extract_attr,
    extract_code,
    extract_deletion,
    extract_id,
    extract_identifier,
    extract_identifiers,
    extract_nested_identifier,
    extract_nested_permid,
    extract_nested_permids,
    extract_permid,
    extract_person,
    extract_userId,
    format_timestamp,
    is_identifier,
    is_number,
    is_permid,
    parse_jackson,
    split_identifier,
)
from .vocabulary import Vocabulary, VocabularyTerm

# import the various openBIS entities

LOG_NONE = 0
LOG_SEVERE = 1
LOG_ERROR = 2
LOG_WARNING = 3
LOG_INFO = 4
LOG_ENTRY = 5
LOG_PARM = 6
LOG_DEBUG = 7
PYBIS_FOLDER = Path.home() / ".pybis"
CONFIG_FILENAME = ".pybis.json"

DEBUG_LEVEL = LOG_NONE


def now():
    return time.time()


def get_search_type_for_entity(entity, operator=None):
    """Returns a dictionary containing the correct search criteria type
    for a given entity.

    Example::
        get_search_type_for_entity('space')
        # returns:
        {'@type': 'as.dto.space.search.SpaceSearchCriteria'}
    """
    search_criteria = {
        "personalAccessToken": "as.dto.pat.search.PersonalAccessTokenSearchCriteria",
        "space": "as.dto.space.search.SpaceSearchCriteria",
        "userId": "as.dto.person.search.UserIdSearchCriteria",
        "email": "as.dto.person.search.EmailSearchCriteria",
        "firstName": "as.dto.person.search.FirstNameSearchCriteria",
        "lastName": "as.dto.person.search.LastNameSearchCriteria",
        "project": "as.dto.project.search.ProjectSearchCriteria",
        "experiment": "as.dto.experiment.search.ExperimentSearchCriteria",
        "experiment_type": "as.dto.experiment.search.ExperimentTypeSearchCriteria",
        "sample": "as.dto.sample.search.SampleSearchCriteria",
        "sample_type": "as.dto.sample.search.SampleTypeSearchCriteria",
        "dataset": "as.dto.dataset.search.DataSetSearchCriteria",
        "dataset_type": "as.dto.dataset.search.DataSetTypeSearchCriteria",
        "external_dms": "as.dto.externaldms.search.ExternalDmsSearchCriteria",
        "material": "as.dto.material.search.MaterialSearchCriteria",
        "material_type": "as.dto.material.search.MaterialTypeSearchCriteria",
        "vocabulary_term": "as.dto.vocabulary.search.VocabularyTermSearchCriteria",
        "tag": "as.dto.tag.search.TagSearchCriteria",
        "authorizationGroup": "as.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria",
        "person": "as.dto.person.search.PersonSearchCriteria",
        "code": "as.dto.common.search.CodeSearchCriteria",
        "global": "as.dto.global.GlobalSearchObject",
        "plugin": "as.dto.plugin.search.PluginSearchCriteria",
        "propertyType": "as.dto.property.search.PropertyTypeSearchCriteria",
    }

    sc = {"@type": search_criteria[entity]}
    if operator is not None:
        sc["operator"] = operator

    return sc


def is_session_token(token: str):
    return not token.startswith("$pat")


def is_personal_access_token(token: str):
    return token.startswith("$pat")


def get_saved_tokens():
    tokens = {}
    for filepath in PYBIS_FOLDER.glob("*.token"):
        with open(filepath) as fh:
            if filepath.is_file:
                token = fh.read()
                tokens[filepath.stem] = token
    return tokens


def get_token_for_hostname(hostname, session_token_needed=True):
    """Searches for a stored token for a given host in this order:
    ~/.pybis/hostname.token
    """
    tokens = get_saved_tokens()
    if hostname in tokens:
        if session_token_needed:
            if is_session_token(tokens[hostname]):
                return tokens[hostname]
        else:
            return tokens[hostname]
    return


def save_pats_to_disk(hostname: str, url: str, resp: dict) -> None:
    pats = resp["objects"]
    parse_jackson(pats)
    path = PYBIS_FOLDER / hostname
    path.mkdir(exist_ok=True)
    for existing_file in path.glob("*.pat"):
        existing_file.unlink()

    for token in pats:
        data = {
            "url": url,
            "hostname": hostname,
            "owner": token["owner"]["userId"],
            "registrationDate": format_timestamp(token["owner"]["registrationDate"]),
            "validFromDate": format_timestamp(token["validFromDate"]),
            "validToDate": format_timestamp(token["validToDate"]),
            "sessionName": token["sessionName"],
            "permId": token["permId"]["permId"],
        }
        with open(path / (token["hash"] + ".pat"), "w", encoding="utf-8") as fh:
            fh.write(json.dumps(data, indent=4))


def get_saved_pats(hostname=None, sessionName=None):
    """return all personal access tokens stored on disk."""
    if hostname is None:
        hostname = ""
    path = PYBIS_FOLDER / hostname
    tokens = []
    for filepath in path.rglob("*.pat"):
        with open(filepath) as fh:
            if filepath.is_file:
                pat = json.load(fh)
                if sessionName:
                    if pat["sessionName"] != sessionName:
                        continue
                tokens.append(pat)
    return tokens


def _type_for_id(ident, entity):
    """Returns the data type for a given identifier/permId for use with the API call, e.g.
    {
        "identifier": "/DEFAULT/SAMPLE_NAME",
        "@type": "as.dto.sample.id.SampleIdentifier"
    }
    or
    {
        "permId": "20160817175233002-331",
        "@type": "as.dto.sample.id.SamplePermId"
    }
    """
    # Tags have strange permIds...
    ident = ident.strip()
    if entity.lower() == "tag":
        if "/" in ident:
            if not ident.startswith("/"):
                ident = "/" + ident
            return {"permId": ident, "@type": "as.dto.tag.id.TagPermId"}
        else:
            return {"code": ident, "@type": "as.dto.tag.id.TagCode"}
    if entity == "personalAccessToken":
        return {"permId": ident, "@type": "as.dto.pat.id.PersonalAccessTokenPermId"}

    entities = {
        "sample": "Sample",
        "dataset": "DataSet",
        "experiment": "Experiment",
        "plugin": "Plugin",
        "space": "Space",
        "project": "Project",
        "semanticannotation": "SemanticAnnotation",
    }
    search_request = {}
    if entity.lower() in entities:
        entity_capitalize = entities[entity.lower()]
    else:
        entity_capitalize = entity.capitalize()

    if is_identifier(ident):
        # people tend to omit the / prefix of an identifier...
        if not ident.startswith("/"):
            ident = "/" + ident
        # ELN-LIMS style contains also experiment in sample identifer, i.e. /space/project/experiment/sample_code
        # we have to remove the experiment-code
        if ident.count("/") == 4:
            codes = ident.split("/")
            ident = "/".join([codes[0], codes[1], codes[2], codes[4]])

        search_request = {
            "identifier": ident.upper(),
            "@type": f"as.dto.{entity.lower()}.id.{entity_capitalize}Identifier",
        }
    else:
        search_request = {
            "permId": ident,
            "@type": f"as.dto.{entity.lower()}.id.{entity_capitalize}PermId",
        }
    return search_request


def get_search_criteria(entity, **search_args):
    search_criteria = get_search_type_for_entity(entity)

    criteria = []
    attrs = openbis_definitions(entity)["attrs"]
    for attr in attrs:
        if attr in search_args:
            sub_crit = get_search_type_for_entity(attr)
            sub_crit["fieldValue"] = get_field_value_search(attr, search_args[attr])
            criteria.append(sub_crit)

    search_criteria["criteria"] = criteria
    search_criteria["operator"] = "AND"

    return search_criteria


def crc32(fileName):
    """since Python3 the zlib module returns unsigned integers (2.7: signed int)"""
    prev = 0
    for eachLine in open(fileName, "rb"):
        prev = zlib.crc32(eachLine, prev)
    # return as hex
    return "%x" % (prev & 0xFFFFFFFF)


def _tagIds_for_tags(tags=None, action="Add"):
    """creates an action item to add or remove tags.
    Action is either 'Add', 'Remove' or 'Set'
    """
    if tags is None:
        return
    if not isinstance(tags, list):
        tags = [tags]

    items = list(map(lambda tag: {"code": tag, "@type": "as.dto.tag.id.TagCode"}, tags))

    tagIds = {
        "actions": [
            {
                "items": items,
                "@type": f"as.dto.common.update.ListUpdateAction{action.capitalize()}",
            }
        ],
        "@type": "as.dto.common.update.IdListUpdateValue",
    }
    return tagIds


def _list_update(ids=None, entity=None, action="Add"):
    """creates an action item to add, set or remove ids."""
    if ids is None:
        return
    if not isinstance(ids, list):
        ids = [ids]

    items = list(
        map(
            lambda id: {
                "code": id,
                "@type": f"as.dto.{entity.lower()}.id.{entity}Code",
            },
            ids,
        )
    )

    list_update = {
        "actions": [
            {
                "items": items,
                "@type": f"as.dto.common.update.ListUpdateAction{action.capitalize()}",
            }
        ],
        "@type": "as.dto.common.update.IdListUpdateValue",
    }
    return list_update


def get_field_value_search(field, value, comparison="StringEqualToValue"):
    return {"value": value, "@type": f"as.dto.common.search.{comparison}"}


def _common_search(search_type, value, comparison="StringEqualToValue"):
    sreq = {
        "@type": search_type,
        "fieldValue": {
            "value": value,
            "@type": f"as.dto.common.search.{comparison}",
        },
    }
    return sreq


def _criteria_for_code(code):
    return {
        "fieldValue": {
            "value": code.upper(),
            "@type": "as.dto.common.search.StringEqualToValue",
        },
        "@type": "as.dto.common.search.CodeSearchCriteria",
    }


def _criteria_for_permId(permId):
    return {
        "fieldName": "perm_id",
        "fieldType": "ATTRIBUTE",
        "fieldValue": {
            "value": permId,
            "@type": "as.dto.common.search.StringEqualToValue",
        },
        "@type": "as.dto.common.search.PermIdSearchCriteria",
    }


def _subcriteria_for_userId(userId):
    return {
        "criteria": [
            {
                "fieldName": "userId",
                "fieldType": "ATTRIBUTE",
                "fieldValue": {
                    "value": userId,
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "@type": "as.dto.person.search.UserIdSearchCriteria",
            }
        ],
        "@type": "as.dto.person.search.PersonSearchCriteria",
        "operator": "AND",
    }


def _subcriteria_for_type(code, entity):
    return {
        "@type": f"as.dto.{entity.lower()}.search.{entity}TypeSearchCriteria",
        "criteria": [
            {
                "@type": "as.dto.common.search.CodeSearchCriteria",
                "fieldValue": {
                    "value": code.upper(),
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
            }
        ],
    }


def _subcriteria_for_status(status_value):
    status_value = status_value.upper()
    valid_status = "AVAILABLE LOCKED ARCHIVED UNARCHIVE_PENDING ARCHIVE_PENDING BACKUP_PENDING".split()
    if not status_value in valid_status:
        raise ValueError(
            "status must be one of the following: " + ", ".join(valid_status)
        )

    return {
        "@type": "as.dto.dataset.search.PhysicalDataSearchCriteria",
        "operator": "AND",
        "criteria": [
            {
                "@type": "as.dto.dataset.search.StatusSearchCriteria",
                "fieldName": "status",
                "fieldType": "ATTRIBUTE",
                "fieldValue": status_value,
            }
        ],
    }


def _gen_search_criteria(req):
    sreq = {}
    for key, val in req.items():
        if key == "criteria":
            sreq["criteria"] = list(
                map(lambda item: _gen_search_criteria(item), req["criteria"])
            )
        elif key == "code":
            sreq["criteria"] = [
                _common_search("as.dto.common.search.CodeSearchCriteria", val.upper())
            ]
        elif key == "identifier":
            if is_identifier(val):
                # if we have an identifier, we need to search in Space and Code separately
                si = split_identifier(val)
                sreq["criteria"] = []
                if "space" in si:
                    sreq["criteria"].append(
                        _gen_search_criteria({"space": "Space", "code": si["space"]})
                    )
                if "experiment" in si:
                    pass

                if "code" in si:
                    sreq["criteria"].append(
                        _common_search(
                            "as.dto.common.search.CodeSearchCriteria",
                            si["code"].upper(),
                        )
                    )
            elif is_permid(val):
                sreq["criteria"] = [
                    _common_search("as.dto.common.search.PermIdSearchCriteria", val)
                ]
            else:
                # we assume we just got a code
                sreq["criteria"] = [
                    _common_search(
                        "as.dto.common.search.CodeSearchCriteria", val.upper()
                    )
                ]

        elif key == "operator":
            sreq["operator"] = val.upper()
        else:
            sreq["@type"] = f"as.dto.{key}.search.{val}SearchCriteria"
    return sreq


def _subcriteria_for_tags(tags):
    if not isinstance(tags, list):
        tags = [tags]

    criteria = list(
        map(
            lambda tag: {
                "fieldName": "code",
                "fieldType": "ATTRIBUTE",
                "fieldValue": {
                    "value": tag,
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "@type": "as.dto.common.search.CodeSearchCriteria",
            },
            tags,
        )
    )

    return {
        "@type": "as.dto.tag.search.TagSearchCriteria",
        "operator": "AND",
        "criteria": criteria,
    }


def _subcriteria_for_is_finished(is_finished):
    return {
        "@type": "as.dto.common.search.StringPropertySearchCriteria",
        "fieldName": "FINISHED_FLAG",
        "fieldType": "PROPERTY",
        "fieldValue": {
            "value": is_finished,
            "@type": "as.dto.common.search.StringEqualToValue",
        },
    }


def _subcriteria_for_properties(prop, value, entity):
    """This internal method creates the JSON RPC criterias for searching
    in properties. It distinguishes between numbers, dates and strings
    and uses the comparative operator (< > >= <=), if available.
    creationDate and modificationDate attributes can be searched as well.
    To search in the properties of parents, children, etc. the user has to
    prefix the propery accordingly:

    - parent_propertyName
    - child_propertyName
    - container_propertyName
    """
    additional_attr = {}
    if "*" in str(value):
        additional_attr["useWildcards"] = True
    else:
        additional_attr["useWildcards"] = False

    search_types = {
        "sample": {
            "parent": "as.dto.sample.search.SampleParentsSearchCriteria",
            "parents": "as.dto.sample.search.SampleParentsSearchCriteria",
            "child": "as.dto.sample.search.SampleChildrenSearchCriteria",
            "children": "as.dto.sample.search.SampleChildrenSearchCriteria",
            "container": "as.dto.sample.search.SampleContainerSearchCriteria",
        },
        "dataset": {
            "parent": "as.dto.dataset.search.DataSetParentsSearchCriteria",
            "parents": "as.dto.dataset.search.DataSetParentsSearchCriteria",
            "child": "as.dto.dataset.search.DataSetChildrenSearchCriteria",
            "children": "as.dto.dataset.search.DataSetChildrenSearchCriteria",
            "container": "as.dto.dataset.search.DataSetContainerSearchCriteria",
        },
    }

    # default values of fieldType, str_type and eq_type
    fieldType = "PROPERTY"
    eq_type = "as.dto.common.search.StringEqualToValue"
    str_type = "as.dto.common.search.StringPropertySearchCriteria"

    is_date = False
    if "date" in prop.lower() and re.search(r"\d{4}\-\d{2}\-\d{2}", value):
        is_date = True
        eq_type = "as.dto.common.search.DateEqualToValue"
        if prop.lower().endswith("registrationdate"):
            str_type = "as.dto.common.search.RegistrationDateSearchCriteria"
            fieldType = "ATTRIBUTE"
        elif prop.lower().endswith("modificationdate"):
            str_type = "as.dto.common.search.ModificationDateSearchCriteria"
            fieldType = "ATTRIBUTE"
        else:
            str_type = "as.dto.common.search.DatePropertySearchCriteria"

    if any(str(value).startswith(operator) for operator in [">", "<", "="]):
        match = re.search(
            r"""
            ^
            (?P<comp_operator>\>\=|\>|\<\=|\<|\=\=|\=)  # extract the comparative operator
            \s*
            (?P<value>.*)                           # extract the value
            """,
            value,
            flags=re.X,
        )
        if match:
            comp_operator = match.groupdict()["comp_operator"]
            value = match.groupdict()["value"]

            # date comparison
            if is_date:
                if comp_operator == ">":
                    eq_type = "as.dto.common.search.DateLaterThanOrEqualToValue"
                elif comp_operator == ">=":
                    eq_type = "as.dto.common.search.DateLaterThanOrEqualToValue"
                elif comp_operator == "<":
                    eq_type = "as.dto.common.search.DateEarlierThanOrEqualToValue"
                elif comp_operator == "<=":
                    eq_type = "as.dto.common.search.DateEarlierThanOrEqualToValue"
                else:
                    eq_type = "as.dto.common.search.DateEqualToValue"

            # numeric comparison
            elif is_number(value):
                str_type = "as.dto.common.search.NumberPropertySearchCriteria"
                if comp_operator == ">":
                    eq_type = "as.dto.common.search.NumberGreaterThanValue"
                elif comp_operator == ">=":
                    eq_type = "as.dto.common.search.NumberGreaterThanOrEqualToValue"
                elif comp_operator == "<":
                    eq_type = "as.dto.common.search.NumberLessThanValue"
                elif comp_operator == "<=":
                    eq_type = "as.dto.common.search.NumberLessThanOrEqualToValue"
                else:
                    eq_type = "as.dto.common.search.NumberEqualToValue"

            # string comparison
            else:
                if comp_operator == ">":
                    eq_type = "as.dto.common.search.StringGreaterThanValue"
                elif comp_operator == ">=":
                    eq_type = "as.dto.common.search.StringGreaterThanOrEqualToValue"
                elif comp_operator == "<":
                    eq_type = "as.dto.common.search.StringLessThanValue"
                elif comp_operator == "<=":
                    eq_type = "as.dto.common.search.StringLessThanOrEqualToValue"
                elif comp_operator == "=":
                    eq_type = "as.dto.common.search.StringEqualToValue"
                    additional_attr["useWildcards"] = False
                else:
                    eq_type = "as.dto.common.search.StringEqualToValue"

    # searching for parent/child/container identifier
    if any(
            relation == prop.lower()
            for relation in [
                "parent",
                "child",
                "container",
                "parents",
                "children",
                "containers",
            ]
    ):
        relation = prop.lower()
        if is_identifier(value):
            identifier_search_type = "as.dto.common.search.IdentifierSearchCriteria"
        # find any parent, child, container
        elif value == "*":
            return {
                "@type": search_types[entity][relation],
                "criteria": [
                    {
                        "@type": "as.dto.common.search.AnyFieldSearchCriteria",
                        "fieldValue": {
                            "@type": "as.dto.common.search.AnyStringValue",
                        },
                    }
                ],
            }
        elif is_permid(value):
            identifier_search_type = "as.dto.common.search.PermIdSearchCriteria"
        else:
            identifier_search_type = "as.dto.common.search.CodeSearchCriteria"
        return {
            "@type": search_types[entity][relation],
            "criteria": [
                {
                    "@type": identifier_search_type,
                    "fieldType": "ATTRIBUTE",
                    "fieldValue": {
                        "@type": "as.dto.common.search.StringEqualToValue",
                        "value": value,
                    },
                    **additional_attr,
                }
            ],
        }

    # searching for parent/child/container property:
    elif any(
            prop.lower().startswith(relation)
            for relation in ["parent_", "child_", "container_"]
    ):
        match = re.search(r"^(\w+?)_(.*)", prop.lower())
        if match:
            relation, property_name = match.groups()
            return {
                "@type": search_types[entity][relation],
                "criteria": [
                    {
                        "@type": str_type,
                        "fieldName": property_name.upper(),
                        "fieldType": fieldType,
                        "fieldValue": {
                            "@type": eq_type,
                            "value": value,
                        },
                        **additional_attr,
                    }
                ],
            }

    # searching for properties
    if prop.startswith("_"):
        fieldName = "$" + prop[1:]
    else:
        fieldName = prop
    return {
        "@type": str_type,
        "fieldName": fieldName.upper(),
        "fieldType": fieldType,
        "fieldValue": {"value": value, "@type": eq_type},
        **additional_attr,
    }


def _subcriteria_for(thing, entity, parents_or_children="", operator="AND"):
    """Returns the sub-search criteria for «thing», which can be either:
    - a python object (sample, dataSet, experiment)
    - a permId
    - an identifier
    - a code
    """

    entity, *_ = entity.split(".")
    if _:
        new_entity = ".".join(_)
        subcrit = _subcriteria_for(thing, new_entity)

        search_type = get_type_for_entity(entity, "search", parents_or_children)
        return {"criteria": subcrit, **search_type, "operator": operator}

    if isinstance(thing, str):
        if is_permid(thing):
            return _subcriteria_for_permid(
                thing,
                entity=entity,
                parents_or_children=parents_or_children,
                operator=operator,
            )
        elif is_identifier(thing):
            return _subcriteria_for_identifier(
                thing,
                entity=entity,
                parents_or_children=parents_or_children,
                operator=operator,
            )
        else:
            # look for code
            return _subcriteria_for_code_new(
                thing,
                entity=entity,
                parents_or_children=parents_or_children,
                operator=operator,
            )

    elif isinstance(thing, list):
        criteria = []
        for element in thing:
            crit = _subcriteria_for(element, entity, parents_or_children, operator)
            criteria += crit["criteria"]

        return {"criteria": criteria, "@type": crit["@type"], "operator": "OR"}
    elif thing is None:
        # we just need the type
        search_type = get_type_for_entity(entity, "search", parents_or_children)
        return {"criteria": [], **search_type, "operator": operator}
    else:
        # we passed an object
        return _subcriteria_for_permid(
            thing.permId,
            entity=entity,
            parents_or_children=parents_or_children,
            operator=operator,
        )


def _subcriteria_for_identifier(ids, entity, parents_or_children="", operator="AND"):
    if not isinstance(ids, list):
        ids = [ids]

    criteria = list(
        map(
            lambda id: {
                "@type": "as.dto.common.search.IdentifierSearchCriteria",
                "fieldValue": {
                    "value": id,
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "fieldType": "ATTRIBUTE",
                "fieldName": "identifier",
            },
            ids,
        )
    )

    search_type = get_type_for_entity(entity, "search", parents_or_children)
    return {"criteria": criteria, **search_type, "operator": operator}


def _subcriteria_for_permid(permids, entity, parents_or_children="", operator="AND"):
    if not isinstance(permids, list):
        permids = [permids]

    criteria = list(
        map(
            lambda permid: {
                "@type": "as.dto.common.search.PermIdSearchCriteria",
                "fieldValue": {
                    "value": permid,
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "fieldType": "ATTRIBUTE",
                "fieldName": "perm_id",
            },
            permids,
        )
    )

    search_type = get_type_for_entity(entity, "search", parents_or_children)
    return {"criteria": criteria, **search_type, "operator": operator}


def _subcriteria_for_permid_new(codes, entity, parents_or_children="", operator="AND"):
    if not isinstance(codes, list):
        codes = [codes]

    criteria = list(
        map(
            lambda code: {
                "@type": "as.dto.common.search.PermIdSearchCriteria",
                "fieldValue": {
                    "value": code,
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "fieldType": "ATTRIBUTE",
                "fieldName": "perm_id",
            },
            codes,
        )
    )

    search_type = get_type_for_entity(entity, "search", parents_or_children)
    return {"criteria": criteria, **search_type, "operator": operator}


def _subcriteria_for_code_new(codes, entity, parents_or_children="", operator="AND"):
    if not isinstance(codes, list):
        codes = [codes]

    criteria = list(
        map(
            lambda code: {
                "@type": "as.dto.common.search.CodeSearchCriteria",
                "fieldValue": {
                    "value": code,
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "fieldType": "ATTRIBUTE",
                "fieldName": "code",
            },
            codes,
        )
    )

    search_type = get_type_for_entity(entity, "search", parents_or_children)
    return {"criteria": criteria, **search_type, "operator": operator}


def _subcriteria_for_code(code, entity):
    """Creates the often used search criteria for code values. Returns a dictionary.

    Example::
        _subcriteria_for_code("username", "space")

    {
        "criteria": [
            {
                "fieldType": "ATTRIBUTE",
                "@type": "as.dto.common.search.CodeSearchCriteria",
                "fieldName": "code",
                "fieldValue": {
                    "@type": "as.dto.common.search.StringEqualToValue",
                    "value": "USERNAME"
                }
            }
        ],
        "operator": "AND",
        "@type": "as.dto.space.search.SpaceSearchCriteria"
    }
    """
    if code is not None:
        if is_permid(code):
            fieldname = "permId"
            fieldtype = "as.dto.common.search.PermIdSearchCriteria"
        else:
            fieldname = "code"
            fieldtype = "as.dto.common.search.CodeSearchCriteria"

        # search_criteria = get_search_type_for_entity(entity.lower())
        search_criteria = get_type_for_entity(entity, "search")
        search_criteria["criteria"] = [
            {
                "fieldName": fieldname,
                "fieldType": "ATTRIBUTE",
                "fieldValue": {
                    "value": code.upper(),
                    "@type": "as.dto.common.search.StringEqualToValue",
                },
                "@type": fieldtype,
            }
        ]

        search_criteria["operator"] = "AND"
        return search_criteria
    else:
        return get_type_for_entity(entity, "search")
        # return get_search_type_for_entity(entity.lower())


class Openbis:
    """Interface for communicating with openBIS.

    Note:
        * A recent version of openBIS is required (minimum 16.05.2).
        * For creation of datasets, the dataset-uploader-api ingestion plugin must be present.

    """

    token: str

    def __init__(
            self,
            url=None,
            verify_certificates=True,
            token=None,
            use_cache=True,
            allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks=False,
    ):
        """Initialize a new connection to an openBIS server.

        Examples:
            o = Openbis('https://openbis.example.com')
            o_test = Openbis('https://test_openbis.example.com:8443', verify_certificates=False)

        Args:
            url (str): https://openbis.example.com
            verify_certificates (bool): set to False when you use self-signed certificates
            token (str): a valid openBIS token. If not set, pybis will try to read a valid token from ~/.pybis
            use_cache: make openBIS to store spaces, projects, sample types, vocabulary terms and oder more-or-less static objects to optimise speed
            allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks (bool): False
        """

        self.as_v3 = "/openbis/openbis/rmi-application-server-v3.json"
        self.as_v1 = "/openbis/openbis/rmi-general-information-v1.json"
        self.reg_v1 = "/openbis/openbis/rmi-query-v1.json"
        self.dss_v3 = "/datastore_server/rmi-data-store-server-v3.json"
        self.verify_certificates = verify_certificates
        if not verify_certificates:
            urllib3.disable_warnings()

        if url is None:
            url = os.environ.get("OPENBIS_URL") or os.environ.get("OPENBIS_HOST")
            if url is None:
                raise ValueError("please provide a URL you want to connect to.")

        if not url.startswith("http"):
            url = "https://" + url

        url_obj = urlparse(url)
        if url_obj.netloc is None or url_obj.netloc == "":
            raise ValueError(
                "please provide the url in this format: https://openbis.host.ch:8443"
            )
        if url_obj.hostname is None:
            raise ValueError("hostname is missing")
        if (
                url_obj.scheme == "http"
                and not allow_http_but_do_not_use_this_in_production_and_only_within_safe_networks
        ):
            raise ValueError("always use https!")

        self.url = url_obj.geturl()
        self.port = url_obj.port
        self.hostname = url_obj.hostname
        self.download_prefix = os.path.join("data", self.hostname)
        self.use_cache = use_cache
        self.cache = {}
        self.server_information = None
        if token is not None:
            try:
                self.set_token(token)
            except ValueError:
                raise ValueError(
                    "This token is no longer valid. Please provide an valid token or use the login method."
                )
        else:
            # We try to set the saved token, during initialisation instead of errors, a message is printed
            try:
                token = self._get_saved_token()
                self.token = token
            except ValueError:
                pass

    def _get_username(self):
        if self.token:
            match = re.search(r"(?P<username>.*)-.*", self.token)
            username = match.groupdict()["username"]
            return username
        return ""

    @property
    def token(self):
        return self.__dict__.get("token")

    @token.setter
    def token(self, token: str):
        self.set_token(token)

    def __dir__(self):
        return [
            "url",
            "port",
            "hostname",
            "token",
            "login()",
            "logout()",
            "is_session_active()",
            "is_token_valid()",
            "mount()",
            "unmount()",
            "use_cache",
            "clear_cache()",
            "download_prefix",
            "get_mountpoint()",
            "get_server_information()",
            "get_dataset()",
            "get_datasets()",
            "get_dataset_type()",
            "get_dataset_types()",
            "get_datastores()",
            "gen_code()",
            "get_deletions()",
            "get_experiment()",
            "get_experiments()",
            "get_experiment_type()",
            "get_experiment_types()",
            "get_collection()",
            "get_collections()",
            "get_collection_type()",
            "get_collection_types()",
            "get_external_data_management_systems()",
            "get_external_data_management_system()",
            "get_material_type()",
            "get_material_types()",
            "get_project()",
            "get_projects()",
            "get_sample()",
            "get_object()",
            "get_samples()",
            "get_objects()",
            "get_sample_type()",
            "get_object_type()",
            "get_sample_types()",
            "get_object_types()",
            "get_property_types()",
            "get_property_type()",
            "get_personal_access_tokens()",
            "new_property_type()",
            "get_semantic_annotations()",
            "get_semantic_annotation()",
            "get_space()",
            "get_spaces()",
            "get_tags()",
            "get_tag()",
            "new_tag()",
            "get_terms()",
            "get_term()",
            "get_vocabularies()",
            "get_vocabulary()",
            "new_person()",
            "get_persons()",
            "get_person()",
            "get_groups()",
            "get_group()",
            "get_role_assignments()",
            "get_role_assignment()",
            "get_plugins()",
            "get_plugin()",
            "new_plugin()",
            "new_group()",
            "new_space()",
            "new_project()",
            "new_experiment()",
            "new_collection()",
            "new_sample()",
            "new_object()",
            "new_sample_type()",
            "new_object_type()",
            "new_dataset()",
            "new_dataset_type()",
            "new_experiment_type()",
            "new_collection_type()",
            "new_material_type()",
            "new_semantic_annotation()",
            "new_transaction()",
            "get_or_create_personal_access_token()",
            "set_token()",
        ]

    def _repr_html_(self):
        html = """
            <table border="1" class="dataframe">
            <thead>
                <tr style="text-align: right;">
                <th>attribute</th>
                <th>value</th>
                </tr>
            </thead>
            <tbody>
        """

        attrs = [
            "url",
            "port",
            "hostname",
            "verify_certificates",
            "as_v3",
            "as_v1",
            "reg_v1",
            "token",
        ]
        for attr in attrs:
            html += f"<tr> <td>{attr}</td> <td>{getattr(self, attr, '')}</td> </tr>"

        html += """
            </tbody>
            </table>
        """
        return html

    @property
    def spaces(self):
        return self.get_spaces()

    @property
    def projects(self):
        return self.get_projects()

    def gen_token_path(self, os_home=None):
        """generates a path to the token file.
        The token is usually saved in a file called
        ~/.pybis/hostname.token
        """
        if self.hostname is None:
            raise ValueError(
                "hostname needs to be set before retrieving the token path."
            )

        if os_home is None:
            home = os.path.expanduser("~")
        else:
            home = os_home
        parent_folder = os.path.join(home, ".pybis")
        path = os.path.join(parent_folder, self.hostname + ".token")
        return path

    def save_token_on_behalf(self, os_home):
        """Set the correct user, only the owner of the token should be able to access it,
        used by jupyterhub authenticator
        """
        token_path = self._save_token_to_disk(os_home)

        lastIndexOfMinus = len(self.token) - "".join(reversed(self.token)).index("-") - 1
        token_user_name = self.token[0:lastIndexOfMinus]
        if token_user_name.startswith("$pat-"):
            token_user_name = token_user_name[5:]
        from pwd import getpwnam

        token_user_name_uid = getpwnam(token_user_name).pw_uid
        token_user_name_gid = getpwnam(token_user_name).pw_gid

        os.chown(token_path, token_user_name_uid, token_user_name_gid)

        path = Path(token_path)
        token_parent_path = path.parent.absolute()
        os.chown(token_parent_path, token_user_name_uid, token_user_name_gid)

    def _save_token_to_disk(self, os_home=None):
        """saves the session token to the disk, usually here: ~/.pybis/hostname.token. When a new Openbis instance is created, it tries to read this saved token by default."""
        token_path = self.gen_token_path(os_home)
        # create the necessary directories, if they don't exist yet
        os.makedirs(os.path.dirname(token_path), exist_ok=True)
        with open(token_path, "w") as f:
            f.write(self.token)
        # prevent other users to be able to read the token
        os.chmod(token_path, 0o600)
        return token_path

    def _delete_saved_token(self, os_home=None):
        token_path = self.gen_token_path(os_home)
        if os.path.exists(token_path):
            os.unlink(token_path)

    def _get_saved_token(self):
        """Read the token from the .pybis, on the default user location"""
        token_path = self.gen_token_path()
        if not os.path.exists(token_path):
            return None
        try:
            with open(token_path) as f:
                token = f.read()
                if token == "":
                    return None
                else:
                    return token
        except FileNotFoundError:
            return None

    def _post_request(self, resource, request):
        """internal method, used to handle all post requests and serializing / deserializing
        data
        """
        return self._post_request_full_url(urljoin(self.url, resource), request)

    def _recover_session(self, full_url, request):
        """Current token seems to be expired,
        try to use other means to connect.
        """
        if is_session_token(self.token):
            for session_token in get_saved_tokens():
                pass

        else:
            for token in get_saved_pats(hostname=self.hostname):
                if self.is_token_valid(token=token):
                    return requests.post(
                        full_url, json.dumps(request), verify=self.verify_certificates
                    )

    def _post_request_full_url(self, full_url, request):
        """internal method, used to handle all post requests and serializing / deserializing
        data
        """

        if "id" not in request:
            request["id"] = "2"
        if "jsonrpc" not in request:
            request["jsonrpc"] = "2.0"
        if request["params"][0] is None:
            raise ValueError("Your session expired, please log in again")

        if DEBUG_LEVEL >= LOG_DEBUG:
            print(json.dumps(request))
        try:
            resp = requests.post(
                full_url, json.dumps(request), verify=self.verify_certificates
            )
        except requests.exceptions.SSLError as exc:
            raise requests.exceptions.SSLError(
                "Certificate validation failed. Use o=Openbis(url, verify_certificates=False) if you are using self-signed certificates."
            ) from exc
        except requests.ConnectionError as exc:
            raise requests.ConnectionError(
                "Could not connecto to the openBIS server. Please check your internet connection, the specified hostname and port."
            ) from exc
        if resp.ok:
            resp = resp.json()
            if "error" in resp:
                print(json.dumps(request))
                raise ValueError(resp["error"]["message"])
            elif "result" in resp:
                return resp["result"]
            else:
                raise ValueError("request did not return either result nor error")
        else:
            raise ValueError(
                f"general error while performing post request. {resp.status_code}:{resp.reason}")

    def logout(self):
        """Log out of openBIS. After logout, the session token is no longer valid."""
        if self.token is None:
            return

        logout_request = {
            "method": "logout",
            "params": [self.token],
        }
        resp = self._post_request(self.as_v3, logout_request)
        self.token = None
        return resp

    def login(self, username=None, password=None, save_token=False):
        """Log into openBIS.
        Expects a username and a password and updates the token (session-ID).
        The token is then used for every request.
        Clients may want to store the credentials object in a credentials store after successful login.
        Throw a ValueError with the error message if login failed.
        """

        if password is None:
            import getpass
            password = getpass.getpass()

        def is_different_login():
            return username != self._get_username()

        login_request = {
            "method": "login",
            "params": [username, password],
        }
        token = self._post_request(self.as_v3, login_request)
        if token is None or (is_different_login() and token == self.token):
            raise ValueError("login to openBIS failed")
        self.token = token
        if save_token:
            self._save_token_to_disk()
            self._password(password)
            self.username = username
        return self.token

    def _password(self, password=None, pstore={}):
        """An elegant way to store passwords which are used later
        without giving the user an easy possibility to retrieve it.
        """
        import inspect

        allowed_methods = ["mount"]

        if password is not None:
            pstore["password"] = password
        else:
            if inspect.stack()[1][3] in allowed_methods:
                return pstore.get("password")
            else:
                raise Exception(
                    f"This method can only be called from these internal methods: {allowed_methods}"
                )

    def unmount(self, mountpoint=None):
        """Unmount a given mountpoint or unmount the stored mountpoint.
        If the umount command does not work, try the pkill command.
        If still not successful, throw an error message.
        """

        if mountpoint is None and not getattr(self, "mountpoint", None):
            raise ValueError("please provide a mountpoint to unmount")

        if mountpoint is None:
            mountpoint = self.mountpoint

        full_mountpoint_path = os.path.abspath(os.path.expanduser(mountpoint))

        if not os.path.exists(full_mountpoint_path):
            return

        # mountpoint is not a mountpoint path
        if not os.path.ismount(full_mountpoint_path):
            return

        status = subprocess.call(f"umount {full_mountpoint_path}", shell=True)
        if status == 1:
            status = subprocess.call(
                f'pkill -9 sshfs && umount "{full_mountpoint_path}"', shell=True
            )

        if status == 1:
            raise OSError(
                f"could not unmount mountpoint: {full_mountpoint_path} Please try to unmount manually"
            )
        else:
            if VERBOSE:
                print(f"Successfully unmounted {full_mountpoint_path}")
            self.mountpoint = None

    def is_mounted(self, mountpoint=None):
        if mountpoint is None:
            mountpoint = getattr(self, "mountpoint", None)

        if mountpoint is None:
            return False

        return os.path.ismount(mountpoint)

    def get_mountpoint(self, search_mountpoint=False):
        """Returns the path to the active mountpoint.
        Returns None if no mountpoint is found or if the mountpoint is not mounted anymore.

        search_mountpoint=True:  Tries to figure out an existing mountpoint for a given hostname
                                 (experimental, does not work under Windows yet)
        """

        mountpoint = getattr(self, "mountpoint", None)
        if mountpoint:
            if self.is_mounted(mountpoint):
                return mountpoint
            else:
                return None
        else:
            if not search_mountpoint:
                return None

        # try to find out the mountpoint
        p1 = subprocess.Popen(["mount", "-d"], stdout=subprocess.PIPE)
        p2 = subprocess.Popen(
            ["grep", "--fixed-strings", self.hostname],
            stdin=p1.stdout,
            stdout=subprocess.PIPE,
        )
        p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
        output = p2.communicate()[0]
        output = output.decode()
        # output will either be '' (=not mounted) or a string like this:
        # {username}@{hostname}:{path} on {mountpoint} (osxfuse, nodev, nosuid, synchronous, mounted by vermeul)
        try:
            mountpoint = output.split()[2]
            self.mountpoint = mountpoint
            return mountpoint
        except Exception:
            return None

    def mount(
            self,
            username=None,
            password=None,
            hostname=None,
            mountpoint=None,
            volname=None,
            path="/",
            port=2222,
            kex_algorithms="+diffie-hellman-group1-sha1",
    ):
        """Mounts openBIS dataStore without being root, using sshfs and fuse. Both
        SSHFS and FUSE must be installed on the system (see below)

        Params:
        username -- default: the currently used username
        password -- default: the currently used password
        hostname -- default: the current hostname
        mountpoint -- default: ~/hostname


        FUSE / SSHFS Installation (requires root privileges):

        Mac OS X
        ========
        Follow the installation instructions on
        https://osxfuse.github.io

        Unix Cent OS 7
        ==============
        $ sudo yum install epel-release
        $ sudo yum --enablerepo=epel -y install fuse-sshfs
        $ user="$(whoami)"
        $ usermod -a -G fuse "$user"

        """
        if self.is_mounted():
            if VERBOSE:
                print(f"openBIS dataStore is already mounted on {self.mountpoint}")
            return

        def check_sshfs_is_installed():
            import errno
            import subprocess

            try:
                subprocess.call("sshfs --help", shell=True)
            except OSError as e:
                if e.errno == errno.ENOENT:
                    raise ValueError(
                        'Your system seems not to have SSHFS installed. For Mac OS X, see installation instructions on https://osxfuse.github.io For Unix: $ sudo yum install epel-release && sudo yum --enablerepo=epel -y install fuse-sshfs && user="$(whoami)" && usermod -a -G fuse "$user"'
                    )

        check_sshfs_is_installed()

        if username is None:
            username = self._get_username()
        if not username:
            raise ValueError("no token available - please provide a username")

        if password is None:
            password = self._password()
        if not password:
            raise ValueError("please provide a password")

        if hostname is None:
            hostname = self.hostname
        if not hostname:
            raise ValueError("please provide a hostname")

        if mountpoint is None:
            mountpoint = os.path.join("~", self.hostname)

        # check if mountpoint exists, otherwise create it
        full_mountpoint_path = os.path.abspath(os.path.expanduser(mountpoint))
        if not os.path.exists(full_mountpoint_path):
            os.makedirs(full_mountpoint_path)

        print("full_mountpoint_path: ", full_mountpoint_path)

        from sys import platform

        supported_platforms = ["darwin", "linux"]
        if platform not in supported_platforms:
            raise ValueError(
                f"This method is not yet supported on {platform} plattform"
            )

        os_options = {
            "darwin": f"-oauto_cache,reconnect,defer_permissions,noappledouble,negative_vncache,volname={hostname} -oStrictHostKeyChecking=no ",
            "linux": "-oauto_cache,reconnect -oStrictHostKeyChecking=no",
        }

        if volname is None:
            volname = hostname

        import subprocess

        args = {
            "username": username,
            "password": password,
            "hostname": hostname,
            "port": port,
            "path": path,
            "mountpoint": mountpoint,
            "volname": volname,
            "os_options": os_options[platform],
            "kex_algorithms": kex_algorithms,
        }

        cmd = (
            'echo "{password}" | sshfs'
            " {username}@{hostname}:{path} {mountpoint}"
            ' -o port={port} -o ssh_command="ssh -oKexAlgorithms={kex_algorithms}" -o password_stdin'
            " {os_options}".format(**args)
        )

        status = subprocess.call(cmd, shell=True)

        if status == 0:
            if VERBOSE:
                print(f"Mounted successfully to {full_mountpoint_path}")
            self.mountpoint = full_mountpoint_path
            return self.mountpoint
        else:
            raise OSError("mount failed, exit status: ", status)

    def get_server_information(self):
        """Returns a dict containing the following server information:
        api-version, archiving-configured, authentication-service, enabled-technologies, project-samples-enabled
        """
        if self.server_information is not None:
            return self.server_information

        request = {
            "method": "getServerInformation",
            "params": [self.token],
        }
        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            self.server_information = ServerInformation(resp)
            return self.server_information
        else:
            raise ValueError("Could not get the server information")

    def create_permId(self):
        """Have the server generate a new permId"""
        # Request just 1 permId
        request = {
            "method": "createPermIdStrings",
            "params": [self.token, 1],
        }
        resp = self._post_request(self.as_v3, request)
        if resp is not None:
            return resp[0]
        else:
            raise ValueError("Could not create permId")

    def get_datastores(self):
        """Get a list of all available datastores. Usually there is only one, but in some cases
        there might be multiple servers. If you upload a file, you need to specifiy the datastore you want
        the file uploaded to.
        """
        if hasattr(self, "datastores"):
            return self.datastores # pylint: disable=E0203

        request = {
            "method": "searchDataStores",
            "params": [
                self.token,
                {"@type": "as.dto.datastore.search.DataStoreSearchCriteria"},
                {"@type": "as.dto.datastore.fetchoptions.DataStoreFetchOptions"},
            ],
        }
        resp = self._post_request(self.as_v3, request)
        attrs = ["code", "downloadUrl", "remoteUrl"]
        if len(resp["objects"]) == 0:
            raise ValueError("No datastore found!")
        else:
            objects = resp["objects"]
            parse_jackson(objects)
            datastores = DataFrame(objects)
            self.datastores = datastores[attrs]
            return datastores[attrs]

    def gen_codes(self, entity: str, prefix: str = "", count: int = 1) -> List[str]:
        entity = entity.upper()

        entity2enum = {
            "DATASET": "DATA_SET",
            "OBJECT": "SAMPLE",
            "SAMPLE": "SAMPLE",
            "EXPERIMENT": "EXPERIMENT",
            "COLLECTION": "EXPERIMENT",
            "MATERIAL": "MATERIAL",
        }

        if entity not in entity2enum:
            raise ValueError(
                "no such entity: {}. Allowed entities are: DATA_SET, SAMPLE, EXPERIMENT, MATERIAL"
            )

        request = {
            "method": "createCodes",
            "params": [self.token, prefix, entity2enum[entity], count],
        }
        try:
            return self._post_request(self.as_v3, request)
        except Exception as e:
            raise ValueError(f"Could not generate a code(s) for {entity}: {e}")

    def gen_code(self, entity, prefix="") -> str:
        """Get the next sequence number for a Sample, Experiment, DataSet and Material. Other entities are currently not supported.
        Usage::
            gen_code('sample', 'SAM-')
            gen_code('collection', 'COL-')
            gen_code('dataset', '')
        """
        return self.gen_codes(entity=entity, prefix=prefix)[0]

    def gen_permId(self, count=1):
        """Generate a permId (or many permIds) for a dataSet"""

        request = {"method": "createPermIdStrings", "params": [self.token, count]}
        try:
            return self._post_request(self.as_v3, request)
        except Exception as exc:
            raise ValueError(f"Could not generate a code: {exc}")

    def new_person(self, userId, space=None):
        """creates an openBIS person or returns the existing person"""
        try:
            person = self.get_person(userId=userId)
            return person
        except Exception:
            return Person(self, userId=userId, space=space)

    def new_group(self, code, description=None, userIds=None):
        """creates an openBIS group or returns an existing one."""
        try:
            group = self.get_group(code=code)
            group.description = description
            return group
        except Exception:
            return Group(self, code=code, description=description, userIds=userIds)

    def get_group(self, code, only_data=False):
        """Get an openBIS AuthorizationGroup. Returns a Group object."""

        ids = [
            {
                "@type": "as.dto.authorizationgroup.id.AuthorizationGroupPermId",
                "permId": code,
            }
        ]

        fetchopts = {
            "@type": "as.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions"
        }
        for option in ["roleAssignments", "users", "registrator"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        fetchopts["users"]["space"] = get_fetchoption_for_entity("space")

        request = {
            "method": "getAuthorizationGroups",
            "params": [self.token, ids, fetchopts],
        }
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No group found!")

        for permid in resp:
            group = resp[permid]
            parse_jackson(group)

            if only_data:
                return group
            else:
                return Group(self, data=group)

    def get_role_assignments(self, start_with=None, count=None, **search_args):
        """Get the assigned roles for a given group, person or space"""
        entity = "roleAssignment"
        search_criteria = get_type_for_entity(entity, "search")
        allowed_search_attrs = ["role", "roleLevel", "user", "group", "person", "space"]

        sub_crit = []
        for attr in search_args:
            if attr in allowed_search_attrs:
                if attr == "space":
                    sub_crit.append(_subcriteria_for_code(search_args[attr], "space"))
                elif attr in ["user", "person"]:
                    userId = ""
                    if isinstance(search_args[attr], str):
                        userId = search_args[attr]
                    else:
                        userId = search_args[attr].userId

                    sub_crit.append(_subcriteria_for_userId(userId))
                elif attr == "group":
                    groupId = ""
                    if isinstance(search_args[attr], str):
                        groupId = search_args[attr]
                    else:
                        groupId = search_args[attr].code
                    sub_crit.append(
                        _subcriteria_for_permid(groupId, "authorizationGroup")
                    )
                elif attr == "role":
                    # TODO
                    raise ValueError("not yet implemented")
                elif attr == "roleLevel":
                    # TODO
                    raise ValueError("not yet implemented")
                else:
                    pass
            else:
                raise ValueError(f"unknown search argument {attr}")

        search_criteria["criteria"] = sub_crit

        method_name = get_method_for_entity(entity, "search")
        fetchopts = get_fetchoption_for_entity(entity)
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for option in ["space", "project", "user", "authorizationGroup", "registrator"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": method_name,
            "params": [self.token, search_criteria, fetchopts],
        }

        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = ["techId", "role", "roleLevel", "user", "group", "space", "project"]
            if len(response["objects"]) == 0:
                roles = DataFrame(columns=attrs)
            else:
                objects = response["objects"]
                parse_jackson(objects)
                roles = DataFrame(objects)
                roles["techId"] = roles["id"].map(extract_id)
                roles["user"] = roles["user"].map(extract_userId)
                roles["group"] = roles["authorizationGroup"].map(extract_code)
                roles["space"] = roles["space"].map(extract_code)
                roles["project"] = roles["project"].map(extract_code)
            return roles[roles.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="role_assignment",
            identifier_name="techId",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_role_assignment(self, techId, only_data=False):
        """Fetches one assigned role by its techId."""

        fetchopts = get_fetchoption_for_entity("roleAssignment")
        for option in ["space", "project", "user", "authorizationGroup", "registrator"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "getRoleAssignments",
            "params": [
                self.token,
                [
                    {
                        "techId": str(techId),
                        "@type": "as.dto.roleassignment.id.RoleAssignmentTechId",
                    }
                ],
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError(f"No assigned role found for techId={techId}")

        for permid in resp:
            data = resp[permid]
            parse_jackson(data)

            if only_data:
                return data
            else:
                return RoleAssignment(self, data=data)

    def assign_role(self, role, **args):
        """general method to assign a role to either
            - a person
            - a group
        The scope is either
            - the whole instance
            - a space
            - a project
        """
        role = role.upper()
        defs = get_definition_for_entity("roleAssignment")
        if role not in defs["role"]:
            raise ValueError(f"Role should be one of these: {defs['role']}")
        userId = None
        groupId = None
        spaceId = None
        projectId = None

        for arg in args:
            if arg in ["person", "group", "space", "project"]:
                permId = args[arg] if isinstance(args[arg], str) else args[arg].permId
                if arg == "person":
                    userId = {
                        "permId": permId,
                        "@type": "as.dto.person.id.PersonPermId",
                    }
                elif arg == "group":
                    groupId = {
                        "permId": permId,
                        "@type": "as.dto.authorizationgroup.id.AuthorizationGroupPermId",
                    }
                elif arg == "space":
                    spaceId = {"permId": permId, "@type": "as.dto.space.id.SpacePermId"}
                elif arg == "project":
                    projectId = {
                        "permId": permId,
                        "@type": "as.dto.project.id.ProjectPermId",
                    }

        request = {
            "method": "createRoleAssignments",
            "params": [
                self.token,
                [
                    {
                        "role": role,
                        "userId": userId,
                        "authorizationGroupId": groupId,
                        "spaceId": spaceId,
                        "projectId": projectId,
                        "@type": "as.dto.roleassignment.create.RoleAssignmentCreation",
                    }
                ],
            ],
        }
        self._post_request(self.as_v3, request)
        return

    def get_groups(self, start_with=None, count=None, **search_args):
        """Get openBIS AuthorizationGroups. Returns a «Things» object.

        Usage:
            groups = e.get.groups()
            groups[0]             # select first group
            groups['GROUP_NAME']  # select group with this code
            for group in groups:
                ...               # a Group object
            groups.df             # get a DataFrame object of the group list
            print(groups)         # print a nice ASCII table (eg. in IPython)
            groups                # HTML table (in a Jupyter notebook)

        """

        criteria = []
        for search_arg in ["code"]:
            # unfortunately, there aren't many search possibilities yet...
            if search_arg in search_args:
                if search_arg == "code":
                    criteria.append(_criteria_for_code(search_args[search_arg]))

        search_criteria = get_search_type_for_entity("authorizationGroup")
        search_criteria["criteria"] = criteria
        search_criteria["operator"] = "AND"

        fetchopts = get_fetchoption_for_entity("authorizationGroup")
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for option in ["roleAssignments", "registrator", "users"]:
            fetchopts[option] = get_fetchoption_for_entity(option)
        request = {
            "method": "searchAuthorizationGroups",
            "params": [self.token, search_criteria, fetchopts],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = [
                "permId",
                "code",
                "description",
                "users",
                "registrator",
                "registrationDate",
                "modificationDate",
            ]
            if len(response["objects"]) == 0:
                groups = DataFrame(columns=attrs)
            else:
                objects = response["objects"]
                parse_jackson(objects)
                groups = DataFrame(objects)

                groups["permId"] = groups["permId"].map(extract_permid)
                groups["registrator"] = groups["registrator"].map(extract_person)
                groups["users"] = groups["users"].map(extract_userId)
                groups["registrationDate"] = groups["registrationDate"].map(
                    format_timestamp
                )
                groups["modificationDate"] = groups["modificationDate"].map(
                    format_timestamp
                )
            return groups[groups.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="group",
            identifier_name="permId",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_or_create_personal_access_token(
            self,
            sessionName: str,
            validFrom: datetime = datetime.now(),
            validTo: datetime = None,
            force=False,
    ) -> str:
        """Creates a new personal access token (PAT).  If a PAT with the given sessionName
        already exists and its expiry date (validToDate) is not within the warning period,
        the existing PAT is returned instead.

        Args:

            sessionName (str):    a session name (mandatory)
            validFrom (datetime): begin of the validity period (default: now)
            validTo (datetime):   end of the validity period (default: validFrom + maximum validity period, as configured in openBIS)
            force (bool):         if set to True, a new PAT is created, regardless of existing ones.
        """

        server_info = self.get_server_information()
        session_token = self.token
        if not is_session_token(session_token):
            session_token = None
        if not session_token:
            session_token = get_token_for_hostname(
                self.hostname, session_token_needed=True
            )

        if not self.is_token_valid(session_token):
            raise ValueError(
                "You you need a session token to create a new personal access token."
            )

        for existing_pat in self.get_personal_access_tokens(sessionName=sessionName):
            # check if we already reached the warning period
            validTo_date = datetime.strptime(
                existing_pat.validToDate, "%Y-%m-%d %H:%M:%S"
            )
            user = self._get_username()
            if validTo_date > (
                    datetime.now()
                    + relativedelta(seconds=server_info.personal_access_tokens_validity_warning_period)
            ) and user == existing_pat.owner:
                # return existing PAT which is within warning period
                if not force:
                    return existing_pat

        if validTo is None:
            validTo = datetime.now() + relativedelta(
                seconds=server_info.personal_access_tokens_max_validity_period
            )

        entity = "personalAccessToken"
        request = {
            "method": get_method_for_entity(entity, "create"),
            "params": [
                self.token,
                {
                    "@type": "as.dto.pat.create.PersonalAccessTokenCreation",
                    "sessionName": sessionName,
                    "validFromDate": int(validFrom.timestamp() * 1000),
                    "validToDate": int(validTo.timestamp() * 1000),
                },
            ],
        }
        try:
            resp = self._post_request(self.as_v3, request)
        except ValueError as exc:
            raise NotImplementedError(
                "Your openBIS instance does not support personal access tokens. Please upgrade your server and activate them."
            )
        try:
            return self.get_personal_access_token(resp[0]["permId"])
        except KeyError:
            pass

    def get_personal_access_tokens(
            self,
            sessionName=None,
            start_with=None,
            count=None,
            save_to_disk=False,
            **search_args,
    ):
        """Get a list of Personal Access Tokens (PAT).

        Args:

            sessionName (str)  :  a session name
            save_to_disk (bool):  saves the PATs to the disk, in ~/.pybis
        """
        entity = "personalAccessToken"

        search_criteria = get_search_criteria(entity, **search_args)
        if sessionName:
            sub_crit = {
                "fieldName": "sessionName",
                "fieldType": "ATTRIBUTE",
                "fieldValue": {
                    "value": sessionName,
                    "@type": "as.dto.common.search.StringStartsWithValue",
                },
                "@type": "as.dto.pat.search.PersonalAccessTokenSessionNameSearchCriteria",
            }

            search_criteria["criteria"].append(sub_crit)
        fetchopts = get_fetchoption_for_entity(entity)
        fetchopts["from"] = start_with
        fetchopts["count"] = count

        for person in ["owner", "registrator", "modifier"]:
            fetchopts[person] = get_fetchoption_for_entity(person)
        request = {
            "method": get_method_for_entity(entity, "search"),
            "params": [self.token, search_criteria, fetchopts],
        }
        try:
            resp = self._post_request(self.as_v3, request)
        except ValueError:
            raise NotImplementedError(
                "This method is not supported by your openBIS instance."
            )

        defs = get_definition_for_entity(entity)

        def create_data_frame(attrs, props, response):
            attrs = defs["attrs"]
            objects = response["objects"]
            if len(objects) == 0:
                pats = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)

                pats = DataFrame(objects)
                pats["permId"] = pats["permId"].map(extract_permid)
                for date in [
                    "validFromDate",
                    "validToDate",
                    "accessDate",
                    "registrationDate",
                    "modificationDate",
                ]:
                    pats[date] = pats[date].map(format_timestamp)
                for person in ["owner", "registrator", "modifier"]:
                    pats[person] = pats[person].map(extract_person)
            return pats[pats.columns.intersection(attrs)]

        if save_to_disk:
            save_pats_to_disk(hostname=self.hostname, url=self.url, resp=resp)

        return Things(
            openbis_obj=self,
            entity=entity,
            identifier_name="permId",
            single_item_method=self.get_personal_access_token,
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_personal_access_token(self, permId, only_data=False):
        """Get a single Personal Access Token (PAT) by its permId.
        If you want to get the latest PAT for a given sessionName or create a new one,
        please use the get_or_create_personal_access_token() method instead.

        Args:

            permId (str)  :  The id of the PAT
        """
        entity = "personalAccessToken"
        identifiers = []
        only_one = True
        if isinstance(permId, list):
            only_one = False
            for ident in permId:
                identifiers.append(_type_for_id(ident, entity))
        else:
            identifiers.append(_type_for_id(permId, entity))

        defs = get_definition_for_entity(entity)
        fetchopts = get_fetchoption_for_entity(entity)
        for person in ["owner", "registrator", "modifier"]:
            fetchopts[person] = get_fetchoption_for_entity(person)
        request = {
            "method": get_method_for_entity(entity, "get"),
            "params": [self.token, identifiers, fetchopts],
        }
        resp = self._post_request(self.as_v3, request)
        if only_one:
            if len(resp) == 0:
                raise ValueError(f"no such {entity} found: {permId}")

            parse_jackson(resp)
            for permId in resp:
                if only_data:
                    return resp[permId]
                else:
                    return PersonalAccessToken(
                        openbis_obj=self,
                        data=resp[permId],
                    )

    def get_persons(self, start_with=None, count=None, **search_args):
        """Get openBIS users"""

        search_criteria = get_search_criteria("person", **search_args)
        fetchopts = get_fetchoption_for_entity("person")
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for option in ["space"]:
            fetchopts[option] = get_fetchoption_for_entity(option)
        request = {
            "method": "searchPersons",
            "params": [self.token, search_criteria, fetchopts],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = [
                "permId",
                "userId",
                "firstName",
                "lastName",
                "email",
                "space",
                "registrationDate",
                "active",
            ]
            objects = response["objects"]
            if len(objects) == 0:
                persons = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)

                persons = DataFrame(objects)
                persons["permId"] = persons["permId"].map(extract_permid)
                persons["registrationDate"] = persons["registrationDate"].map(
                    format_timestamp
                )
                persons["space"] = persons["space"].map(extract_nested_permid)
            return persons[persons.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="person",
            identifier_name="permId",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    get_users = get_persons  # Alias

    def get_person(self, userId, only_data=False):
        """Get a person (user)"""

        ids = [{"@type": "as.dto.person.id.PersonPermId", "permId": userId}]

        fetchopts = {"@type": "as.dto.person.fetchoptions.PersonFetchOptions"}
        for option in ["roleAssignments", "space"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "getPersons",
            "params": [
                self.token,
                ids,
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No person found!")

        for permid in resp:
            person = resp[permid]
            parse_jackson(person)

            if only_data:
                return person
            else:
                return Person(self, data=person)

    get_user = get_person  # Alias

    def get_spaces(self, code=None, start_with=None, count=None, use_cache=True):
        """Get a list of all available spaces (DataFrame object). To create a sample or a
        dataset, you need to specify in which space it should live.
        """

        method = get_method_for_entity("space", "search")
        search_criteria = _subcriteria_for_code(code, "space")
        fetchopts = get_fetchoption_for_entity("space")
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        fetchopts["registrator"] = get_fetchoption_for_entity("registrator")
        request = {
            "method": method,
            "params": [
                self.token,
                search_criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)

        def create_data_frame(attrs, props, response):
            attrs = [
                "code",
                "description",
                "registrationDate",
                "registrator",
                "modificationDate",
                "frozen",
                "frozenForProjects",
                "frozenForSamples",
            ]
            if len(resp["objects"]) == 0:
                spaces = DataFrame(columns=attrs)
            else:
                spaces = DataFrame(resp["objects"])
                spaces["registrationDate"] = spaces["registrationDate"].map(
                    format_timestamp
                )
                spaces["modificationDate"] = spaces["modificationDate"].map(
                    format_timestamp
                )
                spaces["registrator"] = spaces["registrator"].map(extract_userId)
            return spaces[spaces.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="space",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_space(self, code, only_data=False, use_cache=True):
        """Returns a Space object for a given identifier."""

        code = str(code).upper()
        space = (
                not only_data
                and use_cache
                and self._object_cache(entity="space", code=code)
        )
        if space:
            return space

        fetchopts = {"@type": "as.dto.space.fetchoptions.SpaceFetchOptions"}
        for option in ["registrator"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        method = get_method_for_entity("space", "get")

        request = {
            "method": method,
            "params": [
                self.token,
                [{"permId": code, "@type": "as.dto.space.id.SpacePermId"}],
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError("No such space: %s" % code)

        for permid in resp:
            if only_data:
                return resp[permid]
            else:
                space = Space(self, data=resp[permid])
                if self.use_cache:
                    self._object_cache(entity="space", code=code, value=space)
                return space

    def get_samples(
            self,
            identifier=None,
            code=None,
            permId=None,
            space=None,
            project=None,
            experiment=None,
            collection=None,
            type=None,
            start_with=None,
            count=None,
            withParents=None,
            withChildren=None,
            tags=None,
            attrs=None,
            props=None,
            where=None,
            raw_response=False,
            **properties,
    ):
        """Returns a DataFrame of all samples for a given space/project/experiment (or any combination).
        The default result contains only basic attributes, i.e identifier, permId, type, registrator,
        registrationDate, modifier, modificationDate. Additional attributes may be downloaded by specifying
        'attrs' list.

        Filters
        -------
        type         -- sampleType code or object
        space        -- space code or object
        project      -- project code or object
        experiment   -- experiment code or object (can be a list, too)
        collection   -- same as above
        tags         -- only return samples with the specified tags
        where        -- key-value pairs of property values to search for

        Paging
        ------
        start_with   -- default=None
        count        -- number of samples that should be fetched. default=None.

        Include in result list
        ----------------------
        withParents  -- the list of parent's permIds in a column 'parents'
        withChildren -- the list of children's permIds in a column 'children'
        attrs        -- list of all desired attributes. Examples:
                        space, project, experiment, container: returns identifier
                        parents, children, components: return a list of identifiers
                        space.code, project.code, experiment.code
                        registrator.email, registrator.firstName
                        type.generatedCodePrefix
        props        -- list of all desired properties. Returns an empty string if
                        a) property is not present
                        b) property is not defined for this sampleType
        """

        if collection is not None:
            experiment = collection
        if attrs is None:
            attrs = []

        sub_criteria = []

        if identifier:
            crit = _subcriteria_for(identifier, "sample")
            sub_criteria += crit["criteria"]

        if space:
            sub_criteria.append(_subcriteria_for(space, "space"))
        if project:
            sub_criteria.append(_subcriteria_for(project, "project"))
        if experiment:
            sub_criteria.append(_subcriteria_for(experiment, "experiment"))

        if withParents:
            sub_criteria.append(_subcriteria_for(withParents, "sample", "Parents"))
        if withChildren:
            sub_criteria.append(_subcriteria_for(withChildren, "sample", "Children"))

        if where:
            if properties is None:
                properties = where
            else:
                properties = {**where, **properties}

        if properties is not None:
            for prop in properties:
                sub_criteria.append(
                    _subcriteria_for_properties(prop, properties[prop], entity="sample")
                )
        if type:
            sub_criteria.append(_subcriteria_for_code(type, "sampleType"))
        if tags:
            sub_criteria.append(_subcriteria_for_tags(tags))
        if code:
            sub_criteria.append(
                _subcriteria_for_code_new(code, "sample", operator="OR")
            )
        if permId:
            sub_criteria.append(
                _subcriteria_for_permid_new(permId, "sample", operator="OR")
            )

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.sample.search.SampleSearchCriteria",
            "operator": "AND",
            "relation": "SAMPLE",
        }

        # build the various fetch options
        fetchopts = get_fetchoption_for_entity("sample")
        fetchopts["from"] = start_with
        fetchopts["count"] = count

        options = [
            "tags",
            "properties",
            "attachments",
            "space",
            "experiment",
            "registrator",
            "modifier",
        ]

        if self.get_server_information().project_samples_enabled:
            options.append("project")
        for option in options:
            fetchopts[option] = get_fetchoption_for_entity(option)
        for relation in ["parents", "children", "components", "container"]:
            if relation in attrs:
                fetchopts[relation] = get_fetchoption_for_entity("sample")

        if props is not None:
            fetchopts["properties"] = get_fetchoption_for_entity("properties")

        if "dataSets" in attrs:
            fetchopts["dataSets"] = get_fetchoptions("dataSets")

        request = {
            "method": "searchSamples",
            "params": [
                self.token,
                criteria,
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)

        parse_jackson(resp)
        if raw_response:
            return resp

        response = resp["objects"]

        result = self._sample_list_for_response(
            response=response,
            attrs=attrs,
            props=props,
            start_with=start_with,
            count=count,
            totalCount=resp["totalCount"],
            parsed=True,
        )

        return result

    get_objects = get_samples  # Alias

    def _get_fetchopts_for_attrs(self, attrs=None):
        if attrs is None:
            return []

        fetchopts = []
        for attr in attrs:
            if attr.startswith("space"):
                fetchopts.append("space")
            if attr.startswith("project"):
                fetchopts.append("project")
            if attr.startswith("experiment"):
                fetchopts.append("experiment")
            if attr.startswith("sample"):
                fetchopts.append("sample")
            if attr.startswith("registrator"):
                fetchopts.append("registrator")
            if attr.startswith("modifier"):
                fetchopts.append("modifier")

        return fetchopts

    def get_experiments(
            self,
            code=None,
            permId=None,
            type=None,
            space=None,
            project=None,
            start_with=None,
            count=None,
            tags=None,
            is_finished=None,
            attrs=None,
            props=None,
            where=None,
            **properties,
    ):
        """Returns a DataFrame of all samples for a given space/project (or any combination).
        The default result contains only basic attributes, i.e identifier, permId, type, registrator,
        registrationDate, modifier, modificationDate. Additional attributes may be downloaded by specifying
        'attrs' list.

        Filters:
        --------
        space        -- a space code or a space object
        project      -- a project code or a project object
        tags         -- only experiments with the specified tags
        type         -- a experimentType code
        where        -- key-value pairs of property values to search for

        Paging:
        -------
        start_with   -- default=None
        count        -- number of samples that should be fetched. default=None.

        Include:
        --------
        attrs        -- list of all desired attributes. Examples:
                        space, project, experiment: just return their identifier
                        space.code, project.code, experiment.code
                        registrator.email, registrator.firstName
                        type.generatedCodePrefix
        props        -- list of all desired properties. Returns an empty string if
                        a) property is not present
                        b) property is not defined for this sampleType
        """

        def extract_attribute(attribute_to_extract):
            def return_attribute(obj):
                if obj is None:
                    return ""
                return obj.get(attribute_to_extract, "")

            return return_attribute

        def extract_space(obj):
            if isinstance(obj, dict):
                return obj.get("space", {})
            else:
                return ""

        sub_criteria = []
        if space:
            sub_criteria.append(
                _subcriteria_for(space, "project.space", operator="AND")
            )
        if project:
            sub_criteria.append(_subcriteria_for(project, "project", operator="AND"))
        if code:
            sub_criteria.append(_criteria_for_code(code))
        if permId:
            sub_criteria.append(_criteria_for_permId(permId))
        if type:
            sub_criteria.append(_subcriteria_for_code(type, "experimentType"))
        if tags:
            sub_criteria.append(_subcriteria_for_tags(tags))
        if is_finished is not None:
            sub_criteria.append(_subcriteria_for_is_finished(is_finished))
        if where:
            if properties is None:
                properties = where
            else:
                properties = {**where, **properties}
        if properties is not None:
            sub_criteria.extend(
                list(
                    map(
                        lambda prop: _subcriteria_for_properties(
                            prop, properties[prop], entity="experiment"
                        ),
                        properties,
                    )
                )
            )

        search_criteria = get_search_type_for_entity("experiment")
        search_criteria["criteria"] = sub_criteria
        search_criteria["operator"] = "AND"

        fetchopts = get_fetchoption_for_entity("experiment")
        fetchopts["from"] = start_with
        fetchopts["count"] = count

        if attrs is None:
            attrs = []
        options = self._get_fetchopts_for_attrs(attrs)
        for option in ["tags", "properties", "registrator", "modifier"] + options:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "searchExperiments",
            "params": [
                self.token,
                search_criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            response = response["objects"]
            parse_jackson(response)

            default_attrs = [
                "identifier",
                "permId",
                "type",
                "registrator",
                "registrationDate",
                "modifier",
                "modificationDate",
            ]

            display_attrs = default_attrs + attrs

            if props is None:
                props = []
            else:
                if isinstance(props, str):
                    props = [props]

            if len(response) == 0:
                for prop in props:
                    if prop == "*":
                        continue
                    display_attrs.append(prop)
                experiments = DataFrame(columns=display_attrs)
            else:
                experiments = DataFrame(response)
                experiments["space"] = experiments["project"].map(extract_space)
                for attr in attrs:
                    if "." in attr:
                        entity, attribute_to_extract = attr.split(".")
                        experiments[attr] = experiments[entity].map(
                            extract_attribute(attribute_to_extract)
                        )
                for attr in attrs:
                    # if no dot supplied, just display the code of the space, project or experiment
                    if attr in ["project"]:
                        experiments[attr] = experiments[attr].map(
                            extract_nested_identifier
                        )
                    if attr in ["space"]:
                        experiments[attr] = experiments[attr].map(extract_code)

                experiments["registrationDate"] = experiments["registrationDate"].map(
                    format_timestamp
                )
                experiments["modificationDate"] = experiments["modificationDate"].map(
                    format_timestamp
                )
                experiments["project"] = experiments["project"].map(extract_code)
                experiments["registrator"] = experiments["registrator"].map(
                    extract_person
                )
                experiments["modifier"] = experiments["modifier"].map(extract_person)
                experiments["identifier"] = experiments["identifier"].map(
                    extract_identifier
                )
                experiments["permId"] = experiments["permId"].map(extract_permid)
                experiments["type"] = experiments["type"].map(extract_code)

                for prop in props:
                    if prop == "*":
                        # include all properties in dataFrame.
                        # expand the dataFrame by adding new columns
                        columns = []
                        for i, experiment in enumerate(response):
                            for prop_name, val in experiment.get(
                                    "properties", {}
                            ).items():
                                experiments.loc[i, prop_name.upper()] = val
                                columns.append(prop_name.upper())

                        display_attrs += set(columns)
                        continue
                    else:
                        # property name is provided
                        for i, experiment in enumerate(response):
                            val = experiment.get("properties", {}).get(
                                prop, ""
                            ) or experiment.get("properties", {}).get(prop.upper(), "")
                            experiments.loc[i, prop.upper()] = val
                        display_attrs.append(prop.upper())
            return experiments[experiments.columns.intersection(display_attrs)]

        return Things(
            openbis_obj=self,
            entity="experiment",
            identifier_name="identifier",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            attrs=attrs,
            props=props,
            response=resp,
            df_initializer=create_data_frame,
        )

    get_collections = get_experiments  # Alias

    def get_datasets(
            self,
            permId=None,
            code=None,
            type=None,
            withParents=None,
            withChildren=None,
            start_with=None,
            count=None,
            kind=None,
            status=None,
            sample=None,
            experiment=None,
            collection=None,
            project=None,
            space=None,
            tags=None,
            attrs=None,
            props=None,
            where=None,
            **properties,
    ):
        """Returns a DataFrame of all dataSets for a given project/experiment/sample (or any combination).
        The default result contains only basic attributes, i.e permId, type, experiment, sample, registrationDate,
        modificationDate, location, status, presentInArchive, size.
        Additional attributes may be downloaded by specifying 'attrs' list.

        Filters
        -------
        permId       -- the permId is the unique identifier of a dataSet. A list of permIds can be provided.
        code         -- actually a synonym for the permId of the dataSet.
        project      -- a project code or a project object
        experiment   -- an experiment code or an experiment object
        sample       -- a sample code/permId or a sample/object
        collection   -- same as experiment
        tags         -- only return dataSets with the specified tags
        type         -- a dataSetType code
        where        -- key-value pairs of property values to search for

        Paging
        ------
        start_with   -- default=None
        count        -- number of dataSets that should be fetched. default=None.

        Include in result list
        ----------------------
        withParents  -- the list of parent's permIds in a column 'parents'
        withChildren -- the list of children's permIds in a column 'children'
        attrs        -- list of all desired attributes. Examples:
                        project, experiment, sample: returns identifier
                        parents, children, components, containers: return a list of identifiers
                        space.code, project.code, experiment.code
                        registrator.email, registrator.firstName
                        type.generatedCodePrefix
        props        -- list of all desired properties. Returns an empty string if
                        a) property is not present
                        b) property is not defined for this dataSetType
        """

        if "object" in properties:
            sample = properties["object"]
        if collection is not None:
            experiment = collection

        sub_criteria = []

        if code or permId:
            if code is None:
                code = permId
            sub_criteria.append(
                _subcriteria_for_code_new(code, "dataSet", operator="OR")
            )
        if type:
            sub_criteria.append(_subcriteria_for_code(type, "dataSetType"))

        if withParents:
            sub_criteria.append(_subcriteria_for(withParents, "dataSet", "Parents"))
        if withChildren:
            sub_criteria.append(_subcriteria_for(withChildren, "dataSet", "Children"))

        if sample:
            sub_criteria.append(_subcriteria_for(sample, "sample"))
        if experiment:
            sub_criteria.append(_subcriteria_for(experiment, "experiment"))
        if attrs is None:
            attrs = []
        if project:
            sub_criteria.append(_subcriteria_for(project, "experiment.project"))
        if space:
            sub_criteria.append(_subcriteria_for(space, "experiment.project.space"))
        if tags:
            sub_criteria.append(_subcriteria_for_tags(tags))
        if status:
            sub_criteria.append(_subcriteria_for_status(status))

        if where:
            if properties is None:
                properties = where
            else:
                properties = {**where, **properties}

        if properties is not None:
            sub_criteria.extend(
                list(
                    map(
                        lambda prop: _subcriteria_for_properties(
                            prop, properties[prop], entity="dataset"
                        ),
                        properties,
                    )
                )
            )

        search_criteria = get_search_type_for_entity("dataset")
        search_criteria["criteria"] = sub_criteria
        search_criteria["operator"] = "AND"

        fetchopts = get_fetchoptions("dataSet", including=["type"])
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for relation in ["parents", "children", "components", "containers"]:
            if relation in attrs:
                fetchopts[relation] = get_fetchoption_for_entity("dataSet")

        for option in [
            "tags",
            "properties",
            "dataStore",
            "physicalData",
            "linkedData",
            "experiment",
            "sample",
            "registrator",
            "modifier",
        ]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        fetchopts["experiment"]["project"] = get_fetchoption_for_entity("project")

        if kind:
            kind = kind.upper()
            if kind not in ["PHYSICAL", "CONTAINER", "LINK"]:
                raise ValueError(
                    f"unknown dataSet kind: {kind}. It should be one of the following: PHYSICAL, CONTAINER or LINK"
                )
            fetchopts["kind"] = kind
            raise NotImplementedError("you cannot search for dataSet kinds yet")
        request = {
            "method": "searchDataSets",
            "params": [
                self.token,
                search_criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)

        parse_jackson(resp)
        datasets = []
        for obj in resp["objects"]:
            dataset = DataSet(
                openbis_obj=self,
                type=self.get_dataset_type(obj["type"]["code"]),
                data=obj,
            )
            datasets.append(dataset)

        return self._dataset_list_for_response(
            response=resp["objects"],
            attrs=attrs,
            props=props,
            start_with=start_with,
            count=count,
            totalCount=resp["totalCount"],
            objects=datasets,
            parsed=True,
        )

    def get_experiment(
            self, code, withAttachments=False, only_data=False, use_cache=True
    ):
        """Returns an experiment object for a given identifier (code)."""

        experiment = (
                not only_data
                and use_cache
                and self._object_cache(entity="experiment", code=code)
        )
        if experiment:
            return experiment

        fetchopts = get_fetchoption_for_entity("experiment")

        search_request = _type_for_id(code, "experiment")
        for option in [
            "tags",
            "properties",
            "attachments",
            "project",
            "samples",
            "registrator",
            "modifier",
        ]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        if withAttachments:
            fetchopts["attachments"] = get_fetchoption_for_entity(
                "attachmentsWithContent"
            )

        request = {
            "method": "getExperiments",
            "params": [self.token, [search_request], fetchopts],
        }
        resp = self._post_request(self.as_v3, request)
        if len(resp) == 0:
            raise ValueError(f"No such experiment: {code}")

        parse_jackson(resp)
        data = resp[code]
        if only_data:
            return data

        experiment = Experiment(
            openbis_obj=self,
            type=self.get_experiment_type(data["type"]["code"]),
            data=data,
        )
        if self.use_cache:
            identifier = data["identifier"]["identifier"]
            self._object_cache(entity="experiment", code=identifier, value=experiment)
        return experiment

    get_collection = get_experiment  # Alias

    def new_experiment(self, type, code, project, props=None, **kwargs):
        """Creates a new experiment of a given experiment type."""
        return Experiment(
            openbis_obj=self,
            type=self.get_experiment_type(type),
            project=project,
            data=None,
            props=props,
            code=code,
            **kwargs,
        )

    new_collection = new_experiment  # Alias

    def create_external_data_management_system(
            self, code, label, address, address_type="FILE_SYSTEM"
    ):
        """Create an external DMS.
        :param code: An openBIS code for the external DMS.
        :param label: A human-readable label.
        :param address: The address for accessing the external DMS. E.g., a URL.
        :param address_type: One of OPENBIS, URL, or FILE_SYSTEM
        :return:
        """
        request = {
            "method": "createExternalDataManagementSystems",
            "params": [
                self.token,
                [
                    {
                        "code": code,
                        "label": label,
                        "addressType": address_type,
                        "address": address,
                        "@type": "as.dto.externaldms.create.ExternalDmsCreation",
                    }
                ],
            ],
        }
        resp = self._post_request(self.as_v3, request)
        return self.get_external_data_management_system(resp[0]["permId"])

    def delete_entity(self, entity, id, reason, id_name="permId"):
        """Deletes Spaces, Projects, Experiments, Samples and DataSets"""

        type = get_type_for_entity(entity, "delete")
        method = get_method_for_entity(entity, "delete")
        request = {
            "method": method,
            "params": [
                self.token,
                [{id_name: id, "@type": type}],
                {"reason": reason, "@type": type},
            ],
        }
        self._post_request(self.as_v3, request)

    def delete_openbis_entity(self, entity, objectId, reason="No reason given"):
        method = get_method_for_entity(entity, "delete")
        delete_options = get_type_for_entity(entity, "delete")
        delete_options["reason"] = reason

        request = {"method": method, "params": [self.token, [objectId], delete_options]}
        return self._post_request(self.as_v3, request)

    def confirm_deletions(self, deletion_ids):
        request = {
            "method": "confirmDeletions",
            "params": [
                self.token,
                deletion_ids,
            ],
        }
        self._post_request(self.as_v3, request)

    def get_deletions(self, start_with=None, count=None):
        search_criteria = {"@type": "as.dto.deletion.search.DeletionSearchCriteria"}
        fetchopts = get_fetchoption_for_entity("deletion")
        fetchoptsDeleted = get_fetchoption_for_entity("deletedObjects")
        fetchoptsDeleted["from"] = start_with
        fetchoptsDeleted["count"] = count
        fetchopts["deletedObjects"] = fetchoptsDeleted

        request = {
            "method": "searchDeletions",
            "params": [
                self.token,
                search_criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)
        objects = resp["objects"]
        parse_jackson(objects)

        new_objs = []
        for value in objects:
            del_objs = extract_deletion(value)
            if len(del_objs) > 0:
                new_objs.append(*del_objs)

        return DataFrame(new_objs)

    def new_project(self, space, code, description=None, **kwargs):
        return Project(
            self, None, space=space, code=code, description=description, **kwargs
        )

    def _gen_fetchoptions(self, options, foType):
        fo = {"@type": foType}
        for option in options:
            fo[option] = get_fetchoption_for_entity(option)
        return fo

    def get_project(self, projectId, only_data=False, use_cache=True):
        """Returns a Project object for a given identifier, code or permId."""

        project = (
                not only_data
                and use_cache
                and self._object_cache(entity="project", code=projectId)
        )
        if project:
            return project

        options = ["space", "registrator", "modifier", "attachments"]
        if is_identifier(projectId) or is_permid(projectId):
            request = self._create_get_request(
                "getProjects",
                "project",
                projectId,
                options,
                "as.dto.project.fetchoptions.ProjectFetchOptions",
            )
            resp = self._post_request(self.as_v3, request)
            if len(resp) == 0:
                raise ValueError("No such project: %s" % projectId)
            if only_data:
                return resp[projectId]
            project = Project(openbis_obj=self, type=None, data=resp[projectId])
            if self.use_cache:
                self._object_cache(entity="project", code=projectId, value=project)
            return project

        else:
            search_criteria = _gen_search_criteria(
                {"project": "Project", "operator": "AND", "code": projectId}
            )
            fo = self._gen_fetchoptions(
                options, foType="as.dto.project.fetchoptions.ProjectFetchOptions"
            )
            request = {
                "method": "searchProjects",
                "params": [self.token, search_criteria, fo],
            }
            resp = self._post_request(self.as_v3, request)
            if len(resp["objects"]) == 0:
                raise ValueError("No such project: %s" % projectId)
            if only_data:
                return resp["objects"][0]

            project = Project(openbis_obj=self, type=None, data=resp["objects"][0])
            if self.use_cache:
                self._object_cache(entity="project", code=projectId, value=project)
            return project

    def get_projects(
            self,
            space=None,
            code=None,
            start_with=None,
            count=None,
    ):
        """Get a list of all available projects (DataFrame object)."""

        sub_criteria = []
        if space:
            sub_criteria.append(_subcriteria_for_code(space, "space"))
        if code:
            sub_criteria.append(_criteria_for_code(code))

        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.project.search.ProjectSearchCriteria",
            "operator": "AND",
        }

        fetchopts = {"@type": "as.dto.project.fetchoptions.ProjectFetchOptions"}
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for option in ["registrator", "modifier", "leader"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "searchProjects",
            "params": [
                self.token,
                criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = [
                "code",
                "identifier",
                "permId",
                "description",
                "leader",
                "registrator",
                "registrationDate",
                "modifier",
                "modificationDate",
                "frozen",
                "frozenForExperiments",
                "frozenForSamples",
            ]
            objects = response["objects"]
            if len(objects) == 0:
                projects = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)

                projects = DataFrame(objects)

                projects["registrationDate"] = projects["registrationDate"].map(
                    format_timestamp
                )
                projects["modificationDate"] = projects["modificationDate"].map(
                    format_timestamp
                )
                projects["leader"] = projects["leader"].map(extract_person)
                projects["registrator"] = projects["registrator"].map(extract_person)
                projects["modifier"] = projects["modifier"].map(extract_person)
                projects["permId"] = projects["permId"].map(extract_permid)
                projects["identifier"] = projects["identifier"].map(extract_identifier)
            return projects[projects.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="project",
            identifier_name="identifier",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def _create_get_request(self, method_name, entity, permids, options, foType):

        if not isinstance(permids, list):
            permids = [permids]

        type = f"as.dto.{entity.lower()}.id.{entity.capitalize()}"
        search_params = []
        for permid in permids:
            # decide if we got a permId or an identifier
            match = re.match("/", permid)
            if match:
                search_params.append(
                    {"identifier": permid, "@type": type + "Identifier"}
                )
            else:
                search_params.append({"permId": permid, "@type": type + "PermId"})

        fo = {"@type": foType}
        for option in options:
            fo[option] = get_fetchoption_for_entity(option)

        request = {
            "method": method_name,
            "params": [self.token, search_params, fo],
        }
        return request

    def clear_cache(self, entity=None):
        """Empty the internal object cache
        If you do not specify any entity, the complete cache is cleared.
        As entity, you can specify either:
        space, project, vocabulary, term, sampleType, experimentType, dataSetType
        """
        if entity:
            self.cache[entity] = {}
        else:
            self.cache = {}

    def _object_cache(self, entity=None, code=None, value=None):

        # return the value, if no value provided
        if value is None:
            if entity in self.cache:
                return self.cache[entity].get(code)
        else:
            if entity not in self.cache:
                self.cache[entity] = {}

            self.cache[entity][code] = value

    def get_terms(self, vocabulary=None, start_with=None, count=None, use_cache=True):
        """Returns information about existing vocabulary terms.
        If a vocabulary code is provided, it only returns the terms of that vocabulary.
        """

        if (
                use_cache
                and self.use_cache
                and vocabulary is not None
                and start_with is None
                and count is None
        ):
            voc = self._object_cache(entity="term", code=vocabulary)
            if voc:
                return voc

        search_request = {}
        if vocabulary is not None:
            search_request = _gen_search_criteria(
                {
                    "vocabulary": "VocabularyTerm",
                    "criteria": [{"vocabulary": "Vocabulary", "code": vocabulary}],
                }
            )
        search_request[
            "@type"
        ] = "as.dto.vocabulary.search.VocabularyTermSearchCriteria"

        fetchopts = get_fetchoption_for_entity("vocabularyTerm")
        fetchopts["from"] = start_with
        fetchopts["count"] = count

        request = {
            "method": "searchVocabularyTerms",
            "params": [self.token, search_request, fetchopts],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = "code vocabularyCode label description registrationDate modificationDate official ordinal".split()

            objects = response["objects"]
            if len(objects) == 0:
                terms = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)
                terms = DataFrame(objects)
                terms["vocabularyCode"] = terms["permId"].map(
                    extract_attr("vocabularyCode")
                )
                terms["registrationDate"] = terms["registrationDate"].map(
                    format_timestamp
                )
                terms["modificationDate"] = terms["modificationDate"].map(
                    format_timestamp
                )
            return terms[terms.columns.intersection(attrs)]

        things = Things(
            openbis_obj=self,
            entity="term",
            identifier_name="code",
            additional_identifier="vocabularyCode",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )
        if (
                self.use_cache
                and vocabulary is not None
                and start_with is None
                and count is None
        ):
            self._object_cache(entity="term", code=vocabulary, value=things)

        return things

    def new_term(self, code, vocabularyCode, label=None, description=None):
        return VocabularyTerm(
            self,
            data=None,
            code=code,
            vocabularyCode=vocabularyCode,
            label=label,
            description=description,
        )

    def get_term(self, code, vocabularyCode, only_data=False):
        search_request = {
            "code": code,
            "vocabularyCode": vocabularyCode,
            "@type": "as.dto.vocabulary.id.VocabularyTermPermId",
        }
        fetchopts = get_fetchoption_for_entity("vocabularyTerm")
        for opt in ["registrator"]:
            fetchopts[opt] = get_fetchoption_for_entity(opt)

        request = {
            "method": "getVocabularyTerms",
            "params": [self.token, [search_request], fetchopts],
        }
        resp = self._post_request(self.as_v3, request)

        if resp is None or len(resp) == 0:
            raise ValueError(
                f"no VocabularyTerm found with code='{code}' and vocabularyCode='{vocabularyCode}'"
            )
        else:
            parse_jackson(resp)
            for ident in resp:
                if only_data:
                    return resp[ident]
                else:
                    return VocabularyTerm(self, resp[ident])

    def get_vocabularies(self, code=None, start_with=None, count=None):
        """Returns information about vocabulary"""

        sub_criteria = []
        if code:
            sub_criteria.append(_criteria_for_code(code))
        criteria = {
            "criteria": sub_criteria,
            "@type": "as.dto.vocabulary.search.VocabularySearchCriteria",
            "operator": "AND",
        }

        fetchopts = get_fetchoption_for_entity("vocabulary")
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for option in ["registrator"]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "searchVocabularies",
            "params": [self.token, criteria, fetchopts],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = "code description managedInternally chosenFromList urlTemplate registrator registrationDate modificationDate".split()

            objects = response["objects"]
            if len(objects) == 0:
                vocs = DataFrame(columns=attrs)
            else:
                parse_jackson(response)
                vocs = DataFrame(objects)
                vocs["registrationDate"] = vocs["registrationDate"].map(
                    format_timestamp
                )
                vocs["modificationDate"] = vocs["modificationDate"].map(
                    format_timestamp
                )
                vocs["registrator"] = vocs["registrator"].map(extract_person)
            return vocs[vocs.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="vocabulary",
            identifier_name="code",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_vocabulary(self, code, only_data=False, use_cache=True):
        """Returns the details of a given vocabulary (including vocabulary terms)"""

        code = str(code).upper()
        voc = (
                not only_data
                and use_cache
                and self._object_cache(entity="vocabulary", code=code)
        )
        if voc:
            return voc

        entity = "vocabulary"
        method_name = get_method_for_entity(entity, "get")
        objectIds = _type_for_id(code.upper(), entity)
        fetchopts = get_fetchoption_for_entity(entity)

        request = {
            "method": method_name,
            "params": [self.token, [objectIds], fetchopts],
        }
        resp = self._post_request(self.as_v3, request)

        if len(resp) == 0:
            raise ValueError(f"no {entity} found with identifier: {code}")
        else:
            parse_jackson(resp)
            for ident in resp:
                data = resp[ident]
                if only_data:
                    return data
                vocabulary = Vocabulary(openbis_obj=self, data=data)
                if self.use_cache:
                    self._object_cache(entity="vocabulary", code=code, value=vocabulary)
                return vocabulary

    def new_tag(self, code, description=None):
        """Creates a new tag (for this user)"""
        return Tag(self, code=code, description=description)

    def get_tags(self, code=None, start_with=None, count=None):
        """Returns a DataFrame of all tags"""

        search_criteria = get_search_type_for_entity("tag", "AND")

        criteria = []
        fetchopts = get_fetchoption_for_entity("tag")
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        for option in ["owner"]:
            fetchopts[option] = get_fetchoption_for_entity(option)
        if code:
            criteria.append(_criteria_for_code(code))
        search_criteria["criteria"] = criteria
        request = {
            "method": "searchTags",
            "params": [self.token, search_criteria, fetchopts],
        }

        resp = self._post_request(self.as_v3, request)
        return self._tag_list_for_response(
            response=resp["objects"], totalCount=resp["totalCount"]
        )

    def get_tag(self, permId, only_data=False, use_cache=True):
        """Returns a specific tag"""

        just_one = True
        identifiers = []
        if isinstance(permId, list):
            just_one = False
            for ident in permId:
                identifiers.append(_type_for_id(ident, "tag"))
        else:
            tag = (
                    not only_data
                    and use_cache
                    and self._object_cache(entity="tag", code=permId)
            )
            if tag:
                return tag
            identifiers.append(_type_for_id(permId, "tag"))

        fetchopts = get_fetchoption_for_entity("tag")
        for option in ["owner"]:
            fetchopts[option] = get_fetchoption_for_entity(option)
        request = {
            "method": "getTags",
            "params": [self.token, identifiers, fetchopts],
        }

        resp = self._post_request(self.as_v3, request)

        if just_one:
            if len(resp) == 0:
                raise ValueError(f"no such tag found: {permId}")

            parse_jackson(resp)
            for permId in resp:
                if only_data:
                    return resp[permId]
                else:
                    tag = Tag(self, data=resp[permId])
                    if self.use_cache:
                        self._object_cache(entity="tag", code=permId, value=tag)
                    return tag
        else:
            return self._tag_list_for_response(response=list(resp.values()))

    def _tag_list_for_response(self, response, totalCount=0):
        def create_data_frame(attrs, props, response):
            parse_jackson(response)
            attrs = [
                "permId",
                "code",
                "description",
                "owner",
                "private",
                "registrationDate",
            ]
            if len(response) == 0:
                tags = DataFrame(columns=attrs)
            else:
                tags = DataFrame(response)
                tags["registrationDate"] = tags["registrationDate"].map(
                    format_timestamp
                )
                tags["permId"] = tags["permId"].map(extract_permid)
                tags["description"] = tags["description"].map(
                    lambda x: "" if x is None else x
                )
                tags["owner"] = tags["owner"].map(extract_person)
            return tags[tags.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="tag",
            identifier_name="permId",
            totalCount=totalCount,
            response=response,
            df_initializer=create_data_frame,
        )

    def search_semantic_annotations(
            self, permId=None, entityType=None, propertyType=None, only_data=False
    ):
        """Get a list of semantic annotations for permId, entityType, propertyType or
        property type assignment (DataFrame object).
        :param permId: permId of the semantic annotation.
        :param entityType: entity (sample) type to search for.
        :param propertyType: property type to search for
        :param only_data: return result as plain data object.
        :return:  Things of DataFrame objects or plain data object
        """

        criteria = []
        typeCriteria = []

        if permId is not None:
            criteria.append(
                {
                    "@type": "as.dto.common.search.PermIdSearchCriteria",
                    "fieldValue": {
                        "@type": "as.dto.common.search.StringEqualToValue",
                        "value": permId,
                    },
                }
            )

        if entityType is not None:
            typeCriteria.append(
                {
                    "@type": "as.dto.entitytype.search.EntityTypeSearchCriteria",
                    "criteria": [_criteria_for_code(entityType)],
                }
            )

        if propertyType is not None:
            typeCriteria.append(
                {
                    "@type": "as.dto.property.search.PropertyTypeSearchCriteria",
                    "criteria": [_criteria_for_code(propertyType)],
                }
            )

        if entityType is not None and propertyType is not None:
            criteria.append(
                {
                    "@type": "as.dto.property.search.PropertyAssignmentSearchCriteria",
                    "criteria": typeCriteria,
                }
            )
        else:
            criteria += typeCriteria

        saCriteria = {
            "@type": "as.dto.semanticannotation.search.SemanticAnnotationSearchCriteria",
            "criteria": criteria,
        }

        objects = self._search_semantic_annotations(saCriteria)

        if only_data:
            return objects

        def create_data_frame(attrs, props, response):
            attrs = [
                "permId",
                "entityType",
                "propertyType",
                "predicateOntologyId",
                "predicateOntologyVersion",
                "predicateAccessionId",
                "descriptorOntologyId",
                "descriptorOntologyVersion",
                "descriptorAccessionId",
                "creationDate",
            ]
            if len(response) == 0:
                annotations = DataFrame(columns=attrs)
            else:
                annotations = DataFrame(response)
            return annotations[annotations.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="semantic_annotation",
            identifier_name="permId",
            response=objects,
            df_initializer=create_data_frame,
        )

    def _search_semantic_annotations(self, criteria):

        fetch_options = {
            "@type": "as.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions",
            "entityType": {
                "@type": "as.dto.entitytype.fetchoptions.EntityTypeFetchOptions"
            },
            "propertyType": {
                "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions"
            },
            "propertyAssignment": {
                "@type": "as.dto.property.fetchoptions.PropertyAssignmentFetchOptions",
                "entityType": {
                    "@type": "as.dto.entitytype.fetchoptions.EntityTypeFetchOptions"
                },
                "propertyType": {
                    "@type": "as.dto.property.fetchoptions.PropertyTypeFetchOptions"
                },
            },
        }

        request = {
            "method": "searchSemanticAnnotations",
            "params": [self.token, criteria, fetch_options],
        }

        resp = self._post_request(self.as_v3, request)
        if len(resp["objects"]) == 0:
            return []
        else:
            objects = resp["objects"]
            parse_jackson(objects)

            for obj in objects:
                obj["permId"] = obj["permId"]["permId"]
                if obj.get("entityType") is not None:
                    obj["entityType"] = obj["entityType"]["code"]
                elif obj.get("propertyType") is not None:
                    obj["propertyType"] = obj["propertyType"]["code"]
                elif obj.get("propertyAssignment") is not None:
                    obj["entityType"] = obj["propertyAssignment"]["entityType"]["code"]
                    obj["propertyType"] = obj["propertyAssignment"]["propertyType"][
                        "code"
                    ]
                obj["creationDate"] = format_timestamp(obj["creationDate"])
            return objects

    def get_semantic_annotations(self):
        """Get a list of all available semantic annotations (DataFrame object)."""

        objects = self._search_semantic_annotations(
            {
                "@type": "as.dto.semanticannotation.search.SemanticAnnotationSearchCriteria"
            }
        )

        def create_data_frame(attrs, props, response):
            attrs = [
                "permId",
                "entityType",
                "propertyType",
                "predicateOntologyId",
                "predicateOntologyVersion",
                "predicateAccessionId",
                "descriptorOntologyId",
                "descriptorOntologyVersion",
                "descriptorAccessionId",
                "creationDate",
            ]
            if len(response) == 0:
                annotations = DataFrame(columns=attrs)
            else:
                annotations = DataFrame(response)
            return annotations[annotations.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="semantic_annotation",
            identifier_name="permId",
            response=objects,
            df_initializer=create_data_frame,
        )

    def get_semantic_annotation(self, permId, only_data=False):
        objects = self.search_semantic_annotations(permId=permId, only_data=True)
        if len(objects) == 0:
            raise ValueError(
                "Semantic annotation with permId " + permId + " not found."
            )
        obj = objects[0]
        if only_data:
            return obj
        else:
            return SemanticAnnotation(self, isNew=False, **obj)

    def get_plugins(self, start_with=None, count=None):

        criteria = []
        search_criteria = get_search_type_for_entity("plugin", "AND")
        search_criteria["criteria"] = criteria

        fetchopts = get_fetchoption_for_entity("plugin")
        for option in ["registrator"]:
            fetchopts[option] = get_fetchoption_for_entity(option)
        fetchopts["from"] = start_with
        fetchopts["count"] = count

        request = {
            "method": "searchPlugins",
            "params": [
                self.token,
                search_criteria,
                fetchopts,
            ],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            attrs = [
                "name",
                "description",
                "pluginType",
                "pluginKind",
                "entityKinds",
                "registrator",
                "registrationDate",
                "permId",
            ]

            objects = response["objects"]
            if len(objects) == 0:
                plugins = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)

                plugins = DataFrame(objects)
                plugins["permId"] = plugins["permId"].map(extract_permid)
                plugins["registrator"] = plugins["registrator"].map(extract_person)
                plugins["registrationDate"] = plugins["registrationDate"].map(
                    format_timestamp
                )
                plugins["description"] = plugins["description"].map(
                    lambda x: "" if x is None else x
                )
                plugins["entityKinds"] = plugins["entityKinds"].map(
                    lambda x: "" if x is None else x
                )
            return plugins[plugins.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="plugin",
            identifier_name="name",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_plugin(self, permId, only_data=False, with_script=True):
        search_request = _type_for_id(permId, "plugin")
        fetchopts = get_fetchoption_for_entity("plugin")
        options = ["registrator"]
        if with_script:
            options.append("script")

        for option in options:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "getPlugins",
            "params": [self.token, [search_request], fetchopts],
        }

        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)

        if resp is None or len(resp) == 0:
            raise ValueError("no such plugin found: " + permId)
        else:
            for permId in resp:
                if only_data:
                    return resp[permId]
                else:
                    return Plugin(self, data=resp[permId])

    def new_plugin(self, name, pluginType, **kwargs):
        """Creates a new Plugin in openBIS.
        name        -- name of the plugin
        description --
        pluginType  -- DYNAMIC_PROPERTY, MANAGED_PROPERTY, ENTITY_VALIDATION
        entityKind  -- MATERIAL, EXPERIMENT, SAMPLE, DATA_SET
        script      -- string of the script itself
        available   --
        """
        return Plugin(self, name=name, pluginType=pluginType, **kwargs)

    def new_property_type(
            self,
            code,
            label,
            description,
            dataType,
            managedInternally=False,
            vocabulary=None,
            materialType=None,
            schema=None,
            transformation=None,
            metaData=None,
            multiValue=False
    ):
        """Creates a new property type.

        code               -- name of the property type
        managedInternally  -- must be set to True if code starts with a $
        label              -- displayed label of that property
        description        --
        dataType           -- must contain any of these values:
                              INTEGER VARCHAR MULTILINE_VARCHAR
                              REAL TIMESTAMP BOOLEAN HYPERLINK
                              XML CONTROLLEDVOCABULARY MATERIAL
        vocabulary         -- if dataType is CONTROLLEDVOCABULARY, this attribute
                              must contain the code of the vocabulary object.
        materialType       --
        schema             --
        transformation     --
        metaData           -- used to create properties that contain either RichText or tabular, spreadsheet-like data.
                              use {'custom_widget' : 'Word Processor'} and MULTILINE_VARCHAR for RichText
                              use {'custom_widget' : 'Spreadhseet'} and XML for tabular data.
        PropertyTypes can be assigned to
        - sampleTypes
        - dataSetTypes
        - experimentTypes
        - materialTypes (deprecated)
        """

        if isinstance(vocabulary, Vocabulary):
            vocabulary = vocabulary.code

        return PropertyType(
            openbis_obj=self,
            code=code,
            label=label,
            description=description,
            dataType=dataType,
            managedInternally=managedInternally,
            vocabulary=vocabulary,
            materialType=materialType,
            schema=schema,
            transformation=transformation,
            metaData=metaData,
            multiValue=multiValue
        )

    def get_property_type(
            self, code, only_data=False, start_with=None, count=None, use_cache=True
    ):

        if not isinstance(code, list) and start_with is None and count is None:
            code = str(code).upper()
            pt = (
                    use_cache
                    and self.use_cache
                    and self._object_cache(entity="property_type", code=code)
            )
            if pt:
                if only_data:
                    return pt.data
                else:
                    return pt

        identifiers = []
        only_one = False
        if not isinstance(code, list):
            code = [code]
            only_one = True

        for c in code:
            identifiers.append(
                {"permId": c.upper(), "@type": "as.dto.property.id.PropertyTypePermId"}
            )

        fetchopts = get_fetchoption_for_entity("propertyType")
        options = ["vocabulary", "materialType", "semanticAnnotations", "registrator"]
        for option in options:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "getPropertyTypes",
            "params": [self.token, identifiers, fetchopts],
        }

        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)

        if only_one:
            if len(resp) == 0:
                raise ValueError(f"no such propertyType: {code}")
            for ident in resp:
                if only_data:
                    return resp[ident]
                else:
                    pt = PropertyType(openbis_obj=self, data=resp[ident])
                    if self.use_cache:
                        self._object_cache(
                            entity="property_type", code=code[0], value=pt
                        )
                    return pt

        # return a list of objects
        else:
            return self._property_type_things(
                objects=list(resp.values()),
                start_with=start_with,
                count=count,
                totalCount=len(resp),
            )

    def get_property_types(self, code=None, start_with=None, count=None):
        fetchopts = get_fetchoption_for_entity("propertyType")
        fetchopts["from"] = start_with
        fetchopts["count"] = count
        search_criteria = get_search_criteria("propertyType", code=code)

        request = {
            "method": "searchPropertyTypes",
            "params": [
                self.token,
                search_criteria,
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        objects = resp["objects"]
        parse_jackson(objects)
        return self._property_type_things(
            objects=objects,
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
        )

    def _property_type_things(
            self, objects, start_with=None, count=None, totalCount=None
    ):
        """takes a list of objects and returns a Things object"""

        def create_data_frame(attrs, props, response):
            attrs = openbis_definitions("propertyType")["attrs"]
            if len(response) == 0:
                df = DataFrame(columns=attrs)
            else:
                df = DataFrame(response)
                df["registrationDate"] = df["registrationDate"].map(format_timestamp)
                df["registrator"] = df["registrator"].map(extract_person)
                df["vocabulary"] = df["vocabulary"].map(extract_code)
                df["semanticAnnotations"] = df["semanticAnnotations"].map(
                    extract_nested_permids
                )
            return df[df.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="propertyType",
            single_item_method=self.get_property_type,
            start_with=start_with,
            count=count,
            totalCount=totalCount,
            response=objects,
            df_initializer=create_data_frame,
        )

    def get_material_types(self, type=None, start_with=None, count=None):
        """Returns a list of all available material types"""
        return self.get_entity_types(
            entity="materialType",
            cls=MaterialType,
            type=type,
            start_with=start_with,
            count=count,
        )

    def get_material_type(self, type, only_data=False):
        return self.get_entity_type(
            entity="materialType",
            cls=MaterialType,
            identifier=type,
            method=self.get_material_type,
            only_data=only_data,
        )

    def get_experiment_types(self, type=None, start_with=None, count=None):
        """Returns a list of all available experiment types"""
        return self.get_entity_types(
            entity="experimentType",
            cls=ExperimentType,
            type=type,
            start_with=start_with,
            count=count,
        )

    get_collection_types = get_experiment_types  # Alias

    def get_experiment_type(self, type, only_data=False):
        return self.get_entity_type(
            entity="experimentType",
            cls=ExperimentType,
            identifier=type,
            method=self.get_experiment_type,
            only_data=only_data,
        )

    get_collection_type = get_experiment_type  # Alias

    def get_dataset_types(self, type=None, start_with=None, count=None):
        """Returns a list of all available dataSet types"""
        return self.get_entity_types(
            entity="dataSetType",
            cls=DataSetType,
            type=type,
            start_with=start_with,
            count=count,
        )

    def get_dataset_type(self, type, only_data=False):
        return self.get_entity_type(
            entity="dataSetType",
            identifier=type,
            cls=DataSetType,
            method=self.get_dataset_type,
            only_data=only_data,
        )

    def get_sample_types(self, type=None, start_with=None, count=None):
        """Returns a list of all available sample types"""
        return self.get_entity_types(
            entity="sampleType",
            cls=SampleType,
            type=type,
            start_with=start_with,
            count=count,
        )

    get_object_types = get_sample_types  # Alias

    def get_sample_type(self, type, only_data=False, with_vocabulary=False, use_cache=True):
        return self.get_entity_type(
            entity="sampleType",
            identifier=type,
            cls=SampleType,
            with_vocabulary=with_vocabulary,
            method=self.get_sample_type,
            only_data=only_data,
            use_cache=use_cache
        )

    get_object_type = get_sample_type  # Alias

    def get_entity_types(
            self, entity, cls, type=None, start_with=None, count=None, with_vocabulary=False
    ):
        method_name = get_method_for_entity(entity, "search")
        if type is not None:
            search_request = _subcriteria_for_code(type, entity)
        else:
            search_request = get_type_for_entity(entity, "search")

        fetch_options = get_fetchoption_for_entity(entity)
        fetch_options["from"] = start_with
        fetch_options["count"] = count

        request = {
            "method": method_name,
            "params": [self.token, search_request, fetch_options],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            parse_jackson(response)
            entity_types = []
            defs = get_definition_for_entity(entity)
            attrs = defs["attrs"]
            objects = response["objects"]
            if len(objects) == 0:
                entity_types = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)
                entity_types = DataFrame(objects)
                entity_types["permId"] = entity_types["permId"].map(extract_permid)
                entity_types["modificationDate"] = entity_types["modificationDate"].map(
                    format_timestamp
                )
                entity_types["validationPlugin"] = entity_types["validationPlugin"].map(
                    extract_nested_permid
                )
            return entity_types[entity_types.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity=entity,
            start_with=start_with,
            single_item_method=getattr(self, cls._single_item_method_name),
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def get_entity_type(
            self,
            entity,
            identifier,
            cls,
            method=None,
            only_data=False,
            with_vocabulary=False,
            use_cache=True,
    ):

        et = (
                not only_data
                and not isinstance(identifier, list)
                and use_cache
                and self._object_cache(entity=entity, code=identifier)
        )
        if et:
            return et

        method_name = get_method_for_entity(entity, "get")
        fetch_options = get_fetchoption_for_entity(entity)
        if with_vocabulary:
            fetch_options["propertyAssignments"]["propertyType"]["vocabulary"] = {
                "@type": "as.dto.vocabulary.fetchoptions.VocabularyFetchOptions",
                "terms": {
                    "@type": "as.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions"
                },
            }

        if not isinstance(identifier, list):
            identifier = [identifier]

        identifiers = []
        for ident in identifier:
            identifiers.append(
                {
                    "permId": ident,
                    "@type": "as.dto.entitytype.id.EntityTypePermId",
                }
            )

        request = {
            "method": method_name,
            "params": [self.token, identifiers, fetch_options],
        }
        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)
        if len(identifiers) == 1:
            if len(resp) == 0:
                raise ValueError(f"no such {entity}: {identifier[0]}")
        for ident in resp:
            if only_data:
                return resp[ident]
            else:
                obj = cls(
                    openbis_obj=self,
                    data=resp[ident],
                    method=method,
                )
                if self.use_cache:
                    self._object_cache(entity=entity, code=ident, value=obj)
                return obj

    def _get_types_of(
            self,
            method_name,
            entity,
            type_name=None,
            start_with=None,
            count=None,
            additional_attributes=None,
            optional_attributes=None,
    ):
        """Returns a list of all available types of an entity.
        If the name of the entity-type is given, it returns a PropertyAssignments object
        """
        if additional_attributes is None:
            additional_attributes = []

        if optional_attributes is None:
            optional_attributes = []

        search_request = {
            "@type": f"as.dto.{entity.lower()}.search.{entity}TypeSearchCriteria"
        }
        fetch_options = {
            "@type": f"as.dto.{entity.lower()}.fetchoptions.{entity}TypeFetchOptions"
        }
        fetch_options["from"] = start_with
        fetch_options["count"] = count

        if type_name is not None:
            search_request = _gen_search_criteria(
                {entity.lower(): entity + "Type", "operator": "AND", "code": type_name}
            )
            fetch_options["propertyAssignments"] = get_fetchoption_for_entity(
                "propertyAssignments"
            )
            if self.get_server_information().is_version_greater_than(3, 3):
                fetch_options["validationPlugin"] = get_fetchoption_for_entity("plugin")

        request = {
            "method": method_name,
            "params": [self.token, search_request, fetch_options],
        }
        resp = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            parse_jackson(response)

            if type_name is not None:
                if len(response["objects"]) == 1:
                    return EntityType(openbis_obj=self, data=response["objects"][0])
                elif len(response["objects"]) == 0:
                    raise ValueError(f"No such {entity} type: {type_name}")
                else:
                    raise ValueError(
                        f"There is more than one entry for entity={entity} and type={type_name}"
                    )

            types = []
            attrs = self._get_attributes(
                type_name, types, additional_attributes, optional_attributes
            )
            objects = response["objects"]
            if len(objects) == 0:
                types = DataFrame(columns=attrs)
            else:
                parse_jackson(objects)
                types = DataFrame(objects)
                types["modificationDate"] = types["modificationDate"].map(
                    format_timestamp
                )
            return types[types.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity=entity.lower() + "_type",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def _get_attributes(
            self, type_name, types, additional_attributes, optional_attributes
    ):
        attributes = ["code", "description"] + additional_attributes
        attributes += [
            attribute for attribute in optional_attributes if attribute in types
        ]
        attributes += ["modificationDate"]
        if type_name is not None:
            attributes += ["propertyAssignments"]
        return attributes

    def is_session_active(self):
        """checks whether a session is still active. Returns true or false."""
        return self.is_token_valid(self.token)

    def is_token_valid(self, token: str = None):
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
            "params": [token],
        }
        resp = self._post_request(self.as_v3, request)
        return resp

    def get_session_info(self, token=None):
        if token is None:
            token = self.token

        if token is None:
            return None

        request = {"method": "getSessionInformation", "params": [token]}
        try:
            resp = self._post_request(self.as_v3, request)
            parse_jackson(resp)
        except Exception as exc:
            return None
        return SessionInformation(openbis_obj=self, data=resp)

    def set_token(self, token, save_token=False):
        """Checks the validity of a token, sets it as the current token and (by default) saves it
        to the disk, i.e. in the ~/.pybis directory
        """
        if not token:
            return
        if type(token) is PersonalAccessToken:
            token = token.permId
        if not self.is_token_valid(token):
            raise ValueError("Session is no longer valid. Please log in again.")
        else:
            self.__dict__["token"] = token
        if save_token:
            self._save_token_to_disk()

    def get_dataset(self, permIds, only_data=False, props=None, **kvals):
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

        just_one = True
        identifiers = []
        if isinstance(permIds, list):
            just_one = False
            for permId in permIds:
                identifiers.append(_type_for_id(permId, "dataset"))
        else:
            identifiers.append(_type_for_id(permIds, "dataset"))

        fetchopts = get_fetchoption_for_entity("dataSet")

        for option in [
            "tags",
            "properties",
            "dataStore",
            "physicalData",
            "linkedData",
            "experiment",
            "sample",
            "registrator",
            "modifier",
        ]:
            fetchopts[option] = get_fetchoption_for_entity(option)

        request = {
            "method": "getDataSets",
            "params": [
                self.token,
                identifiers,
                fetchopts,
            ],
        }

        resp = self._post_request(self.as_v3, request)
        if just_one:
            if len(resp) == 0:
                raise ValueError(f"no such dataset found: {permIds}")

            parse_jackson(resp)

            for permId in resp:
                if only_data:
                    return resp[permId]
                else:
                    return DataSet(
                        openbis_obj=self,
                        type=self.get_dataset_type(resp[permId]["type"]["code"]),
                        data=resp[permId],
                    )
        else:
            return self._dataset_list_for_response(
                response=list(resp.values()), props=props, parsed=False
            )

    def _dataset_list_for_response(
            self,
            response,
            attrs=None,
            props=None,
            start_with=None,
            count=None,
            totalCount=0,
            objects=None,
            parsed=False,
    ):
        """returns a Things object, containing a DataFrame plus some additional information"""

        def extract_attribute(attribute_to_extract):
            def return_attribute(obj):
                if obj is None:
                    return ""
                return obj.get(attribute_to_extract, "")

            return return_attribute

        if not parsed:
            parse_jackson(response)

        if attrs is None:
            attrs = []

        def extract_project(attr):
            entity, _, attr = attr.partition(".")

            def extract_attr(obj):
                try:
                    if attr:
                        return obj["project"][attr]
                    else:
                        return obj["project"]["identifier"]["identifier"]
                except KeyError:
                    return ""

            return extract_attr

        def extract_space(attr):
            entity, _, attr = attr.partition(".")

            def extract_attr(obj):
                try:
                    if attr:
                        return obj["project"]["space"][attr]
                    else:
                        return obj["project"]["space"]["code"]
                except KeyError:
                    return ""

            return extract_attr

        def create_data_frame(attrs, props, response):
            default_attrs = [
                "permId",
                "type",
                "experiment",
                "sample",
                "registrationDate",
                "modificationDate",
                "location",
                "status",
                "presentInArchive",
                "size",
            ]
            display_attrs = default_attrs + attrs

            if props is None:
                props = []
            else:
                if isinstance(props, str):
                    props = [props]

            if len(response) == 0:
                for prop in props:
                    if prop == "*":
                        continue
                    display_attrs.append(prop)
                datasets = DataFrame(columns=display_attrs)
            else:
                datasets = DataFrame(response)
                for attr in attrs:
                    if "project" in attr:
                        datasets[attr] = datasets["experiment"].map(
                            extract_project(attr)
                        )
                    elif "space" in attr:
                        datasets[attr] = datasets["experiment"].map(extract_space(attr))
                    elif "." in attr:
                        entity, attribute_to_extract = attr.split(".")
                        datasets[attr] = datasets[entity].map(
                            extract_attribute(attribute_to_extract)
                        )
                for attr in attrs:
                    # if no dot supplied, just display the code of the space, project or experiment
                    if any(entity == attr for entity in ["experiment", "sample"]):
                        datasets[attr] = datasets[attr].map(extract_nested_identifier)

                datasets["registrationDate"] = datasets["registrationDate"].map(
                    format_timestamp
                )
                datasets["modificationDate"] = datasets["modificationDate"].map(
                    format_timestamp
                )
                datasets["experiment"] = datasets["experiment"].map(
                    extract_nested_identifier
                )
                datasets["sample"] = datasets["sample"].map(extract_nested_identifier)
                datasets["type"] = datasets["type"].map(extract_code)
                datasets["permId"] = datasets["code"]
                for column in ["parents", "children", "components", "containers"]:
                    if column in datasets:
                        datasets[column] = datasets[column].map(extract_identifiers)
                datasets["size"] = datasets["physicalData"].map(
                    lambda x: x.get("size") if x else ""
                )
                datasets["status"] = datasets["physicalData"].map(
                    lambda x: x.get("status") if x else ""
                )
                datasets["presentInArchive"] = datasets["physicalData"].map(
                    lambda x: x.get("presentInArchive") if x else ""
                )
                datasets["location"] = datasets["physicalData"].map(
                    lambda x: x.get("location") if x else ""
                )

                for prop in props:
                    if prop == "*":
                        # include all properties in dataFrame.
                        # expand the dataFrame by adding new columns
                        columns = []
                        for i, dataSet in enumerate(response):
                            for prop_name, val in dataSet.get("properties", {}).items():
                                datasets.loc[i, prop_name.upper()] = val
                                columns.append(prop_name.upper())

                        display_attrs += set(columns)
                        continue

                    else:
                        # property name is provided
                        for i, dataSet in enumerate(response):
                            val = dataSet.get("properties", {}).get(
                                prop, ""
                            ) or dataSet.get("properties", {}).get(prop.upper(), "")
                            datasets.loc[i, prop.upper()] = val
                        display_attrs.append(prop.upper())
            return datasets[datasets.columns.intersection(display_attrs)]

        def create_objects(response):
            return objects

        return Things(
            openbis_obj=self,
            entity="dataset",
            identifier_name="permId",
            start_with=start_with,
            count=count,
            totalCount=totalCount,
            attrs=attrs,
            props=props,
            response=response,
            df_initializer=create_data_frame,
            objects_initializer=create_objects,
        )

    def get_sample(
            self, sample_ident, only_data=False, withAttachments=False, props=None,
            withDataSetIds=False, raw_response=False, **kvals
    ):
        """Retrieve metadata for the sample.
        Get metadata for the sample and any directly connected parents of the sample to allow access
        to the same information visible in the ELN UI. The metadata will be on the file system.
        :param sample_identifiers: A list of sample identifiers to retrieve.
        """

        only_one = True
        identifiers = []
        if isinstance(sample_ident, list):
            only_one = False
            for ident in sample_ident:
                identifiers.append(_type_for_id(ident, "sample"))
        else:
            identifiers.append(_type_for_id(sample_ident, "sample"))

        fetchopts = get_fetchoption_for_entity("sample")
        options = [
            "tags",
            "properties",
            "attachments",
            "space",
            "experiment",
            "registrator",
            "modifier",
            "dataSets",
        ]
        if self.get_server_information().project_samples_enabled:
            options.append("project")
        for option in options:
            fetchopts[option] = get_fetchoption_for_entity(option)

        if withAttachments:
            fetchopts["attachments"] = get_fetchoption_for_entity(
                "attachmentsWithContent"
            )

        for key in ["parents", "children", "container", "components"]:
            fetchopts[key] = {"@type": "as.dto.sample.fetchoptions.SampleFetchOptions"}

        if withDataSetIds:
            fetchopts["dataSets"] = get_fetchoptions("dataSets")

        request = {
            "method": "getSamples",
            "params": [self.token, identifiers, fetchopts],
        }

        resp = self._post_request(self.as_v3, request)

        if only_one:
            if len(resp) == 0:
                raise ValueError(f"no such sample found: {sample_ident}")

            parse_jackson(resp)
            for sample_ident in resp:
                if only_data:
                    return resp[sample_ident]
                else:
                    return Sample(
                        openbis_obj=self,
                        type=self.get_sample_type(resp[sample_ident]["type"]["code"]),
                        data=resp[sample_ident],
                    )
        else:
            if raw_response:
                parse_jackson(resp)
                return resp
            return self._sample_list_for_response(
                response=list(resp.values()), props=props, parsed=False
            )

    def _sample_list_for_response(
            self,
            response,
            attrs=None,
            props=None,
            start_with=None,
            count=None,
            totalCount=0,
            parsed=False,
    ):
        if not parsed:
            parse_jackson(response)

        def create_data_frame(attrs, props, response):
            """returns a Things object, containing a DataFrame plus additional information"""

            def extract_attribute(attribute_to_extract):
                def return_attribute(obj):
                    if obj is None:
                        return ""
                    return obj.get(attribute_to_extract, "")

                return return_attribute

            if attrs is None:
                attrs = []
            default_attrs = [
                "identifier",
                "permId",
                "type",
                "registrator",
                "registrationDate",
                "modifier",
                "modificationDate",
            ]
            display_attrs = default_attrs + attrs
            if props is None:
                props = []
            else:
                if isinstance(props, str):
                    props = [props]
            if len(response) == 0:
                for prop in props:
                    if prop == "*":
                        continue
                    display_attrs.append(prop)
                samples = DataFrame(columns=display_attrs)
            else:

                samples = DataFrame(response)
                for attr in attrs:
                    if "." in attr:
                        entity, attribute_to_extract = attr.split(".")
                        samples[attr] = samples[entity].map(
                            extract_attribute(attribute_to_extract)
                        )
                    # if no dot supplied, just display the code of the space, project or experiment
                    elif attr in ["project", "experiment"]:
                        samples[attr] = samples[attr].map(extract_nested_identifier)
                    elif attr in ["space"]:
                        samples[attr] = samples[attr].map(extract_code)

                samples["registrationDate"] = samples["registrationDate"].map(
                    format_timestamp
                )
                samples["modificationDate"] = samples["modificationDate"].map(
                    format_timestamp
                )
                samples["registrator"] = samples["registrator"].map(extract_person)
                samples["modifier"] = samples["modifier"].map(extract_person)
                samples["identifier"] = samples["identifier"].map(extract_identifier)
                samples["container"] = samples["container"].map(
                    extract_nested_identifier
                )
                for column in ["parents", "children", "components", "dataSets"]:
                    if column in samples:
                        samples[column] = samples[column].map(extract_identifiers)
                samples["permId"] = samples["permId"].map(extract_permid)
                samples["type"] = samples["type"].map(extract_nested_permid)

                for prop in props:
                    if prop == "*":
                        # include all properties in dataFrame.
                        # expand the dataFrame by adding new columns
                        columns = []
                        for i, sample in enumerate(response):
                            for prop_name, val in sample.get("properties", {}).items():
                                samples.loc[i, prop_name.upper()] = val
                                columns.append(prop_name.upper())

                        display_attrs += set(columns)
                        continue
                    else:
                        # property name is provided
                        for i, sample in enumerate(response):
                            if "properties" in sample:
                                properties = sample["properties"]
                                val = properties.get(prop, "") or properties.get(
                                    prop.upper(), ""
                                )
                                samples.loc[i, prop.upper()] = val
                            else:
                                samples.loc[i, prop.upper()] = ""
                        display_attrs.append(prop.upper())

            return samples[samples.columns.intersection(display_attrs)]

        def create_objects(response):
            return list(
                map(
                    lambda obj: Sample(
                        openbis_obj=self,
                        type=self.get_sample_type(obj["type"]["code"]),
                        data=obj,
                        attrs=attrs
                    ),
                    response,
                )
            )

        result = Things(
            openbis_obj=self,
            entity="sample",
            identifier_name="identifier",
            start_with=start_with,
            count=count,
            totalCount=totalCount,
            response=response,
            df_initializer=create_data_frame,
            objects_initializer=create_objects,
            attrs=attrs,
            props=props,
        )

        return result

    @staticmethod
    def decode_attribute(entity, attribute):
        params = {}
        attribute, *alias = re.split(r"\s+AS\s+", attribute, flags=re.IGNORECASE)
        alias = alias[0] if alias else attribute

        regex = re.compile(
            r"""^                         # beginning of the string
                (?P<requested_entity>\w+) # the entity itself
                (\.(?P<attribute>\w+))?   # capture an optional .attribute
                $                         # end of string
        """,
            re.X,
        )
        match = re.search(regex, attribute)
        params = match.groupdict()

        if params["requested_entity"] == "object":
            params["entity"] = "sample"
        elif params["requested_entity"] == "collection":
            params["entity"] = "experiment"
        elif params["requested_entity"] in ["space", "project"]:
            params["entity"] = params["requested_entity"]
        else:
            params["entity"] = params["requested_entity"]

        if not params["attribute"]:
            params["attribute"] = "code"
        params["alias"] = alias

        del params["requested_entity"]
        return params

    def _decode_property(self, entity, property):
        # match something like: property_name.term.label AS label_alias
        regex = re.compile(
            r"""^
                (?P<alias_alternative>
                (?P<property>[^\.]*  )
                (?:
                    \.
                    (?P<subentity>term|pa) \.
                    (?P<field>code|vocabularyCode|label|description|ordinal|dataType)
                )?
                )
                (
                \s+(?i)AS\s+
                (?P<alias>\w+)
                )?
                \s*
                $
            """,
            re.X,
        )
        match = re.search(regex, property)
        if not match:
            try:
                params = self.decode_attribute(entity, property)
                return params
            except ValueError:
                raise ValueError(f"unable to parse property: {property}")
        params = match.groupdict()
        if not params["alias"]:
            params["alias"] = params["alias_alternative"]

        return params

    get_object = get_sample  # Alias

    def get_external_data_management_systems(
            self, start_with=None, count=None, only_data=False
    ):
        entity = "externalDms"

        criteria = get_type_for_entity(entity, "search")
        fetchopts = get_fetchoption_for_entity(entity)
        request = {
            "method": "searchExternalDataManagementSystems",
            "params": [
                self.token,
                criteria,
                fetchopts,
            ],
        }
        response = self._post_request(self.as_v3, request)

        def create_data_frame(attrs, props, response):
            parse_jackson(response)
            attrs = "code label address addressType urlTemplate openbis".split()

            if len(response["objects"]) == 0:
                entities = DataFrame(columns=attrs)
            else:
                objects = response["objects"]
                parse_jackson(objects)
                entities = DataFrame(objects)
                entities["permId"] = entities["permId"].map(extract_permid)
            return entities[entities.columns.intersection(attrs)]

        return Things(
            openbis_obj=self,
            entity="externalDms",
            identifier_name="permId",
            start_with=start_with,
            count=count,
            totalCount=response.get("totalCount"),
            response=response,
            df_initializer=create_data_frame,
        )

    def get_external_data_management_system(self, permId, only_data=False):
        """Retrieve metadata for the external data management system.
        :param permId: A permId for an external DMS.
        :param only_data: Return the result data as a hash-map, not an object.
        """

        request = {
            "method": "getExternalDataManagementSystems",
            "params": [
                self.token,
                [
                    {
                        "@type": "as.dto.externaldms.id.ExternalDmsPermId",
                        "permId": permId,
                    }
                ],
                {
                    "@type": "as.dto.externaldms.fetchoptions.ExternalDmsFetchOptions",
                },
            ],
        }

        resp = self._post_request(self.as_v3, request)
        parse_jackson(resp)

        if resp is None or len(resp) == 0:
            raise ValueError("no such external DMS found: " + permId)
        else:
            for ident in resp:
                if only_data:
                    return resp[ident]
                else:
                    return ExternalDMS(self, resp[ident])

    get_externalDms = get_external_data_management_system  # alias

    def new_space(self, **kwargs):
        """Creates a new space in the openBIS instance."""
        return Space(self, None, **kwargs)

    def new_git_data_set(
            self,
            data_set_type,
            path,
            commit_id,
            repository_id,
            dms,
            sample=None,
            experiment=None,
            properties={},
            dss_code=None,
            parents=None,
            data_set_code=None,
            contents=[],
    ):
        """Create a link data set.
        :param data_set_type: The type of the data set
        :param data_set_type: The type of the data set
        :param path: The path to the git repository
        :param commit_id: The git commit id
        :param repository_id: The git repository id - same for copies
        :param dms: An external data managment system object or external_dms_id
        :param sample: A sample object or sample id.
        :param dss_code: Code for the DSS -- defaults to the first dss if none is supplied.
        :param properties: Properties for the data set.
        :param parents: Parents for the data set.
        :param data_set_code: A data set code -- used if provided, otherwise generated on the server
        :param contents: A list of dicts that describe the contents:
            {'file_length': [file length],
             'crc32': [crc32 checksum],
             'directory': [is path a directory?]
             'path': [the relative path string]}
        :return: A DataSet object
        """
        return pbds.GitDataSetCreation(
            self,
            data_set_type,
            path,
            commit_id,
            repository_id,
            dms,
            sample,
            experiment,
            properties,
            dss_code,
            parents,
            data_set_code,
            contents,
        ).new_git_data_set()

    def new_content_copy(self, path, commit_id, repository_id, edms_id, data_set_id):
        """
        Create a content copy in an existing link data set.
        :param path: path of the new content copy
        "param commit_id: commit id of the new content copy
        "param repository_id: repository id of the content copy
        "param edms_id: Id of the external data managment system of the content copy
        "param data_set_id: Id of the data set to which the new content copy belongs
        """
        return pbds.GitDataSetUpdate(self, data_set_id).new_content_copy(
            path, commit_id, repository_id, edms_id
        )

    def search_files(self, data_set_id, dss_code=None):
        return pbds.GitDataSetFileSearch(self, data_set_id).search_files()

    def delete_content_copy(self, data_set_id, content_copy):
        """
        Deletes a content copy from a data set.
        :param data_set_id: Id of the data set containing the content copy
        :param content_copy: The content copy to be deleted
        """
        return pbds.GitDataSetUpdate(self, data_set_id).delete_content_copy(
            content_copy
        )

    @staticmethod
    def sample_to_sample_id(sample):
        """Take sample which may be a string or object and return an identifier for it."""
        return Openbis._object_to_object_id(
            sample, "as.dto.sample.id.SampleIdentifier", "as.dto.sample.id.SamplePermId"
        )

    @staticmethod
    def experiment_to_experiment_id(experiment):
        """Take experiment which may be a string or object and return an identifier for it."""
        return Openbis._object_to_object_id(
            experiment,
            "as.dto.experiment.id.ExperimentIdentifier",
            "as.dto.experiment.id.SamplePermId",
        )

    @staticmethod
    def _object_to_object_id(obj, identifierType, permIdType):
        object_id = None
        if isinstance(obj, str):
            if is_identifier(obj):
                object_id = {"identifier": obj, "@type": identifierType}
            else:
                object_id = {"permId": obj, "@type": permIdType}
        else:
            object_id = {"identifier": obj.identifier, "@type": identifierType}
        return object_id

    @staticmethod
    def data_set_to_data_set_id(data_set):
        if isinstance(data_set, str):
            code = data_set
        else:
            code = data_set.permId
        return {"permId": code, "@type": "as.dto.dataset.id.DataSetPermId"}

    def external_data_managment_system_to_dms_id(self, dms):
        if isinstance(dms, str):
            dms_id = {"permId": dms, "@type": "as.dto.externaldms.id.ExternalDmsPermId"}
        else:
            dms_id = {
                "identifier": dms.code,
                "@type": "as.dto.sample.id.SampleIdentifier",
            }
        return dms_id

    def new_sample(self, type, project=None, props=None, **kwargs):
        """Creates a new sample of a given sample type.
        type         -- sampleType code or object: mandatory
        code         -- name/code for the sample, if not generated automatically
        space        -- space code or object
        project      -- project code or object
        experiment   -- experiment code or object
        collection   -- same as above
        props        -- a dictionary containing the properties
        """
        if "collection" in kwargs:
            kwargs["experiment"] = kwargs["collection"]
            kwargs.pop("collection", None)

        if isinstance(type, str):
            sample_type = self.get_sample_type(type)
        else:
            sample_type = type
        return Sample(
            self, type=sample_type, project=project, data=None, props=props, **kwargs
        )

    new_object = new_sample  # Alias

    def new_transaction(self, *entities):
        return Transaction(*entities)

    def new_sample_type(
            self,
            code,
            generatedCodePrefix,
            subcodeUnique=False,
            autoGeneratedCode=False,
            listable=True,
            showContainer=False,
            showParents=True,
            showParentMetadata=False,
            validationPlugin=None,
    ):
        """Creates a new sample type."""

        return SampleType(
            self,
            code=code,
            generatedCodePrefix=generatedCodePrefix,
            autoGeneratedCode=autoGeneratedCode,
            listable=listable,
            showContainer=showContainer,
            showParents=showParents,
            showParentMetadata=showParentMetadata,
            validationPlugin=validationPlugin,
            method=self.get_sample_type,
        )

    new_object_type = new_sample_type

    def new_dataset_type(
            self,
            code,
            description=None,
            mainDataSetPattern=None,
            mainDataSetPath=None,
            disallowDeletion=False,
            validationPlugin=None,
    ):
        """Creates a new dataSet type."""

        return DataSetType(
            self,
            code=code,
            description=description,
            mainDataSetPattern=mainDataSetPattern,
            mainDataSetPath=mainDataSetPath,
            disallowDeletion=disallowDeletion,
            validationPlugin=validationPlugin,
            method=self.get_dataset_type,
        )

    def new_experiment_type(
            self,
            code,
            description=None,
            validationPlugin=None,
    ):
        """Creates a new experiment type (collection type)"""
        return ExperimentType(
            self,
            code=code,
            description=description,
            validationPlugin=validationPlugin,
            method=self.get_experiment_type,
        )

    new_collection_type = new_experiment_type

    def new_material_type(
            self,
            code,
            description=None,
            validationPlugin=None,
    ):
        """Creates a new material type."""
        return MaterialType(
            self,
            code=code,
            description=description,
            validationPlugin=validationPlugin,
            method=self.get_material_type,
        )

    def new_dataset(
            self,
            type=None,
            kind="PHYSICAL",
            files=None,
            file=None,
            props=None,
            folder=None,
            **kwargs,
    ):
        """Creates a new dataset of a given type.

        type         -- sampleType code or object: mandatory
        sample       -- sample code or object
        experiment   -- experiment code or object
        collection   -- same as above
        file         -- path to a single file or a directory
        files        -- list of paths to files. Instead of a file, a directory (or many directories)
                        can be provided, the structure is kept intact in openBIS
        zipfile      -- path to a zipfile, which is unzipped in openBIS
        kind         -- if set to CONTAINER, no files should be provided.
                        Instead, the dataset acts as a container for other datasets.

        props        -- a dictionary containing the properties
        """

        if type is None:
            raise ValueError("Please provide a dataSet type")

        if file:
            files = [file]

        if isinstance(type, str):
            type_obj = self.get_dataset_type(type.upper())
        else:
            type_obj = type

        if "object" in kwargs:
            kwargs["sample"] = kwargs["object"]
            kwargs.pop("object", None)
        if "collection" in kwargs:
            kwargs["experiment"] = kwargs["collection"]
            kwargs.pop("collection", None)

        return DataSet(
            self,
            type=type_obj,
            kind=kind,
            files=files,
            folder=folder,
            props=props,
            **kwargs,
        )

    def new_semantic_annotation(self, entityType=None, propertyType=None, **kwargs):
        """Note: not functional yet."""
        return SemanticAnnotation(
            openbis_obj=self,
            isNew=True,
            entityType=entityType,
            propertyType=propertyType,
            **kwargs,
        )

    def new_vocabulary(
            self, code, terms, managedInternally=False, chosenFromList=True, **kwargs
    ):
        """Creates a new vocabulary
        Usage::
            new_vocabulary(
                code = 'vocabulary_code',
                description = '',
                terms = [
                    { "code": "term1", "label": "label1", "description": "description1" },
                    { "code": "term2", "label": "label2", "description": "description2" },
                ]
            )
        """
        kwargs["code"] = code
        kwargs["managedInternally"] = managedInternally
        kwargs["chosenFromList"] = chosenFromList
        return Vocabulary(self, data=None, terms=terms, **kwargs)

    def _get_dss_url(self, dss_code=None):
        """internal method to get the downloadURL of a datastore."""
        dss = self.get_datastores()
        if dss_code is None:
            return dss["downloadUrl"][0]
        else:
            return dss[dss["code"] == dss_code]["downloadUrl"][0]


class ExternalDMS:
    """managing openBIS external data management systems"""

    def __init__(self, openbis_obj, data=None, **kwargs):
        self.__dict__["openbis"] = openbis_obj

        if data is not None:
            self.__dict__["data"] = data

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def __getattr__(self, name):
        return self.__dict__["data"].get(name)

    def __dir__(self):
        """all the available methods and attributes that should be displayed
        when using the autocompletion feature (TAB) in Jupyter
        """
        return ["code", "label", "urlTemplate", "address", "addressType", "openbis"]

    def __str__(self):
        return self.data.get("code", None)


class ServerInformation:
    def __init__(self, info):
        self._info = self._reformat_info(info)
        self.attrs = [
            "api_version",
            "archiving_configured",
            "authentication_service",
            "authentication_service.switch_aai.label",
            "authentication_service.switch_aai.link",
            "create_continuous_sample_codes",
            "enabled_technologies",
            "openbis_version",
            "openbis_support_email",
            "personal_access_tokens_enabled",
            "personal_access_tokens_max_validity_period",
            "personal_access_tokens_validity_warning_period",
            "project_samples_enabled",
        ]

    def _reformat_info(self, info):
        for bool_field in [
            "archiving-configured",
            "project-samples-enabled",
            "personal-access-tokens-enabled",
        ]:
            if bool_field in info:
                info[bool_field] = info[bool_field] == "true"
        for csv_field in ["enabled-technologies"]:
            if csv_field in info:
                info[csv_field] = list(
                    map(lambda item: item.strip(), info[csv_field].split(","))
                )
        for int_field in [
            "personal-access-tokens-max-validity-period",
            "personal-access-tokens-validity-warning-period",
        ]:
            if int_field in info:
                info[int_field] = int(info[int_field])
        info["openbis-support-email"] = info.get("openbis.support.email", "")
        info.pop("openbis.support.email", "")
        return info

    def __dir__(self):
        return self.attrs

    def __getattr__(self, name):
        return self._info.get(name.replace("_", "-"))

    def get_major_version(self):
        return int(self._info["api-version"].split(".")[0])

    def get_minor_version(self):
        return int(self._info["api-version"].split(".")[1])

    def is_openbis_1605(self):
        return (self.get_major_version() == 3) and (self.get_minor_version() <= 2)

    def is_openbis_1806(self):
        return (self.get_major_version() == 3) and (self.get_minor_version() >= 5)

    def is_version_greater_than(self, major: int, minor: int):
        """Checks if server api version is greater than provided"""
        current_major = self.get_major_version()
        current_minor = self.get_minor_version()
        return (current_major == major and current_minor > minor) or current_major > major

    def _repr_html_(self):
        html = """
            <table border="1" class="dataframe">
            <thead>
                <tr style="text-align: right;">
                <th>attribute</th>
                <th>value</th>
                </tr>
            </thead>
            <tbody>
        """

        for attr in self.attrs:
            html += f"<tr> <td>{attr}</td> <td>{getattr(self, attr, '')}</td> </tr>"

        html += """
            </tbody>
            </table>
        """
        return html


class PropertyType(
    OpenBisObject, entity="propertyType", single_item_method_name="get_property_type"
):
    pass


class Plugin(OpenBisObject, entity="plugin", single_item_method_name="get_plugin"):
    pass


class PersonalAccessToken(
    OpenBisObject,
    entity="personalAccessToken",
    single_item_method_name="get_personal_access_token",
):
    def renew(self, validFrom: datetime = None, validTo: datetime = None):
        """Create a new personal access token (PAT) based on an existing one.
        The same sessionName and validity period will be used, starting from now.
        A new PAT will be created, regardless if there is already an existing
        (and still valid) one.

        Args:
            validFrom (datetime): begin of the validity period (default:now)
            validTo (datetime):   end of the validity period (default: validFrom + maximum validity period, as configured in openBIS)
        """
        if not validFrom:
            validFrom = datetime.now()

        if not validTo:
            validFrom_orig = datetime.strptime(self.validFromDate, "%Y-%m-%d %H:%M:%S")
            validTo_orig = datetime.strptime(self.validToDate, "%Y-%m-%d %H:%M:%S")
            days_delta = abs(validFrom_orig - validTo_orig).days
            validTo = validFrom + relativedelta(days=days_delta)

        new_pat = self.openbis.new_personal_access_token(
            sessionName=self.sessionName,
            validFrom=validFrom,
            validTo=validTo,
            force=True,
        )
        return new_pat


class SessionInformation(
    OpenBisObject,
    entity="sessionInformation",
):
    pass
