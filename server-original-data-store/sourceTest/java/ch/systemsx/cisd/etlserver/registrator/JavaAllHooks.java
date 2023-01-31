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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.IOException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;

/**
 * @author Pawel Glyzewski
 */
public abstract class JavaAllHooks implements IJavaDataSetRegistrationDropboxV2
{
    protected JythonHookTestTool jythonHookTestTool = JythonHookTestTool.createInTest();

    @Override
    public void postStorage(DataSetRegistrationContext context)
    {
        try
        {
            jythonHookTestTool.log("post_storage");
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void preMetadataRegistration(DataSetRegistrationContext context)
    {
        try
        {
            jythonHookTestTool.log("pre_metadata_registration");
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void postMetadataRegistration(DataSetRegistrationContext context)
    {
        try
        {
            jythonHookTestTool.log("post_metadata_registration");
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    public void rollbackPreRegistration(DataSetRegistrationContext context, Throwable throwable)
    {
        try
        {
            jythonHookTestTool.log("rollback_pre_registration");
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}