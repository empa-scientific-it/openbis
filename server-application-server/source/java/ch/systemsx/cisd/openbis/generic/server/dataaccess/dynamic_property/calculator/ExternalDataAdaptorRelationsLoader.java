/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorRelationsLoader.IEntityIdsOfTypesLoader;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorRelationsLoader.IEntityTypesLoader;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
public class ExternalDataAdaptorRelationsLoader
{

    private EntityAdaptorRelationsLoader relationsLoader;

    private IExternalDataAdaptorRelationsQuery relationsQuery;

    public ExternalDataAdaptorRelationsLoader(DataPE data, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        this.relationsLoader = new EntityAdaptorRelationsLoader(data.getId(), evaluator, session);
        this.relationsQuery = QueryTool.getManagedQuery(IExternalDataAdaptorRelationsQuery.class);
    }

    public Iterable<IDataAdaptor> parentsOfType(String typeRegexp, final long relationShipTypeId)
    {
        return relationsLoader.entitiesOfType(DataPE.class, typeRegexp, new IEntityTypesLoader()
            {
                @Override
                public List<EntityTypeRecord> loadEntityTypes()
                {
                    return relationsQuery.getDataSetTypes();
                }
            }, new IEntityIdsOfTypesLoader()
            {

                @Override
                public List<Long> loadEntityIdsOfTypes(Long entityId, LongSet entityTypeIds)
                {
                    return relationsQuery.getParentIdsOfTypes(entityId, entityTypeIds, relationShipTypeId);
                }
            });
    }

    public Iterable<IDataAdaptor> childrenOfType(String typeRegexp, final long relationShipTypeId)
    {
        return relationsLoader.entitiesOfType(DataPE.class, typeRegexp, new IEntityTypesLoader()
            {

                @Override
                public List<EntityTypeRecord> loadEntityTypes()
                {
                    return relationsQuery.getDataSetTypes();
                }
            }, new IEntityIdsOfTypesLoader()
            {

                @Override
                public List<Long> loadEntityIdsOfTypes(Long entityId, LongSet entityTypeIds)
                {
                    return relationsQuery.getChildIdsOfTypes(entityId, entityTypeIds, relationShipTypeId);
                }
            });
    }

}
