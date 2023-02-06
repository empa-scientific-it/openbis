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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions")
public class VocabularyTermFetchOptions extends FetchOptions<VocabularyTerm> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private VocabularyFetchOptions vocabulary;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private VocabularyTermSortOptions sort;

    // Method automatically generated with DtoGenerator
    public VocabularyFetchOptions withVocabulary()
    {
        if (vocabulary == null)
        {
            vocabulary = new VocabularyFetchOptions();
        }
        return vocabulary;
    }

    // Method automatically generated with DtoGenerator
    public VocabularyFetchOptions withVocabularyUsing(VocabularyFetchOptions fetchOptions)
    {
        return vocabulary = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasVocabulary()
    {
        return vocabulary != null;
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
    @Override
    public VocabularyTermSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new VocabularyTermSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public VocabularyTermSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("VocabularyTerm", this);
        f.addFetchOption("Vocabulary", vocabulary);
        f.addFetchOption("Registrator", registrator);
        return f;
    }

}
