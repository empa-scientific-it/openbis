/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Piotr Buczek
 */
public class BasicDataSetUpdates implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private TechId datasetId;

    private int version;

    // ----- the data which should be changed:

    // new set of properties for the data set, they will replace the old set
    private List<IEntityProperty> properties;

    private String[] modifiedParentDatasetCodesOrNull;

    private String[] metaprojectsOrNull;

    // Optional:

    // 1. external data (non-virtual)

    private String fileFormatTypeCode;

    private String modifiedContainerDatasetCodeOrNull;

    // 2. container (virtual)
    private String[] modifiedContainedDatasetCodesOrNull;

    private String externalDataManagementSystemCode;

    private String externalCode;

    public BasicDataSetUpdates()
    {
    }

    public BasicDataSetUpdates(TechId datasetId, List<IEntityProperty> properties, int version)
    {
        this.datasetId = datasetId;
        this.version = version;
        this.properties = properties;
    }

    public TechId getDatasetId()
    {
        return datasetId;
    }

    public void setDatasetId(TechId datasetId)
    {
        this.datasetId = datasetId;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    public String[] getModifiedParentDatasetCodesOrNull()
    {
        return modifiedParentDatasetCodesOrNull;
    }

    public void setModifiedParentDatasetCodesOrNull(String[] modifiedParentDatasetCodesOrNull)
    {
        this.modifiedParentDatasetCodesOrNull = modifiedParentDatasetCodesOrNull;
    }

    public String getFileFormatTypeCode()
    {
        return fileFormatTypeCode;
    }

    public void setFileFormatTypeCode(String fileFormatTypeCode)
    {
        this.fileFormatTypeCode = fileFormatTypeCode;
    }

    public String[] getModifiedContainedDatasetCodesOrNull()
    {
        return modifiedContainedDatasetCodesOrNull;
    }

    public void setModifiedContainedDatasetCodesOrNull(String[] modifiedContainedDatasetCodesOrNull)
    {
        this.modifiedContainedDatasetCodesOrNull = modifiedContainedDatasetCodesOrNull;
    }

    public String getModifiedContainerDatasetCodeOrNull()
    {
        return modifiedContainerDatasetCodeOrNull;
    }

    public void setModifiedContainerDatasetCodeOrNull(String modifiedContainerDatasetCodeOrNull)
    {
        this.modifiedContainerDatasetCodeOrNull = modifiedContainerDatasetCodeOrNull;
    }

    public String getExternalDataManagementSystemCode()
    {
        return externalDataManagementSystemCode;
    }

    public void setExternalDataManagementSystemCode(String externalDataManagementSystemCode)
    {
        this.externalDataManagementSystemCode = externalDataManagementSystemCode;
    }

    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    public String[] getMetaprojectsOrNull()
    {
        return metaprojectsOrNull;
    }

    public void setMetaprojectsOrNull(String[] metaprojectsOrNull)
    {
        this.metaprojectsOrNull = metaprojectsOrNull;
    }

}
