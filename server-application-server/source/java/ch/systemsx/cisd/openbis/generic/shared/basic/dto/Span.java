package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

public class Span implements Serializable
{

    private static final long serialVersionUID = 1L;

    private int start;

    private int end;

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getEnd()
    {
        return end;
    }

    public void setEnd(int end)
    {
        this.end = end;
    }
}
