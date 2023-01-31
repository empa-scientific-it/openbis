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

import org.apache.ftpserver.ftplet.FtpFile;

/**
 * @author Kaloyan Enimanev
 */
public interface IFtpPathResolverRegistry
{

    /**
     * Method called once to initialize object
     */
    void initialize(FtpPathResolverConfig config);

    /**
     * Returns an {@link FtpFile} for <var>path</var>.
     * <p>
     * This method will return a {@link FtpPathResolverRegistry#getNonExistingFile(String, String)} when the file does not exist.
     */
    FtpFile resolve(String path, FtpPathResolverContext resolverContext);

}
