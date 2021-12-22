from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id import ProjectIdentifier
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier
from ch.systemsx.cisd.common.exceptions import UserFailureException
import os
import re

# TODO DRY IT WITH CreationTYpes in definition_to_creation!!!
VocabularyTermDefinitionToCreationType = "VocabularyTerm"


def is_internal_namespace(property_value):
    return property_value and property_value.startswith(u'$')


def get_script_name_for(owner_code, script_path):
    return owner_code + '.' + get_filename_from_path(script_path)


def create_sample_identifier_string(sample_creation):
    # No automagical detection of project_samples flag on openbis
    spaceId = str(sample_creation.spaceId) if sample_creation.spaceId is not None else None
    projectCode = str(sample_creation.projectId).split("/")[2] if sample_creation.projectId is not None else None
    code = sample_creation.code
    sample_identifier = SampleIdentifier(spaceId, projectCode, None, code)
    return sample_identifier.identifier


def create_project_identifier_string(project_creation):
    spaceId = str(project_creation.spaceId)
    code = project_creation.code
    project_identifier = ProjectIdentifier(spaceId, code)
    return str(project_identifier)

def create_experiment_identifier_string(experiment_creation):
    projectId = str(experiment_creation.projectId)
    code = experiment_creation.code
    experiment_identifier = ExperimentIdentifier(projectId + "/" + code)
    return str(experiment_identifier)


def get_filename_from_path(path):
    return os.path.splitext(os.path.basename(path))[0]


def get_version_name_for(name):
    return 'VERSION-{}'.format(name)


def get_metadata_name_for(creation_type, creation):
    if creation_type == VocabularyTermDefinitionToCreationType:
        code = "{}-{}-{}".format(creation_type, str(creation.vocabularyId), creation.code)
    else:
        code = "{}-{}".format(creation_type, creation.code)
    code = code.upper()
    return code


def get_metadata_name_for_existing_element(existing_type, existing_element):
    if existing_type == VocabularyTermDefinitionToCreationType:
        code = str(existing_element.permId).split("(")
        code = code[1].split(")")
        code = "{}-{}".format(code[0], existing_element.code)
    else:
        code = existing_element.code

    code = "{}-{}".format(existing_type, code)
    code = code.upper()
    return code


def get_normalized_code(dict, row_number, dollar_prefix_allowed=False):
    return get_normalized(dict, 'code', row_number, dollar_prefix_allowed)

def get_normalized(dict, key, row_number, dollar_prefix_allowed=False):
    value = upper_case_code(dict[key])
    if value is not None:
        if value.startswith('$') and not dollar_prefix_allowed:
            raise UserFailureException("Error in row %s: %s starts with '$': %s" 
                                       % (row_number, key.capitalize(), value))
        if re.match('\$?[A-Z0-9_\-.]+$', value) is None:
            leading_dollar_test = " after the leasing $" if value.startswith('$') else ""
            raise UserFailureException(("Error in row %s: %s contains an invalid character. "
                + "Only digits, letter, '-', '_', and '.' are allowed%s: %s") 
                % (row_number, key.capitalize(), leading_dollar_test, value))
    return value


def upper_case_code(code):
    return code.upper() if code is not None else None