/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchVocabularyExecutor extends AbstractSearchObjectManuallyExecutor<VocabularySearchCriteria, VocabularyPE> implements
        ISearchVocabularyExecutor
{
    @Autowired
    private IVocabularyAuthorizationExecutor authorizationExecutor;

    @Override
    public List<VocabularyPE> search(IOperationContext context, VocabularySearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<VocabularyPE> listAll()
    {
        return daoFactory.getVocabularyDAO().listAllEntities();
    }

    @Override
    protected Matcher<VocabularyPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof CodeSearchCriteria || criteria instanceof PermIdSearchCriteria)
        {
            return new CodeMatcher<VocabularyPE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<VocabularyPE>();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<VocabularyPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, VocabularyPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof VocabularyPermId)
            {
                VocabularyPermId permId = (VocabularyPermId) id;
                return object.getCode().equals(permId.getPermId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }
}
