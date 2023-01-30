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
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;

interface IColumnCalculator
{
    /**
     * Calculates the values of the specified custom column definition by using specified data and specified column definitions. In case of an error
     * the column value is an error message.
     * 
     * @param errorMessagesAreLong if <code>true</code> a long error message will be created.
     */
    public <T> List<PrimitiveValue> evalCustomColumn(List<T> data,
            GridCustomColumn customColumn, Set<IColumnDefinition<T>> availableColumns,
            boolean errorMessagesAreLong);
}