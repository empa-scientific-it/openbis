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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.lang.reflect.Method;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.server.business.IEntityOperationChecker;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ScriptBO.IScriptFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ICorePluginDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataManagementSystemDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomFilterDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;

/**
 * An <i>abstract</i> test for <i>Business Object</i>.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = ScriptBO.IScriptFactory.class)
public abstract class AbstractBOTest extends AssertJUnit
{
    protected Mockery context;

    protected IDAOFactory daoFactory;

    protected IScriptFactory scriptFactory;

    protected ISpaceDAO spaceDAO;

    protected IScriptDAO scriptDAO;

    protected IRelationshipTypeDAO relationshipTypeDAO;

    protected IExperimentDAO experimentDAO;

    protected IProjectDAO projectDAO;

    protected IEntityTypeDAO entityTypeDAO;

    protected IMaterialDAO materialDAO;

    protected IDataDAO dataDAO;

    protected ISampleDAO sampleDAO;

    protected IEntityPropertyTypeDAO entityPropertyTypeDAO;

    protected IPropertyTypeDAO propertyTypeDAO;

    protected IPersonDAO personDAO;

    protected ISampleTypeDAO sampleTypeDAO;

    protected IVocabularyDAO vocabularyDAO;

    protected IVocabularyTermDAO vocabularyTermDAO;

    protected IEntityPropertiesConverter propertiesConverter;

    protected IDataSetTypeDAO dataSetTypeDAO;

    protected IFileFormatTypeDAO fileFormatTypeDAO;

    protected ILocatorTypeDAO locatorTypeDAO;

    protected IDataStoreDAO dataStoreDAO;

    protected IPermIdDAO permIdDAO;

    protected IEventDAO eventDAO;

    protected IAuthorizationGroupDAO authorizationGroupDAO;

    protected IGridCustomFilterDAO filterDAO;

    protected IDeletionDAO deletionDAO;

    protected ICorePluginDAO corePluginDAO;

    protected IRelationshipService relationshipService;

    protected IServiceConversationClientManagerLocal conversationClient;

    protected IEntityOperationChecker entityOperationChecker;

    protected ISampleLister sampleLister;

    protected IDatasetLister datasetLister;

    protected IExternalDataManagementSystemDAO dataManagementSystemDAO;

    protected IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        scriptFactory = context.mock(IScriptFactory.class);
        spaceDAO = context.mock(ISpaceDAO.class);
        relationshipTypeDAO = context.mock(IRelationshipTypeDAO.class);
        experimentDAO = context.mock(IExperimentDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
        entityTypeDAO = context.mock(IEntityTypeDAO.class);
        sampleDAO = context.mock(ISampleDAO.class);
        dataDAO = context.mock(IDataDAO.class);
        personDAO = context.mock(IPersonDAO.class);
        propertiesConverter = context.mock(IEntityPropertiesConverter.class);
        sampleTypeDAO = context.mock(ISampleTypeDAO.class);
        propertyTypeDAO = context.mock(IPropertyTypeDAO.class);
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);
        vocabularyDAO = context.mock(IVocabularyDAO.class);
        vocabularyTermDAO = context.mock(IVocabularyTermDAO.class);
        materialDAO = context.mock(IMaterialDAO.class);
        dataSetTypeDAO = context.mock(IDataSetTypeDAO.class);
        fileFormatTypeDAO = context.mock(IFileFormatTypeDAO.class);
        locatorTypeDAO = context.mock(ILocatorTypeDAO.class);
        dataStoreDAO = context.mock(IDataStoreDAO.class);
        permIdDAO = context.mock(IPermIdDAO.class);
        eventDAO = context.mock(IEventDAO.class);
        authorizationGroupDAO = context.mock(IAuthorizationGroupDAO.class);
        filterDAO = context.mock(IGridCustomFilterDAO.class);
        scriptDAO = context.mock(IScriptDAO.class);
        deletionDAO = context.mock(IDeletionDAO.class);
        corePluginDAO = context.mock(ICorePluginDAO.class);
        relationshipService = context.mock(IRelationshipService.class);
        conversationClient = context.mock(IServiceConversationClientManagerLocal.class);
        entityOperationChecker = context.mock(IEntityOperationChecker.class);
        sampleLister = context.mock(ISampleLister.class);
        datasetLister = context.mock(IDatasetLister.class);
        dataManagementSystemDAO = context.mock(IExternalDataManagementSystemDAO.class);
        managedPropertyEvaluatorFactory = new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool());
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSessionFactory();
                    allowing(daoFactory).getSpaceDAO();
                    will(returnValue(spaceDAO));
                    allowing(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));
                    allowing(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));
                    allowing(daoFactory).getDataSetTypeDAO();
                    will(returnValue(dataSetTypeDAO));
                    allowing(daoFactory).getFileFormatTypeDAO();
                    will(returnValue(fileFormatTypeDAO));
                    allowing(daoFactory).getLocatorTypeDAO();
                    will(returnValue(locatorTypeDAO));
                    allowing(daoFactory).getDataDAO();
                    will(returnValue(dataDAO));
                    allowing(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));
                    allowing(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));
                    allowing(daoFactory).getVocabularyTermDAO();
                    will(returnValue(vocabularyTermDAO));
                    allowing(daoFactory).getEventDAO();
                    will(returnValue(eventDAO));
                    allowing(daoFactory).getAuthorizationGroupDAO();
                    will(returnValue(authorizationGroupDAO));
                    allowing(daoFactory).getGridCustomFilterDAO();
                    will(returnValue(filterDAO));
                    allowing(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));
                    allowing(daoFactory).getScriptDAO();
                    will(returnValue(scriptDAO));
                    allowing(daoFactory).getMaterialDAO();
                    will(returnValue(materialDAO));
                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));
                    allowing(daoFactory).getPropertyTypeDAO();
                    will(returnValue(propertyTypeDAO));
                    allowing(daoFactory).getDeletionDAO();
                    will(returnValue(deletionDAO));
                    allowing(daoFactory).getCorePluginDAO();
                    will(returnValue(corePluginDAO));
                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                    allowing(daoFactory).getExternalDataManagementSystemDAO();
                    will(returnValue(dataManagementSystemDAO));
                    allowing(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));
                    allowing(daoFactory).getRelationshipTypeDAO();
                    will(returnValue(relationshipTypeDAO));
                    allowing(daoFactory).getTransactionTimestamp();
                    will(returnValue(new Date()));
                }
            });
    }

    @AfterMethod
    public void afterMethod(Method m)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(m.getName() + "() : ", t);
        }
    }
}
