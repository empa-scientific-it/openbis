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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.time.DateFormatThreadLocal;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SpeedUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ToStringUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExtractableData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Container class for data extracted from the data set directory.
 * 
 * @author Bernd Rinn
 */
public class DataSetInformation implements Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    private String sampleCode;

    // top sample properties
    private IEntityProperty[] sampleProperties = IEntityProperty.EMPTY_ARRAY;

    private DataSetType dataSetType;

    private DataSetKind dataSetKind;

    private int speedHint = ch.systemsx.cisd.openbis.generic.shared.Constants.DEFAULT_SPEED_HINT;

    private String shareId;

    /**
     * The database instance <i>UUID</i>.
     */
    private String instanceUUID;

    private String projectCode;
    
    private String spaceCode;

    /** An object that uniquely identifies the experiment. Can be <code>null</code>. */
    private ExperimentIdentifier experimentIdentifier;

    /** sample with properties, enriched with connected experiment with properties. */
    private transient Sample sample;

    private transient boolean linkSample = true;

    private transient Experiment experiment;

    private BooleanOrUnknown isCompleteFlag = BooleanOrUnknown.U;

    private List<String> containedDataSetCodes = new ArrayList<String>();

    private String externalDataManagementSystem;

    private String externalCode;

    /**
     * A subset of {@link NewExternalData} which gets set by the code extractor.
     * <p>
     * Initialized with <code>new ExtractableData()</code>.
     * </p>
     */
    private ExtractableData extractableData = new ExtractableData();

    /**
     * Email of uploading user.
     */
    private String uploadingUserEmailOrNull;

    private String uploadingUserIdOrNull;

    private String containerDatasetPermIdOrNull;

    private AbstractExternalData containerDataSetOrNull;

    /** This constructor is for serialization. */
    public DataSetInformation()
    {
    }

    public String getShareId()
    {
        return shareId;
    }

    public void setShareId(String shareId)
    {
        this.shareId = shareId;
    }

    public int getSpeedHint()
    {
        return speedHint;
    }

    public void setSpeedHint(int speedHint)
    {
        this.speedHint = SpeedUtils.trimSpeedHint(speedHint);
    }

    public String tryGetUploadingUserEmail()
    {
        return uploadingUserEmailOrNull;
    }

    public void setUploadingUserEmail(String uploadingUserEmail)
    {
        this.uploadingUserEmailOrNull = uploadingUserEmail;
    }

    public void setUploadingUserId(String uploadingUserIdOrNull)
    {
        this.uploadingUserIdOrNull = uploadingUserIdOrNull;
    }

    public String getUploadingUserIdOrNull()
    {
        return uploadingUserIdOrNull;
    }

    public final BooleanOrUnknown getIsCompleteFlag()
    {
        return isCompleteFlag;
    }

    public final void setComplete(final boolean complete)
    {
        isCompleteFlag = BooleanOrUnknown.resolve(complete);
    }

    /**
     * Returns the sample properties.
     * 
     * @return never <code>null</code> but could return an empty array.
     */
    public final IEntityProperty[] getSampleProperties()
    {
        return sampleProperties == null ? new IEntityProperty[0] : sampleProperties;
    }

    public final void setSampleProperties(final IEntityProperty[] properties)
    {
        this.sampleProperties = properties;
    }

    public DataSetType getDataSetType()
    {
        return dataSetType;
    }

    public void setDataSetType(DataSetType dataSetType)
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

	public final String getInstanceUUID()
    {
        return instanceUUID;
    }

    public final void setInstanceUUID(final String instanceUUID)
    {
        this.instanceUUID = instanceUUID;
    }

    /** Sets <code>experimentIdentifier</code>. */
    public final void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    /**
     * Returns the identifier of experiment which makes it unique.
     * 
     * @return <code>null</code> if no <code>ExperimentIdentifier</code> has been set.
     */
    public final ExperimentIdentifier getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    /**
     * Returns the basic information about the experiment.
     */
    // It can be null when we use e.g. CifexDataSetInfoExtractor - only identifier is set.
    public Experiment tryToGetExperiment()
    {
        return experiment == null ? (sample == null ? null : sample.getExperiment()) : experiment;
    }

    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    public Sample tryToGetSample()
    {
        return sample;
    }

    public void setSample(final Sample sample)
    {
        this.sample = sample;
    }

    /**
     * Returns the sample identifier.
     * 
     * @return <code>null</code> if <code>sampleCode</code> has not been set.
     */
    public final SampleIdentifier getSampleIdentifier()
    {
        if (sampleCode == null)
        {
            return null;
        }
        if (projectCode != null)
        {
            return new SampleIdentifier(new ProjectIdentifier(spaceCode, projectCode), sampleCode);
        }
        if (spaceCode == null)
        {
            return new SampleIdentifier(sampleCode);
        }
        return new SampleIdentifier(new SpaceIdentifier(spaceCode),
                sampleCode);
    }

    /**
     * Sets the sample identifier.
     */
    public final void setSampleIdentifier(SampleIdentifier sampleIdentifierOrNull)
    {
        if (sampleIdentifierOrNull != null)
        {
            setSampleCode(sampleIdentifierOrNull.getSampleCode());
            if (sampleIdentifierOrNull.isProjectLevel())
            {
                setProjectCode(sampleIdentifierOrNull.getProjectLevel().getProjectCode());
                setSpaceCode(sampleIdentifierOrNull.getProjectLevel().getSpaceCode());
            } else
            {
                final SpaceIdentifier spaceLevel = sampleIdentifierOrNull.getSpaceLevel();
                if (spaceLevel != null)
                {
                    setSpaceCode(spaceLevel.getSpaceCode());
                }
            }
        }
    }

    public final void setSampleIdentifier(String sampleIdentifier)
    {
        setSampleIdentifier(SampleIdentifierFactory.parse(sampleIdentifier));
    }

    public final String getSampleCode()
    {
        return sampleCode;
    }

    public final void setSampleCode(final String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public boolean isLinkSample()
    {
        return linkSample;
    }

    public void setLinkSample(boolean linkSample)
    {
        this.linkSample = linkSample;
    }

    public final String getDataSetCode()
    {
        return extractableData.getCode();
    }

    public final void setDataSetCode(final String dataSetCode)
    {
        extractableData.setCode(dataSetCode);
    }

    public final String getProducerCode()
    {
        return extractableData.getDataProducerCode();
    }

    public final void setProducerCode(final String producerCode)
    {
        extractableData.setDataProducerCode(producerCode);
    }

    public final Date getProductionDate()
    {
        return extractableData.getProductionDate();
    }

    public final void setProductionDate(final Date productionDate)
    {
        extractableData.setProductionDate(productionDate);
    }

    public final ExtractableData getExtractableData()
    {
        return extractableData;
    }

    public final void setExtractableData(final ExtractableData extractableData)
    {
        this.extractableData = extractableData;
    }

    public final List<String> getParentDataSetCodes()
    {
        return extractableData.getParentDataSetCodes();
    }

    public final void setParentDataSetCodes(List<String> parentDataSetCodes)
    {
        extractableData.setParentDataSetCodes(parentDataSetCodes);
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public final void setSpaceCode(final String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public final String getSpaceCode()
    {
        return spaceCode;
    }

    public void setDataSetProperties(List<NewProperty> dataSetProperties)
    {
        extractableData.setDataSetProperties(dataSetProperties);
    }

    public List<NewProperty> getDataSetProperties()
    {
        return extractableData.getDataSetProperties();
    }

    public List<String> getContainedDataSetCodes()
    {
        return containedDataSetCodes;
    }

    public void setContainedDataSetCodes(List<String> containedDataSetCodes)
    {
        this.containedDataSetCodes = containedDataSetCodes;
    }

    public boolean isContainerDataSet()
    {
        // This doesn't work because the dataSetType is not always retrieved from openBIS.
        // return (dataSetType == null) ? (false == containedDataSetCodes.isEmpty()) : dataSetType
        // .isContainerType();
        return false == containedDataSetCodes.isEmpty();
    }

    public void setExternalDataManagementSystem(final String code)
    {
        this.externalDataManagementSystem = code;
    }

    public String getExternalDataManagementSystem()
    {
        return this.externalDataManagementSystem;
    }

    public boolean isLinkDataSet()
    {
        return this.externalDataManagementSystem != null;
    }

    public boolean isNoFileDataSet()
    {
        return isLinkDataSet() || isContainerDataSet();
    }

    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    public final String describe()
    {
        if (experimentIdentifier == null)
        {
            return String.format("CODE('%s') SAMPLE_CODE('%s')", extractableData.getCode(),
                    sampleCode);
        } else
        {
            return String.format("CODE('%s') SAMPLE_CODE('%s') EXPERIMENT('%s')",
                    extractableData.getCode(), sampleCode, experimentIdentifier.describe());
        }
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        String userID = getUploadingUserIdOrNull();
        String userEMail = tryGetUploadingUserEmail();
        if (userID != null || userEMail != null)
        {
            appendNameAndObject(buffer, "User", userID == null ? userEMail : userID);
        }
        appendNameAndObject(buffer, "Data Set Code", getDataSetCode());
        if (null != getDataSetType())
        {
            appendNameAndObject(buffer, "Data Set Type", getDataSetType().getCode());
        } else
        {
            appendNameAndObject(buffer, "Data Set Type", "null");
        }
        if (null != getDataSetKind())
        {
            appendNameAndObject(buffer, "Data Set Kind", getDataSetKind().name());
        } else
        {
            appendNameAndObject(buffer, "Data Set Kind", "null");
        }        appendNameAndObject(buffer, "Experiment Identifier", getExperimentIdentifier());
        appendNameAndObject(buffer, "Sample Identifier", getSampleIdentifier());
        if (StringUtils.isBlank(getProducerCode()) == false)
        {
            appendNameAndObject(buffer, "Producer Code", getProducerCode());
        }
        if (getProductionDate() != null)
        {
            appendNameAndObject(buffer, "Production Date", formatDate(getProductionDate()));
        }
        final List<String> parentDataSetCodes = getParentDataSetCodes();
        if (parentDataSetCodes.isEmpty() == false)
        {
            appendNameAndObject(buffer, "Parent Data Sets",
                    StringUtils.join(parentDataSetCodes, ' '));
        }
        appendNameAndObject(buffer, "Is complete", getIsCompleteFlag());
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    public String tryGetContainerDatasetPermId()
    {
        return containerDatasetPermIdOrNull;
    }

    public void setContainerDatasetPermId(String containerDatasetPermIdOrNull)
    {
        this.containerDatasetPermIdOrNull = containerDatasetPermIdOrNull;
    }

    public AbstractExternalData tryGetContainerDataSet()
    {
        return containerDataSetOrNull;
    }

    public void setContainerDataSet(AbstractExternalData containerDataSetOrNull)
    {
        this.containerDataSetOrNull = containerDataSetOrNull;
    }

    private static String formatDate(Date productionDate)
    {
        return productionDate == null ? "" : DateFormatThreadLocal.DATE_FORMAT.get().format(productionDate);
    }

    protected static final void appendNameAndObject(final StringBuilder buffer, final String name,
            final Object object)
    {
        ToStringUtil.appendNameAndObject(buffer, name, object);
    }
}