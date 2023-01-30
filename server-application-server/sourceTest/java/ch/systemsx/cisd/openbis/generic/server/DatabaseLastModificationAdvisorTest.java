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

package ch.systemsx.cisd.openbis.generic.server;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind.DATASET_TYPE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind.DATA_SET;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;

/**
 * @author Kaloyan Enimanev
 */
public class DatabaseLastModificationAdvisorTest extends AssertJUnit
{
    private Mockery context;

    private MethodInvocation methodInvocation;

    private LastModificationState state;

    private DatabaseLastModificationAdvisor advisor;

    @BeforeMethod
    public void setUp()
    {
        state = new LastModificationState();
        advisor = new DatabaseLastModificationAdvisor(state);

        context = new Mockery();
        methodInvocation = context.mock(MethodInvocation.class);
    }

    @Test
    public void testCreateModificationNoticed() throws Throwable
    {
        long initialState = state.getLastModificationTime(createOrDelete(DATASET_TYPE));
        waitForClockChange();

        context.checking(new Expectations()
            {
                {
                    one(methodInvocation).proceed();

                    one(methodInvocation).getMethod();
                    will(returnValue(getCreateMethod()));
                }
            });

        MethodInterceptor interceptor = (MethodInterceptor) advisor.getAdvice();
        interceptor.invoke(methodInvocation);
        long changedState = state.getLastModificationTime(createOrDelete(DATASET_TYPE));
        assertTrue("No modification registered in the backend state", initialState != changedState);
    }

    @Test
    public void testUpdatedModificationNoticed() throws Throwable
    {
        long initialState = state.getLastModificationTime(edit(DATA_SET));
        waitForClockChange();

        context.checking(new Expectations()
            {
                {
                    one(methodInvocation).proceed();

                    one(methodInvocation).getMethod();
                    will(returnValue(getUpdateMethod()));
                }
            });

        MethodInterceptor interceptor = (MethodInterceptor) advisor.getAdvice();
        interceptor.invoke(methodInvocation);
        long changedState = state.getLastModificationTime(edit(DATA_SET));
        assertTrue("No modification registered in the backend state", initialState != changedState);
    }

    private Method getCreateMethod() throws Exception
    {
        return ICommonServer.class.getMethod("registerDataSetType", new Class[]
        { String.class, DataSetType.class });
    }

    private Method getUpdateMethod() throws Exception
    {
        return ICommonServer.class.getMethod("updateDataSet", new Class[]
        { String.class, DataSetUpdatesDTO.class });
    }

    private void waitForClockChange()
    {
        try
        {
            Thread.sleep(1001);
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
}
