/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.admin;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * @author Franz-Josef Elmer
 */
public class ShareManagerApplicationTest extends AssertJUnit
{
    private static final String SEESION_TOKEN = "seesiontoken";

    private static final class MockCommand extends AbstractCommand
    {
        private final CommonArguments arguments = new CommonArguments()
            {
                @Override
                protected boolean allAdditionalMandatoryArgumentsPresent()
                {
                    return arguments.size() == 1;
                }
            };

        private final IGeneralInformationService service;

        private final IDssServiceRpcGeneric dssService;

        private String recordedDownloadUrl;

        private String recordedOpenBisServerUrl;

        private boolean executed;

        MockCommand(IGeneralInformationService service, IDssServiceRpcGeneric dssService)
        {
            super("mock");
            this.service = service;
            this.dssService = dssService;
        }

        @Override
        protected CommonArguments getArguments()
        {
            return arguments;
        }

        @Override
        protected String getRequiredArgumentsString()
        {
            return "<arg>";
        }

        @Override
        IDssServiceRpcGeneric createDssService(String downloadUrl)
        {
            this.recordedDownloadUrl = downloadUrl;
            return dssService;
        }

        @Override
        IGeneralInformationService createGeneralInfoService(String openBisServerUrl)
        {
            recordedOpenBisServerUrl = openBisServerUrl;
            return service;
        }

        @Override
        void execute()
        {
            executed = true;
        }

    }

    private Mockery context;

    private IDssServiceRpcGeneric dssService;

    private IGeneralInformationService service;

    private ShareManagerApplication shareManagerApplication;

    private MockCommand mockCommand;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dssService = context.mock(IDssServiceRpcGeneric.class);
        service = context.mock(IGeneralInformationService.class);
        mockCommand = new MockCommand(service, dssService);
        shareManagerApplication = new ShareManagerApplication(mockCommand);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test(expectedExceptionsMessageRegExp = "No command specified. Allowed commands are \\[mock\\]\\.", expectedExceptions = UserFailureException.class)
    public void testParseAndRunMissingCommand()
    {
        shareManagerApplication.parseAndRun();

        context.assertIsSatisfied();
    }

    @Test(expectedExceptionsMessageRegExp = "Unknown command 'hello'. Allowed commands are \\[mock\\]\\.", expectedExceptions = UserFailureException.class)
    public void testParseAndRunUnknowngCommand()
    {
        shareManagerApplication.parseAndRun("hello");

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingMandatoryArgument()
    {
        try
        {
            shareManagerApplication.parseAndRun("mock", "-u", "user", "-p", "pswd");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Usage: "
                            + AbstractCommand.BASH_COMMAND
                            + " mock [options] <arg>\n"
                            + " [-p,--password] VAL            : User login password\n"
                            + " [-sp,--service-properties] VAL : Path to DSS service.properties (default:\n"
                            + "                                  etc/service.properties)\n"
                            + " [-u,--username] VAL            : User login name\n" + "Example: "
                            + AbstractCommand.BASH_COMMAND + " mock -p VAL -sp VAL -u VAL <arg>\n",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testParseAndRunLoginFailed()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices("user", "pswd");
                    will(returnValue(null));
                }
            });

        try
        {
            shareManagerApplication.parseAndRun("mock", "-u", "user", "-p", "pswd", "a");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Invalid username/password combination.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testParseAndRun()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices("user", "pswd");
                    will(returnValue(SEESION_TOKEN));
                }
            });

        shareManagerApplication.parseAndRun("mock", "-u", "user", "-p", "pswd", "a");

        assertEquals(true, mockCommand.executed);
        assertEquals("http://localhost:8888", mockCommand.recordedOpenBisServerUrl);
        assertEquals("http://localhost:8889", mockCommand.recordedDownloadUrl);
        context.assertIsSatisfied();
    }

}
