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

import java.io.OutputStream;
import java.util.List;

import ch.systemsx.cisd.hdf5.h5ar.ArchiveEntry;

/**
 * A simple abstraction of the methods needed to read from an HDF5 container.
 * 
 * @author Bernd Rinn
 */
public interface IHDF5ContainerReader
{

    /**
     * Closes this object and the file referenced by this object. This object must not be used after being closed.
     */
    public void close();

    /**
     * Returns the members of <var>groupPath</var>. The order is <i>not</i> well defined.
     * 
     * @param groupPath The path of the group to get the members for.
     * @throws IllegalArgumentException If <var>groupPath</var> is not a group.
     */
    public List<ArchiveEntry> getGroupMembers(final String groupPath);

    /**
     * Returns the entry for <var>path</var>, or <code>null</code>, if such a path does not exist in the container.
     */
    public ArchiveEntry tryGetEntry(final String path);

    /**
     * Reads the data set <var>objectPath</var> into the <var>ostream</var>.
     * 
     * @param objectPath The name (including path information) of the data set object in the file. <br>
     *            <b>For unit tests only!</b>
     */
    public void readFromHDF5Container(final String objectPath, final OutputStream ostream);

    /**
     * Returns <code>true</code>, if <var>objectPath</var> exists and <code>false</code> otherwise. <br>
     * <b>For unit tests only!</b>
     */
    public boolean exists(final String objectPath);

    public boolean isFileAbstraction(ArchiveEntry entry);

}
