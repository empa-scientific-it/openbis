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

package ch.systemsx.cisd.cina.client.util.cli;

import java.util.ArrayList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandGetReplicaTest extends AssertJUnit
{
    private final class MockCommandGetReplica extends CommandGetReplica
    {
        @Override
        protected ICinaUtilities login()
        {
            facade =
                    ch.systemsx.cisd.cina.client.util.v1.impl.CinaUtilitiesFacadeTest.createFacade(
                            service, openbisService, USER_ID, PASSWORD);
            return facade;
        }
    }

    private final static String USER_ID = "userid";

    private final static String PASSWORD = "password";

    private final static String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private ICinaUtilities facade;

    private IGeneralInformationService service;

    private IETLLIMSService openbisService;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        openbisService = context.mock(IETLLIMSService.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    private void setupAuthenticationExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).getMinorVersion();
                    will(returnValue(1));

                    one(service).logout(SESSION_TOKEN);
                }
            });
    }

    private void setupListDataSetsExpectations(final String sampleCode)
    {
        context.checking(new Expectations()
            {
                {
                    SearchCriteria searchCriteria = new SearchCriteria();
                    searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                            MatchClauseAttribute.CODE, sampleCode));

                    ArrayList<Sample> samples = new ArrayList<Sample>();
                    SampleInitializer initializer = new SampleInitializer();
                    initializer.setCode(sampleCode);
                    initializer.setId((long) 1);
                    initializer.setIdentifier("SPACE/" + sampleCode);
                    initializer.setPermId("PERM-ID");
                    initializer.setSampleTypeCode("SAMPLE-TYPE");
                    initializer.setSampleTypeId((long) 1);
                    Sample sample = new Sample(initializer);
                    samples.add(sample);

                    one(service).searchForSamples(SESSION_TOKEN, searchCriteria);
                    will(returnValue(samples));

                    ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
                    one(service).listDataSets(SESSION_TOKEN, samples);
                    will(returnValue(dataSets));

                }
            });
    }

    @Test
    public void testCodePath()
    {
        setupAuthenticationExpectations();
        setupListDataSetsExpectations("REPLICA-ID");

        ICommand command = new MockCommandGetReplica();

        ResultCode exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "REPLICA-ID" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testOutputFolder()
    {
        setupAuthenticationExpectations();
        setupListDataSetsExpectations("REPLICA-ID");

        ICommand command = new MockCommandGetReplica();

        ResultCode exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "-o", "Foo.bundle/", "REPLICA-ID" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testMultipleReplicas()
    {
        setupAuthenticationExpectations();
        setupListDataSetsExpectations("REPLICA-ID1");
        setupListDataSetsExpectations("REPLICA-ID2");

        ICommand command = new MockCommandGetReplica();

        ResultCode exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "REPLICA-ID1", "REPLICA-ID2" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testOldVersion()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    // The service used wasn't available in version 0
                    one(service).getMinorVersion();
                    will(returnValue(0));

                    one(service).logout(SESSION_TOKEN);
                }
            });

        ICommand command = new MockCommandGetReplica();

        try
        {
            command.execute(new String[]
                { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "REPLICA-ID" });
            fail("Command should throw an exception when run against an older version of the interface.");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("Server does not support this feature.", e.getMessage());
        }
    }
}
