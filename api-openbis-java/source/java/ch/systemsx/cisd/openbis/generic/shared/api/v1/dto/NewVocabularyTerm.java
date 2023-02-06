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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * A value object representing a new vocabulary term to be created by the openBIS backend.
 * 
 * @author Kaloyan Enimanev
 */
@SuppressWarnings("unused")
@JsonObject("NewVocabularyTerm")
public class NewVocabularyTerm implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String description;

    private Long previousTermOrdinal;

    /**
     * Return the term's code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Set the term's code.
     */
    public void setCode(String code)
    {
        this.code = code;
    }

    /**
     * Return the term's label.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Set the term's label.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Return the term's description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the term's description.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Return the position of predecessor term in the vocabulary.
     */
    @JsonIgnore
    public Long getPreviousTermOrdinal()
    {
        return previousTermOrdinal;
    }

    /**
     * Set the position of predecessor term in the vocabulary.
     */
    @JsonIgnore
    public void setPreviousTermOrdinal(Long previousTermOrdinal)
    {
        this.previousTermOrdinal = previousTermOrdinal;
    }

    @Override
    public String toString()
    {
        return "NewVocabularyTerm [code=" + code + ", label=" + label + ", description="
                + description + ", previousTermOrdinal=" + previousTermOrdinal + "]";
    }

    //
    // JSON-RPC
    //

    @JsonProperty("previousTermOrdinal")
    private String getPreviousTermOrdinalAsString()
    {
        return JsonPropertyUtil.toStringOrNull(previousTermOrdinal);
    }

    private void setPreviousTermOrdinalAsString(String previousTermOrdinal)
    {
        this.previousTermOrdinal = JsonPropertyUtil.toLongOrNull(previousTermOrdinal);
    }

}
