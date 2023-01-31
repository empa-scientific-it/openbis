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
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IJavaDataSetRegistrationDropboxV2;

/**
 * @author Pawel Glyzewski
 */
public class JavaV2TestcaseRegistrationContext implements IJavaDataSetRegistrationDropboxV2
{
    private JythonHookTestTool jythonHookTestTool = JythonHookTestTool.createInTest();

    @Override
    public void process(IDataSetRegistrationTransactionV2 transaction)
    {
        IDataSet dataSet = transaction.createNewDataSet();
        transaction.moveFile(transaction.getIncoming().getPath() + "/sub_data_set_1", dataSet);
        dataSet.setDataSetType("O1");
        dataSet.setExperiment(transaction.getSearchService().getExperiment("/SPACE/PROJECT/EXP"));
        transaction.getRegistrationContext().getPersistentMap().put("body", "1");
    }

    @Override
    public void postStorage(DataSetRegistrationContext context)
    {
        assert_context_content(context, "post_storage", "body", "1");
        assert_context_content(context, "post_storage", "pre_metadata_registration", "2");
        assert_context_content(context, "post_storage", "post_metadata_registration", "3");

        context.getPersistentMap().put("post_storage", "4");
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
        assert_context_content(context, "pre_metadata_registration", "body", "1");
        assert_context_content(context, "pre_metadata_registration", "pre_metadata_registration",
                null);
        assert_context_content(context, "pre_metadata_registration", "post_storage", null);

        context.getPersistentMap().put("pre_metadata_registration", "2");
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
        assert_context_content(context, "post_metadata_registration", "body", "1");
        assert_context_content(context, "post_metadata_registration", "pre_metadata_registration",
                "2");
        assert_context_content(context, "post_metadata_registration", "post_storage", null);

        context.getPersistentMap().put("post_metadata_registration", "3");
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

    @Override
    public boolean isRetryFunctionDefined()
    {
        return false;
    }

    @Override
    public boolean shouldRetryProcessing(DataSetRegistrationContext context, Exception problem)
            throws NotImplementedException
    {
        throw new NotImplementedException();
    }

    private void assert_context_content(DataSetRegistrationContext context, String caller,
            String name, String expected)
    {
        Object value = context.getPersistentMap().get(name);
        String expectedString = expected;
        if ((expectedString == null && expectedString != value)
                || (expectedString != null && (false == expectedString.equals(value))))
        {
            if (value != null && expectedString != null
                    && value.getClass() != expectedString.getClass())
            {
                value = String.format("%s:%s", value.getClass(), value);
                expectedString = String.format("%s:%s", expectedString.getClass(), expected);
            }
            try
            {
                jythonHookTestTool
                        .log(String
                                .format("transaction context failed.in %s the value of %s should have been '%s', but was '%s'",
                                        caller, name, expectedString, value));
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }
}
