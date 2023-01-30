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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores distinct values which could be found in one grid column.
 * 
 * @author Tomasz Pylak
 */
public class ColumnDistinctValues implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 1L;

    private String columnIdentifier;

    // sorted alphabetically
    private List<String> distinctValues;

    public ColumnDistinctValues(String columnIdentifier, List<String> distinctValues)
    {
        this.columnIdentifier = columnIdentifier;
        this.distinctValues = distinctValues;
        Collections.sort(distinctValues);
    }

    public String getColumnIdentifier()
    {
        return columnIdentifier;
    }

    public List<String> getDistinctValues()
    {
        return distinctValues;
    }

    // GWT only
    @SuppressWarnings("unused")
    private ColumnDistinctValues()
    {
    }
}
