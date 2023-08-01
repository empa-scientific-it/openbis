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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMetaDataUpdateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.update.AttachmentListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.experiment.update.ExperimentUpdate")
public class ExperimentUpdate implements IUpdate, IObjectUpdate<IExperimentId>, IPropertiesHolder,
        IMetaDataUpdateHolder
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IExperimentId experimentId;

    @JsonProperty
    private boolean freeze;

    @JsonProperty
    private boolean freezeForDataSets;

    @JsonProperty
    private boolean freezeForSamples;

    @JsonProperty
    @JsonDeserialize(contentUsing = PropertiesDeserializer.class)
    private Map<String, Serializable> properties = new HashMap<>();

    @JsonProperty
    private FieldUpdateValue<IProjectId> projectId = new FieldUpdateValue<IProjectId>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    private AttachmentListUpdateValue attachments = new AttachmentListUpdateValue();

    @JsonProperty
    private ListUpdateMapValues metaData = new ListUpdateMapValues();

    @Override
    @JsonIgnore
    public IExperimentId getObjectId()
    {
        return getExperimentId();
    }

    @JsonIgnore
    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    @JsonIgnore
    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    @JsonIgnore
    public boolean shouldBeFrozen()
    {
        return freeze;
    }

    public void freeze()
    {
        this.freeze = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForDataSets()
    {
        return freezeForDataSets;
    }

    public void freezeForDataSets()
    {
        this.freeze = true;
        this.freezeForDataSets = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForSamples()
    {
        return freezeForSamples;
    }

    public void freezeForSamples()
    {
        this.freeze = true;
        this.freezeForSamples = true;
    }

    @Override
    @JsonIgnore
    public void setProperty(String propertyName, Serializable propertyValue)
    {
        properties.put(propertyName, propertyValue);
    }

    @Override
    @JsonIgnore
    public String getProperty(String propertyName)
    {
        return properties != null ? (String) properties.get(propertyName) : null;
    }

    @Override
    @JsonIgnore
    public void setProperties(Map<String, Serializable> properties)
    {
        this.properties = properties;
    }

    @Override
    @JsonIgnore
    public Map<String, Serializable> getProperties()
    {
        return properties;
    }

    @JsonIgnore
    public void setProjectId(IProjectId projectId)
    {
        this.projectId.setValue(projectId);
    }

    @JsonIgnore
    public FieldUpdateValue<IProjectId> getProjectId()
    {
        return projectId;
    }

    @JsonIgnore
    public IdListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
    }

    @JsonIgnore
    public AttachmentListUpdateValue getAttachments()
    {
        return attachments;
    }

    @JsonIgnore
    public void setAttachmentsActions(List<ListUpdateAction<Object>> actions)
    {
        attachments.setActions(actions);
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
            return Arrays.stream(values).map(x -> (String)x).toArray(String[]::new);
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

    @Override
    @JsonIgnore
    public ListUpdateMapValues getMetaData()
    {
        return metaData;
    }

    @JsonIgnore
    public void setMetaDataActions(List<ListUpdateAction<Object>> actions)
    {
        metaData.setActions(actions);
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("experimentId", experimentId).toString();
    }

}
