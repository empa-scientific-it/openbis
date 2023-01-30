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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * A table cell for links to the dss.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssLinkTableCell implements ISerializableComparable
{
    // Servlet parameters
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String linkText;

    private LinkModel linkModel;

    public DssLinkTableCell(String linkText, LinkModel linkModel)
    {
        this.linkText = linkText;
        this.linkModel = linkModel;
    }

    // For GWT
    @SuppressWarnings("unused")
    private DssLinkTableCell()
    {
    }

    public void setLinkModel(LinkModel linkModel)
    {
        this.linkModel = linkModel;
    }

    public LinkModel getLinkModel()
    {
        return linkModel;
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

        if (!(obj instanceof DssLinkTableCell))
            return false;

        DssLinkTableCell other = (DssLinkTableCell) obj;

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
        return getLinkUrl(linkModel, "sessionToken");
    }

    public String getHtmlString(String sessionId)
    {
        return URLMethodWithParameters.createEmbededLinkHtml(linkText,
                getLinkUrl(linkModel, sessionId), "center");
    }

    /**
     * Get a string containing HTML that shows a thumbnail image with a link to the larger image
     */
    public static String getLinkUrl(LinkModel linkModel, String sessionId)
    {
        // Add the sessionId
        linkModel.setSessionId(sessionId);

        URLMethodWithParameters urlMethod =
                new URLMethodWithParameters(linkModel.getSchemeAndDomain() + "/"
                        + linkModel.getPath());
        for (LinkModel.LinkParameter param : linkModel.getParameters())
        {
            urlMethod.addParameter(param.getName(), param.getValue());
        }

        return urlMethod.toString();
    }

}
