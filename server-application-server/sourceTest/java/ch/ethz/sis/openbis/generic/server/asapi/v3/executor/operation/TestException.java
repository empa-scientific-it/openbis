/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

public class TestException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    private boolean stackTraceFilled;

    public TestException()
    {
        super("testException");
    }

    public TestException(String message)
    {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        // Make sure the stack trace is only initialized once. Without it the TestNG throwException
        // action would reinitialize the stack trace and it would be different from the one that was
        // retrieved when defining Expectations.

        if (false == stackTraceFilled)
        {
            super.fillInStackTrace();
            stackTraceFilled = true;
        }

        return this;
    }
}