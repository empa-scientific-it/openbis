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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Implementation of {@link ISampleDAO} for databases.
 * 
 * @author Tomasz Pylak
 */
public class SampleDAO extends AbstractGenericEntityWithPropertiesDAO<SamplePE> implements
        ISampleDAO
{
    private final static Class<SamplePE> ENTITY_CLASS = SamplePE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SampleDAO.class);

    SampleDAO(final PersistencyResources persistencyResources, EntityHistoryCreator historyCreator)
    {
        super(persistencyResources, SamplePE.class, historyCreator);
    }

    // LockSampleModificationsInterceptor automatically obtains lock
    private final void internalCreateOrUpdateSample(final SamplePE sample, final PersonPE modifier,
            final HibernateTemplate hibernateTemplate, final boolean doLog)
    {
        validatePE(sample);
        sample.setCode(CodeConverter.tryToDatabase(sample.getCode()));
        if (sample.getModificationDate() == null)
        {
            sample.setModificationDate(getTransactionTimeStamp());
        }
        lockEntity(sample.getExperiment());
        lockEntity(sample.getContainer());
        lockEntities(sample.getParents());
        hibernateTemplate.saveOrUpdate(sample);
        if (doLog && operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: sample '%s'.", sample));
        }
    }

    //
    // ISampleDAO
    //

    @Override
    public final void createOrUpdateSample(final SamplePE sample, final PersonPE modifier)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        try
        {
            final HibernateTemplate hibernateTemplate = getHibernateTemplate();

            internalCreateOrUpdateSample(sample, modifier, hibernateTemplate, true);

            // need to deal with exception thrown by trigger checking code uniqueness
            flushWithSqlExceptionHandling(hibernateTemplate);
            scheduleDynamicPropertiesEvaluation(Collections.singletonList(sample));
            scheduleDynamicPropertiesEvaluation(getDynamicPropertyEvaluatorScheduler(),
                    DataPE.class, new ArrayList<DataPE>(sample.getDatasets()));
        } catch (DataAccessException e)
        {
            SampleDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public final List<SamplePE> listSamplesByGeneratedFrom(final SamplePE sample)
    {
        return sample.getGenerated();
    }

    @Override
    public final List<SamplePE> listSamplesBySpaceAndProperty(final String propertyCode,
            final String propertyValue, final SpacePE space) throws DataAccessException
    {
        assert space != null : "Unspecified space.";
        assert propertyCode != null : "Unspecified property code";
        assert propertyValue != null : "Unspecified property value";

        String queryFormat =
                "from " + SamplePropertyPE.class.getSimpleName()
                        + " where %s = ? and entity.space = ? "
                        + " and entityTypePropertyType.propertyTypeInternal.simpleCode = ?"
                        + " and entityTypePropertyType.propertyTypeInternal.managedInternally = ?";
        List<SamplePE> entities =
                listByPropertyValue(queryFormat, propertyCode, propertyValue, space);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d samples have been found for space '%s' and property '%s' equal to '%s'.",
                    entities.size(), space, propertyCode, propertyValue));
        }
        return entities;
    }

    private List<SamplePE> listByPropertyValue(String queryFormat, String propertyCode,
            String propertyValue, SpacePE parent)
    {
        String simplePropertyCode = CodeConverter.tryToDatabase(propertyCode);
        boolean isInternalNamespace = CodeConverter.isInternalNamespace(propertyCode);
        Object[] arguments =
                toArray(propertyValue, parent, simplePropertyCode, isInternalNamespace);

        String queryPropertySimpleValue = String.format(queryFormat, "value");
        List<SamplePropertyPE> properties1 =
                cast(getHibernateTemplate().find(queryPropertySimpleValue, arguments));

        String queryPropertyVocabularyTerm = String.format(queryFormat, "vocabularyTerm.code");
        List<SamplePropertyPE> properties2 =
                cast(getHibernateTemplate().find(queryPropertyVocabularyTerm, arguments));

        properties1.addAll(properties2);
        List<SamplePE> entities = extractEntities(properties1);
        return entities;
    }

    private static List<SamplePE> extractEntities(List<SamplePropertyPE> properties)
    {
        List<SamplePE> samples = new ArrayList<SamplePE>();
        for (SamplePropertyPE prop : properties)
        {
            samples.add(prop.getEntity());
        }
        return samples;
    }

    @Override
    public SamplePE tryToFindByPermID(String permID) throws DataAccessException
    {
        assert permID != null : "Unspecified permanent ID.";
        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("permId", permID));
        criteria.setFetchMode("sampleType.sampleTypePropertyTypesInternal", FetchMode.JOIN);
        final SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Following sample '%s' has been found for "
                    + "permanent ID '%s'.", sample, permID));
        }
        return sample;
    }

    @Override
    public final SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode)
    {
        assert sampleCode != null : "Unspecified sample code.";

        Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        addSampleCodeCriterion(criteria, sampleCode);
        criteria.add(Restrictions.isNull("space"));
        SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (sample == null && isFullCode(sampleCode) == false)
        {
            criteria = currentSession().createCriteria(ENTITY_CLASS);
            criteria.add(Restrictions.isNull("space"));
            sample = tryFindContainedSampleWithUniqueSubcode(criteria, sampleCode);
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String
                    .format("Following sample '%s' has been found for "
                            + "code '%s'.", sample, sampleCode));
        }
        return sample;
    }

    @Override
    public final List<SamplePE> listByCodesAndDatabaseInstance(final List<String> sampleCodes,
            final String containerCodeOrNull)
    {
        assert sampleCodes != null : "Unspecified sample codes.";

        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        return listByCodes(criteria, sampleCodes, containerCodeOrNull);
    }

    @Override
    public SamplePE tryfindByCodeAndProject(String sampleCode, ProjectPE project)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert project != null : "Unspecified project.";

        Criteria criteria = createProjectCriteria(project);
        addSampleCodeCriterion(criteria, sampleCode);
        SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (sample == null && isFullCode(sampleCode) == false)
        {
            criteria = createProjectCriteria(project);
            sample = tryFindContainedSampleWithUniqueSubcode(criteria, sampleCode);
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following sample '%s' has been found for code '%s' and project '%s'.", sample,
                    sampleCode, project));
        }
        return sample;
    }

    @Override
    public final SamplePE tryFindByCodeAndSpace(final String sampleCode, final SpacePE space)
    {
        assert sampleCode != null : "Unspecified sample code.";
        assert space != null : "Unspecified space.";

        Criteria criteria = createSpaceCriteria(space);
        addSampleCodeCriterion(criteria, sampleCode);
        SamplePE sample = (SamplePE) criteria.uniqueResult();
        if (sample == null && isFullCode(sampleCode) == false)
        {
            criteria = createSpaceCriteria(space);
            sample = tryFindContainedSampleWithUniqueSubcode(criteria, sampleCode);
        }
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following sample '%s' has been found for code '%s' and space '%s'.", sample,
                    sampleCode, space));
        }
        return sample;
    }

    @Override
    public final List<SamplePE> listByCodesAndSpace(final List<String> sampleCodes,
            final String containerCodeOrNull, final SpacePE space)
    {
        assert sampleCodes != null : "Unspecified sample codes.";
        assert space != null : "Unspecified space.";

        Criteria criteria = createSpaceCriteria(space);
        return listByCodes(criteria, sampleCodes, containerCodeOrNull);
    }

    @Override
    public List<SamplePE> listByCodesAndProject(List<String> sampleCodes, String containerCodeOrNull, ProjectPE project)
    {
        assert sampleCodes != null : "Unspecified sample codes.";
        assert project != null : "Unspecified project.";

        Criteria criteria = createProjectCriteria(project);
        return listByCodes(criteria, sampleCodes, containerCodeOrNull);
    }

    private List<SamplePE> listByCodes(Criteria criteria, List<String> sampleCodes, String containerCodeOrNull)
    {
        addSampleCodesCriterion(criteria, sampleCodes, containerCodeOrNull);
        List<SamplePE> result = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s samples has been found", result.size()));
        }
        return result;
    }

    private boolean isFullCode(String sampleCode)
    {
        return sampleCode.contains(SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
    }

    private SamplePE tryFindContainedSampleWithUniqueSubcode(Criteria criteria, String sampleCode)
    {
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(sampleCode)));
        criteria.add(Restrictions.isNotNull("container"));
        List<SamplePE> list = cast(criteria.list());
        return list.size() == 1 ? list.get(0) : null;
    }

    private Criteria createFindCriteria(Criterion criterion)
    {
        final Criteria criteria = currentSession().createCriteria(ENTITY_CLASS);
        criteria.setFetchMode("sampleType.sampleTypePropertyTypesInternal", FetchMode.JOIN);
        criteria.add(criterion);
        return criteria;
    }

    private Criteria createSpaceCriteria(final SpacePE space)
    {
        Criteria criteria = createFindCriteria(Restrictions.eq("space", space));
        criteria.add(Restrictions.isNull("projectInternal"));
        return criteria;
    }

    private Criteria createProjectCriteria(final ProjectPE project)
    {
        return createFindCriteria(Restrictions.eq("projectInternal", project));
    }

    private void addSampleCodesCriterion(Criteria criteria, List<String> sampleCodes,
            String containerCodeOrNull)
    {
        List<String> convertedCodes = new ArrayList<String>();
        for (String sampleCode : sampleCodes)
        {
            convertedCodes.add(CodeConverter.tryToDatabase(sampleCode));
        }
        criteria.add(Restrictions.in("code", convertedCodes));
        criteria.setFetchMode("sampleProperties", FetchMode.JOIN);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        addSampleContainerCriterion(criteria, containerCodeOrNull);
    }

    private void addSampleCodeCriterion(Criteria criteria, String sampleCode)
    {
        String[] sampleCodeTokens =
                sampleCode.split(SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
        String subCode = sampleCodeTokens.length > 1 ? sampleCodeTokens[1] : sampleCode;
        String containerCodeOrNull = sampleCodeTokens.length > 1 ? sampleCodeTokens[0] : null;
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(subCode)));
        addSampleContainerCriterion(criteria, containerCodeOrNull);
    }

    private void addSampleContainerCriterion(Criteria criteria, String containerCodeOrNull)
    {
        if (containerCodeOrNull != null)
        {
            criteria.createAlias("container", "c");
            criteria.add(Restrictions
                    .eq("c.code", CodeConverter.tryToDatabase(containerCodeOrNull)));
        } else
        {
            criteria.add(Restrictions.isNull("container"));
        }
    }

    @Override
    public final void createOrUpdateSamples(final List<SamplePE> samples, final PersonPE modifier,
            boolean clearCache)
            throws DataAccessException
    {
        assert samples != null && samples.size() > 0 : "Unspecified or empty samples.";

        try
        {
            final HibernateTemplate hibernateTemplate = getHibernateTemplate();

            for (final SamplePE samplePE : samples)
            {
                internalCreateOrUpdateSample(samplePE, modifier, hibernateTemplate, false);
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("ADD: %d samples.", samples.size()));
            }

            // need to deal with exception thrown by trigger checking code uniqueness
            flushWithSqlExceptionHandling(getHibernateTemplate());
            scheduleDynamicPropertiesEvaluation(samples);

            if (clearCache)
            {
                hibernateTemplate.clear();
            }
        } catch (DataAccessException e)
        {
            SampleDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public final void updateSample(final SamplePE sample, final PersonPE modifier)
            throws DataAccessException
    {
        assert sample != null : "Unspecified sample";

        try
        {
            sample.setModifier(modifier);
            validatePE(sample);

            // need to deal with exception thrown by trigger checking code uniqueness
            flushWithSqlExceptionHandling(getHibernateTemplate());
            scheduleDynamicPropertiesEvaluation(Collections.singletonList(sample));

            if (operationLog.isInfoEnabled())
            {
                operationLog.info("UPDATE: sample '" + sample + "'.");
            }
        } catch (DataAccessException e)
        {
            SampleDataAccessExceptionTranslator.translateAndThrow(e);
        }
    }

    @Override
    public List<SamplePE> listByPermID(Collection<String> values)
    {
        return listByIDsOfName("permId", values);
    }

    @Override
    public List<SamplePE> listByIDs(Collection<Long> ids)
    {
        return listByIDsOfName("id", ids);
    }

    private List<SamplePE> listByIDsOfName(String idName, Collection<?> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<SamplePE>();
        }
        final List<SamplePE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), SamplePE.class, idName, values);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d sample(s) have been found.", list.size()));
        }
        return list;
    }

    @Override
    public void delete(final List<TechId> sampleIds, final PersonPE registrator, final String reason)
            throws DataAccessException
    {
        // NOTE: we use SAMPLES_ALL_TABLE, not DELETED_SAMPLES_VIEW because we still want to be
        // able to directly delete samples without going to trash (trash may be disabled)
        final String samplesTable = TableNames.SAMPLES_ALL_TABLE;

        final String sqlSelectPermIds = SQLBuilder.createSelectPermIdsSQL(samplesTable);
        final String sqlDeleteProperties =
                SQLBuilder.createDeletePropertiesSQL(TableNames.SAMPLE_PROPERTIES_TABLE,
                        ColumnNames.SAMPLE_COLUMN);
        final String sqlDeleteAttachments =
                SQLBuilder.createDeleteAttachmentsSQL(ColumnNames.SAMPLE_COLUMN);
        final String sqlDeleteSamples = SQLBuilder.createDeleteEnitiesSQL(samplesTable);
        final String sqlInsertEvent = SQLBuilder.createInsertEventSQL();
        final String sqlSelectPropertyHistory = createQueryPropertyHistorySQL();
        final String sqlSelectRelationshipHistory = createQueryRelationshipHistorySQL();
        final String sqlSelectAttributes = createQueryAttributesSQL();

        executePermanentDeleteAction(EntityType.SAMPLE, sampleIds, registrator, reason,
                sqlSelectPermIds, sqlDeleteProperties,
                sqlDeleteAttachments, sqlDeleteSamples, sqlInsertEvent, sqlSelectPropertyHistory,
                sqlSelectRelationshipHistory, sqlSelectAttributes, null, AttachmentHolderKind.SAMPLE);
    }

    private static String createQueryPropertyHistorySQL()
    {
        return "("
                + "SELECT s.perm_id, pt.code, coalesce(h.value, h.vocabulary_term, h.material) as value, "
                + "p.user_id, h.valid_from_timestamp, h.valid_until_timestamp "
                + "FROM samples_all s, sample_properties_history h, sample_type_property_types stpt, property_types pt, persons p "
                + "WHERE h.samp_id " + SQLBuilder.inEntityIds() + " AND "
                + "s.id = h.samp_id AND "
                + "h.stpt_id = stpt.id AND "
                + "stpt.prty_id = pt.id AND "
                + "pers_id_author = p.id "
                + ") UNION ("
                + "SELECT s.perm_id, pt.code, coalesce(value, "
                + "(SELECT (t.code || ' [' || v.code || ']') "
                + "FROM controlled_vocabulary_terms as t JOIN controlled_vocabularies as v ON t.covo_id = v.id "
                + "WHERE t.id = pr.cvte_id), "
                + "(SELECT (m.code || ' [' || mt.code || ']') "
                + "FROM materials AS m JOIN material_types AS mt ON m.maty_id = mt.id "
                + "WHERE m.id = pr.mate_prop_id)) as value, "
                + "author.user_id, pr.modification_timestamp, null "
                + "FROM samples_all s, sample_properties pr, sample_type_property_types stpt, property_types pt, persons author "
                + "WHERE pr.samp_id " + SQLBuilder.inEntityIds() + " AND "
                + "s.id = pr.samp_id AND "
                + "pr.stpt_id = stpt.id AND "
                + "stpt.prty_id = pt.id AND "
                + "pr.pers_id_author = author.id "
                + ") "
                + " ORDER BY 1, valid_from_timestamp";
    }

    private static String createQueryRelationshipHistorySQL()
    {
        return "SELECT s.perm_id, h.relation_type, h.entity_perm_id, " + ENTITY_TYPE + ", "
                + "p.user_id, h.valid_from_timestamp, h.valid_until_timestamp "
                + "FROM samples_all s, sample_relationships_history h, persons p "
                + "WHERE s.id = h.main_samp_id AND "
                + "h.main_samp_id " + SQLBuilder.inEntityIds() + " AND "
                + "h.pers_id_author = p.id "
                + "ORDER BY 1, valid_from_timestamp";
    }

    private static final String ENTITY_TYPE = "case "
            + "when h.space_id is not null then 'SPACE' "
            + "when h.samp_id is not null then 'SAMPLE' "
            + "when h.proj_id is not null then 'PROJECT' "
            + "when h.expe_id is not null then 'EXPERIMENT' "
            + "else 'UNKNOWN' end as entity_type";

    private static String createQueryAttributesSQL()
    {
        return "SELECT s.id, s.perm_id, s.code, t.code as entity_type, "
                + "s.registration_timestamp, r.user_id as registrator "
                + "FROM samples_all s "
                + "JOIN sample_types t on s.saty_id = t.id "
                + "JOIN persons r on s.pers_id_registerer = r.id "
                + "WHERE s.id " + SQLBuilder.inEntityIds();
    }

    @Override
    public void deletePermanently(final DeletionPE deletion, final PersonPE registrator)
    {
        getHibernateTemplate().execute(new HibernateCallback<Object>()
            {
                @SuppressWarnings("unchecked")
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    String permIdQuery = "SELECT id, perm_id FROM samples_all WHERE del_id = :id";

                    String properties =
                            "DELETE FROM sample_properties WHERE samp_id IN ("
                                    + "SELECT id FROM samples_all WHERE del_id = :id)";

                    String attachmentContentIdQuery =
                            "SELECT exac_id FROM attachments WHERE samp_id IN (SELECT id FROM samples_all WHERE del_id = :id)";

                    String attachments =
                            "DELETE FROM attachments WHERE samp_id IN ("
                                    + "SELECT id FROM samples_all WHERE del_id = :id)";

                    String attachmentContents =
                            "DELETE FROM attachment_contents WHERE id IN (:ids)";

                    String samples =
                            "DELETE FROM samples_all WHERE del_id = :id";

                    String event =
                            "INSERT INTO events (id, event_type, description, reason, pers_id_registerer, entity_type, identifiers, content) "
                                    + "VALUES (nextval('EVENT_ID_SEQ'), 'DELETION', :description, :reason, :registerer, 'SAMPLE', :identifiers, :content)";

                    SQLQuery getPermIds = session.createSQLQuery(permIdQuery);
                    getPermIds.setParameter("id", deletion.getId());

                    StringBuffer permIdList = new StringBuffer();
                    List<Long> entityIdsToDelete = new ArrayList<>();
                    for (Object[] result : (List<Object[]>) getPermIds.list())
                    {
                        permIdList.append(", ");
                        permIdList.append((String) result[1]);
                        entityIdsToDelete.add(((BigInteger) result[0]).longValue());
                    }

                    if (permIdList.length() == 0)
                    {
                        return null;
                    }

                    String permIds = permIdList.substring(2);

                    InQueryScroller<Long> entityIdsToDeleteScroller = new InQueryScroller<>(entityIdsToDelete, 16384 /*
                                                                                                                      * createQueryPropertyHistorySQL
                                                                                                                      * uses the parameters twice
                                                                                                                      */);
                    List<Long> partialEntityIdsToDelete = null;
                    String content = "";
                    while ((partialEntityIdsToDelete = entityIdsToDeleteScroller.next()) != null)
                    {
                        if (content.length() > 0)
                        {
                            content += ", ";
                        }
                        content += historyCreator.apply(session, partialEntityIdsToDelete, createQueryPropertyHistorySQL(),
                                createQueryRelationshipHistorySQL(), createQueryAttributesSQL(), null,
                                AttachmentHolderKind.SAMPLE, registrator);
                    }

                    SQLQuery deleteProperties = session.createSQLQuery(properties);
                    deleteProperties.setParameter("id", deletion.getId());
                    deleteProperties.executeUpdate();

                    SQLQuery getAttachmentContentIds =
                            session.createSQLQuery(attachmentContentIdQuery);
                    getAttachmentContentIds.setParameter("id", deletion.getId());
                    List<Long> attachmentContentIdList = getAttachmentContentIds.list();

                    SQLQuery deleteAttachments = session.createSQLQuery(attachments);
                    deleteAttachments.setParameter("id", deletion.getId());
                    deleteAttachments.executeUpdate();

                    // if (attachmentContentIdList.size() > 0)
                    // {
                    // SQLQuery deleteAttachmentContents =
                    // session.createSQLQuery(attachmentContents);
                    // deleteAttachmentContents.setParameterList("ids", attachmentContentIdList);
                    // deleteAttachmentContents.executeUpdate();
                    // }
                    //
                    SQLQuery deleteSamples = session.createSQLQuery(samples);
                    deleteSamples.setParameter("id", deletion.getId());
                    deleteSamples.executeUpdate();

                    SQLQuery insertEvent = session.createSQLQuery(event);
                    insertEvent.setParameter("description", permIds);
                    insertEvent.setParameter("reason", deletion.getReason());
                    insertEvent.setParameter("registerer", registrator.getId());
                    insertEvent.setParameter("identifiers", permIds);
                    insertEvent.setParameter("content", content);
                    insertEvent.executeUpdate();

                    return null;
                }
            });
    }

    @Override
    public Set<TechId> listSampleIdsByChildrenIds(final Collection<TechId> children,
            final TechId relationship)
    {
        final String query =
                "select sample_id_parent from " + TableNames.SAMPLE_RELATIONSHIPS_VIEW
                        + " where sample_id_child in (:ids) and relationship_id = :r ";
        @SuppressWarnings("unchecked")
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        @Override
                        public final Object doInHibernate(final Session session)
                        {
                            InQuery<Long, Object> inQuery = new InQuery<>();
                            Map<String, Object> fixParams = new HashMap<String, Object>();
                            fixParams.put("r", relationship.getId());

                            final List<Long> longIds = TechId.asLongs(children);
                            List<Object> list = inQuery.withBatch(session, query, "ids", longIds, fixParams);
                            return list;
                        }
                    });
        Set<TechId> result = transformNumbers2TechIdSet(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("found %d sample parents for given children",
                    results.size()));
        }
        return result;
    }

    @Override
    public Map<Long, Set<Long>> mapSampleIdsByChildrenIds(final Collection<Long> children, final Long relationship)
    {
        final String query = "select sample_id_child, sample_id_parent from " + TableNames.SAMPLE_RELATIONSHIPS_VIEW
                + " where relationship_id = :relationship and sample_id_child in (:children)";

        @SuppressWarnings("unchecked")
        final List<Object[]> results = (List<Object[]>) getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public final Object doInHibernate(final Session session)
                {
                    InQuery<Long, Object> inQuery = new InQuery<>();
                    Map<String, Object> fixParams = new HashMap<String, Object>();
                    fixParams.put("relationship", relationship);

                    List<Object> list = inQuery.withBatch(session, query, "children", new ArrayList<>(children), fixParams);
                    return list;
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
    public Set<TechId> listSampleIdsByParentIds(Collection<TechId> parentIds)
    {
        return listChildrenIds(parentIds, TableNames.SAMPLE_RELATIONSHIPS_VIEW);
    }

    @Override
    public Set<TechId> listChildrenForTrashedSamples(Collection<TechId> parentIds)
    {
        return listChildrenIds(parentIds, TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE);
    }

    private Set<TechId> listChildrenIds(final Collection<TechId> parents, String tableName)
    {
        final String query = "SELECT sample_id_child FROM " + tableName + " WHERE sample_id_parent IN (:ids)";

        @SuppressWarnings("unchecked")
        final List<? extends Number> results =
                (List<? extends Number>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        @Override
                        public final Object doInHibernate(final Session session)
                        {
                            final List<Long> longIds = TechId.asLongs(parents);
                            InQuery<Long, Object> inQuery = new InQuery<>();
                            List<Object> list = inQuery.withBatch(session, query, "ids", longIds, null);
                            return list;
                        }
                    });
        Set<TechId> result = transformNumbers2TechIdSet(results);
        if (operationLog.isDebugEnabled())
        {
            operationLog.info(String.format("found %d sample children for given parents",
                    results.size()));
        }
        return result;
    }

    @Override
    public List<TechId> listSampleIdsByContainerIds(final Collection<TechId> containers)
    {
        final List<Long> longIds = TechId.asLongs(containers);
        return listSampleIdsByColumn("container.id", longIds, "sample components for given containers");
    }

    @Override
    public List<TechId> listSampleIdsBySampleTypeIds(Collection<TechId> sampleTypeIds)
    {
        final List<Long> longIds = TechId.asLongs(sampleTypeIds);
        return listSampleIdsByColumn("sampleType.id", longIds, "samples for given sample types");
    }

    @Override
    public List<TechId> listSampleIdsByExperimentIds(final Collection<TechId> experiments)
    {
        final List<Long> longIds = TechId.asLongs(experiments);
        return listSampleIdsByColumn("experimentInternal.id", longIds, "samples for given experiments");
    }

    private List<TechId> listSampleIdsByColumn(String columnName, final List<Long> longIds, String message)
    {
        final List<Long> results =
                DAOUtils.listByCollection(getHibernateTemplate(), new IDetachedCriteriaFactory()
                    {
                        @Override
                        public DetachedCriteria createCriteria()
                        {
                            final DetachedCriteria criteria =
                                    DetachedCriteria.forClass(SamplePE.class);
                            criteria.setProjection(Projections.id());
                            return criteria;
                        }
                    }, columnName, longIds);
        operationLog.info(String.format("found %s " + message, results.size()));
        return transformNumbers2TechIdList(results);
    }

    @Override
    public void setSampleContainer(final Long sampleId, final Long containerId)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery q = session.createSQLQuery("update samples set samp_id_part_of = :containerId where id = :sampleId");
                    q.setLong("containerId", containerId);
                    q.setLong("sampleId", sampleId);
                    q.executeUpdate();
                    return null;
                }
            });
    }

    @Override
    public void setSampleContained(final Long sampleId, final Collection<Long> containedIds)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery clearQuery =
                            session.createSQLQuery(
                                    "update samples set samp_id_part_of = null where id not in :containedIds and samp_id_part_of = :containerId");
                    clearQuery.setLong("containerId", sampleId);
                    clearQuery.setParameterList("containedIds", containedIds);
                    clearQuery.executeUpdate();

                    addSampleContained(sampleId, containedIds);
                    return null;
                }
            });
    }

    @Override
    public void addSampleContained(final Long sampleId, final Collection<Long> containedIds)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery setQuery =
                            session.createSQLQuery("update samples set samp_id_part_of = :containerId where id in :containedIds");
                    setQuery.setLong("containerId", sampleId);
                    setQuery.setParameterList("containedIds", containedIds);
                    setQuery.executeUpdate();
                    return null;
                }
            });
    }

    @Override
    public void removeSampleContained(final Long sampleId, final Collection<Long> containedIds)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery clearQuery =
                            session.createSQLQuery(
                                    "update samples set samp_id_part_of = null where id in :containedIds and samp_id_part_of = :containerId");
                    clearQuery.setLong("containerId", sampleId);
                    clearQuery.setParameterList("containedIds", containedIds);
                    clearQuery.executeUpdate();
                    return null;
                }
            });
    }

    @Override
    public void setSampleRelationshipChildren(final Long sampleId, final Collection<Long> childrenIds, final Long relationshipId,
            final PersonPE author)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery q =
                            session.createSQLQuery(
                                    "delete from sample_relationships where sample_id_child not in :childrenIds and sample_id_parent = :parentId and relationship_id = :relationshipId");
                    q.setParameterList("childrenIds", childrenIds);
                    q.setLong("parentId", sampleId);
                    q.setLong("relationshipId", relationshipId);

                    q.executeUpdate();

                    addSampleRelationshipChildren(sampleId, childrenIds, relationshipId, author);
                    return null;
                }
            });
    }

    @Override
    public void addSampleRelationshipChildren(final Long sampleId, final Collection<Long> childrenIds, final Long relationshipId,
            final PersonPE author)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    for (Long relatedSampleId : childrenIds)
                    {
                        SQLQuery q =
                                session.createSQLQuery(
                                        "insert into sample_relationships (id, sample_id_parent, sample_id_child, relationship_id, pers_id_author, registration_timestamp, modification_timestamp) "
                                                + "select nextval('sample_relationship_id_seq'),  :parentId, :childId, :relationshipId, :authorId, now(), now() where not exists "
                                                + "(select 1 from sample_relationships where sample_id_parent = :parentId and sample_id_child = :childId and relationship_id = :relationshipId)");
                        q.setLong("parentId", sampleId);
                        q.setLong("childId", relatedSampleId);
                        q.setLong("relationshipId", relationshipId);
                        q.setLong("authorId", author.getId());

                        q.executeUpdate();
                    }
                    return null;
                }
            });
    }

    @Override
    public void removeSampleRelationshipChildren(final Long sampleId, final Collection<Long> childrenIds, final Long relationshipId,
            final PersonPE author)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery q =
                            session.createSQLQuery(
                                    "delete from sample_relationships where sample_id_parent = :parentId and sample_id_child in :childrenIds and relationship_id = :relationshipId");
                    q.setLong("parentId", sampleId);
                    q.setParameterList("childrenIds", childrenIds);
                    q.setLong("relationshipId", relationshipId);

                    q.executeUpdate();
                    return null;
                }
            });
    }

    @Override
    public void setSampleRelationshipParents(final Long sampleId, final Collection<Long> parentsIds, final Long relationshipId, final PersonPE author)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery q =
                            session.createSQLQuery(
                                    "delete from sample_relationships where sample_id_parent not in :parentIds and sample_id_child = :childId and relationship_id = :relationshipId");
                    q.setParameterList("parentIds", parentsIds);
                    q.setLong("childId", sampleId);
                    q.setLong("relationshipId", relationshipId);

                    q.executeUpdate();

                    addSampleRelationshipParents(sampleId, parentsIds, relationshipId, author);
                    return null;
                }
            });
    }

    @Override
    public void addSampleRelationshipParents(final Long sampleId, final Collection<Long> parentsIds, final Long relationshipId, final PersonPE author)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    for (Long parentId : parentsIds)
                    {
                        SQLQuery q =
                                session.createSQLQuery(
                                        "insert into sample_relationships (id, sample_id_parent, sample_id_child, relationship_id, pers_id_author, registration_timestamp, modification_timestamp) "
                                                + "select nextval('sample_relationship_id_seq'),  :parentId, :childId, :relationshipId, :authorId, now(), now() where not exists "
                                                + "(select 1 from sample_relationships where sample_id_parent = :parentId and sample_id_child = :childId and relationship_id = :relationshipId)");
                        q.setLong("parentId", parentId);
                        q.setLong("childId", sampleId);
                        q.setLong("relationshipId", relationshipId);
                        q.setLong("authorId", author.getId());

                        q.executeUpdate();
                    }
                    return null;
                }
            });
    }

    @Override
    public void removeSampleRelationshipParents(final Long sampleId, final Collection<Long> parentsIds, final Long relationshipId,
            final PersonPE author)
    {
        getHibernateTemplate().execute(new HibernateCallback()
            {
                @Override
                public Object doInHibernate(Session session) throws HibernateException
                {
                    SQLQuery q =
                            session.createSQLQuery(
                                    "delete from sample_relationships where sample_id_parent in :parentIds and sample_id_child = :childId and relationship_id = :relationshipId");
                    q.setParameterList("parentIds", parentsIds);
                    q.setLong("childId", sampleId);
                    q.setLong("relationshipId", relationshipId);

                    q.executeUpdate();
                    return null;
                }
            });
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

}
