/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.session.fetchoptions.SessionInformationFetchOptions")
public class SessionInformationFetchOptions extends FetchOptions<SessionInformation> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions person;

    @JsonProperty
    private PersonFetchOptions creatorPerson;

    @JsonProperty
    private SessionInformationSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withPerson()
    {
        if (person == null)
        {
            person = new PersonFetchOptions();
        }
        return person;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withPersonUsing(PersonFetchOptions fetchOptions)
    {
        return person = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasPerson()
    {
        return person != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withCreatorPerson()
    {
        if (creatorPerson == null)
        {
            creatorPerson = new PersonFetchOptions();
        }
        return creatorPerson;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withCreatorPersonUsing(PersonFetchOptions fetchOptions)
    {
        return creatorPerson = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasCreatorPerson()
    {
        return creatorPerson != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SessionInformationSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new SessionInformationSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SessionInformationSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("SessionInformation", this);
        f.addFetchOption("Person", person);
        f.addFetchOption("CreatorPerson", creatorPerson);
        return f;
    }

}
