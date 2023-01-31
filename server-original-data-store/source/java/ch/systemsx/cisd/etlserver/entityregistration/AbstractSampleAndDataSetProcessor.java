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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractSampleAndDataSetProcessor
{

    protected final SampleAndDataSetRegistrationGlobalState globalState;

    protected final File folder;

    public AbstractSampleAndDataSetProcessor(SampleAndDataSetRegistrationGlobalState globalState,
            File folder)
    {
        super();

        this.globalState = globalState;
        this.folder = folder;
    }

    protected void logInfo(String message)
    {
        globalState.getOperationLog().info(message);
    }

    protected void logError(String message)
    {
        globalState.getOperationLog().error(message);
    }

    protected void logError(String message, Throwable e)
    {
        globalState.getOperationLog().error(message, e);
    }

}