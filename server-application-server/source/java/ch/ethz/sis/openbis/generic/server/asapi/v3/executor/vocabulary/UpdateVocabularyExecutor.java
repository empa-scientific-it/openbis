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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdateVocabularyExecutor
        extends AbstractUpdateEntityExecutor<VocabularyUpdate, VocabularyPE, IVocabularyId, VocabularyPermId>
        implements IUpdateVocabularyExecutor
{
    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private IMapVocabularyByIdExecutor mapVocabularyByIdExecutor;
    
    @Autowired
    private IVocabularyAuthorizationExecutor authorizationExecutor;

    @Override
    protected IVocabularyId getId(VocabularyUpdate update)
    {
        return update.getVocabularyId();
    }

    @Override
    protected VocabularyPermId getPermId(VocabularyPE entity)
    {
        return new VocabularyPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, VocabularyUpdate update)
    {
        if (update.getVocabularyId() == null)
        {
            throw new UserFailureException("Vocabulary id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IVocabularyId id, VocabularyPE entity)
    {
        authorizationExecutor.canUpdate(context, id, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<VocabularyUpdate, VocabularyPE> batch)
    {
        Set<Entry<VocabularyUpdate, VocabularyPE>> entrySet = batch.getObjects().entrySet();
        for (Entry<VocabularyUpdate, VocabularyPE> entry : entrySet)
        {
            VocabularyUpdate update = entry.getKey();
            VocabularyPE vocabulary = entry.getValue();
            vocabulary.setDescription(getNewValue(update.getDescription(), vocabulary.getDescription()));
            vocabulary.setChosenFromList(getNewValue(update.getChosenFromList(), vocabulary.isChosenFromList()));
            vocabulary.setURLTemplate(getNewValue(update.getUrlTemplate(), vocabulary.getURLTemplate()));
        }
    }
    
    @Override
    protected void updateAll(IOperationContext context, MapBatch<VocabularyUpdate, VocabularyPE> batch)
    {
    }

    @Override
    protected Map<IVocabularyId, VocabularyPE> map(IOperationContext context, Collection<IVocabularyId> ids)
    {
        return mapVocabularyByIdExecutor.map(context, ids);
    }

    @Override
    protected List<VocabularyPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getVocabularyDAO().listAllEntities();
    }

    @Override
    protected void save(IOperationContext context, List<VocabularyPE> entities, boolean clearCache)
    {
        for (VocabularyPE vocabulary : entities)
        {
            daoFactory.getVocabularyDAO().validateAndSaveUpdatedEntity(vocabulary);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "vocabulary", null);
    }

}
