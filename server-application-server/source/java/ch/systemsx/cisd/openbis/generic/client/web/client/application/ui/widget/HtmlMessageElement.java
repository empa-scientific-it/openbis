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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Message element for a HTML.
 * 
 * @author anttil
 */
public class HtmlMessageElement implements IMessageElement
{

    private final String content;

    private static final String TRUNCATE_SUFFIX = "...";

    public HtmlMessageElement(String content)
    {
        this.content = content;

    }

    @Override
    public int length()
    {
        return content.length();
    }

    @Override
    public Widget render()
    {
        return createHTMLWidget(content);
    }

    @Override
    public Widget render(int maxLength)
    {
        return createHTMLWidget(truncate(content, maxLength));
    }

    private Widget createHTMLWidget(String truncatedContent)
    {
        return new HTML(truncatedContent);
    }

    private String truncate(String text, int maxLength)
    {
        if (shouldTruncate(text, maxLength))
        {
            return text.substring(0, maxLength) + TRUNCATE_SUFFIX;
        } else
        {
            return text;
        }
    }

    private boolean shouldTruncate(String text, int maxLength)
    {
        return text != null && text.length() > maxLength;
    }

    public String getHtml()
    {
        return content;
    }

    @Override
    public String toString()
    {
        return content;
    }
}
