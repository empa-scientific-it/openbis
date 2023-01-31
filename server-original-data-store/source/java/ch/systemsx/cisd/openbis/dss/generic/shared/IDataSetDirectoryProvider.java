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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Provider of the root directory of a data set in the data store.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSetDirectoryProvider
{
    /**
     * Returns the root directory of data store.
     */
    public File getStoreRoot();

    /**
     * Returns the root directory of specified data set.
     */
    public File getDataSetDirectory(IDatasetLocation dataSet);

    /**
     * Returns the root directory of a data set with specified shareId and location.
     */
    public File getDataSetDirectory(String shareId, String location);

    /**
     * Returns the share id manager which is used for {@link #getDataSetDirectory(IDatasetLocation)} .
     */
    public IShareIdManager getShareIdManager();
}
