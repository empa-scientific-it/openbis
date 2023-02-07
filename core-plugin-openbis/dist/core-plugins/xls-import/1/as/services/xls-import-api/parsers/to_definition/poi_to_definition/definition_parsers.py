from java.lang import UnsupportedOperationException
from ..definition import Definition
from .poi_cleaner import PoiCleaner


class DefinitionParserFactory(object):

    @staticmethod
    def get_parser(definition):
        general_definitions = ['VOCABULARY_TYPE', 'SAMPLE_TYPE', 'EXPERIMENT_TYPE', 'DATASET_TYPE', 'EXPERIMENT',
                               'SAMPLE']
        general_definitions_with_type = ['SAMPLE:']
        properties_only_definitions = ['PROPERTY_TYPE', 'SPACE', 'PROJECT']
        definition_type = definition[0]
        if definition_type in general_definitions:
            return GeneralDefinitionParser
        if DefinitionParserFactory.start_with(definition_type, general_definitions_with_type):
            # expect to see all attributes after symbol ':'
            return PropertiesOnlyDefinitionParser
        if definition_type in properties_only_definitions:
            return PropertiesOnlyDefinitionParser

        raise UnsupportedOperationException("Error in row %s: Cannot create %s. Only the following types are allowed: %s"
                                            % (definition['row number'], definition_type,
                                               general_definitions + properties_only_definitions))

    @staticmethod
    def start_with(type, definition_type_prefixes):
        start_with = False
        for prefix in definition_type_prefixes:
            if type.startswith(prefix):
                start_with = True
        return start_with


class PropertiesOnlyDefinitionParser(object):

    @staticmethod
    def parse(poi_definition):
        DEFINITION_TYPE_ROW = 0
        DEFINITION_TYPE_CELL = 0
        PROPERTIES_HEADER_ROW = 1
        PROPERTIES_VALUES_ROW_START = 2

        row_numbers = {
            'DEFINITION_TYPE_ROW': DEFINITION_TYPE_ROW,
            'DEFINITION_TYPE_CELL': DEFINITION_TYPE_CELL,
            'ATTRIBUTES_HEADER_ROW': None,
            'ATTRIBUTES_VALUES_ROW': None,
            'PROPERTIES_HEADER_ROW': PROPERTIES_HEADER_ROW,
            'PROPERTIES_VALUES_ROW_START': PROPERTIES_VALUES_ROW_START
        }

        poi_definition = PoiCleaner.clean_data(poi_definition, row_numbers)
        definition = Definition()
        definition.type = poi_definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]

        if PropertiesOnlyDefinitionParser.has_properties(poi_definition):
            properties_headers = poi_definition[PROPERTIES_HEADER_ROW]

            for property_definitions in poi_definition[PROPERTIES_VALUES_ROW_START:]:
                property = {}
                for col, header in properties_headers.items():
                    property[header] = property_definitions[col]
                definition.properties.append(property)

        return definition

    @staticmethod
    def has_properties(poi_definition):
        PROPERTIES_HEADER_ROW = 1
        return len(poi_definition) > PROPERTIES_HEADER_ROW


class GeneralDefinitionParser(object):

    @staticmethod
    def parse(poi_definition):
        DEFINITION_TYPE_ROW = 0
        DEFINITION_TYPE_CELL = 0
        ATTRIBUTES_HEADER_ROW = 1
        ATTRIBUTES_VALUES_ROW = 2
        PROPERTIES_HEADER_ROW = 3
        PROPERTIES_VALUES_ROW_START = 4

        row_numbers = {
            'DEFINITION_TYPE_ROW': DEFINITION_TYPE_ROW,
            'DEFINITION_TYPE_CELL': DEFINITION_TYPE_CELL,
            'ATTRIBUTES_HEADER_ROW': ATTRIBUTES_HEADER_ROW,
            'ATTRIBUTES_VALUES_ROW': ATTRIBUTES_VALUES_ROW,
            'PROPERTIES_HEADER_ROW': PROPERTIES_HEADER_ROW,
            'PROPERTIES_VALUES_ROW_START': PROPERTIES_VALUES_ROW_START
        }

        poi_definition = PoiCleaner.clean_data(poi_definition, row_numbers)

        definition = Definition()
        definition.type = poi_definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]
        for col, header in poi_definition[ATTRIBUTES_HEADER_ROW].items():
            cell_value = poi_definition[ATTRIBUTES_VALUES_ROW][col]
            definition.attributes[header] = cell_value

        if GeneralDefinitionParser.has_properties(poi_definition):
            properties_headers = poi_definition[PROPERTIES_HEADER_ROW]

            for property_definitions in poi_definition[PROPERTIES_VALUES_ROW_START:]:
                property = {}
                for col, header in properties_headers.items():
                    property[header] = property_definitions[col]
                definition.properties.append(property)

        return definition

    @staticmethod
    def has_properties(poi_definition):
        PROPERTIES_HEADER_ROW = 3
        return len(poi_definition) > PROPERTIES_HEADER_ROW
