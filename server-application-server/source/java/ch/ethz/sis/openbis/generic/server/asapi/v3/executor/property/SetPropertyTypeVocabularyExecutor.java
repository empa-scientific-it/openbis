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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IMapVocabularyByIdExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SetPropertyTypeVocabularyExecutor
        extends AbstractSetEntityToOneRelationExecutor<PropertyTypeCreation, PropertyTypePE, IVocabularyId, VocabularyPE>
        implements ISetPropertyTypeVocabularyExecutor
{
    @Autowired
    private IMapVocabularyByIdExecutor mapVocabularyByIdExecutor;

    @Override
    protected String getRelationName()
    {
        return "property type-vocabulary";
    }

    @Override
    protected IVocabularyId getRelatedId(PropertyTypeCreation creation)
    {
        return creation.getVocabularyId();
    }

    @Override
    protected Map<IVocabularyId, VocabularyPE> map(IOperationContext context, List<IVocabularyId> relatedIds)
    {
        return mapVocabularyByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, PropertyTypePE entity, IVocabularyId relatedId, VocabularyPE related)
    {
    }

    @Override
    protected void set(IOperationContext context, PropertyTypePE entity, VocabularyPE related)
    {
        entity.setVocabulary(related);
    }
}
