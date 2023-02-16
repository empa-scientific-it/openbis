/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;

import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share.ShufflePriority;

/**
 * A common parent for {@link IShareFinder} test cases.
 * 
 * @author Kaloyan Enimanev
 */
public class AbstractIShareFinderTestCase extends AssertJUnit
{
    protected Share incomingShare(String shareId, final long freeSpace, int speed)
    {
        return incomingShare(shareId, freeSpace, speed, ShufflePriority.SPEED);
    }

    protected Share incomingShare(String shareId, final long freeSpace, int speed,
            ShufflePriority shufflePriority)
    {
        Share result = extensionShare(shareId, freeSpace, speed);
        result.setIncoming(true);
        result.setShufflePriority(shufflePriority);
        return result;
    }

    protected Share extensionShare(String shareId, final long freeSpace)
    {
        return extensionShare(shareId, freeSpace, 0);
    }

    protected Share extensionShare(String shareId, final long freeSpace, int speed)
    {
        final File file = new File(shareId);
        Share share = new Share(file, speed, new IFreeSpaceProvider()
            {
                @Override
                public long freeSpaceKb(HostAwareFile path) throws IOException
                {
                    assertEquals(file, path.getLocalFile());
                    return freeSpace / FileUtils.ONE_KB;
                }
            });
        return share;
    }

    protected long megaBytes(int megaBytes)
    {
        return megaBytes * org.apache.commons.io.FileUtils.ONE_MB;
    }

    protected long kiloBytes(int kiloBytes)
    {
        return kiloBytes * org.apache.commons.io.FileUtils.ONE_KB;
    }
}
