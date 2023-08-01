/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.material;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.material.Material")
public class Material implements Serializable, ICodeHolder, IEntityTypeHolder, IMaterialPropertiesHolder, IModificationDateHolder, IPermIdHolder, IPropertiesHolder, IRegistrationDateHolder, IRegistratorHolder, ITagsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private MaterialFetchOptions fetchOptions;

    @JsonProperty
    private MaterialPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private MaterialType type;

    @JsonProperty
    private List<HistoryEntry> history;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    @JsonDeserialize(contentUsing = PropertiesDeserializer.class)
    private Map<String, Serializable> properties;

    @JsonProperty
    private Map<String, Material> materialProperties;

    @JsonProperty
    private Set<Tag> tags;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public MaterialFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(MaterialFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public MaterialPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with DtoGenerator
    public void setPermId(MaterialPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public MaterialType getType()
    {
        if (getFetchOptions() != null && getFetchOptions().hasType())
        {
            return type;
        }
        else
        {
            throw new NotFetchedException("Material type has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setType(MaterialType type)
    {
        this.type = type;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public List<HistoryEntry> getHistory()
    {
        if (getFetchOptions() != null && getFetchOptions().hasHistory())
        {
            return history;
        }
        else
        {
            throw new NotFetchedException("History have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setHistory(List<HistoryEntry> history)
    {
        this.history = history;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions() != null && getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with DtoGenerator
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Map<String, Serializable> getProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    @Override
    public void setProperties(Map<String, Serializable> properties)
    {
        this.properties = properties;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Map<String, Material> getMaterialProperties()
    {
        if (getFetchOptions() != null && getFetchOptions().hasMaterialProperties())
        {
            return materialProperties;
        }
        else
        {
            throw new NotFetchedException("Material Properties have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    @Override
    public void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public Set<Tag> getTags()
    {
        if (getFetchOptions() != null && getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw new NotFetchedException("Tags have not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    @Override
    public String getProperty(String propertyName)
    {
        return getProperties() != null ? (String) getProperties().get(propertyName) : null;
    }

    @Override
    public void setProperty(String propertyName, Serializable propertyValue)
    {
        if (properties == null)
        {
            properties = new HashMap<>();
        }
        properties.put(propertyName, propertyValue);
    }

    @Override
    public Material getMaterialProperty(String propertyName)
    {
        return getMaterialProperties() != null ? getMaterialProperties().get(propertyName) : null;
    }

    @Override
    public void setMaterialProperty(String propertyName, Material propertyValue)
    {
        if (materialProperties == null)
        {
            materialProperties = new HashMap<String, Material>();
        }
        materialProperties.put(propertyName, propertyValue);
    }

    @Override
    public Long getIntegerProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : Long.parseLong(propertyValue);
    }

    @Override
    public void setIntegerProperty(String propertyName, Long propertyValue)
    {
        setProperty(propertyName, Objects.toString(propertyValue, null));
    }

    @Override
    public String getVarcharProperty(String propertyName)
    {
        return getProperty(propertyName);
    }

    @Override
    public void setVarcharProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public String getMultilineVarcharProperty(String propertyName)
    {
        return getProperty(propertyName);
    }

    @Override
    public void setMultilineVarcharProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public Double getRealProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : Double.parseDouble(propertyValue);
    }

    @Override
    public void setRealProperty(String propertyName, Double propertyValue)
    {
        setProperty(propertyName, Objects.toString(propertyValue, null));
    }

    @Override
    public ZonedDateTime getTimestampProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return propertyValue == null ? null : ZonedDateTime.parse(getProperty(propertyName));
    }

    @Override
    public void setTimestampProperty(String propertyName, ZonedDateTime propertyValue)
    {
        String value = (propertyValue == null) ? null : propertyValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"));
        setProperty(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : Boolean.parseBoolean(propertyValue);
    }

    @Override
    public void setBooleanProperty(String propertyName, Boolean propertyValue)
    {
        setProperty(propertyName, Objects.toString(propertyValue, null));
    }

    @Override
    public String getHyperlinkProperty(String propertyName)
    {
        return getProperty(propertyName);
    }

    @Override
    public void setHyperlinkProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public String getXmlProperty(String propertyName)
    {
        return getProperty(propertyName);
    }

    @Override
    public void setXmlProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public String[] getControlledVocabularyProperty(String propertyName)
    {
        if(getProperties() == null || getProperties().get(propertyName) == null) {
            return null;
        }
        Serializable value = getProperties().get(propertyName);
        if(value.getClass().isArray()) {
            Serializable[] values = (Serializable[]) value;
            return Arrays.stream(values).map(x->(String)x).toArray(String[]::new);
        } else {
            String propertyValue = (String) value;
            return new String[]{ propertyValue };
        }
    }

    @Override
    public void setControlledVocabularyProperty(String propertyName, String[] propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public SamplePermId[] getSampleProperty(String propertyName)
    {
        if(getProperties() == null || getProperties().get(propertyName) == null) {
            return null;
        }
        Serializable value = getProperties().get(propertyName);
        if(value.getClass().isArray()) {
            Serializable[] values = (Serializable[]) value;
            return Arrays.stream(values).map(x -> new SamplePermId((String)x)).toArray(SamplePermId[]::new);
        } else {
            String propertyValue = (String) value;
            return new SamplePermId[]{new SamplePermId(propertyValue)};
        }
    }

    @Override
    public void setSampleProperty(String propertyName, SamplePermId[] propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : Arrays.stream(propertyValue)
                .map(ObjectPermId::getPermId)
                .toArray(String[]::new));
    }

    @Override
    public Long[] getIntegerArrayProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : Arrays.stream(propertyValue.split(",")).map(String::trim).map(Long::parseLong).toArray(Long[]::new);
    }

    @Override
    public void setIntegerArrayProperty(String propertyName, Long[] propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : Arrays.stream(propertyValue).map(Object::toString).reduce((a,b) -> a + ", " + b).get());
    }

    @Override
    public Double[] getRealArrayProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : Arrays.stream(propertyValue.split(",")).map(String::trim).map(Double::parseDouble).toArray(Double[]::new);
    }

    @Override
    public void setRealArrayProperty(String propertyName, Double[] propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : Arrays.stream(propertyValue).map(Object::toString).reduce((a,b) -> a + ", " + b).get());
    }

    @Override
    public String[] getStringArrayProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : Arrays.stream(propertyValue.split(",")).map(String::trim).toArray(String[]::new);
    }

    @Override
    public void setStringArrayProperty(String propertyName, String[] propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : Arrays.stream(propertyValue).reduce((a,b) -> a + ", " + b).get());
    }

    @Override
    public ZonedDateTime[] getTimestampArrayProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return propertyValue == null ? null : Arrays.stream(propertyValue.split(","))
             .map(String::trim)
             .map(dateTime -> ZonedDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X")))
             .toArray(ZonedDateTime[]::new);
    }

    @Override
    public void setTimestampArrayProperty(String propertyName, ZonedDateTime[] propertyValue)
    {
        String value = (propertyValue == null) ? null : Arrays.stream(propertyValue)
                 .map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")))
                 .reduce((a,b) -> a + ", " + b)
                 .get();
        setProperty(propertyName, value);
    }

    @Override
    public String getJsonProperty(String propertyName)
    {
        return getProperty(propertyName);
    }

    @Override
    public void setJsonProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "Material " + code;
    }

}
