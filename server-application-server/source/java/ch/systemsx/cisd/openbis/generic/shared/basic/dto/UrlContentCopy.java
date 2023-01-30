package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicURLEncoder;

public class UrlContentCopy implements IContentCopy
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String url;

    private String externalCode;

    public UrlContentCopy(String code, String label, String url, String externalCode)
    {
        this.code = code;
        this.label = label;
        this.url = url;
        this.externalCode = externalCode;
    }

    public UrlContentCopy()
    {
    }

    @Override
    public String getLocation()
    {
        String labelString;
        if (label == null || label.length() == 0)
        {
            labelString = code;
        } else
        {
            labelString = code + " (" + label + ")";
        }

        if (url != null && externalCode != null)
        {
            url = url.replaceAll(BasicConstant.EXTERNAL_DMS_URL_TEMPLATE_CODE_PATTERN, BasicURLEncoder.encode(externalCode));
        }
        return "External DMS: " + labelString + "</br>Link: <a class=\"gwt-Anchor\" href=\"" + url 
                + "\" target=\"_blank\" \">" + url + "</a><br>";
    }

    @Override
    public String getExternalDMSCode()
    {
        return this.code;
    }

    @Override
    public String getExternalDMSLabel()
    {
        return this.label;
    }

    @Override
    public String getExternalDMSAddress()
    {
        return this.url;
    }

    @Override
    public String getPath()
    {
        return null;
    }

    @Override
    public String getCommitHash()
    {
        return null;
    }

    @Override
    public String getRespitoryId()
    {
        return null;
    }

    @Override
    public String getExternalCode()
    {
        return this.externalCode;
    }

}
