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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExtractableData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetRegistrationDetails<T extends DataSetInformation> implements ITypeExtractor
{
    private FileFormatType fileFormatType;

    private boolean measuredData;

    private String processorType;

    private T dataSetInformation;

    public DataSetRegistrationDetails()
    {
        fileFormatType = new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE);
        measuredData = true;
    }

    @Override
    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return fileFormatType;
    }

    @Override
    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return measuredData;
    }

    @Override
    public String getProcessorType(File incomingDataSetPath)
    {
        return processorType;
    }

    @Override
    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        return getDataSetType();
    }

    @Override
    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return getLocatorType();
    }

    public LocatorType getLocatorType()
    {
        return new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
    }

    public FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    public void setFileFormatType(FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    public void setFileFormatType(String fileFormatTypeCode)
    {
        this.fileFormatType = new FileFormatType();
        this.fileFormatType.setCode(fileFormatTypeCode);
    }

    public boolean isMeasuredData()
    {
        return measuredData;
    }

    public void setMeasuredData(boolean measuredData)
    {
        this.measuredData = measuredData;
    }

    public String getProcessorType()
    {
        return processorType;
    }

    public void setProcessorType(String processorType)
    {
        this.processorType = processorType;
    }

    public DataSetType getDataSetType()
    {
        return dataSetInformation.getDataSetType();
    }

    public void setDataSetType(DataSetType dataSetType)
    {
        dataSetInformation.setDataSetType(dataSetType);
    }

    public void setDataSetType(String dataSetTypeCode)
    {
        setDataSetType(new DataSetType(dataSetTypeCode));
    }

    public DataSetKind getDataSetKind()
    {
        return dataSetInformation.getDataSetKind();
    }

    public void setDataSetKind(DataSetKind dataSetKind)
    {
        dataSetInformation.setDataSetKind(dataSetKind);
    }

    public T getDataSetInformation()
    {
        return dataSetInformation;
    }

    public void setDataSetInformation(T dataSetInformation)
    {
        this.dataSetInformation = dataSetInformation;
    }

    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        ExtractableData datasetExtractableData = dataSetInformation.getExtractableData();
        if (propertyValue == null)
        {
            datasetExtractableData.removeDataSetProperty(propertyCode);
        } else
        {
            List<NewProperty> properties = datasetExtractableData.getDataSetProperties();
            NewProperty property = EntityHelper.tryFindProperty(properties, propertyCode);
            if (property != null)
            {
                property.setValue(propertyValue);
            } else
            {
                properties.add(new NewProperty(propertyCode, propertyValue));
            }
        }
    }

    public String getPropertyValue(String propertyCode)
    {
        NewProperty property =
                EntityHelper.tryFindProperty(dataSetInformation.getExtractableData()
                        .getDataSetProperties(), propertyCode);
        return (property != null) ? property.getValue() : null;
    }
}
