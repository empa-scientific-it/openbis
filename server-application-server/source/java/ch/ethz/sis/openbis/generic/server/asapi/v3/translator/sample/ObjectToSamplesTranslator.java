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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToManyRelationTranslator;

/**
 * @author pkupczyk
 */
public abstract class ObjectToSamplesTranslator extends ObjectToManyRelationTranslator<SampleWithAnnotations, SampleFetchOptions> implements
        IObjectToSamplesTranslator
{

    @Autowired
    private ISampleTranslator sampleTranslator;

    @Override
    protected Map<Long, SampleWithAnnotations> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            SampleFetchOptions relatedFetchOptions)
    {
        Map<Long, SampleWithAnnotations> result = new LinkedHashMap<Long, SampleWithAnnotations>();
        Map<Long, Sample> map = sampleTranslator.translate(context, relatedIds, relatedFetchOptions);
        for (Entry<Long, Sample> entry : map.entrySet())
        {
            result.put(entry.getKey(), new SampleWithAnnotations(entry.getValue()));
        }
        return result;
    }

    @Override
    protected Collection<SampleWithAnnotations> createCollection()
    {
        return new ArrayList<>();
    }

    @Override
    protected void injectAnnotations(Long objectId, SampleWithAnnotations relatedObject, String annotations, String relatedAnnotations)
    {
        relatedObject.setAnnotations(objectId, annotations);
        relatedObject.setRelatedAnnotations(objectId, relatedAnnotations);
    }

}
