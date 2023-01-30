/*
 * Copyright 2018 ETH Zuerich, SIS
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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreateVocabularyExecutor
        extends AbstractCreateEntityExecutor<VocabularyCreation, VocabularyPE, VocabularyPermId>
        implements ICreateVocabularyExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IVocabularyAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ICreateVocabularyTermExecutor createTermExecutor;

    @Override
    protected IObjectId getId(VocabularyPE entity)
    {
        return new VocabularyPermId(entity.getCode());
    }

    @Override
    protected VocabularyPermId createPermId(IOperationContext context, VocabularyPE entity)
    {
        return new VocabularyPermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, VocabularyCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
    }

    @Override
    protected void checkAccess(IOperationContext context, VocabularyPE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected List<VocabularyPE> createEntities(IOperationContext context, CollectionBatch<VocabularyCreation> batch)
    {
        List<VocabularyPE> vocabularies = new ArrayList<>();
        PersonPE person = context.getSession().tryGetPerson();
        new CollectionBatchProcessor<VocabularyCreation>(context, batch)
            {
                @Override
                public void process(VocabularyCreation vocabularyCreation)
                {
                    VocabularyPE vocabulary = new VocabularyPE();
                    vocabulary.setCode(vocabularyCreation.getCode());
                    vocabulary.setDescription(vocabularyCreation.getDescription());
                    vocabulary.setManagedInternally(vocabularyCreation.isManagedInternally());
                    vocabulary.setChosenFromList(vocabularyCreation.isChosenFromList());
                    vocabulary.setURLTemplate(vocabularyCreation.getUrlTemplate());
                    vocabulary.setRegistrator(person);
                    vocabularies.add(vocabulary);
                }

                @Override
                public IProgress createProgress(VocabularyCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };
        return vocabularies;
    }

    @Override
    protected void save(IOperationContext context, List<VocabularyPE> entities, boolean clearCache)
    {
        for (VocabularyPE vocabulary : entities)
        {
            daoFactory.getVocabularyDAO().createOrUpdateVocabulary(vocabulary);
        }
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<VocabularyCreation, VocabularyPE> batch)
    {
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<VocabularyCreation, VocabularyPE> batch)
    {
        for (Entry<VocabularyCreation, VocabularyPE> entry : batch.getObjects().entrySet())
        {
            VocabularyCreation vocabularyCreation = entry.getKey();
            List<VocabularyTermCreation> terms = vocabularyCreation.getTerms();
            if (terms != null)
            {
                for (VocabularyTermCreation termCreation : terms)
                {
                    termCreation.setVocabularyId(new VocabularyPermId(entry.getValue().getCode()));
                }
                createTermExecutor.create(context, terms);
            }
        }
    }

    @Override
    protected List<VocabularyPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getVocabularyDAO().listAllEntities();
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "vocabulary", null);
    }

}
