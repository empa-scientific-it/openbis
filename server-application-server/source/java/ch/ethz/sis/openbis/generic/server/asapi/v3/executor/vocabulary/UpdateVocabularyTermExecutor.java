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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyTermBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class UpdateVocabularyTermExecutor implements IUpdateVocabularyTermExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    private ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IMapVocabularyTermByIdExecutor mapVocabularyTermByIdExecutor;

    @Autowired
    private IVocabularyTermAuthorizationExecutor authorizationExecutor;

    @Override
    public List<VocabularyTermPermId> update(IOperationContext context, List<VocabularyTermUpdate> updates)
    {
        authorizationExecutor.canUpdate(context);

        checkData(context, updates);

        final Map<IVocabularyTermId, VocabularyTermPE> terms = getTermsMap(context, updates);
        final Map<IVocabularyTermId, VocabularyTermPE> previousTerms = getPreviousTermsMap(context, updates);

        IVocabularyTermBO termBO = businessObjectFactory.createVocabularyTermBO(context.getSession());
        List<VocabularyTermPermId> permIds = new ArrayList<VocabularyTermPermId>();

        for (final VocabularyTermUpdate update : updates)
        {
            final VocabularyTermPE termPE = terms.get(update.getVocabularyTermId());

            permIds.add(new VocabularyTermPermId(termPE.getCode(), termPE.getVocabulary().getCode()));

            if (update.getDescription().isModified() || update.getLabel().isModified() || update.getPreviousTermId().isModified())
            {
                termBO.update(new IVocabularyTermUpdates()
                    {
                        @Override
                        public Long getId()
                        {
                            return termPE.getId();
                        }

                        @Override
                        public String getCode()
                        {
                            return termPE.getCode();
                        }

                        @Override
                        public String getLabel()
                        {
                            return update.getLabel().isModified() ? update.getLabel().getValue() : termPE.getLabel();
                        }

                        @Override
                        public String getDescription()
                        {
                            return update.getDescription().isModified() ? update.getDescription().getValue() : termPE.getDescription();
                        }

                        @Override
                        public Long getOrdinal()
                        {
                            if (update.getPreviousTermId().isModified())
                            {
                                if (update.getPreviousTermId().getValue() == null)
                                {
                                    Long minOrdinal = termPE.getOrdinal();

                                    for (VocabularyTermPE otherTermPE : termPE.getVocabulary().getTerms())
                                    {
                                        if (minOrdinal > otherTermPE.getOrdinal())
                                        {
                                            minOrdinal = otherTermPE.getOrdinal();
                                        }
                                    }

                                    return minOrdinal;
                                } else
                                {
                                    VocabularyTermPE previousTermPE = previousTerms.get(update.getPreviousTermId().getValue());

                                    if (false == previousTermPE.getVocabulary().equals(termPE.getVocabulary()))
                                    {
                                        throw new UserFailureException("Position of term " + update.getVocabularyTermId()
                                                + " could not be found as the specified previous term " + update.getPreviousTermId().getValue()
                                                + " is in a different vocabulary (" + previousTermPE.getVocabulary().getCode() + ").");
                                    }

                                    return previousTermPE.getOrdinal() + 1;
                                }
                            } else
                            {
                                return termPE.getOrdinal();
                            }
                        }

                        @Override
                        public Date getModificationDate()
                        {
                            return termPE.getModificationDate();
                        }

                    });
            }

            if (update.isOfficial().isModified())
            {
                if (termPE.isOfficial() && Boolean.FALSE.equals(update.isOfficial().getValue()))
                {
                    throw new UserFailureException(
                            "Offical vocabulary term " + update.getVocabularyTermId() + " cannot be updated to be unofficial.");
                }
                VocabularyTerm term = new VocabularyTerm();
                term.setId(termPE.getId());
                termBO.makeOfficial(Arrays.asList(term));
            }
        }

        return permIds;
    }

    private void checkData(IOperationContext context, Collection<VocabularyTermUpdate> updates)
    {
        for (VocabularyTermUpdate update : updates)
        {
            if (update.getVocabularyTermId() == null)
            {
                throw new UserFailureException("Vocabulary term id cannot be null");
            }
        }
    }

    private Map<IVocabularyTermId, VocabularyTermPE> getTermsMap(IOperationContext context, List<VocabularyTermUpdate> updates)
    {
        Collection<IVocabularyTermId> ids = new HashSet<IVocabularyTermId>();

        for (VocabularyTermUpdate update : updates)
        {
            ids.add(update.getVocabularyTermId());
        }

        Map<IVocabularyTermId, VocabularyTermPE> termsMap = mapVocabularyTermByIdExecutor.map(context, ids);

        for (IVocabularyTermId id : ids)
        {
            if (termsMap.get(id) == null)
            {
                throw new ObjectNotFoundException(id);
            }
        }

        return termsMap;
    }

    private Map<IVocabularyTermId, VocabularyTermPE> getPreviousTermsMap(IOperationContext context, List<VocabularyTermUpdate> updates)
    {
        Collection<IVocabularyTermId> ids = new HashSet<IVocabularyTermId>();

        for (VocabularyTermUpdate update : updates)
        {
            if (update.getPreviousTermId().getValue() != null)
            {
                ids.add(update.getPreviousTermId().getValue());
            }
        }

        Map<IVocabularyTermId, VocabularyTermPE> termsMap = mapVocabularyTermByIdExecutor.map(context, ids);

        for (IVocabularyTermId id : ids)
        {
            if (termsMap.get(id) == null)
            {
                throw new ObjectNotFoundException(id);
            }
        }

        return termsMap;
    }

}
