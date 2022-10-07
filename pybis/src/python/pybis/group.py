from .attribute import AttrHolder
from .openbis_object import OpenBisObject
from .utils import VERBOSE, extract_permid, extract_nested_permid, format_timestamp
from .things import Things
from pandas import DataFrame


class Group(
    OpenBisObject, entity="authorizationGroup", single_item_method_name="get_group"
):
    """Managing openBIS authorization groups"""

    def __dir__(self):
        return [
            "code",
            "description",
            "users",
            "roleAssignments",
            "get_users()",
            "set_users()",
            "add_users()",
            "del_users()",
            "get_roles()",
            "assign_role()",
            "revoke_role(techId)",
        ]

    def get_persons(self):
        """Returns a Things object wich contains all Persons (Users)
        that belong to this group.
        """

        def create_data_frame(attrs, props, response):
            columns = [
                "permId",
                "userId",
                "firstName",
                "lastName",
                "email",
                "space",
                "registrationDate",
                "active",
            ]
            persons = DataFrame(response)
            if len(persons) == 0:
                persons = DataFrame(columns=columns)
            persons["permId"] = persons["permId"].map(extract_permid)
            persons["registrationDate"] = persons["registrationDate"].map(
                format_timestamp
            )
            persons["space"] = persons["space"].map(extract_nested_permid)

            return persons[columns]

        p = Things(
            self.openbis,
            entity="person",
            identifier_name="permId",
            response=self._users,
            df_initializer=create_data_frame,
        )
        return p

    get_users = get_persons  # Alias
    get_members = get_persons  # Alias

    def get_roles(self, **search_args):
        """Get all roles that are assigned to this group.
        Provide additional search arguments to refine your search.

        Usage::
            group.get_roles()
            group.get_roles(space='TEST_SPACE')
        """
        return self.openbis.get_role_assignments(group=self, **search_args)

    def assign_role(self, role, **kwargs):
        """Assign a role to this group. If no additional attribute is provided,
        roleLevel will default to INSTANCE. If a space attribute is provided,
        the roleLevel will be SPACE. If a project attribute is provided,
        roleLevel will be PROJECT.

        Usage::
            group.assign_role(role='ADMIN')
            group.assign_role(role='ADMIN', space='TEST_SPACE')

        """

        try:
            self.openbis.assign_role(role=role, group=self, **kwargs)
            if VERBOSE:
                print(f"Role {role} successfully assigned to group {self.code}")
        except ValueError as e:
            if "exists" in str(e):
                if VERBOSE:
                    print(f"Role {role} already assigned to group {self.code}")
            else:
                raise ValueError(str(e))

    def revoke_role(self, role, space=None, project=None, reason="no reason specified"):
        """Revoke a role from this group."""

        techId = None
        if isinstance(role, int):
            techId = role
        else:
            query = {"role": role}
            if space is None:
                query["space"] = ""
            else:
                query["space"] = space.upper()

            if project is None:
                query["project"] = ""
            else:
                query["project"] = project.upper()

            # build a query string for dataframe
            querystr = " & ".join(f'{key} == "{value}"' for key, value in query.items())
            roles = self.get_roles().df
            if len(roles) == 0:
                if VERBOSE:
                    print(
                        f"Role {role} has already been revoked from group {self.code}"
                    )
                return
            techId = roles.query(querystr)["techId"].values[0]

        # finally delete the role assignment
        ra = self.openbis.get_role_assignment(techId)
        ra.delete(reason)
        if VERBOSE:
            print(f"Role {role} successfully revoked from group {self.code}")
        return

    def save(self):
        if self.is_new:
            request = self._new_attrs()
            resp = self.openbis._post_request(self.openbis.as_v3, request)
            if VERBOSE:
                print("Group successfully created.")
            # re-fetch group from openBIS
            new_data = self.openbis.get_group(resp[0]["permId"], only_data=True)
            self._set_data(new_data)
            return self

        else:
            request = self._up_attrs()
            self.openbis._post_request(self.openbis.as_v3, request)
            if VERBOSE:
                print("Group successfully updated.")
            # re-fetch group from openBIS
            new_data = self.openbis.get_group(self.permId, only_data=True)
            self._set_data(new_data)
