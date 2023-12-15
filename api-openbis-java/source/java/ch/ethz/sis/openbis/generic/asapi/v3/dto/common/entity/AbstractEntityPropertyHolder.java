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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Serializable getProperty(String propertyName)
    {
        return getProperties() != null ? getProperties().get(propertyName) : null;
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
    public String getStringProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setStringProperty(String propertyName, String propertyValue)
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
        return propertyValue == null ?
                null :
                ZonedDateTime.parse(propertyValue,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X"));
    }

    @Override
    public void setTimestampProperty(String propertyName, ZonedDateTime propertyValue)
    {
        String value = (propertyValue == null) ?
                null :
                propertyValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
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
    public String getControlledVocabularyProperty(String propertyName)
    {
        return (String) getProperty(propertyName);
    }

    @Override
    public void setControlledVocabularyProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public SamplePermId getSampleProperty(String propertyName)
    {

        String propertyValue = (String) getProperty(propertyName);
        return new SamplePermId(propertyValue);
    }

    @Override
    public void setSampleProperty(String propertyName, SamplePermId propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : propertyValue.getPermId());
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
                .map(dateTime -> dateTime.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
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

    private <T> List<T> getListOfValues(Serializable generalValue, Function<Serializable, T> fun)
    {
        if (generalValue != null)
        {
            if (generalValue.getClass().isArray())
            {
                List<T> result = new ArrayList<>();
                for (Serializable singleValue : (Serializable[]) generalValue)
                {
                    result.add(fun.apply(singleValue));
                }
                return result;
            } else
            {
                return List.of(fun.apply(generalValue));
            }
        }
        return null;
    }

    @Override
    public List<Long> getMultiValueIntegerProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> Long.parseLong((String) x));
    }

    @Override
    public void setMultiValueIntegerProperty(String propertyName, List<Long> propertyValues)
    {
        if (propertyValues != null)
        {
            setProperty(propertyName, propertyValues.toArray(Long[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<String> getMultiValueStringProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> (String) x);
    }

    @Override
    public void setMultiValueStringProperty(String propertyName, List<String> propertyValues)
    {
        if (propertyValues != null)
        {
            setProperty(propertyName, propertyValues.toArray(String[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    public List<String> getMultiValueControlledVocabularyProperty(String propertyName)
    {
        if (getProperties() == null || getProperties().get(propertyName) == null)
        {
            return null;
        }
        Serializable value = getProperties().get(propertyName);
        if (value.getClass().isArray())
        {
            Serializable[] values = (Serializable[]) value;
            return Arrays.stream(values).map(x -> (String) x).collect(Collectors.toList());
        } else
        {
            return List.of((String) value);
        }
    }

    @Override
    public void setMultiValueControlledVocabularyProperty(String propertyName,
            List<String> propertyValues)
    {
        if (propertyValues != null)
        {
            setProperty(propertyName, propertyValues.toArray(String[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<SamplePermId> getMultiValueSampleProperty(String propertyName)
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
                    .collect(Collectors.toList());
        } else
        {
            String propertyValue = (String) value;
            return List.of(new SamplePermId(propertyValue));
        }
    }

    @Override
    public void setMultiValueSampleProperty(String propertyName, List<SamplePermId> propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : propertyValue.stream()
                .map(ObjectPermId::getPermId)
                .toArray(String[]::new));
    }

    @Override
    public List<Double> getMultiValueRealProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> Double.parseDouble((String) x));
    }

    @Override
    public void setMultiValueRealProperty(String propertyName, List<Double> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(Double[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<ZonedDateTime> getMultiValueTimestampProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> ZonedDateTime.parse((String)x,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X")));
    }

    @Override
    public void setMultiValueTimestampProperty(String propertyName,
            List<ZonedDateTime> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.stream()
                    .map(x -> x.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                    .toArray(String[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<Boolean> getMultiValueBooleanProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> Boolean.parseBoolean((String) x));
    }

    @Override
    public void setMultiValueBooleanProperty(String propertyName, List<Boolean> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(Boolean[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<String> getMultiValueHyperlinkProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> (String) x);
    }

    @Override
    public void setMultiValueHyperlinkProperty(String propertyName, List<String> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(String[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<String> getMultiValueXmlProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> (String) x);
    }

    @Override
    public void setMultiValueXmlProperty(String propertyName, List<String> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(String[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<Long[]> getMultiValueIntegerArrayProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue,
                (x) -> Arrays.stream((Serializable[]) x)
                        .map(Serializable::toString)
                        .map(Long::parseLong)
                        .toArray(Long[]::new));
    }

    @Override
    public void setMultiValueIntegerArrayProperty(String propertyName, List<Long[]> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(Long[][]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<Double[]> getMultiValueRealArrayProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue,
                (x) -> Arrays.stream((Serializable[]) x)
                        .map(Serializable::toString)
                        .map(Double::parseDouble)
                        .toArray(Double[]::new));
    }

    @Override
    public void setMultiValueRealArrayProperty(String propertyName, List<Double[]> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(Double[][]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<String[]> getMultiValueStringArrayProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue,
                (x) -> Arrays.stream((Serializable[]) x)
                        .map(Serializable::toString)
                        .toArray(String[]::new));
    }

    @Override
    public void setMultiValueStringArrayProperty(String propertyName, List<String[]> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(String[][]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }

    @Override
    public List<ZonedDateTime[]> getMultiValueTimestampArrayProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue,
                (x) -> Arrays.stream((Serializable[]) x)
                        .map(Serializable::toString)
                        .map(dateTime -> ZonedDateTime.parse(dateTime,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss X")))
                        .toArray(ZonedDateTime[]::new));
    }

    @Override
    public void setMultiValueTimestampArrayProperty(String propertyName,
            List<ZonedDateTime[]> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.stream()
                    .map(dateTimeArray -> Arrays.stream(dateTimeArray)
                            .map(x -> x.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")))
                            .toArray(String[]::new))
                    .toArray(String[][]::new));
        } else
        {
            setProperty(propertyName, null);
        }
        //        String[] value = (propertyValue == null) ? null : Arrays.stream(propertyValue)
        //                .map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")))
        //                .toArray(String[]::new);
        //        setProperty(propertyName, value);
    }

    @Override
    public List<String> getMultiValueJsonProperty(String propertyName)
    {
        Serializable propertyValue = getProperty(propertyName);
        return getListOfValues(propertyValue, (x) -> (String) x);
    }

    @Override
    public void setMultiValueJsonProperty(String propertyName, List<String> propertyValue)
    {
        if (propertyValue != null)
        {
            setProperty(propertyName, propertyValue.toArray(String[]::new));
        } else
        {
            setProperty(propertyName, null);
        }
    }
}
