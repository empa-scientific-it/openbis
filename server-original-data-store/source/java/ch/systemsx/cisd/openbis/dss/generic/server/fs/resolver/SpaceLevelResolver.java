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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.IResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IDirectoryResponse;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file.IFileSystemViewResponse;

class SpaceLevelResolver extends AbstractResolver
{
    String spaceCode;

    public SpaceLevelResolver(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    @Override
    public IFileSystemViewResponse resolve(String[] subPath, IResolverContext context)
    {
        if (subPath.length == 0)
        {
            SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
            searchCriteria.withCode().thatEquals(spaceCode);
            SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
            fetchOptions.withProjects();
            fetchOptions.withSamples().withProject();

            SpacePermId spaceCodeId = new SpacePermId(spaceCode);

            Map<ISpaceId, Space> spaces =
                    context.getApi().getSpaces(context.getSessionToken(), Collections.singletonList(spaceCodeId), fetchOptions);

            Space space = spaces.get(spaceCodeId);

            if (space == null)
            {
                return context.createNonExistingFileResponse(null);
            }

            IDirectoryResponse response = context.createDirectoryResponse();
            Set<String> projectPermIds = new HashSet<>();
            for (Project project : space.getProjects())
            {
                response.addDirectory(project.getCode(), project.getModificationDate());
                String permId = project.getPermId().getPermId();
                projectPermIds.add(permId);
            }
            for (Sample sample : space.getSamples())
            {
                Project project = sample.getProject();
                if (project == null || projectPermIds.contains(project.getPermId().getPermId()) == false)
                {
                    response.addDirectory(getFullCodeWithPrefix(sample), sample.getModificationDate());
                }
            }
            return response;
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            String pathWithPrefix = "/" + spaceCode + "/" + item;
            if (hasSamplePrefix(pathWithPrefix))
            {
                SampleIdentifier sampleIdentifier = new SampleIdentifier(removeSamplePrefix(pathWithPrefix));
                return new SampleLevelResolver(sampleIdentifier).resolve(remaining, context);
            }
            return new ProjectLevelResolver(spaceCode, removeSamplePrefix(item)).resolve(remaining, context);
        }
    }

}
