/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * This interface implements a strategy for storing data in the <i>store</i> directory.
 * 
 * @author Christian Ribeaud
 */
public interface IDataStoreStrategy
{

    /**
     * Returns the key associated with this <code>IDataStoreStrategy</code>.
     * <p>
     * This key uniquely identifies this <code>IDataStoreStrategy</code>.
     * </p>
     */
    public DataStoreStrategyKey getKey();

    /**
     * Returns the base directory where the data are going to be moved into.
     */
    public File getBaseDirectory(final File baseDirectory, final DataSetInformation dataSetInfo,
            final DataSetType dataSetType);

    /**
     * Create the target path for given <var>baseDirectory</var> and given <var>incomingDataSetPath</var>.
     * <p>
     * Note that each call either produces a new <i>target path</i> or throws an exception if computed <i>target path</i> already exists.
     * </p>
     * 
     * @return The target path.
     */
    public File getTargetPath(final File baseDirectory, final File incomingDataSetPath);
}
