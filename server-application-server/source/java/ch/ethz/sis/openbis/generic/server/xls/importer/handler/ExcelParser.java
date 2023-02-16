/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.openbis.generic.server.xls.importer.handler;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelParser
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ExcelParser.class);

    public static List<List<List<String>>> parseExcel(byte xls[])
    {
        List<List<List<String>>> lines = new ArrayList<>();

        try
        {
            Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(xls));

            for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); ++sheetIndex)
            {
                Sheet sheet = wb.getSheetAt(sheetIndex);
                List<List<String>> sheetLines = new ArrayList<>();

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); ++rowIndex)
                {
                    Row row = sheet.getRow(rowIndex);

                    List<String> columns = new ArrayList<>();

                    if (row != null)
                    {
                        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); ++cellIndex)
                        {
                            Cell cell = row.getCell(cellIndex);
                            if (cell != null)
                            {
                                String value = extractCellValue(cell, sheetIndex, rowIndex, cellIndex);
                                columns.add(value);
                            } else
                            {
                                columns.add(null);
                            }
                        }
                    }

                    sheetLines.add(columns);
                }
                lines.add(sheetLines);
            }

        } catch (Exception e)
        {
            throw new UserFailureException(e.getMessage());
        }

        return lines;
    }

    private static String extractCellValue(Cell cell, int sheet, int row, int column)
    {
        String position = "[ sheet = " + sheet + ", row = " + row + ", column = " + column + "]";
        switch (cell.getCellTypeEnum())
        {
            case BLANK:
                return "";
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                double numeric = cell.getNumericCellValue();
                if (Math.ceil(numeric) == Math.floor(numeric))
                {
                    return Integer.toString((int) numeric);
                } else
                {
                    return Double.toString(numeric);
                }
            case STRING:
                return cell.getStringCellValue().trim();
            case FORMULA:
                throw new ch.systemsx.cisd.common.exceptions.UserFailureException(
                        "Excel formulas are not supported but one was found in cell " + position
                );
            case ERROR:
                throw new ch.systemsx.cisd.common.exceptions.UserFailureException("There is an error in cell " + position);
            default:
                throw new ch.systemsx.cisd.common.exceptions.UserFailureException("Unknown data type of cell " + position);
        }
    }
}
