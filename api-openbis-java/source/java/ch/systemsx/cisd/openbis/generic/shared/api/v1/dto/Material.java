/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * @author jakubs
 */
@SuppressWarnings("unused")
@JsonObject("MaterialGeneric")
public class Material extends MaterialIdentifier implements IIdHolder
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Map<String, String> properties;

    private Map<String, Material> materialProperties;

    private EntityRegistrationDetails registrationDetails;

    private List<Metaproject> metaprojects;

    public static final class MaterialInitializer
    {
        private MaterialTypeIdentifier materialTypeIdentifier;

        private Long id;

        private String materialCode;

        private Map<String, String> properties;

        private Map<String, Material> materialProperties;

        private EntityRegistrationDetails registrationDetails;

        private List<Metaproject> metaprojects = new ArrayList<Metaproject>();

        public MaterialTypeIdentifier getMaterialTypeIdentifier()
        {
            return materialTypeIdentifier;
        }

        public void setMaterialTypeIdentifier(MaterialTypeIdentifier materialTypeIdentifier)
        {
            this.materialTypeIdentifier = materialTypeIdentifier;
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public String getMaterialCode()
        {
            return materialCode;
        }

        public void setMaterialCode(String materialCode)
        {
            this.materialCode = materialCode;
        }

        public Map<String, String> getProperties()
        {
            return properties;
        }

        public void setProperties(Map<String, String> properties)
        {
            this.properties = properties;
        }

        public Map<String, Material> getMaterialProperties()
        {
            return materialProperties;
        }

        public void setMaterialProperties(Map<String, Material> materialProperties)
        {
            this.materialProperties = materialProperties;
        }

        public List<Metaproject> getMetaprojects()
        {
            return metaprojects;
        }

        public void addMetaproject(Metaproject metaproject)
        {
            metaprojects.add(metaproject);
        }

        public EntityRegistrationDetails getRegistrationDetails()
        {
            return registrationDetails;
        }

        public void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
        {
            this.registrationDetails = registrationDetails;
        }

    }

    public Material(MaterialInitializer initializer)
    {
        super(initializer.getMaterialTypeIdentifier(), initializer.getMaterialCode());
        this.id = initializer.getId();
        this.properties = initializer.getProperties();
        this.materialProperties = initializer.getMaterialProperties();
        this.registrationDetails = initializer.getRegistrationDetails();
        this.metaprojects = initializer.getMetaprojects();
    }

    @Override
    @JsonIgnore
    public Long getId()
    {
        return id;
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

    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    public List<Metaproject> getMetaprojects() throws IllegalArgumentException
    {
        if (metaprojects == null)
        {
            return Collections.unmodifiableList(new ArrayList<Metaproject>());
        }
        return Collections.unmodifiableList(metaprojects);
    }

    //
    // JSON-RPC
    //

    private Material()
    {
        super(null, null);
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

    private void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

    private void setMetaProjects(List<Metaproject> metaprojects)
    {
        this.metaprojects = metaprojects;
    }
}
