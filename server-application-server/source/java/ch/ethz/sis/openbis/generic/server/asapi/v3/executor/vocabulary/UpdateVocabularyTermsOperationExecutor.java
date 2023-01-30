/*
 * Copyright 2016 ETH Zuerich, CISD
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.UpdateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.UpdateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.UpdateVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.update.UpdateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class UpdateVocabularyTermsOperationExecutor extends UpdateObjectsOperationExecutor<VocabularyTermUpdate, IVocabularyTermId> implements
        IUpdateVocabularyTermsOperationExecutor
{

    @Autowired
    private IUpdateVocabularyTermExecutor executor;

    @Override
    protected Class<? extends UpdateObjectsOperation<VocabularyTermUpdate>> getOperationClass()
    {
        return UpdateVocabularyTermsOperation.class;
    }

    @Override
    protected UpdateObjectsOperationResult<? extends IVocabularyTermId> doExecute(IOperationContext context,
            UpdateObjectsOperation<VocabularyTermUpdate> operation)
    {
        return new UpdateVocabularyTermsOperationResult(executor.update(context, operation.getUpdates()));
    }

}
