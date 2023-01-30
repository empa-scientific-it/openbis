/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The <i>GWT</i> counterpart to ch.systemsx.cisd.openbis.generic.shared.api.v1.SearchableEntityKind.
 * 
 * @author Piotr Buczek
 */
public enum AssociatedEntityKind implements Serializable
{
    SAMPLE("Sample", EntityKind.SAMPLE, EnumSet.of(EntityKind.DATA_SET)),

    EXPERIMENT("Experiment", EntityKind.EXPERIMENT, EnumSet.of(EntityKind.SAMPLE,
            EntityKind.DATA_SET)),

    DATA_SET("Data Set", EntityKind.DATA_SET, EnumSet.noneOf(EntityKind.class)),

    DATA_SET_PARENT("Parent", EntityKind.DATA_SET, EnumSet.of(EntityKind.DATA_SET)),

    DATA_SET_CHILD("Child", EntityKind.DATA_SET, EnumSet.of(EntityKind.DATA_SET)),

    DATA_SET_CONTAINER("Container", EntityKind.DATA_SET, EnumSet.of(EntityKind.DATA_SET)),

    SAMPLE_CONTAINER("Container", EntityKind.SAMPLE, EnumSet.of(EntityKind.SAMPLE)),

    SAMPLE_CHILD("Child", EntityKind.SAMPLE, EnumSet.of(EntityKind.SAMPLE)),

    SAMPLE_PARENT("Parent", EntityKind.SAMPLE, EnumSet.of(EntityKind.SAMPLE)),

    MATERIAL("Material", EntityKind.MATERIAL, EnumSet.noneOf(EntityKind.class));

    private final String description;

    private final EntityKind entityKind;

    private final Set<EntityKind> sourceEntityKinds;

    private AssociatedEntityKind(final String description, final EntityKind entityKind,
            final Set<EntityKind> sourceEntityKinds)
    {
        this.description = description;
        this.entityKind = entityKind;
        this.sourceEntityKinds = sourceEntityKinds;
    }

    public final String getDescription()
    {
        return description;
    }

    public final EntityKind getEntityKind()
    {
        return entityKind;
    }

    public static List<AssociatedEntityKind> getAssociatedEntityKinds(EntityKind sourceEntity)
    {
        List<AssociatedEntityKind> result = new ArrayList<AssociatedEntityKind>();
        for (AssociatedEntityKind associatedEntityKind : values())
        {
            if (associatedEntityKind.sourceEntityKinds.contains(sourceEntity))
            {
                result.add(associatedEntityKind);
            }
        }
        return result;
    }

}
