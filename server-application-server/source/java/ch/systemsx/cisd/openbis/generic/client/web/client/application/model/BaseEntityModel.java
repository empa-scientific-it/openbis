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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;

/**
 * The generic model which can be created automatically using the array of {@link IColumnDefinition} .
 * 
 * @author Franz-Josef Elmer
 * @author Tomasz Pylak
 */
public class BaseEntityModel<T> extends SimplifiedBaseModelData
{
    private static final long serialVersionUID = 1L;

    private boolean outdated = false;

    private final Set<String> outdatable = new HashSet<String>();

    /** NOTE: it's assumed that columnDefinitions do not contain custom columns */
    public BaseEntityModel(final GridRowModel<T> entity,
            List<? extends IColumnDefinition<T>> columnDefinitions)
    {
        set(ModelDataPropertyNames.OBJECT, entity.getOriginalObject());

        for (IColumnDefinition<T> column : columnDefinitions)
        {
            String value = column.getValue(entity);
            set(column.getIdentifier(), value);
            if (column instanceof IColumnDefinitionUI<?>)
            {
                set(ModelDataPropertyNames.link(column.getIdentifier()),
                        ((IColumnDefinitionUI<T>) column).tryGetLink(entity.getOriginalObject()));
                if (((IColumnDefinitionUI<?>) column).isDynamicProperty())
                {
                    outdatable.add(column.getIdentifier());
                }
            }
        }
        addCustomColumns(entity);
    }

    private void addCustomColumns(final GridRowModel<T> model)
    {
        for (Entry<String, PrimitiveValue> entry : model.getCalculatedColumnValues().entrySet())
        {
            set(entry.getKey(), entry.getValue());
            outdatable.add(entry.getKey());
        }
    }

    public final T getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }

    public final String tryGetLink(String columnId)
    {
        return get(ModelDataPropertyNames.link(columnId));
    }

    public boolean isOutdated()
    {
        return outdated;
    }

    public void setOutdated(boolean outdated)
    {
        this.outdated = outdated;
    }

    public boolean isOutdatable(String columnName)
    {
        return outdatable.contains(columnName);
    }
}
