/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

/**
 * H5 folder flags of a sub tree.
 *
 * @author Franz-Josef Elmer
 */
public class H5FolderFlags
{
    private final String treeRoot;
    private final boolean h5Folders;
    private final boolean h5arFolders;

    public H5FolderFlags(String treeRoot, boolean h5Folders, boolean h5arFolders)
    {
        this.treeRoot = treeRoot;
        this.h5Folders = h5Folders;
        this.h5arFolders = h5arFolders;
    }

    public String getTreeRoot()
    {
        return treeRoot;
    }

    public boolean isH5Folders()
    {
        return h5Folders;
    }

    public boolean isH5arFolders()
    {
        return h5arFolders;
    }

    @Override
    public String toString()
    {
        return treeRoot + ": h5Folders = " + h5Folders + ", h5arFolders = " + h5arFolders;
    }

}
