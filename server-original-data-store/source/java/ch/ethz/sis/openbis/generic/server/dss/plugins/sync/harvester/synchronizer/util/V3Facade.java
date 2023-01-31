/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;

/**
 * @author Ganime Betul Akin
 */
public class V3Facade
{
    private final IDataStoreServerApi dss;

    private final IApplicationServerApi as;

    private final String sessionToken;

    public V3Facade(SyncConfig config)
    {
        as = ServiceUtils.createAsV3Api(config.getDataSourceOpenbisURL());
        dss = ServiceUtils.createDssV3Api(config.getDataSourceDSSURL());
        sessionToken = as.login(config.getUser(), config.getPassword());
    }

    public SearchResult<DataSetFile> searchFiles(DataSetFileSearchCriteria criteria, DataSetFileFetchOptions dsFileFetchOptions)
    {
        return dss.searchFiles(sessionToken, criteria, dsFileFetchOptions);
    }

    public SearchResult<DataSetFile> searchWithDataSetCode(String dataSetCode, DataSetFileFetchOptions dsFileFetchOptions)
    {
        DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
        criteria.withDataSet().withCode().thatEquals(dataSetCode);
        return searchFiles(criteria, dsFileFetchOptions);
    }

    public List<Attachment> getExperimentAttachments(IExperimentId experimentId)
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersionUsing(fetchOptions.withAttachments());
        fetchOptions.withAttachments().withPreviousVersion().withContentUsing(fetchOptions.withAttachments().withContent());
        Map<IExperimentId, Experiment> experiments = as.getExperiments(sessionToken, Arrays.asList(experimentId), fetchOptions);
        if (experiments.size() == 1)
        {
            Experiment experiment = experiments.get(experimentId);
            return experiment.getAttachments();
        }
        return null;
    }

    public List<Attachment> getSampleAttachments(ISampleId sampleId)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersionUsing(fetchOptions.withAttachments());
        fetchOptions.withAttachments().withPreviousVersion().withContentUsing(fetchOptions.withAttachments().withContent());
        Map<ISampleId, Sample> samples = as.getSamples(sessionToken, Arrays.asList(sampleId), fetchOptions);
        if (samples.size() == 1)
        {
            Sample sample = samples.get(sampleId);
            return sample.getAttachments();
        }
        return null;
    }

    public List<Attachment> getProjectAttachments(IProjectId projectId)
    {
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withAttachments().withContent();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersionUsing(fetchOptions.withAttachments());
        fetchOptions.withAttachments().withPreviousVersion().withContentUsing(fetchOptions.withAttachments().withContent());
        Map<IProjectId, Project> projects = as.getProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        if (projects.size() == 1)
        {
            Project project = projects.get(projectId);
            return project.getAttachments();
        }
        return null;
    }

    public InputStream downloadFiles(List<IDataSetFileId> fileIds, DataSetFileDownloadOptions options)
    {
        return dss.downloadFiles(sessionToken, fileIds, options);
    }

    public FastDownloadSession createFastDownloadSession(List<? extends IDataSetFileId> fileIds, 
            FastDownloadSessionOptions options)
    {
        return dss.createFastDownloadSession(sessionToken, fileIds, options);
    }

}
