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
package ch.systemsx.cisd.openbis.common.spring;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

/**
 * @author Franz-Josef Elmer
 */
public final class WaitAction implements Action
{
    private final long waitingTime;

    public WaitAction(long waitingTime)
    {
        this.waitingTime = waitingTime;
    }

    @Override
    public Object invoke(Invocation inv) throws Throwable
    {
        Thread.sleep(waitingTime);
        return null;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("wait " + waitingTime);
    }
}
