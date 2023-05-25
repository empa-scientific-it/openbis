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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.IObjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICreationIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.IDataStoreId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.create.DataSetCreation")
public class DataSetCreation implements ICreation, ICreationIdHolder, IObjectCreation, IPropertiesHolder
{
    private static final long serialVersionUID = 1L;

    private IEntityTypeId typeId;

    private IExperimentId experimentId;

    private ISampleId sampleId;

    private IDataStoreId dataStoreId;

    private String code;

    private DataSetKind dataSetKind;

    private boolean measured;

    private String dataProducer;

    private Date dataProductionDate;

    private PhysicalDataCreation physicalData;

    private LinkedDataCreation linkedData;

    private List<? extends ITagId> tagIds;

    private Map<String, String> properties = new HashMap<String, String>();

    private List<? extends IDataSetId> containerIds;

    private List<? extends IDataSetId> componentIds;

    private List<? extends IDataSetId> parentIds;

    private List<? extends IDataSetId> childIds;

    private CreationId creationId;

    private boolean autoGeneratedCode;

    public IEntityTypeId getTypeId()
    {
        return typeId;
    }

    public void setTypeId(IEntityTypeId typeId)
    {
        this.typeId = typeId;
    }

    public IExperimentId getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(IExperimentId experimentId)
    {
        this.experimentId = experimentId;
    }

    public ISampleId getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(ISampleId sampleId)
    {
        this.sampleId = sampleId;
    }

    public IDataStoreId getDataStoreId()
    {
        return dataStoreId;
    }

    public void setDataStoreId(IDataStoreId dataStoreId)
    {
        this.dataStoreId = dataStoreId;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public DataSetKind getDataSetKind()
    {
        return dataSetKind;
    }

    public void setDataSetKind(DataSetKind dataSetKind)
    {
        this.dataSetKind = dataSetKind;
    }

    public boolean isMeasured()
    {
        return measured;
    }

    public void setMeasured(boolean measured)
    {
        this.measured = measured;
    }

    public String getDataProducer()
    {
        return dataProducer;
    }

    public void setDataProducer(String dataProducer)
    {
        this.dataProducer = dataProducer;
    }

    public Date getDataProductionDate()
    {
        return dataProductionDate;
    }

    public void setDataProductionDate(Date dataProductionDate)
    {
        this.dataProductionDate = dataProductionDate;
    }

    public PhysicalDataCreation getPhysicalData()
    {
        return physicalData;
    }

    public void setPhysicalData(PhysicalDataCreation physicalData)
    {
        this.physicalData = physicalData;
    }

    public LinkedDataCreation getLinkedData()
    {
        return linkedData;
    }

    public void setLinkedData(LinkedDataCreation linkedData)
    {
        this.linkedData = linkedData;
    }

    public List<? extends ITagId> getTagIds()
    {
        return tagIds;
    }

    public void setTagIds(List<? extends ITagId> tagIds)
    {
        this.tagIds = tagIds;
    }

    public List<? extends IDataSetId> getContainerIds()
    {
        return containerIds;
    }

    public void setContainerIds(List<? extends IDataSetId> containerIds)
    {
        this.containerIds = containerIds;
    }

    public List<? extends IDataSetId> getComponentIds()
    {
        return componentIds;
    }

    public void setComponentIds(List<? extends IDataSetId> componentIds)
    {
        this.componentIds = componentIds;
    }

    public List<? extends IDataSetId> getParentIds()
    {
        return parentIds;
    }

    public void setParentIds(List<? extends IDataSetId> parentIds)
    {
        this.parentIds = parentIds;
    }

    public List<? extends IDataSetId> getChildIds()
    {
        return childIds;
    }

    public void setChildIds(List<? extends IDataSetId> childIds)
    {
        this.childIds = childIds;
    }

    @Override
    public void setProperty(String propertyName, String propertyValue)
    {
        this.properties.put(propertyName, propertyValue);
    }

    @Override
    public String getProperty(String propertyName)
    {
        return properties != null ? properties.get(propertyName) : null;
    }

    @Override
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public CreationId getCreationId()
    {
        return creationId;
    }

    public void setCreationId(CreationId creationId)
    {
        this.creationId = creationId;
    }

    public boolean isAutoGeneratedCode()
    {
        return this.autoGeneratedCode;
    }

    public void setAutoGeneratedCode(boolean autoGeneratedCode)
    {
        this.autoGeneratedCode = autoGeneratedCode;
    }

    @Override
    public Long getIntegerProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.isBlank()) ? null : Long.parseLong(propertyValue);
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
        return (propertyValue == null || propertyValue.isBlank()) ? null : Double.parseDouble(propertyValue);
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
        return (propertyValue == null || propertyValue.isBlank()) ? null : Boolean.parseBoolean(propertyValue);
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
    public String getControlledVocabularyProperty(String propertyName)
    {
        return getProperty(propertyName);
    }

    @Override
    public void setControlledVocabularyProperty(String propertyName, String propertyValue)
    {
        setProperty(propertyName, propertyValue);
    }

    @Override
    public SamplePermId getSampleProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.isBlank()) ? null : new SamplePermId(propertyValue);
    }

    @Override
    public void setSampleProperty(String propertyName, SamplePermId propertyValue)
    {
        setProperty(propertyName, propertyValue == null ? null : propertyValue.getPermId());
    }

    @Override
    public Long[] getIntegerArrayProperty(String propertyName)
    {
        String propertyValue = getProperty(propertyName);
        return (propertyValue == null || propertyValue.isBlank()) ? null : Arrays.stream(propertyValue.split(",")).map(String::trim).map(Long::parseLong).toArray(Long[]::new);
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
        return (propertyValue == null || propertyValue.isBlank()) ? null : Arrays.stream(propertyValue.split(",")).map(String::trim).map(Double::parseDouble).toArray(Double[]::new);
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
        return (propertyValue == null || propertyValue.isBlank()) ? null : Arrays.stream(propertyValue.split(",")).map(String::trim).toArray(String[]::new);
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
    public String toString()
    {
        return new ObjectToString(this).append("experimentId", experimentId).append("sampleId", sampleId).append("code", code).toString();
    }

}
