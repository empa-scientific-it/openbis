from copy import deepcopy


class PoiCleaner(object):

    @staticmethod
    def hasProperties(poi_definition, properties_header_row):
        return len(poi_definition) > properties_header_row

    @staticmethod
    def clean_data(xls_definition, row_numbers):
        DEFINITION_TYPE_ROW = row_numbers['DEFINITION_TYPE_ROW']
        DEFINITION_TYPE_CELL = row_numbers['DEFINITION_TYPE_CELL']
        ATTRIBUTES_HEADER_ROW = row_numbers['ATTRIBUTES_HEADER_ROW']
        ATTRIBUTES_VALUES_ROW = row_numbers['ATTRIBUTES_VALUES_ROW']
        PROPERTIES_HEADER_ROW = row_numbers['PROPERTIES_HEADER_ROW']
        PROPERTIES_VALUES_ROW_START = row_numbers['PROPERTIES_VALUES_ROW_START']

        definition = deepcopy(xls_definition)
        PoiCleaner.delete_row_numbers(definition)
        '''
            First row can only have definition type, rest is trimmed. Rules for cleaning are the following:
            1. Header rows cannot have empty cells
            2. Values that do not have corresponding headers are removed.
            3. All attributes and properties should have corresponding value in values row when header exists.
                   If there's no corresponding value, None is inserted.
            4. Headers to lowercase
        '''
        definition[DEFINITION_TYPE_ROW] = {0: definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]}

        if ATTRIBUTES_HEADER_ROW is not None:
            PoiCleaner.delete_empty_cells_from(definition, ATTRIBUTES_HEADER_ROW)
            if ATTRIBUTES_VALUES_ROW is not None:
                PoiCleaner.delete_cells_if_no_header(definition, ATTRIBUTES_HEADER_ROW, ATTRIBUTES_VALUES_ROW)
                PoiCleaner.create_cells_if_no_value_but_header_exists(definition, ATTRIBUTES_HEADER_ROW,
                                                                      ATTRIBUTES_VALUES_ROW)
            definition[ATTRIBUTES_HEADER_ROW] = PoiCleaner.dict_values_to_lowercase(definition[ATTRIBUTES_HEADER_ROW])

        if PROPERTIES_HEADER_ROW is not None and PoiCleaner.hasProperties(definition, PROPERTIES_HEADER_ROW):
            PoiCleaner.delete_empty_cells_from(definition, PROPERTIES_HEADER_ROW)
            if PROPERTIES_VALUES_ROW_START is not None:
                for property_value_row_num in range(PROPERTIES_VALUES_ROW_START, len(definition)):
                    PoiCleaner.delete_cells_if_no_header(definition, PROPERTIES_HEADER_ROW, property_value_row_num)
                    PoiCleaner.create_cells_if_no_value_but_header_exists(definition, PROPERTIES_HEADER_ROW,
                                                                          property_value_row_num)
            definition[PROPERTIES_HEADER_ROW] = PoiCleaner.dict_values_to_lowercase(definition[PROPERTIES_HEADER_ROW])

        return definition

    @staticmethod
    def delete_row_numbers(definitions):
        for defintion in definitions:
            del defintion['row number']

    @staticmethod
    def delete_empty_cells_from(definition, row_number):
        header_cell_cols_to_pop = []
        for cell_col, cell in definition[row_number].items():
            if cell is None or cell == '':
                header_cell_cols_to_pop.append(cell_col)

        for cell_col in header_cell_cols_to_pop:
            del definition[row_number][cell_col]

        return definition

    @staticmethod
    def delete_cells_if_no_header(definition, header_row_number, value_row_number):
        values_cell_cols_to_pop = []
        for cell_col in definition[value_row_number]:
            if cell_col not in definition[header_row_number]:
                values_cell_cols_to_pop.append(cell_col)

        for cell_col in values_cell_cols_to_pop:
            del definition[value_row_number][cell_col]

        return definition

    @staticmethod
    def create_cells_if_no_value_but_header_exists(definition, header_row_number, value_row_number):
        values_cell_cols_to_insert = []
        for cell_col in definition[header_row_number]:
            if cell_col not in definition[value_row_number]:
                values_cell_cols_to_insert.append(cell_col)

        for cell_col in values_cell_cols_to_insert:
            definition[value_row_number][cell_col] = None

        return definition

    @staticmethod
    def dict_values_to_lowercase(row):
        return dict((k, v.lower()) for (k, v) in row.items())
