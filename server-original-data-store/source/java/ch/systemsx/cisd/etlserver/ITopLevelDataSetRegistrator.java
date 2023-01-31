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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.concurrent.locks.Lock;

import ch.systemsx.cisd.common.filesystem.IPathHandler;
import ch.systemsx.cisd.common.filesystem.FaultyPathDirectoryScanningHandler.IFaultyPathDirectoryScanningHandlerDelegate;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A path handler that registers data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface ITopLevelDataSetRegistrator extends IPathHandler, ISelfTestable,
        IFaultyPathDirectoryScanningHandlerDelegate
{
    /**
     * A lock used to synchronize shutting down the processing thread of the top-level data set registrator.
     */
    public Lock getRegistrationLock();

    /**
     * State information about the top-level data set registrator.
     */
    public TopLevelDataSetRegistratorGlobalState getGlobalState();

    /**
     * Process a file, using the callerDataSetInformation as a guide for creating the data set and notifying the delegate of activity.
     * 
     * @param file The file to process.
     * @param callerDataSetInformation A DataSetInformation provided by the caller. Implementors will want to use this as a template when they
     *            register data sets.
     * @param delegate The delegate to notify as activity happens.
     */
    public void handle(File file, String userSessionToken,
            DataSetInformation callerDataSetInformation, ITopLevelDataSetRegistratorDelegate delegate);

}
