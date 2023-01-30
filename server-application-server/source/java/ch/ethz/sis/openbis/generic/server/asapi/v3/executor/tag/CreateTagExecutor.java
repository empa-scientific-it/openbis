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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class CreateTagExecutor extends AbstractCreateEntityExecutor<TagCreation, MetaprojectPE, TagPermId> implements ICreateTagExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ITagAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISetTagExperimentsExecutor setTagExperimentsExecutor;

    @Autowired
    private ISetTagSamplesExecutor setTagSamplesExecutor;

    @Autowired
    private ISetTagDataSetsExecutor setTagDataSetsExecutor;

    @Autowired
    private ISetTagMaterialsExecutor setTagMaterialsExecutor;

    @Override
    protected List<MetaprojectPE> createEntities(final IOperationContext context, CollectionBatch<TagCreation> batch)
    {
        final List<MetaprojectPE> tags = new LinkedList<MetaprojectPE>();

        new CollectionBatchProcessor<TagCreation>(context, batch)
            {
                @Override
                public void process(TagCreation object)
                {
                    MetaprojectPE tag = new MetaprojectPE();
                    tag.setName(object.getCode());
                    tag.setDescription(object.getDescription());
                    tag.setOwner(context.getSession().tryGetPerson());
                    tag.setPrivate(true);
                    tags.add(tag);
                }

                @Override
                public IProgress createProgress(TagCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return tags;
    }

    @Override
    protected TagPermId createPermId(IOperationContext context, MetaprojectPE entity)
    {
        return new TagPermId(entity.getOwner().getUserId(), entity.getName());
    }

    @Override
    protected void checkData(IOperationContext context, TagCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, MetaprojectPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<TagCreation, MetaprojectPE> batch)
    {
        // nothing to do
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<TagCreation, MetaprojectPE> batch)
    {
        setTagExperimentsExecutor.set(context, batch);
        setTagSamplesExecutor.set(context, batch);
        setTagDataSetsExecutor.set(context, batch);
        setTagMaterialsExecutor.set(context, batch);
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
            daoFactory.getMetaprojectDAO().createOrUpdateMetaproject(entity, entity.getOwner());
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "tag", null);
    }

    @Override
    protected IObjectId getId(MetaprojectPE entity)
    {
        return new TagCode(entity.getCode());
    }

}
