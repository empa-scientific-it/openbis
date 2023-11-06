/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateMapValues;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateActionSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.RelationshipUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityToManyRelationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractUpdateSampleToSampleParentChildRelationExecutor
        extends AbstractUpdateEntityToManyRelationExecutor<SampleUpdate, SamplePE, ISampleId, SamplePE>
{
    @Override
    protected Collection<? extends ISampleId> getRelatedForRelationshipUpdate(IOperationContext context, SampleUpdate update)
    {
        return update.getRelationships().keySet();
    }

    @Override
    protected void updateRelationships(IOperationContext context, SampleUpdate update, SamplePE sample,
            ISampleId relatedSampleId, SamplePE relatedSample)
    {
        SamplePE child = getChild(sample, relatedSample);
        SamplePE parent = getParent(sample, relatedSample);
        SampleRelationshipPE currentRelationship = getCurrentRelationship(child, parent);
        if (currentRelationship != null)
        {
            RelationshipUpdate relationshipUpdate = update.getRelationships().get(relatedSampleId);
            Map<String, String> childAnnotations = updateAnnotations(currentRelationship.getChildAnnotations(),
                    relationshipUpdate.getChildAnnotations());
            Map<String, String> parentAnnotations = updateAnnotations(currentRelationship.getParentAnnotations(),
                    relationshipUpdate.getParentAnnotations());
            relationshipService.setSampleParentChildAnnotations(context.getSession(), child, parent,
                    childAnnotations, parentAnnotations);
        }
    }

    private SampleRelationshipPE getCurrentRelationship(SamplePE child, SamplePE parent)
    {
        Set<SampleRelationshipPE> parentRelationships = child.getParentRelationships();
        for (SampleRelationshipPE sampleRelationship : parentRelationships)
        {
            if (sampleRelationship.getParentSample() == parent)
            {
                return sampleRelationship;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> updateAnnotations(Map<String, String> currentAnnotations, ListUpdateMapValues annotations)
    {
//        if (annotations.hasActions() == false)
//        {
//            return null;
//        }
        Map<String, String> result = currentAnnotations == null ? new HashMap<>() : new HashMap<>(currentAnnotations);
        List<ListUpdateAction<Object>> actions = annotations.getActions();
        for (ListUpdateAction<Object> action : actions)
        {
            if (action instanceof ListUpdateActionAdd<?>)
            {
                Collection<Map<String, String>> items = (Collection<Map<String, String>>) action.getItems();
                for (Map<String, String> item : items)
                {
                    result.putAll(item);
                }
            } else if (action instanceof ListUpdateActionRemove<?>)
            {
                Collection<String> keys = (Collection<String>) action.getItems();
                for (String key : keys)
                {
                    result.remove(key);
                }
            } else if (action instanceof ListUpdateActionSet<?>)
            {
                Collection<Map<String, String>> items = (Collection<Map<String, String>>) action.getItems();
                for (Map<String, String> item : items)
                {
                    result = item;
                }
            }
        }
        return result;
    }

    protected abstract SamplePE getChild(SamplePE sample, SamplePE relatedSample);

    protected abstract SamplePE getParent(SamplePE sample, SamplePE relatedSample);
}
