package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

public class PropertyMatch implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String value;

    private List<Span> spans;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public List<Span> getSpans()
    {
        return spans;
    }

    public void setSpans(List<Span> spans)
    {
        this.spans = spans;
    }
}
