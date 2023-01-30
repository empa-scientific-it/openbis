/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HTML;

/**
 * A {@link HTML} panel for displaying static pages.
 * <p>
 * An <i>HTML</i> page named {@code <pageBaseName>.html} is expected in the public directory.
 * </p>
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
public class HtmlPage extends HTML
{
    private final String pageUrl;

    public HtmlPage(final String pageBaseName)
    {
        final RequestBuilder requestBuilder =
                new RequestBuilder(RequestBuilder.GET, pageUrl = createUrl(pageBaseName));
        try
        {
            requestBuilder.sendRequest(null, new HelpRequestCallback());
        } catch (final RequestException ex)
        {
            displayException(ex);
        }
    }

    private final static String createUrl(final String pageBaseName)
    {
        return pageBaseName + ".html";
    }

    private final void displayException(final Throwable ex)
    {
        setText("An error has occurred while getting page '" + pageUrl + "': " + ex.getMessage()
                + ".");
    }

    private final void displayHelp(final String help)
    {
        setHTML(help);
    }

    private final class HelpRequestCallback implements RequestCallback
    {

        @Override
        public final void onError(final Request request, final Throwable ex)
        {
            displayException(ex);
        }

        @Override
        public final void onResponseReceived(final Request request, final Response response)
        {
            displayHelp(response.getText());
        }
    }
}
