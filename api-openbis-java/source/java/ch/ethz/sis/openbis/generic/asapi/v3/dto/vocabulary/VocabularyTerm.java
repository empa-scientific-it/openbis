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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.vocabulary.VocabularyTerm")
public class VocabularyTerm implements Serializable, ICodeHolder, IDescriptionHolder, IModificationDateHolder, IPermIdHolder, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private VocabularyTermFetchOptions fetchOptions;

    @JsonProperty
    private VocabularyTermPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private String label;

    @JsonProperty
    private String description;

    @JsonProperty
    private Long ordinal;

    @JsonProperty
    private Boolean official;

    @JsonProperty
    private Vocabulary vocabulary;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date modificationDate;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public VocabularyTermFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(VocabularyTermFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public VocabularyTermPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(VocabularyTermPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getLabel()
    {
        return label;
    }

    // Method automatically generated with DtoGenerator
    public void setLabel(String label)
    {
        this.label = label;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Long getOrdinal()
    {
        return ordinal;
    }

    // Method automatically generated with DtoGenerator
    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isOfficial()
    {
        return official;
    }

    // Method automatically generated with DtoGenerator
    public void setOfficial(Boolean official)
    {
        this.official = official;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Vocabulary getVocabulary()
    {
        if (getFetchOptions() != null && getFetchOptions().hasVocabulary())
        {
            return vocabulary;
        }
        else
        {
            throw new NotFetchedException("Vocabulary has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setVocabulary(Vocabulary vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "VocabularyTerm " + code;
    }

}
