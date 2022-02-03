from org.apache.commons.lang3 import StringUtils
from org.apache.poi.ss.usermodel import CellType
from org.apache.poi.ss.usermodel import WorkbookFactory
from org.apache.poi.ss.util import NumberToTextConverter
from java.io import ByteArrayInputStream


class ExcelToPoiParser(object):

    @staticmethod
    def parse(excel_byte_array):
        workbook = WorkbookFactory.create(ByteArrayInputStream(excel_byte_array))

        definitions = []
        for sheet in workbook.sheetIterator():
            i = 0
            row_count = sheet.getLastRowNum() + 1

            while i < row_count:
                definition_rows = []

                while i < row_count and not ExcelToPoiParser.is_row_empty(sheet.getRow(i)):
                    row = sheet.getRow(i)
                    definition_rows.append(row)
                    i += 1

                # skip all empty rows
                while i < row_count and ExcelToPoiParser.is_row_empty(sheet.getRow(i)):
                    i += 1

                definitions.append(definition_rows)

        workbook.close()

        definitions_stripped = []
        for definition in definitions:
            definition_stripped = []
            for row in definition:
                row_stripped = {'row number' : row.getRowNum() + 1}
                for cell in row.cellIterator():
                    cell_value = ExcelToPoiParser.extract_string_value_from(cell)
                    cell_value = cell_value if cell_value != '' else None
                    cell_col = cell.getColumnIndex()
                    row_stripped[cell_col] = cell_value
                definition_stripped.append(row_stripped)
            definitions_stripped.append(definition_stripped)

        return definitions_stripped

    @staticmethod
    def extract_string_value_from(cell):
        cell_type = cell.getCellTypeEnum()
        if cell_type == CellType.BLANK:
            return ""
        if cell_type == CellType.BOOLEAN:
            return str(cell.getBooleanCellValue())
        if cell_type == CellType.NUMERIC:
            return NumberToTextConverter.toText(cell.getNumericCellValue())
        if cell_type == CellType.STRING:
            return cell.getStringCellValue()
        if cell_type == CellType.FORMULA:
            raise SyntaxError("Excel formulas are not supported but one was found in cell %s" % cell.getAddress())
        if cell_type == CellType.ERROR:
            raise SyntaxError("There is an error in cell %s" % cell.getAddress())

        raise SyntaxError("Unknown data type of cell %s" % cell.getAddress())

    @staticmethod
    def is_row_empty(row):
        if row is None:
            return True
        if row.getLastCellNum <= 0:
            return True

        return all(ExcelToPoiParser.is_cell_empty(cell) for cell in row.cellIterator())

    @staticmethod
    def is_cell_empty(cell):
        if cell is not None and cell.getCellType() != CellType.BLANK and StringUtils.isNotBlank(cell.toString()):
            return False
        return True
