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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMetaDataUpdateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.property.PropertiesDeserializer;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.update.DataSetUpdate")
public class DataSetUpdate implements IUpdate, IObjectUpdate<IDataSetId>, IPropertiesHolder,
        IMetaDataUpdateHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IDataSetId dataSetId;

    @JsonProperty
    private boolean freeze;

    @JsonProperty
    private boolean freezeForChildren;

    @JsonProperty
    private boolean freezeForParents;

    @JsonProperty
    private boolean freezeForComponents;

    @JsonProperty
    private boolean freezeForContainers;

    @JsonProperty
    private FieldUpdateValue<IExperimentId> experimentId = new FieldUpdateValue<IExperimentId>();

    @JsonProperty
    private FieldUpdateValue<ISampleId> sampleId = new FieldUpdateValue<ISampleId>();

    @JsonProperty
    private FieldUpdateValue<PhysicalDataUpdate> physicalData = new FieldUpdateValue<PhysicalDataUpdate>();

    @JsonProperty
    private FieldUpdateValue<LinkedDataUpdate> linkedData = new FieldUpdateValue<LinkedDataUpdate>();

    @JsonProperty
    private IdListUpdateValue<ITagId> tagIds = new IdListUpdateValue<ITagId>();

    @JsonProperty
    @JsonDeserialize(contentUsing = PropertiesDeserializer.class)
    private Map<String, Serializable> properties = new HashMap<String, Serializable>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> containerIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> componentIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> parentIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private IdListUpdateValue<IDataSetId> childIds = new IdListUpdateValue<IDataSetId>();

    @JsonProperty
    private ListUpdateMapValues metaData = new ListUpdateMapValues();

    @Override
    @JsonIgnore
    public IDataSetId getObjectId()
    {
        return getDataSetId();
    }

    public IDataSetId getDataSetId()
    {
        return dataSetId;
    }

    @JsonIgnore
    public void setDataSetId(IDataSetId dataSetId)
    {
        this.dataSetId = dataSetId;
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
    public boolean shouldBeFrozenForChildren()
    {
        return freezeForChildren;
    }

    public void freezeForChildren()
    {
        this.freeze = true;
        this.freezeForChildren = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForParents()
    {
        return freezeForParents;
    }

    public void freezeForParents()
    {
        this.freeze = true;
        this.freezeForParents = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForComponents()
    {
        return freezeForComponents;
    }

    public void freezeForComponents()
    {
        this.freeze = true;
        this.freezeForComponents = true;
    }

    @JsonIgnore
    public boolean shouldBeFrozenForContainers()
    {
        return freezeForContainers;
    }

    public void freezeForContainers()
    {
        this.freeze = true;
        this.freezeForContainers = true;
    }

    @JsonIgnore
    public FieldUpdateValue<IExperimentId> getExperimentId()
    {
        return experimentId;
    }

    @JsonIgnore
    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId.setValue(experimentId);
    }

    @JsonIgnore
    public FieldUpdateValue<ISampleId> getSampleId()
    {
        return sampleId;
    }

    @JsonIgnore
    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId.setValue(sampleId);
    }

    @JsonIgnore
    public FieldUpdateValue<PhysicalDataUpdate> getPhysicalData()
    {
        return physicalData;
    }

    @JsonIgnore
    public void setPhysicalData(PhysicalDataUpdate physicalData)
    {
        this.physicalData.setValue(physicalData);
    }

    public FieldUpdateValue<LinkedDataUpdate> getLinkedData()
    {
        return linkedData;
    }

    @JsonIgnore
    public void setLinkedData(LinkedDataUpdate linkedData)
    {
        this.linkedData.setValue(linkedData);
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
    public IdListUpdateValue<ITagId> getTagIds()
    {
        return tagIds;
    }

    @JsonIgnore
    public void setTagActions(List<ListUpdateAction<ITagId>> actions)
    {
        tagIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getContainerIds()
    {
        return containerIds;
    }

    @JsonIgnore
    public void setContainerActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        containerIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getComponentIds()
    {
        return componentIds;
    }

    @JsonIgnore
    public void setComponentActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        componentIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getParentIds()
    {
        return parentIds;
    }

    @JsonIgnore
    public void setParentActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        parentIds.setActions(actions);
    }

    @JsonIgnore
    public IdListUpdateValue<IDataSetId> getChildIds()
    {
        return childIds;
    }

    @JsonIgnore
    public void setChildActions(List<ListUpdateAction<IDataSetId>> actions)
    {
        childIds.setActions(actions);
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
        return new ObjectToString(this).append("dataSetId", dataSetId).toString();
    }

}
