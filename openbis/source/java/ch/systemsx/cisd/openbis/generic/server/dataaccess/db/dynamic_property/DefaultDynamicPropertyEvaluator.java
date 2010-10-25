/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyValueValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PropertyValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.DynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * A default {@link IDynamicPropertyEvaluator}.
 * 
 * @author Piotr Buczek
 */
final class DefaultDynamicPropertyEvaluator implements IDynamicPropertyEvaluator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DefaultDynamicPropertyEvaluator.class);

    private static String ID_PROPERTY_NAME = "id";

    private final static Map<Class<? extends IEntityInformationWithPropertiesHolder>, EntityKind> entityKindsByClass;

    static
    {
        entityKindsByClass =
                new HashedMap<Class<? extends IEntityInformationWithPropertiesHolder>, EntityKind>();
        entityKindsByClass.put(SamplePE.class, EntityKind.SAMPLE);
        entityKindsByClass.put(ExperimentPE.class, EntityKind.EXPERIMENT);
        entityKindsByClass.put(MaterialPE.class, EntityKind.MATERIAL);
        entityKindsByClass.put(ExternalDataPE.class, EntityKind.DATA_SET);
    }

    private static IPropertyValueValidator validator = new PropertyValidator();

    private final int batchSize;

    DefaultDynamicPropertyEvaluator(final int batchSize)
    {
        assert batchSize > -1 : "Batch size can not be negative.";
        this.batchSize = batchSize;
    }

    //
    // IDynamicPropertyEvaluator
    //

    public final <T extends IEntityInformationWithPropertiesHolder> List<Long> doEvaluateProperties(
            final Session hibernateSession, final Class<T> clazz) throws DataAccessException
    {
        operationLog.info(String.format("Evaluating dynamic properties for all %ss...",
                clazz.getSimpleName()));

        Transaction transaction = null;

        try
        {
            transaction = hibernateSession.beginTransaction();
            // we evaluate properties of entities in batches loading them in groups restricted by
            // id: [ ids[index], ids[min(index+batchSize, maxIndex))] )
            int index = 0;
            final List<Long> ids = getAllIds(hibernateSession, clazz);
            retainDynamicIds(hibernateSession, clazz, ids);
            final int idsSize = ids.size();
            operationLog.info(String.format("... got %d '%s' ids...", idsSize,
                    clazz.getSimpleName()));
            final int maxIndex = idsSize - 1;
            // need to increment last id because we use 'lt' condition
                if (maxIndex > -1)
        {
                     ids.set(maxIndex, ids.get(maxIndex) + 1);
       }
            while (index < maxIndex)
            {
                final int nextIndex = getNextIndex(index, maxIndex);
                final long minId = ids.get(index);
                final long maxId = ids.get(nextIndex);
                final List<T> results =
                        listEntitiesWithRestrictedId(hibernateSession, clazz, minId, maxId);
                evaluateProperties(hibernateSession, results, clazz);
                index = nextIndex;
                operationLog.info(String.format("%d/%d %ss have been updated...", index, maxIndex,
                        clazz.getSimpleName()));
            }
            transaction.commit();
            operationLog.info(String.format(
                    "Evaluation of dynamic properties for '%s' is complete. "
                            + "%d entities have been updated.", clazz.getSimpleName(), index));
            return ids;
        } catch (Exception e)
        {
            operationLog.error(e.getMessage());
            if (transaction != null)
            {
                transaction.rollback();
            }
        }
        return new ArrayList<Long>();
    }

    public <T extends IEntityInformationWithPropertiesHolder> List<Long> doEvaluateProperties(
            final Session hibernateSession, final Class<T> clazz, final List<Long> ids)
            throws DataAccessException
    {
        operationLog.info(String.format("Evaluating dynamic properties for %ss...",
                clazz.getSimpleName()));

        Transaction transaction = null;
        try
        {
            transaction = hibernateSession.beginTransaction();
            List<Long> dynamicIds = new ArrayList<Long>(ids);
            retainDynamicIds(hibernateSession, clazz, dynamicIds);
            operationLog.info(String.format("... got %d '%s' ids...", dynamicIds.size(),
                    clazz.getSimpleName()));
            // we index entities in batches loading them in groups by id
            final int maxIndex = dynamicIds.size();
            int index = 0;

            while (index < maxIndex)
            {
                final int nextIndex = getNextIndex(index, maxIndex);
                List<Long> subList = dynamicIds.subList(index, nextIndex);
                final List<T> results =
                        listEntitiesWithRestrictedId(hibernateSession, clazz, subList);
                evaluateProperties(hibernateSession, results, clazz);
                index = nextIndex;
                operationLog.info(String.format("%d/%d %ss have been updated...", index + 1, maxIndex + 1,
                        clazz.getSimpleName()));
            }
            transaction.commit();
            operationLog.info(String.format(
                    "Evaluation of dynamic properties for '%s' is complete. "
                            + "%d entities have been updated.", clazz.getSimpleName(), index + 1));
            return dynamicIds;
        } catch (Exception e)
        {
            operationLog.error(e.getMessage());
            if (transaction != null)
            {
                transaction.rollback();
            }
        }
        return new ArrayList<Long>();
    }

    private int getNextIndex(int index, int maxIndex)
    {
        return Math.min(index + batchSize, maxIndex);
    }

    private static final <T extends IEntityInformationWithPropertiesHolder> void evaluateProperties(
            final Session session, final List<T> entities, final Class<T> clazz)
    {
        for (T entity : entities)
        {
            evaluateProperties(session, entity);
        }
        session.flush();
        session.clear();
    }

    private static final <T> List<Long> getAllIds(final Session session, final Class<T> clazz)
    {
        Criteria criteria =
                createCriteria(session, clazz)
                        .setProjection(Projections.property(ID_PROPERTY_NAME)).addOrder(
                                Order.asc(ID_PROPERTY_NAME));
        return list(criteria);
    }

    private static final <T> List<T> listEntitiesWithRestrictedId(final Session session,
            final Class<T> clazz, final long minId, final long maxId)
    {
        Criteria criteria =
                createCriteria(session, clazz).add(Restrictions.ge(ID_PROPERTY_NAME, minId)).add(
                        Restrictions.lt(ID_PROPERTY_NAME, maxId));
        return list(criteria);

    }

    private static final <T> List<T> listEntitiesWithRestrictedId(final Session hibernateSession,
            final Class<T> clazz, final List<Long> ids)
    {
        Criteria criteria =
                createCriteria(hibernateSession, clazz).add(Restrictions.in(ID_PROPERTY_NAME, ids));
        return list(criteria);

    }

    private static final <T> Criteria createCriteria(final Session session, final Class<T> clazz)
    {
        return session.createCriteria(clazz);
    }

    @SuppressWarnings("unchecked")
    private static final <T> List<T> list(final Criteria criteria)
    {
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    private static final <T> List<T> list(final Query query)
    {
        return query.list();
    }

    /** Evaluates properties of specified entity using given session. */
    private static final <T extends IEntityInformationWithPropertiesHolder> void evaluateProperties(
            final Session session, T entity)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating dynamic properties of entity '%s'.",
                    entity));
        }
        for (EntityPropertyPE property : entity.getProperties())
        {
            EntityTypePropertyTypePE etpt = property.getEntityTypePropertyType();
            if (etpt.isDynamic())
            {
                try
                {
                    DynamicPropertyCalculator calculator =
                            new DynamicPropertyCalculator(etpt.getScript().getScript());
                    final IEntityAdaptor entityAdaptor = EntityAdaptorFactory.create(entity);
                    calculator.setEntity(entityAdaptor);
                    final String dynamicValue = calculator.evalAsString();
                    final String validatedValue =
                            validator.validatePropertyValue(etpt.getPropertyType(), dynamicValue);
                    property.setValue(validatedValue);
                } catch (Exception e)
                {
                    String errorMsg = "ERROR: " + e.getMessage();
                    operationLog.info(errorMsg);
                    property.setValue(BasicConstant.ERROR_PROPERTY_PREFIX + errorMsg);
                }
            }
        }
    }

    /**
     * Retains only those elements in the <code>ids</code> list that are ids of entities of given
     * class that have a dynamic property.
     */
    private static <T extends IEntityInformationWithPropertiesHolder> void retainDynamicIds(
            final Session hibernateSession, final Class<T> clazz, final List<Long> ids)
    {
        final EntityKind entityKind = getEntityKind(clazz);
        final List<Long> dynamicEntityIds =
                listEntityIdsWithDynamicProperty(hibernateSession, entityKind);
        ids.retainAll(dynamicEntityIds);
    }

    private final static <T extends IEntityInformationWithPropertiesHolder> EntityKind getEntityKind(
            Class<T> clazz)
    {
        return entityKindsByClass.get(clazz);
    }

    private static List<Long> listEntityIdsWithDynamicProperty(Session hibernateSession,
            EntityKind entityKind) throws DataAccessException
    {
        final String query =
                String.format("SELECT DISTINCT pv.entity.id FROM %s pa join pa.propertyValues pv "
                        + "WHERE pa.dynamic = true", entityKind
                        .getEntityTypePropertyTypeAssignmentClass().getSimpleName());
        final List<Long> list = list(hibernateSession.createQuery(query));

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "LIST: found %s ids of entities of class '%s' assigned to dynamic property.",
                    list.size(), entityKind.getEntityTypePropertyTypeAssignmentClass()
                            .getSimpleName()));
        }
        return list;
    }

}
