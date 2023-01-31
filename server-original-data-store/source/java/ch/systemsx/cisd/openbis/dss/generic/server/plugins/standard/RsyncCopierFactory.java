/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;

public final class RsyncCopierFactory implements Serializable, IPathCopierFactory
{
    private static final long serialVersionUID = 1L;

    @Override
    public IPathCopier create(File rsyncExecutable, File sshExecutableOrNull, long timeoutInMillis, 
            List<String> additionalCmdLineFlagsOrNull)
    {
        String[] additionalCmdLineFlags = new String[0];
        if (additionalCmdLineFlagsOrNull != null)
        {
            additionalCmdLineFlags = additionalCmdLineFlagsOrNull.toArray(new String[0]);
        }
        return new RsyncCopier(rsyncExecutable, sshExecutableOrNull, false, false, 
                additionalCmdLineFlags);
    }
}