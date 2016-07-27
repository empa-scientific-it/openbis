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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.v3;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;

class V3SpaceLevelResolver extends V3Resolver
{
    String spaceCode;

    public V3SpaceLevelResolver(String spaceCode, FtpPathResolverContext resolverContext)
    {
        super(resolverContext);
        this.spaceCode = spaceCode;
    }

    @Override
    public FtpFile resolve(String fullPath, String[] subPath)
    {
        if (subPath.length == 0)
        {
            ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
            searchCriteria.withSpace().withCode().thatEquals(spaceCode);
            ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
            List<Project> projects =
                    api.searchProjects(sessionToken, searchCriteria, fetchOptions).getObjects();
            List<FtpFile> files = new LinkedList<>();
            for (Project project : projects)
            {
                files.add(V3Resolver.createDirectoryScaffolding(fullPath, project.getCode()));
            }
            return V3Resolver.createDirectoryWithContent(fullPath, files);
        } else
        {
            String item = subPath[0];
            String[] remaining = Arrays.copyOfRange(subPath, 1, subPath.length);
            V3ProjectLevelResolver resolver = new V3ProjectLevelResolver(spaceCode, item, resolverContext);
            return resolver.resolve(fullPath, remaining);
        }
    }
}