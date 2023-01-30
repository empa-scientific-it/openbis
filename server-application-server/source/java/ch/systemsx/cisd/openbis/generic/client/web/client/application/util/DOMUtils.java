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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Useful DOM utility methods.
 * 
 * @author Christian Ribeaud
 */
public final class DOMUtils
{

    private DOMUtils()
    {
    }

    public static final String BR = DOM.toString(DOM.createElement("br"));

    /**
     * Creates a &lt;del&gt; <i>HTML</i> element.
     */
    public final static String createDelElement(final String innerText)
    {
        final Element element = DOM.createElement("del");
        DOM.setInnerText(element, innerText);
        return DOM.toString(element);
    }

    /**
     * Creates an <i>HTML</i> link with given <var>href</var>, <var>styleClass</var> and given <var>title</var>.
     * 
     * @param href if <code>null</code> then no <code>href</code> attribute will be added.
     * @param styleClass if <code>null</code> then no <code>class</code> attribute will be added.
     * @param title if <code>null</code> then no <code>title</code> attribute will be added.
     */
    public final static Element createAnchorElement(final String styleClass, final String href,
            final String title)
    {
        final Element anchor = DOM.createAnchor();
        if (styleClass != null)
        {
            DOM.setElementAttribute(anchor, "class", styleClass);
        }
        if (href != null)
        {
            DOM.setElementAttribute(anchor, "href", href);
        }
        if (title != null)
        {
            DOM.setElementAttribute(anchor, "title", title);
        }
        return anchor;
    }
}
