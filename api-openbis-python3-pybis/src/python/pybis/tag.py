from .openbis_object import OpenBisObject
from .utils import VERBOSE
from .attribute import AttrHolder
import json


class Tag(OpenBisObject, entity="tag", single_item_method_name="get_tag"):
    """ """

    def __dir__(self):
        return [
            "get_samples()",
            "get_experiments()",
            "get_materials()",
            "get_owner()",
        ] + super().__dir__()

    def get_owner(self):
        return self.openbis.get_person(self.owner)

    def get_samples(self):
        return self.openbis.get_samples(tags=[self.code])
        # raise ValueError('not yet implemented')

    def get_experiments(self):
        return self.openbis.get_experiments(tags=[self.code])

    def get_materials(self):
        raise ValueError("not yet implemented")
