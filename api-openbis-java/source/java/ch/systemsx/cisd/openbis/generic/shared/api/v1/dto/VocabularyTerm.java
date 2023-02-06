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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * @since {@link IGeneralInformationService} version 1.13
 * @author Kaloyan Enimanev
 */
@SuppressWarnings("unused")
@JsonObject("VocabularyTermGeneric")
public class VocabularyTerm implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String description;

    private Long ordinal;

    private Boolean isOfficial;

    private EntityRegistrationDetails registrationDetails;

    public VocabularyTerm(String code, String label, String description, Long ordinal, Boolean isOfficial,
            EntityRegistrationDetails registrationDetails)
    {
        this.code = code;
        this.label = label;
        this.description = description;
        this.ordinal = ordinal;
        this.isOfficial = isOfficial == null ? Boolean.TRUE : isOfficial;
        this.registrationDetails = registrationDetails;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    @JsonIgnore
    public Long getOrdinal()
    {
        return ordinal;
    }

    public Boolean isOfficial()
    {
        return isOfficial;
    }

    /**
     * Return the vocabulary term registration details.
     * 
     * @since 1.11
     */
    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof VocabularyTerm == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        VocabularyTerm other = (VocabularyTerm) obj;
        builder.append(getCode(), other.getCode());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getCode());
        builder.append(getLabel());
        return builder.toString();
    }

    //
    // JSON-RPC
    //
    protected VocabularyTerm()
    {
    }

    private void setOfficial(Boolean isOfficial)
    {
        this.isOfficial = isOfficial;
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setLabel(String label)
    {
        this.label = label;
    }

    @JsonIgnore
    private void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    @JsonProperty("ordinal")
    private String getOrdinalAsString()
    {
        return JsonPropertyUtil.toStringOrNull(ordinal);
    }

    private void setOrdinalAsString(String ordinal)
    {
        this.ordinal = JsonPropertyUtil.toLongOrNull(ordinal);
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

}
