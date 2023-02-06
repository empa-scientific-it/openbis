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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.FieldUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.vocabulary.update.VocabularyTermUpdate")
public class VocabularyTermUpdate implements IUpdate, IObjectUpdate<IVocabularyTermId>
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IVocabularyTermId vocabularyTermId;

    @JsonProperty
    private FieldUpdateValue<String> label = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<String> description = new FieldUpdateValue<String>();

    @JsonProperty
    private FieldUpdateValue<IVocabularyTermId> previousTermId = new FieldUpdateValue<IVocabularyTermId>();

    @JsonProperty
    private FieldUpdateValue<Boolean> official = new FieldUpdateValue<Boolean>();

    @Override
    @JsonIgnore
    public IVocabularyTermId getObjectId()
    {
        return getVocabularyTermId();
    }

    @JsonIgnore
    public IVocabularyTermId getVocabularyTermId()
    {
        return vocabularyTermId;
    }

    @JsonIgnore
    public void setVocabularyTermId(IVocabularyTermId vocabularyTermId)
    {
        this.vocabularyTermId = vocabularyTermId;
    }

    @JsonIgnore
    public void setLabel(String label)
    {
        this.label.setValue(label);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getLabel()
    {
        return label;
    }

    @JsonIgnore
    public void setDescription(String description)
    {
        this.description.setValue(description);
    }

    @JsonIgnore
    public FieldUpdateValue<String> getDescription()
    {
        return description;
    }

    @JsonIgnore
    public void setPreviousTermId(IVocabularyTermId previousTermId)
    {
        this.previousTermId.setValue(previousTermId);
    }

    @JsonIgnore
    public FieldUpdateValue<IVocabularyTermId> getPreviousTermId()
    {
        return previousTermId;
    }

    @JsonIgnore
    public void setOfficial(Boolean official)
    {
        this.official.setValue(official);
    }

    @JsonIgnore
    public FieldUpdateValue<Boolean> isOfficial()
    {
        return official;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("vocabularyTermId", vocabularyTermId).toString();
    }

}
