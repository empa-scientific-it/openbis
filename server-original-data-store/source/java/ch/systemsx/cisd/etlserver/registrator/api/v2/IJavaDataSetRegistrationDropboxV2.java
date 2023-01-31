/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import ch.ethz.cisd.hotdeploy.Plugin;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;

/**
 * The interface that V2 dropboxes must implement. Defines the process method, which is called to handle new data in the dropbox's incoming folder,
 * and various event methods called as the registration process progresses.
 * 
 * @author Pawel Glyzewski
 */
public interface IJavaDataSetRegistrationDropboxV2 extends Plugin
{

    /**
     * Invoked when new data is found in the incoming folder. Implements the logic of registering and modifying entities.
     * 
     * @param transaction The transaction that offers methods for registering and modifying entities and performing operations on the file system.
     */
    public void process(IDataSetRegistrationTransactionV2 transaction);

    /**
     * Invoked just before the metadata is registered with the openBIS AS. Gives dropbox implementations an opportunity to perform additional
     * operations. If an exception is thrown in this method, the transaction is rolledback.
     * 
     * @param context Context of the registration. Offers access to the global state and persistent map.
     */
    public void preMetadataRegistration(DataSetRegistrationContext context);

    /**
     * Invoked if the transaction is rolledback before the metadata is registered with the openBIS AS.
     * 
     * @param context Context of the registration. Offers access to the global state and persistent map.
     * @param throwable The throwable that triggered rollback.
     */
    public void rollbackPreRegistration(DataSetRegistrationContext context, Throwable throwable);

    /**
     * Invoked just after the metadata is registered with the openBIS AS. Gives dropbox implementations an opportunity to perform additional
     * operations. If an exception is thrown in this method, it is logged but otherwise ignored.
     * 
     * @param context Context of the registration. Offers access to the global state and persistent map.
     */
    public void postMetadataRegistration(DataSetRegistrationContext context);

    /**
     * Invoked after the data has been stored in its final location on the file system and the storage has been confirmed with the AS.
     * 
     * @param context Context of the registration. Offers access to the global state and persistent map.
     */
    public void postStorage(DataSetRegistrationContext context);

    /**
     * Is a function defined that can be used to check if a failed registration should be retried? Primarily for use implementations of this interface
     * that dispatch to dynamic languages.
     * 
     * @return true shouldRetryProcessing is defined, false otherwise.
     */
    public boolean isRetryFunctionDefined();

    /**
     * Given the problem with registration, should it be retried?
     * 
     * @param context Context of the registration. Offers access to the global state and persistent map.
     * @param problem The exception that caused the registration to fail.
     * @return true if the registration should be retried.
     */
    public boolean shouldRetryProcessing(DataSetRegistrationContext context, Exception problem)
            throws NotImplementedException;

}
