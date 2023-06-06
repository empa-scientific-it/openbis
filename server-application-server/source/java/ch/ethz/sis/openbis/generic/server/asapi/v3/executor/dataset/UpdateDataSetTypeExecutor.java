/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IUpdateEntityTypePropertyTypesExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateDataSetTypeExecutor
        extends AbstractUpdateEntityTypeExecutor<DataSetTypeUpdate, DataSetTypePE>
        implements IUpdateDataSetTypeExecutor
{
    @Autowired
    private IDataSetTypeAuthorizationExecutor authorizationExecutor;
    
    @Autowired
    private IUpdateDataSetTypePropertyTypesExecutor updateDataSetTypePropertyTypesExecutor;

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    protected void checkTypeSpecificFields(DataSetTypeUpdate update)
    {
    }

    @Override
    protected void updateSpecific(DataSetTypePE type, DataSetTypeUpdate update)
    {
        type.setMainDataSetPattern(getNewValue(update.getMainDataSetPattern(), type.getMainDataSetPattern()));
        type.setMainDataSetPath(getNewValue(update.getMainDataSetPath(), type.getMainDataSetPath()));
        type.setDeletionDisallow(getNewValue(update.isDisallowDeletion(), type.isDeletionDisallow()));
        updateMetaData(type, update);
    }

    @Override
    protected IUpdateEntityTypePropertyTypesExecutor<DataSetTypeUpdate, DataSetTypePE> getUpdateEntityTypePropertyTypeExecutor()
    {
        return updateDataSetTypePropertyTypesExecutor;
    }

    @Override
    protected void checkAccess(IOperationContext context, IEntityTypeId id, DataSetTypePE entity)
    {
        authorizationExecutor.canUpdate(context);
    }

    private void updateMetaData(DataSetTypePE type, DataSetTypeUpdate update)
    {
        Map<String, String> metaData = new HashMap<>();
        if(type.getMetaData() != null) {
            metaData.putAll(type.getMetaData());
        }
        ListUpdateValue.ListUpdateActionSet<?> lastSetAction = null;
        AtomicBoolean metaDataChanged = new AtomicBoolean(false);
        for (ListUpdateValue.ListUpdateAction<Object> action : update.getMetaData().getActions())
        {
            if (action instanceof ListUpdateValue.ListUpdateActionAdd<?>)
            {
                addTo(metaData, action, metaDataChanged);
            } else if (action instanceof ListUpdateValue.ListUpdateActionRemove<?>)
            {
                for (String key : (Collection<String>) action.getItems())
                {
                    metaDataChanged.set(true);
                    metaData.remove(key);
                }
            } else if (action instanceof ListUpdateValue.ListUpdateActionSet<?>)
            {
                lastSetAction = (ListUpdateValue.ListUpdateActionSet<?>) action;
            }
        }
        if (lastSetAction != null)
        {
            metaData.clear();
            addTo(metaData, lastSetAction, metaDataChanged);
        }
        if (metaDataChanged.get())
        {
            type.setMetaData(metaData.isEmpty() ? null : metaData);
        }
    }

    private void addTo(Map<String, String> metaData, ListUpdateValue.ListUpdateAction<?> lastSetAction, AtomicBoolean metaDataChanged)
    {
        Collection<Map<String, String>> maps = (Collection<Map<String, String>>) lastSetAction.getItems();
        for (Map<String, String> map : maps)
        {
            if (!map.isEmpty())
            {
                metaDataChanged.set(true);
                metaData.putAll(map);
            }
        }
    }

}
