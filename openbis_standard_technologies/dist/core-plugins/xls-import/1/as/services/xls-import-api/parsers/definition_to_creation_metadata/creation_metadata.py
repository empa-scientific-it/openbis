from parsers import VocabularyDefinitionToCreationType, VocabularyTermDefinitionToCreationType


class CreationMetadata(object):

    def __init__(self, metadata):
        self._metadata = metadata

    def __str__(self):
        return str(self._metadata)

    def get_metadata_for(self, creation_type, creation):
        code = creation.code
        if creation_type == VocabularyTermDefinitionToCreationType:
            creation_metadata = self._metadata[VocabularyDefinitionToCreationType][str(creation.vocabularyId)].terms[
                code]
        else:
            creation_metadata = self._metadata[creation_type][code]
        return creation_metadata
