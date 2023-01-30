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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermReplacement;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteVocabularyTermExecutor
        extends AbstractDeleteEntityExecutor<Void, IVocabularyTermId, VocabularyTermPE, VocabularyTermDeletionOptions> implements
        IDeleteVocabularyTermExecutor
{
    @Autowired
    private IVocabularyTermAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapVocabularyTermByIdExecutor mapTermByIdExecutor;

    @Override
    protected Map<IVocabularyTermId, VocabularyTermPE> map(IOperationContext context, List<? extends IVocabularyTermId> entityIds,
            VocabularyTermDeletionOptions deletionOptions)
    {
        return mapTermByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IVocabularyTermId entityId, VocabularyTermPE entity)
    {
        authorizationExecutor.canDelete(context);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, VocabularyTermPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<VocabularyTermPE> terms, VocabularyTermDeletionOptions deletionOptions)
    {
        Map<VocabularyTermPE, VocabularyTermPE> replacementMap = getReplacementMap(context, deletionOptions);
        Map<VocabularyPE, List<VocabularyTerm>> termsToBeDeletedMap = new HashMap<VocabularyPE, List<VocabularyTerm>>();
        Map<VocabularyPE, List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement>> termsToBeReplacedMap =
                new HashMap<VocabularyPE, List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement>>();

        for (VocabularyTermPE term : terms)
        {
            if (replacementMap.get(term) == null)
            {
                List<VocabularyTerm> termsToBeDeleted = termsToBeDeletedMap.get(term.getVocabulary());
                if (termsToBeDeleted == null)
                {
                    termsToBeDeleted = new ArrayList<VocabularyTerm>();
                    termsToBeDeletedMap.put(term.getVocabulary(), termsToBeDeleted);
                }
                termsToBeDeleted.add(createDeleted(term.getCode()));
            } else
            {
                List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement> termsToBeReplaced =
                        termsToBeReplacedMap.get(term.getVocabulary());
                if (termsToBeReplaced == null)
                {
                    termsToBeReplaced = new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement>();
                    termsToBeReplacedMap.put(term.getVocabulary(), termsToBeReplaced);
                }
                termsToBeReplaced.add(createReplaced(term, replacementMap.get(term).getCode()));
            }
        }

        Set<VocabularyTermPE> unmatchedReplacements = new HashSet<VocabularyTermPE>(replacementMap.keySet());
        unmatchedReplacements.removeAll(terms);
        if (unmatchedReplacements.size() > 0)
        {
            throw new UserFailureException(
                    "The following terms where not chosen to be deleted but had replacements specified: " + unmatchedReplacements + ".");
        }

        Set<VocabularyPE> vocabularies = new HashSet<VocabularyPE>();
        vocabularies.addAll(termsToBeDeletedMap.keySet());
        vocabularies.addAll(termsToBeReplacedMap.keySet());

        for (VocabularyPE vocabulary : vocabularies)
        {
            List<VocabularyTerm> termsToBeDeleted = termsToBeDeletedMap.get(vocabulary);
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement> termsToBeReplaced =
                    termsToBeReplacedMap.get(vocabulary);

            if (termsToBeDeleted == null)
            {
                termsToBeDeleted = Collections.emptyList();
            }
            if (termsToBeReplaced == null)
            {
                termsToBeReplaced = Collections.emptyList();
            }

            IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(context.getSession());
            vocabularyBO.loadDataByTechId(new TechId(vocabulary.getId()));
            vocabularyBO.delete(termsToBeDeleted, termsToBeReplaced);
            vocabularyBO.save();
        }

        return null;
    }

    private Map<VocabularyTermPE, VocabularyTermPE> getReplacementMap(IOperationContext context, VocabularyTermDeletionOptions deletionOptions)
    {
        Collection<IVocabularyTermId> termIds = new HashSet<IVocabularyTermId>();
        for (VocabularyTermReplacement replacement : deletionOptions.getReplacements())
        {
            termIds.add(replacement.getReplacedId());
            termIds.add(replacement.getReplacementId());
        }
        Map<IVocabularyTermId, VocabularyTermPE> termMap = mapTermByIdExecutor.map(context, termIds);

        Map<VocabularyTermPE, VocabularyTermPE> replacementMap = new HashMap<VocabularyTermPE, VocabularyTermPE>();
        for (VocabularyTermReplacement replacement : deletionOptions.getReplacements())
        {
            VocabularyTermPE replacedPE = termMap.get(replacement.getReplacedId());
            VocabularyTermPE replacementPE = termMap.get(replacement.getReplacementId());

            if (replacedPE != null && replacementPE != null)
            {
                if (false == replacedPE.getVocabulary().equals(replacementPE.getVocabulary()))
                {
                    throw new UserFailureException("Replaced " + replacement.getReplacedId() + " and replacement " + replacement.getReplacementId()
                            + " terms cannot belong to different vocabularies.");
                } else
                {
                    replacementMap.put(replacedPE, replacementPE);
                }
            } else if (replacementPE == null)
            {
                throw new UserFailureException("Replacement term " + replacement.getReplacementId() + " does not exist.");
            }
        }

        return replacementMap;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement createReplaced(
            VocabularyTermPE term, String replacementCode)
    {
        VocabularyTerm replaced = new VocabularyTerm();
        replaced.setCode(term.getCode());
        replaced.setId(term.getId());

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement replacement =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement();
        replacement.setTerm(replaced);
        replacement.setReplacementCode(replacementCode);

        return replacement;
    }

    private VocabularyTerm createDeleted(String code)
    {
        VocabularyTerm deleted = new VocabularyTerm();
        deleted.setCode(code);
        return deleted;
    }

}
