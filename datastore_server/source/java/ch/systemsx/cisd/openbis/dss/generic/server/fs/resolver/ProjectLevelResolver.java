/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Node;

class ProjectLevelResolver extends AbstractResolver
{
    private ProjectIdentifier projectIdentifier;

    public ProjectLevelResolver(String spaceCode, String projectCode)
    {
        this.projectIdentifier = new ProjectIdentifier(spaceCode, projectCode);
    }

    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        Cache cache = getCache(context);
        if (subPath.length == 0)
        {
            ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
            fetchOptions.withExperiments();
            fetchOptions.withSamples().withExperiment();

            Map<IProjectId, Project> projects =
                    context.getApi().getProjects(context.getSessionToken(), Collections.singletonList(projectIdentifier), fetchOptions);
            Project project = projects.get(projectIdentifier);

            IDirectoryResponse response = context.createDirectoryResponse();
            if (project == null)
            {
                return context.createNonExistingFileResponse(null);
            }
            Set<String> experimentPermIds = new HashSet<>();
            for (Experiment exp : project.getExperiments())
            {
                String permId = exp.getPermId().getPermId();
                experimentPermIds.add(permId);
                response.addDirectory(exp.getCode(), exp.getModificationDate());
                cache.putNode(new Node(EXPERIMENT_TYPE, permId), 
                        projectIdentifier.getIdentifier() + "/" + exp.getCode());
            }
            for (Sample sample : project.getSamples())
            {
                Experiment experiment = sample.getExperiment();
                if (experiment == null || experimentPermIds.contains(experiment.getPermId().getPermId()) == false)
                {
                    response.addDirectory(sample.getCode(), sample.getModificationDate());
                    cache.putNode(new Node(SAMPLE_TYPE, sample.getPermId().getPermId()), 
                            projectIdentifier.getIdentifier() + "/" + sample.getCode());
                }
            }
            return response;
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            String path = projectIdentifier.getIdentifier() + "/" + item;
            String type = getType(path, context);
            if (type.equals(EXPERIMENT_TYPE))
            {
                return new ExperimentLevelResolver(new ExperimentIdentifier(path)).resolve(remaining, context);
            } if (type.equals(SAMPLE_TYPE))
            {
                return new SampleLevelResolver(new SampleIdentifier(path)).resolve(remaining, context);
            }
            throw new IllegalArgumentException("Unknown node type: " + type);
        }
    }

    private String getType(String path, IResolverContext context)
    {
        Node node = getCache(context).getNode(path);
        if (node != null)
        {
            return node.getType();
        }
        
        ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier(path);
        if (context.getApi().getExperiments(context.getSessionToken(), Collections.singletonList(experimentIdentifier),
                new ExperimentFetchOptions()).containsKey(experimentIdentifier))
        {
            return EXPERIMENT_TYPE;
        }
        return SAMPLE_TYPE;
    }
}