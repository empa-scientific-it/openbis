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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

public final class MockDataSetDirectoryProvider implements IDataSetDirectoryProvider
{
    private final File store;

    private final String shareID;

    private final IShareIdManager shareIdManager;

    public MockDataSetDirectoryProvider(File store, String shareID)
    {
        this(store, shareID, null);
    }

    public MockDataSetDirectoryProvider(File store, String shareID, IShareIdManager shareIdManager)
    {
        this.store = store;
        this.shareID = shareID;
        this.shareIdManager = shareIdManager;
    }

    @Override
    public File getStoreRoot()
    {
        return store;
    }

    @Override
    public File getDataSetDirectory(IDatasetLocation dataSet)
    {
        return new File(new File(getStoreRoot(), shareID), dataSet.getDataSetLocation());
    }

    @Override
    public File getDataSetDirectory(String shareId, String location)
    {
        return new File(new File(getStoreRoot(), shareID), location);
    }

    @Override
    public IShareIdManager getShareIdManager()
    {
        return shareIdManager;
    }
}