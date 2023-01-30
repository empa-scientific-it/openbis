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
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class MapTagByIdExecutor implements IMapTagByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IGetTagIdentifierExecutor getTagIdentifierExecutor;

    @Autowired
    private ITagAuthorizationExecutor authorizationExecutor;

    @SuppressWarnings("unused")
    private MapTagByIdExecutor()
    {
    }

    public MapTagByIdExecutor(IDAOFactory daoFactory, IGetTagIdentifierExecutor getTagCodeExecutor, ITagAuthorizationExecutor authorizationExecutor)
    {
        this.daoFactory = daoFactory;
        this.getTagIdentifierExecutor = getTagCodeExecutor;
        this.authorizationExecutor = authorizationExecutor;
    }

    @Override
    public Map<ITagId, MetaprojectPE> map(IOperationContext context, Collection<? extends ITagId> tagIds)
    {
        return map(context, tagIds, true);
    }

    @Override
    public Map<ITagId, MetaprojectPE> map(IOperationContext context, Collection<? extends ITagId> tagIds, boolean checkAccess)
    {
        if (checkAccess)
        {
            authorizationExecutor.canGet(context);
        }

        Map<ITagId, MetaprojectPE> map = new LinkedHashMap<ITagId, MetaprojectPE>();

        if (tagIds != null)
        {
            for (ITagId tagId : tagIds)
            {
                if (tagId != null)
                {
                    MetaprojectIdentifier identifier = getTagIdentifierExecutor.getIdentifier(context, tagId);
                    MetaprojectPE tag =
                            daoFactory.getMetaprojectDAO()
                                    .tryFindByOwnerAndName(identifier.getMetaprojectOwnerId(), identifier.getMetaprojectName());
                    if (tag != null)
                    {
                        map.put(tagId, tag);
                    }
                }
            }
        }

        return map;
    }

}
