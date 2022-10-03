from .property import PropertyHolder
from .attribute import AttrHolder
from .openbis_object import OpenBisObject
from .definitions import openbis_definitions
from .utils import VERBOSE


class Experiment(
    OpenBisObject, entity="experiment", single_item_method_name="get_experiment"
):
    """Handling experiment (collection) entities in openBIS"""

    def _set_data(self, data):
        # assign the attribute data to self.a by calling it
        # (invoking the AttrHolder.__call__ function)
        self.a(data)
        self.__dict__["data"] = data

        # put the properties in the self.p namespace (without checking them)
        for key, value in data["properties"].items():
            self.p.__dict__[key.lower()] = value

    def __str__(self):
        return self.data["code"]

    def __dir__(self):
        # the list of possible methods/attributes displayed
        # when invoking TAB-completition
        return [
            "get_datasets()",
            "get_samples()",
            "set_tags()",
            "add_tags()",
            "del_tags()",
            "add_attachment()",
            "get_attachments()",
            "download_attachments()",
            "save()",
            "attrs",
            "props",
        ] + super().__dir__()

    @property
    def props(self):
        return self.__dict__["p"]

    @property
    def type(self):
        return self.__dict__["type"]

    @type.setter
    def type(self, type_name):
        experiment_type = self.openbis.get_experiment_type(type_name)
        self.p.__dict__["_type"] = experiment_type
        self.a.__dict__["_type"] = experiment_type

    def __getattr__(self, name):
        return getattr(self.__dict__["a"], name)

    def __setattr__(self, name, value):
        if name in ["set_properties", "add_tags()", "del_tags()", "set_tags()"]:
            raise ValueError("These are methods which should not be overwritten")
        elif name in ["p", "props"]:
            if isinstance(value, dict):
                for p in value:
                    setattr(self.__dict__["p"], p, value[p])
            else:
                raise ValueError("please provide a dictionary for setting properties")
        else:
            setattr(self.__dict__["a"], name, value)

    def _repr_html_(self):
        html = self.a._repr_html_()
        return html

    def set_properties(self, properties):
        for prop in properties.keys():
            setattr(self.p, prop, properties[prop])

    set_props = set_properties

    def get_datasets(self, **kwargs):
        if self.permId is None:
            return None
        return self.openbis.get_datasets(experiment=self.permId, **kwargs)

    def get_projects(self, **kwargs):
        if self.permId is None:
            return None
        return self.openbis.get_project(experiment=self.permId, **kwargs)

    def get_samples(self, **kwargs):
        if self.permId is None:
            return None
        return self.openbis.get_samples(experiment=self.permId, **kwargs)

    get_objects = get_samples  # Alias

    def add_samples(self, *samples):

        for sample in samples:
            if isinstance(sample, str):
                obj = self.openbis.get_sample(sample)
            else:
                # we assume we got a sample object
                obj = sample

            # a sample can only belong to exactly one experiment
            if obj.experiment is not None:
                raise ValueError(
                    f"sample {obj.code} already belongs to experiment {obj.experiment}"
                )
            else:
                if self.is_new:
                    raise ValueError(
                        "You need to save this experiment first before you can assign any samples to it"
                    )
                else:
                    # update the sample directly
                    obj.experiment = self.identifier
                    obj.save()
                    self.a.__dict__["_samples"].append(obj._identifier)

    add_objects = add_samples  # Alias

    def del_samples(self, samples):
        # TODO: implement this method using transactions

        raise ValueError("not implemented yet")
        if not isinstance(samples, list):
            samples = [samples]

        objects = []
        for sample in samples:
            if isinstance(sample, str):
                obj = self.openbis.get_sample(sample)
                objects.append(obj)
            else:
                # we assume we got an object
                objects.append(obj)
        self.samples = objects

    del_objects = del_samples  # Alias
