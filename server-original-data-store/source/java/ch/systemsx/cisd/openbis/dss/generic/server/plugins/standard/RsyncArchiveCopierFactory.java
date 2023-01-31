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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;

/**
 * {@link IPathCopierFactory} that is more reliable than {@link RsyncCopierFactory} when it comes to deciding which files to transfer.
 * {@link IPathCopier} created by {@link RsyncCopierFactory} uses "--append" flag causing files that are bigger in destination than in source to be
 * ignored.
 * <p>
 * {@link IPathCopier} created by this factory will compare a checksum on files to make a decision if the files have been changed and are in need of a
 * transfer. The performance of such a check is much slower then the default one but with archiving we are concerned mostly about reliability.
 * 
 * @author Piotr Buczek
 */
public final class RsyncArchiveCopierFactory implements Serializable, IPathCopierFactory
{
    private static final long serialVersionUID = 1L;

    @Override
    public IPathCopier create(File rsyncExecutable, File sshExecutableOrNull, long timeoutInMillis, List<String> additionalCmdLineFlagsOrNull)
    {
        List<String> additionalCmdLineFlags = new ArrayList<>(
                Arrays.asList("--archive", "--delete", "--inplace", "--checksum", getTimeoutParameter(timeoutInMillis)));
        if (additionalCmdLineFlagsOrNull != null)
        {
            additionalCmdLineFlags.addAll(additionalCmdLineFlagsOrNull);
        }
        return new RsyncCopier(rsyncExecutable, sshExecutableOrNull, additionalCmdLineFlags.toArray(new String[0]));
    }

    private String getTimeoutParameter(long timeoutInMillis)
    {
        long timeoutInSeconds = timeoutInMillis / DateUtils.MILLIS_PER_SECOND;
        return "--timeout=" + timeoutInSeconds;
    }
}