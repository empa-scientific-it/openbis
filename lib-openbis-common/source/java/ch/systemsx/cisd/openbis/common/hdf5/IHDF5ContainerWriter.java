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

package ch.systemsx.cisd.openbis.common.hdf5;

import java.io.File;
import java.io.InputStream;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * A simple abstraction of the methods needed to write an HDF5 container.
 * 
 * @author Bernd Rinn
 */
public interface IHDF5ContainerWriter
{
    /**
     * Write the given <code>istream</code> to a new data set named <code>objectPath</code>.
     * 
     * @param objectPath The path of the data set to write the {@link InputStream} to.
     * @param istream The stream to get the data from. This method will <i>not</i> close the <code>istream</code>!</i>
     * @param size The size of the file represented by the <var>istream</var>.
     */
    public void writeToHDF5Container(final String objectPath, final InputStream istream, final long size)
            throws IOExceptionUnchecked;

    /**
     * Archives the given <var>file</var>
     * 
     * @param rootPath The path in the HDF5 container where <var>file</var> will be archived.
     * @param file The file or directory that will be archived (recursively) in the container.
     */
    public void archiveToHDF5Container(String rootPath, File file);

    /**
     * Closes this object and the file referenced by this object. This object must not be used after being closed.
     */
    public void close();

}
