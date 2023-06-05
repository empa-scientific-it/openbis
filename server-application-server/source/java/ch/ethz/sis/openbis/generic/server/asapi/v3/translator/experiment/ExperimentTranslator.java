/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
import java.util.Set;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleSamplePropertyTranslator;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentTranslator extends AbstractCachingTranslator<Long, Experiment, ExperimentFetchOptions> implements IExperimentTranslator
{

    @Autowired
    private IExperimentAuthorizationValidator authorizationValidator;

    @Autowired
    private IExperimentBaseTranslator baseTranslator;

    @Autowired
    private IExperimentTypeRelationTranslator typeTranslator;

    @Autowired
    private IExperimentProjectTranslator projectTranslator;

    @Autowired
    private IExperimentSampleTranslator sampleTranslator;

    @Autowired
    private IExperimentDataSetTranslator dataSetTranslator;

    @Autowired
    private IExperimentPropertyTranslator propertyTranslator;

    @Autowired
    private IExperimentMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IExperimentSamplePropertyTranslator samplePropertyTranslator;

    @Autowired
    private IExperimentRegistratorTranslator registratorTranslator;

    @Autowired
    private IExperimentModifierTranslator modifierTranslator;

    @Autowired
    private IExperimentTagTranslator tagTranslator;

    @Autowired
    private IExperimentAttachmentTranslator attachmentTranslator;

    @Autowired
    private IExperimentHistoryTranslator historyTranslator;

    @Autowired
    private ExperimentPropertyHistoryTranslator propertyHistoryTranslator;

    @Autowired
    private ExperimentProjectRelationshipHistoryTranslator projectRelationshipHistoryTranslator;

    @Autowired
    private ExperimentSampleRelationshipHistoryTranslator sampleRelationshipHistoryTranslator;

    @Autowired
    private ExperimentDataSetRelationshipHistoryTranslator dataSetRelationshipHistoryTranslator;

    @Autowired
    private ExperimentUnknownRelationshipHistoryTranslator unknownRelationshipHistoryTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), experimentIds);
    }

    @Override
    protected Experiment createObject(TranslationContext context, Long experimentId, ExperimentFetchOptions fetchOptions)
    {
        Experiment experiment = new Experiment();
        experiment.setFetchOptions(new ExperimentFetchOptions());
        return experiment;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IExperimentBaseTranslator.class, baseTranslator.translate(context, experimentIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IExperimentTypeRelationTranslator.class, typeTranslator.translate(context, experimentIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProject())
        {
            relations.put(IExperimentProjectTranslator.class, projectTranslator.translate(context, experimentIds, fetchOptions.withProject()));
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(IExperimentSampleTranslator.class, sampleTranslator.translate(context, experimentIds, fetchOptions.withSamples()));
        }

        if (fetchOptions.hasDataSets())
        {
            relations.put(IExperimentDataSetTranslator.class, dataSetTranslator.translate(context, experimentIds, fetchOptions.withDataSets()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IExperimentPropertyTranslator.class,
                    propertyTranslator.translate(context, experimentIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IExperimentMaterialPropertyTranslator.class,
                    materialPropertyTranslator.translate(context, experimentIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasSampleProperties())
        {
            relations.put(ISampleSamplePropertyTranslator.class,
                    samplePropertyTranslator.translate(context, experimentIds, fetchOptions.withSampleProperties()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IExperimentRegistratorTranslator.class,
                    registratorTranslator.translate(context, experimentIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IExperimentModifierTranslator.class, modifierTranslator.translate(context, experimentIds, fetchOptions.withModifier()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IExperimentTagTranslator.class,
                    tagTranslator.translate(context, experimentIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasAttachments())
        {
            relations.put(IExperimentAttachmentTranslator.class,
                    attachmentTranslator.translate(context, experimentIds, fetchOptions.withAttachments()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IExperimentHistoryTranslator.class, historyTranslator.translate(context, experimentIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasPropertiesHistory())
        {
            relations.put(ExperimentPropertyHistoryTranslator.class,
                    propertyHistoryTranslator.translate(context, experimentIds, fetchOptions.withPropertiesHistory()));
        }

        if (fetchOptions.hasProjectHistory())
        {
            relations.put(ExperimentProjectRelationshipHistoryTranslator.class,
                    projectRelationshipHistoryTranslator.translate(context, experimentIds, fetchOptions.withProjectHistory()));
        }

        if (fetchOptions.hasSamplesHistory())
        {
            relations.put(ExperimentSampleRelationshipHistoryTranslator.class,
                    sampleRelationshipHistoryTranslator.translate(context, experimentIds, fetchOptions.withSamplesHistory()));
        }

        if (fetchOptions.hasDataSetsHistory())
        {
            relations.put(ExperimentDataSetRelationshipHistoryTranslator.class,
                    dataSetRelationshipHistoryTranslator.translate(context, experimentIds, fetchOptions.withDataSetsHistory()));
        }

        if (fetchOptions.hasUnknownHistory())
        {
            relations.put(ExperimentUnknownRelationshipHistoryTranslator.class,
                    unknownRelationshipHistoryTranslator.translate(context, experimentIds, fetchOptions.withUnknownHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long experimentId, Experiment result, Object objectRelations,
            ExperimentFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ExperimentBaseRecord baseRecord = relations.get(IExperimentBaseTranslator.class, experimentId);

        result.setCode(baseRecord.code);
        result.setPermId(new ExperimentPermId(baseRecord.permId));
        result.setIdentifier(new ExperimentIdentifier(baseRecord.spaceCode, baseRecord.projectCode, baseRecord.code));
        result.setFrozen(baseRecord.frozen);
        result.setFrozenForDataSets(baseRecord.frozenForDataSets);
        result.setFrozenForSamples(baseRecord.frozenForSamples);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);
        result.setMetaData(CommonUtils.asMap(baseRecord.metaData));

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IExperimentTypeRelationTranslator.class, experimentId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProject())
        {
            result.setProject(relations.get(IExperimentProjectTranslator.class, experimentId));
            result.getFetchOptions().withProjectUsing(fetchOptions.withProject());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples(SampleUtils.extractSamples(relations.get(IExperimentSampleTranslator.class, experimentId)));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasDataSets())
        {
            result.setDataSets((List<DataSet>) relations.get(IExperimentDataSetTranslator.class, experimentId));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IExperimentPropertyTranslator.class, experimentId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IExperimentMaterialPropertyTranslator.class, experimentId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasSampleProperties())
        {
            result.setSampleProperties(relations.get(ISampleSamplePropertyTranslator.class, experimentId));
            result.getFetchOptions().withSamplePropertiesUsing(fetchOptions.withSampleProperties());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IExperimentRegistratorTranslator.class, experimentId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IExperimentModifierTranslator.class, experimentId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IExperimentTagTranslator.class, experimentId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments((List<Attachment>) relations.get(IExperimentAttachmentTranslator.class, experimentId));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IExperimentHistoryTranslator.class, experimentId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasPropertiesHistory())
        {
            result.setPropertiesHistory(relations.get(ExperimentPropertyHistoryTranslator.class, experimentId));
            result.getFetchOptions().withPropertiesHistoryUsing(fetchOptions.withPropertiesHistory());
        }

        if (fetchOptions.hasProjectHistory())
        {
            result.setProjectHistory(relations.get(ExperimentProjectRelationshipHistoryTranslator.class, experimentId));
            result.getFetchOptions().withProjectHistoryUsing(fetchOptions.withProjectHistory());
        }

        if (fetchOptions.hasSamplesHistory())
        {
            result.setSamplesHistory(relations.get(ExperimentSampleRelationshipHistoryTranslator.class, experimentId));
            result.getFetchOptions().withSamplesHistoryUsing(fetchOptions.withSamplesHistory());
        }

        if (fetchOptions.hasDataSetsHistory())
        {
            result.setDataSetsHistory(relations.get(ExperimentDataSetRelationshipHistoryTranslator.class, experimentId));
            result.getFetchOptions().withDataSetsHistoryUsing(fetchOptions.withDataSetsHistory());
        }

        if (fetchOptions.hasUnknownHistory())
        {
            result.setUnknownHistory(relations.get(ExperimentUnknownRelationshipHistoryTranslator.class, experimentId));
            result.getFetchOptions().withUnknownHistoryUsing(fetchOptions.withUnknownHistory());
        }
    }

}
