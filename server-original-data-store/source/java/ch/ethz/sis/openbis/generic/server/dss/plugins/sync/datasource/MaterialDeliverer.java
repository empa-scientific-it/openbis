/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.datasource;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class MaterialDeliverer extends AbstractEntityDeliverer<Material>
{

    MaterialDeliverer(DeliveryContext context)
    {
        super(context, "material");
    }

    @Override
    protected List<Material> getAllEntities(DeliveryExecutionContext executionContext, String sessionToken)
    {

        MaterialSearchCriteria searchCriteria = new MaterialSearchCriteria();
        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.sortBy().permId();
        return context.getV3api().searchMaterials(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    @Override
    protected void deliverEntities(DeliveryExecutionContext context, List<Material> materials) throws XMLStreamException
    {
        XMLStreamWriter writer = context.getWriter();
        String sessionToken = context.getSessionToken();
        IApplicationServerApi v3api = getV3Api();
        List<MaterialPermId> permIds = materials.stream().map(Material::getPermId).collect(Collectors.toList());
        Collection<Material> fullMaterials = v3api.getMaterials(sessionToken, permIds, createFullFetchOptions()).values();
        for (Material material : fullMaterials)
        {
            startUrlElement(writer);
            String type = material.getType().getCode();
            String code = material.getCode();
            addLocation(writer, type + "/" + code, "MATERIAL");
            addLastModificationDate(writer, material.getModificationDate());
            addLink(writer, "#action=VIEW&entity=MATERIAL&code=" + code + "&type=" + type);
            startXdElement(writer);
            writer.writeAttribute("code", code);
            writer.writeAttribute("kind", "MATERIAL");
            addRegistrationDate(writer, material);
            addRegistrator(writer, material);
            addType(writer, material.getType());
            HashMap<String, Serializable> allProperties = new HashMap<>(material.getProperties());
            Map<String, Material> materialProperties = material.getMaterialProperties();
            Set<Entry<String, Material>> entrySet = materialProperties.entrySet();
            for (Entry<String, Material> entity : entrySet)
            {
                allProperties.put(entity.getKey(), entity.getValue().getPermId().toString());
            }
            addProperties(writer, allProperties, context);
            writer.writeEndElement();
            writer.writeEndElement();
        }
        operationLog.info(materials.size() + " of " + materials.size() + " materials have been delivered.");
    }

    private MaterialFetchOptions createFullFetchOptions()
    {
        MaterialFetchOptions fetchOptions = new MaterialFetchOptions();
        fetchOptions.withMaterialProperties();
        fetchOptions.withRegistrator();
        fetchOptions.withType();
        fetchOptions.withProperties();
        return fetchOptions;
    }

}
