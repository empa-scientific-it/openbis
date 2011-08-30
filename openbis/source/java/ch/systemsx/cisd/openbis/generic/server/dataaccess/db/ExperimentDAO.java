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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Data access object for {@link ExperimentPE}.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentDAO extends AbstractGenericEntityWithPropertiesDAO<ExperimentPE> implements
        IExperimentDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExperimentDAO.class);

    protected ExperimentDAO(final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources, databaseInstance, ExperimentPE.class);
    }

    public List<ExperimentPE> listExperimentsWithProperties(final ProjectPE project)
            throws DataAccessException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project wasn't set");
        }
        return listExperimentsWithProperties(null, project, null);
    }

    public List<ExperimentPE> listExperimentsWithProperties(final SpacePE space)
            throws DataAccessException
    {
        if (space == null)
        {
            throw new IllegalArgumentException("Space wasn't set");
        }
        return listExperimentsWithProperties(null, null, space);
    }

    public List<ExperimentPE> listExperimentsWithProperties(
            final ExperimentTypePE experimentTypeOrNull, final ProjectPE projectOrNull,
            final SpacePE spaceOrNull) throws DataAccessException
    {
        final DetachedCriteria criteria = createCriteriaForUndeleted();
        if (experimentTypeOrNull != null)
        {
            criteria.add(Restrictions.eq("experimentType", experimentTypeOrNull));
        }
        if (projectOrNull != null)
        {
            criteria.add(Restrictions.eq("projectInternal", projectOrNull));
        }
        if (spaceOrNull != null)
        {
            // alias is the easiest way to restrict on association using criteria
            criteria.createAlias("projectInternal", "project");
            criteria.add(Restrictions.eq("project.space", spaceOrNull));
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiments have been found for project '%s'%s.",
                    list.size(), projectOrNull, (experimentTypeOrNull == null) ? ""
                            : " and experiment type '" + experimentTypeOrNull + "'"));
        }
        return list;
    }

    public List<ExperimentPE> listExperimentsWithProperties(Collection<Long> experimentIDs)
            throws DataAccessException
    {
        if (experimentIDs == null || experimentIDs.isEmpty())
        {
            return new ArrayList<ExperimentPE>();
        }
        DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.in("id", experimentIDs));
        criteria.setFetchMode("experimentProperties", FetchMode.JOIN);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiments have been found for specified IDs.",
                    list.size()));
        }
        return list;
    }

    public List<ExperimentPE> listExperiments() throws DataAccessException
    {
        final DetachedCriteria criteria = createCriteriaForUndeleted();
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d experiment(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    private DetachedCriteria createCriteriaForUndeleted()
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.isNull("deletion"));
        return criteria;
    }

    public ExperimentPE tryFindByCodeAndProject(final ProjectPE project, final String experimentCode)
    {
        assert experimentCode != null : "Unspecified experiment code.";
        assert project != null : "Unspecified project.";

        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(experimentCode)));
        criteria.add(Restrictions.eq("projectInternal", project));
        criteria.setFetchMode("experimentType.experimentTypePropertyTypesInternal", FetchMode.JOIN);
        final ExperimentPE experiment = (ExperimentPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following experiment '%s' has been found for code '%s' and project '%s'.",
                    experiment, experimentCode, project));
        }
        return experiment;
    }

    public List<ExperimentPE> listExperimentsByProjectAndProperty(String propertyCode,
            String propertyValue, ProjectPE project) throws DataAccessException
    {
        assert project != null : "Unspecified space.";
        assert propertyCode != null : "Unspecified property code";
        assert propertyValue != null : "Unspecified property value";

        String queryFormat =
                "from " + ExperimentPropertyPE.class.getSimpleName()
                        + " where %s = ? and entity.projectInternal = ? "
                        + " and entityTypePropertyType.propertyTypeInternal.simpleCode = ?"
                        + " and entityTypePropertyType.propertyTypeInternal.internalNamespace = ?";

        List<ExperimentPE> entities =
                listByPropertyValue(queryFormat, propertyCode, propertyValue, project);
        if (operationLog.isDebugEnabled())
        {
            operationLog
                    .debug(String
                            .format("%d experiments have been found for project '%s' and property '%s' equal to '%s'.",
                                    entities.size(), project, propertyCode, propertyValue));
        }
        return entities;
    }

    private List<ExperimentPE> listByPropertyValue(String queryFormat, String propertyCode,
            String propertyValue, ProjectPE project)
    {
        String simplePropertyCode = CodeConverter.tryToDatabase(propertyCode);
        boolean isInternalNamespace = CodeConverter.isInternalNamespace(propertyCode);
        Object[] arguments =
                toArray(propertyValue, project, simplePropertyCode, isInternalNamespace);

        String queryPropertySimpleValue = String.format(queryFormat, "value");
        List<ExperimentPropertyPE> properties1 =
                cast(getHibernateTemplate().find(queryPropertySimpleValue, arguments));

        String queryPropertyVocabularyTerm = String.format(queryFormat, "vocabularyTerm.code");
        List<ExperimentPropertyPE> properties2 =
                cast(getHibernateTemplate().find(queryPropertyVocabularyTerm, arguments));

        properties1.addAll(properties2);
        List<ExperimentPE> entities = extractEntities(properties1);
        return entities;
    }

    private static List<ExperimentPE> extractEntities(List<ExperimentPropertyPE> properties)
    {
        List<ExperimentPE> samples = new ArrayList<ExperimentPE>();
        for (ExperimentPropertyPE prop : properties)
        {
            samples.add(prop.getEntity());
        }
        return samples;
    }

    public ExperimentPE tryGetByPermID(String permId)
    {
        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("permId", permId));
        final ExperimentPE experimentOrNull = (ExperimentPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "Following experiment '%s' has been found for permId '%s'.", experimentOrNull,
                    permId));
        }
        return experimentOrNull;
    }

    public List<ExperimentPE> listByPermID(Set<String> permIds)
    {
        return listByIDsOfName("permId", permIds);
    }

    public List<ExperimentPE> listByIDs(Collection<Long> ids)
    {
        return listByIDsOfName("id", ids);
    }

    private List<ExperimentPE> listByIDsOfName(String idName, Collection<?> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return new ArrayList<ExperimentPE>();
        }
        final DetachedCriteria criteria = DetachedCriteria.forClass(ExperimentPE.class);
        criteria.add(Restrictions.in(idName, ids));
        final List<ExperimentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d experiment(s) have been found.", list.size()));
        }
        return list;
    }

    public void createOrUpdateExperiment(ExperimentPE experiment)
    {
        HibernateTemplate template = getHibernateTemplate();
        internalCreateOrUpdateExperiment(experiment, template);
        template.flush();

        scheduleDynamicPropertiesEvaluation(Collections.singletonList(experiment));
    }

    public void createOrUpdateExperiments(List<ExperimentPE> experiments)
    {
        assert experiments != null && experiments.size() > 0 : "Unspecified or empty experiments.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        for (final ExperimentPE experiment : experiments)
        {
            internalCreateOrUpdateExperiment(experiment, hibernateTemplate);
        }
        hibernateTemplate.flush();

        // if session is not cleared registration of many experiments slows down after each batch
        hibernateTemplate.clear();
        scheduleDynamicPropertiesEvaluation(experiments);
    }

    private void internalCreateOrUpdateExperiment(ExperimentPE experiment,
            HibernateTemplate hibernateTemplate)
    {
        assert experiment != null : "Missing experiment.";
        experiment.setCode(CodeConverter.tryToDatabase(experiment.getCode()));
        validatePE(experiment);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(experiment);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("ADD: experiment '%s'.", experiment));
        }
    }

    public void delete(final List<TechId> experimentIds, final PersonPE registrator,
            final String reason) throws DataAccessException
    {
        // NOTE: we use EXPERIMENT_ALL_TABLE, not DELETED_EXPERIMENTS_VIEW because we still want to
        // be able to directly delete samples without going to trash (trash may be disabled)
        final String experimentsTable = TableNames.EXPERIMENTS_ALL_TABLE;

        final String sqlSelectPermIds = SQLBuilder.createSelectPermIdsSQL(experimentsTable);
        final String sqlDeleteProperties =
                SQLBuilder.createDeletePropertiesSQL(TableNames.EXPERIMENT_PROPERTIES_TABLE,
                        ColumnNames.EXPERIMENT_COLUMN);
        final String sqlSelectAttachmentContentIds =
                SQLBuilder.createSelectAttachmentContentIdsSQL(ColumnNames.EXPERIMENT_COLUMN);
        final String sqlDeleteAttachmentContents = SQLBuilder.createDeleteAttachmentContentsSQL();
        final String sqlDeleteAttachments =
                SQLBuilder.createDeleteAttachmentsSQL(ColumnNames.EXPERIMENT_COLUMN);
        final String sqlDeleteExperiments = SQLBuilder.createDeleteEnitiesSQL(experimentsTable);
        final String sqlInsertEvent = SQLBuilder.createInsertEventSQL();

        executePermanentDeleteAction(EntityType.EXPERIMENT, experimentIds, registrator, reason,
                sqlSelectPermIds, sqlDeleteProperties, sqlSelectAttachmentContentIds,
                sqlDeleteAttachmentContents, sqlDeleteAttachments, sqlDeleteExperiments,
                sqlInsertEvent);
    }

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

}
