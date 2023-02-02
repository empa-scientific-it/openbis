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

package ch.systemsx.cisd.common.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.security.MD5ChecksumCalculator;

/**
 * Tests for {@link MD5ChecksumCalculator}.
 * 
 * @author Tomasz Pylak
 */
public class MD5ChecksumCalculatorTest
{
    @Test
    public void testCalculateForString()
    {
        String md5 = MD5ChecksumCalculator.calculate("Dataset_200903031234987-321");
        AssertJUnit.assertEquals("9b0351271044c1a843d51811984968cd", md5);

        md5 = MD5ChecksumCalculator.calculate("x");
        AssertJUnit.assertEquals("9dd4e461268c8034f5c8564e155c67a6", md5);
    }

    @Test
    public void testCalculateForStream() throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100000; i++)
        {
            builder.append(i);
        }
        String string = builder.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());
        String streamChecksum = MD5ChecksumCalculator.calculate(inputStream);
        String stringChecksum = MD5ChecksumCalculator.calculate(string);
        AssertJUnit.assertEquals(stringChecksum, streamChecksum);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testCalculateForEmptyString()
    {
        MD5ChecksumCalculator.calculate("");
    }

}
