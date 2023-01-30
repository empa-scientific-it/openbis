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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;

/**
 * @author Jakub Straszewski
 */
@Component
public class DeleteMaterialExecutor extends AbstractDeleteEntityExecutor<Void, IMaterialId, MaterialPE, MaterialDeletionOptions> implements
        IDeleteMaterialExecutor
{
    private static final Comparator<Entry<Integer, List<TechId>>> INVERSE_LEVEL_COMPARATOR =
            new SimpleComparator<Entry<Integer, List<TechId>>, Integer>()
                {
                    @Override
                    public Integer evaluate(Entry<Integer, List<TechId>> item)
                    {
                        return -item.getKey();
                    }
                };

    @Autowired
    private IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Autowired
    private IMaterialAuthorizationExecutor authorizationExecutor;

    @Override
    protected Map<IMaterialId, MaterialPE> map(IOperationContext context, List<? extends IMaterialId> entityIds,
            MaterialDeletionOptions deletionOptions)
    {
        return mapMaterialByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IMaterialId entityId, MaterialPE entity)
    {
        authorizationExecutor.canDelete(context, entityId, entity);
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, MaterialPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<MaterialPE> materials, MaterialDeletionOptions deletionOptions)
    {
        Map<String, String> links = createLinks(materials);
        List<Entry<Integer, List<TechId>>> entries = orderMaterialsByLevel(materials, links);
        IMaterialTable materialTable = businessObjectFactory.createMaterialTable(context.getSession());
        for (Entry<Integer, List<TechId>> entry : entries)
        {
            materialTable.deleteByTechIds(entry.getValue(), deletionOptions.getReason());
        }
        return null;
    }

    private Map<String, String> createLinks(Collection<MaterialPE> materials)
    {
        Map<String, String> links = new HashMap<>();
        for (MaterialPE material : materials)
        {
            Set<MaterialPropertyPE> properties = material.getProperties();
            for (MaterialPropertyPE property : properties)
            {
                MaterialPE materialProperty = property.getMaterialValue();
                if (materialProperty != null)
                {
                    links.put(material.getPermId(), materialProperty.getPermId());

                }
            }
        }
        return links;
    }

    private List<Entry<Integer, List<TechId>>> orderMaterialsByLevel(Collection<MaterialPE> materials,
            Map<String, String> links)
    {
        Map<Integer, List<TechId>> levelMap = new HashMap<>();
        for (MaterialPE material : materials)
        {
            int level = getLevel(material.getPermId(), links, 0);
            List<TechId> ids = levelMap.get(level);
            if (ids == null)
            {
                ids = new ArrayList<>();
                levelMap.put(level, ids);
            }
            ids.add(new TechId(material));
        }
        List<Entry<Integer, List<TechId>>> entries = new ArrayList<>(levelMap.entrySet());
        Collections.sort(entries, INVERSE_LEVEL_COMPARATOR);
        return entries;
    }

    private int getLevel(String material, Map<String, String> links, int level)
    {
        String nextMaterial = links.get(material);
        if (nextMaterial == null)
        {
            return level;
        }
        return getLevel(nextMaterial, links, level + 1);
    }

}
