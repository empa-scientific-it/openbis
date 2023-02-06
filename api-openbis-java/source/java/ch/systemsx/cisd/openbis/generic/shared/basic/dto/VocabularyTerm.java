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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil;

/**
 * A vocabulary term.
 * 
 * @author Izabela Adamczyk
 */
@SuppressWarnings("unused")
@JsonObject("VocabularyTermBasic")
public class VocabularyTerm extends CodeWithRegistrationAndModificationDate<VocabularyTerm> implements
        IVocabularyTermUpdates
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String CODE = "code";

    public static final String LABEL = "label";

    public static final String DESCRIPTION = "description";

    private Long id;

    private String description;

    private String label;

    private String url;

    private Long ordinal;

    private Boolean isOfficial = true; // official by default

    private Date modificationDate;

    public VocabularyTerm()
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
    @BeanProperty(label = CODE, optional = false)
    public void setCode(String code)
    {
        super.setCode(code);
    }

    @BeanProperty(label = LABEL, optional = true)
    public void setLabel(String label)
    {
        this.label = label;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @BeanProperty(label = DESCRIPTION, optional = true)
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    @JsonIgnore
    public Date getModificationDate()
    {
        return modificationDate;
    }

    @Override
    @JsonIgnore
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public String toString()
    {
        String code = getCode();
        return label == null ? code : (code == null ? label : label + " [" + code + "]");
    }

    @JsonIgnore
    public String getCodeOrLabel()
    {
        String code = getCode();
        return label == null ? code : label;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    @JsonIgnore
    public Long getOrdinal()
    {
        return ordinal;
    }

    @JsonIgnore
    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    public Boolean isOfficial()
    {
        return isOfficial;
    }

    public void setOfficial(Boolean isOfficial)
    {
        this.isOfficial = isOfficial;
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

    @JsonProperty("ordinal")
    private String getOrdinalAsString()
    {
        return JsonPropertyUtil.toStringOrNull(ordinal);
    }

    private void setOrdinalAsString(String ordinal)
    {
        this.ordinal = JsonPropertyUtil.toLongOrNull(ordinal);
    }

}
