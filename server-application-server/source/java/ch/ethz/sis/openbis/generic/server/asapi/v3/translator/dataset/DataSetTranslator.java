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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class DataSetTranslator extends AbstractCachingTranslator<Long, DataSet, DataSetFetchOptions> implements IDataSetTranslator
{

    @Autowired
    private IDataSetAuthorizationValidator authorizationValidator;

    @Autowired
    private IDataSetBaseTranslator baseTranslator;

    @Autowired
    private IDataSetTypeRelationTranslator typeTranslator;

    @Autowired
    private IDataSetPropertyTranslator propertyTranslator;

    @Autowired
    private IDataSetMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IDataSetSamplePropertyTranslator samplePropertyTranslator;

    @Autowired
    private IDataSetPhysicalDataTranslator physicalDataTranslator;

    @Autowired
    private IDataSetLinkedDataTranslator linkedDataTranslator;

    @Autowired
    private IDataSetDataStoreTranslator dataStoreTranslator;

    @Autowired
    private IDataSetSampleTranslator sampleTranslator;

    @Autowired
    private IDataSetExperimentTranslator experimentTranslator;

    @Autowired
    private IDataSetParentTranslator parentTranslator;

    @Autowired
    private IDataSetChildTranslator childTranslator;

    @Autowired
    private IDataSetContainerTranslator containerTranslator;

    @Autowired
    private IDataSetComponentsTranslator componentsTranslator;

    @Autowired
    private IDataSetTagTranslator tagTranslator;

    @Autowired
    private IDataSetHistoryTranslator historyTranslator;

    @Autowired
    private DataSetPropertyHistoryTranslator propertyHistoryTranslator;

    @Autowired
    private DataSetExperimentRelationshipHistoryTranslator experimentRelationshipHistoryTranslator;

    @Autowired
    private DataSetSampleRelationshipHistoryTranslator sampleRelationshipHistoryTranslator;

    @Autowired
    private DataSetParentRelationshipHistoryTranslator parentRelationshipHistoryTranslator;

    @Autowired
    private DataSetChildRelationshipHistoryTranslator childRelationshipHistoryTranslator;

    @Autowired
    private DataSetContainerRelationshipHistoryTranslator containerRelationshipHistoryTranslator;

    @Autowired
    private DataSetComponentRelationshipHistoryTranslator componentRelationshipHistoryTranslator;

    @Autowired
    private DataSetUnknownRelationshipHistoryTranslator unknownRelationshipHistoryTranslator;

    @Autowired
    private DataSetContentCopyHistoryTranslator contentCopyHistoryTranslator;

    @Autowired
    private IDataSetRegistratorTranslator registratorTranslator;

    @Autowired
    private IDataSetModifierTranslator modifierTranslator;

    @Autowired
    private IDataSetPostRegisteredTranslator postRegisteredTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), dataSetIds);
    }

    @Override
    protected DataSet createObject(TranslationContext context, Long dataSetId, DataSetFetchOptions fetchOptions)
    {
        final DataSet dataSet = new DataSet();
        dataSet.setFetchOptions(new DataSetFetchOptions());
        return dataSet;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IDataSetBaseTranslator.class, baseTranslator.translate(context, dataSetIds, null));
        relations.put(IDataSetPostRegisteredTranslator.class, postRegisteredTranslator.translate(context, dataSetIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IDataSetTypeRelationTranslator.class, typeTranslator.translate(context, dataSetIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IDataSetPropertyTranslator.class, propertyTranslator.translate(context, dataSetIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IDataSetMaterialPropertyTranslator.class,
                    materialPropertyTranslator.translate(context, dataSetIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasSampleProperties())
        {
            relations.put(IDataSetSamplePropertyTranslator.class,
                    samplePropertyTranslator.translate(context, dataSetIds, fetchOptions.withSampleProperties()));
        }

        if (fetchOptions.hasPhysicalData())
        {
            relations.put(IDataSetPhysicalDataTranslator.class,
                    physicalDataTranslator.translate(context, dataSetIds, fetchOptions.withPhysicalData()));
        }

        if (fetchOptions.hasLinkedData())
        {
            relations.put(IDataSetLinkedDataTranslator.class,
                    linkedDataTranslator.translate(context, dataSetIds, fetchOptions.withLinkedData()));
        }

        if (fetchOptions.hasDataStore())
        {
            relations.put(IDataSetDataStoreTranslator.class, dataStoreTranslator.translate(context, dataSetIds, fetchOptions.withDataStore()));
        }

        if (fetchOptions.hasSample())
        {
            relations.put(IDataSetSampleTranslator.class, sampleTranslator.translate(context, dataSetIds, fetchOptions.withSample()));
        }

        if (fetchOptions.hasExperiment())
        {
            relations.put(IDataSetExperimentTranslator.class, experimentTranslator.translate(context, dataSetIds, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasContainers())
        {
            relations.put(IDataSetContainerTranslator.class, containerTranslator.translate(context, dataSetIds, fetchOptions.withContainers()));
        }

        if (fetchOptions.hasComponents())
        {
            relations.put(IDataSetComponentsTranslator.class, componentsTranslator.translate(context, dataSetIds, fetchOptions.withComponents()));
        }

        if (fetchOptions.hasParents())
        {
            relations.put(IDataSetParentTranslator.class, parentTranslator.translate(context, dataSetIds, fetchOptions.withParents()));
        }

        if (fetchOptions.hasChildren())
        {
            relations.put(IDataSetChildTranslator.class, childTranslator.translate(context, dataSetIds, fetchOptions.withChildren()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IDataSetTagTranslator.class, tagTranslator.translate(context, dataSetIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IDataSetHistoryTranslator.class, historyTranslator.translate(context, dataSetIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasPropertiesHistory())
        {
            relations.put(DataSetPropertyHistoryTranslator.class,
                    propertyHistoryTranslator.translate(context, dataSetIds, fetchOptions.withPropertiesHistory()));
        }

        if (fetchOptions.hasExperimentHistory())
        {
            relations.put(DataSetExperimentRelationshipHistoryTranslator.class,
                    experimentRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withExperimentHistory()));
        }

        if (fetchOptions.hasSampleHistory())
        {
            relations.put(DataSetSampleRelationshipHistoryTranslator.class,
                    sampleRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withSampleHistory()));
        }

        if (fetchOptions.hasParentsHistory())
        {
            relations.put(DataSetParentRelationshipHistoryTranslator.class,
                    parentRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withParentsHistory()));
        }

        if (fetchOptions.hasChildrenHistory())
        {
            relations.put(DataSetChildRelationshipHistoryTranslator.class,
                    childRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withChildrenHistory()));
        }

        if (fetchOptions.hasContainersHistory())
        {
            relations.put(DataSetContainerRelationshipHistoryTranslator.class,
                    containerRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withContainersHistory()));
        }

        if (fetchOptions.hasComponentsHistory())
        {
            relations.put(DataSetComponentRelationshipHistoryTranslator.class,
                    componentRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withComponentsHistory()));
        }

        if (fetchOptions.hasUnknownHistory())
        {
            relations.put(DataSetUnknownRelationshipHistoryTranslator.class,
                    unknownRelationshipHistoryTranslator.translate(context, dataSetIds, fetchOptions.withUnknownHistory()));
        }

        if (fetchOptions.hasContentCopiesHistory())
        {
            relations.put(DataSetContentCopyHistoryTranslator.class,
                    contentCopyHistoryTranslator.translate(context, dataSetIds, fetchOptions.withContentCopiesHistory()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations
                    .put(IDataSetRegistratorTranslator.class, registratorTranslator.translate(context, dataSetIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IDataSetModifierTranslator.class, modifierTranslator.translate(context, dataSetIds, fetchOptions.withModifier()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataSetId, DataSet result, Object objectRelations, DataSetFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        DataSetBaseRecord baseRecord = relations.get(IDataSetBaseTranslator.class, dataSetId);

        result.setPermId(new DataSetPermId(baseRecord.code));
        result.setCode(baseRecord.code);
        result.setMeasured(baseRecord.isDerived == false);
        result.setDataProducer(baseRecord.dataProducer);
        result.setDataProductionDate(baseRecord.dataProductionDate);
        result.setAccessDate(baseRecord.accessDate);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setPostRegistered(relations.get(IDataSetPostRegisteredTranslator.class, dataSetId));
        result.setKind(DataSetKind.valueOf(baseRecord.dataSetKind));
        result.setFrozen(baseRecord.frozen);
        result.setFrozenForChildren(baseRecord.frozenForChildren);
        result.setFrozenForParents(baseRecord.frozenForParents);
        result.setFrozenForComponents(baseRecord.frozenForComponents);
        result.setFrozenForContainers(baseRecord.frozenForContainers);
        result.setMetaData(CommonUtils.asMap(baseRecord.metaData));

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IDataSetTypeRelationTranslator.class, dataSetId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IDataSetPropertyTranslator.class, dataSetId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IDataSetMaterialPropertyTranslator.class, dataSetId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasSampleProperties())
        {
            result.setSampleProperties(relations.get(IDataSetSamplePropertyTranslator.class, dataSetId));
            result.getFetchOptions().withSamplePropertiesUsing(fetchOptions.withSampleProperties());
        }

        if (fetchOptions.hasPhysicalData())
        {
            result.setPhysicalData(relations.get(IDataSetPhysicalDataTranslator.class, dataSetId));
            result.getFetchOptions().withPhysicalDataUsing(fetchOptions.withPhysicalData());
        }

        if (fetchOptions.hasLinkedData())
        {
            result.setLinkedData(relations.get(IDataSetLinkedDataTranslator.class, dataSetId));
            result.getFetchOptions().withLinkedDataUsing(fetchOptions.withLinkedData());
        }

        if (fetchOptions.hasDataStore())
        {
            result.setDataStore(relations.get(IDataSetDataStoreTranslator.class, dataSetId));
            result.getFetchOptions().withDataStoreUsing(fetchOptions.withDataStore());
        }

        if (fetchOptions.hasSample())
        {
            result.setSample(relations.get(IDataSetSampleTranslator.class, dataSetId));
            result.getFetchOptions().withSampleUsing(fetchOptions.withSample());
        }

        if (fetchOptions.hasExperiment())
        {
            result.setExperiment(relations.get(IDataSetExperimentTranslator.class, dataSetId));
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasContainers())
        {
            result.setContainers((List<DataSet>) relations.get(IDataSetContainerTranslator.class, dataSetId));
            result.getFetchOptions().withContainersUsing(fetchOptions.withContainers());
        }

        if (fetchOptions.hasComponents())
        {
            result.setComponents((List<DataSet>) relations.get(IDataSetComponentsTranslator.class, dataSetId));
            result.getFetchOptions().withComponentsUsing(fetchOptions.withComponents());
        }

        if (fetchOptions.hasParents())
        {
            result.setParents((List<DataSet>) relations.get(IDataSetParentTranslator.class, dataSetId));
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasChildren())
        {
            result.setChildren((List<DataSet>) relations.get(IDataSetChildTranslator.class, dataSetId));
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IDataSetTagTranslator.class, dataSetId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IDataSetHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasPropertiesHistory())
        {
            result.setPropertiesHistory(relations.get(DataSetPropertyHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withPropertiesHistoryUsing(fetchOptions.withPropertiesHistory());
        }

        if (fetchOptions.hasExperimentHistory())
        {
            result.setExperimentHistory(relations.get(DataSetExperimentRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withExperimentHistoryUsing(fetchOptions.withExperimentHistory());
        }

        if (fetchOptions.hasSampleHistory())
        {
            result.setSampleHistory(relations.get(DataSetSampleRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withSampleHistoryUsing(fetchOptions.withSampleHistory());
        }

        if (fetchOptions.hasParentsHistory())
        {
            result.setParentsHistory(relations.get(DataSetParentRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withParentsHistoryUsing(fetchOptions.withParentsHistory());
        }

        if (fetchOptions.hasChildrenHistory())
        {
            result.setChildrenHistory(relations.get(DataSetChildRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withChildrenHistoryUsing(fetchOptions.withChildrenHistory());
        }

        if (fetchOptions.hasContainersHistory())
        {
            result.setContainersHistory(relations.get(DataSetContainerRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withContainersHistoryUsing(fetchOptions.withContainersHistory());
        }

        if (fetchOptions.hasComponentsHistory())
        {
            result.setComponentsHistory(relations.get(DataSetComponentRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withComponentsHistoryUsing(fetchOptions.withComponentsHistory());
        }

        if (fetchOptions.hasUnknownHistory())
        {
            result.setUnknownHistory(relations.get(DataSetUnknownRelationshipHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withUnknownHistoryUsing(fetchOptions.withUnknownHistory());
        }

        if (fetchOptions.hasContentCopiesHistory())
        {
            result.setContentCopiesHistory(relations.get(DataSetContentCopyHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withContentCopiesHistoryUsing(fetchOptions.withContentCopiesHistory());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IDataSetRegistratorTranslator.class, dataSetId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IDataSetModifierTranslator.class, dataSetId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

    }

}
