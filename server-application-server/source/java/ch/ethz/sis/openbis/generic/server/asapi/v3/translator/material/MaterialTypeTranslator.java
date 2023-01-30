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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class MaterialTypeTranslator extends AbstractCachingTranslator<Long, MaterialType, MaterialTypeFetchOptions> implements
        IMaterialTypeTranslator
{

    @Autowired
    private IMaterialTypeBaseTranslator baseTranslator;

    @Autowired
    private IMaterialPropertyAssignmentTranslator assignmentTranslator;

    @Autowired
    private IMaterialTypeValidationPluginTranslator validationPluginTranslator;

    @Override
    protected MaterialType createObject(TranslationContext context, Long typeId, MaterialTypeFetchOptions fetchOptions)
    {
        final MaterialType materialType = new MaterialType();
        materialType.setFetchOptions(fetchOptions);
        return materialType;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds, MaterialTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IMaterialTypeBaseTranslator.class, baseTranslator.translate(context, typeIds, null));
        if (fetchOptions.hasPropertyAssignments())
        {
            relations.put(IMaterialPropertyAssignmentTranslator.class,
                    assignmentTranslator.translate(context, typeIds, fetchOptions.withPropertyAssignments()));
        }
        if (fetchOptions.hasValidationPlugin())
        {
            relations.put(IMaterialTypeValidationPluginTranslator.class,
                    validationPluginTranslator.translate(context, typeIds, fetchOptions.withValidationPlugin()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, MaterialType result, Object objectRelations,
            MaterialTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        MaterialTypeBaseRecord baseRecord = relations.get(IMaterialTypeBaseTranslator.class, typeId);

        result.setPermId(new EntityTypePermId(baseRecord.code, EntityKind.MATERIAL));
        result.setCode(baseRecord.code);
        result.setDescription(baseRecord.description);
        result.setModificationDate(baseRecord.modificationDate);

        if (fetchOptions.hasPropertyAssignments())
        {
            result.setPropertyAssignments((List<PropertyAssignment>) relations.get(IMaterialPropertyAssignmentTranslator.class, typeId));
        }
        if (fetchOptions.hasValidationPlugin())
        {
            result.setValidationPlugin(relations.get(IMaterialTypeValidationPluginTranslator.class, typeId));
        }
    }

}
