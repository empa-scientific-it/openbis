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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;

/**
 * @author pkupczyk
 */
public class EntityAdaptorRelationsLoader
{

    private Long entityId;

    private IDynamicPropertyEvaluator evaluator;

    private Session session;

    public EntityAdaptorRelationsLoader(Long entityId, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        this.entityId = entityId;
        this.evaluator = evaluator;
        this.session = session;
    }

    public <T> Iterable<T> entitiesOfType(Class<?> entityClass, String entityTypeRegexp,
            IEntityTypesLoader entityTypesLoader, IEntityIdsOfTypesLoader entityIdsOfTypesLoader)
    {
        List<EntityTypeRecord> allTypes = entityTypesLoader.loadEntityTypes();
        LongSet matchingTypeIds = new LongOpenHashSet();

        if (allTypes != null)
        {
            for (EntityTypeRecord type : allTypes)
            {
                if (type.code.matches(entityTypeRegexp))
                {
                    matchingTypeIds.add(type.id);
                }
            }
        }

        if (matchingTypeIds.isEmpty())
        {
            return Collections.emptyList();
        } else
        {
            List<Long> entityIds =
                    entityIdsOfTypesLoader.loadEntityIdsOfTypes(entityId, matchingTypeIds);

            if (entityIds != null && entityIds.size() > 0)
            {
                Criteria criteria = session.createCriteria(entityClass);
                criteria.setFetchSize(10);
                criteria.add(Restrictions.in("id", entityIds));
                ScrollableResults results = criteria.scroll(ScrollMode.FORWARD_ONLY);
                return new EntityAdaptorIterator<T>(results, evaluator, session);
            } else
            {
                return Collections.emptyList();
            }
        }
    }

    public static interface IEntityTypesLoader
    {
        public List<EntityTypeRecord> loadEntityTypes();

    }

    public static interface IEntityIdsOfTypesLoader
    {
        public List<Long> loadEntityIdsOfTypes(Long entityId, LongSet entityTypeIds);

    }

}
