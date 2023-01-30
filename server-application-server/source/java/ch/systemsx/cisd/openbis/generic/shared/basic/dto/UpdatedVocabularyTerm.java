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

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A vocabulary term to update.
 * 
 * @author Piotr Buczek
 */
@JsonObject("UpdatedVocabularyTerm")
public final class UpdatedVocabularyTerm extends VocabularyTerm
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private VocabularyTermBatchUpdateDetails batchUpdateDetails;

    public UpdatedVocabularyTerm(VocabularyTerm vocabularyTerm,
            VocabularyTermBatchUpdateDetails batchUpdateDetails)
    {
        super();
        setCode(vocabularyTerm.getCode());
        setDescription(vocabularyTerm.getDescription());
        setId(vocabularyTerm.getId());
        setLabel(vocabularyTerm.getLabel());
        setOrdinal(vocabularyTerm.getOrdinal());
        setOfficial(vocabularyTerm.isOfficial());
        setModificationDate(vocabularyTerm.getModificationDate());
        this.batchUpdateDetails = batchUpdateDetails;
    }

    public VocabularyTermBatchUpdateDetails getBatchUpdateDetails()
    {
        return batchUpdateDetails;
    }

    public void setBatchUpdateDetails(VocabularyTermBatchUpdateDetails batchUpdateDetails)
    {
        this.batchUpdateDetails = batchUpdateDetails;
    }
}
