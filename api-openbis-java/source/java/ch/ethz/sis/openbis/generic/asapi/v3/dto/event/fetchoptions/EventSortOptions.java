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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonObject("as.dto.event.fetchoptions.EventSortOptions")
public class EventSortOptions extends SortOptions<Event>
{
    @JsonIgnore
    public static final String ID = "event_id";

    @JsonIgnore
    public static final String IDENTIFIER = "event_identifier";

    @JsonIgnore
    public static final String REGISTRATION_DATE = "event_registration_date";

    public SortOrder id()
    {
        return getOrCreateSorting(ID);
    }

    public SortOrder getId()
    {
        return getSorting(ID);
    }

    public SortOrder identifier()
    {
        return getOrCreateSorting(IDENTIFIER);
    }

    public SortOrder getIdentifier()
    {
        return getSorting(IDENTIFIER);
    }

    public SortOrder registrationDate()
    {
        return getOrCreateSorting(REGISTRATION_DATE);
    }

    public SortOrder getRegistrationDate()
    {
        return getSorting(REGISTRATION_DATE);
    }

}
