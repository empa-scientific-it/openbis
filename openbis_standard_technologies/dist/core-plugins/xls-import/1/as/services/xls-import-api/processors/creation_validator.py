from parsers import PropertyTypeDefinitionToCreationType
from ch.systemsx.cisd.common.exceptions import UserFailureException


def validate_creations(creations):
    '''
        This validator checks for:
         - whether property types with same code are all the same across xls
         - If allowed entries align with creations about to be made

        It throws an exception if this is false.
    '''
    if PropertyTypeDefinitionToCreationType not in creations:
        return

    property_type_creations = creations[PropertyTypeDefinitionToCreationType]
    different_duplicates = {}
    for property_type in property_type_creations:
        for second_property_type in property_type_creations:
            if property_type.code == second_property_type.code:
                difference_info = {}
                attributes_to_check = ['label', 'description', 'dataType', 'internalNameSpace', 'vocabularyId',
                                       'metaData']
                for attribute in attributes_to_check:
                    attribute_of_property_type = getattr(property_type, attribute)
                    attribute_of_second_property_type = getattr(second_property_type, attribute)
                    if attribute_of_property_type != attribute_of_second_property_type:
                        if attribute not in difference_info:
                            difference_info[attribute] = []
                        attribute_pair = [str(attribute_of_property_type), str(attribute_of_second_property_type)]
                        difference_info[attribute].extend(attribute_pair)

                if difference_info:
                    if property_type.code not in different_duplicates:
                        different_duplicates[property_type.code] = {}
                    for key in difference_info:
                        if key not in different_duplicates[property_type.code]:
                            different_duplicates[property_type.code][key] = set(difference_info[key])
                        else:
                            different_duplicates[property_type.code][key].update(difference_info[key])

    if different_duplicates:
        error_msg = "Following property types have ambiguous definition: \n" + \
                    '\n'.join(['Property Code: ' + str(code) + '\n' + '\n'.join(
                        ["Attribute with difference: " + attr_name + ", has following values: " + '\n' + ', '.join(
                            ['"' + str(attribute_value) + '"' for attribute_value in duplicates_attributes])
                         for attr_name, duplicates_attributes in diff_info.items()]) for code, diff_info in
                               different_duplicates.items()]) + '\n'

        raise UserFailureException(error_msg)
