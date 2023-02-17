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
