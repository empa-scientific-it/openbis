/*
 * Copyright 2008 ETH Zuerich, CISD
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

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * Helper class to create URL's with parameters. Characters in path, parameter names and values are URL encoded except '0'-'9', 'a'-'z', 'A'-'Z', ':',
 * '/', '.', '*', '-', and '_'. Space character is replaced by '+'.
 * 
 * @author Franz-Josef Elmer
 */
public class URLMethodWithParameters implements IsSerializable
{
    private static final char STARTING_DELIMITER = '?';

    private final StringBuilder builder;

    private char delim = STARTING_DELIMITER;

    /**
     * Create an instance with specified method URL without parameters.
     */
    public URLMethodWithParameters(String methodName)
    {
        builder = new StringBuilder();
        builder.append(BasicURLEncoder.encode(methodName, ":/"));
    }

    /**
     * Adds a parameter with specified name and value (without encoding).
     */
    public void addParameterWithoutEncoding(String parameterName, Object value)
    {
        addParameter(parameterName, value, false);
    }

    /**
     * Adds a parameter with specified name and value (with encoding).
     */
    public void addParameter(String parameterName, Object value)
    {
        addParameter(parameterName, value, true);
    }

    /**
     * Sets '#' as the next separator.
     */
    public void startHistoryToken()
    {
        delim = '#';
    }

    /**
     * Adds a parameter with specified name and value with optional encoding.
     */
    private void addParameter(String parameterName, Object value, boolean withEncoding)
    {
        String maybeEncodedName =
                withEncoding ? BasicURLEncoder.encode(parameterName) : parameterName;
        builder.append(delim).append(maybeEncodedName).append('=');
        if (value != null)
        {
            String maybeEncodedValue =
                    withEncoding ? BasicURLEncoder.encode(value.toString()) : value.toString();
            builder.append(maybeEncodedValue);
        }
        delim = '&';
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

    public String toStringWithoutDelimiterPrefix()
    {
        String string = toString();

        if (StringUtils.isBlank(string))
        {
            // do nothing

        } else if (string.charAt(0) == STARTING_DELIMITER)
        {
            return string.substring(1);

        }
        return string;

    }

    /** Creates HTML which displays an image linking to the given URL (if it is specified). */
    public static String createEmbededImageHtml(String imageURL, String linkURLOrNull, int width,
            int height)
    {
        String linkStart = "", linkEnd = "";
        if (linkURLOrNull != null)
        {
            linkStart = "<a class='link-style' href='" + linkURLOrNull + "' target='_blank'>";
            linkEnd = "</a>";
        }
        StringBuilder builder = new StringBuilder();
        if (width > 0)
        {
            builder.append(" width='").append(width).append("'");
        }
        if (height > 0)
        {
            builder.append(" height='").append(height).append("'");
        }
        return "<div align='center'>" + linkStart + "<img src='" + imageURL + "' alt='" + " image "
                + "'" + builder + "/>" + linkEnd + "</div>";
    }

    /** Creates HTML which displays a link. */
    public static String createEmbededLinkHtml(String linkText, String linkUrl, String linkAlign)
    {
        String linkStart = "", linkEnd = "";
        if (linkUrl != null)
        {
            linkStart = "<a class='link-style' href='" + linkUrl + "' target='_blank'>";
            linkEnd = "</a>";
        }

        String align = linkAlign != null ? "align='" + linkAlign + "'" : "";
        return "<div " + align + ">" + linkStart + linkText + linkEnd + "</div>";
    }
}
