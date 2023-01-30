/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IDataSetTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IExperimentTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IMaterialTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISampleTypeAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.MapObjectById;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.ListEntityTypeByPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class MapEntityTypeByIdExecutor implements IMapEntityTypeByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IExperimentTypeAuthorizationExecutor experimentTypeAuthorizationExecutor;

    @Autowired
    private ISampleTypeAuthorizationExecutor sampleTypeAuthorizationExecutor;

    @Autowired
    private IDataSetTypeAuthorizationExecutor dataSetTypeAuthorizationExecutor;

    @Autowired
    private IMaterialTypeAuthorizationExecutor materialTypeAuthorizationExecutor;

    @SuppressWarnings("unused")
    private MapEntityTypeByIdExecutor()
    {
    }

    public MapEntityTypeByIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public Map<IEntityTypeId, EntityTypePE> map(IOperationContext context, EntityKind entityKind, Collection<? extends IEntityTypeId> entityTypeIds)
    {
        Collection<IEntityTypeId> entityTypeIdsWithEntityKinds = new ArrayList<IEntityTypeId>();
        Map<IEntityTypeId, IEntityTypeId> entityTypeIdMapping = new IdentityHashMap<IEntityTypeId, IEntityTypeId>();

        if (entityTypeIds != null)
        {
            for (IEntityTypeId entityTypeId : entityTypeIds)
            {
                if (entityTypeId == null)
                {
                    continue;
                } else if (entityTypeId instanceof EntityTypePermId)
                {
                    EntityTypePermId entityTypePermId = (EntityTypePermId) entityTypeId;
                    EntityTypePermId entityTypeIdWithEntityKind = null;

                    if (entityKind == null)
                    {
                        if (entityTypePermId.getEntityKind() == null)
                        {
                            throw new UserFailureException("Entity type entity kind cannot be null");
                        } else
                        {
                            entityTypeIdWithEntityKind = entityTypePermId;
                        }
                    } else
                    {
                        if (entityTypePermId.getEntityKind() == null)
                        {
                            entityTypeIdWithEntityKind = new EntityTypePermId(entityTypePermId.getPermId(), EntityKindConverter.convert(entityKind));
                        } else
                        {
                            if (entityKind.equals(EntityKindConverter.convert(entityTypePermId.getEntityKind())))
                            {
                                entityTypeIdWithEntityKind = entityTypePermId;
                            } else
                            {
                                throw new UserFailureException("Incorrect entity type entity kind. Expected '" + entityKind + "' but was '"
                                        + entityTypePermId.getEntityKind() + "'.");
                            }
                        }
                    }

                    entityTypeIdsWithEntityKinds.add(entityTypeIdWithEntityKind);
                    entityTypeIdMapping.put(entityTypeIdWithEntityKind, entityTypeId);
                } else
                {
                    throw new UnsupportedObjectIdException(entityTypeId);
                }
            }
        }

        checkAccess(context, entityTypeIdsWithEntityKinds);

        List<IListObjectById<? extends IEntityTypeId, EntityTypePE>> listers =
                new LinkedList<IListObjectById<? extends IEntityTypeId, EntityTypePE>>();
        listers.add(new ListEntityTypeByPermId(daoFactory));

        Map<IEntityTypeId, EntityTypePE> mapWithModifiedIds =
                new MapObjectById<IEntityTypeId, EntityTypePE>().map(context, listers, entityTypeIdsWithEntityKinds);

        Map<IEntityTypeId, EntityTypePE> mapWithOriginalIds = new LinkedHashMap<IEntityTypeId, EntityTypePE>();
        for (Map.Entry<IEntityTypeId, EntityTypePE> entry : mapWithModifiedIds.entrySet())
        {
            IEntityTypeId modifiedId = entry.getKey();
            IEntityTypeId originalId = entityTypeIdMapping.get(modifiedId);
            mapWithOriginalIds.put(originalId, entry.getValue());
        }

        return mapWithOriginalIds;
    }

    private void checkAccess(IOperationContext context, Collection<? extends IEntityTypeId> entityTypeIds)
    {
        Set<ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind> entityKinds = new HashSet<>();

        for (IEntityTypeId entityTypeId : entityTypeIds)
        {
            entityKinds.add(((EntityTypePermId) entityTypeId).getEntityKind());
        }

        if (entityKinds.contains(ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT))
        {
            experimentTypeAuthorizationExecutor.canGet(context);
        }

        if (entityKinds.contains(ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE))
        {
            sampleTypeAuthorizationExecutor.canGet(context);
        }

        if (entityKinds.contains(ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET))
        {
            dataSetTypeAuthorizationExecutor.canGet(context);
        }

        if (entityKinds.contains(ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.MATERIAL))
        {
            materialTypeAuthorizationExecutor.canGet(context);
        }
    }

}
