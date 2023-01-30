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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.google.gwt.user.client.ui.InlineHTML;

/**
 * A {@link InlineHTML} widget extension that renders as an anchor to an external resource. <br>
 * The anchor opens in a new window/tab depending on the browser.
 * 
 * @author Piotr Buczek
 */
public final class ExternalHyperlink extends InlineHTML
{

    /**
     * Creates a hyperlink with its text and href specified.
     * 
     * @param text the hyperlink's text
     * @param href the hyperlink's href
     */
    public ExternalHyperlink(String text, String href)
    {
        // using DOM.createAnchor() like in Hypelink is another way to implement this
        super(createAnchorString(text, href));
    }

    public static String createAnchorString(String text, String href)
    {
        return "<a href=\"" + href + "\" target=\"_blank\">" + text + "</a>";
    }
}
