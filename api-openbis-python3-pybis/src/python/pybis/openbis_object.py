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
from collections import defaultdict

from .attribute import AttrHolder
from .definitions import (
    get_definition_for_entity,
    get_type_for_entity,
    get_method_for_entity,
)
from .property import PropertyHolder
from .property_reformatter import PropertyReformatter
from .utils import VERBOSE


class OpenBisObject:
    def __init_subclass__(cls, entity=None, single_item_method_name=None):
        """create a specialized parent class.
        The class that inherits from OpenBisObject does not need
        to implement its own __init__ method in order to provide the
        entity name. Instead, it can pass the entity name as a param:
        class XYZ(OpenBisObject, entity="myEntity")
        """
        cls._entity = entity
        cls._single_item_method_name = single_item_method_name

    def __init__(self, openbis_obj, type=None, data=None, props=None, **kwargs):
        self.__dict__["openbis"] = openbis_obj
        self.__dict__["type"] = type
        self.__dict__["p"] = PropertyHolder(openbis_obj, type)
        self.__dict__["a"] = AttrHolder(openbis_obj, self._entity, type)
        self.__dict__["formatter"] = PropertyReformatter(openbis_obj)

        # existing OpenBIS object
        if data is not None:
            self._set_data(data)

        if props is not None:
            for key in props:
                setattr(self.p, key, props[key])

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def __dir__(self):
        defs = get_definition_for_entity(self.entity)
        if self.is_new:
            return defs["attrs_new"]
        else:
            return list(set(defs["attrs"] + defs["attrs_up"]))

    def _set_data(self, data):
        # assign the attribute data to self.a by calling it
        # (invoking the AttrHolder.__call__ function)
        self.a(data)
        self.__dict__["data"] = data

        # put the properties in the self.p namespace (without checking them)
        if "properties" in data:
            for key, value in data["properties"].items():
                if self.p.type:
                    property_type = self.p._property_names[key.lower()]
                    data_type = property_type['dataType']
                    if "multiValue" in property_type:
                        if property_type['multiValue'] is True:
                            if type(value) is not list:
                                value = [value]
                            if data_type in (
                            "ARRAY_INTEGER", "ARRAY_REAL", "ARRAY_STRING", "ARRAY_TIMESTAMP"):
                                value = [self.formatter.to_array(data_type, x) for x in value]
                            else:
                                value = self.formatter.to_array(data_type, value)
                        else:
                            if type(value) is list and data_type not in (
                            "ARRAY_INTEGER", "ARRAY_REAL", "ARRAY_STRING", "ARRAY_TIMESTAMP"):
                                raise ValueError(
                                    f'Property type {property_type} is not a multi-value property!')
                            if data_type in (
                            "ARRAY_INTEGER", "ARRAY_REAL", "ARRAY_STRING", "ARRAY_TIMESTAMP"):
                                value = self.formatter.to_array(data_type, value)
                    else:
                        if data_type in ("ARRAY_INTEGER", "ARRAY_REAL", "ARRAY_STRING", "ARRAY_TIMESTAMP"):
                            value = self.formatter.to_array(data_type, value)
                self.p.__dict__[key.lower()] = value

        # object is already saved to openBIS, so it is not new anymore
        self.a.__dict__["_is_new"] = False

    @property
    def attrs(self):
        return self.__dict__["a"]

    @property
    def space(self):
        try:
            return self.openbis.get_space(self._space["permId"])
        except Exception:
            pass

    @property
    def project(self):
        try:
            return self.openbis.get_project(self._project["identifier"])
        except Exception:
            pass

    @property
    def experiment(self):
        try:
            return self.openbis.get_experiment(self._experiment["identifier"])
        except Exception:
            pass

    collection = experiment  # Alias

    @property
    def sample(self):
        try:
            return self.openbis.get_sample(self._sample["identifier"])
        except Exception:
            pass

    object = sample  # Alias

    @property
    def _permId(self):
        try:
            return self.data["permId"]
        except Exception:
            return ""

    @property
    def permId(self):
        try:
            return self.data["permId"]["permId"]
        except Exception:
            try:
                return self.a.__dict__["_permId"]["permId"]
            except Exception:
                return ""

    def __getattr__(self, name):
        return getattr(self.__dict__["a"], name)

    def __setattr__(self, name, value):
        if name in ["set_properties", "set_tags", "add_tags"]:
            raise ValueError("These are methods which should not be overwritten")
        setattr(self.__dict__["a"], name, value)

    def _repr_html_(self):
        """Print all the assigned attributes (identifier, tags, etc.) in a nicely formatted table. See
        AttributeHolder class.
        """
        return self.a._repr_html_()

    def __repr__(self):
        """same thing as _repr_html_() but for IPython"""
        return self.a.__repr__()

    def mark_to_be_deleted(self):
        self.__dict__["mark_to_be_deleted"] = True

    def unmark_to_be_deleted(self):
        self.__dict__["mark_to_be_deleted"] = False

    def is_marked_to_be_deleted(self):
        return self.__dict__.get("mark_to_be_deleted", False)

    def delete(self, reason, permanently=False):
        """Delete this openbis entity.
        A reason is mandatory to delete any entity.
        """
        if not self.data:
            return

        deletion_id = self.openbis.delete_openbis_entity(
            entity=self._entity, objectId=self.data["permId"], reason=reason
        )
        if VERBOSE:
            print(f"{self._entity} {self.permId} successfully deleted.")

        if permanently:
            self.openbis.confirm_deletions([deletion_id])
            if VERBOSE:
                print(f"{self._entity} {self.permId} successfully deleted permanently.")

    def _get_single_item_method(self):
        single_item_method = None
        if self._single_item_method_name:
            single_item_method = getattr(self.openbis, self._single_item_method_name)
        else:
            # try to guess the method...
            single_item_method = getattr(self.openbis, "get_" + self.entity)

        return single_item_method

    def save(self):
        get_single_item = self._get_single_item_method()
        # check for mandatory properties before saving the object
        props = None
        if self.props:
            for prop_name, prop in self.props._property_names.items():
                if prop["mandatory"]:
                    if (
                        getattr(self.props, prop_name) is None
                        or getattr(self.props, prop_name) == ""
                    ):
                        raise ValueError(
                            f"Property '{prop_name}' is mandatory and must not be None"
                        )

            props = self.formatter.format(self.p._all_props())

        # NEW
        if self.is_new:
            request = self._new_attrs()

            version = self.openbis.get_server_information().openbis_version
            if version is not None:
                if 'SNAPSHOT' not in version and not version.startswith('7') and 'UNKNOWN' not in version:
                    if request['method'] == 'createPropertyTypes' and 'multiValue' in request['params'][1][0]:
                        del request['params'][1][0]['multiValue']
                    if (request['method'] in ('createSampleTypes', 'createSamples',
                                              'createExperimentTypes', 'createExperiments',
                                              'createDataSetTypes', 'createDataSets')
                            and 'metaData' in request['params'][1][0]):
                        del request['params'][1][0]['metaData']

            if props:
                request["params"][1][0]["properties"] = props

            resp = self.openbis._post_request(self.openbis.as_v3, request)

            if VERBOSE:
                print(f"{self.entity} successfully created.")
            new_entity_data = get_single_item(resp[0]["permId"], only_data=True)
            self._set_data(new_entity_data)
            return self

        # UPDATE
        else:
            request = self._up_attrs(method_name=None, permId=self._permId)

            version = self.openbis.get_server_information().openbis_version
            if version is not None:
                if 'SNAPSHOT' not in version and not version.startswith('7') and 'UNKNOWN' not in version:
                    if (request['method'] in ('updateSampleTypes', 'updateSamples',
                                              'updateExperimentTypes', 'updateExperiments',
                                              'updateDataSetTypes', 'updateDataSets')
                            and 'metaData' in request['params'][1][0]):
                        del request['params'][1][0]['metaData']

            if props:
                request["params"][1][0]["properties"] = props

            resp = self.openbis._post_request(self.openbis.as_v3, request)
            if VERBOSE:
                print(f"{self.entity} successfully updated.")
            new_entity_data = get_single_item(
                self.permId, only_data=True, use_cache=False
            )
            self._set_data(new_entity_data)
            return self


class Transaction:
    def __init__(self, *entities):

        self.entities = {}
        self.reason = "no reason"

        if not entities:
            return

        for entity in entities:
            self.add(entity)

    def add(self, entity_obj):
        """Collect the creations or updates of entities.
        self.entities = {
            "sample": {
                {
                    "create": [...],
                    "update": [...]
                },
            },
            "dataSet": {
                {
                    "update": [...]
                }
            }
        }
        """

        if entity_obj.entity == "dataSet" and entity_obj.is_new:
            raise ValueError(
                "pyBIS currently does not support transactions for new dataSets yet."
            )

        if not entity_obj.entity in self.entities:
            self.entities[entity_obj.entity] = defaultdict(list)

        mode = "update"
        if entity_obj.is_new:
            mode = "create"
        elif entity_obj.is_marked_to_be_deleted():
            mode = "delete"
        else:
            mode = "update"

        self.entities[entity_obj.entity][mode].append(entity_obj)

    def commit(self):
        """Merge the individual requests to Make one single request.
        For each entity-type and mode (create, update) an individual request will be sent,
        as the method name differs.
        """

        import copy

        version = None
        for entity_type in self.entities:
            for mode in self.entities[entity_type]:

                request_coll = []
                for entity in self.entities[entity_type][mode]:
                    if version is None:
                        version = entity.openbis.get_server_information().openbis_version
                    if mode == "delete":
                        delete_options = get_type_for_entity(entity_type, "delete")
                        delete_options["reason"] = self.reason
                        method = get_method_for_entity(entity_type, "delete")
                        request = {
                            "method": method,
                            "params": [
                                entity.openbis.token,
                                [entity.data["permId"]],
                                delete_options,
                            ],
                        }
                        request_coll.append(request)
                        continue
                    props = None
                    if entity.props:
                        for prop_name, prop in entity.props._property_names.items():
                            if prop["mandatory"]:
                                if (
                                    getattr(entity.props, prop_name) is None
                                    or getattr(entity.props, prop_name) == ""
                                ):
                                    raise ValueError(
                                        f"Property '{prop_name}' is mandatory and must not be None"
                                    )
                    props = entity.p._all_props()

                    if mode == "create":
                        request = entity._new_attrs()
                        if props:
                            request["params"][1][0]["properties"] = props

                    elif mode == "update":
                        request = entity._up_attrs(
                            method_name=None, permId=entity._permId
                        )
                        if props:
                            request["params"][1][0]["properties"] = props

                    else:
                        raise ValueError(f"Unkown mode: {mode}")

                    if version is not None:
                        if 'SNAPSHOT' not in version and not version.startswith(
                                '7') and 'UNKNOWN' not in version:
                            if request['method'] == 'createPropertyTypes' and 'multiValue' in \
                                    request['params'][1][0]:
                                del request['params'][1][0]['multiValue']
                            if (request['method'] in ('createSampleTypes', 'createSamples',
                                                      'createExperimentTypes', 'createExperiments',
                                                      'createDataSetTypes', 'createDataSets',
                                                      'updateSampleTypes', 'updateSamples',
                                                      'updateExperimentTypes', 'updateExperiments',
                                                      'updateDataSetTypes', 'updateDataSets'
                                                      )
                                    and 'metaData' in request['params'][1][0]):
                                del request['params'][1][0]['metaData']

                    request_coll.append(request)

                if request_coll:
                    # copy the first item of all requests
                    batch_request = copy.deepcopy(request_coll[0])
                    # merge all requests into one
                    for i, request in enumerate(request_coll):
                        if i == 0:
                            continue
                        batch_request["params"][1].append(request["params"][1][0])

                    try:
                        resp = entity.openbis._post_request(
                            entity.openbis.as_v3, batch_request
                        )
                        if VERBOSE:
                            print(f"{i+1} {entity_type}(s) {mode}d.")

                        # mark every sample as not being new anymore
                        # and add the permId attribute received by the response
                        # we assume the response permIds are the same order as we sent them
                        if resp:
                            for i, resp_item in enumerate(resp):
                                if mode == "delete":
                                    continue
                                self.entities[entity_type][mode][i].a.__dict__[
                                    "_is_new"
                                ] = False
                                self.entities[entity_type][mode][i].a.__dict__[
                                    "_permId"
                                ] = resp_item

                    except ValueError as err:
                        if VERBOSE:
                            print(f"ERROR: {mode} of {i+1} {entity_type}(s) FAILED")
                        raise ValueError(err)
