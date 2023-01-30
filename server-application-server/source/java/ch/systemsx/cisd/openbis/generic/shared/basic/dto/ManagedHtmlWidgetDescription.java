/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedOutputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedOutputWidgetType;

/**
 * {@link IManagedOutputWidgetDescription} implementation for multi-line text fields. This class functions as a simple marker that the UI should use
 * an HTML widget to display the result, and offers no configuration options or behavior.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ManagedHtmlWidgetDescription implements IManagedOutputWidgetDescription
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String html;

    public ManagedHtmlWidgetDescription()
    {
        setHtml("");
    }

    public ManagedHtmlWidgetDescription(String htmlText)
    {
        setHtml(htmlText);
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    //
    // IManagedWidgetDescription
    //
    @Override
    public ManagedOutputWidgetType getManagedOutputWidgetType()
    {
        return ManagedOutputWidgetType.HTML;
    }

}
