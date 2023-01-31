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

/**
 * Implementations of this interface specifies the location of the store root directory.
 * 
 * @author Christian Ribeaud
 */
public interface IStoreRootDirectoryHolder
{

    /**
     * Returns the store root directory.
     * <p>
     * Note that this method does not call {@link File#mkdirs()} on the returned path.
     * </p>
     */
    public File getStoreRootDirectory();

    /**
     * Sets the store root directory.
     */
    public void setStoreRootDirectory(final File storeRootDirectory);
}