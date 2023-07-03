/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;

/**
 * @author Jakub Straszewski
 */
public class MaterialGroupingDAG extends EntityGroupingDAG<NewMaterialWithType>
{
    private Map<String, Set<String>> typesWithMaterialProperty;

    private MaterialGroupingDAG(Collection<NewMaterialWithType> materials, Map<String, Set<String>> typesWithMaterialProperty)
    {
        super(materials);
        this.typesWithMaterialProperty = typesWithMaterialProperty;
    }

    /**
     * Return the new materials in the list of groups, where the earlier groups are independent to the latter ones.
     * 
     * @param materials The list of samples to create.
     * @param typesWithMaterialProperty The cache saying which properties of a certain type are materials, so that they can be used to calculate
     *            dependencies
     */
    public static List<List<NewMaterialWithType>> groupByDepencies(Collection<NewMaterialWithType> materials,
            Map<String, Set<String>> typesWithMaterialProperty)
    {
        if (materials.size() == 0)
        {
            return Collections.emptyList();
        }

        MaterialGroupingDAG dag = new MaterialGroupingDAG(materials, typesWithMaterialProperty);
        return dag.getDependencyGroups();
    }

    @Override
    public String getCode(NewMaterialWithType material)
    {
        return MaterialIdentifier.toString(material.getCode(), material.getType());
    }

    @Override
    public Collection<String> getDependent(NewMaterialWithType sample)
    {
        return null;
    }

    @Override
    public Collection<String> getDependencies(NewMaterialWithType newMaterial)
    {
        LinkedList<String> relatedMaterials = null;
        IEntityProperty[] properties = newMaterial.getProperties();

        for (IEntityProperty iEntityProperty : properties)
        {
            if (relatedMaterials == null)
            {
                relatedMaterials = new LinkedList<String>();
            }

            if (typesWithMaterialProperty.get(newMaterial.getType()).contains(iEntityProperty.getPropertyType().getCode()))
            {
                relatedMaterials.add(iEntityProperty.getStringValue());
            }
        }
        return relatedMaterials;
    }
}
