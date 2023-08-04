/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * A component that manages a connection to openBIS and 1 or more data store servers.
 * <p>
 * The component is a kind of state machine. In the initial state, only login is allowed. After login, other operations may be called. Thus clients
 * should follow the following usage pattern:
 * <ol>
 * <li>login</li>
 * <li>...do stuff...</li>
 * <li>logout</li>
 * </ol>
 * <p>
 * The IDssComponent itself is designed to be used in a single thread, though it may return objects that can be used in multiple threads.
 * Documentation for the return values clarifies their level of thread safety.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssComponent
{
    /**
     * Checks whether the session is alive.
     * 
     * @throws InvalidSessionException If the session is not alive.
     */
    public void checkSession() throws InvalidSessionException;

    /**
     * Returns the session token.
     * 
     * @return The session token for an authenticated user.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     */
    public String getSessionToken() throws IllegalStateException;

    /**
     * Get a proxy to the data set designated by the given data set code.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    public IDataSetDss getDataSet(String code) throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Upload a new data set to the DSS.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     * @throws IOExceptionUnchecked If the file transfer fails.
     */
    public IDataSetDss putDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException, IOExceptionUnchecked;

    /**
     * Uploads a file to the session workspace.
     * 
     * @param filePath The path (directory and name) of the file to upload.
     * @param inputStream The content of the file to upload.
     * @throws IOExceptionUnchecked If the file transfer fails.
     */
    public void putFileToSessionWorkspace(String filePath, InputStream inputStream)
            throws IOExceptionUnchecked;

    /**
     * Uploads a file to the session workspace.
     * 
     * @param directory The directory in the session workspace where the file should be uploaded.
     * @param file The file to upload.
     * @throws IOExceptionUnchecked If the file cannot be written.
     */
    public void putFileToSessionWorkspace(String directory, File file)
            throws IOExceptionUnchecked;

    /**
     * Downloads a file from the session workspace.
     * 
     * @param filePath The path (directory and name) of the file to download.
     * @return outputStream The content of the file to download.
     * @throws IOExceptionUnchecked If the file does not exist, is a directory or cannot be opened.
     */
    public InputStream getFileFromSessionWorkspace(String filePath) throws IOExceptionUnchecked;

    /**
     * Downloads a file from the session workspace.
     * 
     * @param filePath The path (directory and name) of the file to download.
     * @param localFile The local file to write the file from the session workspace to.
     * @throws IOExceptionUnchecked If the file does not exist.
     */
    public void getFileFromSessionWorkspace(String filePath, File localFile)
            throws IOExceptionUnchecked;

    /**
     * Delete a file or directory in the session workspace.
     * 
     * @return <code>true</code> if the <var>path</var> doesn't exist anymore.
     */
    public boolean deleteSessionWorkspaceFile(String path);

    /**
     * Validate a data set.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A list of validation errors. The list is empty if there were no validation errors.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Tries to extract the data set property key-values (metadata) from the data. The extracted metadata can be used by clients to minimize the input
     * needed when uploading data sets.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A map of extracted property-key values
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    public Map<String, Serializable> extractMetadata(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Logs the current user out.
     */
    public void logout();

}
