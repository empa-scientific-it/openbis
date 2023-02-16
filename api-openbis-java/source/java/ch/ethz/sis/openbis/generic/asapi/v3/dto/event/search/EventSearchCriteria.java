/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public EventIdentifierSearchCriteria withIdentifier()
    {
        return with(new EventIdentifierSearchCriteria());
    }

    public EventReasonSearchCriteria withReason()
    {
        return with(new EventReasonSearchCriteria());
    }

    public EventDescriptionSearchCriteria withDescription()
    {
        return with(new EventDescriptionSearchCriteria());
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
