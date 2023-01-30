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

package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import static ch.systemsx.cisd.openbis.systemtest.TypedTableAssertions.assertColumn;
import static ch.systemsx.cisd.openbis.systemtest.TypedTableAssertions.assertTable;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFormatTypeGridTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * A headless system test having the same functionality as {@link FileFormatTypeGridTest}.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups = "system test")
public class FileFormatTypeHeadlessTest extends SystemTestCase
{
    private static final int FILE_TYPES_IN_DB = 8;

    @BeforeMethod
    public final void setUp()
    {
        logIntoCommonClientService();
    }

    @Test
    public void testListFileFormats()
    {
        DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>>();
        TypedTableResultSet<FileFormatType> tableResultSet =
                commonClientService.listFileTypes(criteria);

        assertTable(tableResultSet).hasNumberOfRows(FILE_TYPES_IN_DB);
        assertColumn(tableResultSet, "CODE").containsValues("TIFF", "XML", "HDF5");
    }

    @Test
    public void testRegisterNewFileType()
    {

        FileFormatType fft = new FileFormatType();
        fft.setCode("FFT-CODE");
        fft.setDescription("FFT-DESC");

        commonClientService.registerFileType(fft);

        DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>>();
        TypedTableResultSet<FileFormatType> tableResultSet =
                commonClientService.listFileTypes(criteria);

        Row expectedRow =
                new Row().withCell("CODE", fft.getCode()).withCell("DESCRIPTION",
                        fft.getDescription());

        assertTable(tableResultSet).hasNumberOfRows(FILE_TYPES_IN_DB + 1);
        assertTable(tableResultSet).containsRow(expectedRow);

    }

}
