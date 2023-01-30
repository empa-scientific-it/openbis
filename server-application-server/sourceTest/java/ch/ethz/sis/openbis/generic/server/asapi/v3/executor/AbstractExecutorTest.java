/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor;

import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class AbstractExecutorTest
{

    protected Mockery context;

    protected IDAOFactory daoFactory;

    protected IOperationContext operationContext;

    @BeforeMethod
    protected void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        operationContext = context.mock(IOperationContext.class);
        init();
    }

    protected void init()
    {

    }

    @AfterMethod
    protected void afterMethod()
    {
        context.assertIsSatisfied();
    }

    public static Session createSession()
    {
        final PersonPE person = new PersonPE();
        person.setUserId("TEST_PERSON");

        final Session session = new Session(person.getUserId(), "TEST_SESSION_TOKEN", new Principal(), "http://test-host.com", 1);
        session.setPerson(person);

        return session;
    }

}
