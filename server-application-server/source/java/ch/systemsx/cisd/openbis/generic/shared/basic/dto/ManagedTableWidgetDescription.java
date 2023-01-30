/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedOutputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedOutputWidgetType;

/**
 * {@link IManagedOutputWidgetDescription} implementation for tables
 * 
 * @author Piotr Buczek
 */
public class ManagedTableWidgetDescription implements IManagedOutputWidgetDescription
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private TableModel tableModel;

    public ManagedTableWidgetDescription()
    {
    }

    public TableModel getTableModel()
    {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel)
    {
        this.tableModel = tableModel;
    }

    //
    // IManagedWidgetDescription
    //

    @Override
    public ManagedOutputWidgetType getManagedOutputWidgetType()
    {
        return ManagedOutputWidgetType.TABLE;
    }

}
