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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Immutable value object representing a sample.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("Sample")
public final class Sample implements Serializable, IIdentifierHolder, IIdHolder
{
    private static final long serialVersionUID = 1L;

    private static class Reference
    {
        private final Long id;

        private final Map<Long, Sample> repository;

        Reference(Long id, Map<Long, Sample> repository)
        {
            this.id = id;
            this.repository = repository;
        }

        Sample resolve()
        {
            return repository.get(id);
        }
    }

    /**
     * Class used to initialize a new sample instance. Necessary since all the fields of a sample are final.
     * <p>
     * All of the properties must be filled (non-null) before being used to initialize a Sample, otherwise the Sample constructor will throw an
     * exception.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class SampleInitializer
    {
        private Long id;

        private String spaceCode;

        private String permId;

        private String code;

        private String identifier;

        private String experimentIdentifierOrNull;

        private Long sampleTypeId;

        private String sampleTypeCode;

        private HashMap<String, String> properties = new HashMap<String, String>();

        private List<Metaproject> metaprojects = new ArrayList<Metaproject>();

        private EntityRegistrationDetails registrationDetails;

        private EnumSet<SampleFetchOption> retrievedFetchOptions = EnumSet
                .noneOf(SampleFetchOption.class);

        private List<Reference> parentReferences;

        private List<Sample> parents = Collections.emptyList();

        private List<Reference> childReferences;

        private List<Sample> children = Collections.emptyList();

        private boolean isStub;

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getId()
        {
            return id;
        }

        public void setSpaceCode(String spaceCode)
        {
            this.spaceCode = spaceCode;
        }

        public String getSpaceCode()
        {
            return spaceCode;
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

        public String getExperimentIdentifierOrNull()
        {
            return experimentIdentifierOrNull;
        }

        public void setExperimentIdentifierOrNull(String experimentIdentifierOrNull)
        {
            this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        }

        public void setSampleTypeId(Long sampleTypeId)
        {
            this.sampleTypeId = sampleTypeId;
        }

        public Long getSampleTypeId()
        {
            return sampleTypeId;
        }

        public void setSampleTypeCode(String sampleTypeCode)
        {
            this.sampleTypeCode = sampleTypeCode;
        }

        public String getSampleTypeCode()
        {
            return sampleTypeCode;
        }

        public HashMap<String, String> getProperties()
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

        public void setRetrievedFetchOptions(EnumSet<SampleFetchOption> retrievedFetchOptions)
        {
            this.retrievedFetchOptions =
                    (null == retrievedFetchOptions) ? EnumSet.noneOf(SampleFetchOption.class)
                            : retrievedFetchOptions;
        }

        public EnumSet<SampleFetchOption> getRetrievedFetchOptions()
        {
            return retrievedFetchOptions;
        }

        public void setParents(List<Sample> parents)
        {
            this.parents = (null == parents) ? new ArrayList<Sample>() : parents;
        }

        public List<Sample> getParents()
        {
            return parents;
        }

        public List<Sample> getChildren()
        {
            return children;
        }

        public void setChildren(List<Sample> children)
        {
            this.children = (null == children) ? new ArrayList<Sample>() : children;
        }

        public void setParentReferences(List<Long> parentIDs, Map<Long, Sample> repository)
        {
            parentReferences = new ArrayList<Reference>();
            for (Long parentID : parentIDs)
            {
                parentReferences.add(new Reference(parentID, repository));
            }
        }

        public void setChildReferences(List<Long> childIDs, Map<Long, Sample> repository)
        {
            childReferences = new ArrayList<Reference>();
            for (Long childID : childIDs)
            {
                childReferences.add(new Reference(childID, repository));
            }
        }

        public void setStub(boolean isStub)
        {
            this.isStub = isStub;
        }
    }

    private Long id;

    private String spaceCode;

    private String permId;

    private String code;

    private String identifier;

    private String experimentIdentifierOrNull;

    private Long sampleTypeId;

    private String sampleTypeCode;

    private HashMap<String, String> properties;

    private EntityRegistrationDetails registrationDetails;

    private EnumSet<SampleFetchOption> retrievedFetchOptions;

    private List<Reference> parentReferences = Collections.emptyList();

    private List<Sample> parents;

    private List<Reference> childReferences = Collections.emptyList();

    private List<Sample> children;

    private boolean isStub;

    private List<Metaproject> metaprojects;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public Sample(SampleInitializer initializer)
    {
        InitializingChecks.checkValidLong(initializer.getId(), "Unspecified id.");
        this.id = initializer.getId();

        this.spaceCode = initializer.getSpaceCode();

        InitializingChecks.checkValidString(initializer.getPermId(), "Unspecified permanent id.");
        this.permId = initializer.getPermId();

        this.retrievedFetchOptions = initializer.getRetrievedFetchOptions();

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

            this.experimentIdentifierOrNull = initializer.getExperimentIdentifierOrNull();

            InitializingChecks.checkValidLong(initializer.getSampleTypeId(),
                    "Unspecified sample type id.");
            this.sampleTypeId = initializer.getSampleTypeId();

            InitializingChecks.checkValidString(initializer.getSampleTypeCode(),
                    "Unspecified sample type code.");
            this.sampleTypeCode = initializer.getSampleTypeCode();

            this.properties = initializer.getProperties();

            this.metaprojects = initializer.getMetaprojects();

            InitializingChecks.checkValidRegistrationDetails(initializer.getRegistrationDetails(),
                    "Unspecified entity registration details.");
            this.registrationDetails = initializer.getRegistrationDetails();

            if (initializer.parentReferences != null)
            {
                this.parentReferences = initializer.parentReferences;
            } else
            {
                this.parents = initializer.getParents();
            }
            if (initializer.childReferences != null)
            {
                this.childReferences = initializer.childReferences;
            } else
            {
                this.children = initializer.getChildren();
            }
        }
    }

    /**
     * Returns the sample id.
     */
    @Override
    @JsonIgnore
    public Long getId()
    {
        return id;
    }

    /**
     * Returns the space code.
     */
    public String getSpaceCode()
    {
        return spaceCode;
    }

    /**
     * Returns the sample permanent id.
     */
    public String getPermId()
    {
        return permId;
    }

    /**
     * Returns the sample code.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the sample identifier;
     */
    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    public String getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    /**
     * Returns the sample type id.
     */
    @JsonIgnore
    public Long getSampleTypeId()
    {
        return sampleTypeId;
    }

    /**
     * Returns the sample type code.
     */
    public String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    @JsonIgnore
    public Map<String, String> getProperties() throws IllegalArgumentException
    {
        if (getRetrievedFetchOptions().contains(SampleFetchOption.PROPERTIES))
        {
            return Collections.unmodifiableMap(properties);
        } else
        {
            throw new IllegalArgumentException("Properties were not retrieved for sample "
                    + getIdentifier() + ".");
        }
    }

    @JsonIgnore
    public List<Metaproject> getMetaprojects() throws IllegalArgumentException
    {
        if (getRetrievedFetchOptions().contains(SampleFetchOption.METAPROJECTS))
        {
            return Collections.unmodifiableList(metaprojects);
        } else
        {
            throw new IllegalArgumentException("Metaprojects were not retrieved for sample "
                    + getIdentifier() + ".");
        }
    }

    /**
     * Return the sample registration details.
     * 
     * @since 1.11
     */
    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    /**
     * Returns fetch options used to retrieved this sample object.
     */
    public EnumSet<SampleFetchOption> getRetrievedFetchOptions()
    {
        return retrievedFetchOptions;
    }

    /**
     * Return the children.
     * 
     * @return Children of this sample or an empty list if there are no children.
     * @throws IllegalArgumentException Thrown if the children were not retrieved from the server.
     */
    @JsonIgnore
    public List<Sample> getChildren() throws IllegalArgumentException
    {
        if (getRetrievedFetchOptions().contains(SampleFetchOption.CHILDREN))
        {
            if (children == null)
            {
                children = new ArrayList<Sample>();
                for (Reference reference : childReferences)
                {
                    Sample child = reference.resolve();
                    if (child != null)
                    {
                        children.add(child);
                    }
                }
                childReferences = null;
            }

            return Collections.unmodifiableList(children);
        } else
        {
            throw new IllegalArgumentException("Children were not retrieved for sample "
                    + getIdentifier() + ".");
        }
    }

    /**
     * Return the parents.
     * 
     * @return All parents of this sample or an empty list if there are no parents.
     * @throws IllegalArgumentException Thrown if the parents were not retrieved from the server.
     */
    @JsonIgnore
    public List<Sample> getParents() throws IllegalArgumentException
    {
        if (getRetrievedFetchOptions().contains(SampleFetchOption.PARENTS))
        {
            if (parents == null)
            {
                parents = new ArrayList<Sample>();
                for (Reference reference : parentReferences)
                {
                    Sample parent = reference.resolve();
                    if (parent != null)
                    {
                        parents.add(parent);
                    }
                }
                parentReferences = null;
            }
            return Collections.unmodifiableList(parents);
        } else
        {
            throw new IllegalArgumentException("Parents were not retrieved for sample "
                    + getIdentifier() + ".");
        }
    }

    public boolean isStub()
    {
        return this.isStub;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Sample == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        Sample other = (Sample) obj;
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
            builder.append(getSampleTypeCode());
            if (retrievedFetchOptions.contains(SampleFetchOption.PROPERTIES))
            {
                builder.append(getProperties());
            } else
            {
                builder.append("properties=?");
            }
            if (retrievedFetchOptions.contains(SampleFetchOption.PARENTS))
            {
                builder.append("parents", getParents());
            } else
            {
                builder.append("parents=?");
            }
            if (retrievedFetchOptions.contains(SampleFetchOption.CHILDREN))
            {
                builder.append("children", getChildren());
            } else
            {
                builder.append("children=?");
            }
        }
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private Sample()
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

    private void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
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

    private void setExperimentIdentifierOrNull(String experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    @JsonIgnore
    private void setSampleTypeId(Long sampleTypeId)
    {
        this.sampleTypeId = sampleTypeId;
    }

    @JsonProperty("sampleTypeId")
    private String getSampleTypeIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(sampleTypeId);
    }

    private void setSampleTypeIdAsString(String sampleTypeId)
    {
        this.sampleTypeId = JsonPropertyUtil.toLongOrNull(sampleTypeId);
    }

    private void setSampleTypeCode(String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }

    @JsonProperty("properties")
    public Map<String, String> getPropertiesJson()
    {
        return retrievedFetchOptions.contains(SampleFetchOption.PROPERTIES) ? properties : null;
    }

    @JsonProperty("properties")
    private void setProperties(HashMap<String, String> properties)
    {
        this.properties = properties;
    }

    @JsonProperty("metaprojects")
    public List<Metaproject> getMetaprojectsJson()
    {
        return retrievedFetchOptions.contains(SampleFetchOption.METAPROJECTS) ? metaprojects : null;
    }

    @JsonProperty("metaprojects")
    private void setMetaprojectsJson(List<Metaproject> metaprojects)
    {
        this.metaprojects = metaprojects;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }

    private void setRetrievedFetchOptions(EnumSet<SampleFetchOption> fetchOptions)
    {
        this.retrievedFetchOptions = fetchOptions;
    }

    @JsonProperty("children")
    public List<Sample> getChildrenJson()
    {
        return retrievedFetchOptions.contains(SampleFetchOption.CHILDREN) ? children : null;
    }

    @JsonProperty("parents")
    public List<Sample> getParentsJson()
    {
        return retrievedFetchOptions.contains(SampleFetchOption.PARENTS) ? parents : null;
    }

    @JsonProperty("parents")
    private void setParents(List<Sample> parents)
    {
        this.parents = parents;
    }

    @JsonProperty("children")
    private void setChildren(List<Sample> children)
    {
        this.children = children;
    }

    private void setStub(boolean isStub)
    {
        this.isStub = isStub;
    }
}
