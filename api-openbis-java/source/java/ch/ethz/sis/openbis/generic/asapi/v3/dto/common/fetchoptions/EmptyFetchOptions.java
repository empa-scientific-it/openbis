package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.common.fetchoptions.EmptyFetchOptions")
public class EmptyFetchOptions extends FetchOptions<Void>
{

    private static final long serialVersionUID = 1L;

    @Override
    public SortOptions<Void> sortBy()
    {
        return null;
    }

    @Override
    public SortOptions<Void> getSortBy()
    {
        return null;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        return new FetchOptionsToStringBuilder("Empty", this);
    }

}
