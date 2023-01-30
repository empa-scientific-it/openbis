/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.clientnonjs;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;

/**
 * @author Tomasz Pylak
 */
public class URLListEncoderTest extends AssertJUnit
{
    @DataProvider(name = "lists")
    protected Object[][] getLists()
    {
        return new Object[][]
        {
                { new String[]
                { "a +:\"\\b", "bobo" } },
                { new String[]
                { "a ,,", ",, b" } },
                { new String[]
                { "a,,b ", " ", " c,d,e" } },

        };
    }

    @Test(dataProvider = "lists")
    public void testItemListEncoding(String[] items)
    {
        String encoded = URLListEncoder.encodeItemList(items, false);
        String[] decoded = URLListEncoder.decodeItemList(encoded, false);
        assertEquals(items.length, decoded.length);
        for (int i = 0; i < items.length; i++)
        {
            assertEquals(items[i], decoded[i]);
        }
    }
}
