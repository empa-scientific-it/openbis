/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.PropertyAssignmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityTypeUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * @author Franz-Josef Elmer
 */
@Component
public abstract class AbstractUpdateEntityTypeExecutor<UPDATE extends IEntityTypeUpdate, TYPE_PE extends EntityTypePE>
        extends AbstractUpdateEntityExecutor<UPDATE, TYPE_PE, IEntityTypeId, EntityTypePermId>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private SetEntityTypeValidationScriptExecutor setEntityTypeValidationScriptExecutor;

    protected abstract EntityKind getDAOEntityKind();

    protected abstract void checkTypeSpecificFields(UPDATE update);
    
    protected abstract IUpdateEntityTypePropertyTypesExecutor<UPDATE, TYPE_PE> getUpdateEntityTypePropertyTypeExecutor();

    @Override
    protected IEntityTypeId getId(UPDATE update)
    {
        return update.getTypeId();
    }

    @Override
    protected EntityTypePermId getPermId(TYPE_PE entity)
    {
        return new EntityTypePermId(entity.getCode(), EntityKindConverter.convert(entity.getEntityKind()));
    }

    @Override
    protected void checkData(IOperationContext context, UPDATE update)
    {
        IEntityTypeId id = update.getTypeId();
        if (id == null)
        {
            throw new UserFailureException("Missing type id.");
        }
        if (id instanceof EntityTypePermId)
        {
            EntityTypePermId entityTypePermId = (EntityTypePermId) id;
            ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind entityKind = EntityKindConverter.convert(getDAOEntityKind());
            if (entityTypePermId.getEntityKind() == null)
            {
                update.setTypeId(new EntityTypePermId(entityTypePermId.getPermId(), entityKind));
            } else if (entityTypePermId.getEntityKind().equals(entityKind) == false)
            {
                throw new UserFailureException("Entity kind " + entityKind + " expected: " + id);
            }
        }
        checkTypeSpecificFields(update);
        
        PropertyAssignmentListUpdateValue propertyAssignments = update.getPropertyAssignments();
        if (propertyAssignments != null)
        {
            EntityTypeUtils.checkPropertyAssignmentCreations(propertyAssignments.getAdded());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<IEntityTypeId, TYPE_PE> map(IOperationContext context, Collection<IEntityTypeId> ids)
    {
        Map<IEntityTypeId, EntityTypePE> map = mapEntityTypeByIdExecutor.map(context, getDAOEntityKind(), ids);
        return (Map<IEntityTypeId, TYPE_PE>) map;
    }

    @Override
    protected List<TYPE_PE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getEntityTypeDAO(getDAOEntityKind()).listEntityTypes();
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<UPDATE, TYPE_PE> batch)
    {
        IPluginIdProvider<UPDATE> pluginIdProvider = new IPluginIdProvider<UPDATE>()
            {
                @Override
                public IPluginId getPluginId(UPDATE pluginIdHolder)
                {
                    return pluginIdHolder.getValidationPluginId().getValue();
                }

                @Override
                public boolean isModified(UPDATE pluginIdHolder)
                {
                    return pluginIdHolder.getValidationPluginId().isModified();
                }
            };
        setEntityTypeValidationScriptExecutor.setValidationPlugin(context, batch, pluginIdProvider, 
                DtoConverters.convertEntityKind(getDAOEntityKind()));
        for (Map.Entry<UPDATE, TYPE_PE> entry : batch.getObjects().entrySet())
        {
            UPDATE update = entry.getKey();
            TYPE_PE type = entry.getValue();
            type.setDescription(getNewValue(update.getDescription(), type.getDescription()));
            updateSpecific(type, update);
        }
    }
    
    protected void updateSpecific(TYPE_PE type, UPDATE update)
    {
        // To be overwritten in sub classes 
    }
    
    protected <T> T getNewValue(FieldUpdateValue<T> fieldUpdateValue, T currentValue)
    {
        return fieldUpdateValue != null && fieldUpdateValue.isModified() ? fieldUpdateValue.getValue() : currentValue;
    }
    
    @Override
    protected void updateAll(IOperationContext context, MapBatch<UPDATE, TYPE_PE> batch)
    {
        getUpdateEntityTypePropertyTypeExecutor().update(context, batch);
    }

    @Override
    protected void save(IOperationContext context, List<TYPE_PE> entities, boolean clearCache)
    {
        for (TYPE_PE entityType : entities)
        {
            daoFactory.getEntityTypeDAO(getDAOEntityKind()).createOrUpdateEntityType(entityType);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, getDAOEntityKind().name() + "_TYPE", null);
    }
}
