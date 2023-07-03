/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.PropertyValueDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * Immutable value object representing an experiment.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("Experiment")
public final class Experiment implements Serializable, IIdentifierHolder, IIdHolder
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new experiment instance. Necessary since all the fields of a sample are final.
     * <p>
     * All of the properties must be filled (non-null) before being used to initialize an Experiment, otherwise the Experiment constructor will throw
     * an exception.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class ExperimentInitializer
    {
        private Long id;

        private String permId;

        private String code;

        private String identifier;

        private String experimentTypeCode;

        private EntityRegistrationDetails registrationDetails;

        private boolean isStub;

        private HashMap<String, Serializable> properties = new HashMap<>();

        private List<Metaproject> metaprojects = new ArrayList<Metaproject>();

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getId()
        {
            return id;
        }

        public void setPermId(String permId)
        {
            this.permId = permId;
        }

        public String getPermId()
        {
            return permId;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public String getIdentifier()
        {
            return identifier;
        }

        public void setIdentifier(String identifier)
        {
            this.identifier = identifier;
        }

        public void setExperimentTypeCode(String experimentTypeCode)
        {
            this.experimentTypeCode = experimentTypeCode;
        }

        public String getExperimentTypeCode()
        {
            return experimentTypeCode;
        }

        public HashMap<String, Serializable> getProperties()
        {
            return properties;
        }

        public void putProperty(String propCode, String value)
        {
            if(properties.containsKey(propCode)) {
                properties.put(propCode, properties.get(propCode) + ", " + value);
            } else
            {
                properties.put(propCode, value);
            }
        }

        public List<Metaproject> getMetaprojects()
        {
            return metaprojects;
        }

        public void addMetaproject(Metaproject metaproject)
        {
            metaprojects.add(metaproject);
        }

        public void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
        {
            this.registrationDetails = registrationDetails;
        }

        public EntityRegistrationDetails getRegistrationDetails()
        {
            return registrationDetails;
        }

        public void setIsStub(boolean isStub)
        {
            this.isStub = isStub;
        }
    }

    private Long id;

    private String permId;

    private String code;

    private String identifier;

    private String experimentTypeCode;

    private EntityRegistrationDetails registrationDetails;

    @JsonDeserialize(contentUsing = PropertyValueDeserializer.class)
    private HashMap<String, Serializable> properties;

    private List<Metaproject> metaprojects;

    private boolean isStub;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Experiment(ExperimentInitializer initializer)
    {
        InitializingChecks.checkValidLong(initializer.getId(), "Unspecified id.");
        this.id = initializer.getId();

        InitializingChecks.checkValidString(initializer.getPermId(), "Unspecified permanent id.");
        this.permId = initializer.getPermId();

        if (initializer.isStub)
        {
            this.isStub = true;
        } else
        {
            InitializingChecks.checkValidString(initializer.getCode(), "Unspecified code.");
            this.code = initializer.getCode();

            InitializingChecks.checkValidString(initializer.getIdentifier(),
                    "Unspecified identifier.");
            this.identifier = initializer.getIdentifier();

            InitializingChecks.checkValidString(initializer.getExperimentTypeCode(),
                    "Unspecified experiment type code.");
            this.experimentTypeCode = initializer.getExperimentTypeCode();

            this.metaprojects = initializer.getMetaprojects();

            InitializingChecks.checkValidRegistrationDetails(initializer.getRegistrationDetails(),
                    "Unspecified entity registration details.");
            this.registrationDetails = initializer.getRegistrationDetails();

        }

        this.properties = initializer.getProperties();
    }

    /**
     * Returns the experiment id.
     */
    @Override
    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the experiment permanent id.
     */
    public String getPermId()
    {
        return permId;
    }

    /**
     * Returns the experiment code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the experiment identifier.
     */
    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Returns the experiment type code.
     */
    public String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    public Map<String, Serializable> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    public List<Metaproject> getMetaprojects() throws IllegalArgumentException
    {
        if (metaprojects == null)
        {
            return Collections.unmodifiableList(new ArrayList<Metaproject>());
        }
        return Collections.unmodifiableList(metaprojects);
    }

    public boolean isStub()
    {
        return isStub;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Experiment == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        Experiment other = (Experiment) obj;
        builder.append(getId(), other.getId());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        if (isStub())
        {
            builder.append("STUB");
            builder.append(getPermId());
        } else
        {
            builder.append(getIdentifier());
            builder.append(getExperimentTypeCode());
            builder.append(getProperties());
        }
        return builder.toString();
    }

    //
    // JSON-RPC
    //
    private Experiment()
    {
    }

    @JsonIgnore
    private void setId(Long id)
    {
        this.id = id;
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

    private void setPermId(String permId)
    {
        this.permId = permId;
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    private void setExperimentTypeCode(String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

    private void setProperties(HashMap<String, Serializable> properties)
    {
        this.properties = properties;
    }

    private void setStub(boolean isStub)
    {
        this.isStub = isStub;
    }

    @JsonProperty("metaprojects")
    private void setMetaprojectsJson(List<Metaproject> metaprojects)
    {
        this.metaprojects = metaprojects;
    }

}
