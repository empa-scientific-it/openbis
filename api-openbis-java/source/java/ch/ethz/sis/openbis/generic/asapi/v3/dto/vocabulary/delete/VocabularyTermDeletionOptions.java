/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.AbstractObjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.vocabulary.delete.VocabularyTermDeletionOptions")
public class VocabularyTermDeletionOptions extends AbstractObjectDeletionOptions<VocabularyTermDeletionOptions>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<VocabularyTermReplacement> replacements = new ArrayList<VocabularyTermReplacement>();

    @JsonIgnore
    public void replace(IVocabularyTermId replacedId, IVocabularyTermId replacementId)
    {
        replacements.add(new VocabularyTermReplacement(replacedId, replacementId));
    }

    @JsonIgnore
    public List<VocabularyTermReplacement> getReplacements()
    {
        return replacements;
    }

}
