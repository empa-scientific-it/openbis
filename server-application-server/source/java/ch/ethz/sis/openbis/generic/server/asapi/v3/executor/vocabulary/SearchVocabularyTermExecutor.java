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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchVocabularyTermExecutor extends AbstractSearchObjectManuallyExecutor<VocabularyTermSearchCriteria, VocabularyTermPE> implements
        ISearchVocabularyTermExecutor
{

    @Autowired
    private ISearchVocabularyExecutor searchVocabularyExecutor;

    @Autowired
    private IVocabularyTermAuthorizationExecutor authorizationExecutor;

    @Override
    public List<VocabularyTermPE> search(IOperationContext context, VocabularyTermSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<VocabularyTermPE> listAll()
    {
        return daoFactory.getVocabularyTermDAO().listAllEntities();
    }

    @Override
    protected Matcher<VocabularyTermPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return new PermIdMatcher();
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<VocabularyTermPE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<VocabularyTermPE>();
        } else if (criteria instanceof VocabularySearchCriteria)
        {
            return new VocabularyMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<VocabularyTermPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, VocabularyTermPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof VocabularyTermPermId)
            {
                VocabularyTermPermId permId = (VocabularyTermPermId) id;
                return object.getCode().equals(permId.getCode()) && object.getVocabulary().getCode().equals(permId.getVocabularyCode());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class PermIdMatcher extends StringFieldMatcher<VocabularyTermPE>
    {

        @Override
        protected String getFieldValue(VocabularyTermPE object)
        {
            return new VocabularyTermPermId(object.getCode(), object.getVocabulary().getCode()).toString();
        }

    }

    private class VocabularyMatcher extends Matcher<VocabularyTermPE>
    {

        @Override
        public List<VocabularyTermPE> getMatching(IOperationContext context, List<VocabularyTermPE> objects, ISearchCriteria criteria)
        {
            List<VocabularyPE> vocabularyList = searchVocabularyExecutor.search(context, (VocabularySearchCriteria) criteria);
            Set<VocabularyPE> vocabularySet = new HashSet<VocabularyPE>(vocabularyList);

            List<VocabularyTermPE> matches = new ArrayList<VocabularyTermPE>();

            for (VocabularyTermPE object : objects)
            {
                if (vocabularySet.contains(object.getVocabulary()))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

    }

}
