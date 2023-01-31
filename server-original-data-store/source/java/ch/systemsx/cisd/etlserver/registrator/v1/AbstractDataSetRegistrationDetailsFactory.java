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

package ch.systemsx.cisd.etlserver.registrator.v1;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDataSetRegistrationDetailsFactory<T extends DataSetInformation>
        implements IDataSetRegistrationDetailsFactory<T>
{
    protected final OmniscientTopLevelDataSetRegistratorState registratorState;

    protected final DataSetInformation userProvidedDataSetInformationOrNull;

    public AbstractDataSetRegistrationDetailsFactory(
            OmniscientTopLevelDataSetRegistratorState registratorState,
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        this.registratorState = registratorState;
        this.userProvidedDataSetInformationOrNull = userProvidedDataSetInformationOrNull;
    }

    @Override
    public String getUserIdOrNull()
    {
        return (null == this.userProvidedDataSetInformationOrNull) ? null
                : this.userProvidedDataSetInformationOrNull.getUploadingUserIdOrNull();
    }

    /**
     * Factory method that creates a new registration details object.
     */
    @Override
    public DataSetRegistrationDetails<T> createDataSetRegistrationDetails()
    {
        DataSetRegistrationDetails<T> registrationDetails = new DataSetRegistrationDetails<T>();
        T dataSetInfo = createDataSetInformation();
        if (null != userProvidedDataSetInformationOrNull)
        {
            applyUserProvidedValues(dataSetInfo);
            dataSetInfo.setUploadingUserId(userProvidedDataSetInformationOrNull
                    .getUploadingUserIdOrNull());
        }
        if (null == dataSetInfo.getDataSetType())
        {
            setDataSetTypeToDefaultValue(dataSetInfo);
        }
        if (null == dataSetInfo.getDataSetKind())
        {
            setDataSetKindToDefaultValue(dataSetInfo);
        }
        setDatabaseInstance(dataSetInfo);
        registrationDetails.setDataSetInformation(dataSetInfo);
        return registrationDetails;
    }

    @Override
    public DataSet<T> createDataSet(DataSetRegistrationDetails<T> registrationDetails,
            File stagingFile)
    {
        IEncapsulatedOpenBISService service = registratorState.getGlobalState().getOpenBisService();
        return new DataSet<T>(registrationDetails, stagingFile, service);
    }

    /**
     * The field userProvidedDataSetInformationOrNull is non-null. Apply the values to the dataSetInfo. Subclasses may override.
     */
    protected void applyUserProvidedValues(T dataSetInfo)
    {
        SampleIdentifier sampleId = userProvidedDataSetInformationOrNull.getSampleIdentifier();
        if (null != sampleId)
        {
            dataSetInfo.setSampleIdentifier(sampleId);
        }

        if (null != userProvidedDataSetInformationOrNull.tryToGetSample())
        {
            dataSetInfo.setSample(userProvidedDataSetInformationOrNull.tryToGetSample());
        }

        ExperimentIdentifier experimentId =
                userProvidedDataSetInformationOrNull.getExperimentIdentifier();
        if (null != experimentId)
        {
            dataSetInfo.setExperimentIdentifier(experimentId);
        }

        if (null != userProvidedDataSetInformationOrNull.tryToGetExperiment())
        {
            dataSetInfo.setExperiment(userProvidedDataSetInformationOrNull.tryToGetExperiment());
        }

        DataSetType type = userProvidedDataSetInformationOrNull.getDataSetType();
        if (null != type)
        {
            dataSetInfo.setDataSetType(type);
        }

        DataSetKind kind = userProvidedDataSetInformationOrNull.getDataSetKind();
        if (null != kind)
        {
            dataSetInfo.setDataSetKind(kind);
        }

        List<NewProperty> props = userProvidedDataSetInformationOrNull.getDataSetProperties();
        if (false == props.isEmpty())
        {
            dataSetInfo.setDataSetProperties(props);
        }

        if (userProvidedDataSetInformationOrNull.getParentDataSetCodes() != null)
        {
            dataSetInfo.setParentDataSetCodes(userProvidedDataSetInformationOrNull
                    .getParentDataSetCodes());
        }
    }

    /**
     * Set the data set type value to the default value. Subclasses may override.
     */
    protected void setDataSetTypeToDefaultValue(T dataSetInfo)
    {
        dataSetInfo.setDataSetType(new DataSetType(DataSetTypeCode.UNKNOWN.getCode()));
    }

    protected void setDataSetKindToDefaultValue(T dataSetInfo)
    {
        dataSetInfo.setDataSetKind(DataSetKind.PHYSICAL);
    }

    protected final void setDatabaseInstance(DataSetInformation dataSetInfo)
    {
        dataSetInfo.setInstanceUUID(registratorState.getHomeDatabaseInstance().getUuid());
    }

    /**
     * Factory method that creates a new data set information object.
     */
    protected abstract T createDataSetInformation();

}
