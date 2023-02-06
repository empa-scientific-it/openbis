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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.history;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.history.HistoryEntry")
public class HistoryEntry implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private HistoryEntryFetchOptions fetchOptions;

    @JsonProperty
    private Date validFrom;

    @JsonProperty
    private Date validTo;

    @JsonProperty
    private Person author;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public HistoryEntryFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(HistoryEntryFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getValidFrom()
    {
        return validFrom;
    }

    // Method automatically generated with DtoGenerator
    public void setValidFrom(Date validFrom)
    {
        this.validFrom = validFrom;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Date getValidTo()
    {
        return validTo;
    }

    // Method automatically generated with DtoGenerator
    public void setValidTo(Date validTo)
    {
        this.validTo = validTo;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Person getAuthor()
    {
        if (getFetchOptions() != null && getFetchOptions().hasAuthor())
        {
            return author;
        }
        else
        {
            throw new NotFetchedException("Author has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setAuthor(Person author)
    {
        this.author = author;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "HistoryEntry from: " + validFrom + ", to: " + validTo;
    }

}
