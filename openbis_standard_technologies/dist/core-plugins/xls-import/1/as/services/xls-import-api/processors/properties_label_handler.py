from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from parsers import SampleDefinitionToCreationType, ExperimentDefinitionToCreationType
from .representation_unifier import entity_and_type_uniform_mapping


class PropertiesLabelHandler(object):

    @staticmethod
    def rewrite_property_labels_to_codes(creations_map, properties_helper_map):
        '''
            This handler rewrites labels for column names and for vocabulary column values
        '''
        creations_with_rewritable_properties = [SampleDefinitionToCreationType, ExperimentDefinitionToCreationType]
        for creations_type, creations in creations_map.iteritems():
            if creations_type in creations_with_rewritable_properties:
                for creation in creations:
                    entity_kind = entity_and_type_uniform_mapping[creations_type]
                    if (entity_kind, str(creation.typeId)) in properties_helper_map:
                        entity_property_types = properties_helper_map[(entity_kind, str(creation.typeId))]
                        new_properties = {}
                        for key, val in dict(creation.properties).iteritems():
                            try:
                                new_val = val
                                entity = entity_property_types[key.lower()]
                                if val is not None and entity.dataType == DataType.CONTROLLEDVOCABULARY:
                                    for term in entity.vocabulary.terms:
                                        if term.label.lower() == val.lower():
                                            new_val = term.code
                                            break
                                new_properties[entity_property_types[key.lower()].code] = new_val
                            except:
                                raise Exception("Entity %s of type %s doesn't have such code or label: %s"
                                                            % (creation.code, creation.typeId, key))
                        creation.properties = new_properties
        return creations_map
