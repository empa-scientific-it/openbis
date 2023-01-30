/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author anttil
 */
public class WorkBookWriter
{

    private HSSFWorkbook workbook;

    private HashMap<String, Integer> rows;

    private String fileName;

    public WorkBookWriter(String fileName)
    {
        this.workbook = new HSSFWorkbook();
        this.rows = new HashMap<String, Integer>();
        this.fileName = fileName;
    }

    public Sheet createSheet(String name)
    {
        Sheet sheet = workbook.createSheet(name);
        rows.put(name, 0);
        return sheet;
    }

    public void write(Sheet sheet, String... values)
    {
        Integer rowNumber = rows.get(sheet.getSheetName());
        Row row = sheet.createRow(rowNumber);
        int cellNum = 0;
        for (String value : values)
        {
            row.createCell(cellNum).setCellValue(value);
            cellNum++;
        }
        rowNumber++;
        rows.put(sheet.getSheetName(), rowNumber);
    }

    public File writeToDisk()
    {
        try
        {
            File dir = new File("targets/tmp");
            dir.mkdirs();

            if (fileName == null)
            {
                fileName = UUID.randomUUID().toString() + "_general_batch_import.xls";
            } else
            {
                fileName = fileName + "_general_batch_import.xls";
            }

            File file = new File(dir, fileName);

            if (file.exists())
            {
                file.delete();
            }

            file.createNewFile();
            OutputStream stream = new FileOutputStream(file);

            workbook.write(stream);

            stream.flush();
            stream.close();
            return file;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
