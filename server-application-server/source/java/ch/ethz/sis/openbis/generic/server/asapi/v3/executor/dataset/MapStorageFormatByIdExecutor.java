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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IStorageFormatId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.StorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IMapVocabularyTermByIdExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class MapStorageFormatByIdExecutor implements IMapStorageFormatByIdExecutor
{

    @Autowired
    private IMapVocabularyTermByIdExecutor mapVocabularyTermByIdExecutor;

    @Autowired
    private IStorageFormatAuthorizationExecutor authorizationExecutor;

    @Override
    public Map<IStorageFormatId, VocabularyTermPE> map(IOperationContext context, Collection<? extends IStorageFormatId> ids)
    {
        return map(context, ids, true);
    }

    @Override
    public Map<IStorageFormatId, VocabularyTermPE> map(IOperationContext context, Collection<? extends IStorageFormatId> ids, boolean checkAccess)
    {
        if (checkAccess)
        {
            authorizationExecutor.canGet(context);
        }

        Map<IVocabularyTermId, IStorageFormatId> idsMap = new HashMap<IVocabularyTermId, IStorageFormatId>();

        if (ids != null)
        {
            for (IStorageFormatId id : ids)
            {
                IVocabularyTermId termId = null;

                if (id instanceof StorageFormatPermId)
                {
                    termId = new VocabularyTermPermId(((StorageFormatPermId) id).getPermId(), StorageFormat.VOCABULARY_CODE);
                } else
                {
                    throw new UserFailureException("Unsupported storageFormat: " + id);
                }

                idsMap.put(termId, id);
            }
        }

        Map<IVocabularyTermId, VocabularyTermPE> termMap =
                mapVocabularyTermByIdExecutor.map(context, idsMap.keySet());
        Map<IStorageFormatId, VocabularyTermPE> formatMap = new HashMap<IStorageFormatId, VocabularyTermPE>();

        for (Map.Entry<IVocabularyTermId, VocabularyTermPE> termEntry : termMap.entrySet())
        {
            IStorageFormatId formatId = idsMap.get(termEntry.getKey());
            formatMap.put(formatId, termEntry.getValue());
        }

        return formatMap;
    }
}
