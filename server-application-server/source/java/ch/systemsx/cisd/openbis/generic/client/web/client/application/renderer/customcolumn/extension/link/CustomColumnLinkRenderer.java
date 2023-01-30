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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.extension.link;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.core.CustomColumnJSONClientData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.core.CustomColumnMethodRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.customcolumn.extension.link.CustomColumnLinkParam;

/**
 * @author pkupczyk
 */
public class CustomColumnLinkRenderer extends CustomColumnMethodRenderer
{

    public CustomColumnLinkRenderer(CustomColumnJSONClientData jsonData)
    {
        super(jsonData);
    }

    public Object render()
    {
        String linkText = getData().getStringParam(CustomColumnLinkParam.LINK_TEXT.name());
        String linkUrl = getData().getStringParam(CustomColumnLinkParam.LINK_URL.name());
        return LinkRenderer.renderAsLinkWithAnchor(StringEscapeUtils.escapeHtml(linkText), linkUrl,
                true);
    }
}
