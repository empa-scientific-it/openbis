from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id import CreationId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import DataSetTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import ExperimentTypeCreation, ExperimentCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin import PluginType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import PluginCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id import PluginPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import ProjectCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import PropertyAssignmentCreation, PropertyTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id import PropertyTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleTypeCreation, SampleCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyTermCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id import VocabularyPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier
from java.lang import UnsupportedOperationException
from ch.systemsx.cisd.common.exceptions import UserFailureException
from utils.openbis_utils import is_internal_namespace, get_script_name_for, upper_case_code, \
                                get_normalized_code, get_normalized, CODE_REGEX
from .creation_types import PropertyTypeDefinitionToCreationType, VocabularyDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, \
    PropertyAssignmentDefinitionToCreationType, SampleTypeDefinitionToCreationType, \
    ExperimentTypeDefinitionToCreationType, \
    DatasetTypeDefinitionToCreationType, SpaceDefinitionToCreationType, ProjectDefinitionToCreationType, \
    ExperimentDefinitionToCreationType, \
    SampleDefinitionToCreationType, ScriptDefinitionToCreationType
import json
import re


def get_boolean_from_string(text):
    if text.lower() not in [u'true', u'false']:
        raise UserFailureException(
            "Boolean field should either be 'true' or 'false' (case insensitive) but was " + text)
    return True if text and text.lower() == u'true' else False


def get_project_identifier(dictionary, row_number):
    project_identifier = upper_case_code(dictionary['project'])
    if project_identifier is None:
        return None
    if re.match('(/%s){2}$' % CODE_REGEX, project_identifier) is None:
        raise UserFailureException("Error in row %s: Invalid project identifier: %s" % (row_number, project_identifier))
    return ProjectIdentifier(project_identifier)


def get_experiment_identifier(dictionary, row_number):
    experiment_identifier = upper_case_code(dictionary['experiment'])
    if experiment_identifier is None:
        return None
    if re.match('(/%s){3}$' % CODE_REGEX, experiment_identifier) is None:
        raise UserFailureException("Error in row %s: Invalid experiment identifier: %s" % (row_number, experiment_identifier))
    return ExperimentIdentifier(experiment_identifier)


def get_sample_identifier(dictionary, row_number):
    sample_identifier = upper_case_code(dictionary['identifier'])
    if sample_identifier is None:
        return None
    if re.match('(/%s){1,3}(:%s)?$' % (CODE_REGEX, CODE_REGEX), sample_identifier) is None:
        raise UserFailureException("Error in row %s: Invalid sample identifier: %s" % (row_number, sample_identifier))
    return sample_identifier


class DefinitionToCreationParserFactory(object):

    @staticmethod
    def get_parsers(definition, context):
        if definition.type == u'VOCABULARY_TYPE':
            return [VocabularyDefinitionToCreationParser(), VocabularyTermDefinitionToCreationParser()]
        if definition.type == u'SAMPLE_TYPE':
            return [SampleTypeDefinitionToCreationParser(), PropertyTypeDefinitionToCreationParser(),
                    ScriptDefinitionToCreationParser(context)]
        if definition.type == u'EXPERIMENT_TYPE':
            return [ExperimentTypeDefinitionToCreationParser(), PropertyTypeDefinitionToCreationParser(),
                    ScriptDefinitionToCreationParser(context)]
        if definition.type == u'DATASET_TYPE':
            return [DatasetTypeDefinitionToCreationParser(), PropertyTypeDefinitionToCreationParser(),
                    ScriptDefinitionToCreationParser(context)]
        if definition.type == u'SPACE':
            return [SpaceDefinitionToCreationParser()]
        if definition.type == u'PROJECT':
            return [ProjectDefinitionToCreationParser()]
        if definition.type == u'EXPERIMENT':
            return [ExperimentDefinitionToCreationParser()]
        if definition.type == u'SAMPLE' or definition.type.startswith(u'SAMPLE:'):
            return [SampleDefinitionToCreationParser()]
        if definition.type == u'PROPERTY_TYPE':
            return [PropertyTypeDefinitionToCreationParser()]

        raise UnsupportedOperationException(
            "Definition of " + str(definition.type) + " is not supported.")


class PropertyTypeDefinitionToCreationParser(object):

    def parse(self, definition):
        property_creations = []

        for prop in definition.properties:
            property_type_creation = PropertyTypeCreation()
            code = upper_case_code(prop.get(u'code'))
            property_type_creation.code = code
            property_type_creation.label = prop.get(u'property label')
            property_type_creation.description = prop.get(u'description')
            property_type_creation.dataType = DataType.valueOf(prop.get(u'data type'))
            property_type_creation.managedInternally = is_internal_namespace(upper_case_code(prop.get(u'code')))
            property_type_creation.vocabularyId = VocabularyPermId(prop.get(u'vocabulary code')) if prop.get(
                u'vocabulary code') is not None else None
            metadata = json.loads(prop.get(u'metadata')) if prop.get(u'metadata') is not None else None
            property_type_creation.metaData = metadata
            property_creations.append(property_type_creation)

        return property_creations

    def get_type(self):
        return PropertyTypeDefinitionToCreationType


class VocabularyDefinitionToCreationParser(object):

    def parse(self, definition):
        code = upper_case_code(definition.attributes.get(u'code'))
        vocabulary_creation = VocabularyCreation()
        vocabulary_creation.code = code
        vocabulary_creation.managedInternally = is_internal_namespace(code)
        vocabulary_creation.description = definition.attributes.get(u'description')

        return vocabulary_creation

    def get_type(self):
        return VocabularyDefinitionToCreationType


class VocabularyTermDefinitionToCreationParser(object):

    def parse(self, definition):
        vocabulary_code = VocabularyPermId(upper_case_code(definition.attributes.get(u'code')))
        vocabulary_creations_terms = []
        for prop in definition.properties:
            vocabulary_creation_term = VocabularyTermCreation()
            vocabulary_creation_term.code = upper_case_code(prop.get(u'code'))
            vocabulary_creation_term.label = prop.get(u'label')
            vocabulary_creation_term.description = prop.get(u'description')
            vocabulary_creation_term.vocabularyId = vocabulary_code
            vocabulary_creations_terms.append(vocabulary_creation_term)

        return vocabulary_creations_terms

    def get_type(self):
        return VocabularyTermDefinitionToCreationType


class PropertyAssignmentDefinitionToCreationParser(object):

    def parse(self, prop):
        code = upper_case_code(prop.get(u'code'))
        property_assignment_creation = PropertyAssignmentCreation()
        is_mandatory = get_boolean_from_string(prop.get(u'mandatory'))
        property_assignment_creation.mandatory = is_mandatory
        should_show_in_edit_view = get_boolean_from_string(prop.get(u'show in edit views'))
        property_assignment_creation.showInEditView = should_show_in_edit_view
        property_assignment_creation.section = prop.get(u'section')
        property_assignment_creation.propertyTypeId = PropertyTypePermId(code)
        if u'dynamic script' in prop and prop.get(u'dynamic script') is not None:
            dynamic_script_path = prop.get(u'dynamic script')
            property_assignment_creation.pluginId = PluginPermId(get_script_name_for(code, dynamic_script_path))

        return property_assignment_creation

    def get_type(self):
        return PropertyAssignmentDefinitionToCreationType


class SampleTypeDefinitionToCreationParser(object):

    def parse(self, definition):
        code = upper_case_code(definition.attributes.get(u'code'))
        sample_creation = SampleTypeCreation()
        sample_creation.code = code
        should_auto_generate_codes = get_boolean_from_string(definition.attributes.get(u'auto generate codes'))
        sample_creation.autoGeneratedCode = should_auto_generate_codes
        sample_creation.description = definition.attributes.get(u'description')
        generatedCodePrefix = definition.attributes.get(u'generated code prefix')
        if generatedCodePrefix is not None:
            sample_creation.generatedCodePrefix = generatedCodePrefix
        if u'validation script' in definition.attributes and definition.attributes.get(
                u'validation script') is not None:
            validation_script_path = definition.attributes.get(u'validation script')
            sample_creation.validationPluginId = PluginPermId(get_script_name_for(code, validation_script_path))

        property_assignment_creations = []
        property_assignment_parser = PropertyAssignmentDefinitionToCreationParser()
        for prop in definition.properties:
            property_assignment_creation = property_assignment_parser.parse(prop)
            property_assignment_creations.append(property_assignment_creation)

        sample_creation.propertyAssignments = property_assignment_creations
        return sample_creation

    def get_type(self):
        return SampleTypeDefinitionToCreationType


class ExperimentTypeDefinitionToCreationParser(object):

    def parse(self, definition):
        code = upper_case_code(definition.attributes.get(u'code'))
        experiment_type_creation = ExperimentTypeCreation()
        experiment_type_creation.code = code
        experiment_type_creation.description = definition.attributes.get(u'description')
        if u'validation script' in definition.attributes and definition.attributes.get(
                u'validation script') is not None:
            validation_script_path = definition.attributes.get(u'validation script')
            experiment_type_creation.validationPluginId = PluginPermId(
                get_script_name_for(code, validation_script_path))

        property_assignment_creations = []
        property_assignment_parser = PropertyAssignmentDefinitionToCreationParser()
        for prop in definition.properties:
            property_assignment_creation = property_assignment_parser.parse(prop)
            property_assignment_creations.append(property_assignment_creation)

        experiment_type_creation.propertyAssignments = property_assignment_creations
        return experiment_type_creation

    def get_type(self):
        return ExperimentTypeDefinitionToCreationType


class DatasetTypeDefinitionToCreationParser(object):

    def parse(self, definition):
        dataset_type_creation = DataSetTypeCreation()
        code = upper_case_code(definition.attributes.get(u'code'))
        dataset_type_creation.code = code
        dataset_type_creation.description = definition.attributes.get(u'description')
        if u'validation script' in definition.attributes and definition.attributes.get(
                u'validation script') is not None:
            validation_script_path = definition.attributes.get(u'validation script')
            dataset_type_creation.validationPluginId = PluginPermId(get_script_name_for(code, validation_script_path))

        property_assignment_creations = []
        property_assignment_parser = PropertyAssignmentDefinitionToCreationParser()
        for prop in definition.properties:
            property_assignment_creation = property_assignment_parser.parse(prop)
            property_assignment_creations.append(property_assignment_creation)

        dataset_type_creation.propertyAssignments = property_assignment_creations
        return dataset_type_creation

    def get_type(self):
        return DatasetTypeDefinitionToCreationType


class SpaceDefinitionToCreationParser(object):

    def parse(self, definition):
        row_number = definition.row_number + 2
        space_creations = []
        for prop in definition.properties:
            space_creation = SpaceCreation()
            space_creation.code = get_normalized_code(prop, row_number)
            space_creation.description = prop.get(u'description')
            space_creation.creationId = CreationId(space_creation.code)
            space_creations.append(space_creation)
            row_number += 1
        return space_creations

    def get_type(self):
        return SpaceDefinitionToCreationType


class ProjectDefinitionToCreationParser(object):

    def parse(self, definition):
        row_number = definition.row_number + 2
        project_creations = []
        for prop in definition.properties:
            project_creation = ProjectCreation()
            project_creation.code = get_normalized_code(prop, row_number)
            project_creation.description = prop.get(u'description')
            project_creation.spaceId = CreationId(get_normalized(prop, u'space', row_number))
            project_creation.creationId = CreationId(project_creation.code)
            project_creations.append(project_creation)
            row_number += 1
        return project_creations

    def get_type(self):
        return ProjectDefinitionToCreationType


class ExperimentDefinitionToCreationParser(object):

    def parse(self, definition):
        experiments = []
        project_attributes = [u'code', u'project']
        row_number = definition.row_number + 4
        for experiment_properties in definition.properties:
            experiment_creation = ExperimentCreation()
            experiment_creation.typeId = EntityTypePermId(definition.attributes.get(u'experiment type'))
            experiment_creation.code = get_normalized_code(experiment_properties, row_number)
            experiment_creation.projectId = get_project_identifier(experiment_properties, row_number)
            creation_id = get_normalized_code(experiment_properties, row_number)
            experiment_creation.creationId = CreationId(creation_id)
            for prop, value in experiment_properties.items():
                if prop not in project_attributes:
                    experiment_creation.setProperty(prop, value)
            experiments.append(experiment_creation)
            row_number += 1
        return experiments

    def get_type(self):
        return ExperimentDefinitionToCreationType


class SampleDefinitionToCreationParser(object):

    def parse(self, definition):
        samples = []
        sample_type = EntityTypePermId(definition.type[7:]) if definition.type.startswith('SAMPLE:') else None

        sample_attributes = [u'$', u'identifier', u'code', u'space', u'project', u'experiment', u'auto generate code', u'parents',
                             u'children']
        row_number = definition.row_number + 4
        for sample_properties in definition.properties:
            sample_creation = SampleCreation()
            if sample_type is not None:
                sample_creation.typeId = sample_type
            else:
                sample_creation.typeId = EntityTypePermId(definition.attributes.get(u'sample type'))
            if self._has_property(sample_properties, u'code'):
                sample_creation.code = get_normalized_code(sample_properties, row_number)
                sample_creation.creationId = CreationId(sample_creation.code)
            if self._has_property(sample_properties, u'$'):
                # may overwrite creationId from code, which is intended
                sample_creation.creationId = CreationId(sample_properties.get(u'$'))
            if self._has_property(sample_properties, u'identifier'):
                sample_creation.creationId = CreationId(get_sample_identifier(sample_properties, row_number))
            if self._has_property(sample_properties, u'auto generate code') and \
                    sample_properties.get(u'auto generate code') != '':
                sample_creation.autoGeneratedCode = get_boolean_from_string(
                    sample_properties.get(u'auto generate code'))
            if self._has_property(sample_properties, u'space'):
                sample_creation.spaceId = CreationId(get_normalized(sample_properties, u'space', row_number))
            if self._has_property(sample_properties, u'project'):
                sample_creation.projectId = get_project_identifier(sample_properties, row_number)
            if self._has_property(sample_properties, u'experiment'):
                sample_creation.experimentId = get_experiment_identifier(sample_properties, row_number)
            if self._has_property(sample_properties, u'parents'):
                parent_creation_ids = []
                parents = sample_properties.get(u'parents').split('\n')
                for parent in parents:
                    parent_creation_ids.append(CreationId(parent))
                sample_creation.parentIds = parent_creation_ids
            if self._has_property(sample_properties, u'children'):
                child_creationids = []
                children = sample_properties.get(u'children').split('\n')
                for child in children:
                    child_creationids.append(CreationId(child))
                sample_creation.childIds = child_creationids

            for prop, value in sample_properties.items():
                if prop not in sample_attributes:
                    sample_creation.setProperty(prop, value)
            samples.append(sample_creation)
            row_number += 1
        return samples

    def get_type(self):
        return SampleDefinitionToCreationType

    def _has_property(self, properties, key):
        return key in properties and properties.get(key) is not None


class ScriptDefinitionToCreationParser(object):
    type = ScriptDefinitionToCreationType

    def __init__(self, context=None):
        self.context = context

    def parse(self, definition):
        scripts = []
        if u'validation script' in definition.attributes:
            validation_script_path = definition.attributes.get(u'validation script')
            if validation_script_path is not None:
                code = upper_case_code(definition.attributes.get(u'code'))
                validation_script_creation = PluginCreation()
                script = self.context.get_script(validation_script_path)
                validation_script_creation.name = get_script_name_for(code, validation_script_path)
                validation_script_creation.script = script
                validation_script_creation.pluginType = PluginType.ENTITY_VALIDATION
                scripts.append(validation_script_creation)

        for prop in definition.properties:
            if u'dynamic script' in prop:
                dynamic_prop_script_path = prop.get(u'dynamic script')
                code = upper_case_code(prop.get(u'code'))
                if dynamic_prop_script_path is not None:
                    validation_script_creation = PluginCreation()
                    script = self.context.get_script(dynamic_prop_script_path)
                    validation_script_creation.name = get_script_name_for(code, dynamic_prop_script_path)
                    validation_script_creation.script = script
                    validation_script_creation.pluginType = PluginType.DYNAMIC_PROPERTY
                    scripts.append(validation_script_creation)

        return scripts

    def get_type(self):
        return ScriptDefinitionToCreationType
