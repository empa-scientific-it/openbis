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
from .attribute import AttrHolder
from .openbis_object import OpenBisObject
from .utils import VERBOSE


class RoleAssignment(OpenBisObject):
    """managing openBIS role assignments"""

    def __init__(self, openbis_obj, data=None, **kwargs):
        self.__dict__["openbis"] = openbis_obj
        self.__dict__["a"] = AttrHolder(openbis_obj, "roleAssignment")

        if data is not None:
            self.a(data)
            self.__dict__["data"] = data

        if kwargs is not None:
            for key in kwargs:
                setattr(self, key, kwargs[key])

    def __dir__(self):
        """all the available methods and attributes that should be displayed
        when using the autocompletion feature (TAB) in Jupyter
        """
        return ["id", "role", "roleLevel", "space", "project", "group"]

    def __str__(self):
        return f"{self.get('role')}"

    def delete(self, reason="no reason specified"):
        self.openbis.delete_openbis_entity(
            entity="roleAssignment", objectId=self._id, reason=reason
        )
        if VERBOSE:
            print(
                f"RoleAssignment role={self.role}, roleLevel={self.roleLevel} successfully deleted."
            )
