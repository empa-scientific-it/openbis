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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.vocabulary.Vocabulary")
public class Vocabulary implements Serializable, ICodeHolder, IDescriptionHolder, IModificationDateHolder, IRegistrationDateHolder, IRegistratorHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private VocabularyFetchOptions fetchOptions;

    @JsonProperty
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date modificationDate;
    
    @JsonProperty
    private boolean managedInternally;
    
    @JsonProperty
    private boolean chosenFromList;
    
    @JsonProperty
    private String urlTemplate;
    
    @JsonProperty
    private List<VocabularyTerm> terms;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public VocabularyFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(VocabularyFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public VocabularyPermId getPermId()
    {
        return new VocabularyPermId(code);
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

    @JsonIgnore
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    public void setManagedInternally(boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    /**
     * @deprecated use {@link #isManagedInternally()}
     */
    @JsonIgnore
    @Deprecated
    public boolean isInternalNameSpace()
    {
        return isManagedInternally();
    }

    /**
     * @deprecated use {@link #setManagedInternally(boolean)}
     */
    @Deprecated
    public void setInternalNameSpace(boolean internalNameSpace)
    {
        setManagedInternally(internalNameSpace);
    }

    @JsonIgnore
    public boolean isChosenFromList()
    {
        return chosenFromList;
    }

    public void setChosenFromList(boolean chosenFromList)
    {
        this.chosenFromList = chosenFromList;
    }

    @JsonIgnore
    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    @JsonIgnore
    public List<VocabularyTerm> getTerms()
    {
        if (getFetchOptions() != null && getFetchOptions().hasTerms())
        {
            return terms;
        }
        throw new NotFetchedException("Terms have not been fetched.");
    }

    public void setTerms(List<VocabularyTerm> terms)
    {
        this.terms = terms;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Vocabulary " + code;
    }

}
