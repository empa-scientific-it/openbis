/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.freezing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.StorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;

/**
 * @author Franz-Josef Elmer
 */
public abstract class FreezingTest extends AbstractTest
{
    static final SpacePermId DEFAULT_SPACE_ID = new SpacePermId("CISD");

    static final ProjectIdentifier DEFAULT_PROJECT_ID = new ProjectIdentifier("CISD", "NEMO");

    @Autowired
    protected SessionFactory sessionFactory;

    protected void setFrozenFlagForSpaces(boolean frozen, SpacePermId... spacePermIds)
    {
        setFrozenFlagForSpaces(frozen, Arrays.asList(spacePermIds));
    }

    protected void setFrozenFlagForSpaces(boolean frozen, List<SpacePermId> spacePermIds)
    {
        Session session = sessionFactory.getCurrentSession();
        NativeQuery<?> query = session.createNativeQuery("update spaces set frozen = :frozen where code in :permIds")
                .setParameter("frozen", frozen)
                .setParameter("permIds", spacePermIds.stream().map(SpacePermId::getPermId).collect(Collectors.toList()));
        query.executeUpdate();
        session.flush();
    }

    protected SpaceCreation space(String code)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(code);
        return spaceCreation;
    }

    protected void setFrozenFlagForProjects(boolean frozen, ProjectPermId... projectPermIds)
    {
        setFrozenFlagForProjects(frozen, Arrays.asList(projectPermIds));
    }

    protected void setFrozenFlagForProjects(boolean frozen, List<ProjectPermId> projectPermIds)
    {
        Session session = sessionFactory.getCurrentSession();
        NativeQuery<?> query = session.createNativeQuery("update projects set frozen = :frozen where perm_id in :permIds")
                .setParameter("frozen", frozen)
                .setParameter("permIds", projectPermIds.stream().map(ProjectPermId::getPermId).collect(Collectors.toList()));
        query.executeUpdate();
        session.flush();
    }

    protected ProjectCreation project(ISpaceId spaceId, String code)
    {
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode(code);
        projectCreation.setSpaceId(spaceId);
        return projectCreation;
    }
    
    protected Project getProject(IProjectId projectId)
    {
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withAttachments();
        fetchOptions.withLeader();
        fetchOptions.withSpace();
        return v3api.getProjects(systemSessionToken, Arrays.asList(projectId), fetchOptions).get(projectId);
    }

    protected void setFrozenFlagForExperiments(boolean frozen, ExperimentPermId... experimentPermIds)
    {
        setFrozenFlagForExperiments(frozen, Arrays.asList(experimentPermIds));
    }

    protected void setFrozenFlagForExperiments(boolean frozen, List<ExperimentPermId> experimentPermIds)
    {
        Session session = sessionFactory.getCurrentSession();
        NativeQuery<?> query = session.createNativeQuery("update experiments_all set frozen = :frozen where perm_id in :permIds")
                .setParameter("frozen", frozen)
                .setParameter("permIds", experimentPermIds.stream().map(ExperimentPermId::getPermId).collect(Collectors.toList()));
        query.executeUpdate();
        session.flush();
    }

    protected Experiment getExperiment(IExperimentId experimentId)
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withTags();
        fetchOptions.withProject();
        return v3api.getExperiments(systemSessionToken, Arrays.asList(experimentId), fetchOptions).get(experimentId);
    }

    protected ExperimentCreation experiment(IProjectId projectId, String code)
    {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode(code);
        experimentCreation.setTypeId(new EntityTypePermId("DELETION_TEST", EntityKind.EXPERIMENT));
        experimentCreation.setProjectId(projectId);
        return experimentCreation;
    }

    protected void setFrozenFlagForSamples(boolean frozen, SamplePermId... samplePermIds)
    {
        setFrozenFlagForSamples(frozen, Arrays.asList(samplePermIds));
    }

    protected void setFrozenFlagForSamples(boolean frozen, List<SamplePermId> samplePermIds)
    {
        Session session = sessionFactory.getCurrentSession();
        NativeQuery<?> query = session.createNativeQuery("update samples_all set frozen = :frozen where perm_id in :permIds")
                .setParameter("frozen", frozen)
                .setParameter("permIds", samplePermIds.stream().map(SamplePermId::getPermId).collect(Collectors.toList()));
        query.executeUpdate();
        session.flush();
    }

    protected Sample getSample(ISampleId sampleId)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withTags();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        fetchOptions.withExperiment();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        fetchOptions.withComponents();
        fetchOptions.withContainer();
        return v3api.getSamples(systemSessionToken, Arrays.asList(sampleId), fetchOptions).get(sampleId);
    }

    protected SampleCreation cellPlate(String code)
    {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode(code);
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE", EntityKind.SAMPLE));
        sampleCreation.setSpaceId(DEFAULT_SPACE_ID);
        return sampleCreation;
    }

    protected AttachmentCreation attachment(String fileName, String title, String description, String content)
    {
        AttachmentCreation attachmentCreation = new AttachmentCreation();
        attachmentCreation.setTitle(title);
        attachmentCreation.setFileName(fileName);
        attachmentCreation.setDescription(description);
        attachmentCreation.setContent(content.getBytes());
        return attachmentCreation;
    }

    protected void setFrozenFlagForDataSets(boolean frozen, DataSetPermId... dataSetPermIds)
    {
        setFrozenFlagForDataSets(frozen, Arrays.asList(dataSetPermIds));
    }

    protected void setFrozenFlagForDataSets(boolean frozen, List<DataSetPermId> dataSetPermIds)
    {
        Session session = sessionFactory.getCurrentSession();
        NativeQuery<?> query = session.createNativeQuery("update data_all set frozen = :frozen where code in :permIds")
                .setParameter("frozen", frozen)
                .setParameter("permIds", dataSetPermIds.stream().map(DataSetPermId::getPermId).collect(Collectors.toList()));
        query.executeUpdate();
        session.flush();
    }

    protected DataSet getDataSet(IDataSetId dataSetId)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withTags();
        fetchOptions.withChildren();
        fetchOptions.withComponents();
        fetchOptions.withExperiment();
        fetchOptions.withSample();
        return v3api.getDataSets(systemSessionToken, Arrays.asList(dataSetId), fetchOptions).get(dataSetId);
    }

    protected DataSetCreation physicalDataSet(String code)
    {
        DataSetCreation dataSet = dataSet(code);
        PhysicalDataCreation physicalData = new PhysicalDataCreation();
        physicalData.setLocation("a/b/" + code);
        physicalData.setLocatorTypeId(new LocatorTypePermId("RELATIVE_LOCATION"));
        physicalData.setFileFormatTypeId(new FileFormatTypePermId("PROPRIETARY"));
        physicalData.setStorageFormatId(new StorageFormatPermId("PROPRIETARY"));
        dataSet.setPhysicalData(physicalData);
        return dataSet;
    }

    protected DataSetCreation dataSet(String code)
    {
        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setCode(code);
        dataSetCreation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER", EntityKind.DATA_SET));
        dataSetCreation.setDataStoreId(new DataStorePermId("STANDARD"));
        return dataSetCreation;
    }
}
