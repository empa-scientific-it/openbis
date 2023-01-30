/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.EmptyInterceptor;
import org.hibernate.FetchMode;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.type.Type;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * {@link Interceptor} which reacts to creation and update of entities, and calls the validation script. It is coupled with
 * {@link OpenBISHibernateTransactionManager} with the callback object, as the only way to cancel transaction from this interceptor is to rollback
 * hibernate transaction object. In order to fail the transaction with a {@link UserFailureException} we have to provide information
 * 
 * @author Jakub Straszewski
 */
public class EntityValidationInterceptor extends EmptyInterceptor implements
        IValidationRequestDelegate<INonAbstractEntityAdapter>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private static int BATCH_SIZE = 1000;

    private IDAOFactory daoFactory;

    private final IEntityValidatorFactory entityValidationFactory;

    private final IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public EntityValidationInterceptor(IHibernateTransactionManagerCallback callback,
            IDAOFactory daoFactory, IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.callback = callback;
        this.daoFactory = daoFactory;
        this.entityValidationFactory = entityValidationFactory;
        this.dynamicPropertyCalculatorFactory = dynamicPropertyCalculatorFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
        initializeLists();

        totalEntitiesToValidateCount = 0;

        entitiesValidatedCount = 0;

        isRolledBack = false;
    }

    IHibernateTransactionManagerCallback callback;

    /**
     * Used only to store information about whether some objects are new or not
     */
    Set<EntityIdentifier> newEntities;

    /**
     * Keeps the list of all items that should be validated
     */
    Set<EntityIdentifier> entitiesToValidate;

    /**
     * Keeps the list of all items that have already been validated
     */
    Set<EntityIdentifier> validatedEntities;

    /**
     * WE get information about progress listener, form the caller of updates. Luckily the onFlushDirty and onSave hooks are executed in the same
     * thread as the caller.
     * <p>
     * The beforeTransactionCompletionHook is called in the separate thread, therefore we persist progress listener in a designated variable
     */
    IServiceConversationProgressListener progressListener;

    int totalEntitiesToValidateCount;

    int entitiesValidatedCount;

    /**
     * if true - than it means that at least one validation has failed, and we don't need to do any additional validations
     */
    private boolean isRolledBack;

    private void updateListener()
    {
        progressListener = ServiceConversationsThreadContext.getProgressListener();
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types)
    {
        updateListener();

        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            newEntity((IEntityInformationWithPropertiesHolder) entity);
        }
        return false;
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types)
    {
        updateListener();

        if (entity instanceof IEntityInformationWithPropertiesHolder)
        {
            modifiedEntity((IEntityInformationWithPropertiesHolder) entity);
        }
        return false;
    }

    // @Override
    // public void postFlush(Iterator entities)
    // {
    // updateListener();
    // while (entities.hasNext())
    // {
    // Object entity = entities.next();
    // if (entity instanceof IEntityInformationWithPropertiesHolder)
    // {
    // modifiedEntity((IEntityInformationWithPropertiesHolder) entity);
    // }
    // }
    // }

    private boolean isCached(Session session, EntityIdentifier identifier)
    {
        return ((SessionImpl) session)
                .getEntityUsingInterceptor(new EntityKey(identifier.getId(),
                        ((SessionFactoryImplementor) daoFactory.getSessionFactory())
                                .getEntityPersister(identifier.getEntityClass().getName()))) != null;
    }

    private Collection<EntityIdentifier> cachedEntities(Session session)
    {
        Set<EntityIdentifier> cached = new HashSet<EntityIdentifier>();
        for (EntityIdentifier identifier : entitiesToValidate)
        {
            if (isCached(session, identifier))
            {
                cached.add(identifier);
            }
        }
        return cached;
    }

    @Override
    public void beforeTransactionCompletion(Transaction tx)
    {
        Session session = daoFactory.getSessionFactory().getCurrentSession();
        session.flush();
        for (EntityIdentifier identifier : cachedEntities(session))
        {
            validateEntity(
                    tx,
                    (IEntityInformationWithPropertiesHolder) session.get(
                            identifier.getEntityClass(), identifier.getId()));
            if (isRolledBack)
            {
                return;
            }
        }

        while (entitiesToValidate.size() > 0)
        {
            Set<Long> foundAllIds = new HashSet<Long>();
            for (List<EntityIdentifier> identifiers : identifierBatchOf(BATCH_SIZE))
            {
                List<IEntityInformationWithPropertiesHolder> foundEntities = findEntities(session, identifiers);
                Set<Long> foundIds = getIdsOfFoundEntitiesToBeValidated(foundEntities);
                foundAllIds.addAll(foundIds);
                for (IEntityInformationWithPropertiesHolder entity : foundEntities)
                {
                    if (foundIds.contains(entity.getId()) == false)
                    {
                        continue;
                    }
                    validateEntity(tx, entity);
                    if (isRolledBack)
                    {
                        return;
                    }
                }
            }
            Iterator<EntityIdentifier> iterator = entitiesToValidate.iterator();
            while (iterator.hasNext())
            {
                EntityIdentifier next = iterator.next();
                if (foundAllIds.contains(next.getId()) == false)
                {
                    iterator.remove();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<IEntityInformationWithPropertiesHolder> findEntities(Session session, List<EntityIdentifier> identifiers)
    {
        Criteria criteria = session.createCriteria(identifiers.get(0).getEntityClass());
        criteria.add(Restrictions.in("id", identifiers.stream().map(EntityIdentifier::getId).collect(Collectors.toSet())));
        criteria.setFetchMode("sampleProperties", FetchMode.JOIN);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return (List<IEntityInformationWithPropertiesHolder>) criteria.list();
    }

    private Set<Long> getIdsOfFoundEntitiesToBeValidated(List<IEntityInformationWithPropertiesHolder> foundEntities)
    {
        Set<Long> foundIdsToValidate = new HashSet<>();
        Set<Long> foundIds = foundEntities.stream().map(IIdHolder::getId).collect(Collectors.toSet());
        for (EntityIdentifier entityToValidate : entitiesToValidate)
        {
            if (foundIds.contains(entityToValidate.getId()))
            {
                foundIdsToValidate.add(entityToValidate.getId());
            }
        }
        return foundIdsToValidate;
    }

    private Iterable<List<EntityIdentifier>> identifierBatchOf(final int batchSize)
    {
        final List<EntityIdentifier> ids = new ArrayList<EntityIdentifier>(entitiesToValidate);

        Collections.sort(ids, new Comparator<EntityIdentifier>()
            {
                @Override
                public int compare(EntityIdentifier arg0, EntityIdentifier arg1)
                {
                    return arg0.getKind().toString().compareTo(arg1.getKind().toString());
                }
            });

        return new Iterable<List<EntityIdentifier>>()
            {
                @Override
                public Iterator<List<EntityIdentifier>> iterator()
                {
                    return new Iterator<List<EntityIdentifier>>()
                        {
                            private int index = 0;

                            @Override
                            public boolean hasNext()
                            {
                                return index < ids.size();
                            }

                            @Override
                            public List<EntityIdentifier> next()
                            {
                                if (index >= ids.size())
                                {
                                    return null;
                                }
                                List<EntityIdentifier> list = new ArrayList<EntityIdentifier>();
                                EntityKind kind = ids.get(index).getKind();
                                while (list.size() < batchSize && index < ids.size()
                                        && ids.get(index).getKind().equals(kind))
                                {
                                    list.add(ids.get(index));
                                    index++;
                                }
                                return list;
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    private void validateEntity(Transaction tx, IEntityInformationWithPropertiesHolder entity)
    {
        boolean isNewEntity = newEntities.contains(new EntityIdentifier(entity));

        IEntityValidator entityValidator =
                entityValidationFactory.createEntityValidator(entity.getEntityType(), this);
        if (entityValidator != null)
        {
            try
            {
                validatedEntity(entity);
                validateEntityWithScript(tx, entityValidator, entity, isNewEntity);
            } catch (Throwable e)
            {
                setRollback(tx, entity, " resulted in error. " + e.getMessage());
            }
        } else
        {
            entitiesToValidate.remove(new EntityIdentifier(entity));
        }

    }

    private void validateEntityWithScript(Transaction tx, IEntityValidator entityValidator,
            IEntityInformationWithPropertiesHolder entity, boolean isNewEntity)
    {
        String result = null;

        if (progressListener != null)
        {
            progressListener.update("Validation of entities", totalEntitiesToValidateCount,
                    entitiesValidatedCount);
        }
        result = calculate(entityValidator, entity, isNewEntity);

        if (result != null)
        {
            setRollback(tx, entity, " failed. " + result);
        }
    }

    private void setRollback(Transaction tx, IEntityInformationWithPropertiesHolder entity,
            String msg)
    {
        callback.rollbackTransaction(tx, "Validation of " + entityDescription(entity) + msg);
        isRolledBack = true;
    }

    private String entityDescription(IEntityInformationWithPropertiesHolder entity)
    {
        return entity.getEntityKind().getLabel() + " " + entity.getIdentifier() + " ("
                + entity.getEntityType().getCode() + ")";
    }

    private String calculate(IEntityValidator entityValidator,
            IEntityInformationWithPropertiesHolder entity, boolean isNewEntity)
    {
        IDynamicPropertyEvaluator evaluator =
                new DynamicPropertyEvaluator(daoFactory, null, dynamicPropertyCalculatorFactory,
                        managedPropertyEvaluatorFactory);
        IEntityAdaptor adaptor =
                EntityAdaptorFactory.create(entity, evaluator, daoFactory.getSessionFactory()
                        .getCurrentSession());

        return entityValidator.validate(adaptor, isNewEntity);
    }

    private boolean addToBeValidated(IEntityInformationWithPropertiesHolder entity)
    {
        return entitiesToValidate.add(new EntityIdentifier(entity));
    }

    private void newEntity(IEntityInformationWithPropertiesHolder entity)
    {
        if (addToBeValidated(entity))
        {
            newEntities.add(new EntityIdentifier(entity));
            totalEntitiesToValidateCount++;
        }
    }

    private void validatedEntity(IEntityInformationWithPropertiesHolder entity)
    {
        entitiesValidatedCount++;
        if (false == validatedEntities.add(new EntityIdentifier(entity)))
        {
            throw new IllegalStateException(
                    "Programming error - trying to validate the same entity twice (" + entity + ")");
        }

        if (false == entitiesToValidate.remove(new EntityIdentifier(entity)))
        {
            throw new IllegalStateException(
                    "Programming error - could not remove entity from to be validated list ("
                            + entity + ")");
        }
    }

    private void modifiedEntity(IEntityInformationWithPropertiesHolder entity)
    {
        if (addToBeValidated(entity))
        {
            totalEntitiesToValidateCount++;
        }
    }

    private void initializeLists()
    {
        validatedEntities = new HashSet<EntityIdentifier>();
        newEntities = new HashSet<EntityIdentifier>();
        entitiesToValidate = new HashSet<EntityIdentifier>();
    }

    @Override
    public void requestValidation(INonAbstractEntityAdapter entityAdapter)
    {
        IEntityInformationWithPropertiesHolder entity = entityAdapter.entityPE();
        if ((validatedEntities.contains(new EntityIdentifier(entity)) == false)
                && addToBeValidated(entity))
        {
            totalEntitiesToValidateCount++;
        }
    }

    private static class EntityIdentifier
    {
        private final Class<? extends IEntityInformationWithPropertiesHolder> clazz;

        private final String code;

        private final EntityKind kind;

        private final Long id;

        public EntityIdentifier(IEntityInformationWithPropertiesHolder entity)
        {
            this.clazz = entity.getClass();
            this.code = entity.getCode();
            this.kind = entity.getEntityKind();
            this.id = entity.getId();
        }

        public Class<? extends IEntityInformationWithPropertiesHolder> getEntityClass()
        {
            return clazz;
        }

        public Long getId()
        {
            return id;
        }

        public EntityKind getKind()
        {
            return kind;
        }

        @Override
        public int hashCode()
        {
            return code.hashCode() + kind.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof EntityIdentifier)
            {
                EntityIdentifier e = (EntityIdentifier) o;
                return (e.code.equals(code)) && e.kind.equals(kind);
            } else
            {
                throw new IllegalArgumentException(o.toString());
            }
        }

        @Override
        public String toString()
        {
            return clazz.getSimpleName() + ": " + code;
        }
    }

}
