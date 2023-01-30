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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Jakub Straszewski
 */
@Component
public class UpdateMaterialExecutor extends AbstractUpdateEntityExecutor<MaterialUpdate, MaterialPE, IMaterialId, MaterialPermId> implements
        IUpdateMaterialExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMaterialAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IUpdateMaterialPropertyExecutor updateMaterialPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Override
    protected IMaterialId getId(MaterialUpdate update)
    {
        return update.getMaterialId();
    }

    @Override
    protected MaterialPermId getPermId(MaterialPE entity)
    {
        return new MaterialPermId(entity.getCode(), entity.getMaterialType().getCode());
    }

    @Override
    protected void checkData(IOperationContext context, MaterialUpdate update)
    {
        if (update.getMaterialId() == null)
        {
            throw new UserFailureException("Sample id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IMaterialId id, MaterialPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<MaterialUpdate, MaterialPE> batch)
    {
        updateTags(context, batch);
        updateMaterialPropertyExecutor.update(context, batch);
    }

    private void updateTags(final IOperationContext context, final MapBatch<MaterialUpdate, MaterialPE> batch)
    {
        new MapBatchProcessor<MaterialUpdate, MaterialPE>(context, batch)
            {
                @Override
                public void process(MaterialUpdate update, MaterialPE entity)
                {
                    if (update.getTagIds() != null && update.getTagIds().hasActions())
                    {
                        updateTagForEntityExecutor.update(context, entity, update.getTagIds());
                    }
                }

                @Override
                public IProgress createProgress(MaterialUpdate update, MaterialPE entity, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(update, entity, "material-tag", objectIndex, totalObjectCount);
                }
            };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<MaterialUpdate, MaterialPE> batch)
    {
    }

    @Override
    protected Map<IMaterialId, MaterialPE> map(IOperationContext context, Collection<IMaterialId> ids)
    {
        return mapMaterialByIdExecutor.map(context, ids);
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
        DataAccessExceptionTranslator.throwException(e, EntityKind.SAMPLE.getLabel(), EntityKind.SAMPLE);
    }

}
