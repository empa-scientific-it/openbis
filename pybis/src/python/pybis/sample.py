from .attribute import AttrHolder
from .definitions import openbis_definitions
from .openbis_object import OpenBisObject
from .property import PropertyHolder
from .utils import VERBOSE


class Sample(OpenBisObject, entity="sample", single_item_method_name="get_sample"):
    """A Sample (new: Object) is one of the most commonly used entities in openBIS."""

    def __init__(
        self, openbis_obj, type, project=None, data=None, props=None, **kwargs
    ):
        self.__dict__["openbis"] = openbis_obj
        self.__dict__["type"] = type
        ph = PropertyHolder(openbis_obj, type)
        self.__dict__["p"] = ph
        self.__dict__["props"] = ph
        self.__dict__["a"] = AttrHolder(openbis_obj, "sample", type)

        if data is not None:
            self._set_data(data)

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

            if "experiment" in kwargs:
                try:
                    experiment = self.experiment
                    if not "space" in kwargs:
                        project = experiment.project
                        self.a.space = project.space
                except Exception:
                    pass

        if project is None:
            if self.experiment:
                self.project = self.experiment.project
        else:
            self.project = project

        if props is not None:
            for key in props:
                # self.p[key] = props[key]
                setattr(self.p, key, props[key])

        if getattr(self, "parents") is None:
            self.a.__dict__["_parents"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_parents_orig"] = self.a.__dict__["_parents"]

        if getattr(self, "children") is None:
            self.a.__dict__["_children"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_children_orig"] = self.a.__dict__["_children"]

        if getattr(self, "components") is None:
            self.a.__dict__["_components"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_components_orig"] = self.a.__dict__["_components"]

    def _set_data(self, data):
        # assign the attribute data to self.a by calling it
        # (invoking the AttrHolder.__call__ function)
        self.a(data)
        self.__dict__["data"] = data

        # put the properties in the self.p namespace (without checking them)
        for key, value in data["properties"].items():
            self.p.__dict__[key.lower()] = value

    def __dir__(self):
        return [
            "type",
            "get_parents()",
            "get_children()",
            "get_components()",
            "add_parents()",
            "add_children()",
            "add_components()",
            "del_parents()",
            "del_children()",
            "del_components()",
            "set_parents()",
            "set_children()",
            "set_components()",
            "get_datasets()",
            "space",
            "project",
            "experiment",
            "container",
            "tags",
            "set_tags()",
            "add_tags()",
            "del_tags()",
            "add_attachment()",
            "get_attachments()",
            "download_attachments()",
            "save()",
            "delete()",
            "mark_to_be_deleted()",
            "unmark_to_be_deleted()",
            "is_marked_to_be_deleted()",
            "attrs",
            "props",
        ] + super().__dir__()

    def _container(self, value=None):
        if value is not None:
            if value == "":
                if self.is_new:
                    pass
                else:
                    self.a.__dict__["_container"] = {}
            else:
                obj = None
                if isinstance(value, str):
                    # fetch object in openBIS, make sure it actually exists
                    obj = getattr(self._openbis, "get_sample")(value)
                elif value is None:
                    self.a.__dict__["_container"] = {}
                else:
                    obj = value

                self.a.__dict__["_container"] = obj.data["identifier"]

                # mark attribute as modified, if it's an existing entity
                if self.is_new:
                    pass
                else:
                    self.a.__dict__["_container"]["isModified"] = True
        else:
            try:
                return self.openbis.get_sample(self.a._container["identifier"])
            except Exception:
                pass

    @property
    def type(self):
        return self.__dict__["type"]

    @type.setter
    def type(self, type_name):
        sample_type = self.openbis.get_sample_type(type_name)
        self.p.__dict__["_type"] = sample_type
        self.a.__dict__["_type"] = sample_type

    def __getattr__(self, name):
        if name in ["container"]:
            return getattr(self, "_" + name)()

        return getattr(self.__dict__["a"], name)

    def __setattr__(self, name, value):
        if name in ["set_properties", "set_tags", "add_tags"]:
            raise ValueError("These are methods which should not be overwritten")

        if name in ["container"]:
            return getattr(self, "_" + name)(value)

        if name in ["p", "props"]:
            if isinstance(value, dict):
                for p in value:
                    setattr(self.__dict__["p"], p, value[p])
            else:
                raise ValueError("please provide a dictionary for setting properties")
        else:
            # must be an attribute in the AttributeHolder class
            setattr(self.__dict__["a"], name, value)

    def _repr_html_(self):
        return self.a._repr_html_()

    def __repr__(self):
        return self.a.__repr__()

    def set_properties(self, properties):
        for prop in properties.keys():
            setattr(self.p, prop, properties[prop])

    set_props = set_properties

    def get_datasets(self, **kwargs):
        return self.openbis.get_datasets(sample=self.permId, **kwargs)

    def get_projects(self, **kwargs):
        return self.openbis.get_project(withSamples=[self.permId], **kwargs)

    @property
    def experiment(self):
        try:
            return self.openbis.get_experiment(self._experiment["identifier"])
        except Exception:
            pass

    def save(self):
        """invoked when code is provided in cases when the type already generates
        the code automatically. In this case, we need to invoke the old V1 method.
        """
        if self.is_new and self.code is not None and self.type.autoGeneratedCode:
            request = self._new_attrs()
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

            sampleProject = self.project.code if self.project else None
            sampleExperiment = self.experiment.code if self.experiment else None

            request = {
                "method": "createReportFromAggregationService",
                "params": [
                    self.openbis.token,
                    self.openbis.get_datastores()["code"][0],
                    "eln-lims-api",
                    {
                        "method": "insertSample",
                        "sampleSpace": self.space.code,
                        "sampleProject": sampleProject,
                        "sampleExperiment": sampleExperiment,
                        "sampleCode": self.code,
                        "sampleType": self.type.code,
                        "sampleProperties": self.props(),
                        "sampleParents": self.parents,
                        "sampleParentsNew": None,
                        "sampleChildrenNew": self.children,
                        "sampleChildrenAdded": [],
                        "sampleChildrenRemoved": [],
                        "changesToDo": [],
                        "sessionToken": self.openbis.token,
                    },
                ],
            }
            resp = self.openbis._post_request(self.openbis.reg_v1, request)
            try:
                if resp["rows"][0][0]["value"] != "OK":
                    raise ValueError("Status is not OK")
                if VERBOSE:
                    print(f"{self.entity} successfully created.")
                permId = permid = resp["rows"][0][2]["value"]
                new_entity_data = self.openbis.get_sample(permId, only_data=True)
                self._set_data(new_entity_data)
                return self
            except Exception as exc:
                errmsg = f"Could not create {self.entity}"
                try:
                    errmsg = resp["rows"][0][1]["value"]
                    errmsg = errmsg.split("\n")[0].split("UserFailureException: ")[1]
                except IndexError:
                    pass
                raise ValueError(errmsg) from exc

        else:
            super().save()
