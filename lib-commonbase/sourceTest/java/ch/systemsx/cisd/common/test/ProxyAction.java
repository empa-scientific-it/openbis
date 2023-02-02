/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.test;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

/**
 * Abstract proxy class for {@link Action} instances. {@link #doBeforeReturn()} will be invoked before {@link Action#invoke(Invocation)} of the
 * wrapped action.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class ProxyAction implements Action
{
    private final Action action;

    public ProxyAction(Action action)
    {
        this.action = action;
    }

    @Override
    public void describeTo(Description description)
    {
        action.describeTo(description);
    }

    @Override
    public Object invoke(Invocation invocation) throws Throwable
    {
        doBeforeReturn();
        return action.invoke(invocation);
    }

    protected abstract void doBeforeReturn();

}
