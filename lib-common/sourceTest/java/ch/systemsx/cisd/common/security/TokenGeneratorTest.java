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

package ch.systemsx.cisd.common.security;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test cases for the {@link TokenGenerator}.
 * 
 * @author Bernd Rinn
 */
public class TokenGeneratorTest
{

    private static final String TIMESTAMP_FORMAT = TokenGenerator.TIMESTAMP_FORMAT;

    private static boolean isHexString(String s)
    {
        for (int i = 0; i < s.length(); ++i)
        {
            if (Character.isLetterOrDigit(s.charAt(i)) == false)
            {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testTokenIsWellformed()
    {
        final TokenGenerator generator = new TokenGenerator();
        final long now = System.currentTimeMillis();
        final String nowFormatted = String.format(TIMESTAMP_FORMAT, now);
        char separator = '-';
        final String[] token = StringUtils.split(generator.getNewToken(now, separator), separator);
        assertEquals(2, token.length);
        assertEquals(nowFormatted, token[0]);
        assert isHexString(token[1]) : token[1];
    }

    @Test
    public void testTokensAreDifferent()
    {
        final TokenGenerator generator = new TokenGenerator();
        final long now = System.currentTimeMillis();
        char separator = '-';
        final String[] token1 = StringUtils.split(generator.getNewToken(now, separator), separator);
        final String[] token2 = StringUtils.split(generator.getNewToken(now, separator), separator);
        assert token1[0].equals(token2[0]) : "'" + token1[0] + "' != '" + token2[0] + "'";
        assert token1[1].equals(token2[1]) == false : "'" + token1[1] + "' == '" + token2[1] + "'";
    }
}
