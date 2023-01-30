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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.Arrays;

import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.HtmlMessageElement;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IInfoHandler;

/**
 * @author Christian Ribeaud
 */
abstract public class FormPanelListener implements Listener<FormEvent>
{

    private final IInfoHandler infoBox;

    public FormPanelListener(final IInfoHandler infoBox)
    {
        this.infoBox = infoBox;
    }

    private final void extractAndDisplay(final String msg)
    {
        final Document document = XMLParser.parse(msg);
        final Node message = document.getFirstChild();
        final Node typeNode = message.getAttributes().getNamedItem("type");
        final String messageText = message.getFirstChild().getNodeValue();
        final String type = typeNode.getNodeValue();
        if ("info".equals(type))
        {
            infoBox.displayInfo(Arrays.asList(new HtmlMessageElement(messageText)));
        } else
        {
            infoBox.displayError(messageText);
        }
    }

    @Override
    public final void handleEvent(final FormEvent be)
    {
        final String msg = be.getResultHtml();
        // Was not successful
        if (StringUtils.isBlank(msg) == false)
        {
            if (msg.startsWith("<message"))
            {
                if (msg.indexOf("<![CDATA[") > -1 && XMLParser.supportsCDATASection() == false)
                {
                    infoBox.displayError(msg.replaceAll("<", "&lt;"));
                } else
                {
                    extractAndDisplay(msg);
                }
            } else
            {
                infoBox.displayError(msg);
            }
            setUploadEnabled();
        } else
        {
            onSuccessfullUpload();
        }
    }

    abstract protected void setUploadEnabled();

    abstract protected void onSuccessfullUpload();
}
