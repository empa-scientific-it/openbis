package ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id.IEventId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.event.search.EventSearchCriteria")
public class EventSearchCriteria extends AbstractObjectSearchCriteria<IEventId>
{

    private static final long serialVersionUID = 1L;

    public EventTypeSearchCriteria withEventType()
    {
        return with(new EventTypeSearchCriteria());
    }

    public EventEntityTypeSearchCriteria withEntityType()
    {
        return with(new EventEntityTypeSearchCriteria());
    }

    public EventEntitySpaceSearchCriteria withEntitySpace()
    {
        return with(new EventEntitySpaceSearchCriteria());
    }

    public EventEntitySpaceIdSearchCriteria withEntitySpaceId()
    {
        return with(new EventEntitySpaceIdSearchCriteria());
    }

    public EventEntityProjectSearchCriteria withEntityProject()
    {
        return with(new EventEntityProjectSearchCriteria());
    }

    public EventEntityProjectIdSearchCriteria withEntityProjectId()
    {
        return with(new EventEntityProjectIdSearchCriteria());
    }

    public EventEntityRegistratorSearchCriteria withEntityRegistrator()
    {
        return with(new EventEntityRegistratorSearchCriteria());
    }

    public EventEntityRegistrationDateSearchCriteria withEntityRegistrationDate()
    {
        return with(new EventEntityRegistrationDateSearchCriteria());
    }

    public RegistratorSearchCriteria withRegistrator()
    {
        return with(new RegistratorSearchCriteria());
    }

    public RegistrationDateSearchCriteria withRegistrationDate()
    {
        return with(new RegistrationDateSearchCriteria());
    }

    public EventSearchCriteria withOrOperator()
    {
        return (EventSearchCriteria) withOperator(SearchOperator.OR);
    }

    public EventSearchCriteria withAndOperator()
    {
        return (EventSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("EVENT");
        return builder;
    }

}
