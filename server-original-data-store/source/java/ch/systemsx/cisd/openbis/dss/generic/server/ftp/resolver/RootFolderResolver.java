/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Creates a root folder {@link FtpFile} listing all existing spaces as its children.
 * 
 * @author Kaloyan Enimanev
 */
public class RootFolderResolver implements IFtpPathResolver
{

    @Override
    public boolean canResolve(String path)
    {
        return FtpConstants.ROOT_DIRECTORY.equals(path);
    }

    @Override
    public FtpFile resolve(String path, final FtpPathResolverContext resolverContext)
    {
        return new AbstractFtpFolder(path)
            {
                @Override
                public List<FtpFile> unsafeListFiles()
                {

                    List<Project> projects = listProjects(resolverContext);
                    List<FtpFile> result = new ArrayList<FtpFile>();
                    Set<String> spacesSeen = new TreeSet<String>();

                    for (Project project : projects)
                    {
                        String spaceCode = project.getSpace().getCode();
                        spacesSeen.add(spaceCode);
                    }

                    for (String spaceCode : spacesSeen)
                    {
                        String childPath = FtpConstants.ROOT_DIRECTORY + spaceCode;
                        FtpFile child =
                                resolverContext.getResolverRegistry().resolve(childPath,
                                        resolverContext);
                        if (child != null)
                        {
                            result.add(child);
                        }
                    }

                    return result;
                }

                private List<Project> listProjects(FtpPathResolverContext context)
                {
                    IServiceForDataStoreServer service = context.getService();
                    String sessionToken = context.getSessionToken();
                    List<Project> projects = service.listProjects(sessionToken);
                    return projects;
                }
            };
    }

}
