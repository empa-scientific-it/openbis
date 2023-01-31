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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.BooleanStatus;

/**
 * @author Franz-Josef Elmer
 */
public interface IDataSetFileOperationsExecutor
{
    BooleanStatus checkSame(File dataSet, File destination);

    BooleanStatus exists(File file);

    void createFolder(File folder);

    void deleteFolder(File folder);

    void copyDataSetToDestination(File dataSet, File destination);

    // uses rsync --delete for performance both locally and remotely
    void syncDataSetWithDestination(File dataSet, File destination);

    void retrieveDataSetFromDestination(File dataSet, File destination);

    void renameTo(File newFile, File oldFile);

    void createMarkerFile(File markerFile);

    long freeSpaceKb(String path);

}