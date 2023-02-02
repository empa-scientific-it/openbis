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

package ch.systemsx.cisd.common.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author pkupczyk
 */
public class ExcelTestUtil
{

    private static final String TEST_FOLDER = "sourceTest/java/";

    public static final Sheet getSheet(Class<?> excelTestClass, String excelFileName)
            throws Exception
    {
        File excelDir =
                new File(TEST_FOLDER
                        + excelTestClass.getPackage().getName().replace('.', '/'));
        File excelFile = new File(excelDir, excelFileName);
        final InputStream stream = new FileInputStream(excelFile);
        try
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
            HSSFWorkbook workbook = new HSSFWorkbook(poifsFileSystem);
            return workbook.getSheetAt(0);
        } finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

}
