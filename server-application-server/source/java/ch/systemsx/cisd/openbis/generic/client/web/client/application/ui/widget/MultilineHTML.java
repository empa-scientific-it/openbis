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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;

/**
 * A {@link HTML} widget extension that preserves newlines in multiline text and provides a tooltip.
 * 
 * @author Piotr Buczek
 */
public final class MultilineHTML extends HTML
{
    /**
     * Creates a MultilineHtml widget with the specified HTML contents.
     * 
     * @param html the new widget's HTML contents
     */
    public MultilineHTML(String html)
    {
        // Another way to implement this would be to preserve white-space:
        // getElement().getStyle().setProperty("whiteSpace", "pre");
        // but then too long lines would not fit in property grid with no word wrapping.
        // So the only option is to replace newlines with <br/> and use '&nbsp;' for other
        // whitespace characters.
        super(preserveWhitespace(html));
    }

    public void setMultilineHTML(String html)
    {
        setHTML(preserveWhitespace(html));
    }

    private static final String BR = DOM.toString(DOM.createElement("br"));

    private static String preserveWhitespace(String html)
    {
        String result = html;
        // to be independent of regexp implementation we have to replace newlines in two steps
        result = result.replaceAll("\n\r", BR);
        result = result.replaceAll("[\n\r]", BR);
        // additionally preserve remaining whitespace (tabs and spaces) using '&nbsp;'
        // the trick is not to change all whitespace to make word wrapping possible
        result = result.replaceAll("\t", "&nbsp;&nbsp;&nbsp; ");
        result = result.replaceAll("[\t]", "&nbsp;&nbsp;&nbsp; ");
        result = result.replaceAll("  ", "&nbsp; ");
        // result will not be wrapped in AbstractBrowserGrid so we wrap it up in div with tooltip
        // WORKAROUND HTML tooltips don't support <br/> :(
        return wrapUpInDivWithTooltip(result, result.replaceAll(BR, "&nbsp;"));
    }

    public static String wrapUpInDivWithTooltip(String text, String tooltip)
    {
        return wrapUpInDivWithTooltip(text, tooltip, null);
    }

    public static String wrapUpInDivWithTooltip(String text, String tooltip, String styleOrNull)
    {
        final Element div = DOM.createDiv();
        div.setInnerHTML(text);
        if (styleOrNull != null)
        {
            div.setAttribute("style", styleOrNull);
        }
        div.setTitle(StringEscapeUtils.unescapeHtml(tooltip));
        return DOM.toString(div);
    }
}
