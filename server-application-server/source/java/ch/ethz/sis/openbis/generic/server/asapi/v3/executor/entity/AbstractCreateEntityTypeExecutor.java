/*
 * Copyright 2017 ETH Zuerich, CISD
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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.create.IEntityTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityTypeUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public abstract class AbstractCreateEntityTypeExecutor<CREATION extends IEntityTypeCreation, TYPE extends EntityType, TYPE_PE extends EntityTypePE>
        extends AbstractCreateEntityExecutor<CREATION, TYPE_PE, EntityTypePermId>
{

    @Autowired
    protected IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private CreatePropertyAssignmentsExecutor createPropertyAssignmentsExecutor;
    
    @Autowired
    private SetEntityTypeValidationScriptExecutor setEntityTypeValidationScriptExecutor;

    protected abstract ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind getPEEntityKind();

    protected abstract EntityKind getDAOEntityKind();

    protected abstract TYPE newType();

    protected abstract void checkTypeSpecificFields(CREATION creation);

    protected abstract void fillTypeSpecificFields(TYPE type, CREATION creation);

    protected abstract void defineType(IOperationContext context, TYPE type);

    @Override
    protected List<TYPE_PE> createEntities(final IOperationContext context, CollectionBatch<CREATION> typeCreations)
    {
        final List<TYPE_PE> typePEs = new LinkedList<TYPE_PE>();

        new CollectionBatchProcessor<CREATION>(context, typeCreations)
            {
                @Override
                public void process(CREATION typeCreation)
                {
                    TYPE_PE typePE = createType(context, typeCreation);
                    typePEs.add(typePE);

                    String entityTypeCode = typeCreation.getCode();
                    List<PropertyAssignmentCreation> propertyAssignments = typeCreation.getPropertyAssignments();
                    createPropertyAssignmentsExecutor.createPropertyAssignments(context, entityTypeCode, 
                            propertyAssignments, getPEEntityKind());
                }

                @Override
                public IProgress createProgress(CREATION object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return typePEs;
    }

    @SuppressWarnings("unchecked")
    private TYPE_PE createType(IOperationContext context, CREATION typeCreation)
    {
        TYPE type = newType();
        type.setCode(typeCreation.getCode());
        type.setDescription(typeCreation.getDescription());

        fillTypeSpecificFields(type, typeCreation);

        defineType(context, type);

        return (TYPE_PE) daoFactory.getEntityTypeDAO(getDAOEntityKind()).tryToFindEntityTypeByCode(typeCreation.getCode());
    }

    @Override
    protected EntityTypePermId createPermId(IOperationContext context, TYPE_PE entity)
    {
        return new EntityTypePermId(entity.getCode(), EntityKindConverter.convert(entity.getEntityKind()));
    }

    @Override
    protected void checkData(IOperationContext context, CREATION creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        checkTypeSpecificFields(creation);

        EntityTypeUtils.checkPropertyAssignmentCreations(creation.getPropertyAssignments());
    }

    @Override
    protected IObjectId getId(TYPE_PE entity)
    {
        // nothing to do
        return null;
    }

    @Override
    protected void checkAccess(IOperationContext context, TYPE_PE entity)
    {
        // nothing to do
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<CREATION, TYPE_PE> batch)
    {
        IPluginIdProvider<CREATION> pluginIdProvider = new IPluginIdProvider<CREATION>()
            {
                @Override
                public IPluginId getPluginId(CREATION pluginIdHolder)
                {
                    return pluginIdHolder.getValidationPluginId();
                }

                @Override
                public boolean isModified(CREATION pluginIdHolder)
                {
                    return true;
                }
            };
        setEntityTypeValidationScriptExecutor.setValidationPlugin(context, batch, pluginIdProvider, getPEEntityKind());
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<CREATION, TYPE_PE> batch)
    {
        // nothing to do
    }

    @Override
    protected List<TYPE_PE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getEntityTypeDAO(getDAOEntityKind()).listEntityTypes();
    }

    @Override
    protected void save(IOperationContext context, List<TYPE_PE> entities, boolean clearCache)
    {
        // nothing to do
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, getDAOEntityKind().name() + "_TYPE", null);
    }

}
