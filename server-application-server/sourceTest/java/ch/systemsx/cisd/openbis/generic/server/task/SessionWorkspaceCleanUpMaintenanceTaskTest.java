/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author pkupczyk
 */
public class SessionWorkspaceCleanUpMaintenanceTaskTest extends AssertJUnit
{

    private BufferedAppender logRecorder;

    private Mockery context;

    private IApplicationServerApi applicationServerApi;

    private ISessionWorkspaceProvider sessionWorkspaceProvider;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);

        context = new Mockery();
        applicationServerApi = context.mock(IApplicationServerApi.class);
        sessionWorkspaceProvider = context.mock(ISessionWorkspaceProvider.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testExecuteWithNoWorkspaces()
    {
        Map<String, File> sessionWorkspaces = new HashMap<String, File>();

        context.checking(new Expectations()
            {
                {
                    one(sessionWorkspaceProvider).getSessionWorkspaces();
                    will(returnValue(sessionWorkspaces));
                }
            });

        SessionWorkspaceCleanUpMaintenanceTask task = new SessionWorkspaceCleanUpMaintenanceTask(applicationServerApi, sessionWorkspaceProvider);
        task.execute();

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.SessionWorkspaceCleanUpMaintenanceTask - Session workspace clean up finished. Removed 0 workspace(s) of inactive session(s).",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteWithMultipleWorkspaces()
    {
        Map<String, File> sessionWorkspaces = new HashMap<String, File>();
        sessionWorkspaces.put("token1", new File("workspace1"));
        sessionWorkspaces.put("token2", new File("workspace2"));

        context.checking(new Expectations()
            {
                {
                    one(sessionWorkspaceProvider).getSessionWorkspaces();
                    will(returnValue(sessionWorkspaces));

                    one(applicationServerApi).isSessionActive("token1");
                    will(returnValue(true));

                    one(applicationServerApi).isSessionActive("token2");
                    will(returnValue(false));

                    one(sessionWorkspaceProvider).deleteSessionWorkspace("token2");
                }
            });

        SessionWorkspaceCleanUpMaintenanceTask task = new SessionWorkspaceCleanUpMaintenanceTask(applicationServerApi, sessionWorkspaceProvider);
        task.execute();

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.SessionWorkspaceCleanUpMaintenanceTask - Session 'token2' is no longer active. Its session workspace will be removed.",
                logRecorder.getLogContent());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.SessionWorkspaceCleanUpMaintenanceTask - Session workspace clean up finished. Removed 1 workspace(s) of inactive session(s).",
                logRecorder.getLogContent());
    }

}
