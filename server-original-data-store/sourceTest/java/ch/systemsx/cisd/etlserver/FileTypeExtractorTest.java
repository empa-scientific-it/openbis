/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * @author Piotr Buczek
 */
public class FileTypeExtractorTest extends AbstractFileSystemTestCase
{
    private static final String MAPPING =
            "PDF: pdf, MATLAB: mat, ARCHIVE: zip, XML: eicml fiaml mzxml";

    @DataProvider(name = "mappingTypes")
    protected Object[][] getMappingTypes()
    {
        return new Object[][]
        {
                { "file.pdf", "PDF" },
                { "file.mat", "MATLAB" },
                { "file.zip", "ARCHIVE" },
                { "file.eicml", "XML" },
                { "file.fiaml", "XML" },
                { "file.mzxml", "XML" },
                { "file.abc", "UNKNOWN" },

        };
    }

    private static Properties preparePropertiesWithNoMapping()
    {
        return prepareProperties("");
    }

    private static Properties prepareProperties(String typeMapping)
    {
        Properties result = new Properties();
        result.put(FileTypeExtractor.FILE_TYPES_NAME, typeMapping);
        return result;
    }

    @Test(dataProvider = "mappingTypes")
    public void testMapping1(final String fileName, final String expectedType)
    {
        File file = new File(workingDirectory, fileName);
        FileTypeExtractor extractor = new FileTypeExtractor(prepareProperties(MAPPING));

        FileFormatType extractedType = extractor.getFileFormatType(file);
        assertEquals(expectedType, extractedType.getCode());
    }

    @Test
    public void testDefaultMapping()
    {
        File file = new File(workingDirectory, "file.abc");
        Properties properties = preparePropertiesWithNoMapping();
        FileTypeExtractor extractor = new FileTypeExtractor(properties);
        assertEquals(FileTypeExtractor.DEFAULT_FILE_FORMAT_TYPE, extractor.getFileFormatType(file)
                .getCode());

        String defaultType = "DEF";
        properties.put(FileTypeExtractor.DEFAULT_TYPE_PROPERTY_KEY, defaultType);
        FileTypeExtractor extractor2 = new FileTypeExtractor(properties);
        assertEquals(defaultType, extractor2.getFileFormatType(file).getCode());
    }

    @Test
    public void testDirectoryMapping()
    {
        File file = new File(workingDirectory, "dir1");
        file.mkdir();
        Properties properties = preparePropertiesWithNoMapping();
        FileTypeExtractor extractor = new FileTypeExtractor(properties);
        assertEquals(FileTypeExtractor.DIRECTORY_FILE_FORMAT_TYPE, extractor
                .getFileFormatType(file).getCode());

        String directoryType = "DIR";
        properties.put(FileTypeExtractor.DIRECTORY_TYPE_PROPERTY_KEY, "DIR");
        FileTypeExtractor extractor2 = new FileTypeExtractor(properties);
        assertEquals(directoryType, extractor2.getFileFormatType(file).getCode());
    }
}
