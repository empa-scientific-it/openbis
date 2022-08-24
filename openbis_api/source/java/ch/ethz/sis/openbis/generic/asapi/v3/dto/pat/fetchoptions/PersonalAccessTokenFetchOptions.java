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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions")
public class PersonalAccessTokenFetchOptions extends FetchOptions<PersonalAccessToken> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions owner;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private PersonalAccessTokenSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwner()
    {
        if (owner == null)
        {
            owner = new PersonFetchOptions();
        }
        return owner;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwnerUsing(PersonFetchOptions fetchOptions)
    {
        return owner = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasOwner()
    {
        return owner != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withModifierUsing(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasModifier()
    {
        return modifier != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public PersonalAccessTokenSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PersonalAccessTokenSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public PersonalAccessTokenSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("PersonalAccessToken", this);
        f.addFetchOption("Owner", owner);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Modifier", modifier);
        return f;
    }

}
