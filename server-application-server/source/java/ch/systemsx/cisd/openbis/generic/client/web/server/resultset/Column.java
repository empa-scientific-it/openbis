/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;

final class Column
{
    private final GridCustomColumn columnDefinition;

    private List<PrimitiveValue> values;

    Column(GridCustomColumn columnDefinition)
    {
        this.columnDefinition = columnDefinition;
    }

    GridCustomColumnInfo getInfo()
    {
        return CachedResultSetManager.translate(columnDefinition);
    }

    boolean hasSameExpression(GridCustomColumn column)
    {
        return columnDefinition.getExpression().equals(column.getExpression());
    }

    public void setValues(List<PrimitiveValue> values)
    {
        this.values = values;
    }

    public List<PrimitiveValue> getValues()
    {
        return values;
    }
}