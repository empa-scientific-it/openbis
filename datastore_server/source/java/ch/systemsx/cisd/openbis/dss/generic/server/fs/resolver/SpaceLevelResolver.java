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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
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
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Cache;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.Node;

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
        Cache cache = getCache(context);
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
                cache.putNode(new Node(PROJECT_TYPE, permId), project.getIdentifier().getIdentifier());
            }
            for (Sample sample : space.getSamples())
            {
                Project project = sample.getProject();
                if (project == null || projectPermIds.contains(project.getPermId().getPermId()) == false)
                {
                    response.addDirectory(sample.getCode(), sample.getModificationDate());
                    cache.putNode(new Node(SAMPLE_TYPE, sample.getPermId().getPermId()), sample.getIdentifier().getIdentifier());
                }
            }
            return response;
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            String path = "/" + spaceCode + "/" + item;
            String type = getType(path, context);
            if (type.equals(PROJECT_TYPE))
            {
                return new ProjectLevelResolver(spaceCode, item).resolve(remaining, context);
            }
            return new SampleLevelResolver(new SampleIdentifier(path)).resolve(remaining, context);
        }
    }
    
    private String getType(String path, IResolverContext context)
    {
        Node node = getCache(context).getNode(path);
        if (node != null)
        {
            return node.getType();
        }
        
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(path);
        if (context.getApi().getProjects(context.getSessionToken(), Collections.singletonList(projectIdentifier),
                new ProjectFetchOptions()).containsKey(projectIdentifier))
        {
            return PROJECT_TYPE;
        }
        return SAMPLE_TYPE;
    }
}
