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

import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.InvocationException;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Abstract super class of call backs. Subclasses have to implement {@link #process(Object)}. Note, that instances of this class and its subclasses
 * are stateful and can not be reused.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T>
{

    private List<IOnSuccessAction<T>> successActions = new ArrayList<IOnSuccessAction<T>>();

    private List<IDelegatedAction> failureActions = new ArrayList<IDelegatedAction>();

    public void addOnSuccessAction(IOnSuccessAction<T> action)
    {
        successActions.add(action);
    }

    public void addOnFailureAction(IDelegatedAction action)
    {
        failureActions.add(action);
    }

    public static final ICallbackListener<Object> DEFAULT_CALLBACK_LISTENER =
            new CallbackListenerAdapter<Object>()
                {

                    //
                    // ICallbackListener
                    //

                    @Override
                    public final void onFailureOf(final IMessageProvider messageProvider,
                            final AbstractAsyncCallback<Object> callback,
                            final String failureMessage, final Throwable throwable)
                    {
                        String message = GWTUtils.translateToHtmlLineBreaks(failureMessage);
                        if (throwable instanceof UserFailureException)
                        {
                            UserFailureException userException = (UserFailureException) throwable;
                            String details =
                                    GWTUtils.translateToHtmlLineBreaks(userException.getDetails());
                            if (details != null)
                            {
                                GWTUtils.createErrorMessageWithDetailsDialog(messageProvider,
                                        message, details).show();
                                return;
                            }
                        }
                        // no details - show simple error message box
                        GWTUtils.alert("Error", message);
                    }

                };

    private static ICallbackListener<?> staticCallbackListener = DEFAULT_CALLBACK_LISTENER;

    /**
     * Sets the global callback listener.
     * <p>
     * Note: THIS METHOD SHOULD NEVER BE USED. It is only used inside the testing framework.
     * </p>
     */
    public final static <T> void setStaticCallbackListener(
            final ICallbackListener<T> callbackListener)
    {
        assert callbackListener != null : "Unspecified ICallbackListener implementation.";
        staticCallbackListener = callbackListener;
    }

    /**
     * Gets the global callback listener
     * <p>
     * N.b. This method is just for testing purposes.
     * </p>
     */
    public final static ICallbackListener<?> getStaticCallbackListener()
    {
        return staticCallbackListener;
    }

    private final ICallbackListener<T> callbackListener;

    // can be null only during tests
    protected final IViewContext<?> viewContext;

    // should the login page appear when the 'session terminated' exception occurs
    private final boolean reloadWhenSessionTerminated;

    public AbstractAsyncCallback(final IViewContext<?> viewContext)
    {
        this(viewContext, false);
    }

    public AbstractAsyncCallback(final IViewContext<?> viewContext,
            final ICallbackListener<T> callbackListenerOrNull)
    {
        this(viewContext, callbackListenerOrNull, true);
    }

    public AbstractAsyncCallback(final IViewContext<?> viewContext,
            boolean reloadWhenSessionTerminated)
    {
        this(viewContext, null, reloadWhenSessionTerminated);
    }

    private AbstractAsyncCallback(final IViewContext<?> viewContext,
            final ICallbackListener<T> callbackListenerOrNull, boolean reloadWhenSessionTerminated)
    {
        this.viewContext = viewContext;
        this.reloadWhenSessionTerminated = reloadWhenSessionTerminated;
        // If static ICallbackListener is not DEFAULT_CALLBACK_LISTENER, then we assume being in
        // testing mode. So no customized ICallbackListener (specified in the constructor) possible.
        if (staticCallbackListener != DEFAULT_CALLBACK_LISTENER)
        {
            callbackListener = cast(staticCallbackListener);
        } else if (callbackListenerOrNull == null)
        {
            callbackListener = cast(staticCallbackListener);
        } else
        {
            callbackListener = callbackListenerOrNull;
        }
        assert callbackListener != null : "Unspecified ICallbackListener implementation.";

        // could do this only if staticCallbackListener != DEFAULT_CALLBACK_LISTENER
        this.callbackListener.registerCallback(this);
    }

    @SuppressWarnings("unchecked")
    private final static <T> ICallbackListener<T> cast(final ICallbackListener<?> callbackListener)
    {
        return (ICallbackListener<T>) callbackListener;
    }

    /**
     * Terminates {@link #onFailure(Throwable)}.
     * <p>
     * Default behavior does nothing. Override this in subclasses.
     * </p>
     */
    public void finishOnFailure(final Throwable caught)
    {
    }

    /**
     * Processes the specified result of an asynchronous method invocation.
     */
    protected abstract void process(final T result);

    /**
     * Returns the callback id which can be used in tests.
     * <p>
     * This method should be subclassed if differentiation between callbacks of the same class is needed in the tests.
     * </p>
     */
    public String getCallbackId()
    {
        String id = getClass().getName();
        return id;
    }

    @Override
    public String toString()
    {
        return getCallbackId();
    }

    //
    // AsyncCallback
    //

    @Override
    public final void onFailure(final Throwable caught)
    {
        if (isIncompatibleServerException(caught))
        {
            String sessionExpiredMessage = getMessage(Dict.SESSION_EXPIRED_MESSAGE);
            handleSessionTerminated(sessionExpiredMessage, caught);
            return;
        }

        final String msg;
        if (caught instanceof InvocationException)
        {
            if (StringUtils.isBlank(caught.getMessage()))
            {
                msg = getMessage(Dict.EXCEPTION_INVOCATION_MESSAGE);
            } else
            {
                msg = caught.getMessage();
            }
        } else
        {
            final String message = caught.getMessage();
            if (StringUtils.isBlank(message))
            {
                msg = getMessage(Dict.EXCEPTION_WITHOUT_MESSAGE, caught.getClass().getName());
            } else
            {
                msg = message;
            }
        }
        if (caught instanceof InvalidSessionException)
        {
            handleSessionTerminated(msg, caught);
        } else
        {
            callbackListener.onFailureOf(viewContext, this, msg, caught);
        }
        for (IDelegatedAction a : failureActions)
        {
            a.execute();
        }
        finishOnFailure(caught);
    }

    private boolean isIncompatibleServerException(final Throwable caught)
    {
        return caught instanceof IncompatibleRemoteServiceException;
    }

    private String getMessage(String messageKey, Object... params)
    {
        if (viewContext != null)
        {
            return viewContext.getMessage(messageKey, params);
        } else
        {
            return messageKey;
        }
    }

    private void refreshPageInBrowser()
    {
        if (viewContext != null)
        {
            viewContext.getPageController().reload(true);
        }
    }

    private void handleSessionTerminated(String msg, Throwable caught)
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            refreshPageInBrowser();
        } else
        {
            showSessionTerminated(msg);
        }
        // only for tests
        if (staticCallbackListener != DEFAULT_CALLBACK_LISTENER)
        {
            callbackListener.onFailureOf(viewContext, this, msg, caught);
        }
    }

    private void showSessionTerminated(String msg)
    {
        System.err.println("Session terminated"); // only for tests
        Dialog dialog = new Dialog();
        GWTUtils.setToolTip(dialog, getMessage(Dict.MESSAGEBOX_WARNING));

        dialog.addText(msg);
        dialog.setHideOnButtonClick(false);
        dialog.setModal(true);
        dialog.setHideOnButtonClick(true);
        dialog.show();
        // logout the user after his confirmation
        dialog.addWindowListener(new WindowListener()
            {
                @Override
                public void windowHide(WindowEvent we)
                {
                    if (reloadWhenSessionTerminated)
                    {
                        refreshPageInBrowser();
                    }
                }
            });
    }

    @Override
    public final void onSuccess(final T result)
    {
        performSuccessActionOrIgnore(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    doOnSuccess(result);
                }
            });
    }

    private void doOnSuccess(final T result)
    {
        process(result);
        for (IOnSuccessAction<T> a : successActions)
        {
            a.execute(result);
        }
        callbackListener.finishOnSuccessOf(this, result);
    }

    /**
     * This method should be overriden if callback should be ignored on success. By default <var>successAction</var> is always executed.
     * <p>
     * In overriden method one should ignore the callback with {@link #ignore()}. Otherwise the <var>successAction</var> should be executed.
     */
    protected void performSuccessActionOrIgnore(IDelegatedAction successAction)
    {
        successAction.execute();
    }

    /**
     * This method should be called if callback will not be processed immediately after creation. It is needed for our system test framework to work
     * properly.
     */
    public final void ignore()
    {
        callbackListener.ignoreCallback(this);
    }

    /**
     * NOTE: The basic rule is 'Never reuse a callback object: Instances of AbstractAsyncCallback are stateful'.
     * <p>
     * This method is only for special callbacks that are reusable. Call it just before calling service method but make sure {@link #ignore()} was
     * called first in callback constructor after calling abstract constructor. It is needed for our system test framework to work properly.
     */
    public final void reuse()
    {
        callbackListener.registerCallback(this);
    }
}
