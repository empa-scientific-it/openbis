from utils import merge_dicts
from .creation_metadata_parsers import DefinitionToCreationMetadataParserFactory
from .creation_metadata import CreationMetadata


class DefinitionToCreationMetadataParser(object):

    @staticmethod
    def parse(definitions):
        creations_metadata = {}

        for definition in definitions:
            # One definition may contain more than one creation_metadata
            parsers = DefinitionToCreationMetadataParserFactory.get_parsers(definition)
            for parser in parsers:
                creation_metadata = parser.parse(definition)
                if creation_metadata is None or creation_metadata == []:
                    continue
                creation_type = parser.get_type()
                if creation_type not in creations_metadata:
                    creations_metadata[creation_type] = {}
                merge_dicts(creations_metadata[creation_type], creation_metadata)

        return CreationMetadata(creations_metadata)
