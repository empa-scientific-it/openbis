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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.Collections;
import java.util.List;

/**
 * Group of columns all with the same property value. This is used by jython.
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 *
 * @author Franz-Josef Elmer
 */
public class ColumnGroup
{
    private final String propertyValue;

    private final List<Object> values;

    ColumnGroup(String propertyValue, List<Object> values)
    {
        this.propertyValue = propertyValue;
        this.values = Collections.unmodifiableList(values);
    }

    /**
     * Returns the property value of this group.
     */
    public String propertyValue()
    {
        return propertyValue;
    }

    /**
     * Returns the values of all columns of this group.
     */
    public List<Object> values()
    {
        return values;
    }

}
