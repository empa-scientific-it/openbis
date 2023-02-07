from .creation_parsers import DefinitionToCreationParserFactory


class DefinitionToCreationParser(object):

    @staticmethod
    def parse(definitions, context=None):
        creations = {}

        for definition in definitions:
            # One definition may contain more than one creation
            parsers = DefinitionToCreationParserFactory.get_parsers(definition, context)
            for parser in parsers:
                creation = parser.parse(definition)
                if creation is None or creation == []:
                    continue
                creation_type = parser.get_type()
                if creation_type not in creations:
                    creations[creation_type] = []
                creations[creation_type].extend(creation if type(creation) == list else [creation])

        return creations
