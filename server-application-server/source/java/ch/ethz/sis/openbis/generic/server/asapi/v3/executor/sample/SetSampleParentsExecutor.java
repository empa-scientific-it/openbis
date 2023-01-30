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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleParentsExecutor extends SetSampleToSampleParentChildRelationExceutor implements
        ISetSampleParentsExecutor
{

    @Override
    protected String getRelationName()
    {
        return "sample-parents";
    }

    @Override
    protected Collection<? extends ISampleId> getRelatedIds(IOperationContext context, SampleCreation creation)
    {
        return creation.getParentIds();
    }

    @Override
    protected void setRelated(IOperationContext context, SamplePE child, Collection<SamplePE> parents)
    {
        Set<SamplePE> existingParents = new HashSet<SamplePE>(child.getParents());

        for (SamplePE parent : parents)
        {
            if (false == existingParents.contains(parent))
            {
                relationshipService.addParentToSample(context.getSession(), child, parent);
                existingParents.add(parent);
            }
        }
    }

    @Override
    protected SamplePE getChild(SamplePE sample, SamplePE relatedSample)
    {
        return sample;
    }

    @Override
    protected SamplePE getParent(SamplePE sample, SamplePE relatedSample)
    {
        return relatedSample;
    }

    @Override
    protected Map<String, String> getChildAnnotations(Relationship relationship)
    {
        return relationship.getChildAnnotations();
    }

    @Override
    protected Map<String, String> getParentAnnotations(Relationship relationship)
    {
        return relationship.getParentAnnotations();
    }
}
