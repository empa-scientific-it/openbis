from .attribute import AttrHolder
from .openbis_object import OpenBisObject
from .utils import (
    parse_jackson,
    check_datatype,
    split_identifier,
    format_timestamp,
    is_identifier,
    is_permid,
    nvl,
    VERBOSE,
)


class Space(OpenBisObject, entity="space", single_item_method_name="get_space"):
    """managing openBIS spaces"""

    def __dir__(self):
        """all the available methods and attributes that should be displayed
        when using the autocompletion feature (TAB) in Jupyter
        """
        return [
            "get_projects()",
            "new_project()",
            "get_project()",
            "get_experiments()",
            "get_experiment()",
            "get_collections()",
            "get_collection()",
            "get_samples()",
            "get_sample()",
            "get_objects()",
            "get_object()",
            "delete()",
        ] + super().__dir__()

    def __str__(self):
        return self.data.get("code", None)

    def get_samples(self, **kwargs):
        return self.openbis.get_samples(space=self.code, **kwargs)

    get_objects = get_samples  # Alias

    def get_sample(self, sample_code, project_code=None):
        if is_identifier(sample_code) or is_permid(sample_code):
            return self.openbis.get_sample(sample_code)
        else:
            if project_code is None:
                return self.openbis.get_sample(f"/{self.code}/{sample_code}")
            else:
                return self.openbis.get_sample(
                    f"/{self.code}/{project_code}/{sample_code}"
                )

    get_object = get_sample  # Alias

    def get_project(self, project_code):
        if is_identifier(project_code):
            return self.openbis.get_project(project_code)
        else:
            projects = self.openbis.get_projects(code=project_code, space=self.code)
            if len(projects) == 0:
                raise ValueError(
                    f"No project named {project_code} in space {self.code}"
                )

            return projects[0]

    def get_projects(self, **kwargs):
        return self.openbis.get_projects(space=self.code, **kwargs)

    def new_project(self, code, description=None, **kwargs):
        return self.openbis.new_project(self.code, code, description, **kwargs)

    def get_experiments(self, **kwargs):
        return self.openbis.get_experiments(space=self.code, **kwargs)

    get_collections = get_experiments

    def get_experiment(self, experiment_code):
        if is_identifier(experiment_code):
            return self.openbis.get_experiment(experiment_code)
        else:
            experiments = self.openbis.get_experiments(
                code=experiment_code, space=self.code
            )
            if len(experiments) == 0:
                raise ValueError(
                    f"No project named {experiment_code} in space {self.code}"
                )

            return experiments[0]

    get_collection = get_experiment

    def new_sample(self, **kwargs):
        return self.openbis.new_sample(space=self, **kwargs)
