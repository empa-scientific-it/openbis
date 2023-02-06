package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.sample.search.ListableSampleTypeSearchCriteria")
public class ListableSampleTypeSearchCriteria extends AbstractSearchCriteria
{
    private static final long serialVersionUID = 1L;

    private boolean listable;

    public ListableSampleTypeSearchCriteria()
    {

    }

    public void thatEquals(boolean value)
    {
        setListable(value);
    }

    public void setListable(boolean value)
    {
        this.listable = value;
    }

    public boolean isListable()
    {
        return listable;
    }

    @Override
    public String toString()
    {
        return "listable: " + isListable();
    }
}
