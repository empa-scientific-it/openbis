/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.globalsearch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationCache.CacheKey;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTranslator;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.MatchingEntityValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyMatch;

/**
 * @author pkupczyk
 */
@Component
public class GlobalSearchObjectTranslator extends AbstractCachingTranslator<MatchingEntity, GlobalSearchObject, GlobalSearchObjectFetchOptions>
        implements IGlobalSearchObjectTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IExperimentTranslator experimentTranslator;

    @Autowired
    private ISampleTranslator sampleTranslator;

    @Autowired
    private IDataSetTranslator dataSetTranslator;

    @Autowired
    private IMaterialTranslator materialTranslator;

    @Override
    protected Set<MatchingEntity> shouldTranslate(TranslationContext context, Collection<MatchingEntity> inputs,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        MatchingEntityValidator validator = new MatchingEntityValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));
        return new HashSet<MatchingEntity>(validator.getValid(context.getSession().tryGetPerson(), inputs));
    }

    @Override
    protected CacheKey getObjectCacheKey(MatchingEntity input, GlobalSearchObjectFetchOptions fetchOptions)
    {
        return new CacheKey(getClass().getName() + "." + input.getEntityKind(), getObjectId(input), fetchOptions);
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<MatchingEntity> inputs, GlobalSearchObjectFetchOptions fetchOptions)
    {
        if (fetchOptions.hasExperiment() == false && fetchOptions.hasSample() == false && fetchOptions.hasDataSet() == false
                && fetchOptions.hasMaterial() == false)
        {
            return Collections.emptyMap();
        }

        List<Long> experimentIds = new LinkedList<Long>();
        List<Long> sampleIds = new LinkedList<Long>();
        List<Long> dataSetIds = new LinkedList<Long>();
        List<Long> materialIds = new LinkedList<Long>();

        for (MatchingEntity input : inputs)
        {
            switch (input.getEntityKind())
            {
                case EXPERIMENT:
                    experimentIds.add(input.getId());
                    break;
                case SAMPLE:
                    sampleIds.add(input.getId());
                    break;
                case DATA_SET:
                    dataSetIds.add(input.getId());
                    break;
                case MATERIAL:
                    materialIds.add(input.getId());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported entity kind " + input.getEntityKind());
            }
        }

        Map<Object, Object> relations = new HashMap<Object, Object>();

        if (fetchOptions.hasExperiment())
        {
            relations.put(IExperimentTranslator.class, experimentTranslator.translate(context, experimentIds, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasSample())
        {
            relations.put(ISampleTranslator.class, sampleTranslator.translate(context, sampleIds, fetchOptions.withSample()));
        }

        if (fetchOptions.hasDataSet())
        {
            relations.put(IDataSetTranslator.class, dataSetTranslator.translate(context, dataSetIds, fetchOptions.withDataSet()));
        }

        if (fetchOptions.hasMaterial())
        {
            relations.put(IMaterialTranslator.class, materialTranslator.translate(context, materialIds, fetchOptions.withMaterial()));
        }

        return relations;
    }

    @Override
    protected GlobalSearchObject createObject(TranslationContext context, MatchingEntity input, GlobalSearchObjectFetchOptions fetchOptions)
    {
        GlobalSearchObject object = new GlobalSearchObject();

        object.setObjectKind(getObjectKind(input));
        object.setObjectPermId(getObjectPermId(input));
        object.setObjectIdentifier(getObjectIdentifier(input));
        if (input.getMatches() != null)
        {
            String s = "";
            for (PropertyMatch p : input.getMatches())
            {
                s += p.getCode() + ": " + p.getValue() + "\n";
            }

            object.setMatch(s.trim());
        }
        object.setScore(input.getScore());
        object.setFetchOptions(new GlobalSearchObjectFetchOptions());

        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateObject(TranslationContext context, MatchingEntity input, GlobalSearchObject output, Object objectRelations,
            GlobalSearchObjectFetchOptions fetchOptions)
    {
        Map<Object, Object> relations = (Map<Object, Object>) objectRelations;

        if (fetchOptions.hasExperiment())
        {
            output.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());

            if (EntityKind.EXPERIMENT.equals(input.getEntityKind()))
            {
                Map<Long, Experiment> experiments = (Map<Long, Experiment>) relations.get(IExperimentTranslator.class);
                if (experiments != null)
                {
                    output.setExperiment(experiments.get(input.getId()));
                }
            }
        }

        if (fetchOptions.hasSample())
        {
            output.getFetchOptions().withSampleUsing(fetchOptions.withSample());

            if (EntityKind.SAMPLE.equals(input.getEntityKind()))
            {
                Map<Long, Sample> samples = (Map<Long, Sample>) relations.get(ISampleTranslator.class);
                if (samples != null)
                {
                    output.setSample(samples.get(input.getId()));
                }
            }
        }

        if (fetchOptions.hasDataSet())
        {
            output.getFetchOptions().withDataSetUsing(fetchOptions.withDataSet());

            if (EntityKind.DATA_SET.equals(input.getEntityKind()))
            {
                Map<Long, DataSet> dataSets = (Map<Long, DataSet>) relations.get(IDataSetTranslator.class);
                if (dataSets != null)
                {
                    output.setDataSet(dataSets.get(input.getId()));
                }
            }
        }

        if (fetchOptions.hasMaterial())
        {
            output.getFetchOptions().withMaterialUsing(fetchOptions.withMaterial());

            if (EntityKind.MATERIAL.equals(input.getEntityKind()))
            {
                Map<Long, Material> materials = (Map<Long, Material>) relations.get(IMaterialTranslator.class);
                if (materials != null)
                {
                    output.setMaterial(materials.get(input.getId()));
                }
            }
        }
    }

    private IObjectId getObjectPermId(MatchingEntity input)
    {
        switch (input.getEntityKind())
        {
            case EXPERIMENT:
                return new ExperimentPermId(input.getPermId());
            case SAMPLE:
                return new SamplePermId(input.getPermId());
            case DATA_SET:
                return new DataSetPermId(input.getCode());
            case MATERIAL:
                return new MaterialPermId(input.getCode(), input.getEntityType().getCode());
            default:
                throw new UnsupportedOperationException("Unsupported entity kind " + input.getEntityKind());
        }
    }

    private IObjectId getObjectIdentifier(MatchingEntity input)
    {
        switch (input.getEntityKind())
        {
            case EXPERIMENT:
                return new ExperimentIdentifier(input.getIdentifier());
            case SAMPLE:
                return new SampleIdentifier(input.getIdentifier());
            case DATA_SET:
                return new DataSetPermId(input.getCode());
            case MATERIAL:
                return new MaterialPermId(input.getCode(), input.getEntityType().getCode());
            default:
                throw new UnsupportedOperationException("Unsupported entity kind " + input.getEntityKind());
        }
    }

    private GlobalSearchObjectKind getObjectKind(MatchingEntity input)
    {
        switch (input.getEntityKind())
        {
            case EXPERIMENT:
                return GlobalSearchObjectKind.EXPERIMENT;
            case SAMPLE:
                return GlobalSearchObjectKind.SAMPLE;
            case DATA_SET:
                return GlobalSearchObjectKind.DATA_SET;
            case MATERIAL:
                return GlobalSearchObjectKind.MATERIAL;
            default:
                throw new UnsupportedOperationException("Unsupported entity kind " + input.getEntityKind());
        }
    }
}
