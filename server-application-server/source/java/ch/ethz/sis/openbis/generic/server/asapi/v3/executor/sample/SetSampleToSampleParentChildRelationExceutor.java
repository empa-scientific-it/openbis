/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.SetRelationProgress;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
public abstract class SetSampleToSampleParentChildRelationExceutor extends SetSampleToSamplesRelationExecutor
{
    @Override
    protected void postSet(IOperationContext context, MapBatch<SampleCreation, SamplePE> batch,
            Map<SampleCreation, Map<ISampleId, SamplePE>> relatedMap)
    {
        Map<SamplePE, Map<SamplePE, Relationship>> parentChildRelationships = new HashMap<>();
        new MapBatchProcessor<SampleCreation, SamplePE>(context, batch)
            {
                @Override
                public void process(SampleCreation sampleCreation, SamplePE sample)
                {
                    Map<ISampleId, SamplePE> relatedSamples = relatedMap.get(sampleCreation);
                    if (relatedSamples != null)
                    {
                        Set<Entry<ISampleId, SamplePE>> entrySet = relatedSamples.entrySet();
                        for (Entry<ISampleId, SamplePE> entry : entrySet)
                        {
                            ISampleId relatedSampleId = entry.getKey();
                            SamplePE relatedSample = entry.getValue();
                            Map<? extends ISampleId, Relationship> relationships = sampleCreation.getRelationships();
                            Relationship relationship = relationships == null ? null : relationships.get(relatedSampleId);
                            if (relationship != null)
                            {
                                SamplePE child = getChild(sample, relatedSample);
                                SamplePE parent = getParent(sample, relatedSample);
                                Map<String, String> childAnnotations = getChildAnnotations(relationship);
                                if (childAnnotations != null)
                                {
                                    addChildAnnotations(getRelationship(parent, child), childAnnotations);
                                }
                                Map<String, String> parentAnnotations = getParentAnnotations(relationship);
                                if (parentAnnotations != null)
                                {
                                    addParentAnnotations(getRelationship(parent, child), parentAnnotations);
                                }
                            }
                        }
                    }
                }

                private Relationship getRelationship(SamplePE parent, SamplePE child)
                {
                    Map<SamplePE, Relationship> map = parentChildRelationships.get(parent);
                    if (map == null)
                    {
                        map = new HashMap<>();
                        parentChildRelationships.put(parent, map);
                    }
                    Relationship relationship = map.get(child);
                    if (relationship == null)
                    {
                        relationship = new Relationship();
                        map.put(child, relationship);
                    }
                    return relationship;
                }

                private void addChildAnnotations(Relationship relationship, Map<String, String> annotations)
                {
                    for (Entry<String, String> entry : annotations.entrySet())
                    {
                        relationship.addChildAnnotation(entry.getKey(), entry.getValue());
                    }
                }

                private void addParentAnnotations(Relationship relationship, Map<String, String> annotations)
                {
                    for (Entry<String, String> entry : annotations.entrySet())
                    {
                        relationship.addParentAnnotation(entry.getKey(), entry.getValue());
                    }
                }

                @Override
                public IProgress createProgress(SampleCreation key, SamplePE value, int objectIndex, int totalObjectCount)
                {
                    return new SetRelationProgress(value, key, getRelationName(), objectIndex, totalObjectCount);
                }
            };
        for (Entry<SamplePE, Map<SamplePE, Relationship>> entry : parentChildRelationships.entrySet())
        {
            SamplePE parent = entry.getKey();
            Set<Entry<SamplePE, Relationship>> entrySet = entry.getValue().entrySet();
            for (Entry<SamplePE, Relationship> entry2 : entrySet)
            {
                SamplePE child = entry2.getKey();
                Relationship relationship = entry2.getValue();
                Map<String, String> childAnnotations = relationship.getChildAnnotations();
                if (childAnnotations == null)
                {
                    childAnnotations = new HashMap<>();
                }
                Map<String, String> parentAnnotations = relationship.getParentAnnotations();
                if (parentAnnotations == null)
                {
                    parentAnnotations = new HashMap<>();
                }
                relationshipService.setSampleParentChildAnnotations(context.getSession(), child, parent,
                        childAnnotations, parentAnnotations);
            }
        }
    }

    protected abstract SamplePE getChild(SamplePE sample, SamplePE relatedSample);

    protected abstract SamplePE getParent(SamplePE sample, SamplePE relatedSample);

    protected abstract Map<String, String> getChildAnnotations(Relationship relationship);

    protected abstract Map<String, String> getParentAnnotations(Relationship relationship);

}
