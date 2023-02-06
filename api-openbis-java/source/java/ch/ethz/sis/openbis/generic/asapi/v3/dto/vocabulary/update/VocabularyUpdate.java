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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.vocabulary.update.VocabularyUpdate")
public class VocabularyUpdate implements IUpdate, IObjectUpdate<IVocabularyId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IVocabularyId vocabularyId;

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<Boolean> chosenFromList = new FieldUpdateValue<Boolean>();

    @JsonProperty
    private FieldUpdateValue<String> urlTemplate = new FieldUpdateValue<String>();

    @Override
    @JsonIgnore
    public IVocabularyId getObjectId()
    {
        return getVocabularyId();
    }

    @JsonIgnore
    public IVocabularyId getVocabularyId()
    {
        return vocabularyId;
    }

    @JsonIgnore
    public void setVocabularyId(IVocabularyId vocabularyId)
    {
        this.vocabularyId = vocabularyId;
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> getChosenFromList()
    {
        return chosenFromList;
    }

    @JsonIgnore
    public void setChosenFromList(Boolean chosenFromList)
    {
        this.chosenFromList.setValue(chosenFromList);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getUrlTemplate()
    {
        return urlTemplate;
    }

    @JsonIgnore
    public void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate.setValue(urlTemplate);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("vocabularyId", vocabularyId).toString();
    }

}
