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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.ExperimentFolderResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.ProjectFolderResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.RootFolderResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.SpaceFolderResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.TemplateBasedDataSetResourceResolver;

/**
 * A registry class keeping references to all known {@link IFtpPathResolver} instances.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpPathResolverRegistry implements IFtpPathResolverRegistry
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpPathResolverRegistry.class);

    private List<IFtpPathResolver> pathResolvers = new ArrayList<IFtpPathResolver>();

    /**
     * initializes the registry with all known {@link IFtpPathResolver}-s.
     */
    public FtpPathResolverRegistry(FtpServerConfig ftpServerConfig)
    {
        pathResolvers.add(new RootFolderResolver());
        pathResolvers.add(new SpaceFolderResolver());
        pathResolvers.add(new ProjectFolderResolver());
        TemplateBasedDataSetResourceResolver dataSetResolver =
                new TemplateBasedDataSetResourceResolver(ftpServerConfig);
        pathResolvers.add(new ExperimentFolderResolver(dataSetResolver));
        pathResolvers.add(dataSetResolver);
    }

    /**
     * tries for find an {@link IFtpPathResolver} instance that can resolve a given path.
     */
    IFtpPathResolver tryFindResolver(String path)
    {
        for (IFtpPathResolver ftpFileCreator : pathResolvers)
        {
            if (ftpFileCreator.canResolve(path))
            {
                return ftpFileCreator;
            }
        }
        return null;
    }

    public FtpFile tryResolve(String path, FtpPathResolverContext resolverContext)
    {
        IFtpPathResolver resolver = tryFindResolver(path);
        if (resolver != null)
        {
            return resolver.resolve(path, resolverContext);
        } else
        {
            String message =
                    String.format("Cannot find resolver for path '%s'. Wrong user input ?", path);
            operationLog.warn(message);
            return null;
        }
    }

}
