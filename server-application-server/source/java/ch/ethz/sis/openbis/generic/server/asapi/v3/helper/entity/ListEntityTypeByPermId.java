/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public class ListEntityTypeByPermId extends AbstractListObjectById<EntityTypePermId, EntityTypePE>
{

    private IDAOFactory daoFactory;

    public ListEntityTypeByPermId(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public Class<EntityTypePermId> getIdClass()
    {
        return EntityTypePermId.class;
    }

    @Override
    public EntityTypePermId createId(EntityTypePE entityType)
    {
        return new EntityTypePermId(entityType.getPermId(), EntityKindConverter.convert(entityType.getEntityKind()));
    }

    @Override
    public List<EntityTypePE> listByIds(IOperationContext context, List<EntityTypePermId> ids)
    {
        List<EntityTypePermId> materialTypeIds = new ArrayList<>();
        List<EntityTypePermId> experimentTypeIds = new ArrayList<>();
        List<EntityTypePermId> sampleTypeIds = new ArrayList<>();
        List<EntityTypePermId> dataSetTypeIds = new ArrayList<>();

        for (EntityTypePermId id : ids)
        {
            EntityKind entityKind = EntityKindConverter.convert(id.getEntityKind());

            if (EntityKind.MATERIAL.equals(entityKind))
            {
                materialTypeIds.add(id);
            } else if (EntityKind.EXPERIMENT.equals(entityKind))
            {
                experimentTypeIds.add(id);
            } else if (EntityKind.SAMPLE.equals(entityKind))
            {
                sampleTypeIds.add(id);
            } else if (EntityKind.DATA_SET.equals(entityKind))
            {
                dataSetTypeIds.add(id);
            }
        }

        List<EntityTypePE> results = new ArrayList<>();
        results.addAll(listByIds(context, materialTypeIds, EntityKind.MATERIAL));
        results.addAll(listByIds(context, experimentTypeIds, EntityKind.EXPERIMENT));
        results.addAll(listByIds(context, sampleTypeIds, EntityKind.SAMPLE));
        results.addAll(listByIds(context, dataSetTypeIds, EntityKind.DATA_SET));
        return results;
    }

    private List<EntityTypePE> listByIds(IOperationContext context, List<EntityTypePermId> ids, EntityKind entityKind)
    {
        if (ids.isEmpty())
        {
            return Collections.emptyList();
        }

        IEntityTypeDAO entityTypeDAO = daoFactory.getEntityTypeDAO(entityKind);

        List<String> permIds = new LinkedList<String>();

        for (EntityTypePermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return entityTypeDAO.tryToFindEntityTypeByCodes(permIds);
    }

}
