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

import ch.systemsx.cisd.openbis.generic.shared.dto.ISampleRelationshipDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomColumnDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomFilterDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Super class of all DAO factories which extend {@link IAuthorizationDAOFactory}.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthorizationDAOFactory implements IAuthorizationDAOFactory
{

    private final IAuthorizationConfig authorizationConfig;

    private final IRoleAssignmentDAO roleAssignmentDAO;

    private final ISpaceDAO groupDAO;

    private final IPersonDAO personDAO;

    private final IDataDAO dataDAO;

    private final IExperimentDAO experimentDAO;

    private final IProjectDAO projectDAO;

    private final ISampleDAO sampleDAO;

    private final ISampleRelationshipDAO sampleRelationshipDAO;

    private final IGridCustomFilterDAO gridCustomFilterDAO;

    private final IGridCustomColumnDAO gridCustomColumnDAO;

    private final QueryDAO queryDAO;

    private final PersistencyResources persistencyResources;

    private final IRelationshipTypeDAO relationshipTypeDAO;

    private final IDeletionDAO deletionDAO;

    private final IMetaprojectDAO metaprojectDAO;

    public AuthorizationDAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory,
            final IDynamicPropertyEvaluationScheduler dynamicPropertyEvaluationScheduler,
            final EntityHistoryCreator historyCreator, final IAuthorizationConfig authorizationConfig)
    {
        persistencyResources =
                new PersistencyResources(context, sessionFactory,
                        dynamicPropertyEvaluationScheduler);
        personDAO = new PersonDAO(sessionFactory, historyCreator);
        groupDAO = new SpaceDAO(sessionFactory, historyCreator);
        roleAssignmentDAO = new RoleAssignmentDAO(sessionFactory, historyCreator);
        relationshipTypeDAO = new RelationshipTypeDAO(sessionFactory, historyCreator);
        dataDAO = new DataDAO(persistencyResources, relationshipTypeDAO, historyCreator);
        experimentDAO = new ExperimentDAO(persistencyResources, historyCreator);
        projectDAO = new ProjectDAO(sessionFactory, historyCreator);
        sampleDAO = new SampleDAO(persistencyResources, historyCreator);
        sampleRelationshipDAO = new SampleRelationshipDAO(sessionFactory, relationshipTypeDAO, historyCreator);
        gridCustomFilterDAO = new GridCustomFilterDAO(sessionFactory, historyCreator);
        gridCustomColumnDAO = new GridCustomColumnDAO(sessionFactory, historyCreator);
        queryDAO = new QueryDAO(sessionFactory, historyCreator);
        deletionDAO = new DeletionDAO(sessionFactory, persistencyResources, historyCreator);
        metaprojectDAO = new MetaprojectDAO(sessionFactory, historyCreator);
        this.authorizationConfig = authorizationConfig;
    }

    @Override
    public final PersistencyResources getPersistencyResources()
    {
        return persistencyResources;
    }

    @Override
    public SessionFactory getSessionFactory()
    {
        return persistencyResources.getSessionFactory();
    }

    @Override
    public final ISpaceDAO getSpaceDAO()
    {
        return groupDAO;
    }

    @Override
    public final IPersonDAO getPersonDAO()
    {
        return personDAO;
    }

    @Override
    public final IRoleAssignmentDAO getRoleAssignmentDAO()
    {
        return roleAssignmentDAO;
    }

    @Override
    public final IDataDAO getDataDAO()
    {
        return dataDAO;
    }

    @Override
    public final IExperimentDAO getExperimentDAO()
    {
        return experimentDAO;
    }

    @Override
    public final IProjectDAO getProjectDAO()
    {
        return projectDAO;
    }

    @Override
    public final ISampleDAO getSampleDAO()
    {
        return sampleDAO;
    }

    @Override
    public ISampleRelationshipDAO getSampleRelationshipDAO() {
        return sampleRelationshipDAO;
    }

    @Override
    public IGridCustomFilterDAO getGridCustomFilterDAO()
    {
        return gridCustomFilterDAO;
    }

    @Override
    public IGridCustomColumnDAO getGridCustomColumnDAO()
    {
        return gridCustomColumnDAO;
    }

    @Override
    public IQueryDAO getQueryDAO()
    {
        return queryDAO;
    }

    @Override
    public IRelationshipTypeDAO getRelationshipTypeDAO()
    {
        return relationshipTypeDAO;
    }

    @Override
    public IDeletionDAO getDeletionDAO()
    {
        return deletionDAO;
    }

    @Override
    public IMetaprojectDAO getMetaprojectDAO()
    {
        return metaprojectDAO;
    }

    @Override
    public IAuthorizationConfig getAuthorizationConfig()
    {
        return authorizationConfig;
    }

    /**
     * Configures current session settings for batch update mode.
     * 
     * @see HibernateUtils#setBatchUpdateMode(org.hibernate.Session, boolean)
     */
    @Override
    public void setBatchUpdateMode(boolean batchMode)
    {
        SessionFactory sessionFactory = persistencyResources.getSessionFactory();
        Session currentSession = sessionFactory.getCurrentSession();
        HibernateUtils.setBatchUpdateMode(currentSession, batchMode);
    }
}
