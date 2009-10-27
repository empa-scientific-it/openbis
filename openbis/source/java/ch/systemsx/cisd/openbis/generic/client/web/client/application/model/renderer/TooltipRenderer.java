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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * A helper used to render tooltips for entities chosen from combo-boxes.
 * 
 * @author Piotr Buczek
 */
public class TooltipRenderer
{
    public static final String renderAsTooltip(String code, String descriptionOrNull)
    {
        return renderAsTooltip(code, "description", descriptionOrNull);
    }

    public static final String renderAsTooltip(String code, String additionalLabel,
            String additionalValueOrNull)
    {
        assert code != null;
        final StringBuilder sb = new StringBuilder();
        sb.append("<b>" + code + "</b>");
        if (StringUtils.isEmpty(additionalValueOrNull) == false)
        {
            sb.append("<br><hr>" + additionalLabel + ": <i>" + additionalValueOrNull + "</i>");
        }
        return sb.toString();
    }

}
