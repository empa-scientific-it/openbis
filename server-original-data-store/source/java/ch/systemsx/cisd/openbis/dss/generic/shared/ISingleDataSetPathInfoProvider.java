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

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * Provides information about paths of single data set.
 * 
 * @author Piotr Buczek
 */
public interface ISingleDataSetPathInfoProvider
{
    /** @return information about path of the data set root directory */
    DataSetPathInfo getRootPathInfo();

    /**
     * @return information about path of data set file with given <var>relativePath</var>, or </code>null<code> if such a path doesn't exist
     */
    DataSetPathInfo tryGetPathInfoByRelativePath(String relativePath);

    /** @return list of paths that are children of given path (may be empty) */
    List<DataSetPathInfo> listChildrenPathInfos(DataSetPathInfo parent);

    /** @return list of paths that match given pattern for relative path */
    List<DataSetPathInfo> listMatchingPathInfos(String relativePathPattern);

    /** @return list of paths that start with given path and have file name matching given pattern */
    List<DataSetPathInfo> listMatchingPathInfos(String startingPath, String fileNamePattern);
}
