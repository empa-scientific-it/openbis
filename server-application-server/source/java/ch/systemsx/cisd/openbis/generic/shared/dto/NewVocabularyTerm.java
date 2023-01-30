/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Jakub Straszewski
 */
public class NewVocabularyTerm implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final String code;

    private final String description;

    private final String label;

    private final Long ordinal;

    public NewVocabularyTerm(String code, String description, String label, Long ordinal)
    {
        this.code = code;
        this.description = description;
        this.label = label;
        this.ordinal = ordinal;
    }

    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    public String getLabel()
    {
        return label;
    }

    public Long getOrdinal()
    {
        return ordinal;
    }

    @Override
    public String toString()
    {
        return "NewVocabularyTerm [code=" + code + ", description=" + description + ", label="
                + label + ", ordinal=" + ordinal + "]";
    }

}
