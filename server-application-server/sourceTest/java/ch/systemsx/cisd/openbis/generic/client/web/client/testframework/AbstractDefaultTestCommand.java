/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import junit.framework.Assert;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;

/**
 * Abstract super class of all test commands which are executed if there are no active callbacks (all callbacks were detected by {@link RemoteConsole}
 * ).
 * 
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public abstract class AbstractDefaultTestCommand extends Assert implements ITestCommand
{
    /**
     * Creates an instance of command.
     */
    public AbstractDefaultTestCommand()
    {
    }

    @Override
    public boolean isValidOnSucess(Object result)
    {
        return true; // if previous command succeeded this command should be executed
    }

    @Override
    public boolean isValidOnFailure(AbstractAsyncCallback<?> callback, String failureMessage,
            Throwable throwable)
    {
        return false; // if previous command failed this command shouldn't be executed
    }

}
