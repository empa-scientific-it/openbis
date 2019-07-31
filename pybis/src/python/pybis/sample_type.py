from .property_assignment import PropertyAssignments
from .semantic_annotation import SemanticAnnotation
from .attribute import AttrHolder
from .entity_type import EntityType

class SampleType(EntityType):
    """ Helper class for sample types, adding functionality.
    """
    def __init__(self, openbis_obj, data=None, **kwargs):

        # call __init__ of EntityType parent class
        super().__init__(openbis_obj, data)

        self.__dict__['a'] = AttrHolder(openbis_obj, 'SampleType')
        if data is not None:
            self._set_data(data)

        self.__dict__['openbis'] = openbis_obj

        defaults = {
            "autoGeneratedCode"   : False,
            "generatedCodePrefix" : "S",
            "subcodeUnique"       : False,
            "description"         : "",
            "listable"            : True,
            "showContainer"       : False,
            "showParents"         : True,
            "showParentMetadata"  : False
        }


    def _set_data(self, data):
        # assign the attribute data to self.a by calling it
        # (invoking the AttrHolder.__call__ function)
        self.a(data)
        self.__dict__['data'] = data


    def add_semantic_annotation(self, **kwargs):
        semantic_annotation = SemanticAnnotation(
            openbis_obj=self.openbis, isNew=True, 
            entityType=self.code, **kwargs
        )
        semantic_annotation.save()
        return semantic_annotation

    def get_semantic_annotations(self):
        return self.openbis.search_semantic_annotations(entityType=self.code)

