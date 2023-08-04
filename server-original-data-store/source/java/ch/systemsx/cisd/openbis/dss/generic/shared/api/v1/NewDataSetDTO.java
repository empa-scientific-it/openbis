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
package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Represents a new data set that the DSS should register.
 * <p>
 * The information required to register a new data set are the owner of the data set, the name of the container of the data set (folder or file name),
 * and file info about the files in the data set.
 * <p>
 * Optionally, a data set type and properties may be specified. The type and properties will override those inferred by the server.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@JsonObject("NewDataSetDTO")
public class NewDataSetDTO implements Serializable
{
 
	//TO-DO - Fix Hack: The only reason to have this property as transient is because Jackson is not ignoring it using @JsonIgnore
    transient UploadObserver uploadObserver = null; 
    
    public void addUploadObserver(UploadObserver uploadObserver)
    {
        this.uploadObserver = uploadObserver;
    }
    
    public void notifyUploadProgress(long totalBytesRead) {
    	if(uploadObserver != null) {
    		uploadObserver.updateTotalBytesRead(totalBytesRead);
    	}
    }
    
    public static String DEFAULT_DATA_SET_FOLDER_NAME = "original";

    /**
     * The different types of owners of data sets; there are two: experiment and sample.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    @JsonObject("DataSetOwnerType")
    public static enum DataSetOwnerType
    {
        EXPERIMENT
        {
            @Override
            public String toString()
            {
                return "Experiment";
            }
        },
        SAMPLE
        {
            @Override
            public String toString()
            {
                return "Sample";
            }
        },
        DATA_SET
        {
            @Override
            public String toString()
            {
                return "Data Set";
            }
        };

        @Override
        public abstract String toString();
    }

    /**
     * The identifier of the owner of the new data set. This could either be an experiment identifier or a sample identifier.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    @JsonObject("DataSetOwner")
    public static class DataSetOwner implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final DataSetOwnerType type;

        private final String identifier;

        public DataSetOwner(DataSetOwnerType type, String identifier)
        {
            this.type = type;
            this.identifier = identifier;
        }

        public DataSetOwnerType getType()
        {
            return type;
        }

        public String getIdentifier()
        {
            return identifier;
        }

        @Override
        public String toString()
        {
            ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
            sb.append(getType());
            sb.append(getIdentifier());
            return sb.toString();
        }
    }

    private static String getFolderNameOrNull(File dataSetFile)
    {
        String folderNameOrNull = null;
        if (dataSetFile.isDirectory())
        {
            folderNameOrNull = dataSetFile.getName();
        }
        return folderNameOrNull;
    }

    private static final long serialVersionUID = 1L;

    private final DataSetOwner dataSetOwner;

    private final String dataSetFolderName;

    private final List<FileInfoDssDTO> fileInfos;

    private final NewDataSetMetadataDTO dataSetMetadata;

    /**
     * Constructor
     * 
     * @param dataSetOwner the owner of the new data set
     * @param dataSetFile a local file or directory whose contents will be uploaded to openBIS.
     */
    public NewDataSetDTO(DataSetOwner dataSetOwner, File dataSetFile)
    {
        this(dataSetOwner, getFolderNameOrNull(dataSetFile), FileInfoDssBuilder
                .getFileInfos(dataSetFile));
    }

    /**
     * Constructor
     * 
     * @param dataSetOwner The owner of the new data set
     * @param dataSetFolderNameOrNull The name of the folder the data is stored in. If the data set is just a single file and the folder name is null,
     *            a folder will be created.
     * @param fileInfos FileInfoDssDTO objects for each of the files in the data set.
     */
    public NewDataSetDTO(DataSetOwner dataSetOwner, String dataSetFolderNameOrNull,
            List<FileInfoDssDTO> fileInfos)
    {
        this(new NewDataSetMetadataDTO(), dataSetOwner, dataSetFolderNameOrNull, fileInfos);
    }

    /**
     * Constructor
     * 
     * @param dataSetType The type of the new data set
     * @param dataSetOwner The owner of the new data set
     * @param dataSetFolderNameOrNull The name of the folder the data is stored in. If the data set is just a single file and the folder name is null,
     *            a folder will be created.
     * @param fileInfos FileInfoDssDTO objects for each of the files in the data set.
     */
    public NewDataSetDTO(String dataSetType, DataSetOwner dataSetOwner,
            String dataSetFolderNameOrNull, List<FileInfoDssDTO> fileInfos)
    {
        this(dataSetOwner, dataSetFolderNameOrNull, fileInfos);
        setDataSetTypeOrNull(dataSetType);
    }

    /**
     * Constructor
     * 
     * @param dataSetMetadata The metadata (type and properties) that will override those inferred by the server.
     * @param dataSetOwner The owner of the new data set
     * @param dataSetFolderNameOrNull The name of the folder the data is stored in. If the data set is just a single file and the folder name is null,
     *            a folder will be created.
     * @param fileInfos FileInfoDssDTO objects for each of the files in the data set.
     */
    public NewDataSetDTO(NewDataSetMetadataDTO dataSetMetadata, DataSetOwner dataSetOwner,
            String dataSetFolderNameOrNull, List<FileInfoDssDTO> fileInfos)
    {
        this.dataSetMetadata = dataSetMetadata;
        this.dataSetOwner = dataSetOwner;
        this.dataSetFolderName =
                (null == dataSetFolderNameOrNull) ? DEFAULT_DATA_SET_FOLDER_NAME
                        : dataSetFolderNameOrNull;
        this.fileInfos = fileInfos;
    }

    /**
     * The direct owner of the data set, either an experiment or a sample.
     */
    public DataSetOwner getDataSetOwner()
    {
        return dataSetOwner;
    }

    /**
     * The name of the folder containing the files in the data set.
     */
    public String getDataSetFolderName()
    {
        return dataSetFolderName;
    }

    /**
     * {@link FileInfoDssDTO} objects describing the files within the data set.
     */
    public List<FileInfoDssDTO> getFileInfos()
    {
        return fileInfos;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(dataSetMetadata);
        sb.append(getDataSetOwner());
        sb.append(getFileInfos());
        return sb.toString();
    }

    //
    // metadata delegation
    //

    public String tryDataSetType()
    {
        return dataSetMetadata.tryDataSetType();
    }

    public void setDataSetTypeOrNull(String dataSetTypeOrNull)
    {
        dataSetMetadata.setDataSetTypeOrNull(dataSetTypeOrNull);
    }

    public Map<String, Serializable> getProperties()
    {
        return dataSetMetadata.getProperties();
    }

    public void setProperties(Map<String, Serializable> props)
    {
        dataSetMetadata.setProperties(props);
    }

    /**
     * The codes of the parent data sets for this new data set. The list may be empty.
     * 
     * @since 1.3
     */
    public List<String> getParentDataSetCodes()
    {
        return dataSetMetadata.getParentDataSetCodes();
    }

    /**
     * Sets the parent data sets of this data set.
     * 
     * @param codesOrNull If the value is null, the parents are cleared.
     * @since 1.3
     */
    public void setParentDataSetCodes(List<String> codesOrNull)
    {
        dataSetMetadata.setParentDataSetCodes(codesOrNull);
    }
}