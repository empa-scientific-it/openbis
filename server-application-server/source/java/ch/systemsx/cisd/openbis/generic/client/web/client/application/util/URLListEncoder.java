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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.http.client.URL;

/**
 * Encodes and decodes a list of items allowing to use them e.g. in the URL.
 * 
 * @author Tomasz Pylak
 */
public class URLListEncoder
{
    private final static char ITEM_LIST_SEPARATOR = ',';

    // we need a special character after the list separator, otherwise if we have two items in a
    // list, one which ends with separators and one which starts, then we cannot reconstruct where
    // the split should occur
    private final static char ITEM_LIST_SEPARATOR_NEXT_CHAR = ' ';

    private final static String ESCAPED_SEPARATOR = "" + ITEM_LIST_SEPARATOR + ITEM_LIST_SEPARATOR;

    /**
     * Encodes a list of items by escaping the separator. Decode it back with {@link #decodeItemList}
     */
    public static String encodeItemList(String[] items)
    {
        return encodeItemList(items, true);
    }

    /** @param urlEncoding if true the whole list is encoded to be used as a part of the URL */
    public static String encodeItemList(String[] items, boolean urlEncoding)
    {
        StringBuffer sb = new StringBuffer();
        String escapedItemListSeparator = ESCAPED_SEPARATOR;
        for (String item : items)
        {
            assert item.length() > 0 : "cannot encode an empty item";
            item = item.replaceAll("" + ITEM_LIST_SEPARATOR, escapedItemListSeparator);
            if (sb.length() > 0)
            {
                sb.append(ITEM_LIST_SEPARATOR);
                sb.append(ITEM_LIST_SEPARATOR_NEXT_CHAR);
            }
            sb.append(item);
        }
        String text = sb.toString();
        return urlEncoding ? encode(text) : text;
    }

    /**
     * Decodes a list of items encoded with {@link #encodeItemList} by unescaping the separator.
     */
    public static String[] decodeItemList(String itemsList)
    {
        return decodeItemList(itemsList, true);
    }

    /** @param urlDecoding if true the whole list is URL-decoded before the list items are extracted */
    public static String[] decodeItemList(String itemsList, boolean urlDecoding)
    {
        List<String> list = new ArrayList<String>();
        String decoded = urlDecoding ? decode(itemsList) : itemsList;
        int length = decoded.length();
        int startIx = 0;
        int endIx = startIx;
        while (startIx < length)
        {
            endIx = decoded.indexOf(ITEM_LIST_SEPARATOR, endIx);
            if (endIx != -1 && endIx < length - 1
                    && decoded.charAt(endIx + 1) == ITEM_LIST_SEPARATOR)
            {
                // skip escaped separator, keep looking for a non-escaped one
                endIx += 2;
            } else
            {
                if (endIx == -1)
                {
                    endIx = length;
                }
                String item = decoded.substring(startIx, endIx);
                item = item.replaceAll(ESCAPED_SEPARATOR, "" + ITEM_LIST_SEPARATOR);
                list.add(item);
                startIx = endIx + 2;
                endIx = startIx;
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private static String encode(String text)
    {
        return URL.encodeQueryString(text);
    }

    private static String decode(String text)
    {
        return URL.decodeQueryString(text);
    }
}
