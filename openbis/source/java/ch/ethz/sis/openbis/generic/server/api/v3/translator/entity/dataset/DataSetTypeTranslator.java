/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class DataSetTypeTranslator extends AbstractCachingTranslator<Long, DataSetType, DataSetTypeFetchOptions> implements
        IDataSetTypeTranslator
{

    @Autowired
    private IDataSetTypeBaseTranslator baseTranslator;

    @Override
    protected DataSetType createObject(TranslationContext context, Long typeId, DataSetTypeFetchOptions fetchOptions)
    {
        final DataSetType type = new DataSetType();
        type.setFetchOptions(new DataSetTypeFetchOptions());
        return type;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds, DataSetTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IDataSetTypeBaseTranslator.class, baseTranslator.translate(context, typeIds, null));

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, DataSetType result, Object objectRelations,
            DataSetTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        DataSetTypeBaseRecord baseRecord = relations.get(IDataSetTypeBaseTranslator.class, typeId);

        result.setPermId(new EntityTypePermId(baseRecord.code));
        result.setCode(baseRecord.code);
        result.setKind(DataSetKind.valueOf(baseRecord.kind));
        result.setDescription(baseRecord.description);
        result.setModificationDate(baseRecord.modificationDate);
    }

}
