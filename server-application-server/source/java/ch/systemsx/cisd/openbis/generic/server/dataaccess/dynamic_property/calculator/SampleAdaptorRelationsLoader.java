/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
public class SampleAdaptorRelationsLoader
{

    private EntityAdaptorRelationsLoader relationsLoader;

    private ISampleAdaptorRelationsQuery relationsQuery;

    public SampleAdaptorRelationsLoader(SamplePE sample, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        this.relationsLoader = new EntityAdaptorRelationsLoader(sample.getId(), evaluator, session);
        this.relationsQuery = QueryTool.getManagedQuery(ISampleAdaptorRelationsQuery.class);
    }

    public Iterable<ISampleAdaptor> parentsOfType(String typeRegexp)
    {
        return relationsLoader.entitiesOfType(SamplePE.class, typeRegexp, new IEntityTypesLoader()
            {
                @Override
                public List<EntityTypeRecord> loadEntityTypes()
                {
                    return relationsQuery.getSampleTypes();
                }
            }, new IEntityIdsOfTypesLoader()
            {

                @Override
                public List<Long> loadEntityIdsOfTypes(Long entityId, LongSet entityTypeIds)
                {
                    return relationsQuery.getParentIdsOfTypes(entityId, entityTypeIds);
                }
            });
    }

    public Iterable<ISampleAdaptor> childrenOfType(String typeRegexp)
    {
        return relationsLoader.entitiesOfType(SamplePE.class, typeRegexp, new IEntityTypesLoader()
            {

                @Override
                public List<EntityTypeRecord> loadEntityTypes()
                {
                    return relationsQuery.getSampleTypes();
                }
            }, new IEntityIdsOfTypesLoader()
            {

                @Override
                public List<Long> loadEntityIdsOfTypes(Long entityId, LongSet entityTypeIds)
                {
                    return relationsQuery.getChildIdsOfTypes(entityId, entityTypeIds);
                }
            });
    }

}
