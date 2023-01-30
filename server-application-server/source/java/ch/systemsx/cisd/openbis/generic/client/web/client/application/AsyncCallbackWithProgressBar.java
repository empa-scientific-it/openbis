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

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * {@link AsyncCallback} decorator adding a progress bar that is visible until response is received from a RPC call.
 * 
 * @author Piotr Buczek
 */
public class AsyncCallbackWithProgressBar<T> implements AsyncCallback<T>
{
    /** Creates and displays a progress bar with a given message. Returns an action which hides it. */
    public static IDelegatedAction createProgressBar(String progressMessage)
    {
        final AsyncCallback<Void> dummyCallback = new AsyncCallback<Void>()
            {
                @Override
                public void onFailure(Throwable caught)
                {
                }

                @Override
                public void onSuccess(Void result)
                {
                }
            };
        final AsyncCallback<Void> progressCallback =
                AsyncCallbackWithProgressBar.decorate(dummyCallback, progressMessage);
        return new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    progressCallback.onSuccess(null);
                }
            };
    }

    /**
     * Decorates given callback with a progress bar containing given message.
     */
    public static <T> AsyncCallbackWithProgressBar<T> decorate(AsyncCallback<T> decoratedCallback,
            String progressMessage)
    {
        return new AsyncCallbackWithProgressBar<T>(decoratedCallback, progressMessage);
    }

    private final AsyncCallback<T> decoratedCallback;

    private final Dialog progressBar;

    private AsyncCallbackWithProgressBar(AsyncCallback<T> decoratedCallback, String progressMessage)
    {
        super();
        this.decoratedCallback = decoratedCallback;
        this.progressBar = createAndShowProgressBar(progressMessage);
    }

    @Override
    public void onFailure(Throwable caught)
    {
        progressBar.hide();
        decoratedCallback.onFailure(caught);
    }

    @Override
    public void onSuccess(T result)
    {
        progressBar.hide();
        decoratedCallback.onSuccess(result);
    }

    private final static Dialog createAndShowProgressBar(final String title)
    {
        ProgressBar progressBar = new ProgressBar();
        progressBar.auto();

        Dialog dialog = new Dialog();
        GWTUtils.setToolTip(dialog, title);

        dialog.add(progressBar);
        dialog.setButtons("");
        dialog.setAutoHeight(true);
        dialog.setClosable(false);
        dialog.addText(title);
        dialog.setResizable(false);
        dialog.show();
        return dialog;
    }

}
