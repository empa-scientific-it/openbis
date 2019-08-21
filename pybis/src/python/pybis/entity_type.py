from tabulate import tabulate
from texttable import Texttable
from pandas import DataFrame
from .openbis_object import OpenBisObject
from .things import Things
from .utils import check_datatype, split_identifier, format_timestamp, is_identifier, is_permid, nvl, extract_permid, extract_code, extract_name

class EntityType:
    """ EntityTypes define a variety of an entity, eg. sample, dataSet, experiment
    This is the parent class of the SampleType, DataSetType, ExperimentType and
    MaterialType classes.
    """ 

    def __init__(self, openbis_obj, data=None, **kwargs):
        """This __init__ is called by OpenBisObject.__init__
        It stores the propertyAssignments data into the _propertyAssignments
        dict
        """
        pas = []
        if data is not None and 'propertyAssignments' in data: 
            pas = data['propertyAssignments'] 
        self.__dict__['_propertyAssignments'] = pas


    def __str__(self):

        """String representation of this entity type
        """
        return self.data['code']

    def _attrs(self):
        return [
            'code', 'description', 'autoGeneratedCode', 'subcodeUnique',
            'generatedCodePrefix', 'listable', 'showContainer', 'showParents',
            'showParentMetadata', 'validationPlugin',
        ]

    def __dir__(self):
        return self._attrs() + [
            'get_property_assignments()',
            'assign_property()',
            'revoke_property()',
            'move_property_to_top()',
            'move_property_after()',
        ]

    def __getattr__(self, name):
        if name in self._attrs():
            if name in self.data:
                return self.data[name]
            else:
                return ''

    def __eq__(self, other):
        return str(self) == str(other)

    def __ne__(self, other):
        return str(self) != str(other)

    def get_property_assignments(self):
        attrs = [
            'propertyType',
            'section', 'ordinal',
            'mandatory', 'initialValueForExistingEntities', 
            'showInEditView', 'showRawValueInForms',
            'registrator', 'registrationDate', 'plugin'
        ]
        
        pas = self.__dict__['_propertyAssignments']
        df = DataFrame(pas, columns=attrs)
        df['propertyType'] = df['propertyType'].map(extract_code)
        df['plugin'] = df['plugin'].map(extract_name)
        df['registrationDate'] = df['registrationDate'].map(format_timestamp)

        return Things(
            openbis_obj = self.openbis,
            entity = 'propertyType',
            single_item_method = self.openbis.get_property_type,
            identifier_name = 'propertyType',
            df = df,
            start_with = 1,
            count = len(pas),
            totalCount = len(pas),
        )

    def assign_property(self, 
        property, plugin=None,
        section=None, ordinal=None,
        mandatory=False, initialValueForExistingEntities=None,
        showInEditView=True, showRawValueInForms=True

    ):
        """The section groups certain properties.
        The ordinal is defining the rank in the list where the property appears.
        The mandatory defines whether a property must be filled in. If you make a
        property mandatory later, you have to define an initialValueForExistingEntities too.

        """
        pas = self.__dict__['_propertyAssignments']
        new_assignment = {
            "section": section,
            "ordinal": ordinal,
            "mandatory": mandatory,
            "initialValueForExistingEntities": initialValueForExistingEntities,
            "showInEditView": showInEditView,
            "showRawValueInForms": showRawValueInForms,
            "propertyType" : {
                "permId": property.upper(),
                "@type": "as.dto.property.id.PropertyTypePermId"
            },
            "@type": "as.dto.property.create.PropertyAssignmentCreation",
        }
        if plugin is not None:
            new_assignment['pluginId'] = {
                "permId": plugin.upper(),
                "@type": "as.dto.plugin.id.PluginPermId"
            }
        pas.append(new_assignment)

    @property
    def validationPlugin(self):
        try:
            return self.openbis.get_plugin(self._validationPlugin['name'])
        except Exception:
            pass


    def revoke_property(self, 
        property, 
        forceRemovingAssignments=False
    ):
        raise ValueError("not implemented yet")

    def move_property_to_top(self, property):
        raise ValueError("not implemented yet")

    def move_property_after(self, property, after_property):
        raise ValueError("not implemented yet")


    def codes(self):
        codes = []
        for pa in self.data['propertyAssignments']:
            codes.append(pa['propertyType']['code'].lower())
        return codes


class SampleType(
    OpenBisObject, EntityType,
    entity='sampleType',
    single_item_method_name='get_sample_type'
):

    def __init__(self, openbis_obj, type=None, data=None, props=None, **kwargs):
        OpenBisObject.__init__(self, openbis_obj, type=type, data=data, props=props, **kwargs)
        EntityType.__init__(self, openbis_obj, type=type, data=data, props=props, **kwargs)
        

    def __dir__(self):
        return [
            'add_semantic_annotation()',
            'get_semantic_annotations()'
        ] + EntityType.__dir__(self) + OpenBisObject.__dir__(self)


    def add_semantic_annotation(self, **kwargs):
        semantic_annotation = SemanticAnnotation(
            openbis_obj=self.openbis, isNew=True, 
            entityType=self.code, **kwargs
        )
        semantic_annotation.save()
        return semantic_annotation

    def get_semantic_annotations(self):
        return self.openbis.search_semantic_annotations(entityType=self.code)


class DataSetType(
    OpenBisObject, EntityType,
    entity='dataSetType',
    single_item_method_name='get_dataset_type'
):
    def __dir__(self):
        return [
        ] + EntityType.__dir__(self) + OpenBisObject.__dir__(self)


class MaterialType(
    OpenBisObject, EntityType,
    entity='materialType',
    single_item_method_name='get_material_type'
):
    def __dir__(self):
        return [
        ] + EntityType.__dir__(self) + OpenBisObject.__dir__(self)


class ExperimentType(
    OpenBisObject, EntityType,
    entity='experimentType',
    single_item_method_name='get_experiment_type'
):
    def __dir__(self):
        return [
        ] + EntityType.__dir__(self) + OpenBisObject.__dir__(self)
