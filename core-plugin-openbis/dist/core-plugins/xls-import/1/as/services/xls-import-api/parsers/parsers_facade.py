from .definition_to_creation import DefinitionToCreationParser
from .excel_to_poi import ExcelToPoiParser
from .to_definition import PoiToDefinitionParser, CsvReaderToDefinitionParser
from .definition_to_creation_metadata import DefinitionToCreationMetadataParser


def get_definitions_from_xls(xls_byte_arrays):
    definitions = []
    for excel_byte_array in xls_byte_arrays or []:
        poi_definitions = ExcelToPoiParser.parse(excel_byte_array)
        partial_definitions = PoiToDefinitionParser.parse(poi_definitions)
        definitions.extend(partial_definitions)
    return definitions


def get_definitions_from_csv(csv_strings):
    definitions = []
    for csv_string in csv_strings or []:
        csv_definitions = CsvReaderToDefinitionParser.parse(csv_string)
        partial_definitions = PoiToDefinitionParser.parse(csv_definitions)
        definitions.extend(partial_definitions)

    return definitions


def get_creations_from(definitions, context):
    return DefinitionToCreationParser.parse(definitions, context)


def get_creation_metadata_from(definitions):
    return DefinitionToCreationMetadataParser.parse(definitions)
