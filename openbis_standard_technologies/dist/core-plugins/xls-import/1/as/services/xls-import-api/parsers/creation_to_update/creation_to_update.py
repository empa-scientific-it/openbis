from utils.openbis_utils import create_sample_identifier_string
from .update_parsers import CreationToUpdateParserFactory
from ..definition_to_creation import SampleDefinitionToCreationParser, ScriptDefinitionToCreationParser


class CreationToUpdateParser(object):

    @staticmethod
    def parse(creations_that_exist, existing_elements):
        updates = {}
        for creation_type, creations in creations_that_exist.items():
            parsers = CreationToUpdateParserFactory.get_parsers(creation_type)
            for creation in creations:
                existing_element = get_existing_element_based_on(creation_type, creation, existing_elements[creation_type])
                if existing_element:
                    for parser in parsers:
                        update = parser.parse(creation, existing_element)
                        update_type = parser.get_type()
                        if update_type not in updates:
                            updates[update_type] = []
                        updates[update_type].extend(update if type(update) == list else [update])

        return updates


def get_existing_element_based_on(creation_type, creation, existing_elements):
    if creation_type == SampleDefinitionToCreationParser.type:
        existing_element = list(filter(lambda existing_element: creation.code is not None and create_sample_identifier_string(creation) == existing_element.identifier.identifier, existing_elements))
    else:
        if creation_type == ScriptDefinitionToCreationParser.type:
            attr = 'name'
        else:
            attr = 'code'
        existing_element = list(filter(lambda existing_element: getattr(creation, attr) == getattr(existing_element, attr), existing_elements))
    return None if len(existing_element) == 0 else existing_element[0]

