/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Information given to openBIS when registering a new external data set.
 * 
 * @author Christian Ribeaud
 */
public class NewExternalData implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private ExtractableData extractableData = new ExtractableData();

    private String shareId;

    private String location;

    private Long size;

    private int speedHint;

    private StorageFormat storageFormat;

    private Date registrationDate;

    private String userId;

    private String userEMail;

    private FileFormatType fileFormatType;

    private DataSetType dataSetType;

    private DataSetKind dataSetKind;

    private LocatorType locatorType;

    private BooleanOrUnknown complete = BooleanOrUnknown.U;

    private String associatedSampleCode;

    private String dataStoreCode;

    private boolean measured;

    private ExperimentIdentifier experimentIdentifierOrNull;

    private SampleIdentifier sampleIdentifierOrNull;

    private String samplePermIdOrNull;

    private boolean h5Folders = false;

    private boolean h5arFolders = true;

    public ExperimentIdentifier getExperimentIdentifierOrNull()
    {
        return experimentIdentifierOrNull;
    }

    public void setExperimentIdentifierOrNull(ExperimentIdentifier experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public SampleIdentifier getSampleIdentifierOrNull()
    {
        return sampleIdentifierOrNull;
    }

    public void setSampleIdentifierOrNull(SampleIdentifier sampleIdentifierOrNull)
    {
        this.sampleIdentifierOrNull = sampleIdentifierOrNull;
    }

    public String getSamplePermIdOrNull()
    {
        return samplePermIdOrNull;
    }

    public void setSamplePermIdOrNull(String samplePermId)
    {
        this.samplePermIdOrNull = samplePermId;
    }

    public final DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public final void setDataSetType(final DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    public DataSetKind getDataSetKind()
	{
		return dataSetKind;
	}

	public void setDataSetKind(DataSetKind dataSetKind)
	{
		this.dataSetKind = dataSetKind;
	}

	public String getShareId()
    {
        return shareId;
    }

    public void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize(Long size)
    {
        this.size = size;
    }

    public int getSpeedHint()
    {
        return speedHint;
    }

    public void setSpeedHint(int speedHint)
    {
        this.speedHint = speedHint;
    }

    /** Returns <code>locator</code>. */
    public final String getLocation()
    {
        return location;
    }

    /** Sets <code>locator</code>. */
    public final void setLocation(final String locator)
    {
        this.location = locator;
    }

    /**
     * Returns the storage format (proprietary or BDS) of this external data set.
     */
    public StorageFormat getStorageFormat()
    {
        return storageFormat;
    }

    /**
     * Sets the storage format (proprietary or BDS) of this external data set.
     */
    public void setStorageFormat(final StorageFormat storageFormat)
    {
        this.storageFormat = storageFormat;
    }

    /** Returns the date when the data set has been registered to the database. */
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    /** Sets the date when the data set has been registered to the database. */
    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserEMail(String userEMail)
    {
        this.userEMail = userEMail;
    }

    public String getUserEMail()
    {
        return userEMail;
    }

    /** Returns <code>fileFormatType</code>. */
    public final FileFormatType getFileFormatType()
    {
        return fileFormatType;
    }

    /** Sets <code>fileFormatType</code>. */
    public final void setFileFormatType(final FileFormatType fileFormatType)
    {
        this.fileFormatType = fileFormatType;
    }

    /** Returns <code>locatorType</code>. */
    public final LocatorType getLocatorType()
    {
        return locatorType;
    }

    /** Sets <code>locatorType</code>. */
    public final void setLocatorType(final LocatorType locatorType)
    {
        this.locatorType = locatorType;
    }

    /**
     * Returns {@link BooleanOrUnknown#T}, if the data set is complete in the data store and {@link BooleanOrUnknown#F}, if some parts of the data are
     * missing. If the completeness is not known (e.g. because the data set is stored in a format that does not allow to assess the completeness,
     * {@link BooleanOrUnknown#U} is returned.
     */
    public final BooleanOrUnknown getComplete()
    {
        return complete;
    }

    /**
     * Sets whether this data set is complete in the data store or not. The default is {@link BooleanOrUnknown#U}, which corresponds to the case where
     * the data are stored in a format that does not allow to assess completeness.
     */
    public final void setComplete(final BooleanOrUnknown complete)
    {
        this.complete = complete;
    }

    public String getAssociatedSampleCode()
    {
        return associatedSampleCode;
    }

    public void setAssociatedSampleCode(final String sampleCode)
    {
        this.associatedSampleCode = sampleCode;
    }

    public final String getDataStoreCode()
    {
        return dataStoreCode;
    }

    public final void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public final boolean isMeasured()
    {
        return measured;
    }

    public final void setMeasured(boolean measured)
    {
        this.measured = measured;
    }

    public final String getCode()
    {
        return extractableData.getCode();
    }

    public String getDataProducerCode()
    {
        return extractableData.getDataProducerCode();
    }

    public final List<String> getParentDataSetCodes()
    {
        return extractableData.getParentDataSetCodes();
    }

    public Date getProductionDate()
    {
        return extractableData.getProductionDate();
    }

    public final void setCode(String code)
    {
        extractableData.setCode(code);
    }

    public void setDataProducerCode(String dataProducerCode)
    {
        extractableData.setDataProducerCode(dataProducerCode);
    }

    public final void setParentDataSetCodes(List<String> parentDataSetCodes)
    {
        extractableData.setParentDataSetCodes(parentDataSetCodes);
    }

    public void setProductionDate(Date productionDate)
    {
        extractableData.setProductionDate(productionDate);
    }

    public List<NewProperty> getDataSetProperties()
    {
        return extractableData.getDataSetProperties();
    }

    public void setDataSetProperties(List<NewProperty> dataSetProperties)
    {
        extractableData.setDataSetProperties(dataSetProperties);
    }

    public ExtractableData getExtractableData()
    {
        return extractableData;
    }

    public void setExtractableData(ExtractableData extractableData)
    {
        this.extractableData = extractableData;
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

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof NewExternalData == false)
        {
            return false;
        }
        final NewExternalData that = (NewExternalData) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.location, location);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(location);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        builder.append("type", getDataSetType());
        builder.append("kind", getDataSetKind());
        builder.append("fileFormat", getFileFormatType());
        builder.append("properties", getDataSetProperties());
        appendMore(builder);
        return builder.toString();
    }
    
    protected void appendMore(ToStringBuilder builder)
    {
    }

}
