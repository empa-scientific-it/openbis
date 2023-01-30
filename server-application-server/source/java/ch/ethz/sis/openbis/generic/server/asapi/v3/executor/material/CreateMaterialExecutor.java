/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Jakub Straszewski
 */
@Component
public class CreateMaterialExecutor extends AbstractCreateEntityExecutor<MaterialCreation, MaterialPE, MaterialPermId> implements
        ICreateMaterialExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMaterialAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISetMaterialTypeExecutor setMaterialTypeExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Override
    protected List<MaterialPE> createEntities(final IOperationContext context, CollectionBatch<MaterialCreation> batch)
    {
        final List<MaterialPE> materials = new LinkedList<MaterialPE>();

        new CollectionBatchProcessor<MaterialCreation>(context, batch)
            {
                @Override
                public void process(MaterialCreation object)
                {
                    MaterialPE material = new MaterialPE();
                    material.setCode(object.getCode());
                    material.setRegistrator(context.getSession().tryGetPerson());
                    materials.add(material);
                }

                @Override
                public IProgress createProgress(MaterialCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return materials;
    }

    @Override
    protected MaterialPermId createPermId(IOperationContext context, MaterialPE entity)
    {
        return new MaterialPermId(entity.getCode(), entity.getEntityType().getCode());
    }

    @Override
    protected void checkData(IOperationContext context, MaterialCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        SpaceIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, MaterialPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<MaterialCreation, MaterialPE> batch)
    {
        setMaterialTypeExecutor.set(context, batch);
        updateEntityPropertyExecutor.update(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<MaterialCreation, MaterialPE> batch)
    {
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<MaterialCreation, MaterialPE> entry : batch.getObjects().entrySet())
        {
            MaterialCreation creation = entry.getKey();
            MaterialPE entity = entry.getValue();
            tagMap.put(entity, creation.getTagIds());
        }

        addTagToEntityExecutor.add(context, tagMap);
    }

    @Override
    protected List<MaterialPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getMaterialDAO().listMaterialsById(ids);
    }

    @Override
    protected void save(IOperationContext context, List<MaterialPE> entities, boolean clearCache)
    {
        daoFactory.getMaterialDAO().createOrUpdateMaterials(entities);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.MATERIAL.getLabel(), EntityKind.MATERIAL);
    }

    @Override
    protected IObjectId getId(MaterialPE entity)
    {
        return new MaterialPermId(entity.getCode(), entity.getMaterialType().getCode());
    }

}
