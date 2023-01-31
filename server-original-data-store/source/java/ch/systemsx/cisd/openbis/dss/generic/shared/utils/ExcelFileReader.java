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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Reader of tabular data from excel files.
 * 
 * @author Piotr Buczek
 */
public class ExcelFileReader
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExcelFileReader.class);

    private final boolean ignoreComments;

    private final Workbook workbook;

    public ExcelFileReader(Workbook workbook, boolean ignoreComments)
    {
        this.ignoreComments = ignoreComments;
        this.workbook = workbook;
    }

    public List<String[]> readLines() throws IOException
    {
        Sheet sheet = workbook.getSheetAt(0);
        return loadLines(sheet, ignoreComments);
    }

    public List<String[]> readLines(int sheetIndex) throws IOException
    {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        return loadLines(sheet, ignoreComments);
    }

    public List<String[]> readLines(String sheetName) throws IOException
    {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null)
        {
            throw new UserFailureException("Couldn't find sheet named " + sheetName);
        }
        return loadLines(sheet, ignoreComments);
    }

    // if the line starts with this character and comments should be ignored, the line is ignored
    private static final String COMMENT = "#";

    /**
     * Uses file extension to figure out if given <var>file</var> is supported Excel file.
     * 
     * @return <code>true</code> if the <var>file</var> is an XSL or XLSX Excel file
     */
    public static boolean isExcelFile(File file)
    {
        return FilenameUtils.isExtension(file.getName().toLowerCase(), new String[]
        { "xls", "xlsx" });
    }

    /**
     * @return {@link Workbook} of an Excel <var>file</var>
     * @throws IOException if an I/O problem occurs
     * @throws IllegalArgumentException if the <var>file</var> is a format that is not supported
     */
    public static Workbook getExcelWorkbook(File file) throws IOException, IllegalArgumentException
    {
        final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        final FileInputStream stream = new FileInputStream(file);
        try
        {
            if ("xls".equals(extension))
            {
                POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
                return new HSSFWorkbook(poifsFileSystem);
            } else if ("xlsx".equals(extension))
            {
                return new XSSFWorkbook(stream);
            } else
            {
                throw new IllegalArgumentException(
                        "Expected an Excel file with 'xls' or 'xlsx' extension, got "
                                + file.getName());
            }
        } finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

    //
    // helper methods
    //

    private static boolean isComment(Row row)
    {
        return row.getCell(0) != null && row.getCell(0).toString().startsWith(COMMENT);
    }

    private static List<String[]> loadLines(Sheet sheet, boolean ignoreComments) throws IOException
    {
        final List<String[]> lines = new ArrayList<String[]>();

        // NOTE: the following code is pretty ugly because poi API is very limited
        int headerSize = extractMaxColumnIndex(sheet) + 1;
        for (Row row : sheet)
        {
            if (ignoreComments && isComment(row))
            {
                continue; // ignore lines with comments
            } else
            {
                String[] line = new String[headerSize];
                for (Cell cell : row)
                {
                    String value = extractCellValue(cell);
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(extractCellPosition(cell) + ": " + value);
                    }
                    line[cell.getColumnIndex()] = value;
                }
                lines.add(line);
            }
        }

        if (operationLog.isDebugEnabled())
        {
            for (String[] line : lines)
            {
                operationLog.debug(Arrays.toString(line));
            }
        }

        return lines;
    }

    private static int extractMaxColumnIndex(Sheet sheet)
    {
        int maxIndex = 0;
        for (Row row : sheet)
        {
            maxIndex = Math.max(maxIndex, extractMaxColumnIndex(row));
        }
        return maxIndex;
    }

    private static int extractMaxColumnIndex(Row row)
    {
        int maxColumnIndex = 0;
        for (Cell cell : row)
        {
            maxColumnIndex = Math.max(maxColumnIndex, cell.getColumnIndex());
        }
        return maxColumnIndex;
    }

    private static String extractCellValue(Cell cell)
    {
        switch (cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
                return "BLANK";
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                return Double.toString(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_FORMULA:
                throw new UserFailureException(
                        "Excel formulas are not supported but one was found in cell "
                                + extractCellPosition(cell));
            case Cell.CELL_TYPE_ERROR:
                throw new UserFailureException("There is an error in cell "
                        + extractCellPosition(cell));
            default:
                throw new UserFailureException("Unknown data type of cell "
                        + extractCellPosition(cell));
        }
    }

    /** @return cell coordinates in Excel style (e.g. D5) */
    private static String extractCellPosition(Cell cell)
    {
        String col = CellReference.convertNumToColString(cell.getColumnIndex());
        String row = "" + (cell.getRowIndex() + 1);
        return col + row;
    }

    //
    // for testing
    //

    public static void main(String[] args) throws Exception
    {
        String filename =
                "/Users/kaloyane/cisd/train-tasks/2011-07-08-LIMBUD/Sample_template_modified.xlsx";
        // String filename = args[0];
        File file = new File(filename);
        Workbook wb = getExcelWorkbook(file);
        ExcelFileReader helper = new ExcelFileReader(wb, false);
        List<String[]> lines = helper.readLines();
        for (String[] line : lines)
        {
            System.err.println(Arrays.toString(line));
        }
    }
}
