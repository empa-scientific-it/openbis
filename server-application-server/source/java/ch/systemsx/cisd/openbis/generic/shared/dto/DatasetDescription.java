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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Describes one dataset which should be processed by the plugin task.
 * 
 * @author Tomasz Pylak
 */
public class DatasetDescription implements Serializable, IDatasetLocation
{
    private static final long serialVersionUID = 20L;

    private String dataSetTypeCode;

    private String fileFormatType;

    private boolean h5Folders;

    private boolean h5arFolders;

    private String datasetCode;

    private String dataSetLocation;

    private final Map<String, Integer> orderInContainers = new HashMap<String, Integer>();

    private Date registrationTimestamp;

    private Long dataSetSize;

    private int speedHint;

    private String sampleCode;

    private String sampleIdentifier;

    private String sampleTypeCode;

    private String spaceCode;

    private String projectCode;

    private String instanceCode;

    private String experimentCode;

    private String experimentIdentifier;

    private String experimentTypeCode;

    private String mainDataSetPattern;

    private String mainDataSetPath;

    private String dataStoreCode;

    private String dataStoreUrl;

    private boolean storageConfirmed;

    private boolean archivingRequested;

    public static List<String> extractCodes(List<DatasetDescription> dataSets)
    {
        List<String> result = new ArrayList<String>();
        if (dataSets != null)
        {
            for (DatasetDescription description : dataSets)
            {
                result.add(description.getDataSetCode());
            }
        }
        return result;
    }

    public void setDatasetTypeCode(String datasetTypeCode)
    {
        this.dataSetTypeCode = datasetTypeCode;
    }

    public String getDataSetTypeCode()
    {
        return dataSetTypeCode;
    }

    public Long getDataSetSize()
    {
        return dataSetSize;
    }

    public void setDataSetSize(Long dataSetSize)
    {
        this.dataSetSize = dataSetSize;
    }

    @Override
    public String getDataSetLocation()
    {
        return dataSetLocation;
    }

    public void setDataSetLocation(String dataSetLocation)
    {
        this.dataSetLocation = dataSetLocation;
    }

    public Date getRegistrationTimestamp()
    {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(Date registrationTimestamp)
    {
        this.registrationTimestamp = registrationTimestamp;
    }

    public String getInstanceCode()
    {
        return instanceCode;
    }

    @Override
    public String getDataSetCode()
    {
        return datasetCode;
    }

    public void setDataSetCode(String datasetCode)
    {
        this.datasetCode = datasetCode;
    }

    public int getSpeedHint()
    {
        return speedHint;
    }

    public void setSpeedHint(int speedHint)
    {
        this.speedHint = speedHint;
    }

    /**
     * NOTE: may be NULL
     */
    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }

    /**
     * NOTE: may be NULL
     */
    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }

    /**
     * NOTE: may be NULL
     */
    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.sampleIdentifier = sampleIdentifier;
    }

    public String getSampleIdentifier()
    {
        return sampleIdentifier;
    }

    public void setSampleTypeCode(String sampleTypeCode)
    {
        this.sampleTypeCode = sampleTypeCode;
    }

    public String getSampleTypeCode()
    {
        return sampleTypeCode;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(String groupCode)
    {
        this.spaceCode = groupCode;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public String getExperimentCode()
    {
        return experimentCode;
    }

    public void setExperimentCode(String experimentCode)
    {
        this.experimentCode = experimentCode;
    }

    public void setExperimentIdentifier(String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    public void setExperimentTypeCode(String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    public String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public String getFileFormatType()
    {
        return fileFormatType;
    }

    public void setFileFormatType(String fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    public boolean isH5Folders()
    {
        return h5Folders;
    }

    public void setH5Folders(boolean h5Folders)
    {
        this.h5Folders = h5Folders;
    }

    public boolean isH5arFolders()
    {
        return h5arFolders;
    }

    public void setH5arFolders(boolean h5arFolders)
    {
        this.h5arFolders = h5arFolders;
    }

    @Override
    public String toString()
    {
        return String.format("Dataset '%s'", datasetCode);
    }

    @Override
    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public void setDataStoreCode(String dss)
    {
        this.dataStoreCode = dss;
    }

    @Override
    public String getDataStoreUrl()
    {
        return this.dataStoreUrl;
    }

    public void setDataStoreUrl(String dataStoreUrl)
    {
        this.dataStoreUrl = dataStoreUrl;
    }

    @Override
    public Integer getOrderInContainer(String containerDataSetCode)
    {
        return orderInContainers.get(containerDataSetCode);
    }

    public void addOrderInContainer(String containerDataSetCode, Integer orderInContainer)
    {
        orderInContainers.put(containerDataSetCode, orderInContainer);
    }

    public Map<String, Integer> getOrderInContainers()
    {
        return orderInContainers;
    }

    public boolean isStorageConfirmed()
    {
        return storageConfirmed;
    }

    public void setStorageConfirmed(boolean storageConfirmed)
    {
        this.storageConfirmed = storageConfirmed;
    }

    public boolean isArchivingRequested()
    {
        return archivingRequested;
    }

    public void setArchivingRequested(boolean archivingRequested)
    {
        this.archivingRequested = archivingRequested;
    }

}
