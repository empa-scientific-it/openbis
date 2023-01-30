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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Listener of invocations of methods of {@link AsyncCallback}.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICallbackListener<T>
{
    /**
     * Handles invocations of {@link AsyncCallback#onFailure(Throwable)} of the specified callback object with the specified failure message and
     * throwable. This method will be invoked before the callback object is showing the failure message in a popup dialog.
     */
    public void onFailureOf(final IMessageProvider messageProvider,
            final AbstractAsyncCallback<T> callback, final String failureMessage,
            final Throwable throwable);

    /**
     * Handles invocations of {@link AsyncCallback#onSuccess(Object)} of the specified callback object with the specified result object. This method
     * will be invoked after the callback object is actually processing the result object.
     */
    public void finishOnSuccessOf(final AbstractAsyncCallback<T> callback, final T result);

    public void registerCallback(AbstractAsyncCallback<?> callback);

    public void ignoreCallback(AbstractAsyncCallback<?> callback);
}
