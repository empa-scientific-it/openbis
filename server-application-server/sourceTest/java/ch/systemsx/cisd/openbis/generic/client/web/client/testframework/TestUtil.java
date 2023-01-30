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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

/**
 * Useful methods.
 * 
 * @author Franz-Josef Elmer
 */
public class TestUtil
{
    public static final String NULL = "<null>";

    private TestUtil()
    {
    }

    /**
     * Checks whether two objects are equal after normalization.
     */
    public static boolean isEqual(final Object object1OrNull, final Object object2OrNull)
    {
        if (object1OrNull == null)
        {
            return object2OrNull == null ? true : false;
        }
        if (object2OrNull == null)
        {
            return false;
        }
        return normalize(object1OrNull).equals(normalize(object2OrNull));
    }

    /**
     * Returns the specified object as a normalised string. Normalization includes trimming, conversion to lower case, stripping off
     * <code>&lt;div&gt;</code> and <code>&lt;a&gt;</code> wrappers.
     * 
     * @return {@link #NULL} if <code>objectOrNull == null</code>
     */
    public static String normalize(final Object objectOrNull)
    {
        if (objectOrNull == null)
        {
            return NULL;
        }
        String value = objectOrNull.toString().toLowerCase().trim();
        while (value.startsWith("<div"))
        {
            value = value.substring(value.indexOf('>') + 1, value.length() - "</div>".length());
        }
        if (value.startsWith("<a"))
        {
            value = value.substring(value.indexOf('>') + 1, value.length() - "</a>".length());
        }
        return value;
    }

}
