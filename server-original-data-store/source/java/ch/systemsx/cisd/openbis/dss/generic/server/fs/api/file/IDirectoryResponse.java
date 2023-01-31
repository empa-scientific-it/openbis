/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.fs.api.file;

import java.util.Date;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * A file system response representing a directory
 * 
 * @author Jakub Straszewski
 */
public interface IDirectoryResponse extends IFileSystemViewResponse
{
    /**
     * Adds a directory entry to the listing with specified last modified timestamp.
     */
    public void addDirectory(String directoryName, final long lastModified);

    /**
     * Adds a directory entry to the listing with specified last modified timestamp.
     */
    public void addDirectory(String directoryName, Date lastModified);

    /**
     * Adds a directory entry to the listing with dss startup time as last modified timestamp
     */
    public void addDirectory(String directoryName);

    /**
     * Adds a file entry to the listing with specified size and modification date.
     */
    public void addFile(String fileName, final long size, final long lastModified);

    /**
     * Adds a file entry to the listing with size and modification date from the specified node
     */
    public void addFile(String fileName, final IHierarchicalContentNode node);

}
