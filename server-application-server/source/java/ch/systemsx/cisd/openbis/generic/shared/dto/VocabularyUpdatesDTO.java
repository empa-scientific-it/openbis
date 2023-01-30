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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Jakub Straszewski
 */
public class VocabularyUpdatesDTO implements IIdHolder, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final Long id;

    private final String code;

    private final String description;

    private final boolean isManagedInternally;

    private final boolean isChosenFromList;

    private final String urlTemplate;

    private final List<NewVocabularyTerm> newTerms;

    public VocabularyUpdatesDTO(Long id, String code, String description,
            boolean isManagedInternally, boolean isChosenFromList,
            String urlTemplate, List<NewVocabularyTerm> newTerms)
    {
        this.id = id;
        this.code = code;
        this.description = description;
        this.isManagedInternally = isManagedInternally;
        this.isChosenFromList = isChosenFromList;
        this.urlTemplate = urlTemplate;
        this.newTerms = newTerms;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isManagedInternally()
    {
        return isManagedInternally;
    }

    public boolean isChosenFromList()
    {
        return isChosenFromList;
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public List<NewVocabularyTerm> getNewTerms()
    {
        return newTerms;
    }

    @Override
    public String toString()
    {
        return "VocabularyUpdatesDTO [id=" + id + ", code=" + code + ", description=" + description
                + ", isManagedInternally=" + isManagedInternally
                + ", isChosenFromList=" + isChosenFromList + ", urlTemplate="
                + urlTemplate + ", newTerms=" + newTerms + "]";
    }

}
