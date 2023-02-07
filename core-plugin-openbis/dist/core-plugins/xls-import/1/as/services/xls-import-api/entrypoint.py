import json
import os
import base64
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id import SpacePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier
from parsers import get_creations_from, get_definitions_from_xls, get_definitions_from_csv, get_creation_metadata_from, \
    CreationOrUpdateToOperationParser, versionable_types
from processors import OpenbisDuplicatesHandler, PropertiesLabelHandler, DuplicatesHandler, \
    unify_properties_representation_of, validate_creations
from search_engines import SearchEngine
from utils import FileHandler
from utils.openbis_utils import get_version_name_for, get_metadata_name_for, get_metadata_name_for_existing_element


def validate_data(xls_byte_arrays, xls_base64_string, csv_strings, update_mode, xls_name):
    if xls_byte_arrays is None and xls_base64_string is None and csv_strings is None:
        raise UserFailureException('Nor Excel sheet nor csv has not been provided. "xls", "xls_base64" and "csv" parameters are None')
    if update_mode not in ['IGNORE_EXISTING', 'FAIL_IF_EXISTS', 'UPDATE_IF_EXISTS']:
        raise UserFailureException(
            'Update mode has to be one of following: IGNORE_EXISTING FAIL_IF_EXISTS UPDATE_IF_EXISTS but was ' + (
                str(update_mode) if update_mode else 'None'))
    if xls_name is None:
        raise UserFailureException('Excel name has not been provided.  parameter is mandatory')


def get_property(key, default_value):
    property_configurer = CommonServiceProvider.getApplicationContext().getBean("propertyConfigurer")
    properties = property_configurer.getResolvedProps()
    return properties.getProperty(key, default_value)


def read_versioning_information(xls_version_filepath):
    if os.path.exists(xls_version_filepath):
        with open(xls_version_filepath, 'r') as f:
            return json.load(f)
    else:
        return {}


def save_versioning_information(versioning_information, xls_version_filepath):
    filepath_new = "%s.new" % xls_version_filepath
    with open(filepath_new, 'w') as f:
        json.dump(versioning_information, f)
    os.rename(filepath_new, xls_version_filepath)


def create_versioning_information(all_versioning_information, creations, creations_metadata, update_mode,
                                  xls_version_name):
    if xls_version_name in all_versioning_information:
        versioning_information = all_versioning_information[xls_version_name].copy()
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_name_for(creation_type, creation)
                    if code in versioning_information:
                        version = versioning_information[code]
                    else:
                        version = creations_metadata.get_metadata_for(creation_type,
                                                                      creation).version if update_mode != "UPDATE_IF_EXISTS" else 0
                    versioning_information[code] = int(version)
    else:
        versioning_information = {}
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_name_for(creation_type, creation)
                    versioning_information[code] = creations_metadata.get_metadata_for(creation_type,
                                                                                       creation).version if update_mode != "UPDATE_IF_EXISTS" else 0
    return versioning_information


def check_data_consistency(existing_elements, all_versioning_information, xls_version_name, creations):
    # This method throw an exception when DB is empty, but xls-import-version-info.json is not

    if xls_version_name not in all_versioning_information:
        return

    versioning_information = all_versioning_information[xls_version_name]

    existing_elements_dict = set()

    for existing_type, elements in existing_elements.items():
        if existing_type not in versionable_types:
            continue
        for element in elements:
            existing_elements_dict.add(get_metadata_name_for_existing_element(existing_type, element))

    versionable_codes_count = 0
    versionable_and_not_exist_codes_count = 0

    for creation_type, creation_collection in creations.items():
        if creation_type in versionable_types:
            for creation in creation_collection:
                code = get_metadata_name_for(creation_type, creation)

                if code in versioning_information:
                    versionable_codes_count = versionable_codes_count + 1
                    if code not in existing_elements_dict:
                        versionable_and_not_exist_codes_count = versionable_and_not_exist_codes_count + 1

    if versionable_codes_count > 0 and versionable_codes_count == versionable_and_not_exist_codes_count:
        raise Exception("All creations from xls-import-version-info.json do not exist in the database." + \
                        "The database may have been deleted. Please delete xls-import-version-info.json too and restart the app.")


def process(context, parameters):
    """
        Excel import AS service.
        For extensive documentation of usage and Excel layout,
        please visit https://wiki-bsse.ethz.ch/display/openBISDoc/Excel+import+service

        :param context: Standard Openbis AS Service context object
        :param parameters: Contains two elements
                        {
                            'xls' : excel byte blob,    - mandatory
                            'xls_name': identifier of excel file - mandatory
                            'scripts': {                - optional
                                file path: loaded file
                            },
                            'experiments_by_type', - optional
                            'spaces_by_type', - optional
                            'definitions_only', - optional (default: False)
                            'disallow_creations', - optional (default: False)
                            'ignore_versioning', - optional (default: False)
                            'render_result', - optional (default: True)
                            'update_mode': [IGNORE_EXISTING|FAIL_IF_EXISTS|UPDATE_IF_EXISTS] - optional, default FAIL_IF_EXISTS
                                                                                             This only takes duplicates that are ON THE SERVER
                        }
        :return: Openbis's execute operations result string. It should contain report on what was created.
    """
    api, session_token = context.applicationService, context.sessionToken
    search_engine = SearchEngine(api, session_token)

    xls_byte_arrays = parameters.get('xls', None)
    xls_base64_string = parameters.get('xls_base64', None)
    csv_strings = parameters.get('csv', None)
    xls_name = parameters.get('xls_name', None)
    experiments_by_type = parameters.get('experiments_by_type', None)
    spaces_by_type = parameters.get('spaces_by_type', None)
    scripts = parameters.get('scripts', {})
    update_mode = parameters.get('update_mode', 'FAIL_IF_EXISTS')
    disallow_creations = parameters.get("disallow_creations", False)
    ignore_versioning = parameters.get('ignore_versioning', False)
    render_result = parameters.get('render_result', True)

    validate_data(xls_byte_arrays, xls_base64_string, csv_strings, update_mode, xls_name)

    if xls_byte_arrays is None and xls_base64_string is not None:
        xls_byte_arrays = [ base64.b64decode(xls_base64_string) ]

    definitions = get_definitions_from_xls(xls_byte_arrays)
    definitions.extend(get_definitions_from_csv(csv_strings))
    if parameters.get('definitions_only', False):
        return definitions
    creations = get_creations_from(definitions, FileHandler(scripts))
    validate_creations(creations)
    creations_metadata = get_creation_metadata_from(definitions)
    creations = DuplicatesHandler.get_distinct_creations(creations)
    existing_elements = search_engine.find_all_existing_elements(creations)

    versioning_information = {}
    if not ignore_versioning:
        xls_version_filepath = get_property("xls-import.version-data-file", "../../../xls-import-version-info.json")
        xls_version_name = get_version_name_for(xls_name)
        all_versioning_information = read_versioning_information(xls_version_filepath)
        check_data_consistency(existing_elements, all_versioning_information, xls_version_name, creations)
        versioning_information = create_versioning_information(all_versioning_information, creations, creations_metadata,
                                                               update_mode, xls_version_name)
    entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
    existing_vocabularies = search_engine.find_all_existing_vocabularies()
    existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies,
                                                                existing_elements)
    creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
    server_duplicates_handler = OpenbisDuplicatesHandler(creations, creations_metadata, existing_elements,
                                                         versioning_information, update_mode)
    creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
    creations = server_duplicates_handler.handle_existing_elements_in_creations()
    entity_type_creation_operations, entity_creation_operations, entity_type_update_operations, entity_update_operations = CreationOrUpdateToOperationParser.parse(
        creations)
    assert_allowed_creations(disallow_creations, entity_creation_operations)
    inject_owner(entity_creation_operations, experiments_by_type, spaces_by_type)

    entity_type_update_results = api.executeOperations(session_token, entity_type_update_operations,
                                                       SynchronousOperationExecutionOptions()).getResults()
    entity_type_creation_results = api.executeOperations(session_token, entity_type_creation_operations,
                                                         SynchronousOperationExecutionOptions()).getResults()
    entity_creation_results = api.executeOperations(session_token, entity_creation_operations,
                                                    SynchronousOperationExecutionOptions()).getResults()
    entity_update_results = api.executeOperations(session_token, entity_update_operations,
                                                  SynchronousOperationExecutionOptions()).getResults()

    if not ignore_versioning:
        all_versioning_information[xls_version_name] = versioning_information
        save_versioning_information(all_versioning_information, xls_version_filepath)
    if render_result:
        return "Update operations performed: {} and {} \n Creation operations performed: {} and {}".format(
            entity_type_update_results, entity_update_results,
            entity_type_creation_results, entity_creation_results)
    ids = []
    add_results(ids, entity_type_update_results)
    add_results(ids, entity_update_results)
    add_results(ids, entity_type_creation_results)
    add_results(ids, entity_creation_results)
    return ids


def add_results(ids, results):
    for result in results:
        for id in result.getObjectIds():
            ids.append(id)


def assert_allowed_creations(disallow_creations, entity_creation_operations):
    if disallow_creations:
        unknown_entities = ""
        counter = 0
        for entity_creation_operation in entity_creation_operations:
            for creation in entity_creation_operation.getCreations():
                unknown_entities += "\n%s [%s]" % (creation.getCreationId(), creation.getTypeId())
                counter += 1
        if counter == 1:
            raise UserFailureException("Unknown entity: %s" % unknown_entities)
        if counter > 1:
            raise UserFailureException("%s unknown entities: %s" % (counter, unknown_entities))


def inject_owner(entity_creation_operations, experiments_by_type, spaces_by_type):
    for eco in entity_creation_operations:
        for creation in eco.getCreations():
            class_name = creation.getClass().getSimpleName()
            if class_name == 'SampleCreation':
                type = creation.getTypeId().getPermId()
                if experiments_by_type is not None and type in experiments_by_type:
                    experiment_identifier = experiments_by_type[type]
                    if experiment_identifier is not None:
                        creation.setExperimentId(ExperimentIdentifier(experiment_identifier))
                if spaces_by_type is not None and type in spaces_by_type:
                    creation.setSpaceId(SpacePermId(spaces_by_type[type]))
                if creation.getExperimentId() is not None and creation.getSpaceId() is None:
                    creation.setSpaceId(SpacePermId(creation.getExperimentId().getIdentifier().split("/")[1]))
                if creation.getProjectId() is not None and creation.getSpaceId() is None:
                    creation.setSpaceId(SpacePermId(creation.getProjectId().getIdentifier().split("/")[1]))
