package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.pat.search.PersonalAccessTokenSessionNameSearchCriteria")
public class PersonalAccessTokenSessionNameSearchCriteria extends StringFieldSearchCriteria
{

    private static final long serialVersionUID = 1L;

    public PersonalAccessTokenSessionNameSearchCriteria()
    {
        super("sessionName", SearchFieldType.ATTRIBUTE);
    }

}
