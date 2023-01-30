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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;

/**
 * A command which will be executed after a successful invocation of {@link AsyncCallback#onSuccess(Object)}.
 * 
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public interface ITestCommand
{
    /**
     * Executes this command.
     */
    public void execute();

    /**
     * @return <tt>true</tt> if the specified result should trigger this command in case of an invocation of {@link AsyncCallback#onSuccess(Object)},
     *         otherwise <tt>false</tt>
     */
    public boolean isValidOnSucess(Object result);

    /**
     * @return <tt>true</tt> if the specified <var>failureMessage</var> and <var>throwable</var> should trigger this command in case of an invocation
     *         of {@link AsyncCallback#onFailure(Throwable)} of specified <var>callback</var>, otherwise <tt>false</tt>
     */
    public boolean isValidOnFailure(AbstractAsyncCallback<?> callback, String failureMessage,
            Throwable throwable);

}
