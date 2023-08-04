/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;

/**
 * A mutable object for building NewDataSetDTO objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetDTOBuilder
{
    private final NewDataSetMetadataDTO dataSetMetadata;

    private String dataSetOwnerIdentifier;

    private DataSetOwnerType dataSetOwnerType = DataSetOwnerType.EXPERIMENT;

    private File file;

    public NewDataSetDTOBuilder()
    {
        dataSetMetadata = new NewDataSetMetadataDTO();
    }

    /**
     * The owner identifier may be and empty during construction but is non-null in a well-formed object.
     */
    public String getDataSetOwnerIdentifier()
    {
        return (null == dataSetOwnerIdentifier) ? "" : dataSetOwnerIdentifier;
    }

    public void setDataSetOwnerIdentifier(String dataSetOwnerIdentifier)
    {
        this.dataSetOwnerIdentifier = dataSetOwnerIdentifier;
    }

    /**
     * The owner type should never be null.
     */
    public DataSetOwnerType getDataSetOwnerType()
    {
        return dataSetOwnerType;
    }

    public void setDataSetOwnerType(DataSetOwnerType dataSetOwnerType)
    {
        this.dataSetOwnerType = dataSetOwnerType;
    }

    /**
     * The owner may be null during construction but is non-null in a well-formed object.
     */
    public DataSetOwner getDataSetOwner()
    {
        return new DataSetOwner(getDataSetOwnerType(), getDataSetOwnerIdentifier());
    }

    /**
     * The metadata data for the data set, including type and properties.
     */
    public NewDataSetMetadataDTO getDataSetMetadata()
    {
        return dataSetMetadata;
    }

    /**
     * The file that contains the data set. May be null during initialization but should be non-null in a well-formed data set.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * Set the file that contains the data set.
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    public List<FileInfoDssDTO> getFileInfos()
    {
        try
        {
            ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
            if (null == file)
            {
                return fileInfos;
            }
            if (false == file.exists())
            {
                return fileInfos;
            }

            String path = file.getCanonicalPath();
            if (false == file.isDirectory())
            {
                path = file.getParentFile().getCanonicalPath();
            }

            FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
            builder.appendFileInfosForFile(file, fileInfos, true);
            return fileInfos;
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    /**
     * Convert the builder into a NewDataSetDTO object.
     */
    public NewDataSetDTO asNewDataSetDTO()
    {
        String dataSetFolderNameOrNull = (file.isDirectory()) ? file.getName() : null;
        return new NewDataSetDTO(dataSetMetadata, getDataSetOwner(), dataSetFolderNameOrNull,
                getFileInfos());
    }

    /**
     * Take over any appropriate values from the template.
     */
    public void initializeFromTemplate(NewDataSetDTOBuilder template)
    {
        DataSetOwner owner = template.getDataSetOwner();
        setDataSetOwnerIdentifier(owner.getIdentifier());
        setDataSetOwnerType(owner.getType());
        setFile(template.getFile());
        NewDataSetMetadataDTO otherMetadata = template.getDataSetMetadata();
        dataSetMetadata.setDataSetTypeOrNull(otherMetadata.tryDataSetType());

        // Create a new map initialized from the other properties (a shallow copy).
        HashMap<String, Serializable> otherProps =
                new HashMap<String, Serializable>(otherMetadata.getProperties());
        dataSetMetadata.setProperties(otherProps);
        dataSetMetadata.setUnmodifiableProperties(new HashSet<String>(otherMetadata
                .getUnmodifiableProperties()));
    }
}
