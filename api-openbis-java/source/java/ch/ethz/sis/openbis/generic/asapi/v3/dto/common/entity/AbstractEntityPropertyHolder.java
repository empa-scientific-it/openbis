/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.entity;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@JsonObject("as.dto.common.entity.AbstractEntityPropertyHolder")
public abstract class AbstractEntityPropertyHolder implements Serializable, IPropertiesHolder
{
    @JsonProperty
    @JsonDeserialize(contentUsing = PropertiesDeserializer.class)
    protected Map<String, Serializable> properties = new HashMap<>();

    @JsonIgnore
    public abstract Map<String, Serializable> getProperties();

    @JsonIgnore
    public abstract void setProperties(Map<String, Serializable> properties);

    @Override
    public Serializable getProperty(String propertyName) // String!!!
    {
        return getProperties() != null ?
                PropertiesDeserializer.getPropertyAsString(getProperties().get(propertyName)) :
                null;
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
    public Long getIntegerProperty(String propertyName)
    {
        String propertyValue = (String) getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ?
                null :
                Long.parseLong(propertyValue);
    }

    @Override
    public void setIntegerProperty(String propertyName, Long propertyValue)
    {
        setProperty(propertyName, Objects.toString(propertyValue, null));
    }

    @Override
    public String getVarcharProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setVarcharProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public String getMultilineVarcharProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setMultilineVarcharProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public Double getRealProperty(String propertyName)
    {
        String propertyValue = (String) getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ?
                null :
                Double.parseDouble(propertyValue);

    }

    @Override
    public void setRealProperty(String propertyName, Double propertyValue)
    {
        setProperty(propertyName, Objects.toString(propertyValue, null));
    }

    @Override
    public ZonedDateTime getTimestampProperty(String propertyName)
    {
        String propertyValue = (String) getProperty(propertyName);
        return propertyValue == null ? null : ZonedDateTime.parse(propertyValue);
    }

    @Override
    public void setTimestampProperty(String propertyName, ZonedDateTime propertyValue)
    {
        String value = (propertyValue == null) ?
                null :
                propertyValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"));
        setProperty(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName)
    {
        String propertyValue = (String) getProperty(propertyName);
        return (propertyValue == null || propertyValue.trim().isEmpty()) ?
                null :
                Boolean.parseBoolean(propertyValue);

    }

    @Override
    public void setBooleanProperty(String propertyName, Boolean propertyValue)
    {
        setProperty(propertyName, Objects.toString(propertyValue, null));
    }

    @Override
    public String[] getControlledVocabularyProperty(String propertyName)
    {
        if (getProperties() == null || getProperties().get(propertyName) == null)
        {
            return null;
        }
        Serializable value = getProperties().get(propertyName);
        if (value.getClass().isArray())
        {
            Serializable[] values = (Serializable[]) value;
            return Arrays.stream(values).map(x -> (String) x).toArray(String[]::new);
        } else
        {
            String propertyValue = (String) value;
            return new String[] { propertyValue };
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
        if (getProperties() == null || getProperties().get(propertyName) == null)
        {
            return null;
        }
        Serializable value = getProperties().get(propertyName);
        if (value.getClass().isArray())
        {
            Serializable[] values = (Serializable[]) value;
            return Arrays.stream(values).map(x -> new SamplePermId((String) x))
                    .toArray(SamplePermId[]::new);
        } else
        {
            String propertyValue = (String) value;
            return new SamplePermId[] { new SamplePermId(propertyValue) };
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
    public String getHyperlinkProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setHyperlinkProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public String getXmlProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setXmlProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public Long[] getIntegerArrayProperty(String propertyName)
    {
        Serializable[] propertyValues =
                getProperties() != null ? (Serializable[]) getProperties().get(propertyName) : null;
        if (propertyValues == null)
        {
            return null;
        }
        return Arrays.stream(propertyValues).map(Serializable::toString).map(Long::parseLong)
                .toArray(Long[]::new);
    }

    @Override
    public void setIntegerArrayProperty(String propertyName, Long[] propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public Double[] getRealArrayProperty(String propertyName)
    {
        Serializable[] propertyValues =
                getProperties() != null ? (Serializable[]) getProperties().get(propertyName) : null;
        if (propertyValues == null)
        {
            return null;
        }
        return Arrays.stream(propertyValues).map(Serializable::toString).map(Double::parseDouble)
                .toArray(Double[]::new);
    }

    @Override
    public void setRealArrayProperty(String propertyName, Double[] propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public String[] getStringArrayProperty(String propertyName)
    {
        Serializable[] propertyValues =
                getProperties() != null ? (Serializable[]) getProperties().get(propertyName) : null;
        if (propertyValues == null)
        {
            return null;
        }
        return Arrays.stream(propertyValues).map(Serializable::toString).toArray(String[]::new);
    }

    @Override
    public void setStringArrayProperty(String propertyName, String[] propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public ZonedDateTime[] getTimestampArrayProperty(String propertyName)
    {
        Serializable[] propertyValues =
                getProperties() != null ? (Serializable[]) getProperties().get(propertyName) : null;
        if (propertyValues == null)
        {
            return null;
        }
        return Arrays.stream(propertyValues).map(Serializable::toString)
                .map(dateTime -> ZonedDateTime.parse(dateTime,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X")))
                .toArray(ZonedDateTime[]::new);
    }

    @Override
    public void setTimestampArrayProperty(String propertyName, ZonedDateTime[] propertyValue)
    {
        String[] value = (propertyValue == null) ? null : Arrays.stream(propertyValue)
                .map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")))
                        .toArray(String[]::new);
        setProperty(propertyName, value);
    }

    @Override
    public String getJsonProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setJsonProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }
}
