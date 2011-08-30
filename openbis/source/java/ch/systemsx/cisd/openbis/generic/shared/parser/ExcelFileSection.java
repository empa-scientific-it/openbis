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

package ch.systemsx.cisd.openbis.generic.shared.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Pawel Glyzewski
 */
public class ExcelFileSection
{
    private static final String SECTION_FILE_DEFAULT = "DEFAULT";

    private final Sheet sheet;

    private final int begin;

    private final int end;

    private final String sectionName;

    private ExcelFileSection(Sheet sheet, String sectionName, int begin, int end)
    {
        assert sheet != null;
        this.sectionName = sectionName;
        this.sheet = sheet;
        this.begin = begin;
        this.end = end;
    }

    public String getSectionName()
    {
        return sectionName;
    }

    public Sheet getSheet()
    {
        return sheet;
    }

    public int getBegin()
    {
        return begin;
    }

    public int getEnd()
    {
        return end;
    }

    public static ExcelFileSection createFromInputStream(InputStream stream, String sectionName)
    {
        try
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
            Workbook wb = new HSSFWorkbook(poifsFileSystem);
            Sheet sheet = wb.getSheetAt(0);
            return new ExcelFileSection(sheet, sectionName, 0, sheet.getLastRowNum());
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    public static List<ExcelFileSection> extractSections(InputStream stream)
    {
        List<ExcelFileSection> sections = new ArrayList<ExcelFileSection>();
        try
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
            Workbook wb = new HSSFWorkbook(poifsFileSystem);
            Sheet sheet = wb.getSheetAt(0);
            String sectionName = null;
            Integer begin = null;
            for (Row row : sheet)
            {
                String newSectionName = tryGetSectionName(row);
                if (newSectionName != null)
                {
                    if (sectionName != null && begin != null)
                    {
                        if (sectionName.equals(newSectionName))
                        {
                            continue;
                        }
                        if (newSectionName.equals(SECTION_FILE_DEFAULT))
                        {
                            continue;
                        } else
                        {
                            sections.add(new ExcelFileSection(sheet, sectionName, begin, row
                                    .getRowNum()));
                            sectionName = newSectionName;
                            begin = row.getRowNum() + 1;
                        }
                    } else
                    {
                        sectionName = newSectionName;
                        begin = row.getRowNum() + 1;
                    }
                } else if (sectionName == null || begin == null)
                {
                    throw new UserFailureException("Discovered the unnamed section in the file");
                }
                if (row.getRowNum() == sheet.getLastRowNum())
                {
                    sections.add(new ExcelFileSection(sheet, sectionName, begin, row.getRowNum()));
                }
            }
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        } finally
        {
            IOUtils.closeQuietly(stream);
        }
        return sections;
    }

    private static String tryGetSectionName(Row row)
    {
        final String beginSection = "[";
        final String endSection = "]";
        if (row == null || row.getCell(0) == null
                || row.getCell(0).getCellType() != Cell.CELL_TYPE_STRING)
        {
            return null;
        }
        String trimmedCell = row.getCell(0).getStringCellValue();

        if (trimmedCell.startsWith(beginSection) && trimmedCell.endsWith(endSection))
        {
            return trimmedCell.substring(1, trimmedCell.length() - 1);
        } else
        {
            return null;
        }
    }
}
