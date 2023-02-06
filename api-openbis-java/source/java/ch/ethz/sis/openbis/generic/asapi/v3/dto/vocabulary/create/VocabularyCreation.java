/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.vocabulary.create.VocabularyCreation")
public class VocabularyCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String description;

    private boolean managedInternally;

    private boolean chosenFromList;

    private String urlTemplate;

    private List<VocabularyTermCreation> terms;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

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
    @Deprecated()
    public boolean isInternalNameSpace()
    {
        return isManagedInternally();
    }

    /**
     * @deprecated use {@link #setManagedInternally(boolean)}
     */
    @JsonIgnore
    @Deprecated()
    public void setInternalNameSpace(boolean internalNameSpace)
    {
        setManagedInternally(internalNameSpace);
    }

    public boolean isChosenFromList()
    {
        return chosenFromList;
    }

    public void setChosenFromList(boolean chosenFromList)
    {
        this.chosenFromList = chosenFromList;
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    public List<VocabularyTermCreation> getTerms()
    {
        return terms;
    }

    public void setTerms(List<VocabularyTermCreation> terms)
    {
        this.terms = terms;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("code", code).toString();
    }

}
