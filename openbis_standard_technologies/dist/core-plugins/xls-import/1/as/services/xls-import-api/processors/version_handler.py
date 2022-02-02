from parsers import versionable_types
from parsers import VocabularyTermCreationToUpdateType, ScriptDefinitionToCreationType, \
    VocabularyTermDefinitionToCreationType, VocabularyDefinitionToCreationType
from utils.openbis_utils import get_metadata_name_for

FORCE = -1


class VersionHandler(object):

    def __init__(self, creations, creations_metadata, existing_elements, versioning_information):
        self.creations = creations
        self.creations_metadata = creations_metadata
        self.existing_elements = existing_elements
        self.versioning_information = versioning_information

    def check_and_filter_versioned_creations(self):
        ignored = []
        for creation_type, creations_collection in self.creations.items():

            if creation_type not in versionable_types:
                continue

            for creation in creations_collection:
                code = creation.code
                version_term_code = get_metadata_name_for(creation_type, creation)
                creation_metadata = self.creations_metadata.get_metadata_for(creation_type, creation)
                entity_excel_version = creation_metadata.version
                key, openbis_version = next(
                    ((key, self.versioning_information[key]) for key in self.versioning_information if key == version_term_code), None)

                if entity_excel_version == FORCE:
                    continue

                if entity_excel_version > openbis_version:
                    self.versioning_information[key] = entity_excel_version
                    continue

                ignored.append(code)

                self.creations[creation_type] = list(filter(lambda c: c != creation, self.creations[creation_type]))
                if ScriptDefinitionToCreationType in self.creations:
                    if hasattr(creation, 'validationPluginId'):
                        pluginId = str(creation.validationPluginId)
                        self.creations[ScriptDefinitionToCreationType] = list(
                            filter(lambda creation: creation.name != pluginId,
                                   self.creations[ScriptDefinitionToCreationType]))

                    if hasattr(creation, 'propertyAssignments'):
                        for propertyAssignment in creation.propertyAssignments:
                            if hasattr(propertyAssignment, 'pluginId'):
                                pluginId = str(propertyAssignment.pluginId)
                                self.creations[ScriptDefinitionToCreationType] = list(
                                    filter(lambda creation: creation.name != pluginId,
                                           self.creations[ScriptDefinitionToCreationType]))
        if ignored:
            print("=======================xls-parser-service====================")
            print("Following elements were not updated due to version mismatch")
            print(", ".join(ignored))
            print("=======================xls-parser-service====================")

        return self.creations
