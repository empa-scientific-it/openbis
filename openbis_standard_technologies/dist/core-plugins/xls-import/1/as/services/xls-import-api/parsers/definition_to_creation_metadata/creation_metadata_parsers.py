from ..definition_to_creation import PropertyTypeDefinitionToCreationType, VocabularyDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, PropertyAssignmentDefinitionToCreationType, \
    SampleTypeDefinitionToCreationType, ExperimentTypeDefinitionToCreationType, DatasetTypeDefinitionToCreationType, \
    SpaceDefinitionToCreationType, ProjectDefinitionToCreationType, ExperimentDefinitionToCreationType, \
    SampleDefinitionToCreationType, ScriptDefinitionToCreationType
from utils.openbis_utils import get_metadata_name_for, upper_case_code, get_normalized_code
from ch.systemsx.cisd.common.exceptions import UserFailureException
from utils.dotdict import dotdict
from java.lang import UnsupportedOperationException


def get_version(value):
    if value == "FORCE":
        return -1
    try:
        return int(value)
    except ValueError:
        raise UserFailureException(
            "Value field accepts integer numbers or string FORCE in case of force creation but was" + value)


class DefinitionToCreationMetadataParserFactory(object):

    @staticmethod
    def get_parsers(definition):
        if definition.type == u'VOCABULARY_TYPE':
            return [VocabularyDefinitionToCreationMetadataParser()]
        elif definition.type == u'SAMPLE_TYPE':
            return [SampleTypeDefinitionToCreationMetadataParser(), PropertyTypeDefinitionToCreationMetadataParser(),
                    ScriptDefinitionToCreationMetadataParser()]
        elif definition.type == u'EXPERIMENT_TYPE':
            return [ExperimentTypeDefinitionToCreationMetadataParser(),
                    PropertyTypeDefinitionToCreationMetadataParser(),
                    ScriptDefinitionToCreationMetadataParser()]
        elif definition.type == u'DATASET_TYPE':
            return [DatasetTypeDefinitionToCreationMetadataParser(), PropertyTypeDefinitionToCreationMetadataParser(),
                    ScriptDefinitionToCreationMetadataParser()]
        elif definition.type == u'SPACE':
            return [SpaceDefinitionToCreationMetadataParser()]
        elif definition.type == u'PROJECT':
            return [ProjectDefinitionToCreationMetadataParser()]
        elif definition.type == u'EXPERIMENT':
            return [ExperimentDefinitionToCreationMetadataParser()]
        elif definition.type == u'SAMPLE' or definition.type.startswith(u'SAMPLE:'):
            return [SampleDefinitionToCreationMetadataParser()]
        elif definition.type == u'PROPERTY_TYPE':
            return [PropertyTypeDefinitionToCreationMetadataParser()]
        else:
            raise UnsupportedOperationException(
                "Definition of " + str(definition.type) + " is not supported.")


class PropertyTypeDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        creation_metadata = dotdict()

        for prop in definition.properties:
            property_creation_metadata = dotdict()
            code = upper_case_code(prop.get(u'code'))
            property_creation_metadata.code = code
            property_creation_metadata.version = get_version(prop.get(u'version', 1))
            creation_metadata[code] = property_creation_metadata
        return creation_metadata

    def get_type(self):
        return PropertyTypeDefinitionToCreationType


class VocabularyDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        creation_metadata = dotdict()
        vocabulary_creation_metadata = dotdict()
        row_number = definition.row_number + 2
        code = get_normalized_code(definition.attributes, row_number, dollar_prefix_allowed=True)
        vocabulary_creation_metadata.code = code
        vocabulary_creation_metadata.version = get_version(definition.attributes.get(u'version', 1))
        vocabulary_creation_metadata.terms = dotdict()
        row_number += 2
        for prop in definition.properties:
            term_code = get_normalized_code(prop, row_number)
            creation_term_metadata = dotdict()
            creation_term_metadata.code = term_code
            creation_term_metadata.version = get_version(prop.get(u'version', 1))
            vocabulary_creation_metadata.terms[term_code] = creation_term_metadata
            row_number += 1
        creation_metadata[code] = vocabulary_creation_metadata
        return creation_metadata

    def get_type(self):
        return VocabularyDefinitionToCreationType


class SampleTypeDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        creation_metadata = dotdict()
        sample_type_creation_metadata = dotdict()
        code = get_normalized_code(definition.attributes, definition.row_number + 2)
        sample_type_creation_metadata.code = code
        sample_type_creation_metadata.version = get_version(definition.attributes.get(u'version', 1))
        creation_metadata[code] = sample_type_creation_metadata

        return creation_metadata

    def get_type(self):
        return SampleTypeDefinitionToCreationType


class ExperimentTypeDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        creation_metadata = dotdict()
        experiment_type_creation_metadata = dotdict()
        code = get_normalized_code(definition.attributes, definition.row_number + 2)
        experiment_type_creation_metadata.code = code
        experiment_type_creation_metadata.version = get_version(definition.attributes.get(u'version', 1))
        creation_metadata[code] = experiment_type_creation_metadata

        return creation_metadata

    def get_type(self):
        return ExperimentTypeDefinitionToCreationType


class DatasetTypeDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        creation_metadata = dotdict()
        dataset_type_creation_metadata = dotdict()
        code = upper_case_code(definition.attributes.get(u'code'))
        dataset_type_creation_metadata.code = code
        dataset_type_creation_metadata.version = get_version(definition.attributes.get(u'version', 1))
        creation_metadata[code] = dataset_type_creation_metadata

        return creation_metadata

    def get_type(self):
        return DatasetTypeDefinitionToCreationType


class PropertyAssignmentDefinitionToCreationMetadataParser(object):

    def parse(self, prop):
        return dotdict()

    def get_type(self):
        return PropertyAssignmentDefinitionToCreationType


class SpaceDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        return dotdict()

    def get_type(self):
        return SpaceDefinitionToCreationType


class ProjectDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        return dotdict()

    def get_type(self):
        return ProjectDefinitionToCreationType


class ExperimentDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        return dotdict()

    def get_type(self):
        return ExperimentDefinitionToCreationType


class SampleDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        return dotdict()

    def get_type(self):
        return SampleDefinitionToCreationType


class ScriptDefinitionToCreationMetadataParser(object):

    def parse(self, definition):
        return dotdict()

    def get_type(self):
        return ScriptDefinitionToCreationType
