/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;

public enum InfoType
{

    ERROR("#f6cece", "#f5a9a9", Dict.INFO_TYPE_ERROR), INFO("#cef6ce", "#a9f5a9",
            Dict.INFO_TYPE_INFO), PROGRESS("#cef6ce", "#a9f5a9", Dict.INFO_TYPE_PROGRESS);

    private final String backgroundColor;

    private final String borderColor;

    private final String messageKey;

    private InfoType(final String backgroundColor, final String borderColor, final String messageKey)
    {

        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.messageKey = messageKey;
    }

    public String getBackgroundColor()
    {
        return backgroundColor;
    }

    public String getBorderColor()
    {
        return borderColor;
    }

    public String getMessageKey()
    {
        return messageKey;
    }

}
