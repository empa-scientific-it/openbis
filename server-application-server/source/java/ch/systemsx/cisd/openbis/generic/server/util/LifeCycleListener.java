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

package ch.systemsx.cisd.openbis.generic.server.util;

import org.eclipse.jetty.util.component.LifeCycle;

/**
 * Stops a component (e.g. Web Server) if it goes into state "Failure". Prints messages on console in case of success and failure.
 *
 * @author Franz-Josef Elmer
 */
public class LifeCycleListener implements LifeCycle.Listener
{

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable)
    {
        System.err.println("ERROR: Failed component " + lifeCycle + ": " + throwable);
        try
        {
            lifeCycle.stop();
        } catch (Exception ex)
        {
            System.err.println("ERROR: Couldn't stop component " + lifeCycle);
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle)
    {
        System.out.println("SERVER STARTED: " + lifeCycle);
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle)
    {
        System.out.println("STARTING SERVER: " + lifeCycle);
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle)
    {
    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle)
    {
    }

}
