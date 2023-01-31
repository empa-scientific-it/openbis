/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Test cases for corresponding {@link SimpleTypeExtractor} class.
 * 
 * @author Christian Ribeaud
 */
public class SimpleTypeExtractorTest
{

    private final static Properties createProperties()
    {
        Properties props = new Properties();
        props.put(SimpleTypeExtractor.FILE_FORMAT_TYPE_KEY, "F");
        props.put(SimpleTypeExtractor.LOCATOR_TYPE_KEY, "L");
        props.put(SimpleTypeExtractor.DATA_SET_TYPE_KEY, "O");
        props.put(SimpleTypeExtractor.PROCESSOR_TYPE_KEY, "P");
        props.put(SimpleTypeExtractor.IS_MEASURED_KEY, "false");
        return props;
    }

    @Test
    public final void testConstructor()
    {
        SimpleTypeExtractor extractor = new SimpleTypeExtractor(new Properties());
        assertEquals(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE, extractor
                .getFileFormatType(null).getCode());
        assertEquals(LocatorType.DEFAULT_LOCATOR_TYPE_CODE, extractor.getLocatorType(null)
                .getCode());
        assertEquals(DataSetTypeCode.UNKNOWN.getCode(), extractor.getDataSetType(null).getCode());
        assertEquals(null, extractor.getProcessorType(null));
        assertEquals(true, extractor.isMeasuredData(null));

        extractor = new SimpleTypeExtractor(createProperties());
        assertEquals("F", extractor.getFileFormatType(null).getCode());
        assertEquals("L", extractor.getLocatorType(null).getCode());
        assertEquals("O", extractor.getDataSetType(null).getCode());
        assertEquals("P", extractor.getProcessorType(null));
        assertEquals(false, extractor.isMeasuredData(null));
    }
}