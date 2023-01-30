/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class AbstractDeletionTest extends AbstractTest
{

    protected ProjectPermId createCisdProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation creation = new ProjectCreation();
        creation.setCode("PROJECT_TO_DELETE");
        creation.setSpaceId(new SpacePermId("CISD"));

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Collections.singletonList(creation));

        return permIds.get(0);
    }

    protected ExperimentPermId createCisdExperiment()
    {
        return createCisdExperiment(new ProjectIdentifier("/CISD/DEFAULT"));
    }

    protected ExperimentPermId createCisdExperiment(IProjectId projectId)
    {
        return createCisdExperiment(projectId, "EXPERIMENT_TO_DELETE");
    }

    protected ExperimentPermId createCisdExperiment(IProjectId projectId, String code)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode(code);
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(projectId);
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Collections.singletonList(creation));
        Map<IExperimentId, Experiment> map = v3api.getExperiments(sessionToken, permIds, new ExperimentFetchOptions());
        List<Experiment> experiments = new ArrayList<Experiment>(map.values());

        Assert.assertEquals(1, experiments.size());
        Assert.assertEquals(code, experiments.get(0).getCode());

        return permIds.get(0);
    }

    protected SamplePermId createCisdSample(IExperimentId experimentId)
    {
        return createCisdSample(experimentId, "SAMPLE_TO_DELETE");
    }

    protected SamplePermId createCisdSample(IExperimentId experimentId, String sampleCode)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode(sampleCode);
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(experimentId);

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, permIds, new SampleFetchOptions());
        List<Sample> samples = new ArrayList<Sample>(map.values());

        Assert.assertEquals(1, samples.size());
        Assert.assertEquals(sampleCode, samples.get(0).getCode());

        return permIds.get(0);
    }

    protected void assertProjectExists(IProjectId projectId)
    {
        assertProjectExists(projectId, true);
    }

    protected void assertProjectDoesNotExist(IProjectId projectId)
    {
        assertProjectExists(projectId, false);
    }

    private void assertProjectExists(IProjectId projectId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map =
                v3api.getProjects(sessionToken, Collections.singletonList(projectId), new ProjectFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertExperimentExists(IExperimentId experimentId)
    {
        assertExperimentExists(experimentId, true);
    }

    protected void assertExperimentDoesNotExist(IExperimentId experimentId)
    {
        assertExperimentExists(experimentId, false);
    }

    private void assertExperimentExists(IExperimentId experimentId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IExperimentId, Experiment> map =
                v3api.getExperiments(sessionToken, Collections.singletonList(experimentId), new ExperimentFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertSampleExists(ISampleId sampleId)
    {
        assertSampleExists(sampleId, true);
    }

    protected void assertSampleDoesNotExist(ISampleId sampleId)
    {
        assertSampleExists(sampleId, false);
    }

    private void assertSampleExists(ISampleId sampleId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<ISampleId, Sample> map = v3api.getSamples(sessionToken, Collections.singletonList(sampleId), new SampleFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertMaterialExists(IMaterialId materialId)
    {
        assertMaterialExists(materialId, true);
    }

    protected void assertMaterialDoesNotExist(IMaterialId materialId)
    {
        assertMaterialExists(materialId, false);
    }

    private void assertMaterialExists(IMaterialId materialId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IMaterialId, Material> map = v3api.getMaterials(sessionToken, Collections.singletonList(materialId), new MaterialFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertDataSetExists(DataSetPermId dataSetId)
    {
        assertDataSetExists(dataSetId, true);
    }

    protected void assertDataSetDoesNotExist(DataSetPermId dataSetId)
    {
        assertDataSetExists(dataSetId, false);
    }

    private void assertDataSetExists(DataSetPermId dataSetId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IDataSetId, DataSet> map = v3api.getDataSets(sessionToken, Collections.singletonList(dataSetId), new DataSetFetchOptions());
        Assert.assertEquals(exists ? 1 : 0, map.size());
    }

    protected void assertDeletionExists(IDeletionId deletionId)
    {
        assertDeletionExists(deletionId, true);
    }

    protected void assertDeletionDoesNotExist(IDeletionId deletionId)
    {
        assertDeletionExists(deletionId, false);
    }

    private void assertDeletionExists(IDeletionId deletionId, boolean exists)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<Deletion> result = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), new DeletionFetchOptions());
        Deletion found = null;

        for (Deletion item : result.getObjects())
        {
            if (item.getId().equals(deletionId))
            {
                found = item;
                break;
            }
        }

        if (exists)
        {
            Assert.assertNotNull(found);
        } else
        {
            Assert.assertNull(found);
        }
    }

}
