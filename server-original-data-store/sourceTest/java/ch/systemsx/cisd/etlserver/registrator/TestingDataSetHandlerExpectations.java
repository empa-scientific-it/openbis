/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.test.AssertionUtil;

public class TestingDataSetHandlerExpectations
{
    /*
     * shouldas
     */
    protected final boolean shouldRegistrationFail;

    protected final boolean shouldReThrowRollbackException;

    /*
     * happends
     */

    protected boolean didServiceRollbackHappen;

    protected boolean didTransactionRollbackHappen;

    public boolean isShouldRegistrationFail()
    {
        return shouldRegistrationFail;
    }

    public boolean isShouldReThrowRollbackException()
    {
        return shouldReThrowRollbackException;
    }

    public TestingDataSetHandlerExpectations(boolean shouldRegistrationFail,
            boolean shouldReThrowRollbackException)
    {
        super();
        this.shouldRegistrationFail = shouldRegistrationFail;
        this.shouldReThrowRollbackException = shouldReThrowRollbackException;
    }

    public void handleRollbackException(Throwable throwable)
    {
        if (shouldReThrowRollbackException || AssertionUtil.tryAsErrorCausedByUnexpectedInvocation(throwable) != null)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } else
        {
            throwable.printStackTrace();
        }
    }
}
