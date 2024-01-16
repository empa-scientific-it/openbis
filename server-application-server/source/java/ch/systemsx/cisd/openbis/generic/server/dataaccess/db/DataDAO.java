/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.ResultTransformer;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.collection.CollectionStyle;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.collection.ToStringDefaultConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Implementation of {@link IDataDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class DataDAO extends AbstractGenericEntityWithPropertiesDAO<DataPE> implements IDataDAO
{
    private final int MAX_BATCH_SIZE = 999;

    private final static Class<DataPE> ENTITY_CLASS = DataPE.class;

    private final static Class<ExternalDataPE> EXTERNAL_DATA_ENTITY_CLASS = ExternalDataPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataDAO.class);

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    private static final String EXTERNAL_DATA_TABLE_NAME = EXTERNAL_DATA_ENTITY_CLASS
            .getSimpleName();

    private Boolean isAccessTimestampColumnDefined;

    private IRelationshipTypeDAO relationshipTypeDAO;

    DataDAO(final PersistencyResources persistencyResources, IRelationshipTypeDAO relationshipTypeDAO, EntityHistoryCreator historyCreator)
    {
        super(persistencyResources, ENTITY_CLASS, historyCreator);
        this.relationshipTypeDAO = relationshipTypeDAO;
    }

    //
    // IExternalDataDAO
    //

    @Override
    public boolean hasDataSet(SamplePE sample) throws DataAccessException
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(DataPE.class);
        criteria.add(Restrictions.eq("sampleInternal", sample));
        criteria.setProjection(Projections.rowCount());
        Integer count =
                ((Number) getHibernateTemplate().findByCriteria(criteria).get(0)).intValue();
        return count > 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<SamplePE, Boolean> haveDataSets(Collection<SamplePE> samples) throws DataAccessException
    {
        if (samples == null || samples.isEmpty())
        {
            return Collections.emptyMap();
        }

        final List<Long> sampleIds = new LinkedList<Long>();
        for (SamplePE sample : samples)
        {
            sampleIds.add(sample.getId());
        }

        Set<Long> idsOfSamplesWithDataSets = (Set<Long>) getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {

                    InQuery inQuery = new InQuery<Long, Number>();
                    List<Number> list =
                            inQuery.withBatch(session, "select distinct samp_id from data where samp_id in (:sampleIds)", "sampleIds", sampleIds,
                                    null);
                    Set<Long> ids = new HashSet<Long>();
                    for (Number item : list)
                    {
                        ids.add(item.longValue());
                    }
                    return ids;
                }
            });

        Map<SamplePE, Boolean> result = new HashMap<SamplePE, Boolean>();
        for (SamplePE sample : samples)
        {
            result.put(sample, idsOfSamplesWithDataSets.contains(sample.getId()));
        }

        return result;
    }

    @Override
    public final List<DataPE> listRelatedDataSets(final List<IEntityInformationHolder> entities,
            EntityKind entityKind) throws DataAccessException
    {
        assert entities != null : "Unspecified entities.";
        assert entities.size() > 0 : "Empty entities.";
        assert entityKind != null : "Unspecified entity kind.";

        final String entityName = entityKind.toString().toLowerCase();
        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.sampleInternal "
                        + "left join fetch e.dataSetParentRelationships "
                        + "left join fetch e.dataSetProperties "
                        + "where e.%sInternal.id IN (:ids)", TABLE_NAME, entityName);

        final List<Long> ids = new ArrayList<Long>();
        for (IEntityInformationHolder entity : entities)
        {
            ids.add(entity.getId());
        }
        final List<DataPE> results = new ArrayList<DataPE>();
        BatchOperationExecutor.executeInBatches(new IBatchOperation<Long>()
            {
                @Override
                public void execute(List<Long> entityIds)
                {

                    final List<DataPE> list =
                            cast(getHibernateTemplate().findByNamedParam(query, "ids", entityIds));
                    results.addAll(list);
                }

                @Override
                public List<Long> getAllEntities()
                {
                    return ids;
                }

                @Override
                public String getEntityName()
                {
                    return "dataSet";
                }

                @Override
                public String getOperationName()
                {
                    return "listRelatedDataSets";
                }
            });

        distinct(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for %d entities.",
                    results.size(), entities.size()));
        }
        return results;
    }

    @Override
    public final List<DataPE> listDataSetsWithoutRelationships(final SamplePE sample)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";

        final String query = String.format("from %s e where e.sampleInternal = ?", TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(sample)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [sample=%s].",
                    list.size(), sample));
        }
        return list;
    }

    @Override
    public final List<DataPE> listDataSets(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample.";

        final String query =
                String.format("from %s e " + "left join fetch e.experimentInternal "
                        + "left join fetch e.dataSetParentRelationships "
                        + "left join fetch e.dataSetProperties " + "where e.sampleInternal = ?",
                        TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(sample)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d external data have been found for [sample=%s].",
                    list.size(), sample));
        }
        return list;
    }

    @Override
    public final List<DataPE> listExternalData(final DataStorePE dataStore)
            throws DataAccessException
    {
        assert dataStore != null : "Unspecified data store.";
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExternalDataPE.class);
        criteria.add(Restrictions.eq("dataStore", dataStore));
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d data set(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), dataStore, list.size()));
        }
        return list;
    }

    @Override
    public final List<DataPE> listDataSets(final ExperimentPE experiment)
            throws DataAccessException
    {
        assert experiment != null : "Unspecified experiment.";

        final String query =
                String.format(
                        "from %s e " + "left join fetch e.experimentInternal "
                                + "left join fetch e.dataSetParentRelationships "
                                + "left join fetch e.dataSetProperties "
                                + "where e.experimentInternal = ?",
                        TABLE_NAME);
        final List<DataPE> list = cast(getHibernateTemplate().find(query, toArray(experiment)));

        // distinct does not work properly in HQL for left joins
        distinct(list);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d external data have been found for [experiment=%s].", list.size(),
                    experiment));
        }
        return list;
    }

    private void distinct(List<DataPE> list)
    {
        Set<DataPE> set = new TreeSet<DataPE>(list);
        list.clear();
        list.addAll(set);
    }

    @Override
    public DataPE tryToFindDataSetByCode(String dataSetCode)
    {
        assert dataSetCode != null : "Unspecified data set code.";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.JOIN);
        criteria.setFetchMode("dataStore", FetchMode.JOIN);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            String methodName = MethodUtils.getCurrentMethod().getName();
            operationLog.debug(String.format("%s(%s): '%s'.", methodName, dataSetCode, entity));
        }
        return entity;
    }

    @Override
    public TechId tryToFindDataSetIdByCode(String dataSetCode)
    {
        SQLQuery query =
                currentSession().createSQLQuery(
                        "select id from data_all where code = :code");
        query.setString("code", CodeConverter.tryToDatabase(dataSetCode));
        Object uniqueResult = query.uniqueResult();
        if (uniqueResult != null)
        {
            return new TechId((BigInteger) uniqueResult);
        } else
        {
            return null;
        }
    }

    @Override
    public List<DeletedDataPE> tryToFindDeletedDataSetsByCodes(Collection<String> dataSetCodes)
    {
        assert dataSetCodes != null : "Unspecified collection";

        if (dataSetCodes.size() == 0)
        {
            return Collections.emptyList();
        }

        final List<DeletedDataPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(DeletedDataPE.class);
                            criteria.setFetchMode("dataStore", FetchMode.SELECT);
                            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                            return criteria;
                        }
                    }, "code", dataSetCodes);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    dataSetCodes));
        }
        return list;
    }

    @Override
    public List<DataPE> tryToFindFullDataSetsByIds(Collection<Long> ids, boolean withPropertyTypes,
            boolean lockForUpdate)
    {
        return tryToFindFullDataSets("id", ids, withPropertyTypes, lockForUpdate);
    }

    @Override
    public List<DataPE> tryToFindFullDataSetsByCodes(Collection<String> dataSetCodes,
            boolean withPropertyTypes, boolean lockForUpdate)
    {
        return tryToFindFullDataSets("code", dataSetCodes, withPropertyTypes, lockForUpdate);
    }

    private List<DataPE> tryToFindFullDataSets(String identifierColumn, Collection<?> identifiers,
            boolean withPropertyTypes, boolean lockForUpdate)
    {
        assert identifiers != null : "Unspecified collection";

        // PostgreSQL has a limit of 2^15-1 values in an IN clause, so we need to split the data set
        // codes into chunks to avoid a
        // java.io.IOException: Tried to send an out-of-range integer as a 2-byte value: nn
        // However, we know from experience that using chunk sizes larger than 999 we get bad
        // performance, so we split the query in chunks of 999 rather than 2^15-1.
        final int len = identifiers.size();
        if (len > MAX_BATCH_SIZE)
        {
            final List<DataPE> result = new ArrayList<DataPE>(identifiers.size());
            final List<?> dataSetCodesList = new ArrayList<Object>(identifiers);
            int startIndex = 0;
            int endIndex = MAX_BATCH_SIZE;
            while (endIndex > startIndex)
            {
                result.addAll(primFindFullDataSetsByCode(identifierColumn,
                        dataSetCodesList.subList(startIndex, endIndex), withPropertyTypes,
                        lockForUpdate));
                startIndex = endIndex;
                endIndex = Math.min(endIndex + MAX_BATCH_SIZE, len);
            }
            return result;
        } else
        {
            return primFindFullDataSetsByCode(identifierColumn, identifiers, withPropertyTypes,
                    lockForUpdate);
        }
    }

    private List<DataPE> primFindFullDataSetsByCode(String identifierColumn,
            Collection<?> identifiers, final boolean withPropertyTypes, final boolean lockForUpdate)
    {

        if (identifiers.size() == 0)
        {
            return Collections.emptyList();
        }

        final List<DataPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(ENTITY_CLASS);
                            criteria.setFetchMode("dataSetType", FetchMode.SELECT);
                            criteria.setFetchMode("dataStore", FetchMode.SELECT);
                            criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
                            criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
                            criteria.setFetchMode("fileFormat", FetchMode.SELECT);
                            if (withPropertyTypes)
                            {
                                criteria.setFetchMode(
                                        "dataSetType.dataSetTypePropertyTypesInternal",
                                        FetchMode.JOIN);
                            }
                            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

                            /**
                             * lockForUpdate parameter is ignored. See LMS-2882 details
                             */
                            /*
                             * if (lockForUpdate) { criteria.setLockMode(LockMode.UPGRADE); }
                             */
                            return criteria;
                        }
                    }, identifierColumn, identifiers);

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Found '%s' data sets for codes '%s'.", list.size(),
                    identifiers));
        }
        return list;
    }

    @Override
    public DataPE tryToFindFullDataSetByCode(String dataSetCode, boolean withPropertyTypes,
            boolean lockForUpdate)
    {
        assert dataSetCode != null : "Unspecified data set code";

        final String mangledCode = CodeConverter.tryToDatabase(dataSetCode);
        final Criterion codeEq = Restrictions.eq("code", mangledCode);

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(codeEq);
        criteria.setFetchMode("dataSetType", FetchMode.SELECT);
        criteria.setFetchMode("dataStore", FetchMode.SELECT);
        criteria.setFetchMode("experimentInternal", FetchMode.SELECT);
        criteria.setFetchMode("sampleInternal", FetchMode.SELECT);
        criteria.setFetchMode("fileFormat", FetchMode.SELECT);
        if (withPropertyTypes)
        {
            criteria.setFetchMode("dataSetType.dataSetTypePropertyTypesInternal", FetchMode.JOIN);
        }
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        /**
         * lockForUpdate parameter is ignored. See LMS-2882 details
         */
        /*
         * if (lockForUpdate) { criteria.setLockMode(LockMode.UPGRADE); }
         */
        final List<DataPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        final DataPE entity = tryFindEntity(list, "data set");

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Data Set '%s' found for data set code '%s'.", entity,
                    dataSetCode));
        }
        return entity;
    }

    @Override
    public void updateDataSetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus status)
    {
        assert dataSetCodes != null : "Unspecified data set codes";
        assert status != null : "Unspecified code";

        if (dataSetCodes.size() == 0)
        {
            return;
        }

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        // PostgreSQL has a limit of 2^15-1 values in an IN clause, so we need to split the data set
        // codes into chunks to avoid a
        // java.io.IOException: Tried to send an out-of-range integer as a 2-byte value: nn
        // However, we know from experience that using chunk sizes larger than 999 we get bad
        // performance, so we split the query in chunks of 999 rather than 2^15-1.
        final int len = dataSetCodes.size();
        int updatedRows = 0;
        if (len > MAX_BATCH_SIZE)
        {
            int startIndex = 0;
            int endIndex = MAX_BATCH_SIZE;
            while (endIndex > startIndex)
            {
                final int startIndexFinal = startIndex;
                final int endIndexFinal = endIndex;
                List<String> codes = dataSetCodes.subList(startIndexFinal, endIndexFinal);
                updatedRows += updateStatus(hibernateTemplate, status, codes);
                startIndex = endIndex;
                endIndex = Math.min(endIndex + MAX_BATCH_SIZE, len);
            }
        } else
        {
            updatedRows = updateStatus(hibernateTemplate, status, dataSetCodes);
        }
        scheduleDynamicPropertiesEvaluationForDataSets(dataSetCodes);
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate("Update of %s data set statuses to %s failed.", dataSetCodes.size(), status);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATED: %s data set statuses to '%s'.", dataSetCodes.size(), status));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Integer updateStatus(final HibernateTemplate hibernateTemplate, final DataSetArchivingStatus status, List<String> codes)
    {
        return (Integer) hibernateTemplate.execute(new HibernateCallback()
            {
                @Override
                public final Object doInHibernate(final Session session)
                        throws HibernateException
                {
                    int result = session.createQuery(
                            "UPDATE " + EXTERNAL_DATA_TABLE_NAME + " SET status = :status WHERE code IN (:codes) ")
                            .setParameter("status", status)
                            .setParameterList("codes", codes)
                            .executeUpdate();
                    updateVersion(session, codes);
                    return result;
                    /*
                    // NOTE: 'VERSIONED' makes modification time modified too
                    return session
                            .createQuery(
                                    "UPDATE VERSIONED "
                                            + EXTERNAL_DATA_TABLE_NAME
                                            + " SET status = :status WHERE code IN (:codes) ")
                            .setParameter("status", status)
                            .setParameterList("codes", codes)
                            .executeUpdate();
                     */
                }
            });
    }

    private static int updateVersion(final Session session, List<String> codes)
    {
        return session.createQuery(
                "UPDATE " + TABLE_NAME + " SET version = version + 1 WHERE code IN (:codes) ")
                .setParameterList("codes", codes)
                .executeUpdate();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void updateSizes(final Map<String, Long> sizeMap)
    {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.execute(new HibernateCallback()
            {
                @Override
                public final Object doInHibernate(final Session session)
                        throws HibernateException
                {
                    for (Map.Entry<String, Long> sizeEntry : sizeMap.entrySet())
                    {
                        // data sets consisting out of empty folders have a size of 0,
                        // but we want the size of a data set to be strictly positive
                        long positiveSize = Math.max(1, sizeEntry.getValue());

                        session.createQuery(
                                "UPDATE " + EXTERNAL_DATA_TABLE_NAME
                                        + " SET size = :size WHERE code = :code")
                                .setParameter("size", positiveSize)
                                .setParameter("code", sizeEntry.getKey())
                                .executeUpdate();
                    }
                    return null;
                }
            });
        hibernateTemplate.flush();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void updateDataSetStatuses(final List<String> dataSetCodes,
            final DataSetArchivingStatus status, final boolean presentInArchive)
    {
        assert dataSetCodes != null : "Unspecified data set codes";
        assert status != null : "Unspecified code";

        if (dataSetCodes.size() == 0)
        {
            return;
        }

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        // PostgreSQL has a limit of 2^15-1 values in an IN clause, so we need to split the data set
        // codes into chunks to avoid a
        // java.io.IOException: Tried to send an out-of-range integer as a 2-byte value: nn
        // However, we know from experience that using chunk sizes larger than 999 we get bad
        // performance, so we split the query in chunks of 999 rather than 2^15-1.
        final int len = dataSetCodes.size();
        int updatedRows = 0;
        if (len > MAX_BATCH_SIZE)
        {
            int startIndex = 0;
            int endIndex = MAX_BATCH_SIZE;
            while (endIndex > startIndex)
            {
                final int startIndexFinal = startIndex;
                final int endIndexFinal = endIndex;
                final List<String> codes = dataSetCodes.subList(startIndexFinal, endIndexFinal);
                updatedRows += (Integer) hibernateTemplate.execute(new StatusUpdater(presentInArchive, status, codes));
                startIndex = endIndex;
                endIndex = Math.min(endIndex + MAX_BATCH_SIZE, len);
            }
        } else
        {
            updatedRows = (Integer) hibernateTemplate.execute(new StatusUpdater(presentInArchive, status, dataSetCodes));
        }
        scheduleDynamicPropertiesEvaluationForDataSets(dataSetCodes);
        hibernateTemplate.flush();
        if (updatedRows != dataSetCodes.size())
        {
            throw UserFailureException.fromTemplate("Update of %s data set statuses to '%s' and presentInArchive to '%s' failed.",
                    dataSetCodes.size(), status, presentInArchive);
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATED: %s data set statuses to '%s' and presentInArchive flag to '%s'.", dataSetCodes.size(), status,
                    presentInArchive));
        }
    }

    private void scheduleDynamicPropertiesEvaluationForDataSets(final List<String> dataSetCodes)
    {
        List<DataPE> dataSets = tryToFindFullDataSetsByCodes(dataSetCodes, false, false);
        scheduleDynamicPropertiesEvaluation(dataSets);
    }

    @Override
    public void createDataSet(DataPE dataset, PersonPE modifier)
    {
        createDataSets(Collections.singletonList(dataset), modifier);
    }

    @Override
    public void createDataSets(final List<DataPE> dataSets, PersonPE modifier) throws DataAccessException
    {
        assert dataSets != null && dataSets.size() > 0 : "Unspecified or empty dataSets.";

        try
        {
            for (final DataPE dataPE : dataSets)
            {
                internalCreateOrUpdateDataSet(dataPE, modifier);
            }

            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("ADD: %d dataSets.", dataSets.size()));
            }

            // need to deal with exception thrown by trigger checking uniqueness
            flushWithSqlExceptionHandling(getHibernateTemplate());

            scheduleDynamicPropertiesEvaluation(dataSets);
        } catch (DataAccessException e)
        {
            DataSetDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    private void internalCreateOrUpdateDataSet(DataPE dataset, PersonPE modifier)
    {
        assert dataset != null : "Unspecified data set.";

        dataset.setCode(CodeConverter.tryToDatabase(dataset.getCode()));
        dataset.setModifier(modifier);
        dataset.setRegistrator(modifier);
        validatePE(dataset);

        lockRelatedEntities(dataset);
        getHibernateTemplate().saveOrUpdate(dataset);
    }

    private void lockRelatedEntities(DataPE data)
    {
        lockEntity(data.getExperiment());
        lockEntity(data.tryGetSample());
        lockEntities(data.getLinkedDataSets());
    }

    @Override
    public void updateDataSet(DataPE data, PersonPE modifier)
    {
        assert data != null : "Given external data can not be null.";
        validatePE(data);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        data.setCode(CodeConverter.tryToDatabase(data.getCode()));
        data.setModifier(modifier);
        lockRelatedEntities(data);
        hibernateTemplate.update(data);
        hibernateTemplate.flush();
        scheduleDynamicPropertiesEvaluation(Collections.singletonList(data));

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: external data '%s'.", data));
        }
    }

    @Override
    public void delete(final List<TechId> dataIds, final PersonPE registrator, final String reason)
            throws DataAccessException
    {
        DeletionEventSQLs sqls = new DeletionEventSQLs();

        // For data sets we load codes, which are logically the same as perm ids.
        // We load them ordered by ID because locations need to be loaded in the same order.
        sqls.selectPermIds = createSelectCodesOrderByIdSQL();
        sqls.selectLocations = createSelectLocationsOrderByIdSQL();
        sqls.deleteProperties = SQLBuilder.createDeletePropertiesSQL(TableNames.DATA_SET_PROPERTIES_TABLE, ColumnNames.DATA_SET_COLUMN);
        sqls.deleteDataSets = SQLBuilder.createDeleteEnitiesSQL(TableNames.DATA_ALL_TABLE);
        sqls.insertEvent = SQLBuilder.createInsertEventSQL();
        // data set specific queries
        sqls.deleteExternalData = createDeleteExternalDataSQL();
        sqls.deleteContentCopies = createDeleteContentCopiesSQL();
        Long relationshipTypeId = RelationshipUtils.getParentChildRelationshipType(relationshipTypeDAO).getId();
        sqls.deleteChildrenConnections = createDeleteChildrenConnectionsSQL(relationshipTypeId);
        sqls.deleteParentConnections = createDeleteParentConnectionsSQL(relationshipTypeId);
        sqls.selectPropertyHistory = createQueryPropertyHistorySQL();
        sqls.selectRelationshipHistory = createQueryRelationshipHistorySQL();
        sqls.selectAttributes = createQueryAttributesHistorySQL();

        executePermanentDeleteOfDataSets(EntityType.DATASET, dataIds, registrator, reason, sqls);
    }

    private static final class StatusUpdater implements HibernateCallback
    {
        private final boolean presentInArchive;

        private final DataSetArchivingStatus status;

        private final List<String> codes;

        private StatusUpdater(boolean presentInArchive, DataSetArchivingStatus status, List<String> codes)
        {
            this.presentInArchive = presentInArchive;
            this.status = status;
            this.codes = codes;
        }

        @Override
        public final Object doInHibernate(final Session session)
                throws HibernateException
        {

            // NOTE: 'VERSIONED' makes modification time modified too
            Query query;
            if (presentInArchive)
            {
                query = session
                        .createQuery(
                                "UPDATE "
                                        + EXTERNAL_DATA_TABLE_NAME
                                        + " SET status = :status, presentInArchive = :presentInArchive, "
                                        + "     archivingRequested = 'f'"
                                        + " WHERE code IN (:codes) ");
            } else
            {
                query = session
                        .createQuery(
                                "UPDATE "
                                        + EXTERNAL_DATA_TABLE_NAME
                                        + " SET status = :status, presentInArchive = :presentInArchive"
                                        + " WHERE code IN (:codes) ");

            }
            int result = query
                    .setParameter("status", status)
                    .setParameter("presentInArchive", presentInArchive)
                    .setParameterList("codes", codes)
                    .executeUpdate();
            System.err.println(result+" updated");
            System.err.println(updateVersion(session, codes)+" updated version");
            return result;
        }
    }

    private static class DeletionEventSQLs
    {
        public String selectPermIds;

        public String selectLocations;

        public String deleteProperties;

        public String deleteDataSets;

        public String insertEvent;

        public String deleteExternalData;

        public String deleteContentCopies;

        public String deleteChildrenConnections;

        public String deleteParentConnections;

        public String selectPropertyHistory;

        public String selectRelationshipHistory;

        public String selectAttributes;
    }

    protected void executePermanentDeleteOfDataSets(final EntityType entityType,
            final List<TechId> entityTechIds, final PersonPE registrator, final String reason,
            DeletionEventSQLs sqls)
    {
        List<Long> entityIds = TechId.asLongs(entityTechIds);
        DeleteDataSetsPermanentlyBatchOperation deleteOperation =
                new DeleteDataSetsPermanentlyBatchOperation(entityType, entityIds, registrator,
                        reason, sqls);
        BatchOperationExecutor.executeInBatches(deleteOperation);

        // FIXME remove this when we remove the switch to disable trash
        scheduleRemoveFromFullTextIndex(entityIds);
    }

    private static String createSelectCodesOrderByIdSQL()
    {
        return "SELECT code FROM " + TableNames.DATA_ALL_TABLE + " WHERE id " + SQLBuilder.inEntityIds()
                + " ORDER BY id";
    }

    private static String createSelectLocationsOrderByIdSQL()
    {
        return "SELECT ed.location, ed.share_id, ds.code FROM " + TableNames.DATA_ALL_TABLE + " d "
                + "JOIN data_stores ds ON (d.dast_id = ds.id) "
                + "LEFT OUTER JOIN external_data ed ON (d.id = ed.id) WHERE d.id "
                + SQLBuilder.inEntityIds() + " ORDER BY d.id";
    }

    private static String createDeleteExternalDataSQL()
    {
        return "DELETE FROM " + TableNames.EXTERNAL_DATA_TABLE + " WHERE id "
                + SQLBuilder.inEntityIds();
    }

    private static String createDeleteContentCopiesSQL()
    {
        return "DELETE FROM " + TableNames.CONTENT_COPIES_TABLE + " WHERE data_id "
                + SQLBuilder.inEntityIds();
    }

    private static String createDeleteChildrenConnectionsSQL(long relationshipTypeId)
    {
        return "DELETE FROM " + TableNames.DATA_SET_RELATIONSHIPS_ALL_TABLE
                + " WHERE data_id_parent " + SQLBuilder.inEntityIds() + " and relationship_id = " + relationshipTypeId;
    }

    private static String createDeleteParentConnectionsSQL(long relationshipTypeId)
    {
        return "DELETE FROM " + TableNames.DATA_SET_RELATIONSHIPS_ALL_TABLE
                + " WHERE data_id_child " + SQLBuilder.inEntityIds() + " and relationship_id = " + relationshipTypeId;
    }

    private static String createQueryPropertyHistorySQL()
    {
        return " (" +
                " select d.code as data_set_code, pt.code as property_code, "
                + "coalesce(value, vocabulary_term, material) as value, p.user_id, "
                + "h.valid_from_timestamp, h.valid_until_timestamp "
                + " from " + TableNames.DATA_ALL_TABLE + " d, " + TableNames.DATA_SET_PROPERTIES_HISTORY_TABLE + " h, "
                + TableNames.DATA_SET_TYPE_PROPERTY_TYPE_TABLE + " dspt, "
                + TableNames.PROPERTY_TYPES_TABLE + " pt, " + TableNames.PERSONS_TABLE + " p " +
                " where ds_id " + SQLBuilder.inEntityIds() + " and d.id = ds_id " +
                " and h.dstpt_id = dspt.id and dspt.prty_id = pt.id and pers_id_author = p.id" +
                " )" +
                " union" +
                " (" +
                " select d.code as data_set_code, pt.code as property_code, coalesce(value, " +
                " (select (t.code || ' [' || v.code || ']') from " + TableNames.CONTROLLED_VOCABULARY_TERM_TABLE + " as t join "
                + TableNames.CONTROLLED_VOCABULARY_TABLE + " as v on t.covo_id = v.id where t.id = pr.CVTE_ID)," +
                " (select (m.code || ' [' || mt.code || ']') from " + TableNames.MATERIALS_TABLE + " as m join " + TableNames.MATERIAL_TYPES_TABLE
                + " as mt on m.maty_id = mt.id where m.id = pr.MATE_PROP_ID)) as value," +
                " author.user_id, pr.modification_timestamp, null" +
                " from " + TableNames.DATA_ALL_TABLE + " d, " + TableNames.DATA_SET_PROPERTIES_TABLE + " pr, "
                + TableNames.DATA_SET_TYPE_PROPERTY_TYPE_TABLE + " dtpt, "
                + TableNames.PROPERTY_TYPES_TABLE + " pt, " + TableNames.PERSONS_TABLE + " author" +
                " where ds_id " + SQLBuilder.inEntityIds() + " and d.id = pr.ds_id " +
                " and pr.dstpt_id = dtpt.id and dtpt.prty_id = pt.id and pr.pers_id_author = author.id" +
                " )" +
                " order by 1, valid_from_timestamp";
    }

    private static String createQueryRelationshipHistorySQL()
    {
        return " SELECT d.code, relation_type,entity_perm_id, " + ENTITY_TYPE + ", "
                + "p.user_id, valid_from_timestamp, valid_until_timestamp "
                + "FROM " + TableNames.DATA_ALL_TABLE + " d, " + TableNames.DATA_SET_RELATIONSHIPS_HISTORY_TABLE + " h, "
                + TableNames.PERSONS_TABLE + " p "
                + "WHERE d.id = main_data_id and  main_data_id " + SQLBuilder.inEntityIds()
                + " and h.pers_id_author = p.id"
                + " order by 1, valid_from_timestamp";
    }

    private static final String ENTITY_TYPE = "case "
            + "when h.data_id is not null then 'DATA_SET' "
            + "when h.samp_id is not null then 'SAMPLE' "
            + "when h.expe_id is not null then 'EXPERIMENT' "
            + "else 'UNKNOWN' end as entity_type";

    private static String createQueryAttributesHistorySQL()
    {
        return "SELECT d.id, d.code, d.code as perm_id, t.code as entity_type, "
                + "st.code as data_store, d.data_producer_code, "
                + "d.registration_timestamp, r.user_id as registrator, "
                + "ed.share_id, ed.size, ed.location, "
                + "fft.code as file_format_type, lt.code as locator_type,"
                + "stf.code as storage_format, ed.is_complete, ed.speed_hint, "
                + "ed.storage_confirmation, ed.status as archiving_status, "
                + "ed.present_in_archive "
                + "FROM data_all d "
                + "left JOIN external_data ed on d.id = ed.id "
                + "left JOIN locator_types lt on lt.id = ed.loty_id "
                + "left JOIN file_format_types fft on fft.id = ffty_id "
                + "left JOIN controlled_vocabulary_terms stf on stf.id = cvte_id_stor_fmt "
                + "JOIN data_stores st on d.dast_id = st.id "
                + "JOIN data_set_types t on d.dsty_id = t.id "
                + "JOIN persons r on d.pers_id_registerer = r.id "
                + "WHERE d.id " + SQLBuilder.inEntityIds();
    }

    // TODO refactor - it is very similar code to the one in AbstractGenericEntityWithPropertiesDAO
    protected class DeleteDataSetsPermanentlyBatchOperation implements IBatchOperation<Long>
    {

        private final EntityType entityType;

        private final List<Long> allEntityIds;

        private final PersonPE registrator;

        private final String reason;

        private final DeletionEventSQLs sqls;

        public DeleteDataSetsPermanentlyBatchOperation(EntityType entityType,
                List<Long> allEntityIds, PersonPE registrator, String reason,
                DeletionEventSQLs sqls)
        {
            this.entityType = entityType;
            this.allEntityIds = allEntityIds;
            this.registrator = registrator;
            this.reason = reason;
            this.sqls = sqls;
        }

        @Override
        public List<Long> getAllEntities()
        {
            return allEntityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityType.name();
        }

        @Override
        public String getOperationName()
        {
            return "permanently deleting";
        }

        @Override
        public void execute(final List<Long> batchEntityIds)
        {
            executeStatelessAction(createPermanentDeleteAction(batchEntityIds));
        }

        private StatelessHibernateCallback createPermanentDeleteAction(
                final List<Long> entityIdsToDelete)
        {
            return new PermanentDeletionAction(entityIdsToDelete);
        }

        private class PermanentDeletionAction implements StatelessHibernateCallback
        {
            private final List<Long> entityIdsToDelete;

            public PermanentDeletionAction(List<Long> entityIdsToDelete)
            {
                this.entityIdsToDelete = entityIdsToDelete;
            }

            @Override
            public Object doInStatelessSession(StatelessSession session)
            {
                final NativeQuery<?> selectPermIds = session.createNativeQuery(sqls.selectPermIds);
                final NativeQuery<?> selectLocations = session.createNativeQuery(sqls.selectLocations);
                final NativeQuery<?> deleteProperties = session.createNativeQuery(sqls.deleteProperties);
                final NativeQuery<?> deleteEntities = session.createNativeQuery(sqls.deleteDataSets);
                final NativeQuery<?> insertEvent = session.createNativeQuery(sqls.insertEvent);
                final NativeQuery<?> deleteExternalData = session.createNativeQuery(sqls.deleteExternalData);
                final NativeQuery<?> deleteContentCopies = session.createNativeQuery(sqls.deleteContentCopies);
                final NativeQuery<?> deleteChildrenConnections = session.createNativeQuery(sqls.deleteChildrenConnections);
                final NativeQuery<?> deleteParentConnections = session.createNativeQuery(sqls.deleteParentConnections);

                final List<String> permIds =
                        selectPermIds(selectPermIds, entityIdsToDelete);
                if (permIds.isEmpty())
                {
                    return null;
                }
                final List<DeletedDataSetLocation> locations =
                        selectLocations(selectLocations, entityIdsToDelete);

                String content = historyCreator.apply(session, entityIdsToDelete, sqls.selectPropertyHistory,
                        sqls.selectRelationshipHistory, sqls.selectAttributes, null, null, registrator);

                executeUpdate(deleteProperties, entityIdsToDelete);
                executeUpdate(deleteExternalData, entityIdsToDelete);
                executeUpdate(deleteContentCopies, entityIdsToDelete);
                executeUpdate(deleteChildrenConnections, entityIdsToDelete);
                executeUpdate(deleteParentConnections, entityIdsToDelete);
                executeUpdate(deleteEntities, entityIdsToDelete);
                insertEvent(insertEvent, permIds, locations, content);
                return null;
            }

            private List<String> selectPermIds(final SQLQuery sqlQuerySelectPermIds,
                    final List<Long> entityIds)
            {
                sqlQuerySelectPermIds.setParameterList(ENTITY_IDS_PARAM, entityIds);
                final List<String> permIdsOrNull = cast(sqlQuerySelectPermIds.list());
                return permIdsOrNull == null ? Collections.<String> emptyList() : permIdsOrNull;
            }

            private List<DeletedDataSetLocation> selectLocations(
                    final SQLQuery sqlQuerySelectLocations, final List<Long> entityIds)
            {
                sqlQuerySelectLocations.setParameterList(ENTITY_IDS_PARAM, entityIds);
                sqlQuerySelectLocations.setResultTransformer(new ResultTransformer()
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public Object transformTuple(Object[] values, String[] aliases)
                        {
                            DeletedDataSetLocation location = new DeletedDataSetLocation();
                            location.setLocation((String) values[0]);
                            location.setShareId((String) values[1]);
                            location.setDatastoreCode((String) values[2]);
                            return location;
                        }

                        @SuppressWarnings("rawtypes")
                        @Override
                        public List transformList(List list)
                        {
                            return list;
                        }
                    });
                return cast(sqlQuerySelectLocations.list());
            }

            private void executeUpdate(final NativeQuery<?> a1,
                    List<Long> entityIds)
            {
                a1.setParameterList(ENTITY_IDS_PARAM, entityIds);
                a1.executeUpdate();
            }

            private void insertEvent(final SQLQuery sqlQueryInsertEvent,
                    final List<String> permIds, final List<DeletedDataSetLocation> locations, String content)
            {
                sqlQueryInsertEvent.setParameter(EVENT_TYPE_PARAM, EventType.DELETION.name());
                sqlQueryInsertEvent.setParameter(REASON_PARAM, reason);
                sqlQueryInsertEvent.setParameter(CONTENT_PARAM, content);
                sqlQueryInsertEvent.setParameter(REGISTRATOR_ID_PARAM, registrator.getId());
                sqlQueryInsertEvent.setParameter(ENTITY_TYPE_PARAM, entityType.name());

                IToStringConverter<Object> toStringConverter = new IToStringConverter<Object>()
                    {
                        IToStringConverter<Object> delegatee = ToStringDefaultConverter
                                .getInstance();

                        @Override
                        public String toString(Object value)
                        {
                            return value == null ? "" : delegatee.toString(value);
                        }
                    };

                final String allPermIdsAsString =
                        CollectionUtils.abbreviate(permIds, -1, toStringConverter,
                                CollectionStyle.NO_BOUNDARY);
                sqlQueryInsertEvent.setParameter(IDENTIFIERS_PARAM, allPermIdsAsString);

                sqlQueryInsertEvent.setParameter(DESCRIPTION_PARAM,
                        DeletedDataSetLocation.format(locations));

                sqlQueryInsertEvent.executeUpdate();
            }
        }

    }

    @Override
    public Map<Long, Set<Long>> mapDataSetIdsByChildrenIds(final Collection<Long> children, final Long relationship)
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final List<Object[]> results = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public final Object doInHibernate(final Session session)
                {
                    InQuery<Long, Object> inQuery = new InQuery<>();
                    Map<String, Object> fixParams = new HashMap<String, Object>();
                    fixParams.put("relationship", relationship);

                    String query = "select data_id_child, data_id_parent from " + TableNames.DATA_SET_RELATIONSHIPS_VIEW
                            + " where relationship_id = :relationship and data_id_child in (:children)";
                    return inQuery.withBatch(session, query, "children", new ArrayList<>(children), fixParams);
                }
            });

        Map<Long, Set<Long>> childIdToParentIdsMap = new HashMap<Long, Set<Long>>();

        for (Object[] result : results)
        {
            Number childId = (Number) result[0];
            Number parentId = (Number) result[1];

            Set<Long> parentIds = childIdToParentIdsMap.get(childId);
            if (parentIds == null)
            {
                parentIds = new HashSet<Long>();
                childIdToParentIdsMap.put(childId.longValue(), parentIds);
            }

            parentIds.add(parentId.longValue());
        }

        return childIdToParentIdsMap;
    }

    @Override
    public Set<TechId> findParentIds(final Collection<TechId> dataSetIds, final long relationshipTypeId)
    {
        return findRelatedIds("data_id_parent", "data_id_child", dataSetIds, relationshipTypeId);
    }

    @Override
    public Set<TechId> findChildrenIds(Collection<TechId> dataSetIds, long relationshipTypeId)
    {
        return findRelatedIds("data_id_child", "data_id_parent", dataSetIds, relationshipTypeId);
    }

    private Set<TechId> findRelatedIds(String side1, String side2, final Collection<TechId> dataSetIds, final long relationshipTypeId)
    {
        // Native SQL query is used to be able to query on 'many-to-many association table -
        // - 'data_set_relationships' without join with 'data' table involved in this association.
        // Drawback of this solution is that instead of list of Long values we get list of
        // BigIntegers and so a transformation is needed.

        final String query =
                "select " + side1 + " from data_set_relationships where " + side2 + " in (:ids) and relationship_id = :type";
        InQueryScroller<TechId> dataSetIdsScroller = new InQueryScroller<>(dataSetIds, 2);
        List<TechId> partialDataSetIds = null;
        Set<TechId> result = new HashSet<>();
        while ((partialDataSetIds = dataSetIdsScroller.next()) != null)
        {
            Set<TechId> partialResult = findRelatedIds(query, partialDataSetIds, relationshipTypeId);
            result.addAll(partialResult);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<TechId> findRelatedIds(final String query, final Collection<TechId> dataSetIds, final long relationshipTypeId)
    {
        @SuppressWarnings("rawtypes")
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        @Override
                        public final Object doInHibernate(final Session session)
                        {
                            // we could remove this transformation if we choose to pass Long values
                            final List<Long> longIds = TechId.asLongs(dataSetIds);
                            return session.createSQLQuery(query).setParameterList("ids", longIds)
                                    .setParameter("type", relationshipTypeId)
                                    .list();
                        }
                    });
        return transformNumbers2TechIdSet(results);
    }

    // data set relationship helper methods

    @Override
    public List<DataPE> listByCode(Set<String> values)
    {
        return listByIDsOfName("code", values);
    }

    @Override
    public List<DataPE> listByIDs(Collection<Long> ids)
    {
        return listByIDsOfName("id", ids);
    }

    public List<DataPE> listByIDsOfName(String idName, Collection<?> dataSetIds)
    {
        if (dataSetIds == null || dataSetIds.isEmpty())
        {
            return new ArrayList<DataPE>();
        }
        final List<DataPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), DataPE.class, idName, dataSetIds);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d data set(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public List<SpacePE> listSpacesByDataSetIds(Collection<Long> values)
    {
        final List<Long> allIds = new ArrayList<Long>(values);
        final String query =
                "from " + SpacePE.class.getSimpleName()
                        + " as s where s.id in (select p.space.id from "
                        + ProjectPE.class.getSimpleName()
                        + " as p where p.id in (select e.projectInternal.id from "
                        + ExperimentPE.class.getSimpleName()
                        + " as e where e.id in (select d.experimentInternal.id from "
                        + DataPE.class.getSimpleName() + " as d where d.id in (:ids))))";
        final List<SpacePE> result = new ArrayList<SpacePE>();
        BatchOperationExecutor.executeInBatches(new IBatchOperation<Long>()
            {
                @Override
                public void execute(List<Long> ids)
                {
                    List<SpacePE> spaces =
                            cast(getHibernateTemplate().findByNamedParam(query, "ids", ids));
                    result.addAll(spaces);
                }

                @Override
                public List<Long> getAllEntities()
                {
                    return allIds;
                }

                @Override
                public String getEntityName()
                {
                    return "space";
                }

                @Override
                public String getOperationName()
                {
                    return "listSpacesByDataSetIds";
                }
            });

        return result;
    }

    @Override
    public void updateDataSets(List<DataPE> dataSets, PersonPE modifier)
    {
        assert dataSets != null : "Data sets not defined";

        try
        {
            final HibernateTemplate hibernateTemplate = getHibernateTemplate();
            for (DataPE data : dataSets)
            {
                validatePE(data);
                data.setCode(CodeConverter.tryToDatabase(data.getCode()));
                hibernateTemplate.saveOrUpdate(data);
            }

            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("UPDATE: %d data sets.", dataSets.size()));
            }

            flushWithSqlExceptionHandling(getHibernateTemplate());
            scheduleDynamicPropertiesEvaluation(dataSets);

            // if session is not cleared registration of many samples slows down after each batch
            hibernateTemplate.clear();
        } catch (DataAccessException e)
        {
            DataSetDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public final void validateAndSaveUpdatedEntity(DataPE entity) throws DataAccessException
    {
        flushWithSqlExceptionHandling(getHibernateTemplate());
        super.validateAndSaveUpdatedEntity(entity);
        scheduleDynamicPropertiesEvaluation(Arrays.asList(entity));
    }

    @Override
    protected void scheduleDynamicPropertiesEvaluation(List<DataPE> dataSets)
    {
        List<DataPE> toUpdate = new ArrayList<DataPE>();
        addAllDataSetsAndComponentsRecursively(toUpdate, dataSets);
        super.scheduleDynamicPropertiesEvaluation(toUpdate);
    }

    private void addAllDataSetsAndComponentsRecursively(List<DataPE> resultDataSets, List<DataPE> dataSets)
    {
        for (DataPE dataSet : dataSets)
        {
            resultDataSets.add(dataSet);
            addAllDataSetsAndComponentsRecursively(resultDataSets, dataSet.getContainedDataSets());
        }
    }

    @Override
    public List<TechId> listDataSetIdsBySampleIds(final Collection<TechId> samples)
    {
        final List<Long> longIds = TechId.asLongs(samples);
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(DataPE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, "sampleInternal.id", longIds);

        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .info(String.format("found %s data sets for given samples", results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    public List<TechId> listDataSetIdsByExperimentIds(final Collection<TechId> experiments)
    {
        final List<Long> longIds = TechId.asLongs(experiments);
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(DataPE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, "experimentInternal.id", longIds);
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %s data sets for given experiments",
                    results.size()));
        }
        return transformNumbers2TechIdList(results);
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

    @Override
    public boolean confirmStorage(String dataSetCode)
    {
        SQLQuery query =
                currentSession().createSQLQuery(
                        "update external_data set storage_confirmation = true "
                                + "where storage_confirmation = false "
                                + "and id in (select id from data_all where code = :code)");
        query.setString("code", CodeConverter.tryToDatabase(dataSetCode));
        return query.executeUpdate() > 0;
    }

    @Override
    public boolean isAccessTimestampEnabled()
    {
        if (isAccessTimestampColumnDefined == null)
        {
            isAccessTimestampColumnDefined = isAccessTimestampColumnDefined();
            if (isAccessTimestampColumnDefined)
            {
                operationLog.info("Access timestamp column for data sets is enabled");
            } else
            {
                operationLog.info("Access timestamp column for data sets is not enabled");
            }
        }
        return isAccessTimestampColumnDefined;
    }

    private boolean isAccessTimestampColumnDefined()
    {
        SQLQuery query =
                currentSession().createSQLQuery(
                        "SELECT column_name FROM information_schema.columns WHERE table_name='data_all' and column_name='"
                                + ColumnNames.ACCESS_TIMESTAMP + "'");
        return query.list().size() > 0;
    }

    @Override
    public boolean updateAccessTimestamp(String dataSetCode)
    {
        if (isAccessTimestampEnabled())
        {
            SQLQuery query =
                    currentSession().createSQLQuery(
                            "update data_all set " + ColumnNames.ACCESS_TIMESTAMP + " = current_timestamp "
                                    + "where code = :code");
            query.setString("code", CodeConverter.tryToDatabase(dataSetCode));
            return query.executeUpdate() > 0;
        }
        return false;
    }

    @Override
    public boolean exists(String dataSetCode)
    {
        SQLQuery query =
                currentSession().createSQLQuery("select count(*) from data_all where code = :code");
        query.setString("code", CodeConverter.tryToDatabase(dataSetCode));
        Number count = (Number) query.uniqueResult();
        return count != null && count.intValue() > 0;
    }

}
