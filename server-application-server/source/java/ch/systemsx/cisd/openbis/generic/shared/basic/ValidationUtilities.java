/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
            try {
                new URL(string).toURI();
                return true;
            } catch (MalformedURLException e) {
                return false;
            } catch (URISyntaxException e) {
                return false;
            }
        }

        public static final String getValidProtocolsAsString()
        {
            return Arrays.toString(HYPERLINK_VALID_PROTOCOLS);
        }

    }

}
