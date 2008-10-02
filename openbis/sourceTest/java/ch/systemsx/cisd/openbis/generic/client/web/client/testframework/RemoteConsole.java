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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ICallbackListener;

/**
 * A class which allows to execute a sequence of {@link ITestCommand} instances. The commands are
 * executed in the order they have been added by {@link #prepare(ITestCommand)}. In order to be
 * executed a command has to be valid for the kind of {@link AsyncCallback} invocation. With the
 * method {@link #finish(int)} a timeout will be specified after which the test will be terminated
 * independent whether all commands have been executed or not. In the later case the test fails.
 * 
 * @author Franz-Josef Elmer
 */
public class RemoteConsole
{
    private final AbstractGWTTestCase testCase;
    private final List<ITestCommand> commands;
    
    private int entryIndex;
    
    /**
     * Creates an instance for the specified test.
     */
    public RemoteConsole(final AbstractGWTTestCase testCase)
    {
        this.testCase = testCase;
        commands = new ArrayList<ITestCommand>();
        AbstractAsyncCallback.setCallbackListener(new ICallbackListener()
            {
                public void onFailureOf(AsyncCallback<Object> callback, String failureMessage,
                        Throwable throwable)
                {
                    if (entryIndex < commands.size()
                            && commands.get(entryIndex).validOnFailure(callback, failureMessage, throwable))
                    {
                        executeCommand();
                    } else
                    {
                        Assert.fail("Failed callback " + callback + ": " + failureMessage + "["
                                + throwable.getClass() + "]");
                    }
                }

                public void finishOnSuccessOf(AsyncCallback<Object> callback, Object result)
                {
                    if (entryIndex < commands.size()
                            && commands.get(entryIndex).validOnSucess(callback, result))
                    {
                        executeCommand();
                    }
                }
                
                private void executeCommand()
                {
                    ITestCommand testCommand = commands.get(entryIndex++);
                    System.out.println("EXECUTE: " + testCommand);
                    testCommand.execute();
                    if (entryIndex == commands.size())
                    {
                        testCase.terminateTest();
                    }
                }

            });
    }

    /**
     * Prepares the console with the specified command which will be executed if the
     * specified condition is fulfilled. 
     */
    public RemoteConsole prepare(ITestCommand command)
    {
        commands.add(command);
        return this;
    }
    
    /**
     * Sets the timeout after which the test is terminated.
     * 
     * @throws AssertionError if not all commands have been executed.
     */
    public void finish(int delayInMilliseconds)
    {
        new Timer()
            {
                @Override
                public void run()
                {
                    AbstractAsyncCallback.setCallbackListener(null);
                    int numberOfUnexcutedCommands = commands.size() - entryIndex;
                    if (numberOfUnexcutedCommands > 0)
                    {
                        Assert.fail("Console not finished. Last "
                                + (numberOfUnexcutedCommands == 1 ? "command has"
                                        : numberOfUnexcutedCommands + " commands have")
                                + " not been executed.");
                    }
                }
            }.schedule(delayInMilliseconds);
       testCase.delayTestTermination(delayInMilliseconds + 1000);
    }
}
