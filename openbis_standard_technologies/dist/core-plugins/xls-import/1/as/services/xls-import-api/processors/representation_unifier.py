from parsers import PropertyTypeDefinitionToCreationType, VocabularyDefinitionToCreationType, VocabularyTermDefinitionToCreationType, \
    SampleTypeDefinitionToCreationType, ExperimentTypeDefinitionToCreationType, ExperimentDefinitionToCreationType, \
    SampleDefinitionToCreationType

from utils.dotdict import dotdict

sample = "sample"
experiment = "experiment"
property = "property"

entity_and_type_uniform_mapping = {
    SampleDefinitionToCreationType: sample,
    SampleTypeDefinitionToCreationType: sample,
    ExperimentDefinitionToCreationType: experiment,
    ExperimentTypeDefinitionToCreationType: experiment,
    PropertyTypeDefinitionToCreationType: property
}


def unify_properties_representation_of(creations, entity_types, vocabularies, existing_elements):
    properties = {}
    vocabulary_type = VocabularyDefinitionToCreationType
    property_type = PropertyTypeDefinitionToCreationType
    existing_vocabularies = vocabularies[vocabulary_type] if vocabulary_type in vocabularies else []
    existing_properties = existing_elements[property_type] if property_type in existing_elements else []
    properties = _fill_properties_from_creations(properties, creations, existing_vocabularies, existing_properties)
    properties = _fill_properties_from_existing_entity_types(properties, entity_types)
    return properties


def _extract_creations_with_properties(creations_map):
    property_types_with_properties = [SampleTypeDefinitionToCreationType, ExperimentTypeDefinitionToCreationType]
    return dict((key, value) for (key, value) in creations_map.iteritems() if key in property_types_with_properties)


def get_value(dictionary, key):
    if key not in dictionary:
        dictionary[key] = {}
    return dictionary[key]


def _fill_properties_from_existing_entity_types(properties, entity_types_map):
    for creation_type, entity_types in entity_types_map.iteritems():
        for entity_type in entity_types:
            entity_kind = entity_and_type_uniform_mapping[creation_type]
            new_key = entity_kind, entity_type.code
            props = get_value(properties, new_key)
            for property_assignment in entity_type.propertyAssignments:
                property_type = property_assignment.propertyType
                props[property_type.label.lower()] = property_type
                props[property_type.code.lower()] = property_type

    return properties


def _find_property_type_for(property_types, property_assignment):
    for property_type in property_types:
        if str(property_assignment.propertyTypeId) == str(property_type.code):
            return property_type


def _find_vocabulary(vocabularies, vocabulary_code):
    for vocabulary in vocabularies:
        if str(vocabulary.code).lower() == str(vocabulary_code).lower():
            return vocabulary


def _find_vocabulary_terms(vocabulary_terms, vocabulary_code):
    return [vocabulary_term for vocabulary_term in vocabulary_terms if str(vocabulary_term.vocabularyId).lower() == str(vocabulary_code).lower()]


def property_type_representation_from(property_type, creations, existing_vocabularies):
    prop_type_representation = dict()
    prop_type_representation['code'] = property_type.code
    prop_type_representation['label'] = property_type.label
    prop_type_representation['dataType'] = property_type.dataType
    vocabulary = None
    if hasattr(property_type, 'vocabularyId') and property_type.vocabularyId is not None:
        if VocabularyDefinitionToCreationType in creations:
            vocabulary = _find_vocabulary(creations[VocabularyDefinitionToCreationType], property_type.vocabularyId)
            if vocabulary is not None:
                vocabulary_terms = _find_vocabulary_terms(creations[VocabularyTermDefinitionToCreationType], property_type.vocabularyId)
                vocabulary = dotdict({'code':vocabulary.code, 'terms':vocabulary_terms})
        if vocabulary is None:
            vocabulary = _find_vocabulary(existing_vocabularies, property_type.vocabularyId)
    if vocabulary is None and hasattr(property_type, 'vocabulary'):
        vocabulary = property_type.vocabulary
    prop_type_representation['vocabulary'] = vocabulary

    return dotdict(prop_type_representation)


def _fill_properties_from_creations(properties, creations, existing_vocabularies, existing_property_types):
    creations_with_properties = _extract_creations_with_properties(creations)
    for creations_type, creationsList in creations_with_properties.items():
        for creation in creationsList:
            entity_kind = entity_and_type_uniform_mapping[creations_type]
            new_key = entity_kind, creation.code
            props = get_value(properties, new_key)
            if creations_type == PropertyTypeDefinitionToCreationType:
                props[creation.label.lower()] = creation
                props[creation.code.lower()] = creation
            else:
                for property_assignment in creation.propertyAssignments:
                    if PropertyTypeDefinitionToCreationType in creations:
                        property_type = _find_property_type_for(creations[PropertyTypeDefinitionToCreationType],
                                                                property_assignment)
                        if property_type is None:
                            property_type = _find_property_type_for(existing_property_types, property_assignment)
                        if property_type is None:
                            raise Exception("No property type found for " + str(
                                property_assignment) + " having such property type creations ")
                        property_type_bean = property_type_representation_from(property_type, creations,
                                                                               existing_vocabularies)
                        props[property_type_bean.label.lower()] = property_type_bean
                        props[property_type_bean.code.lower()] = property_type_bean

    return properties
