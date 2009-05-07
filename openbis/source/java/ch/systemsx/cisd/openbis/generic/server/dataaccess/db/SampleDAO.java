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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Implementation of {@link ISampleDAO} for databases.
 * 
 * @author Tomasz Pylak
 */
public class SampleDAO extends AbstractGenericEntityDAO<SamplePE> implements ISampleDAO
{
    private final static Class<SamplePE> ENTITY_CLASS = SamplePE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    @Override
    Class<SamplePE> getEntityClass()
    {
        return ENTITY_CLASS;
    }

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleDAO.class);

    private static final String LOCK_TABLE_SQL =
            "LOCK TABLE " + TableNames.SAMPLES_TABLE + " IN EXCLUSIVE MODE";

    SampleDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    private final Criteria createListSampleForTypeCriteria(final SampleTypePE sampleType)
    {
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("sampleType", sampleType));
        fetchRelations(criteria, "container", sampleType.getContainerHierarchyDepth());
        fetchRelations(criteria, "generatedFrom", sampleType.getGeneratedFromHierarchyDepth());

        criteria.setFetchMode("experiment", FetchMode.JOIN);

        return criteria;
    }

    private final void fetchRelations(final Criteria criteria, final String relationName,
            final int relationDepth)
    {
        String relationPath = relationName;
        for (int i = 0; i < relationDepth; i++)
        {
            criteria.setFetchMode(relationPath, FetchMode.JOIN);
            relationPath += "." + relationName;
        }
    }

    /**
     * Obtains an explicit exclusive lock on 'samples' table. This function should always be
     * executed before saving a sample because we have a complex unique code check in a trigger and
     * we don't want any race condition or deadlock (if lock is gathered in the trigger). See
     * [LMS-814] for details.
     */
    private final void lockTable()
    {
        executeUpdate(LOCK_TABLE_SQL);
    }

    /**
     * <b>IMPORTANT</b> - every method which executes this method should first obtain lock on table
     * using {@link SampleDAO#lockTable()}. The obtained lock is reentrant so this method could as
     * well obtain it itself with a small additional cost if there are many saves in one
     * transaction.
     */
    private final void internalCreateSample(final SamplePE sample,
            final HibernateTemplate hibernateTemplate)
    {
        validatePE(sample);
        sample.setCode(CodeConverter.tryToDatabase(sample.getCode()));

        hibernateTemplate.saveOrUpdate(sample);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: sample '%s'.", sample));
        }
    }

    //
    // ISampleDAO
    //

    public final void createSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        lockTable();
        internalCreateSample(sample, hibernateTemplate);
        hibernateTemplate.flush();
    }

    public final List<SamplePE> listSamplesByTypeAndGroup(final SampleTypePE sampleType,
            final GroupPE group) throws DataAccessException
    {
        assert sampleType != null : "Unspecified sample type.";
        assert group != null : "Unspecified group.";

        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        criteria.add(Restrictions.eq("group", group));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<SamplePE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type '%s' and group '%s'.", list.size(),
                    sampleType, group));
        }
        return list;
    }

    public final List<SamplePE> listSamplesByTypeAndDatabaseInstance(final SampleTypePE sampleType,
            final DatabaseInstancePE databaseInstance)
    {
        assert sampleType != null : "Unspecified sample type.";
        assert databaseInstance != null : "Unspecified database instance.";

        final Criteria criteria = createListSampleForTypeCriteria(sampleType);
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<SamplePE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for sample type '%s' and database instance '%s'.",
                    list.size(), sampleType, databaseInstance));
        }
        return list;
    }

    public final SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance, final HierarchyType hierarchyType)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert databaseInstance != null : "Unspecified database instance.";
        assert hierarchyType != null : "Unspecified hierarchy type.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
        criteria.add(Restrictions.eq("databaseInstance", databaseInstance));
        criteria.add(Restrictions.isNull(hierarchyType.getOppositeHierarchyType()
                .getParentFieldName()));
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String
                    .format("Following sample '%s' has been found for "
                            + "code '%s' and database instance '%s'.", sample, sampleCode,
                            databaseInstance));
        }
        return sample;
    }

    public final SamplePE tryFindByCodeAndGroup(final String sampleCode, final GroupPE group,
            final HierarchyType hierarchyType)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert group != null : "Unspecified group.";
        assert hierarchyType != null : "Unspecified hierarchy type.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
        criteria.add(Restrictions.eq("group", group));
        criteria.add(Restrictions.isNull(hierarchyType.getOppositeHierarchyType()
                .getParentFieldName()));
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following sample '%s' has been found for code '%s' and group '%s'.", sample,
                    sampleCode, group));
        }
        return sample;
    }

    public final List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample)
    {
        assert sample != null : "Unspecified sample.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        final String hql = String.format("from %s s where s.generatedFrom = ?", TABLE_NAME);
        final List<SamplePE> list = cast(hibernateTemplate.find(hql, toArray(sample)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d sample(s) have been found for \"generatedFrom\" sample '%s'.", list.size(),
                    sample));
        }
        return list;
    }

    public final List<SamplePE> listSamplesByContainer(final SamplePE container)
            throws DataAccessException
    {
        assert container != null : "Unspecified container.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        final String hql =
                String
                        .format("from %s s join fetch s.container as cont where cont = ?",
                                TABLE_NAME);
        final List<SamplePE> list = cast(hibernateTemplate.find(hql, toArray(container)));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d sample(s) have been found for \"partOf\" sample '%s'.", list.size(),
                    container));
        }
        return list;
    }

    public final void createSamples(final List<SamplePE> samples) throws DataAccessException
    {
        assert samples != null && samples.size() > 0 : "Unspecified or empty samples.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        lockTable();
        for (final SamplePE samplePE : samples)
        {
            internalCreateSample(samplePE, hibernateTemplate);
        }
        hibernateTemplate.flush();
    }

    public final void updateSample(final SamplePE sample) throws DataAccessException
    {
        assert sample != null : "Unspecified sample";
        validatePE(sample);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("UPDATE: sample '" + sample + "'.");
        }
    }
}
