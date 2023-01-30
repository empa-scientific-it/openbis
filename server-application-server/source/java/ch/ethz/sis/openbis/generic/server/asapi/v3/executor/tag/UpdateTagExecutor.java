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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateTagExecutor extends AbstractUpdateEntityExecutor<TagUpdate, MetaprojectPE, ITagId, TagPermId> implements
        IUpdateTagExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ITagAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapTagByIdExecutor mapTagByIdExecutor;

    @Autowired
    private IUpdateTagExperimentsExecutor updateTagExperimentsExecutor;

    @Autowired
    private IUpdateTagSamplesExecutor updateTagSamplesExecutor;

    @Autowired
    private IUpdateTagDataSetsExecutor updateTagDataSetsExecutor;

    @Autowired
    private IUpdateTagMaterialsExecutor updateTagMaterialsExecutor;

    @Override
    protected ITagId getId(TagUpdate update)
    {
        return update.getTagId();
    }

    @Override
    protected TagPermId getPermId(MetaprojectPE entity)
    {
        return new TagPermId(entity.getOwner().getUserId(), entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, TagUpdate update)
    {
        if (update.getTagId() == null)
        {
            throw new UserFailureException("Tag id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, ITagId id, MetaprojectPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<TagUpdate, MetaprojectPE> batch)
    {
        for (Map.Entry<TagUpdate, MetaprojectPE> entry : batch.getObjects().entrySet())
        {
            TagUpdate update = entry.getKey();
            MetaprojectPE tag = entry.getValue();

            if (update.getDescription() != null && update.getDescription().isModified())
            {
                tag.setDescription(update.getDescription().getValue());
            }
        }
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<TagUpdate, MetaprojectPE> batch)
    {
        updateTagExperimentsExecutor.update(context, batch);
        updateTagSamplesExecutor.update(context, batch);
        updateTagDataSetsExecutor.update(context, batch);
        updateTagMaterialsExecutor.update(context, batch);
    }

    @Override
    protected Map<ITagId, MetaprojectPE> map(IOperationContext context, Collection<ITagId> ids)
    {
        return mapTagByIdExecutor.map(context, ids);
    }

    @Override
    protected List<MetaprojectPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getMetaprojectDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<MetaprojectPE> entities, boolean clearCache)
    {
        for (MetaprojectPE entity : entities)
        {
            daoFactory.getMetaprojectDAO().validateAndSaveUpdatedEntity(entity);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "tag", null);
    }

}
