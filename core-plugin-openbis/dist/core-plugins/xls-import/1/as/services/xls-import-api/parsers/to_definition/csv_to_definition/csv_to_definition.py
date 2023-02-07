from StringIO import StringIO
import csv


class CsvReaderToDefinitionParser():

    @staticmethod
    def parse(csv_string):
        def is_row_empty(row):
            return not ''.join(row).strip()

        f = StringIO("".join(map(chr, csv_string)))
        reader = csv.reader(f, delimiter=',')
        definitions = []
        definition_rows = []
        previous_row_empty = False
        for row in reader:
            if is_row_empty(row) and previous_row_empty:
                break
            if is_row_empty(row):
                definitions.append(definition_rows)
                definition_rows = []
                previous_row_empty = True
                continue
            previous_row_empty = False
            definition_rows.append({k: v for k, v in enumerate(row) if v != ''})

        return definitions
