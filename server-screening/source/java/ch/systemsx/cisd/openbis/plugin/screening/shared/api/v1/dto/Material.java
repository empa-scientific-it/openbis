/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Material with properties.
 * 
 * @since 1.8
 * @author Kaloyan Enimanev
 */
@SuppressWarnings("unused")
@JsonObject("MaterialScreening")
public class Material extends MaterialIdentifier
{
    private static final long serialVersionUID = 1L;

    private Map<String, String> properties;

    private Map<String, Material> materialProperties;

    public Material(MaterialTypeIdentifier materialTypeIdentifier, String materialCode,
            Map<String, String> properties, Map<String, Material> materialProperties)
    {
        super(materialTypeIdentifier, materialCode);
        this.properties = new HashMap<String, String>(properties);
        this.materialProperties = new HashMap<String, Material>(materialProperties);
    }

    /**
     * @return the material properties
     */
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    public Map<String, Material> getMaterialProperties()
    {
        return Collections.unmodifiableMap(materialProperties);
    }

    //
    // JSON-RPC
    //

    private Material()
    {
        super(null, null);
    }

    private void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

}
