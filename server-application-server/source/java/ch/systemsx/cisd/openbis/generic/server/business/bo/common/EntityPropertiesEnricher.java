/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermEntityProperty;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * A class that can enrich a set of entities with its entity properties.
 * 
 * @author Bernd Rinn
 */
public final class EntityPropertiesEnricher implements IEntityPropertiesEnricher
{
    private final IPropertyListingQuery query;

    private final IEntityPropertySetListingQuery propertySetQuery;

    public EntityPropertiesEnricher(final IPropertyListingQuery query,
            final IEntityPropertySetListingQuery setQuery)
    {
        this.query = query;
        this.propertySetQuery = createEfficientIterator(setQuery);
    }

    private IEntityPropertySetListingQuery createEfficientIterator(
            final IEntityPropertySetListingQuery setQuery)
    {
        return new IEntityPropertySetListingQuery()
            {
                private final static int BATCH_SIZE = 50000;

                @Override
                public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
                        LongSet entityIDs)
                {
                    return new AbstractBatchIterator<GenericEntityPropertyRecord>(entityIDs,
                            BATCH_SIZE)
                        {
                            @Override
                            protected Iterable<GenericEntityPropertyRecord> createUnefficientIterator(
                                    LongSet ids)
                            {
                                return setQuery.getEntityPropertyGenericValues(ids);
                            }
                        };
                }
            };
    }

    /**
     * Enriches the entities with given <var>entityIDs</var> with its properties. The entities will be resolved by the
     * {@link IEntityPropertiesHolderResolver} and will be enriched in place.
     */
    @Override
    public void enrich(final LongSet entityIDs, final IEntityPropertiesHolderResolver entities)
    {
        List<GenericEntityPropertyRecord> records = new ArrayList<>();
        LongSet vocaTermIds = new LongOpenHashSet();
        LongSet materialIds = new LongOpenHashSet();
        LongSet sampleIds = new LongOpenHashSet();
        for (GenericEntityPropertyRecord record : propertySetQuery.getEntityPropertyGenericValues(entityIDs))
        {
            records.add(record);
            if (record.cvte_id != null)
            {
                vocaTermIds.add(record.cvte_id);
            }
            if (record.mate_prop_id != null)
            {
                materialIds.add(record.mate_prop_id);
            }
            if (record.samp_prop_id != null)
            {
                sampleIds.add(record.samp_prop_id);
            }
        }

        Long2ObjectMap<VocabularyTerm> vocabularyTerms = getVocabularyTerms(vocaTermIds);
        Long2ObjectMap<Material> materials = getMaterials(materialIds);
        Long2ObjectMap<Sample> samples = getSamples(sampleIds);
        Long2ObjectMap<PropertyType> propertyTypes = getPropertyTypes();
        for (GenericEntityPropertyRecord val : records)
        {
            AbstractEntityProperty property;
            if (val.cvte_id != null)
            {
                property = new VocabularyTermEntityProperty();
                property.setVocabularyTerm(vocabularyTerms.get(val.cvte_id));
            } else if (val.mate_prop_id != null)
            {
                property = new MaterialEntityProperty();
                property.setMaterial(materials.get(val.mate_prop_id));
            } else if (val.samp_prop_id != null)
            {
                property = new SampleEntityProperty();
                property.setSample(samples.get(val.samp_prop_id));
            } else
            {
                property = new GenericEntityProperty();
                property.setValue(val.value);
                property.setScriptable(val.script_id != null);
                property.setDynamic(ScriptType.DYNAMIC_PROPERTY.name().equals(val.script_type));
            }
            property.setPropertyType(propertyTypes.get(val.prty_id));
            property.setOrdinal(val.ordinal);
            IEntityPropertiesHolder entity = entities.get(val.entity_id);
            entity.getProperties().add(property);
        }
    }

    private Long2ObjectMap<PropertyType> getPropertyTypes()
    {
        final PropertyType[] types = query.getPropertyTypes();
        final Long2ObjectOpenHashMap<PropertyType> propertyTypeMap =
                new Long2ObjectOpenHashMap<PropertyType>(types.length);
        for (PropertyType t : types)
        {
            propertyTypeMap.put(t.getId(), t);
        }
        propertyTypeMap.trim();
        return propertyTypeMap;
    }

    private Long2ObjectMap<String> getVocabularyURLs()
    {
        final CodeRecord[] vocabURLs = query.getVocabularyURLTemplates();
        final Long2ObjectOpenHashMap<String> vocabularyURLMap =
                new Long2ObjectOpenHashMap<String>(vocabURLs.length);
        for (CodeRecord vocabURL : vocabURLs)
        {
            vocabularyURLMap.put(vocabURL.id, vocabURL.code);
        }
        vocabularyURLMap.trim();
        return vocabularyURLMap;
    }

    private Long2ObjectMap<MaterialType> getMaterialTypes()
    {
        final CodeRecord[] typeCodes = query.getMaterialTypes();
        final Long2ObjectOpenHashMap<MaterialType> materialTypeMap =
                new Long2ObjectOpenHashMap<MaterialType>(typeCodes.length);
        for (CodeRecord t : typeCodes)
        {
            final MaterialType type = new MaterialType();
            type.setCode(t.code);
            materialTypeMap.put(t.id, type);
        }
        materialTypeMap.trim();
        return materialTypeMap;
    }

    private Long2ObjectMap<SampleType> getSampleTypes()
    {
        final CodeRecord[] typeCodes = query.getSampleTypeIdsAndCode();
        final Long2ObjectOpenHashMap<SampleType> sampleTypeMap =
                new Long2ObjectOpenHashMap<SampleType>(typeCodes.length);
        for (CodeRecord t : typeCodes)
        {
            final SampleType type = new SampleType();
            type.setCode(t.code);
            sampleTypeMap.put(t.id, type);
        }
        sampleTypeMap.trim();
        return sampleTypeMap;
    }
    
    private Long2ObjectMap<VocabularyTerm> getVocabularyTerms(LongSet vocaTermIds)
    {
        Long2ObjectMap<String> vocabularyURLMap = null;
        Long2ObjectOpenHashMap<VocabularyTerm> map = new Long2ObjectOpenHashMap<VocabularyTerm>();
        if (vocaTermIds.isEmpty() == false)
        {
            for (VocabularyTermRecord record : query.getVocabularyTerms(vocaTermIds))
            {
                if (vocabularyURLMap == null)
                {
                    vocabularyURLMap = getVocabularyURLs();
                }
                VocabularyTerm vocabularyTerm = new VocabularyTerm();
                vocabularyTerm.setCode(record.code);
                vocabularyTerm.setLabel(record.label);
                vocabularyTerm.setDescription(record.description);
                vocabularyTerm.setOrdinal(record.ordinal);
                final String template = vocabularyURLMap.get(record.covo_id);
                if (template != null)
                {
                    String url = template.replaceAll(
                            BasicConstant.DEPRECATED_VOCABULARY_URL_TEMPLATE_TERM_PATTERN,
                            record.code);
                    url = url.replaceAll(BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN, record.code);
                    vocabularyTerm.setUrl(url);
                }
                map.put(record.id, vocabularyTerm);
            }
        }
        return map;
    }
    
    private Long2ObjectMap<Material> getMaterials(LongSet materialIds)
    {
        Long2ObjectMap<MaterialType> materialTypes = null;
        Long2ObjectOpenHashMap<Material> map = new Long2ObjectOpenHashMap<Material>();
        if (materialIds.isEmpty() == false)
        {
            for (MaterialEntityPropertyRecord record : query.getMaterials(materialIds))
            {
                if (materialTypes == null)
                {
                    materialTypes = getMaterialTypes();
                }
                Material material = new Material();
                material.setCode(record.code);
                material.setMaterialType(materialTypes.get(record.maty_id));
                material.setId(record.id);
                map.put(record.id, material);
            }
        }
        return map;
    }
    
    private Long2ObjectMap<Sample> getSamples(LongSet sampleIds)
    {
        Long2ObjectMap<SampleType> sampleTypes = null;
        Long2ObjectOpenHashMap<Sample> map = new Long2ObjectOpenHashMap<Sample>();
        if (sampleIds.isEmpty() == false)
        {
            for (SampleEntityPropertyRecord record : query.getBasicSamples(sampleIds))
            {
                if (sampleTypes == null)
                {
                    sampleTypes = getSampleTypes();
                }
                Sample sample = new Sample();
                sample.setCode(record.code);
                sample.setSampleType(sampleTypes.get(record.saty_id));
                sample.setId(record.id);
                sample.setPermId(record.perm_id);
                map.put(record.id, sample);
            }
        }
        return map;
    }
}
