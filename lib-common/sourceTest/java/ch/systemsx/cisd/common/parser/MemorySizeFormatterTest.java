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

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class MemorySizeFormatterTest
{

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testFormatNegative()
    {
        MemorySizeFormatter.format(-1);
    }

    @Test
    public void testFormatZero()
    {
        testFormat(0, "0");
    }

    @Test
    public void testFormatBytesLowerBound()
    {
        testFormat(1, "1b");
    }

    @Test
    public void testFormatBytesUpperBound()
    {
        testFormat(FileUtils.ONE_KB - 1, "1023b");
    }

    @Test
    public void testFormatKilobytesLowerBound()
    {
        testFormat(FileUtils.ONE_KB, "1k");
    }

    @Test
    public void testFormatKilobytesUpperBound()
    {
        testFormat(FileUtils.ONE_MB - 1, "1024k");
    }

    @Test
    public void testFormatKilobytesWithFractionShown()
    {
        testFormat((long) (FileUtils.ONE_KB * 1023.5), "1023.5k");
    }

    @Test
    public void testFormatKilobytesWithFractionRoundedUp()
    {
        testFormat((long) (FileUtils.ONE_KB * 1023.95), "1024k");
    }

    @Test
    public void testFormatKilobytesWithFractionRoundedDown()
    {
        testFormat((long) (FileUtils.ONE_KB * 1024.05), "1m");
    }

    @Test
    public void testFormatMegabytesLowerBound()
    {
        testFormat(FileUtils.ONE_MB, "1m");
    }

    @Test
    public void testFormatMegabytesUpperBound()
    {
        testFormat(FileUtils.ONE_GB - 1, "1024m");
    }

    @Test
    public void testFormatMegabytesWithFractionShown()
    {
        testFormat((long) (FileUtils.ONE_MB * 1023.5), "1023.5m");
    }

    @Test
    public void testFormatMegabytesWithFractionRoundedUp()
    {
        testFormat((long) (FileUtils.ONE_MB * 1023.95), "1024m");
    }

    @Test
    public void testFormatMegabytesWithFractionRoundedDown()
    {
        testFormat((long) (FileUtils.ONE_MB * 1024.05), "1g");
    }

    @Test
    public void testFormatGigabytesLowerBound()
    {
        testFormat(FileUtils.ONE_GB, "1g");
    }

    @Test
    public void testFormatGigabytesUpperBound()
    {
        testFormat(FileUtils.ONE_GB * FileUtils.ONE_KB - 1, "1024g");
    }

    @Test
    public void testFormatGigabytesWithFractionShown()
    {
        testFormat((long) (FileUtils.ONE_GB * 1023.5), "1023.5g");
    }

    @Test
    public void testFormatGigabytesWithFractionRoundedUp()
    {
        testFormat((long) (FileUtils.ONE_GB * 1023.95), "1024g");
    }

    @Test
    public void testFormatGigabytesWithFractionRoundedDown()
    {
        testFormat((long) (FileUtils.ONE_GB * 1024.05), "1t");
    }

    @Test
    public void testFormatTerabytesLowerBound()
    {
        testFormat(FileUtils.ONE_GB * FileUtils.ONE_KB, "1t");
    }

    @Test
    public void testFormatTerabytesNoUpperBound()
    {
        testFormat(FileUtils.ONE_GB * FileUtils.ONE_KB * FileUtils.ONE_KB * 2, "2048t");
    }

    @Test
    public void testFormatTerabytesWithFractionShown()
    {
        testFormat((long) (FileUtils.ONE_GB * FileUtils.ONE_KB * 1023.5), "1023.5t");
    }

    @Test
    public void testFormatTerabytesWithFractionRoundedUp()
    {
        testFormat((long) (FileUtils.ONE_GB * FileUtils.ONE_KB * 1023.95), "1024t");
    }

    @Test
    public void testFormatTerabytesWithFractionRoundedDown()
    {
        testFormat((long) (FileUtils.ONE_GB * FileUtils.ONE_KB * 1024.05), "1024t");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testParseNegative()
    {
        MemorySizeFormatter.parse("-1");
    }

    @Test
    public void testParseZero()
    {
        testParse(0, "0");
    }

    @Test
    public void testParseWithoutUnit()
    {
        testParse(1, "1");
    }

    @Test
    public void testParseWithByteUnit()
    {
        testParse(1, "1b", "1B", "1 b", "1  B");
    }

    @Test
    public void testParseWithKilobyteUnit()
    {
        testParse(FileUtils.ONE_KB, "1k", "1K", "1 k", "1  K");
    }

    @Test
    public void testParseWithMegabyteUnit()
    {
        testParse(FileUtils.ONE_MB, "1m", "1M", "1 m", "1  M");
    }

    @Test
    public void testParseWithGigabyteUnit()
    {
        testParse(FileUtils.ONE_GB, "1g", "1G", "1 g", "1  G");
    }

    @Test
    public void testParseWithTerabyteUnit()
    {
        testParse(FileUtils.ONE_GB * FileUtils.ONE_KB, "1t", "1T", "1 t", "1  T");
    }

    private void testFormat(long bytes, String str)
    {
        assertEquals(MemorySizeFormatter.format(bytes), str);
    }

    private void testParse(long bytes, String... strs)
    {
        for (String str : strs)
        {
            assertEquals(MemorySizeFormatter.parse(str), bytes);
        }
    }

}
