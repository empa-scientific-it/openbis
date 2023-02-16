/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
            }
            for (Sample sample : project.getSamples())
            {
                Experiment experiment = sample.getExperiment();
                if (experiment == null || experimentPermIds.contains(experiment.getPermId().getPermId()) == false)
                {
                    response.addDirectory(getFullCodeWithPrefix(sample), sample.getModificationDate());
                }
            }
            return response;
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            String pathWithPrefix = projectIdentifier.getIdentifier() + "/" + item;
            String path = removeSamplePrefix(pathWithPrefix);
            if (hasSamplePrefix(pathWithPrefix))
            {
                SampleIdentifier sampleIdentifier = new SampleIdentifier(path);
                return new SampleLevelResolver(sampleIdentifier).resolve(remaining, context);
            }
            return new ExperimentLevelResolver(new ExperimentIdentifier(path)).resolve(remaining, context);
        }
    }
}