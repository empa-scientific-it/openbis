package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.pat.search.PersonalAccessTokenSearchCriteria")
public class PersonalAccessTokenSearchCriteria extends AbstractObjectSearchCriteria<IPersonalAccessTokenId>
{

    private static final long serialVersionUID = 1L;

    public PersonalAccessTokenSearchCriteria()
    {
    }

    public PersonalAccessTokenSearchCriteria withOrOperator()
    {
        return (PersonalAccessTokenSearchCriteria) withOperator(SearchOperator.OR);
    }

    public PersonalAccessTokenSearchCriteria withAndOperator()
    {
        return (PersonalAccessTokenSearchCriteria) withOperator(SearchOperator.AND);
    }

    public PersonalAccessTokenOwnerSearchCriteria withOwner()
    {
        return with(new PersonalAccessTokenOwnerSearchCriteria());
    }

    public PersonalAccessTokenSessionNameSearchCriteria withSessionName()
    {
        return with(new PersonalAccessTokenSessionNameSearchCriteria());
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PERSONAL_ACCESS_TOKEN");
        return builder;
    }

}
