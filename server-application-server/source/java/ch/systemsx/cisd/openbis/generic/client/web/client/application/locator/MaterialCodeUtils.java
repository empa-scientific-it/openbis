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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.io.UnsupportedEncodingException;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;

/**
 * Utility methods for handling of material codes.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialCodeUtils
{
    /**
     * IMPORTANT: The method will work correctly *if* the material codes in the openBIS database do not contain the character '%'.
     * <p>
     * This is an attempt to work-around browser incompatibilities. Basically, all browsers have different ways of handling url decoding of their
     * history tokens.
     * <p>
     * For example, in the current version of Chrome a URL parameter '%28c%3AC%29' will be decoded as '(c%3AC)' instead of the expected '(c:C)'.
     * <p>
     * Firefox has yet another way of dealing with decoding of history tokens. The issue is explained in
     * http://stackoverflow.com/questions/2334312/using-gwt-history-to-pass-parameters
     * <p>
     */
    public static String decode(String materialCode)
    {
        return decodeWithoutExceptions(materialCode);
    }

    public static String[] decodeList(String materialCodesList)
    {
        String[] materialCodes = URLListEncoder.decodeItemList(materialCodesList);

        for (int i = 0; i < materialCodes.length; i++)
        {
            // decode the material codes once again.
            materialCodes[i] = decode(materialCodes[i]);
        }
        return materialCodes;
    }

    /**
     * Assumes GWT has done a preliminary decoding pass-through, but not all characters have been decoded successfully.
     * <p>
     * The implementation is based on the java.net.URLDecoder.decode and uses UTF-8 encoding by default.
     */
    private static String decodeWithoutExceptions(String s)
    {
        StringBuilder sb = new StringBuilder();
        int numChars = s.length();
        int pos = 0;
        while (pos < numChars)
        {
            char ch = s.charAt(pos);
            switch (ch)
            {
                case '%':

                    try
                    {
                        int i = 0;
                        int tmpPos = pos;
                        byte[] bytes = new byte[(numChars - pos) / 3];
                        while (((tmpPos + 2) < numChars) && (ch == '%'))
                        {
                            bytes[i++] =
                                    (byte) Integer
                                            .parseInt(s.substring(tmpPos + 1, tmpPos + 3), 16);
                            tmpPos += 3;
                            if (tmpPos < numChars)
                                ch = s.charAt(tmpPos);
                        }

                        // A trailing, incomplete byte encoding such as
                        // "%x" will cause an exception to be thrown
                        if ((tmpPos < numChars) && (ch == '%'))
                        {
                            // ignore and continue decoding
                            pos++;
                            sb.append('%');
                        } else
                        {
                            pos = tmpPos;
                            sb.append(new String(bytes, 0, i, "UTF-8"));
                        }
                    } catch (NumberFormatException nfe)
                    {
                        // GWT has already escaped something here
                        // Do not throw any errors, skip '%' and continue decoding.
                        sb.append('%');
                    } catch (UnsupportedEncodingException uee)
                    {
                        // will never be thrown
                    }

                    break;

                default:
                    pos++;
                    sb.append(ch);
                    break;
            }

        }
        return sb.toString();
    }

}
