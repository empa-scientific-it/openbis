from .definition_parsers import DefinitionParserFactory


class PoiToDefinitionParser(object):

    @staticmethod
    def parse(poi_definitions):
        '''
            Expecting definitions to be in such layout:
            [
                `DEFINITIONS_LIST`
                [
                    `DEFINITION`
                    {
                        `ROWS`
                        (column, row) : string_value,
                        .
                        .
                        .
                    }
                ],
                .
                .
                .
            ]
        '''
        definitions = []
        for poi_definition in poi_definitions:
            FIRST_ROW = 0
            definition_type = poi_definition[FIRST_ROW]
            definition_parser = DefinitionParserFactory.get_parser(definition_type)
            definition = definition_parser.parse(poi_definition)
            definition.row_number = poi_definition[0]['row number']
            definitions.append(definition)

        return definitions
