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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author pkupczyk
 */
public class SessionCleanUpMaintenanceTaskTest extends AssertJUnit
{

    private BufferedAppender logRecorder;

    private Mockery context;

    private ISessionManager sessionManager;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);

        context = new Mockery();
        sessionManager = context.mock(ISessionManager.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        Session session1 = new Session("test-user-1", "test-session-token-1", new Principal(), "test-host-1", 1);
        Session session2 = new Session("test-user-2", "test-session-token-2", new Principal(), "test-host-2", 2);

        List<Session> sessions = new ArrayList<Session>();
        sessions.add(session1);
        sessions.add(session2);

        context.checking(new Expectations()
        {
            {
                one(sessionManager).getSessions();
                will(returnValue(sessions));

                one(sessionManager).isSessionActive(session1.getSessionToken());
                will(returnValue(true));

                one(sessionManager).isSessionActive(session2.getSessionToken());
                will(returnValue(false));

                one(sessionManager).expireSession(session2.getSessionToken());
            }
        });

        SessionCleanUpMaintenanceTask task = new SessionCleanUpMaintenanceTask(sessionManager);
        task.execute();

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.SessionCleanUpMaintenanceTask - Session 'test-session-token-2' is no longer active. It will be removed.",
                logRecorder.getLogContent());

        AssertionUtil.assertContainsLines(
                "INFO  OPERATION.SessionCleanUpMaintenanceTask - Sessions clean up finished. Removed 1 inactive session(s).",
                logRecorder.getLogContent());
    }

}
