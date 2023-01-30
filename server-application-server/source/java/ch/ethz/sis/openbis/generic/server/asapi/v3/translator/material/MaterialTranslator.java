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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class MaterialTranslator extends AbstractCachingTranslator<Long, Material, MaterialFetchOptions> implements IMaterialTranslator
{

    @Autowired
    private IMaterialBaseTranslator baseTranslator;

    @Autowired
    private IMaterialTypeRelationTranslator typeTranslator;

    @Autowired
    private IMaterialPropertyTranslator propertyTranslator;

    @Autowired
    private IMaterialMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IMaterialRegistratorTranslator registratorTranslator;

    @Autowired
    private IMaterialTagTranslator tagsTranslator;

    @Autowired
    private IMaterialHistoryTranslator historyTranslator;

    @Override
    protected Material createObject(TranslationContext context, Long materialId, MaterialFetchOptions fetchOptions)
    {
        final Material material = new Material();
        material.setFetchOptions(new MaterialFetchOptions());
        return material;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> materialIds, MaterialFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IMaterialBaseTranslator.class, baseTranslator.translate(context, materialIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IMaterialTypeRelationTranslator.class, typeTranslator.translate(context, materialIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IMaterialPropertyTranslator.class, propertyTranslator.translate(context, materialIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IMaterialMaterialPropertyTranslator.class,
                    materialPropertyTranslator.translate(context, materialIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IMaterialRegistratorTranslator.class,
                    registratorTranslator.translate(context, materialIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IMaterialTagTranslator.class, tagsTranslator.translate(context, materialIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IMaterialHistoryTranslator.class, historyTranslator.translate(context, materialIds, fetchOptions.withHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long materialId, Material result, Object objectRelations,
            MaterialFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        MaterialBaseRecord baseRecord = relations.get(IMaterialBaseTranslator.class, materialId);

        result.setPermId(new MaterialPermId(baseRecord.code, baseRecord.typeCode));
        result.setCode(baseRecord.code);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IMaterialTypeRelationTranslator.class, materialId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IMaterialPropertyTranslator.class, materialId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IMaterialMaterialPropertyTranslator.class, materialId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IMaterialRegistratorTranslator.class, materialId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IMaterialTagTranslator.class, materialId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IMaterialHistoryTranslator.class, materialId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

    }

}
