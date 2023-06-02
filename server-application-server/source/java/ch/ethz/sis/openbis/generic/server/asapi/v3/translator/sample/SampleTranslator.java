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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class SampleTranslator extends AbstractCachingTranslator<Long, Sample, SampleFetchOptions> implements ISampleTranslator
{

    @Autowired
    private ISampleAuthorizationValidator authorizationValidator;

    @Autowired
    private ISampleBaseTranslator baseTranslator;

    @Autowired
    private ISampleTypeRelationTranslator typeTranslator;

    @Autowired
    private ISampleSpaceTranslator spaceTranslator;

    @Autowired
    private ISampleProjectTranslator projectTranslator;

    @Autowired
    private ISamplePropertyTranslator propertyTranslator;

    @Autowired
    private ISampleMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private ISampleSamplePropertyTranslator samplePropertyTranslator;

    @Autowired
    private ISampleExperimentTranslator experimentTranslator;

    @Autowired
    private ISampleParentTranslator parentTranslator;

    @Autowired
    private ISampleContainerTranslator containerTranslator;

    @Autowired
    private ISampleComponentsTranslator componentsTranslator;

    @Autowired
    private ISampleChildTranslator childTranslator;

    @Autowired
    private ISampleDataSetTranslator dataSetTranslator;

    @Autowired
    private ISampleTagTranslator tagTranslator;

    @Autowired
    private ISampleAttachmentTranslator attachmentTranslator;

    @Autowired
    private ISampleHistoryTranslator historyTranslator;

    @Autowired
    private SamplePropertyHistoryTranslator propertyHistoryTranslator;

    @Autowired
    private SampleSpaceRelationshipHistoryTranslator spaceRelationshipHistoryTranslator;

    @Autowired
    private SampleProjectRelationshipHistoryTranslator projectRelationshipHistoryTranslator;

    @Autowired
    private SampleExperimentRelationshipHistoryTranslator experimentRelationshipHistoryTranslator;

    @Autowired
    private SampleParentRelationshipHistoryTranslator parentRelationshipHistoryTranslator;

    @Autowired
    private SampleChildRelationshipHistoryTranslator childRelationshipHistoryTranslator;

    @Autowired
    private SampleContainerRelationshipHistoryTranslator containerRelationshipHistoryTranslator;

    @Autowired
    private SampleComponentRelationshipHistoryTranslator componentRelationshipHistoryTranslator;

    @Autowired
    private SampleDataSetRelationshipHistoryTranslator dataSetRelationshipHistoryTranslator;

    @Autowired
    private SampleUnknownRelationshipHistoryTranslator unknownRelationshipHistoryTranslator;

    @Autowired
    private ISampleRegistratorTranslator registratorTranslator;

    @Autowired
    private ISampleModifierTranslator modifierTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> sampleIds, SampleFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), sampleIds);
    }

    @Override
    protected Sample createObject(TranslationContext context, Long sampleId, SampleFetchOptions fetchOptions)
    {
        final Sample sample = new Sample();
        sample.setFetchOptions(new SampleFetchOptions());
        return sample;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> sampleIds, SampleFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISampleBaseTranslator.class, baseTranslator.translate(context, sampleIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(ISampleTypeRelationTranslator.class, typeTranslator.translate(context, sampleIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasSpace())
        {
            relations.put(ISampleSpaceTranslator.class, spaceTranslator.translate(context, sampleIds, fetchOptions.withSpace()));
        }

        if (fetchOptions.hasProject())
        {
            relations.put(ISampleProjectTranslator.class, projectTranslator.translate(context, sampleIds, fetchOptions.withProject()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(ISamplePropertyTranslator.class, propertyTranslator.translate(context, sampleIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(ISampleMaterialPropertyTranslator.class,
                    materialPropertyTranslator.translate(context, sampleIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasSampleProperties())
        {
            relations.put(ISampleSamplePropertyTranslator.class,
                    samplePropertyTranslator.translate(context, sampleIds, fetchOptions.withSampleProperties()));
        }

        if (fetchOptions.hasExperiment())
        {
            relations.put(ISampleExperimentTranslator.class, experimentTranslator.translate(context, sampleIds, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasContainer())
        {
            relations.put(ISampleContainerTranslator.class, containerTranslator.translate(context, sampleIds, fetchOptions.withContainer()));
        }

        if (fetchOptions.hasComponents())
        {
            relations.put(ISampleComponentsTranslator.class, componentsTranslator.translate(context, sampleIds, fetchOptions.withComponents()));
        }

        if (fetchOptions.hasParents())
        {
            relations.put(ISampleParentTranslator.class, parentTranslator.translate(context, sampleIds, fetchOptions.withParents()));
        }

        if (fetchOptions.hasChildren())
        {
            relations.put(ISampleChildTranslator.class, childTranslator.translate(context, sampleIds, fetchOptions.withChildren()));
        }

        if (fetchOptions.hasDataSets())
        {
            relations.put(ISampleDataSetTranslator.class, dataSetTranslator.translate(context, sampleIds, fetchOptions.withDataSets()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(ISampleTagTranslator.class, tagTranslator.translate(context, sampleIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasAttachments())
        {
            relations.put(ISampleAttachmentTranslator.class, attachmentTranslator.translate(context, sampleIds, fetchOptions.withAttachments()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(ISampleHistoryTranslator.class, historyTranslator.translate(context, sampleIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasPropertiesHistory())
        {
            relations.put(SamplePropertyHistoryTranslator.class,
                    propertyHistoryTranslator.translate(context, sampleIds, fetchOptions.withPropertiesHistory()));
        }

        if (fetchOptions.hasSpaceHistory())
        {
            relations.put(SampleSpaceRelationshipHistoryTranslator.class,
                    spaceRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withSpaceHistory()));
        }

        if (fetchOptions.hasProjectHistory())
        {
            relations.put(SampleProjectRelationshipHistoryTranslator.class,
                    projectRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withProjectHistory()));
        }

        if (fetchOptions.hasExperimentHistory())
        {
            relations.put(SampleExperimentRelationshipHistoryTranslator.class,
                    experimentRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withExperimentHistory()));
        }

        if (fetchOptions.hasParentsHistory())
        {
            relations.put(SampleParentRelationshipHistoryTranslator.class,
                    parentRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withParentsHistory()));
        }

        if (fetchOptions.hasChildrenHistory())
        {
            relations.put(SampleChildRelationshipHistoryTranslator.class,
                    childRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withChildrenHistory()));
        }

        if (fetchOptions.hasContainerHistory())
        {
            relations.put(SampleContainerRelationshipHistoryTranslator.class,
                    containerRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withContainerHistory()));
        }

        if (fetchOptions.hasComponentsHistory())
        {
            relations.put(SampleComponentRelationshipHistoryTranslator.class,
                    componentRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withComponentsHistory()));
        }

        if (fetchOptions.hasDataSetsHistory())
        {
            relations.put(SampleDataSetRelationshipHistoryTranslator.class,
                    dataSetRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withDataSetsHistory()));
        }

        if (fetchOptions.hasUnknownHistory())
        {
            relations.put(SampleUnknownRelationshipHistoryTranslator.class,
                    unknownRelationshipHistoryTranslator.translate(context, sampleIds, fetchOptions.withUnknownHistory()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(ISampleRegistratorTranslator.class, registratorTranslator.translate(context, sampleIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(ISampleModifierTranslator.class, modifierTranslator.translate(context, sampleIds, fetchOptions.withModifier()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long sampleId, Sample result, Object objectRelations, SampleFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SampleBaseRecord baseRecord = relations.get(ISampleBaseTranslator.class, sampleId);

        result.setPermId(new SamplePermId(baseRecord.permId));
        result.setCode(baseRecord.code);
        result.setIdentifier(new SampleIdentifier(baseRecord.spaceCode, baseRecord.projectCode,
                baseRecord.containerCode, baseRecord.code));
        result.setFrozen(baseRecord.frozen);
        result.setFrozenForComponents(baseRecord.frozenForComponents);
        result.setFrozenForChildren(baseRecord.frozenForChildren);
        result.setFrozenForParents(baseRecord.frozenForParents);
        result.setFrozenForDataSets(baseRecord.frozenForDataSets);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setMetaData(CommonUtils.asMap(baseRecord.metaData));

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(ISampleTypeRelationTranslator.class, sampleId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasSpace())
        {
            result.setSpace(relations.get(ISampleSpaceTranslator.class, sampleId));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasProject())
        {
            result.setProject(relations.get(ISampleProjectTranslator.class, sampleId));
            result.getFetchOptions().withProjectUsing(fetchOptions.withProject());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(ISamplePropertyTranslator.class, sampleId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(ISampleMaterialPropertyTranslator.class, sampleId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasSampleProperties())
        {
            result.setSampleProperties(relations.get(ISampleSamplePropertyTranslator.class, sampleId));
            result.getFetchOptions().withSamplePropertiesUsing(fetchOptions.withSampleProperties());
        }

        if (fetchOptions.hasExperiment())
        {
            result.setExperiment(relations.get(ISampleExperimentTranslator.class, sampleId));
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasContainer())
        {
            result.setContainer(relations.get(ISampleContainerTranslator.class, sampleId));
            result.getFetchOptions().withContainerUsing(fetchOptions.withContainer());
        }

        if (fetchOptions.hasComponents())
        {
            result.setComponents(SampleUtils.extractSamples(relations.get(ISampleComponentsTranslator.class, sampleId)));
            result.getFetchOptions().withComponentsUsing(fetchOptions.withComponents());
        }

        if (fetchOptions.hasParents())
        {
            Collection<SampleWithAnnotations> samples = relations.get(ISampleParentTranslator.class, sampleId);
            result.setParents(SampleUtils.extractSamples(samples));
            Map<SamplePermId, Relationship> relationships = new HashMap<>();
            for (SampleWithAnnotations sampleWithAnnotations : samples)
            {
                SamplePermId samplePermId = sampleWithAnnotations.getSample().getPermId();
                Relationship relationship = new Relationship();
                relationship.setChildAnnotations(CommonUtils.asMap(sampleWithAnnotations.getAnnotations(sampleId)));
                relationship.setParentAnnotations(CommonUtils.asMap(sampleWithAnnotations.getRelatedAnnotations(sampleId)));
                relationships.put(samplePermId == null ? sampleWithAnnotations : samplePermId, relationship);
            }
            result.setParentsRelationships(relationships);
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasChildren())
        {
            Collection<SampleWithAnnotations> samples = relations.get(ISampleChildTranslator.class, sampleId);
            result.setChildren(SampleUtils.extractSamples(samples));
            Map<SamplePermId, Relationship> relationships = new HashMap<>();
            for (SampleWithAnnotations sampleWithAnnotations : samples)
            {
                SamplePermId samplePermId = sampleWithAnnotations.getSample().getPermId();
                Relationship relationship = new Relationship();
                relationship.setParentAnnotations(CommonUtils.asMap(sampleWithAnnotations.getAnnotations(sampleId)));
                relationship.setChildAnnotations(CommonUtils.asMap(sampleWithAnnotations.getRelatedAnnotations(sampleId)));
                relationships.put(samplePermId == null ? sampleWithAnnotations : samplePermId, relationship);
            }
            result.setChildrenRelationships(relationships);
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasDataSets())
        {
            result.setDataSets((List<DataSet>) relations.get(ISampleDataSetTranslator.class, sampleId));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(ISampleTagTranslator.class, sampleId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments((List<Attachment>) relations.get(ISampleAttachmentTranslator.class, sampleId));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(ISampleHistoryTranslator.class, sampleId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasPropertiesHistory())
        {
            result.setPropertiesHistory(relations.get(SamplePropertyHistoryTranslator.class, sampleId));
            result.getFetchOptions().withPropertiesHistoryUsing(fetchOptions.withPropertiesHistory());
        }

        if (fetchOptions.hasSpaceHistory())
        {
            result.setSpaceHistory(relations.get(SampleSpaceRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withSpaceHistoryUsing(fetchOptions.withSpaceHistory());
        }

        if (fetchOptions.hasProjectHistory())
        {
            result.setProjectHistory(relations.get(SampleProjectRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withProjectHistoryUsing(fetchOptions.withProjectHistory());
        }

        if (fetchOptions.hasExperimentHistory())
        {
            result.setExperimentHistory(relations.get(SampleExperimentRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withExperimentHistoryUsing(fetchOptions.withExperimentHistory());
        }

        if (fetchOptions.hasParentsHistory())
        {
            result.setParentsHistory(relations.get(SampleParentRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withParentsHistoryUsing(fetchOptions.withParentsHistory());
        }

        if (fetchOptions.hasChildrenHistory())
        {
            result.setChildrenHistory(relations.get(SampleChildRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withChildrenHistoryUsing(fetchOptions.withChildrenHistory());
        }

        if (fetchOptions.hasContainerHistory())
        {
            result.setContainerHistory(relations.get(SampleContainerRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withContainerHistoryUsing(fetchOptions.withContainerHistory());
        }

        if (fetchOptions.hasComponentsHistory())
        {
            result.setComponentsHistory(relations.get(SampleComponentRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withComponentsHistoryUsing(fetchOptions.withComponentsHistory());
        }

        if (fetchOptions.hasDataSetsHistory())
        {
            result.setDataSetsHistory(relations.get(SampleDataSetRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withDataSetsHistoryUsing(fetchOptions.withDataSetsHistory());
        }

        if (fetchOptions.hasUnknownHistory())
        {
            result.setUnknownHistory(relations.get(SampleUnknownRelationshipHistoryTranslator.class, sampleId));
            result.getFetchOptions().withUnknownHistoryUsing(fetchOptions.withUnknownHistory());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(ISampleRegistratorTranslator.class, sampleId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(ISampleModifierTranslator.class, sampleId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

    }

    @Override
    protected void postTranslate(TranslationContext context, Map<Long, Sample> samplesByIds)
    {
        Set<Sample> visitedSamples = new HashSet<>();
        for (Sample sample : samplesByIds.values())
        {
            postTranslate(sample, visitedSamples);
        }
    }

    private void postTranslate(Sample sample, Set<Sample> visitedSamples)
    {
        if (visitedSamples.contains(sample) == false)
        {
            visitedSamples.add(sample);
            if (sample.getFetchOptions().hasParents())
            {
                replaceRelationshipPlaceholders(sample.getParentsRelationships());
                for (Sample parent : sample.getParents())
                {
                    postTranslate(parent, visitedSamples);
                }
            }
            if (sample.getFetchOptions().hasChildren())
            {
                replaceRelationshipPlaceholders(sample.getChildrenRelationships());
                for (Sample child : sample.getChildren())
                {
                    postTranslate(child, visitedSamples);
                }
            }
        }
    }

    private void replaceRelationshipPlaceholders(Map<SamplePermId, Relationship> relationships)
    {
        if (relationships != null)
        {
            for (SamplePermId samplePermId : new HashSet<>(relationships.keySet()))
            {
                if (samplePermId instanceof SampleWithAnnotations)
                {
                    SampleWithAnnotations sampleWithAnnotations = (SampleWithAnnotations) samplePermId;
                    SamplePermId permId = sampleWithAnnotations.getSample().getPermId();
                    if (permId != null)
                    {
                        relationships.put(permId, relationships.remove(sampleWithAnnotations));
                    }
                }
            }
        }
    }
}
