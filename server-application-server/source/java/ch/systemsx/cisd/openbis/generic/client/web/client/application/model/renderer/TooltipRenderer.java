/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * A helper used to render tooltips for entities chosen from combo-boxes.
 * 
 * @author Piotr Buczek
 */
public class TooltipRenderer
{

    private static final String NEW_LINE = "\n";

    private static final String END_B = "</b>";

    private static final String START_B = "<b>";

    private static final String BR = "<br/>";

    public static final String renderAsTooltip(String code, String descriptionOrNull)
    {
        assert code != null;
        final StringBuilder sb = new StringBuilder();
        sb.append(START_B + code + END_B);
        if (StringUtils.isBlank(descriptionOrNull) == false)
        {
            sb.append(BR + descriptionOrNull.replace(NEW_LINE, BR));
        }
        return sb.toString();
    }

}
