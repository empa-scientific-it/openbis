/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.IUpdateEntityTypePropertyTypesExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateSampleTypeExecutor
        extends AbstractUpdateEntityTypeExecutor<SampleTypeUpdate, SampleTypePE>
        implements IUpdateSampleTypeExecutor
{
    @Autowired
    private ISampleTypeAuthorizationExecutor authorizationExecutor;
    
    @Autowired
    private IUpdateSampleTypePropertyTypesExecutor updateSampleTypePropertyTypesExecutor;

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected void checkTypeSpecificFields(SampleTypeUpdate update)
    {
    }

    @Override
    protected void checkAccess(IOperationContext context, IEntityTypeId id, SampleTypePE entity)
    {
        authorizationExecutor.canUpdate(context);
    }

    @Override
    protected void updateSpecific(SampleTypePE type, SampleTypeUpdate update)
    {
        type.setGeneratedCodePrefix(getNewValue(update.getGeneratedCodePrefix(), type.getGeneratedCodePrefix()));
        type.setAutoGeneratedCode(getNewValue(update.isAutoGeneratedCode(), type.isAutoGeneratedCode()));
        type.setListable(getNewValue(update.isListable(), type.isListable()));
        type.setSubcodeUnique(getNewValue(update.isSubcodeUnique(), type.isSubcodeUnique()));
        type.setShowParentMetadata(getNewValue(update.isShowParentMetadata(), type.isShowParentMetadata()));
        if (update.isShowContainer() != null && update.isShowContainer().isModified())
        {
            type.setContainerHierarchyDepth(Boolean.TRUE.equals(update.isShowContainer().getValue()) ? 1 : 0);
        }
        if (update.isShowParents() != null && update.isShowParents().isModified())
        {
            type.setGeneratedFromHierarchyDepth(Boolean.TRUE.equals(update.isShowParents().getValue()) ? 1 : 0);
        }
        updateMetaData(type, update);
    }

    @Override
    protected IUpdateEntityTypePropertyTypesExecutor<SampleTypeUpdate, SampleTypePE> getUpdateEntityTypePropertyTypeExecutor()
    {
        return updateSampleTypePropertyTypesExecutor;
    }


    private void updateMetaData(SampleTypePE type, SampleTypeUpdate update) {
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
