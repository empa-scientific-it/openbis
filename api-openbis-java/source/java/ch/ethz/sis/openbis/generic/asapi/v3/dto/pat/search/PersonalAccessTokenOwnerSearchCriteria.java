package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.pat.search.PersonalAccessTokenOwnerSearchCriteria")
public class PersonalAccessTokenOwnerSearchCriteria extends PersonSearchCriteria
{

    private static final long serialVersionUID = 1L;

    public PersonalAccessTokenOwnerSearchCriteria()
    {
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("OWNER");
        return builder;
    }

}
