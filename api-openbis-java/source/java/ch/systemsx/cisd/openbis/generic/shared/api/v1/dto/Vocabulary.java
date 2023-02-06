/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * Immutable value object representing an openBIS vocabulary.
 * 
 * @author Kaloyan Enimanev
 */
@SuppressWarnings("unused")
@JsonObject("VocabularyGeneric")
public class Vocabulary implements Serializable
{

    private static final long serialVersionUID = 1L;

    public static class VocabularyInitializer
    {

        private Long id;

        private String code;

        private String description;

        private boolean managedInternally;

        private boolean chosenFromList;

        private String urlTemplate;

        private List<VocabularyTerm> terms;

        public void setId(Long id)
        {
            this.id = id;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public void setManagedInternally(boolean managedInternally)
        {
            this.managedInternally = managedInternally;
        }

        public void setChosenFromList(boolean chosenFromList)
        {
            this.chosenFromList = chosenFromList;
        }

        public void setUrlTemplate(String urlTemplate)
        {
            this.urlTemplate = urlTemplate;
        }

        public void setTerms(List<VocabularyTerm> terms)
        {
            this.terms = terms;
        }

    }

    private Long id;

    private String code;

    private String description;

    private boolean managedInternally;

    private boolean chosenFromList;

    private String urlTemplate;

    private List<VocabularyTerm> terms;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Vocabulary(VocabularyInitializer initializer)
    {
        InitializingChecks.checkValidLong(initializer.id, "Unspecified id.");
        this.id = initializer.id;

        InitializingChecks.checkValidString(initializer.code, "Unspecified code.");
        this.code = initializer.code;

        this.description = initializer.description;

        this.chosenFromList = initializer.chosenFromList;
        this.managedInternally = initializer.managedInternally;
        this.urlTemplate = initializer.urlTemplate;
        this.terms =
                (initializer.terms == null) ? Collections.<VocabularyTerm> emptyList()
                        : initializer.terms;

    }

    /**
     * Returns the vocabulary id.
     */
    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the vocabulary code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the vocabulary description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns <code>true</code> if the vocabulary is managed internally.
     */
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    /**
     * Returns <code>true</code> if the vocabulary is in the internal openBIS namespace.
     * @deprecated use {@link #isManagedInternally()}
     */
    public boolean isInternalNamespace()
    {
        return isManagedInternally();
    }

    /**
     * Return <code>true</code> if the vocabulary was chosen from a list.
     */
    public boolean isChosenFromList()
    {
        return chosenFromList;
    }

    /**
     * Returns a URL template (e.g a search query) that can display additional information for concrete vocabulary terms.
     */
    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    /**
     * Returns the vocabulary terms.
     */
    public List<VocabularyTerm> getTerms()
    {
        return Collections.unmodifiableList(terms);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Vocabulary == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        Vocabulary other = (Vocabulary) obj;
        builder.append(getId(), other.getId());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getCode());
        builder.append(getTerms());
        return builder.toString();
    }

    //
    // JSON-RPC
    //
    private Vocabulary()
    {

    }

    @JsonIgnore
    private void setId(Long id)
    {
        this.id = id;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setDescription(String description)
    {
        this.description = description;
    }

    private void setManagedInternally(boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    private void setInternalNamespace(boolean internalNamespace)
    {
        setManagedInternally(internalNamespace);
    }

    private void setChosenFromList(boolean chosenFromList)
    {
        this.chosenFromList = chosenFromList;
    }

    private void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    private void setTerms(List<VocabularyTerm> terms)
    {
        this.terms = terms;
    }

}
