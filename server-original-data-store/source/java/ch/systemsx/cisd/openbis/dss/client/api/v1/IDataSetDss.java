/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.io.InputStream;

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * The representation of a Data Set managed by a DSS server. It is safe to use instances in multiple threads.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetDss
{
    /**
     * The code of this data set.
     */
    @Retry
    public String getCode();

    /**
     * List files contained in this data set.
     * 
     * @param startPath The path for the listing. The path must be relative with respect to the data set, such as the path returned by
     *            {@link FileInfoDssDTO#getPathInDataSet}. Use "/" or "" for the root of the hierarchy for this data set.
     * @param isRecursive If true, the contents of any subdirectories will be listed as well.
     */
    @Retry
    public FileInfoDssDTO[] listFiles(String startPath, boolean isRecursive)
            throws IllegalArgumentException, InvalidSessionException;

    /**
     * Get an {@link InputStream} with contents of a file contained in this data set.
     * 
     * @param path The path of the file to retrieve. The path must be relative with respect to the data set, such as the path returned by
     *            {@link FileInfoDssDTO#getPathInDataSet}.
     */
    @Retry
    public InputStream getFile(String path) throws IllegalArgumentException,
            InvalidSessionException;

    /**
     * Get a URL that will return the contents of a file contained in this data set. The URL will be valid for the lifetime of the session.
     * 
     * @param path The path of the file to retrieve. The path must be relative with respect to the data set, such as the path returned by
     *            {@link FileInfoDssDTO#getPathInDataSet}.
     */
    @Retry
    public String getSessionURLForFile(String path) throws IllegalArgumentException,
            InvalidSessionException;

    /**
     * Get a URL that will return the contents of a file contained in this data set. The URL will be valid for specified time duration.
     * 
     * @param path The path of the file to retrieve. The path must be relative with respect to the data set, such as the path returned by
     *            {@link FileInfoDssDTO#getPathInDataSet}.
     * @param validityDurationInSeconds The number of seconds for which the URL is valid.
     */
    @Retry
    public String getURLForFileWithTimeout(String path, long validityDurationInSeconds)
            throws IllegalArgumentException, InvalidSessionException;

    /**
     * Returns a {@link File}, if possible, that directly references the contents of a data set in the data store server. This is only possible if the
     * file system used by the DSS is also mounted locally.
     * 
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, paths are returned
     *            in the context of the DSS' file system mounts.
     * @return Returns null if the operation is not possible (e.g. when the data set is a container), a File that references the contents of the data
     *         set otherwise.
     * @since 1.1
     */
    @Retry
    public File tryLinkToContents(String overrideStoreRootPathOrNull)
            throws InvalidSessionException;

    /**
     * Returns the internal storage path of this data set in the datastore.
     * <p>
     * <b>Caution: using the return value of this methods to perform any operation on the dataset, e.g. in your own scripts, scripts opens you to a
     * set of race conditions, e.g. when the dataset is moved to another share or deleted. Consider using {@link #tryLinkToContents(String)} if you
     * are actually interested in the data set content.</b>
     * 
     * @return Returns null if the operation is not possible (e.g. when the data set is a container), the path of the data set in the data store
     *         otherwise.
     * @since 1.1 (API call)
     */
    @Retry
    public String tryGetInternalPathInDataStore() throws InvalidSessionException;

    /**
     * Returns a {@link File}, if possible, that directly references the contents of a data set in the data store server. If not possible, downloads
     * the data set contents and returns a File in the downloadDir containing the contents of the data set.
     * 
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, datasets are copied
     *            to the downloadDir folder.
     * @param downloadDir The directory in which to place the contents of the data set if they must be downloaded.
     * @return A File containing the contents of the data set.
     * @since 1.1
     */
    @Retry
    public File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, File downloadDir)
            throws IllegalArgumentException, InvalidSessionException;

    /**
     * Returns a {@link File}, if possible, that directly references some specified content of a data set in the data store server. If not possible,
     * downloads that content and returns a File in the downloadDir containing that content.
     * 
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, datasets are copied
     *            to the downloadDir folder.
     * @param downloadDir The directory in which to place the contents of the data set if they must be downloaded.
     * @param pathInDataSet Path of requested content inside the data set.
     * @return A File containing the requested content.
     */
    @Retry
    public File getLinkOrCopyOfContent(String overrideStoreRootPathOrNull, File downloadDir,
            String pathInDataSet) throws IllegalArgumentException, InvalidSessionException;

}
