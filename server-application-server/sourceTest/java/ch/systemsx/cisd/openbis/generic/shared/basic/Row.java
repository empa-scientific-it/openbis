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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;

/**
 * Class which contains all expected cell values to be checked by {@link CheckTableCommand}. It uses a fluent API approach for its methods to prepare
 * expectations.
 * 
 * @author Franz-Josef Elmer
 */
public class Row
{
    private final Map<String, Object> columnIDValuesMap = new HashMap<String, Object>();

    /**
     * Prepares this with an expected cell value.
     * 
     * @param columnID ID of the column to which the cell belongs.
     * @param value Expected value.
     */
    public Row withCell(final String columnID, final Object value)
    {
        columnIDValuesMap.put(columnID, value);
        return this;
    }

    public final Map<String, Object> getColumnIDValuesMap()
    {
        return Collections.unmodifiableMap(columnIDValuesMap);
    }

    @Override
    public String toString()
    {
        return columnIDValuesMap.toString();
    }
}
