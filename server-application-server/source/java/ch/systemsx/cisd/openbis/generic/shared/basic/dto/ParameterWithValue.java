package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Stores parameter name and the value.
 * 
 * @author Izabela Adamczyk
 */
public class ParameterWithValue implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String parameter;

    private String value;

    public ParameterWithValue()
    {
    }

    public ParameterWithValue(String parameter, String value)
    {
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter()
    {
        return parameter;
    }

    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
