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
