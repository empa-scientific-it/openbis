/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.UpdateUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;

/**
 * Abstract super class of all <i>Hibernate</i> DAOs.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDAO extends HibernateDaoSupport
{

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private static final int MAX_STRING_ERROR_LENGTH = 500;

    protected AbstractDAO(final SessionFactory sessionFactory)
    {
        assert sessionFactory != null : "Unspecified session factory";
        setSessionFactory(sessionFactory);
    }

    /*
     * private static Map<Class<?>, ClassValidator<?>> validators = new HashMap<Class<?>, ClassValidator<?>>();
     */
    /**
     * Validates given <i>Persistence Entity</i> using an appropriate {@link Validator}.
     */
    @SuppressWarnings({ "rawtypes" })
    protected final static <E> void validatePE(final E pe) throws DataIntegrityViolationException
    {

        Validator validator = factory.getValidator();

        final Set<ConstraintViolation<E>> violations = validator.validate(pe);

        if (violations.size() > 0)
        {
            String msg = "";
            for (ConstraintViolation v : violations)
            {
                String invalidValue = (v.getInvalidValue() != null) ? v.getInvalidValue().toString() : null;
                if (invalidValue != null && invalidValue.length() > MAX_STRING_ERROR_LENGTH)
                {
                    invalidValue =
                            String.format("%s... (complete value was %d characters)", invalidValue.substring(0, MAX_STRING_ERROR_LENGTH),
                                    invalidValue.length());
                }
                msg += ", " + String.format(v.getMessage(), invalidValue);
            }
            throw new DataIntegrityViolationException(msg.substring(2));
        }
    }

    /**
     * Casts given <var>list</var> to specified type.
     * <p>
     * The purpose of this method is to avoid <code>SuppressWarnings("unchecked")</code> in calling methods.
     * </p>
     */
    @SuppressWarnings("unchecked")
    protected static final <T> List<T> cast(final List<?> list)
    {
        return (List<T>) list;
    }

    /**
     * Ensures that given {@link List} contains one and only one entity.
     * 
     * @throws EmptyResultDataAccessException if given <var>entities</var> are <code>null</code> or empty.
     * @throws IncorrectResultSizeDataAccessException if more than one entity is found in given {@link List}.
     */
    @SuppressWarnings("unchecked")
    protected final static <T> T getEntity(final List<T> entities) throws DataAccessException
    {
        return (T) DataAccessUtils.requiredSingleResult(entities);
    }

    /**
     * Casts given <var>entity</var> to specified type.
     * 
     * @throws EmptyResultDataAccessException if given <var>entity</var> is <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    protected final static <T> T getEntity(final Object entity)
            throws EmptyResultDataAccessException
    {
        if (entity == null)
        {
            throw new EmptyResultDataAccessException(1);
        }
        return (T) entity;
    }

    /**
     * Casts given <var>entityOrNull</var> to specified type or returns null for null input.
     */
    @SuppressWarnings("unchecked")
    protected final static <T> T tryGetEntity(final Object entityOrNull)
    {
        return entityOrNull == null ? null : (T) entityOrNull;
    }

    /**
     * Checks given <var>entities</var> and throws a {@link IncorrectResultSizeDataAccessException} if it contains more than one item.
     * 
     * @return <code>null</code> or the entity found at index <code>0</code>.
     */
    protected final static <T> T tryFindEntity(final List<T> entities, final String entitiesName,
            final Object... parameters) throws IncorrectResultSizeDataAccessException
    {
        final int size = entities.size();
        switch (size)
        {
            case 0:
                return null;
            case 1:
                return entities.get(0);
            default:
                throw new IncorrectResultSizeDataAccessException(String.format(
                        "%d %s found for '%s'. Expected: 1 or 0.", size, entitiesName,
                        parameters.length == 1 ? parameters[0] : Arrays.asList(parameters)), 1,
                        size);
        }
    }

    protected Date getTransactionTimeStamp()
    {
        return UpdateUtils.getTransactionTimeStamp(getSessionFactory());
    }

    /**
     * Executes given <var>sql</var>.
     * <p>
     * Should be an <code>INSERT</code> or <code>UPDATE</code> statement.
     * </p>
     */
    protected final void executeUpdate(final String sql, final Serializable... parameters)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                @Override
                public final Object doInHibernate(final Session session) throws HibernateException
                {
                    final SQLQuery sqlQuery = session.createSQLQuery(sql);
                    for (int i = 0; i < parameters.length; i++)
                    {
                        Serializable parameter = parameters[i];
                        if (parameter instanceof Long)
                        {
                            sqlQuery.setLong(i, (Long) parameter);
                        } else if (parameter instanceof Integer)
                        {
                            sqlQuery.setInteger(i, (Integer) parameter);
                        } else if (parameter instanceof Character)
                        {
                            sqlQuery.setCharacter(i, (Character) parameter);
                        } else if (parameter instanceof Date)
                        {
                            sqlQuery.setDate(i, (Date) parameter);
                        } else
                        {
                            sqlQuery.setSerializable(i, parameter);
                        }
                    }
                    sqlQuery.executeUpdate();
                    return null;
                }
            });
    }

    /**
     * Natively performs given <var>sql</var> and returns an unique result by calling {@link Query#uniqueResult()}.
     */
    @SuppressWarnings("unchecked")
    protected final <T> T getUniqueResult(final String sql, final Object... parameters)
    {
        return (T) getHibernateTemplate().execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                @Override
                public final Object doInHibernate(final Session session)
                {
                    return session.createSQLQuery(String.format(sql, parameters)).uniqueResult();
                }
            });
    }

    protected final static Object[] toArray(final Object... objects)
    {
        return objects;
    }

    protected final long getNextSequenceId(final String sequenceName)
    {
        final Object result = getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session sess) throws HibernateException
                {
                    SQLQuery sqlQuery =
                            sess.createSQLQuery("select nextval('" + sequenceName + "')");
                    return sqlQuery.uniqueResult();
                }
            });

        Long toReturn;
        if (result instanceof BigInteger)
        {
            toReturn = ((BigInteger) result).longValue();
        } else
        {
            toReturn = (Long) result;
        }
        return toReturn;
    }

    protected final Object executeStatelessAction(final StatelessHibernateCallback action) throws DataAccessException
    {
        assert action != null;
        return getHibernateTemplate().execute(new HibernateCallback<Object>()
            {
                @Override
                public final Object doInHibernate(final Session session) throws HibernateException
                {
                    return session.doReturningWork(new ReturningWork<Object>()
                        {
                            @Override
                            public Object execute(Connection connection) throws SQLException
                            {
                                StatelessSessionImpl sls = null;
                                try
                                {
                                    sls = (StatelessSessionImpl) getSessionFactory().openStatelessSession(connection);
                                    return action.doInStatelessSession(sls);
                                } catch (HibernateException ex)
                                {
                                    throw SessionFactoryUtils.convertHibernateAccessException(ex);
                                }
                            }
                        });
                }
            });
    }

    /*
     * protected final Object executeStatelessAction(final StatelessHibernateCallback action) throws DataAccessException { assert action != null;
     * return getHibernateTemplate().execute(new HibernateCallback() {
     * @Override public final Object doInHibernate(final Session session) throws HibernateException { StatelessSession sls = null; try { sls =
     * this.getStatelessSession(); return action.doInStatelessSession(sls); } catch (HibernateException ex) { throw
     * SessionFactoryUtils.convertHibernateAccessException(ex); } catch (RuntimeException ex) { // Callback code threw application exception... throw
     * ex; } finally { if (sls != null) { releaseConnection(sls.connection()); sls.close(); } } } private StatelessSession getStatelessSession()
     * throws CannotGetJdbcConnectionException { return getSessionFactory().openStatelessSession(getThreadBoundConnection()); } private Connection
     * getThreadBoundConnection() throws CannotGetJdbcConnectionException { return DataSourceUtils.getConnection(SessionFactoryUtils
     * .getDataSource(getSessionFactory())); } private void releaseConnection(Connection connection) { DataSourceUtils.releaseConnection(connection,
     * SessionFactoryUtils.getDataSource(getSessionFactory())); } }); }
     */

    protected void lockEntities(Collection<? extends IIdHolder> entitiesOrNull)
    {
        if (entitiesOrNull == null)
        {
            return;
        }
        for (IIdHolder entity : entitiesOrNull)
        {
            lockEntity(entity);
        }
    }

    protected void lockEntity(IIdHolder entityOrNull)
    {
        if (entityOrNull != null && entityOrNull.getId() != null
                && currentSession().contains(entityOrNull))
        {
            getHibernateTemplate().lock(entityOrNull, LockMode.PESSIMISTIC_WRITE);
        }
    }

    /**
     * Callback interface for Hibernate code requiring a {@link org.hibernate.StatelessSession}. To be used with {@link HibernateTemplate}'s execution
     * methods, often as anonymous classes within a method implementation.
     */
    protected interface StatelessHibernateCallback
    {
        Object doInStatelessSession(StatelessSession sls);
    }

    protected static void flushWithSqlExceptionHandling(HibernateTemplate hibernateTemplate)
            throws DataAccessException
    {
        try
        {
            hibernateTemplate.flush();
        } catch (UncategorizedSQLException e)
        {
            translateUncategorizedSQLException(e);
        }
    }

    protected static void translateUncategorizedSQLException(UncategorizedSQLException exception)
            throws DataAccessException
    {
        final SQLException sqlExceptionOrNull =
                ExceptionUtils.tryGetThrowableOfClass(exception, SQLException.class);
        if (sqlExceptionOrNull != null && sqlExceptionOrNull.getNextException() != null)
        {
            throw new DataIntegrityViolationException(sqlExceptionOrNull.getNextException()
                    .getMessage());
        } else
        {
            throw exception;
        }
    }

    protected static Set<TechId> transformNumbers2TechIdSet(Collection<? extends Number> numbers)
    {
        final Set<TechId> result = new HashSet<TechId>();
        for (Number number : numbers)
        {
            result.add(new TechId(number));
        }
        return result;
    }

    protected static List<TechId> transformNumbers2TechIdList(Collection<? extends Number> numbers)
    {
        final List<TechId> result = new ArrayList<TechId>();
        for (Number number : numbers)
        {
            result.add(new TechId(number));
        }
        return result;
    }

    protected static <T extends IEntityInformationWithPropertiesHolder> List<Long> transformEntities2Longs(
            Collection<T> entities)
    {
        final List<Long> result = new ArrayList<Long>();
        for (IEntityInformationWithPropertiesHolder entity : entities)
        {
            result.add(entity.getId());
        }
        return result;
    }

    protected static <T extends IEntityInformationWithPropertiesHolder> void scheduleDynamicPropertiesEvaluationWithIds(
            IDynamicPropertyEvaluationScheduler scheduler, Class<T> entityClass, List<Long> ids)
    {
        scheduleDynamicPropertiesEvaluationForIds(scheduler, entityClass, ids);
    }

    protected static <T extends IEntityInformationWithPropertiesHolder> void scheduleDynamicPropertiesEvaluation(
            IDynamicPropertyEvaluationScheduler scheduler, Class<T> entityClass, List<T> entities)
    {
        List<Long> ids = transformEntities2Longs(entities);
        scheduleDynamicPropertiesEvaluationForIds(scheduler, entityClass, ids);
    }

    protected static <T extends IEntityInformationWithPropertiesHolder> void scheduleDynamicPropertiesEvaluationForIds(
            IDynamicPropertyEvaluationScheduler scheduler, Class<T> entityClass,
            List<Long> entityIds)
    {
        scheduler.scheduleUpdate(DynamicPropertyEvaluationOperation
                .evaluate(entityClass, entityIds));
    }

    public void flush()
    {
        flushWithSqlExceptionHandling(getHibernateTemplate());
    }

    public void clear()
    {
        getHibernateTemplate().clear();
    }

    @SuppressWarnings("unchecked")
    protected final static <T> Class<T> cast(final Class<?> clazz)
    {
        return (Class<T>) clazz;
    }

}
