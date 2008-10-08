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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;



/**
 * Abstract super class of call backs. Its implements {@link #onFailure(Throwable)}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T>
{
    private static final ICallbackListener DUMMY_LISTENER = new ICallbackListener()
        {
            public void onFailureOf(AsyncCallback<Object> callback, String failureMessage, Throwable throwable)
            {
            }

            public void finishOnSuccessOf(AsyncCallback<Object> callback, Object result)
            {
            }
        };
    
    private static final String PREFIX = "exception_";

    private static ICallbackListener callbackListener = DUMMY_LISTENER;
    
    private static final List<AbstractAsyncCallback<?>> callbackObjects =
            new ArrayList<AbstractAsyncCallback<?>>();
    
    /**
     * Sets all callback objects silent. Note: THIS METHOD SHOULD ONLY BE USED IN TEST CODE.
     */
    public static void setAllCallbackObjectsSilent()
    {
        for (AbstractAsyncCallback<?> callback : callbackObjects)
        {
            callback.silent = true;
        }
        callbackObjects.clear();
    }
    
    /**
     * Sets the global callback listener. Note: THIS METHOD SHOULD ONLY BE USED IN TEST CODE.
     */
    public static void setCallbackListener(ICallbackListener listenerOrNull)
    {
        callbackListener = listenerOrNull == null ? DUMMY_LISTENER : listenerOrNull;
    }
    
    protected final GenericViewContext viewContext;
    
    private boolean silent;
    
    /**
     * Creates an instance for the specified view context.
     */
    public AbstractAsyncCallback(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        callbackObjects.add(this);
    }
    
    @SuppressWarnings("unchecked")
    private AsyncCallback<Object> getThis()
    {
        return (AsyncCallback<Object>) this;
    }
    
    public void onFailure(Throwable caught)
    {
        if (silent)
        {
            return;
        }
        final String msg;
        if (caught instanceof InvocationException)
        {
            if (StringUtils.isBlank(caught.getMessage()))
            {
                msg = viewContext.getMessage(PREFIX + "invocationMessage");
            } else
            {
                msg = caught.getMessage();
            }
        } else
        {
            final String message = caught.getMessage();
            if (StringUtils.isBlank(message))
            {
                msg = viewContext.getMessage(PREFIX + "withoutMessage", caught.getClass().getName());
            } else
            {
                msg = message;
            }
        }
        callbackListener.onFailureOf(getThis(), msg, caught);
        MessageBox.alert("Error", msg, null);
        IPageController pageController = viewContext.getPageController();
        if (caught instanceof InvalidSessionException)
        {
            pageController.reload();
        }
    }

    public final void onSuccess(T result)
    {
        if (silent)
        {
            return;
        }
        process(result);
        callbackListener.finishOnSuccessOf(getThis(), result);
    }

    /**
     * Processes the specified result of an asynchronous method invocation.
     */
    protected abstract void process(T result);
}