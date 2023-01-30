/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * @author pkupczyk
 */
public class LinkTableCell implements ISerializableComparable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String text;

    private String url;

    private boolean openInNewWindow;

    public LinkTableCell()
    {
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getText()
    {
        return text;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public void setOpenInNewWindow(boolean openInNewWindow)
    {
        this.openInNewWindow = openInNewWindow;
    }

    public boolean isOpenInNewWindow()
    {
        return openInNewWindow;
    }

    public String getHtmlString()
    {
        return URLMethodWithParameters.createEmbededLinkHtml(text, url, null);
    }

    @Override
    public int compareTo(ISerializableComparable o)
    {
        return toString().compareTo(String.valueOf(o));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (!(obj instanceof LinkTableCell))
            return false;

        LinkTableCell other = (LinkTableCell) obj;
        return other.toString().equals(toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return url != null ? url : "";
    }

}
