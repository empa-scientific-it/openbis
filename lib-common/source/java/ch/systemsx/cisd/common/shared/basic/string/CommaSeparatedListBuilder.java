/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.List;

/**
 * Builder of a comma-separated list of stringified objects.
 * <p>
 * Can be used from GWT code.
 *
 * @author Franz-Josef Elmer
 */
public class CommaSeparatedListBuilder
{
    /**
     * Creates comma-separated list from specified objects.
     */
    public static String toString(Object... objects)
    {
        CommaSeparatedListBuilder listBuilder = new CommaSeparatedListBuilder();
        for (Object object : objects)
        {
            listBuilder.append(object);
        }
        return listBuilder.toString();
    }

    /**
     * Creates comma-separated list from specified objects.
     */
    public static String toString(List<?> objects)
    {
        CommaSeparatedListBuilder listBuilder = new CommaSeparatedListBuilder();
        for (Object object : objects)
        {
            listBuilder.append(object);
        }
        return listBuilder.toString();
    }

    private final StringBuilder builder = new StringBuilder();

    /**
     * Appends specified object.
     */
    public void append(Object object)
    {
        if (builder.length() > 0)
        {
            builder.append(", ");
        }
        builder.append(object);
    }

    /**
     * Returns comma-separated list of appended objects.
     */
    @Override
    public String toString()
    {
        return builder.toString();
    }
}
