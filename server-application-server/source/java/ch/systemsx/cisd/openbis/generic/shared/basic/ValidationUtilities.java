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

import java.util.Arrays;

/**
 * Utility class to be used both on client and server side for field validation.
 * 
 * @author Piotr Buczek
 */
public class ValidationUtilities
{

    /** A helper class for external hyperlink validation. */
    public static class HyperlinkValidationHelper
    {
        // A complete regexp for hyperlink may be very complex like this one:
        // http://internet.ls-la.net/folklore/url-regexpr.html
        // We should rather use sth simple for "safe" hyperlink - with no <>()[]
        /** a simple regular expression for "safe" hyperlinks */
        private static final String HYPERLINK_REGEXP = "[^<>()\\[\\]]*";

        private static final String[] HYPERLINK_VALID_PROTOCOLS =
        { "http://", "https://", "ftp://" };

        /** @return does given <var>string</var> start with a valid external hyperlink protocol */
        public static final boolean isProtocolValid(String string)
        {
            for (String protocol : HYPERLINK_VALID_PROTOCOLS)
            {
                if (string.indexOf(protocol) == 0)
                {
                    return true;
                }
            }
            return false;
        }

        /** @return does given <var>string</var> contain a hyperlink value in a proper format */
        public static final boolean isFormatValid(String string)
        {
            return string.matches(HyperlinkValidationHelper.HYPERLINK_REGEXP);
        }

        public static final String getValidProtocolsAsString()
        {
            return Arrays.toString(HYPERLINK_VALID_PROTOCOLS);
        }

    }

}
