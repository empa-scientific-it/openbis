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

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author pkupczyk
 */
public class ListVocabularyByPermId extends AbstractListObjectById<VocabularyPermId, VocabularyPE>
{

    private IVocabularyDAO vocabularyDAO;

    public ListVocabularyByPermId(IVocabularyDAO vocabularyDAO)
    {
        this.vocabularyDAO = vocabularyDAO;
    }

    @Override
    public Class<VocabularyPermId> getIdClass()
    {
        return VocabularyPermId.class;
    }

    @Override
    public VocabularyPermId createId(VocabularyPE vocabulary)
    {
        return new VocabularyPermId(vocabulary.getCode());
    }

    @Override
    public List<VocabularyPE> listByIds(IOperationContext context, List<VocabularyPermId> ids)
    {
        List<VocabularyPE> vocabularies = new LinkedList<VocabularyPE>();

        for (VocabularyPermId id : ids)
        {
            VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode(id.getPermId());
            if (vocabulary != null)
            {
                vocabularies.add(vocabulary);
            }
        }

        return vocabularies;
    }
}
