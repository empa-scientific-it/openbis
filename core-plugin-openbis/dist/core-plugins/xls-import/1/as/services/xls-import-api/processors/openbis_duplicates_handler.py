from parsers import SpaceDefinitionToCreationType, ProjectDefinitionToCreationType, VocabularyDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, ScriptDefinitionToCreationType, SampleDefinitionToCreationType, \
    CreationToUpdateParser
from utils.openbis_utils import create_sample_identifier_string, create_project_identifier_string
from .version_handler import VersionHandler

FAIL_IF_EXISTS = "FAIL_IF_EXISTS"
IGNORE_EXISTING = "IGNORE_EXISTING"
UPDATE_IF_EXISTS = "UPDATE_IF_EXISTS"


class OpenbisDuplicatesHandler(object):

    def __init__(self, creations, creations_metadata, existing_elements, versioning_information,
                 duplicates_strategy=FAIL_IF_EXISTS):
        self.creations = creations
        self.creations_metadata = creations_metadata
        self.existing_elements = existing_elements
        self.versioning_information = versioning_information
        self.duplicates_strategy = duplicates_strategy

    def rewrite_parentchild_creationid_to_permid(self):
        if ProjectDefinitionToCreationType in self.creations:
            for creation in self.creations[ProjectDefinitionToCreationType]:
                for existing_element in self.existing_elements[SpaceDefinitionToCreationType]:
                    if existing_element.code == str(creation.spaceId):
                        creation.spaceId = existing_element.permId
                        break
        if SampleDefinitionToCreationType in self.creations:
            for creation in self.creations[SampleDefinitionToCreationType]:
                if creation.spaceId is not None:
                    for existing_element in self.existing_elements[SpaceDefinitionToCreationType]:
                        if existing_element.code == str(creation.spaceId):
                            creation.spaceId = existing_element.permId
                            break

                rewritten_children = []
                if creation.childIds is not None:
                    for child in creation.childIds:
                        child_id = str(child)
                        new_id = None
                        for existing_element in self.existing_elements[SampleDefinitionToCreationType]:
                            existing_element_id = str(existing_element.identifier)
                            if existing_element.permId.permId == child_id:
                                new_id = existing_element.permId
                                break
                            if existing_element_id == child_id:
                                new_id = existing_element.identifier
                                break

                            for child_creation in self.creations[SampleDefinitionToCreationType]:
                                child_creation_id = create_sample_identifier_string(child_creation)
                                creation_id_in_xls = str(child_creation.creationId)
                                if creation_id_in_xls == child_id and existing_element_id == child_creation_id:
                                    new_id = existing_element.identifier
                                    break

                        if new_id is None:
                            rewritten_children.append(child)
                        else:
                            rewritten_children.append(new_id)

                rewritten_parents = []
                if creation.parentIds is not None:
                    for parent in creation.parentIds:
                        new_id = None
                        parent_id = str(parent)
                        for existing_element in self.existing_elements[SampleDefinitionToCreationType]:
                            existing_element_id = str(existing_element.identifier)
                            if existing_element.permId.permId == parent_id:
                                new_id = existing_element.permId
                                break
                            if existing_element.identifier.identifier == parent_id:
                                new_id = existing_element.identifier
                                break
                            for parent_creation in self.creations[SampleDefinitionToCreationType]:
                                parent_creation_id = create_sample_identifier_string(parent_creation)
                                creation_id_in_xls = str(parent_creation.creationId)
                                if creation_id_in_xls == parent_id and existing_element_id == parent_creation_id:
                                    new_id = existing_element.identifier
                                    break

                        if new_id is None:
                            rewritten_parents.append(parent)
                        else:
                            rewritten_parents.append(new_id)
                creation.setChildIds(rewritten_children)
                creation.setParentIds(rewritten_parents)

        return self.creations

    def handle_existing_elements_in_creations(self):

        if self.duplicates_strategy == UPDATE_IF_EXISTS:
            version_handler = VersionHandler(self.creations, self.creations_metadata, self.existing_elements,
                                             self.versioning_information)
            self.creations = version_handler.check_and_filter_versioned_creations()
            duplicates_list = {}
            for creations_type, existing_elements in self.existing_elements.items():
                if not creations_type in self.creations:
                    continue
                if creations_type == VocabularyTermDefinitionToCreationType:
                    continue
                if creations_type == VocabularyDefinitionToCreationType:
                    # VocabularyCreation
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    duplicates_list[creations_type] = self.get_creations_for_existing_objects(creations_type,
                                                                                              existing_elements,
                                                                                              distinct_property_name)
                    self.creations[creations_type] = self._filter_creations_from_existing_objects(creations_type,
                                                                                                  existing_elements,
                                                                                                  distinct_property_name)
                    # VocabularyTermCreation
                    terms = [obj.terms for obj in existing_elements]
                    terms = [item for sublist in terms for item in sublist]
                    existing_object_codes = [(obj.code, obj.vocabulary.code) for obj in terms]
                    duplicates_list[VocabularyTermDefinitionToCreationType] = list(filter(
                        lambda creation: (creation.code, str(creation.vocabularyId)) in existing_object_codes,
                        self.creations[VocabularyTermDefinitionToCreationType]))
                    self.creations[VocabularyTermDefinitionToCreationType] = list(filter(
                        lambda creation: (creation.code, str(creation.vocabularyId)) not in existing_object_codes,
                        self.creations[VocabularyTermDefinitionToCreationType]))
                elif creations_type == SampleDefinitionToCreationType:
                    existing_object_codes = [obj.identifier.identifier for obj in existing_elements]
                    filter_func = lambda c: create_sample_identifier_string(c) in existing_object_codes
                    duplicates_list[creations_type] = list(filter(filter_func, self.creations[creations_type]))
                    self.creations[creations_type] = list(filter(lambda c: not filter_func(c),
                                                                 self.creations[creations_type]))
                elif creations_type == ProjectDefinitionToCreationType:
                    existing_object_codes = [str(obj.identifier) for obj in existing_elements]
                    duplicates_list[creations_type] = list(
                        filter(lambda creation: create_project_identifier_string(creation) in existing_object_codes,
                               self.creations[creations_type]))
                    self.creations[creations_type] = list(
                        filter(lambda creation: create_project_identifier_string(creation) not in existing_object_codes,
                               self.creations[creations_type]))
                else:
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    duplicates_list[creations_type] = self.get_creations_for_existing_objects(creations_type,
                                                                                              existing_elements,
                                                                                              distinct_property_name)
                    self.creations[creations_type] = self._filter_creations_from_existing_objects(creations_type,
                                                                                                  existing_elements,
                                                                                                  distinct_property_name)

            updates = CreationToUpdateParser.parse(duplicates_list, self.existing_elements)
            for update_type, update in updates.items():
                if update_type not in self.creations:
                    self.creations[update_type] = []
                self.creations[update_type].extend(update)

        if self.duplicates_strategy == FAIL_IF_EXISTS:
            duplicates_list = {}
            for creations_type, existing_elements in self.existing_elements.items():
                if not creations_type in self.creations:
                    continue
                if creations_type == SampleDefinitionToCreationType:
                    existing_object_codes = [obj.identifier.identifier for obj in existing_elements]
                    duplicates = list(filter(
                        lambda creation: creation.code is not None and create_sample_identifier_string(
                            creation) in existing_object_codes, self.creations[creations_type]))
                    if duplicates:
                        duplicates_list[creations_type] = duplicates
                elif creations_type == VocabularyDefinitionToCreationType:
                    existing_object_codes = [(obj.code, obj.vocabulary.code) for obj in existing_elements.terms]
                    duplicates_list[VocabularyTermDefinitionToCreationType] = list(filter(
                        lambda creation: (creation.code, str(creation.vocabularyId)) in existing_object_codes,
                        self.creations[VocabularyTermDefinitionToCreationType]))
                elif creations_type == VocabularyTermDefinitionToCreationType:
                    continue
                else:
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    duplicates = self.get_creations_for_existing_objects(creations_type, existing_elements,
                                                                         distinct_property_name)
                    if duplicates:
                        duplicates_list[creations_type] = duplicates
            if duplicates_list:
                raise Exception(
                    "Some of the objects you are trying to create already exist on the server. An error is being thrown when FAIL_IF_EXISTS flag is on. Existing elements are: " + str(
                        duplicates_list))

        if self.duplicates_strategy == IGNORE_EXISTING:
            for creations_type, existing_elements in self.existing_elements.items():
                if not creations_type in self.creations:
                    continue
                if creations_type == SampleDefinitionToCreationType:
                    existing_object_codes = [obj.identifier.identifier for obj in existing_elements]
                    self.creations[creations_type] = list(filter(
                        lambda creation: creation.code is None or create_sample_identifier_string(
                            creation) not in existing_object_codes, self.creations[creations_type]))
                else:
                    distinct_property_name = self._get_distinct_property_name(creations_type)
                    self.creations[creations_type] = self._filter_creations_from_existing_objects(creations_type,
                                                                                                  existing_elements,
                                                                                                  distinct_property_name)
        return self.creations

    def _get_distinct_property_name(self, creation_type):
        if creation_type == ScriptDefinitionToCreationType:
            return 'name'

        return 'code'

    def _filter_creations_from_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(obj, attr) for obj in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) not in existing_object_codes,
                           self.creations[creations_type]))

    def get_creations_for_existing_objects(self, creations_type, existing_objects, attr):
        existing_object_codes = [getattr(obj, attr) for obj in existing_objects]
        return list(filter(lambda creation: getattr(creation, attr) in existing_object_codes,
                           self.creations[creations_type]))
