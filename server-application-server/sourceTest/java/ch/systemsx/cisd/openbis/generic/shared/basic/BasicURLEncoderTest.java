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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class BasicURLEncoderTest extends AssertJUnit
{
    @DataProvider(name = "chars")
    protected Object[][] getChars()
    {
        return new Object[][]
        {
                /* original char, encoded */
                { ' ', "+" },
                /* Reserved characters: */
                { '$', "%24" },
                { '&', "%26" },
                { '+', "%2B" },
                { ',', "%2C" },
                { '/', "%2F" },
                { ':', "%3A" },
                { ';', "%3B" },
                { '=', "%3D" },
                { '?', "%3F" },
                { '@', "%40" },
                /* Unsafe characters: */
                { '\"', "%22" },
                { '<', "%3C" },
                { '>', "%3E" },
                { '#', "%23" },
                { '%', "%25" },
                { '{', "%7B" },
                { '}', "%7D" },
                { '|', "%7C" },
                { '\\', "%5C" },
                { '^', "%5E" },
                { '~', "%7E" },
                { '[', "%5B" },
                { ']', "%5D" },
                { '`', "%60" },

        };
    }

    @Test(dataProvider = "chars")
    public void testCharEncoding(char c, String expected)
    {
        assertEquals(expected, BasicURLEncoder.encode(c));
    }

    @Test
    public void testStringEncoding()
    {
        assertEquals("a+%2Bb%3Ac%5Cd%22e%22f", BasicURLEncoder.encode("a +b:c\\d\"e\"f"));
        assertEquals("a %2Bb:c%5Cd%22e%22f", BasicURLEncoder.encode("a +b:c\\d\"e\"f", " :"));
        assertEquals("A-a_5.B%28c%3AC%29%2FD%25d", BasicURLEncoder.encode("A-a_5.B(c:C)/D%d"));
    }

}
