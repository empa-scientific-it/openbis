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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * A generic class that represents a data set for the registration API. Can be subclassed.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSet<T extends DataSetInformation> extends AbstractDataSetImmutable implements
        IDataSet
{
    private final DataSetRegistrationDetails<? extends T> registrationDetails;

    // A folder with either one file or one subfolder representing the data set.
    private final File dataSetFolder;

    private IExperimentImmutable experiment;

    private ISampleImmutable sampleOrNull;

    private IExternalDataManagementSystemImmutable externalDataManagementSystemOrNull;

    public DataSet(DataSetRegistrationDetails<? extends T> registrationDetails, File dataSetFolder,
            IEncapsulatedOpenBISService service)
    {
        super(service);
        this.registrationDetails = registrationDetails;
        this.dataSetFolder = dataSetFolder;
    }

    public DataSetRegistrationDetails<? extends T> getRegistrationDetails()
    {
        return registrationDetails;
    }

    public File getDataSetStagingFolder()
    {
        return dataSetFolder;
    }

    public File tryDataSetContents()
    {
        // How the contents are handled depends on whether this is a container data set or not.
        File[] contents = dataSetFolder.listFiles();
        if (isNoFileDataSet())
        {
            if (contents.length > 0)
            {
                throw new IllegalArgumentException(
                        "A data set of no-file-content type cannot contain files. The data set specification is invalid: "
                                + registrationDetails.getDataSetInformation());
            }
            return null;
        } else
        {
            if (contents.length > 1)
            {
                throw new IllegalArgumentException(
                        "Data set is ambiguous -- there are more than one potential candidates. transaction.moveFile() might have been called several times:"
                                + Arrays.toString(contents));
            }
            if (contents.length < 1)
            {
                throw new IllegalArgumentException("Data set (" + dataSetFolder.getAbsolutePath() + ") is empty. transaction.moveFile() might have been ommitted: "
                        + registrationDetails.getDataSetInformation());
            }
            return contents[0];
        }

    }

    @Override
    public String getDataSetCode()
    {
        return registrationDetails.getDataSetInformation().getDataSetCode();
    }

    @Override
    public IObjectId getEntityId()
    {
        return new DataSetCodeId(getDataSetCode());
    }

    @Override
    public IExperimentImmutable getExperiment()
    {
        if (experiment != null)
        {
            return experiment;
        } else if (sampleOrNull != null)
        {
            return sampleOrNull.getExperiment();
        }
        return null;
    }

    @Override
    public void setExperiment(IExperimentImmutable experiment)
    {
        this.experiment = experiment;
        ExperimentImmutable exp = (ExperimentImmutable) experiment;
        Experiment experimentToSet = (exp != null) ? exp.getExperiment() : null;
        setExperiment(experimentToSet);
    }

    @Override
    public ISampleImmutable getSample()
    {
        return sampleOrNull;
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        this.sampleOrNull = sampleOrNull;

        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();
        if (sampleOrNull == null)
        {
            dataSetInformation.setSample(null);
            dataSetInformation.setSampleCode(null);
        } else
        {
            SampleImmutable sample = (SampleImmutable) sampleOrNull;

            Sample sampleDTO = sample.getSample();
            dataSetInformation.setSample(sampleDTO);
            dataSetInformation.setSampleIdentifier(sampleDTO.getIdentifier());

            if (sampleDTO.getExperiment() != null)
            {
                setExperiment(sampleDTO.getExperiment());
            }
        }
    }

    @Override
    public String getFileFormatType()
    {
        return registrationDetails.getFileFormatType().getCode();
    }

    @Override
    public void setFileFormatType(String fileFormatTypeCode)
    {
        registrationDetails.setFileFormatType(new FileFormatType(fileFormatTypeCode));
    }

    @Override
    public boolean isMeasuredData()
    {
        return registrationDetails.isMeasuredData();
    }

    @Override
    public void setMeasuredData(boolean measuredData)
    {
        registrationDetails.setMeasuredData(measuredData);
    }

    @Override
    public int getSpeedHint()
    {
        return registrationDetails.getDataSetInformation().getSpeedHint();
    }

    @Override
    public void setSpeedHint(int speedHint)
    {
        registrationDetails.getDataSetInformation().setSpeedHint(speedHint);
    }

    @Override
    public String getDataSetType()
    {
        return registrationDetails.getDataSetType().getCode();
    }

    @Override
    public DataSetKind getDataSetKind()
    {
        return registrationDetails.getDataSetKind();
    }

    @Override
    public DataSetType getDataSetTypeWithPropertyTypes()
    {
        String dataSetTypeCode = getDataSetType();
        if (dataSetTypeCode == null)
        {
            throw new UserFailureException(
                    "Unkown data set type. Data set type code has to be set "
                            + "before invoking getDataSetTypeWithPropertyTypes().");
        }
        return getDataSetTypeWithPropertyTypes(dataSetTypeCode);
    }

    @Override
    public void setDataSetType(String dataSetTypeCode)
    {
        registrationDetails.setDataSetType(dataSetTypeCode);
    }

    @Override
    public void setDataSetKind(DataSetKind dataSetKind)
    {
        registrationDetails.setDataSetKind(dataSetKind);
    }

    protected void setExperiment(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment exp)
    {
        registrationDetails.getDataSetInformation().setExperiment(exp);
        ExperimentIdentifier experimentId =
                (exp != null && exp.getIdentifier() != null) ? ExperimentIdentifierFactory
                        .parse(exp.getIdentifier()) : null;
        registrationDetails.getDataSetInformation().setExperimentIdentifier(experimentId);
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return registrationDetails.getPropertyValue(propertyCode);
    }

    @Override
    public List<String> getAllPropertyCodes()
    {
        List<NewProperty> properties =
                registrationDetails.getDataSetInformation().getExtractableData()
                        .getDataSetProperties();
        List<String> codes = new ArrayList<String>();
        for (NewProperty property : properties)
        {
            codes.add(property.getPropertyCode());
        }
        return codes;
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        registrationDetails.setPropertyValue(propertyCode, propertyValue);
    }

    @Override
    public void setParentDatasets(List<String> parentDatasetCodes)
    {
        DataSetInformation dataSetInformation = registrationDetails.getDataSetInformation();
        dataSetInformation.setParentDataSetCodes(parentDatasetCodes);
    }

    @Override
    public List<String> getParentDatasets()
    {
        return registrationDetails.getDataSetInformation().getParentDataSetCodes();
    }

    @Override
    public boolean isContainerDataSet()
    {
        return registrationDetails.getDataSetInformation().isContainerDataSet();
    }

    @Override
    public List<String> getContainedDataSetCodes()
    {
        return Collections.unmodifiableList(registrationDetails.getDataSetInformation()
                .getContainedDataSetCodes());
    }

    @Override
    public void setContainedDataSetCodes(List<String> containedDataSetCodes)
    {
        ArrayList<String> newContainedDataSetCodes =
                (null == containedDataSetCodes) ? new ArrayList<String>()
                        : new ArrayList<String>(
                                containedDataSetCodes);
        registrationDetails.getDataSetInformation().setContainedDataSetCodes(
                newContainedDataSetCodes);

    }

    @Override
    public List<IDataSetImmutable> getChildrenDataSets()
    {
        throw new UnsupportedOperationException("The operation is not supported for data "
                + "sets not existing prior the transaction start.");
    }

    // the dataset cannot be contained before it is created
    @Override
    public boolean isContainedDataSet()
    {
        return false;
    }

    @Override
    public String getContainerDataSet()
    {
        return null;
    }

    @Override
    public List<String> getContainerDataSets()
    {
        return null;
    }

    @Override
    public Integer getOrderInContainer(String containerDataSetCode)
    {
        return null;
    }

    @Override
    public void setExternalDataManagementSystem(
            IExternalDataManagementSystemImmutable externalDataManagementSystem)
    {
        this.externalDataManagementSystemOrNull = externalDataManagementSystem;
        String codeToSet =
                (externalDataManagementSystem != null) ? externalDataManagementSystem.getCode()
                        : null;

        this.registrationDetails.getDataSetInformation().setExternalDataManagementSystem(codeToSet);
    }

    @Override
    public boolean isLinkDataSet()
    {
        return registrationDetails.getDataSetInformation().isLinkDataSet();
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem()
    {
        return externalDataManagementSystemOrNull;
    }

    @Override
    public void setExternalCode(String externalCode)
    {
        this.registrationDetails.getDataSetInformation().setExternalCode(externalCode);
    }

    @Override
    public String getExternalCode()
    {
        return this.registrationDetails.getDataSetInformation().getExternalCode();
    }

    @Override
    public boolean isNoFileDataSet()
    {
        return isContainerDataSet() || isLinkDataSet();
    }

    @Override
    public boolean isPostRegistered()
    {
        return false;
    }
}
