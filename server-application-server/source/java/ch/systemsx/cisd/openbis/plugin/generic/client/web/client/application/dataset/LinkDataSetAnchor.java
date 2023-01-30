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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import com.google.gwt.user.client.ui.Anchor;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSetUrl;

/**
 * @author pkupczyk
 */
public class LinkDataSetAnchor extends Anchor
{

    private LinkDataSetAnchor(String text, String url)
    {
        super(text, url);
        setTarget("_blank");
    }

    public static final LinkDataSetAnchor tryCreate(LinkDataSet dataset)
    {
        String url = new UnescapingLinkDataSetUrl(dataset).toString();

        if (url != null)
        {
            return new LinkDataSetAnchor(url, url);
        } else
        {
            return null;
        }
    }

    private static class UnescapingLinkDataSetUrl extends LinkDataSetUrl
    {
        public UnescapingLinkDataSetUrl(LinkDataSet dataset)
        {
            super(dataset);
        }

        @Override
        protected String maybeUnescape(String str)
        {
            return StringEscapeUtils.unescapeHtml(str);
        }
    }

}
