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

package ch.systemsx.cisd.common.shared.basic.string;

import java.util.ArrayList;
import java.util.List;

/**
 * Some utilities for <code>String</code>.
 * <p>
 * Can be used from GWT code.
 * 
 * @author Christian Ribeaud
 */
public final class StringUtils
{

    public static final String EMPTY_STRING = "";

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final static char ESC = '\\';

    private StringUtils()
    {
        // Can not be instantiated
    }

    /**
     * Whether given <var>value</var> is blank or not.
     */
    public static final boolean isBlank(final String value)
    {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Whether given <var>value</var> is blank or not.
     */
    public static final boolean isBlank(final Object value)
    {
        return value == null || value.toString().trim().length() == 0;
    }

    /**
     * Whether given <var>value</var> is not blank.
     */
    public static final boolean isNotBlank(final String value)
    {
        return value != null && value.trim().length() > 0;
    }

    /**
     * Joins the elements using comma as a separator. <br>
     * Example: "a", "b", "c" -> "a, b, c"
     */
    public final static String joinList(final List<String> list)
    {
        return joinList(list, ",");
    }

    /**
     * Joins the elements using comma as a separator. <br>
     * Example: "a", "b", "c" -> "a, b, c"
     */
    public final static String joinList(final List<String> list, String separator)
    {
        if (list == null)
        {
            return null;
        }
        return join(list.toArray(new String[0]), separator);
    }

    /**
     * Joins the elements of the provided array into a single <code>String</code> containing the provided list of elements.
     */
    public final static String join(final Object[] array, final String separator)
    {
        if (array == null)
        {
            return null;
        }
        final String sep = separator == null ? EMPTY_STRING : separator;
        final StringBuilder builder = new StringBuilder();
        boolean start = true;
        for (final Object element : array)
        {
            if (start == false)
            {
                builder.append(sep);
            }
            builder.append(element);
            start = false;
        }
        return builder.toString();
    }

    public final static String capitalize(String word)
    {
        if (isBlank(word))
        {
            return word;
        } else
        {
            return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        }
    }

    public static final String abbreviate(final String value, final int maxLength)
    {
        assert maxLength > 4;
        if (value.length() > maxLength)
        {
            return value.substring(0, maxLength - 3) + "...";
        } else
        {
            return value;
        }
    }

    /**
     * Trims given <var>value</var> to <code>null</code>.
     * 
     * @return <code>null</code> if given <var>value</var> is blank.
     */
    public final static String trimToNull(final String value)
    {
        return isBlank(value) ? null : value.trim();
    }

    /**
     * Returns <code>null</code> if given <var>value</var> is blank.
     */
    public static final String nullIfBlank(final String value)
    {
        return isBlank(value) ? null : value;
    }

    /**
     * Returns an empty string if given <var>stringOrNull</var> is <code>null</code>, otherwise it returns <var>stringOrNull</var> itself.
     */
    public static final String emptyIfNull(final String stringOrNull)
    {
        return stringOrNull == null ? EMPTY_STRING : stringOrNull;
    }

    /**
     * Returns an empty string if given <var>objectOrNull</var> is <code>null</code>, otherwise it returns result of {@link #toString()} on the
     * object.
     */
    public static final String toStringEmptyIfNull(final Object objectOrNull)
    {
        return objectOrNull == null ? EMPTY_STRING : objectOrNull.toString();
    }

    /**
     * Returns null if given <var>objectOrNull</var> is <code>null</code>, otherwise it returns result of {@link #toString()} on the object.
     */
    public static final String toStringOrNull(final Object objectOrNull)
    {
        return objectOrNull == null ? null : objectOrNull.toString();
    }

    /**
     * Returns <var>defaultStr</var>, if <var>str</var> is blank, or otherwise it returns <var>str</var> itself.
     */
    public static final String defaultIfBlank(String str, String defaultStr)
    {
        return isBlank(str) ? defaultStr : str;
    }

    /**
     * Escapes <var>escapedChars</var> characters in specified <var>text</var>.
     */
    public final static String escape(String text, char... escapedChars)
    {
        final char escapeChar = '\\';
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            char ch = text.charAt(i);
            for (char escapedChar : escapedChars)
            {
                if (ch == escapedChar)
                {
                    sb.append(escapeChar);
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Returns the tokens found in <var>str</var>, where tokens are separated by white spaces.
     */
    public static final List<String> tokenize(String str)
    {
        final List<String> tokens = new ArrayList<String>();
        String s = str;
        int len = s.length();
        char endChar;
        boolean quoteMode;
        while (len > 0)
        {
            int idx = 0;
            while (idx < len && s.charAt(idx) == ' ')
            {
                ++idx;
            }
            if (idx > 0)
            {
                s = s.substring(idx);
                len = s.length();
                idx = 0;
            }
            if (len == 0)
            {
                break;
            }
            endChar = s.charAt(0);
            quoteMode = (endChar == '\'' || endChar == '"');
            if (quoteMode == false)
            {
                endChar = ' ';
            } else
            {
                s = s.substring(1);
                --len;
            }
            char c;
            while (idx < len && (c = s.charAt(idx)) != endChar)
            {
                ++idx;
                if (idx < len && c == ESC)
                {
                    ++idx;
                }
            }
            if (idx > 0)
            {
                tokens.add(s.substring(0, idx));
                if (idx == len)
                {
                    break;
                }
                s = s.substring(idx + 1);
                len = s.length();
            }
        }
        return tokens;
    }

    /**
     * Returns <code>true</code> if given <var>regExp</var> could be found in given <var>value</var>.
     * <p>
     * Allowed modifiers are:
     * <ul>
     * <li><i>i</i> - Perform case-insensitive matching
     * <li><i>g</i> - Perform a global match (find all matches rather than stopping after the first match)
     * <li><i>m</i> - Perform multiline matching
     * </ul>
     */
    public static final native boolean matches(final String regExp, final String value,
            final String modifiers)
    /*-{
        var re = new RegExp(regExp, modifiers);
        return value.search(re) > -1;
    }-*/;

    /**
     * Returns <code>true</code> if given <var>regExp</var> could be found in given <var>value</var>.
     */
    public static final native boolean matches(final String regExp, final String value)
    /*-{
        var re = new RegExp(regExp);
        return value.search(re) > -1;
    }-*/;
}
