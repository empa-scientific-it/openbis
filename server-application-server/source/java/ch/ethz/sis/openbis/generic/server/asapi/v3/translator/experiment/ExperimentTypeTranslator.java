/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment;

import java.util.Collection;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentTypeTranslator extends AbstractCachingTranslator<Long, ExperimentType, ExperimentTypeFetchOptions> implements
        IExperimentTypeTranslator
{

    @Autowired
    private IExperimentTypeBaseTranslator baseTranslator;

    @Autowired
    private IExperimentPropertyAssignmentTranslator assignmentTranslator;

    @Autowired
    private IExperimentTypeValidationPluginTranslator validationPluginTranslator;

    @Override
    protected ExperimentType createObject(TranslationContext context, Long typeId, ExperimentTypeFetchOptions fetchOptions)
    {
        final ExperimentType type = new ExperimentType();
        type.setFetchOptions(fetchOptions);
        return type;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds, ExperimentTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IExperimentTypeBaseTranslator.class, baseTranslator.translate(context, typeIds, null));
        if (fetchOptions.hasPropertyAssignments())
        {
            relations.put(IExperimentPropertyAssignmentTranslator.class,
                    assignmentTranslator.translate(context, typeIds, fetchOptions.withPropertyAssignments()));
        }
        if (fetchOptions.hasValidationPlugin())
        {
            relations.put(IExperimentTypeValidationPluginTranslator.class,
                    validationPluginTranslator.translate(context, typeIds, fetchOptions.withValidationPlugin()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, ExperimentType result, Object objectRelations,
            ExperimentTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ExperimentTypeBaseRecord baseRecord = relations.get(IExperimentTypeBaseTranslator.class, typeId);

        result.setPermId(new EntityTypePermId(baseRecord.code, EntityKind.EXPERIMENT));
        result.setCode(baseRecord.code);
        result.setDescription(baseRecord.description);
        result.setModificationDate(baseRecord.modificationDate);
        result.setMetaData(CommonUtils.asMap(baseRecord.metaData));

        if (fetchOptions.hasPropertyAssignments())
        {
            result.setPropertyAssignments(
                    (List<PropertyAssignment>) relations.get(IExperimentPropertyAssignmentTranslator.class, typeId));
        }
        if (fetchOptions.hasValidationPlugin())
        {
            result.setValidationPlugin(relations.get(IExperimentTypeValidationPluginTranslator.class, typeId));
        }
    }

}
