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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

/**
 * Some utilities around <code>String</code>.
 * 
 * @author Christian Ribeaud
 */
public final class StringUtils
{
    public final static String EMPTY = "";

    private StringUtils()
    {
        // Can not be instantiated
    }

    /**
     * Whether given <var>value</var> is blank or not.
     */
    public final static boolean isBlank(final String value)
    {
        return value == null || value.trim().length() == 0;
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
     * Joins the elements of the provided array into a single <code>String</code> containing the
     * provided list of elements.
     */
    public final static String join(final Object[] array, final String separator)
    {
        if (array == null)
        {
            return null;
        }
        final String sep = separator == null ? EMPTY : separator;
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
}
