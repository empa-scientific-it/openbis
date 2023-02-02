/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.NumberToTextConverter;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * @author Pawel Glyzewski
 */
public class ExcelRowTokenizer implements ILineTokenizer<Row>
{
    @Override
    public void init()
    {
    }

    @Override
    public String[] tokenize(Row row) throws ParserException
    {
        return tokenizeRow(row);
    }

    public static String[] tokenizeRow(Row row)
    {
        short lastCellNum = row.getLastCellNum();

        if (lastCellNum < 0)
        {
            throw new ParserException("Unreadable data in line " + row.getRowNum());
        }

        String[] line = new String[lastCellNum];
        for (int i = 0; i < line.length; i++)
        {
            line[i] = "";
        }
        for (Cell cell : row)
        {
            String value = extractCellValue(cell).trim();
            line[cell.getColumnIndex()] = value;
        }

        return trimEmptyCells(line);
    }

    private static String[] trimEmptyCells(String[] line)
    {
        int last = -1;

        for (int i = line.length - 1; i >= 0; i--)
        {
            if (false == StringUtils.isBlank(line[i]))
            {
                if (line.length - 1 == i)
                {
                    return line;
                } else
                {
                    last = i;
                    break;
                }
            }
        }

        if (last < 0)
        {
            return new String[0];
        }

        String[] result = new String[last + 1];
        System.arraycopy(line, 0, result, 0, last + 1);
        return result;
    }

    private static String extractCellValue(Cell cell) throws ParserException
    {
        switch (cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return NumberToTextConverter.toText(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_FORMULA:
                throw new ParserException(
                        "Excel formulas are not supported but one was found in cell "
                                + extractCellPosition(cell));
            case Cell.CELL_TYPE_ERROR:
                throw new ParserException("There is an error in cell " + extractCellPosition(cell));
            default:
                throw new ParserException("Unknown data type of cell " + extractCellPosition(cell));
        }
    }

    /** @return cell coordinates in Excel style (e.g. D5) */
    private static String extractCellPosition(Cell cell)
    {
        String col = CellReference.convertNumToColString(cell.getColumnIndex());
        String row = "" + (cell.getRowIndex() + 1);
        return col + row;
    }

    @Override
    public void destroy()
    {
    }
}
