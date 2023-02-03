from .attribute import AttrHolder
from .openbis_object import OpenBisObject
from .utils import VERBOSE, is_identifier, is_permid


class Project(OpenBisObject, entity="project", single_item_method_name="get_project"):
    def _modifiable_attrs(self):
        return

    def __dir__(self):
        """all the available methods and attributes that should be displayed
        when using the autocompletion feature (TAB) in Jupyter
        """

        return [
            "add_attachment()",
            "get_attachments()",
            "download_attachments()",
            "get_experiments()",
            "get_samples()",
            "get_datasets()",
            "save()",
            "delete()",
        ] + super().__dir__()

    @property
    def props(self):
        return self.__dict__["p"]

    def get_samples(self, **kwargs):
        return self.openbis.get_samples(project=self.permId, **kwargs)

    get_objects = get_samples  # Alias

    def get_sample(self, sample_code):
        if is_identifier(sample_code) or is_permid(sample_code):
            return self.openbis.get_sample(sample_code)
        else:
            # we assume we just got the code
            return self.openbis.get_sample(project=self, code=sample_code)

    get_object = get_sample  # Alias

    def get_experiments(self):
        return self.openbis.get_experiments(project=self.permId)

    get_collections = get_experiments  # Alias

    def get_datasets(self):
        return self.openbis.get_datasets(project=self.permId)
