/*
 * Copyright 2013 ETH Zuerich, CISD
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.vocabulary.create.VocabularyTermCreation")
public class VocabularyTermCreation implements ICreation, IObjectCreation
{
    private static final long serialVersionUID = 1L;

    private IVocabularyId vocabularyId;

    private String code;

    private String label;

    private String description;

    private Boolean official = Boolean.TRUE;

    private IVocabularyTermId previousTermId;

    public IVocabularyId getVocabularyId()
    {
        return vocabularyId;
    }

    public void setVocabularyId(IVocabularyId vocabularyId)
    {
        this.vocabularyId = vocabularyId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean isOfficial()
    {
        return official;
    }

    public void setOfficial(Boolean official)
    {
        this.official = official;
    }

    public IVocabularyTermId getPreviousTermId()
    {
        return previousTermId;
    }

    public void setPreviousTermId(IVocabularyTermId previousTermId)
    {
        this.previousTermId = previousTermId;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("vocabularyId", vocabularyId).append("code", code).toString();
    }

}
