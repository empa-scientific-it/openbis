/*
 * Copyright 2015 ETH Zuerich, CISD
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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class PercentFormatterTest
{

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testParseNegative()
    {
        PercentFormatter.parse("-1%");
    }

    @Test
    public void testParse()
    {
        testParse(15, "15%", "15 %");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testParseIncorrect()
    {
        PercentFormatter.parse("xyz");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testFormatNegative()
    {
        PercentFormatter.format(-1);
    }

    @Test
    public void testFormat()
    {
        testFormat(15, "15%");
    }

    private void testFormat(int percent, String str)
    {
        assertEquals(PercentFormatter.format(percent), str);
    }

    private void testParse(int percent, String... strs)
    {
        for (String str : strs)
        {
            assertEquals(PercentFormatter.parse(str), percent);
        }
    }

}
