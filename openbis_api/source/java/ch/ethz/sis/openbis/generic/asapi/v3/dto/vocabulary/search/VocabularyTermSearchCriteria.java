/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.vocabulary.search.VocabularyTermSearchCriteria")
public class VocabularyTermSearchCriteria extends AbstractObjectSearchCriteria<IVocabularyTermId>
{

    private static final long serialVersionUID = 1L;

    public VocabularyTermSearchCriteria()
    {
    }

    public PermIdSearchCriteria withPermId()
    {
        return with(new PermIdSearchCriteria());
    }

    public VocabularyTermCodeSearchCriteria withCode()
    {
        return with(new VocabularyTermCodeSearchCriteria());
    }

    public VocabularyCodeSearchCriteria withVocabularyCode()
    {
        return with(new VocabularyCodeSearchCriteria());
    }

    public VocabularyTermSearchCriteria withOrOperator()
    {
        return (VocabularyTermSearchCriteria) withOperator(SearchOperator.OR);
    }

    public VocabularyTermSearchCriteria withAndOperator()
    {
        return (VocabularyTermSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("VOCABULARY_TERM");
        return builder;
    }

}
