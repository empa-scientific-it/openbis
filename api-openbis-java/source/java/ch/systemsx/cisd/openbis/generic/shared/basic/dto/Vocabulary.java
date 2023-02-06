/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.reflection.CollectionMapping;
import ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil;

/**
 * Controlled vocabulary.
 * 
 * @author Izabela Adamczyk
 */
@SuppressWarnings("unused")
@JsonObject("VocabularyBasic")
public class Vocabulary extends CodeWithRegistration<Vocabulary> implements IVocabularyUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    private String description;

    private boolean managedInternally;

    private boolean chosenFromList;

    private String urlTemplate;

    private List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();

    private Date modificationDate;

    public Vocabulary()
    {
    }

    @Override
    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    @JsonIgnore
    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(final String description)
    {
        this.description = description;
    }

    public final boolean isManagedInternally()
    {
        return managedInternally;
    }

    public final void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public final boolean isInternalNamespace()
    {
        return isManagedInternally();
    }

    public final void setInternalNamespace(final boolean internalNamespace)
    {
        setManagedInternally(internalNamespace);
    }

    @Override
    public boolean isChosenFromList()
    {
        return chosenFromList;
    }

    public void setChosenFromList(boolean chosenFromList)
    {
        this.chosenFromList = chosenFromList;
    }

    @Override
    public String getURLTemplate()
    {
        return urlTemplate;
    }

    public void setURLTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    public final List<VocabularyTerm> getTerms()
    {
        return terms;
    }

    @CollectionMapping(collectionClass = ArrayList.class, elementClass = VocabularyTerm.class)
    public final void setTerms(final List<VocabularyTerm> terms)
    {
        this.terms = terms;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof Vocabulary))
        {
            return false;
        }
        Vocabulary other = (Vocabulary) obj;
        if (getCode() == null)
        {
            if (other.getCode() != null)
            {
                return false;
            }
        } else if (!getCode().equals(other.getCode()))
        {
            return false;
        }
        if (id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        } else if (!id.equals(other.id))
        {
            return false;
        }
        return true;
    }

    //
    // JSON-RPC
    //

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

}
