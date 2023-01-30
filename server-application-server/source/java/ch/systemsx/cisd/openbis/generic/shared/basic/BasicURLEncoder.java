/*
 * Copyright 2010 ETH Zuerich, CISD
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

/**
 * java.net.URLEncoder version that can be run on the client side
 * 
 * @author Piotr Buczek
 */
public class BasicURLEncoder
{

    public static String encode(String string)
    {
        return encode(string, "");
    }

    public static String encode(String string, String except)
    {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0, n = string.length(); i < n; i++)
        {
            char orginal = string.charAt(i);
            if (except.indexOf(orginal) >= 0)
            {
                buffer.append(orginal);
            } else
            {
                buffer.append(encode(orginal));
            }
        }
        return buffer.toString();
    }

    static String encode(char c)
    {
        if ("-_.*".indexOf(c) >= 0 || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
                || ('0' <= c && c <= '9'))
        {
            return Character.toString(c);
        }
        if (c == ' ')
        {
            return "+";
        }
        int b1 = (c >> 8) & 0xff;
        int b2 = c & 0xff;
        return (b1 == 0 ? "" : encode(b1)) + encode(b2);
    }

    private static String encode(int number)
    {
        return "%"
                + (Integer.toHexString((number >> 4) & 0xf) + Integer.toHexString(number & 0xf))
                        .toUpperCase();
    }

}
