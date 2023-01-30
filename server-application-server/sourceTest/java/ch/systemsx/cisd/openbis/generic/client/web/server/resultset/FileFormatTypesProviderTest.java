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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * @author Kaloyan Enimanev
 */
public class FileFormatTypesProviderTest extends AbstractProviderTest
{
    private FileFormatType fft1;

    private FileFormatType fft2;

    @BeforeMethod
    public final void setUpExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(server).listFileFormatTypes(SESSION_TOKEN);

                    fft1 = new FileFormatType("SH");
                    fft1.setDescription("Bash scripts");

                    fft2 = new FileFormatType("PY");
                    fft2.setDescription("Python scripts");

                    will(returnValue(Arrays.asList(fft1, fft2)));
                }
            });
    }

    @Test
    public void testBrowse()
    {
        FileFormatTypesProvider vocabulariesProvider =
                new FileFormatTypesProvider(server, SESSION_TOKEN);
        TypedTableModel<FileFormatType> tableModel = vocabulariesProvider.getTableModel(Integer.MAX_VALUE);

        assertEquals("[CODE, DESCRIPTION]", getHeaderIDs(tableModel).toString());

        List<TableModelRowWithObject<FileFormatType>> rows = tableModel.getRows();
        assertSame(fft1, rows.get(0).getObjectOrNull());
        assertSame(fft2, rows.get(1).getObjectOrNull());

        List<String> expectedValues = Arrays.asList(fft2.getCode(), fft2.getDescription());

        assertEquals(expectedValues.toString(), rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
        context.assertIsSatisfied();
    }

}
