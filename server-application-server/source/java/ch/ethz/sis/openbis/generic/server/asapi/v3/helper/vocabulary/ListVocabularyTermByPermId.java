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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.vocabulary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
public class ListVocabularyTermByPermId extends AbstractListObjectById<VocabularyTermPermId, VocabularyTermPE>
{

    private IVocabularyDAO vocabularyDAO;

    public ListVocabularyTermByPermId(IVocabularyDAO vocabularyDAO)
    {
        this.vocabularyDAO = vocabularyDAO;
    }

    @Override
    public Class<VocabularyTermPermId> getIdClass()
    {
        return VocabularyTermPermId.class;
    }

    @Override
    public VocabularyTermPermId createId(VocabularyTermPE term)
    {
        return new VocabularyTermPermId(term.getCode(), term.getVocabulary().getCode());
    }

    @Override
    public List<VocabularyTermPE> listByIds(IOperationContext context, List<VocabularyTermPermId> ids)
    {
        Set<String> loadedVocabularies = new HashSet<String>();
        Map<String, VocabularyTermPE> termsMap = new HashMap<String, VocabularyTermPE>();

        for (VocabularyTermPermId id : ids)
        {
            if (false == loadedVocabularies.contains(id.getVocabularyCode()))
            {
                VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode(id.getVocabularyCode());

                if (vocabulary != null)
                {
                    for (VocabularyTermPE term : vocabulary.getTerms())
                    {
                        termsMap.put(term.getVocabulary().getCode() + " " + term.getCode(), term);
                    }
                }

                loadedVocabularies.add(id.getVocabularyCode());
            }
        }

        List<VocabularyTermPE> terms = new LinkedList<VocabularyTermPE>();

        for (VocabularyTermPermId id : ids)
        {
            VocabularyTermPE term = termsMap.get(id.getVocabularyCode() + " " + id.getCode());
            if (term != null)
            {
                terms.add(term);
            }
        }

        return terms;
    }
}
