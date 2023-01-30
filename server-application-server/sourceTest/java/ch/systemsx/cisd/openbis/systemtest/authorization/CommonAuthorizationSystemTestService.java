/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.DataStoreServiceRegistrator;
import ch.systemsx.cisd.openbis.generic.server.IDataStoreServiceRegistrator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
@Component
public class CommonAuthorizationSystemTestService
{

    private final static String USER = "test";

    private final static String PASSWORD = "password";

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ICommonServer commonServer;

    private List<IIdHolder> createdObjects;
    
    private IDataStoreServiceRegistrator dataStoreServiceRegistrator;
    
    private IDataStoreServiceRegistrator getDataStoreServiceRegistrator()
    {
        if (dataStoreServiceRegistrator == null)
        {
            dataStoreServiceRegistrator = new DataStoreServiceRegistrator(daoFactory);
        }
        return dataStoreServiceRegistrator;
    }

    @Transactional
    public void createPerson(PersonPE person)
    {
        daoFactory.getPersonDAO().createPerson(person);
        createdObjects.add(person);
    }

    @Transactional
    public void createSpace(SpacePE space)
    {
        daoFactory.getSpaceDAO().createSpace(space);
        createdObjects.add(space);
    }

    @Transactional
    public void createProject(ProjectPE project)
    {
        daoFactory.getProjectDAO().createProject(project, null);
        createdObjects.add(project);
    }

    @Transactional
    public void createExperiment(ExperimentPE experiment)
    {
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, null);
        createdObjects.add(experiment);
    }

    @Transactional
    public void createSample(SamplePE sample)
    {
        daoFactory.getSampleDAO().createOrUpdateSample(sample, null);
        createdObjects.add(sample);
    }

    @Transactional
    public void createDataSet(DataPE dataSet)
    {
        dataSet.setRegistrationDate(new Date());
        dataSet.setModificationDate(new Date());
        daoFactory.getDataDAO().createDataSet(dataSet, null);
        createdObjects.add(dataSet);
    }

    @Transactional
    public void createType(EntityTypePE entityType, EntityKind entityKind)
    {
        daoFactory.getEntityTypeDAO(entityKind).createOrUpdateEntityType(entityType);
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(entityType.getCode());
            getDataStoreServiceRegistrator().register(dataSetType);
        }
        createdObjects.add(entityType);
    }

    @Transactional
    public PersonPE tryFindPerson(String userId)
    {
        return daoFactory.getPersonDAO().tryFindPersonByUserId(userId);
    }

    @Transactional
    public SpacePE tryFindSpace(String spaceCode)
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
    }

    @Transactional
    public ProjectPE tryFindProject(String spaceCode, String projectCode)
    {
        ProjectPE project = daoFactory.getProjectDAO().tryFindProject(spaceCode, projectCode);
        HibernateUtils.initialize(project.getExperiments());
        return project;
    }

    @Transactional
    public ExperimentPE tryFindExperiment(ProjectPE project, String experimentCode)
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(project, experimentCode);
    }

    @Transactional
    public SamplePE tryFindSharedSample(String sampleCode)
    {
        return daoFactory.getSampleDAO().tryFindByCodeAndDatabaseInstance(sampleCode);
    }

    @Transactional
    public SamplePE tryFindSpaceSample(SpacePE spacePE, String sampleCode)
    {
        SamplePE sample = daoFactory.getSampleDAO().tryFindByCodeAndSpace(sampleCode, spacePE);
        HibernateUtils.initialize(sample.getProject());
        HibernateUtils.initialize(sample.getExperiment());
        return sample;
    }

    @Transactional
    public SamplePE tryFindProjectSample(ProjectPE projectPE, String sampleCode)
    {
        SamplePE sample = daoFactory.getSampleDAO().tryfindByCodeAndProject(sampleCode, projectPE);
        HibernateUtils.initialize(sample.getProject());
        HibernateUtils.initialize(sample.getExperiment());
        return sample;
    }

    @Transactional
    public DataPE tryFindDataSet(String dataSetCode)
    {
        DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(dataSetCode);
        HibernateUtils.initialize(dataSet.getExperiment());
        HibernateUtils.initialize(dataSet.tryGetSample());
        if (dataSet.tryGetSample() != null)
        {
            HibernateUtils.initialize(dataSet.tryGetSample().getProject());
            HibernateUtils.initialize(dataSet.tryGetSample().getExperiment());
        }
        return dataSet;
    }

    @Transactional
    public DataStorePE tryFindDataStore()
    {
        return daoFactory.getDataStoreDAO().listDataStores().iterator().next();
    }

    @Transactional
    public VocabularyTermPE tryFindStorageFormat(String code)
    {
        VocabularyPE storageFormatVocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode("$STORAGE_FORMAT");

        for (VocabularyTermPE storageFormatTerm : storageFormatVocabulary.getTerms())
        {
            if (storageFormatTerm.getCode().equals(code))
            {
                return storageFormatTerm;
            }
        }
        return null;
    }

    @Transactional
    public FileFormatTypePE tryFileFormatType(String code)
    {
        return daoFactory.getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(code);
    }

    @Transactional
    public LocatorTypePE tryFindLocatorType(String code)
    {
        return daoFactory.getLocatorTypeDAO().tryToFindLocatorTypeByCode(code);
    }

    public void recordCreatedObjects()
    {
        createdObjects = new ArrayList<IIdHolder>();
    }

    @Transactional
    public void removeCreatedObjects()
    {
        Session session = daoFactory.getSessionFactory().getCurrentSession();

        for (int i = createdObjects.size() - 1; i >= 0; i--)
        {
            IIdHolder createdObject = createdObjects.get(i);
            createdObject = (IIdHolder) session.get(createdObject.getClass(), createdObject.getId());
            session.delete(createdObject);
        }
    }

    public Deletion trashExperiment(ExperimentPE experiment)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(USER, PASSWORD);
        commonServer.deleteExperiments(session.getSessionToken(), Arrays.asList(new TechId(experiment.getId())), "testing", DeletionType.TRASH);
        return getLatestDeletion(session);
    }

    public Deletion trashSample(SamplePE sample)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(USER, PASSWORD);
        commonServer.deleteSamples(session.getSessionToken(), Arrays.asList(new TechId(sample.getId())), "testing", DeletionType.TRASH);
        return getLatestDeletion(session);
    }

    public Deletion trashDataSet(DataPE dataSet)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(USER, PASSWORD);
        commonServer.deleteDataSets(session.getSessionToken(), Arrays.asList(dataSet.getCode()), "testing", DeletionType.TRASH, true);
        return getLatestDeletion(session);
    }

    public void untrash(Long deletionId)
    {
        SessionContextDTO session = commonServer.tryAuthenticate(USER, PASSWORD);
        commonServer.revertDeletions(session.getSessionToken(), Arrays.asList(new TechId(deletionId)));
    }

    private Deletion getLatestDeletion(SessionContextDTO session)
    {
        List<Deletion> deletions = commonServer.listDeletions(session.getSessionToken(), false);
        return deletions.get(deletions.size() - 1);
    }

}
