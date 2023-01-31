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

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Resolves experiment folders with path "/<space-code>" to {@link FtpFile}-s.
 * 
 * @author Kaloyan Enimanev
 */
public class SpaceFolderResolver implements IFtpPathResolver
{

    /**
     * @return <code>true</code> for all paths containing single folder, <code>false</code> for all other paths.
     */
    @Override
    public boolean canResolve(String path)
    {
        return false == removeStartingSlash(path).contains(FtpConstants.FILE_SEPARATOR);
    }

    @Override
    public FtpFile resolve(final String path, final FtpPathResolverContext resolverContext)
    {
        final String spaceCode = removeStartingSlash(path);
        String sessionToken = resolverContext.getSessionToken();
        IServiceForDataStoreServer service = resolverContext.getService();
        Space space = service.tryGetSpace(sessionToken, new SpaceIdentifier(spaceCode));
        if (space == null)
        {
            throw new UserFailureException("Unknown space '" + spaceCode + "'.");
        }
        AbstractFtpFolder file = new AbstractFtpFolder(path)
            {
                @Override
                public List<FtpFile> unsafeListFiles()
                {
                    List<Project> projects = listProjects(resolverContext);

                    List<FtpFile> result = new ArrayList<FtpFile>();
                    List<String> childProjects = new ArrayList<String>();

                    for (Project project : projects)
                    {
                        String projectSpaceCode = project.getSpace().getCode();
                        if (projectSpaceCode.equals(spaceCode))
                        {
                            childProjects.add(project.getCode());
                        }
                    }

                    for (String childProject : childProjects)
                    {
                        String childPath = path + FtpConstants.FILE_SEPARATOR + childProject;
                        FtpFile childFile =
                                resolverContext.getResolverRegistry().resolve(childPath,
                                        resolverContext);
                        result.add(childFile);
                    }

                    return result;
                }
            };
        file.setLastModified(space.getModificationDate().getTime());
        return file;
    }

    private List<Project> listProjects(FtpPathResolverContext context)
    {
        IServiceForDataStoreServer service = context.getService();
        String sessionToken = context.getSessionToken();
        List<Project> projects = service.listProjects(sessionToken);
        return projects;
    }

    private String removeStartingSlash(String path)
    {
        if (path.startsWith(FtpConstants.FILE_SEPARATOR))
        {
            return path.substring(1);
        } else
        {
            return path;
        }
    }
}
