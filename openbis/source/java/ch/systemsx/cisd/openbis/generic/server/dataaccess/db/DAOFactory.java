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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import javax.annotation.Resource;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.*;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.common.spring.SpringEoDSQLExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.UpdateUtils;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * {@link IDAOFactory} implementation working with {@link DatabaseConfigurationContext} and {@link SessionFactory}.
 * 
 * @author Franz-Josef Elmer
 */
public final class DAOFactory extends AuthorizationDAOFactory implements IDAOFactory, InitializingBean
{
    private static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DAOFactory.class);

    static
    {
        SpringEoDSQLExceptionTranslator.activate();
    }

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler;

    private final ISampleTypeDAO sampleTypeDAO;

    private final IHibernateSearchDAO hibernateSearchDAO;

    private final IPropertyTypeDAO propertyTypeDAO;

    private final Map<EntityKind, IEntityTypeDAO> entityTypeDAOs =
            new HashMap<EntityKind, IEntityTypeDAO>();

    private final Map<EntityKind, IEntityPropertyTypeDAO> entityPropertyTypeDAOs =
            new HashMap<EntityKind, IEntityPropertyTypeDAO>();

    private final IVocabularyDAO vocabularyDAO;

    private final IVocabularyTermDAO vocabularyTermDAO;

    private final IAttachmentDAO attachmentDAO;

    private final DataSetTypeDAO dataSetTypeDAO;

    private final FileFormatTypeDAO fileFormatTypeDAO;

    private final LocatorTypeDAO locatorTypeDAO;

    private final IMaterialDAO materialDAO;

    private final ICodeSequenceDAO codeSequenceDAO;

    private final IDataStoreDAO dataStoreDAO;

    private final IPermIdDAO permIdDAO;

    private final IEventDAO eventDAO;

    private final IEventsSearchDAO eventsSearchDAO;

    private final IAuthorizationGroupDAO authorizationGroupDAO;

    private final IScriptDAO scriptDAO;

    private final ICorePluginDAO corePluginDAO;

    private final IPostRegistrationDAO postRegistrationDAO;

    private final IEntityOperationsLogDAO entityOperationsLogDAO;

    private EntityHistoryDAO entityPropertyHistoryDAO;

    private final IExternalDataManagementSystemDAO externalDataManagementSystemDAO;

    private final IOperationExecutionDAO operationExecutionDAO;

    private final ISemanticAnnotationDAO semanticAnnotationDAO;

    private DatabaseConfigurationContext context;

    public static final boolean USE_NEW_SQL_ENGINE = true;

    public DAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory,
            final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler,
            final EntityHistoryCreator historyCreator, final IAuthorizationConfig authorizationConfig)
    {
        super(context, sessionFactory, dynamicPropertyEvaluationScheduler, historyCreator, authorizationConfig);
        this.context = context;
        this.dynamicPropertyEvaluationScheduler = dynamicPropertyEvaluationScheduler;
        historyCreator.setDaoFactory(this);
        sampleTypeDAO = new SampleTypeDAO(sessionFactory, historyCreator);
        hibernateSearchDAO = new HibernateSearchDAOV3Adaptor();
        propertyTypeDAO = new PropertyTypeDAO(sessionFactory, historyCreator);
        vocabularyDAO = new VocabularyDAO(sessionFactory, historyCreator);
        vocabularyTermDAO = new VocabularyTermDAO(sessionFactory, historyCreator);
        dataSetTypeDAO = new DataSetTypeDAO(sessionFactory, historyCreator);
        fileFormatTypeDAO = new FileFormatTypeDAO(sessionFactory, historyCreator);
        locatorTypeDAO = new LocatorTypeDAO(sessionFactory, historyCreator);
        materialDAO = new MaterialDAO(getPersistencyResources(), historyCreator);
        codeSequenceDAO = new CodeSequenceDAO(sessionFactory);
        dataStoreDAO = new DataStoreDAO(sessionFactory);
        permIdDAO = new PermIdDAO(sessionFactory);
        eventDAO = new EventDAO(sessionFactory, historyCreator);
        eventsSearchDAO = new EventsSearchDAO(sessionFactory);
        attachmentDAO = new AttachmentDAO(getPersistencyResources(), eventDAO, historyCreator);
        authorizationGroupDAO = new AuthorizationGroupDAO(sessionFactory, historyCreator);
        scriptDAO = new ScriptDAO(sessionFactory, historyCreator);
        corePluginDAO = new CorePluginDAO(sessionFactory);
        postRegistrationDAO = new PostRegistrationDAO(sessionFactory, historyCreator);
        entityOperationsLogDAO = new EntityOperationsLogDAO(sessionFactory, historyCreator);
        final EntityKind[] entityKinds = EntityKind.values();
        for (final EntityKind entityKind : entityKinds)
        {
            final EntityTypeDAO dao =
                    new EntityTypeDAO(entityKind, sessionFactory, historyCreator, this);
            entityTypeDAOs.put(entityKind, dao);
            entityPropertyTypeDAOs.put(entityKind, new EntityPropertyTypeDAO(entityKind,
                    getPersistencyResources()));
        }
        entityPropertyHistoryDAO =
                new EntityHistoryDAO(getPersistencyResources());
        externalDataManagementSystemDAO =
                new ExternalDataManagementSystemDAO(sessionFactory);
        operationExecutionDAO =
                new OperationExecutionDAO(sessionFactory, historyCreator);
        semanticAnnotationDAO = new SemanticAnnotationDAO(sessionFactory, historyCreator);
    }

    //
    // IDAOFactory
    //

    @Override
    public Date getTransactionTimestamp()
    {
        return UpdateUtils.getTransactionTimeStamp(getSessionFactory());
    }

    @Override
    public final ISampleTypeDAO getSampleTypeDAO()
    {
        return sampleTypeDAO;
    }

    @Override
    public final IHibernateSearchDAO getHibernateSearchDAO()
    {
        return hibernateSearchDAO;
    }

    @Override
    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(final EntityKind entityKind)
    {
        return entityPropertyTypeDAOs.get(entityKind);
    }

    @Override
    public IEntityHistoryDAO getEntityPropertyHistoryDAO()
    {
        return entityPropertyHistoryDAO;
    }

    @Override
    public IEntityTypeDAO getEntityTypeDAO(final EntityKind entityKind)
    {
        return entityTypeDAOs.get(entityKind);
    }

    @Override
    public IPropertyTypeDAO getPropertyTypeDAO()
    {
        return propertyTypeDAO;
    }

    @Override
    public final IVocabularyDAO getVocabularyDAO()
    {
        return vocabularyDAO;
    }

    @Override
    public final IVocabularyTermDAO getVocabularyTermDAO()
    {
        return vocabularyTermDAO;
    }

    @Override
    public final IAttachmentDAO getAttachmentDAO()
    {
        return attachmentDAO;
    }

    @Override
    public IDataSetTypeDAO getDataSetTypeDAO()
    {
        return dataSetTypeDAO;
    }

    @Override
    public IFileFormatTypeDAO getFileFormatTypeDAO()
    {
        return fileFormatTypeDAO;
    }

    @Override
    public ILocatorTypeDAO getLocatorTypeDAO()
    {
        return locatorTypeDAO;
    }

    @Override
    public IMaterialDAO getMaterialDAO()
    {
        return materialDAO;
    }

    @Override
    public ICodeSequenceDAO getCodeSequenceDAO()
    {
        return codeSequenceDAO;
    }

    @Override
    public IDataStoreDAO getDataStoreDAO()
    {
        return dataStoreDAO;
    }

    @Override
    public IPermIdDAO getPermIdDAO()
    {
        return permIdDAO;
    }

    @Override
    public IEventDAO getEventDAO()
    {
        return eventDAO;
    }

    @Override
    public IEventsSearchDAO getEventsSearchDAO()
    {
        return eventsSearchDAO;
    }

    @Override
    public IAuthorizationGroupDAO getAuthorizationGroupDAO()
    {
        return authorizationGroupDAO;
    }

    @Override
    public IScriptDAO getScriptDAO()
    {
        return scriptDAO;
    }

    public IDynamicPropertyEvaluationScheduler getDynamicPropertyEvaluationScheduler()
    {
        return dynamicPropertyEvaluationScheduler;
    }

    @Override
    public ICorePluginDAO getCorePluginDAO()
    {
        return corePluginDAO;
    }

    @Override
    public IPostRegistrationDAO getPostRegistrationDAO()
    {
        return postRegistrationDAO;
    }

    @Override
    public IEntityOperationsLogDAO getEntityOperationsLogDAO()
    {
        return entityOperationsLogDAO;
    }

    @Override
    public IExternalDataManagementSystemDAO getExternalDataManagementSystemDAO()
    {
        return externalDataManagementSystemDAO;
    }

    @Override
    public IOperationExecutionDAO getOperationExecutionDAO()
    {
        return operationExecutionDAO;
    }

    @Override
    public ISemanticAnnotationDAO getSemanticAnnotationDAO()
    {
        return semanticAnnotationDAO;
    }

    private static String projectConstraintFunction =
            "CREATE FUNCTION check_project_is_defined_for_experiment_level_samples() " +
                    "  RETURNS trigger AS " +
                    "$BODY$ " +
                    "BEGIN " +
                    "  IF (NEW.proj_id IS NULL AND NEW.expe_id IS NOT NULL) THEN " +
                    "    RAISE EXCEPTION 'Project has to be defined for experiment level samples'; " +
                    "  END IF; " +
                    "  RETURN NEW; " +
                    "END; " +
                    "$BODY$ " +
                    "  LANGUAGE 'plpgsql';";

    private static String projectConstraintTrigger =
            "CREATE TRIGGER check_project_is_defined_for_experiment_level_samples " +
                    "BEFORE INSERT OR UPDATE " +
                    "ON samples_all " +
                    "FOR EACH ROW " +
                    "EXECUTE PROCEDURE check_project_is_defined_for_experiment_level_samples();";

    private static String setProjectsToSamplesWithExperiments =
            "UPDATE samples_all AS s  " +
                    "SET proj_id = (SELECT proj_id FROM experiments_all WHERE id = s.expe_id) " +
                    "WHERE s.proj_id IS NULL AND s.expe_id IS NOT NULL;";

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Properties serviceProperties = configurer.getResolvedProps();
        hibernateSearchDAO.setProperties(serviceProperties);
        SamplePE.projectSamplesEnabled = PropertyUtils.getBoolean(serviceProperties, Constants.PROJECT_SAMPLES_ENABLED_KEY, false);
        Connection connection = null;
        try
        {
            connection = context.getDataSource().getConnection();
            Statement statement = connection.createStatement();
            connection.setAutoCommit(false);

            ResultSet result = statement.executeQuery("SELECT tgname FROM pg_trigger WHERE tgname='disable_project_level_samples'");
            boolean triggerExists = result.next();

            if (SamplePE.projectSamplesEnabled)
            {
                if (triggerExists)
                {
                    operationLog.info("Enable project samples by dropping the trigger 'disable_project_level_samples'.");

                    // CORNER CASE FIX - FROZEN PROJECTS - BEFORE UPDATE
                    ResultSet frozenProjects = statement.executeQuery("SELECT id FROM projects WHERE frozen_for_samp='t'");
                    List<Long> frozenProjectsIds = new ArrayList<>();
                    while (frozenProjects.next()) {
                        frozenProjectsIds.add(frozenProjects.getLong("id"));
                    }
                    statement.executeUpdate("UPDATE projects SET frozen_for_samp='f' WHERE frozen_for_samp='t'");
                    //

                    statement.executeUpdate("DROP TRIGGER disable_project_level_samples ON samples_all");
                    statement.executeUpdate(projectConstraintFunction);
                    statement.executeUpdate(projectConstraintTrigger);
                    statement.executeUpdate(setProjectsToSamplesWithExperiments);

                    // CORNER CASE FIX - FROZEN PROJECTS - AFTER UPDATE
                    if(!frozenProjectsIds.isEmpty()) {
                        StringBuilder frozenProjectsIdsAsString = new StringBuilder();
                        for (int idx = 0; idx < frozenProjectsIds.size(); idx++) {
                            if( idx > 0) {
                                frozenProjectsIdsAsString.append(",");
                            }
                            frozenProjectsIdsAsString.append(frozenProjectsIds.get(idx));
                        }

                        statement.executeUpdate("UPDATE projects SET frozen_for_samp='t' WHERE id in (" + frozenProjectsIdsAsString + ")");
                    }
                    //
                } else
                {
                    operationLog.info("Project samples already enabled.");
                }
            } else
            {
                if (triggerExists == false)
                {
                    operationLog.warn("It is not possible to disable project samples feature. The system still considers "
                            + Constants.PROJECT_SAMPLES_ENABLED_KEY + "=true.");
                    SamplePE.projectSamplesEnabled = true;
                }
            }
            statement.close();
            connection.commit();
        } catch (Throwable t)
        {
            operationLog.info("Failed to enable project level samples.", t);
            if (connection != null)
            {
                connection.rollback();
            }
        } finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                } catch (Exception e)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(e);
                }
            }
        }
    }
}
