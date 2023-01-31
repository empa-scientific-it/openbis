/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;

public class MockFreeSpaceProvider implements IFreeSpaceProvider
{
    final List<String> shares = new ArrayList<String>();

    private Integer[] freeSpaceValues;

    private int index;

    public void setFreeSpaceValues(Integer... freeSpaceValues)
    {
        this.freeSpaceValues = freeSpaceValues;
    }

    public List<String> getShares()
    {
        return shares;
    }

    @Override
    public long freeSpaceKb(HostAwareFile path) throws IOException
    {
        shares.add(path.getLocalFile().getName());
        return freeSpaceValues[index++ % freeSpaceValues.length];
    }
}