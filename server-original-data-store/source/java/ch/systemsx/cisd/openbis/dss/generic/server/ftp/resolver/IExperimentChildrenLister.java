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

import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

/**
 * An interface decoupling the resolver implementations for experiments and data sets.
 * 
 * @author Kaloyan Enimanev
 */
public interface IExperimentChildrenLister
{
    /**
     * Lists the children {@link FtpFile} objects in an experiment.
     * 
     * @param parentPath the FTP path representing the experiment.
     */
    List<FtpFile> listExperimentChildrenPaths(Experiment experiment, String parentPath,
            FtpPathResolverContext context);

}
