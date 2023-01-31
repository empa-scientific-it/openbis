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

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;

/**
 * An abstract superclass for V2 dropboxes implemented in java
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractJavaDataSetRegistrationDropboxV2 implements
        IJavaDataSetRegistrationDropboxV2
{
    /**
     * Default implementation is to do nothing. Subclasses may override.
     */
    @Override
    public void postStorage(DataSetRegistrationContext context)
    {

    }

    /**
     * Default implementation is to do nothing. Subclasses may override.
     */
    @Override
    public void preMetadataRegistration(DataSetRegistrationContext context)
    {

    }

    /**
     * Default implementation is to do nothing. Subclasses may override.
     */
    @Override
    public void postMetadataRegistration(DataSetRegistrationContext context)
    {

    }

    /**
     * Default implementation is to do nothing. Subclasses may override.
     */
    @Override
    public void rollbackPreRegistration(DataSetRegistrationContext context, Throwable throwable)
    {

    }

    /**
     * A retry function is always defined.
     */
    @Override
    public final boolean isRetryFunctionDefined()
    {
        return true;
    }

    /**
     * Default implementation is to not retry processing.
     * 
     * @return false
     */
    @Override
    public boolean shouldRetryProcessing(DataSetRegistrationContext context, Exception problem)
            throws NotImplementedException
    {
        return false;
    }

}
